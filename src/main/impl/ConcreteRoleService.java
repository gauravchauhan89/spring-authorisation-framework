package com.github.gauravchauhan89.framework.authorisation.impl;

import com.github.gauravchauhan89.framework.authorisation.impl.RoleDTO.BusinessObjectRuleDTO;
import com.github.gauravchauhan89.framework.authorisation.BasePermission;
import com.github.gauravchauhan89.framework.authorisation.BusinessObjectRule;
import com.github.gauravchauhan89.framework.authorisation.Role;
import com.github.gauravchauhan89.framework.authorisation.RoleService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.eclipse.jetty.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * This is a sample implementation of RoleService, which fetched roles from Mongodb.
 *
 * Created by gaurav on 15/02/17.
 */
//@Service("RoleService")
public class ConcreteRoleService implements RoleService {

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private ApplicationContext context;

  private Map<String, Role> roleMap = new HashMap<String, Role>();

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @PostConstruct
  public void initialiseRoleMap() throws Exception {
    try {
      List<RoleDTO> roleDTOs = roleRepository.findAll();
      logger.info("roleDTOs: {}", roleDTOs);
      for (RoleDTO roleDTO : roleDTOs) {
        Role role = new Role();
        role.setName(roleDTO.getName());
        role.setParentName(roleDTO.getParentName());
        List<String> permissions = roleDTO.getPermissions();
        if(permissions != null) {
          for (String permissionName : permissions) {
            role.getUserPermissions().add((BasePermission) context.getBean(permissionName));
          }
        }

        List<BusinessObjectRuleDTO> businessObjectRuleDTOs = roleDTO.getBusinessObjectRules();
        if(businessObjectRuleDTOs != null) {
          for (BusinessObjectRuleDTO businessObjectRuleDTO : businessObjectRuleDTOs) {
            role.getRules().add((BusinessObjectRule) context.getBean(businessObjectRuleDTO.getRuleName()));
            role.getArguments().add(businessObjectRuleDTO.getArguments());
          }
        }

        roleMap.put(role.getName(), role);
      }

      extendParentRoles();

      logger.info("roleMap: {}", roleMap);
    }catch(Exception ex) {
      logger.error("Exception in initialiseRoleMap", ex);
      throw ex;
    }
  }

  private void extendParentRoles() throws Exception {
    for(Role role : roleMap.values()) {
      mergeParentRole(role);
    }
  }

  private void mergeParentRole(Role role) throws Exception {
    if(StringUtil.isNotBlank(role.getParentName())) {
      if(roleMap.containsKey(role.getParentName())) {
        Role parentRole = roleMap.get(role.getParentName());
        if(StringUtil.isBlank(parentRole.getParentName())) {
          role.getUserPermissions().addAll(parentRole.getUserPermissions());
        } else {
          mergeParentRole(parentRole);
        }
      } else {
        throw new Exception("impl.ConcreteRoleService: " +role.getParentName()+" is not valid role.");
      }
    }
  }

  @Override
  public Map<String, Role> getRoles() {
    return roleMap;
  }

  public List<Role> getAnonymousRole() {
    List<Role> roles = new ArrayList<Role>();
    roles.add(roleMap.get("AnonymousUser"));
    return roles;
  }
}
