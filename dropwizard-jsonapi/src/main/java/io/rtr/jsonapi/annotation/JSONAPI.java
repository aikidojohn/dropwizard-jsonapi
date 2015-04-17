package io.rtr.jsonapi.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;

@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
public @interface JSONAPI {

}
