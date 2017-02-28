package com.github.gauravchauhan89.framework.authorisation.impl;

import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "roles")
public class RoleDTO {
  @Id
  private String id;

  private String name;
  private String parentName;
  private List<String> permissions;
  private List<BusinessObjectRuleDTO> businessObjectRules;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

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

  public List<String> getPermissions() {
    return permissions;
  }

  public void setPermissions(List<String> permissions) {
    this.permissions = permissions;
  }

  public List<BusinessObjectRuleDTO> getBusinessObjectRules() {
    return businessObjectRules;
  }

  public void setBusinessObjectRules(
      List<BusinessObjectRuleDTO> businessObjectRules) {
    this.businessObjectRules = businessObjectRules;
  }

  public static class BusinessObjectRuleDTO {
    private String ruleName;
    List<String> arguments;

    public String getRuleName() {
      return ruleName;
    }

    public void setRuleName(String ruleName) {
      this.ruleName = ruleName;
    }

    public List<String> getArguments() {
      return arguments;
    }

    public void setArguments(List<String> arguments) {
      this.arguments = arguments;
    }

    @Override
    public String toString() {
      return "BusinessObjectRuleDTO{" +
          "ruleName='" + ruleName + '\'' +
          ", arguments=" + arguments +
          '}';
    }
  }

  @Override
  public String toString() {
    return "RoleDTO{" +
        "id='" + id + '\'' +
        ", name='" + name + '\'' +
        ", permissions=" + permissions +
        ", businessObjectRules=" + businessObjectRules +
        '}';
  }
}
