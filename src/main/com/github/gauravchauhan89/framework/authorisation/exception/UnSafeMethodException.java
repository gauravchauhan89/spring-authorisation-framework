package com.github.gauravchauhan89.framework.authorisation.exception;

/**
 * Http method 'GET' and 'HEAD' are safe.
 * https://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html
 *
 * Created by gaurav on 15/02/17.
 */
public class UnSafeMethodException extends Exception {

  public UnSafeMethodException(String message) {
    super(message);
  }
}
