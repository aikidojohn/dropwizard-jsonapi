package io.rtr.dropwizard.jsonapi.sample;

import io.dropwizard.setup.Environment;

import com.google.inject.AbstractModule;

public class SampleModule extends AbstractModule {
	private final SampleConfiguration config;
	private final Environment env;
	public SampleModule(SampleConfiguration config, Environment env) {
		this.config = config;
		this.env = env;
	}
	@Override
	protected void configure() {
		bind(SampleConfiguration.class).toInstance(config);
		bind(Environment.class).toInstance(env);
	}

}
