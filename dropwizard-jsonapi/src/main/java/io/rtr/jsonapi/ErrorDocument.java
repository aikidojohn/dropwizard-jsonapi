package io.rtr.jsonapi;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

public class ErrorDocument {
	private List<Error> errors;

	public ErrorDocument(Error ...errors) {
		this.errors = Lists.newArrayList(errors);
	}

	public List<Error> getErrors() {
		return Collections.unmodifiableList(this.errors);
	}
}
