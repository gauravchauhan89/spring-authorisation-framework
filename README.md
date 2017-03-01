# Spring Web Authorisation Framework
This framework is based on http://www9.org/w9-papers/EC-Security/153.pdf.
It breaks the problem of Authorisation into 3 constructs: __permissions__, __roles__ and __users__ (4th one is __groups__, which is not yet supported).

### Table of Contents
1. [Getting started](#getting-started)
2. [Concepts](#concepts)
3. [How to use?](#how-to-use)

## Getting started
1. Download `build/lib/spring-authorisation-framework-0.0.1.jar` and all dependency from `build.gradle`.
2. [Setup spring-security authentication.](#setting-up-spring-security-authentication)
3. Implement `RoleService` or use `src/main/impl/ConcreteRoleService.java` with mongodb.
4. Create child classes of `BasePermission`.
5. Annotate controller's method with `@Permission`.

## Concepts

### Permission
Permission is the authority needed to perform an action. It translates to sub-class of `BasePermission`.
Permission is provided with 3 objects:

1. `Authenticated User` : User that has been authenticated by spring-security authentication.
2. `Transactional Object` : All query parameters, path parameters and request body of http request.Use appropriate spring annotations ([RequestParam][1], [PathVariable][2] and [RequestBody][3]) for this to work.
3. `Business Object` : This is basically the resource url is pointing to (think like REST resource).It is the responsibility of permission class to fetch this resource. So, all permission classes should implement `BasePermission.getBusinessObject()`. Instead, `BasePermission.useReturnValueAsBusinessObject()` can used if Http Method is [safe][4] i.e `GET` or `HEAD` .

Permission contains validation rules for all these objects.


### Role
Role is collection of permissions with some additional [`BusinessObjectRule`](#businessobjectrule)


### Group
Group is collection of roles. Groups are not yet supported.

## How to use?

This framework works in conjunction with spring-security.

#### Setting up spring security authentication
`SecurityContextHolder.getContext().getAuthentication().getPrincipal()` should return authenticated user
class object and `SecurityContextHolder.getContext().getAuthentication().getAuthorities()`
should return `List<? extends GrantedAuthority>` of authenticated user.

`RoleService.getRoles().get(GrantedAuthority.getAuthority())` will be used to
get actual `Role` object. How you set these values, is up to you.

#### Implementing RoleService
A sample implementation of RoleService is provided
in `src/main/impl/ConcreteRoleService` which fetches roles from mongodb.

Sample configuration from database :
```
{
	"_id" : ObjectId("58a435cbc5e5637d317d2d81"),
	"name" : "User",
	"permissions" : [
		"ViewUserPermission"
	]
}
{
	"_id" : ObjectId("58a582bc85078ec046b7fa06"),
	"name" : "AnonymousUser",
	"permissions" : [
		"CreateUserPermission"
	]
}
```

#### Creating permission class
Next, create a permission class, like below :
```
@Component("ViewAllUsersPermission")
public class ViewAllUsersPermission extends BasePermission {

  @Override
  public boolean isAuthorised(Object authenticatedUser, RequestObject requestObject)
      throws AuthorisationException {
    AuthenticatedUser user = (AuthenticatedUser) authenticatedUser;
    if(user.isAdmin()) {
      return true;
    }

    return false;
  }

  @Override
  public boolean useReturnValueAsBusinessObject() {
    return true;
  }
}
```

#### Annotate controller's methods
Next annotate `RestController`'s `RequestMapping` method, which requires this permission with `@Permission`, like below:
```
@RestController
public class UsersController {
    @Permission(permission = {ViewAllUsersPermission.class})
    @RequestMapping(value="/users", produces={"application/json"}, method = {RequestMethod.GET})
    public List<User> home() {
        logger.info("Got call for users");
        return userService.getAllUsers();
    }
}
```
This should be it.

Now, when a authenticated user logs in, she/he will only be able to access this method if it has required
permission and permission evaluates to true.

---

####Multiple permissions on Method:
As, you may have noticed, `permission` in `@Permission(permission = {ViewAllUsersPermission.class})`
is an array. So, multiple permissions can be entered. User having either one of them will be allowed
to proceed. It is like ORing the permissions. Support for ANDing is not there (can be added later).

####Multiple Roles:
User can have multiple roles.

####BusinessObjectRule
Role can contain additional business object rules, which basically are constraints on business object
in addition to permission. If parent role has some BusinessObjectRule rules, those will be replaced
by child's rules. Example configuration :
```
{
	"_id" : ObjectId("58ac19c0e8fe35e7961a735f"),
	"name" : "Google User",
	"parent" : "User",
	"businessObjectRules" : [
		{
			"ruleName" : "OrganisationConstraintRule",
			"arguments" : ["google"]
		}
	]
}
```

In this example, User having `Google User` role, will have all the permissions from `User` role. Additionally, business object rule named
`OrganisationConstraintRule` will impose restriction that user can access business objects
of `google` only. If it tries to access business object of any other organisation, it will not be authorised.

```
@Component("OrganisationConstraintRule")
public class OrganisationConstraintRule implements BusinessObjectRule {

  @Override
  public boolean validate(Object authenticatedUser, Object businessObject, List<String> args) throws Exception {
    AuthenticatedUser authenticatedUser1 = (AuthenticatedUser) authenticatedUser;
    if(businessObject instanceof User) {
      User user = (User) businessObject;
      if(args.contains(user.getCompany())) {
        return true;
      }
    } else {
      throw new ServerErrorException("OrganisationConstraintRule: Unknown business object type");
    }

    return false;
  }
}
```

There is scope of a lot of improvements, specially for handling of `BusinessObjectRule`. If you have any idea/suggestion or want to contribute, please shoot a mail at gaurav.the.iiitian@gmail.com.

[1]: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/RequestParam.html
[2]: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/PathVariable.html
[3]: http://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/RequestBody.html
[4]: https://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html
