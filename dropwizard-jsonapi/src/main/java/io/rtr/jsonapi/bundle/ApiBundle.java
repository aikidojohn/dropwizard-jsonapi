package io.rtr.jsonapi.bundle;

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
	}

	@Override
	public void run(Environment environment) {
		environment.jersey().packages("io.rtr.jsonapi.filter");
		environment.jersey().register(JsonApiFeature.class);
		environment.jersey().register(JsonApiMessageBodyWriter.class);
	}

}
