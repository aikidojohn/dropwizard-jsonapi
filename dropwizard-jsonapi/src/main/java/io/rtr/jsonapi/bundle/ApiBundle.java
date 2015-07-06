package io.rtr.jsonapi.bundle;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.rtr.jsonapi.annotation.FieldFilterMixIn;
import io.rtr.jsonapi.filter.JsonApiFeature;
import io.rtr.jsonapi.filter.JsonApiMessageBodyWriter;

public class ApiBundle implements Bundle {

	@Override
	public void initialize(Bootstrap<?> bootstrap) {
		bootstrap.getObjectMapper().addMixInAnnotations(Object.class, FieldFilterMixIn.class);
		bootstrap.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
	}

	@Override
	public void run(Environment environment) {
		environment.jersey().packages("io.rtr.jsonapi.filter");
		environment.jersey().register(JsonApiFeature.class);
		//Is this even necessary? It only adds the mixin that's added above.
		environment.jersey().register(JsonApiMessageBodyWriter.class);
	}

}
