package com.github.gauravchauhan89.framework.authorisation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by gaurav on 09/02/17.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Permission {

    /**
     * Multiple permissions mean any one should satisfy. It is like ORing the permissions
     *
     * @return
     */
    Class<? extends BasePermission>[] permission();
}
