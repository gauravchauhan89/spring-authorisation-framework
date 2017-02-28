package com.github.gauravchauhan89.framework.authorisation.exception;

/**
 * To be thrown by child classes of BasePermission for sending authorization error message to user.
 * Only set message which can be shown to API/Service user, do not set debug messages here.
 *
 * Created by gaurav on 15/02/17.
 */
public class AuthorisationException extends Exception {

  public AuthorisationException(String message) {
    super(message);
  }
}
