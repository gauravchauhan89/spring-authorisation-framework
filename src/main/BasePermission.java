package com.github.gauravchauhan89.framework.authorisation;

import com.github.gauravchauhan89.framework.authorisation.exception.AuthorisationException;
/**
 * Interface for all permission classes. Implementing classes should be declared as spring bean
 */
public abstract class BasePermission {

    /**
     * Validate all transactional parameters (query parameter, request body) and business Objects
     * @param authenticatedUser
     * @param requestObject
     * @return
     * @throws AuthorisationException if want to show any exception message to user
     */
    public abstract boolean isAuthorised(Object authenticatedUser, RequestObject requestObject) throws AuthorisationException;

    /**
     *  This is needed to impose additional business object rules on Roles. Role is collection of
     *  permissions but it might contain additional rules on business object. Therefore, all
     *  permission classes should provide a way to get business object from RequestObject.
     *
     *  If useReturnValueAsBusinessObject() returns true, this method is not used.
     * @param requestObject
     * @return
     */
    public Object getBusinessObject(RequestObject requestObject) throws Exception {
        return null;
    };

    /**
     * If the http method is safe (GET or HEAD), return value of protected function call can also be used as businessObject.
     *
     * https://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html
     */
    public boolean useReturnValueAsBusinessObject() {
        return false;
    }
}
