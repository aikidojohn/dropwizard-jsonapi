package io.rtr.jsonapi.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Link {
	public String name();
	public String uri();
	
	public boolean include() default false;
}
