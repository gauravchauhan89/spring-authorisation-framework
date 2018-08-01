package com.github.gauravchauhan89.framework.authorisation;

import com.github.gauravchauhan89.framework.authorisation.exception.*;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import java.lang.Object;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Aspect
@Component
public class AuthorisationAspect {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Value("${messages.authorisation.authorizationFailure:Authorisation Failure}")
    private String authorizationFailureMessage;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private RoleService roleService;

    /**
     * Advice for permission check. Authenticated User is fetched from org.springframework.security.core.Authentication.getPrincipal().
     * And list of roles are fetched using roleService and SecurityContextHolder.getContext().getAuthentication().getAuthorities()
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("within(@org.springframework.web.bind.annotation.RestController *) && @annotation(in.airtel.b2b.troubleshoot.services.authorisation.Permission)")
    public Object beforeSampleCreation(ProceedingJoinPoint joinPoint) throws Throwable {
        List<Role> roles = new ArrayList<Role>();
        Object user = null;
        if(SecurityContextHolder.getContext().getAuthentication() != null
            && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<? extends GrantedAuthority> authorities = (List<? extends GrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();
            roles.addAll(authorities.stream()
                .map(authority -> {LOGGER.debug("Roles: {}", roleService.getRoles()); return roleService.getRoles().get(authority.getAuthority());})
                .collect(Collectors.toList()));
        }

        // check for permission validation
        return checkPermissions(joinPoint, user, roles);
    }

    private Object checkPermissions(ProceedingJoinPoint joinPoint, Object user, List<Role> roles) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<? extends BasePermission>[] permissionClasses = signature.getMethod().getAnnotation(Permission.class).permission();

        AuthorisationException authorisationException = null;
        boolean returnValueAvailable = false;
        Object businessObject = null;
        List<List<BusinessObjectRule>> rulesToBeEvaluate = new ArrayList<>();
        List<List<List<String>>> rulesArguments = new ArrayList<>();
        for (Role role : roles) {
            for (Class<? extends BasePermission> permissionClass : permissionClasses) {
                BasePermission permission = context.getBean(permissionClass);
                if (role.getUserPermissions().contains(permission)) {
                    LOGGER.info("Required Permission: {}", permissionClass);
                    RequestObject requestObject = getRequestObject(joinPoint);
                    LOGGER.info("RequestObject: {}", requestObject.toString());
                    try {
                        // validate permission
                        if (permission.isAuthorised(user, requestObject)) {
                            LOGGER.info("Permission Valid. Now checking {} additional rules.",
                                role.getRules().size());
                            if (role.getRules().size() > 0) {
                                if(businessObject == null) {
                                    if (permission.useReturnValueAsBusinessObject()) {
                                        if (!isMethodSafe(joinPoint)) {
                                            throw new UnSafeMethodException(
                                                "Method whose return value you are trying to use "
                                                    + "as business object, is not safe. Please modify "
                                                    + permission.getClass().getName()
                                                    + " to return business object.");
                                        }
                                        businessObject = joinPoint.proceed();
                                        returnValueAvailable = true;
                                    } else {
                                        businessObject = permission
                                            .getBusinessObject(requestObject);
                                    }
                                    if (businessObject == null) {
                                        throw new NullPointerException(
                                            "Business object cannot be null. Please modify "
                                                + permission.getClass().getName()
                                                + " to return business object.");
                                    }
                                } else {
                                    if(!permission.useReturnValueAsBusinessObject()) {
                                        Object ruleBusinessObject = permission.getBusinessObject(requestObject);
                                        if(!ruleBusinessObject.getClass().equals(businessObject.getClass())) {
                                            throw new AuthorisationException(
                                                "Permissions on same method should return same business object");
                                        }
                                    }
                                }
                                rulesToBeEvaluate.add(role.getRules());
                                rulesArguments.add(role.getArguments());
                            }
                        }
                    } catch (BeansException ex) {
                        LOGGER.error("Exception in getting permission/rule class bean", ex);
                        throw new AuthorisationException("Cannot get permission/rule class bean");
                    } catch (AuthorisationException ex) {
                        LOGGER.info("authorisation failure: {}", ex.getMessage());
                        authorisationException = ex;
                    }
                }
            }
        }
        if (roles.size() > 0 && validateRules(user, businessObject, rulesToBeEvaluate, rulesArguments)) {
            if (returnValueAvailable) {
                return businessObject;
            } else {
                return joinPoint.proceed();
            }
        }
        if(authorisationException != null) {
            throw new AuthorisationException(authorisationException.getMessage());
        }
        throw new AuthorisationException(authorizationFailureMessage);
    }

    private boolean validateRules(Object user, Object businessObjects, List<List<BusinessObjectRule>> rules, List<List<List<String>>> args) throws Exception {
        if(rules.size() == 0) {
            return true;
        }
        // Collection check
        if(businessObjects instanceof Collection) {
            return validateCollectionBusinessObject(user, businessObjects, rules, args);
        } else {
            int i=0;
            for(List<BusinessObjectRule> ruleList : rules) {
                if (checkRules(user, businessObjects, ruleList, args.get(i))) {
                    return true;
                }
                i++;
            }
            return false;
        }
    }

    private boolean validateCollectionBusinessObject(Object user, Object businessObjects, List<List<BusinessObjectRule>> rules, List<List<List<String>>> args) throws Exception {
        Collection collection = (Collection) businessObjects;
        if(collection.size() == 0) {
            return true;
        }
        int validObjects = 0;
        for (Object businessObject : collection) {
            int i=0;
            for(List<BusinessObjectRule> ruleList : rules) {
                if (checkRules(user, businessObject, ruleList, args.get(i))) {
                    validObjects++;
                    break;
                }
                i++;
            }
        }
        if(validObjects == collection.size()) {
            return true;
        }
        return false;
    }

    /**
     * User might contain multiple roles. Only all additional rules from one role should pass.
     *
     * @param user
     * @param businessObject
     * @param args
     * @return boolean
     * @throws Throwable
     */
    private boolean checkRules(Object user, Object businessObject, List<BusinessObjectRule> businessRules, List<List<String>> args) throws Exception {
        if (businessRules != null && businessRules.size() > 0) {
            for (int j = 0; j < businessRules.size(); j++) {
                BusinessObjectRule rule = businessRules.get(j);
                if (!rule.validate(user, businessObject, args.get(j))) {
                    LOGGER.info("Rule failed: {}", rule.getClass().getSimpleName());
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Extract request parameters
     *
     * @param joinPoint
     * @return
     */
    private RequestObject getRequestObject(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Map<String, Object> queryParameters = new HashMap<String, Object>();
        Map<String, Object> pathParameters = new HashMap<String, Object>();
        Object requestBody = null;
        for (int i = 0; i < signature.getMethod().getParameterCount(); i++) {
            for (Annotation parameterAnn : signature.getMethod()
                .getParameterAnnotations()[i]) {
                if (parameterAnn.annotationType().equals(RequestParam.class)) {
                    RequestParam ann = (RequestParam) parameterAnn;
                    LOGGER.info("Annotation : {}", ann);
                    queryParameters.put(ann.value(), joinPoint.getArgs()[i]);
                } else if (parameterAnn.annotationType().equals(PathVariable.class)) {
                    PathVariable ann = (PathVariable) parameterAnn;
                    LOGGER.info("Annotation : {}", ann);
                    pathParameters.put(ann.value(), joinPoint.getArgs()[i]);
                } else if (parameterAnn.annotationType().equals(RequestBody.class)) {
                    requestBody = joinPoint.getArgs()[i];
                }
            }
        }

        return new RequestObject(queryParameters, pathParameters,
            requestBody);
    }

    /**
     * Checks that Http Method is either GET or HEAD
     *
     * @param joinPoint
     * @return
     */
    private boolean isMethodSafe(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RequestMapping request = signature.getMethod().getAnnotation(RequestMapping.class);
        List<RequestMethod> httpMethods = Arrays.asList(request.method());

        if(httpMethods.size() == 1) {
            if(httpMethods.contains(RequestMethod.GET) || httpMethods.contains(RequestMethod.HEAD)) {
                return true;
            }
        } else if (httpMethods.size() == 2) {
            if(httpMethods.contains(RequestMethod.GET) && httpMethods.contains(RequestMethod.HEAD)) {
                return true;
            }
        }
        return false;
    }
}