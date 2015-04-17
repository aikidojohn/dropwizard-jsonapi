package io.rtr.dropwizard.jsonapi.sample;

import io.dropwizard.setup.Environment;
import io.rtr.dropwizard.jsonapi.sample.core.DataStore;

import javax.inject.Singleton;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class SampleModule extends AbstractBinder {
	private final SampleConfiguration config;
	private final Environment env;
	public SampleModule(SampleConfiguration config, Environment env) {
		this.config = config;
		this.env = env;
	}
	@Override
	protected void configure() {
		bindAsContract(DataStore.class).in(Singleton.class);
	}

}
