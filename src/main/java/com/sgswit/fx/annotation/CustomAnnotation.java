package com.sgswit.fx.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author DeZh
 * @title: CustomAnnotation
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/12/2214:46
 */
@Target({ElementType.TYPE,ElementType.METHOD,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomAnnotation {
    /**是否可复制**/
    public boolean copy() default true;
    /**属性描述**/
    public String desc() default "";
}
