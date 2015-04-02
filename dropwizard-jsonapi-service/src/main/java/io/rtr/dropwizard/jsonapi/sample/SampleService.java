package io.rtr.dropwizard.jsonapi.sample;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.rtr.dropwizard.jsonapi.sample.health.SampleHealthCheck;
import io.rtr.dropwizard.jsonapi.sample.resources.SampleResource;

import com.codahale.metrics.health.HealthCheck;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class SampleService extends Application<SampleConfiguration>{

	private final Class<?>[] HEALTH_CHECKS = { SampleHealthCheck.class };
	private final Class<?>[] RESOURCES = { SampleResource.class};
	private final Class<?>[] MANAGED = { };
	
	@Override
	public String getName() {
		return "Dropwizard JSONAP Sample";
	}
	@Override
	public void initialize(Bootstrap<SampleConfiguration> bootstrap) {
		bootstrap.addBundle(new AssetsBundle("/assets/", "/"));
	}

	@Override
	public void run(SampleConfiguration configuration, Environment environment) throws Exception {
		Injector injector = Guice.createInjector(new SampleModule(configuration, environment));
		
		for (Class<?> c : MANAGED) {
			environment.jersey().register((Managed)injector.getInstance(c));
		}
		for (Class<?> c : RESOURCES) {
			environment.jersey().register(injector.getInstance(c));
		}
		for (Class<?> c : HEALTH_CHECKS) {
			environment.healthChecks().register(c.getName(), (HealthCheck)injector.getInstance(c));
		}
	}
	
	public static void main(String[] args) throws Exception {
		new SampleService().run(args);
	}

}
