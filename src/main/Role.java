package com.github.gauravchauhan89.framework.authorisation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Role is a collection of permissions and businessObjectRules
 *
 * Created by gaurav on 15/02/17.
 */
public class Role {
  private String name;
  private String parentName;
  private Set<BasePermission> userPermissions = new HashSet<BasePermission>();
  private List<BusinessObjectRule> rules = new ArrayList<BusinessObjectRule>();
  /**
   * These arguments will provided to BusinessObjectRule.validate function along with businessObject
   */
  private List<List<String>> arguments = new ArrayList<List<String>>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getParentName() {
    return parentName;
  }

  public void setParentName(String parentName) {
    this.parentName = parentName;
  }

  public Set<BasePermission> getUserPermissions() {
    return userPermissions;
  }

  public void setUserPermissions(
      Set<BasePermission> userPermissions) {
    this.userPermissions = userPermissions;
  }

  public List<BusinessObjectRule> getRules() {
    return rules;
  }

  public void setRules(
      List<BusinessObjectRule> rules) {
    this.rules = rules;
  }

  public List<List<String>> getArguments() {
    return arguments;
  }

  public void setArguments(List<List<String>> arguments) {
    this.arguments = arguments;
  }
}
