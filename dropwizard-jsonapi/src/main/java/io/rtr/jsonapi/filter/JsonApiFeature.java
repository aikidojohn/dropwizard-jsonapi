package io.rtr.jsonapi.filter;

import io.rtr.jsonapi.filter.mapping.ResourceMappingContext;

import javax.inject.Singleton;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class JsonApiFeature implements Feature {

	@Override
	public boolean configure(FeatureContext context) {
		Configuration config = context.getConfiguration();
        if (!config.isRegistered(JsonApiResponseFilter.class)) {
        	context.register(new AbstractBinder() {

                @Override
                protected void configure() {
                    bindAsContract(ResourceMappingContext.class).in(Singleton.class);
                }
            });
        	
        	context.register(JsonApiResponseFilter.class);
        	return true;
        }
		return false;
	}

}
