package io.rtr.dropwizard.jsonapi.sample;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.rtr.dropwizard.jsonapi.sample.health.SampleHealthCheck;
import io.rtr.dropwizard.jsonapi.sample.resources.ArticlesResource;
import io.rtr.dropwizard.jsonapi.sample.resources.PeopleResource;
import io.rtr.dropwizard.jsonapi.sample.resources.SampleResource;
import io.rtr.jsonapi.bundle.ApiBundle;

public class SampleService extends Application<SampleConfiguration> {

	private final Class<?>[] RESOURCES = { SampleResource.class, ArticlesResource.class, PeopleResource.class};
	private final Class<?>[] MANAGED = { };
	
	@Override
	public String getName() {
		return "Dropwizard JSONAP Sample";
	}
	@Override
	public void initialize(Bootstrap<SampleConfiguration> bootstrap) {
		//bootstrap.addBundle(new AssetsBundle("/assets/", "/"));
		bootstrap.addBundle(new ApiBundle());
	}

	@Override
	public void run(SampleConfiguration configuration, Environment environment) throws Exception {
		environment.jersey().register(new SampleModule(configuration, environment));
		
		for (Class<?> c : MANAGED) {
			environment.jersey().register(c);
		}
		for (Class<?> c : RESOURCES) {
			environment.jersey().register(c);
		}
		
		SampleHealthCheck health = new SampleHealthCheck();
		environment.healthChecks().register(SampleHealthCheck.class.getName(), health);
	}
	
	public static void main(String[] args) throws Exception {
		new SampleService().run(args);
	}

}
