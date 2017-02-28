package com.github.gauravchauhan89.framework.authorisation;

import java.util.Map;

/**
 * Fetch roles from datasource
 *
 * Created by gaurav on 15/02/17.
 */
public interface RoleService {
  Map<String, Role> getRoles();
}
