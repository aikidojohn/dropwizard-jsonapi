package io.rtr.jsonapi;

import java.util.List;
import java.util.Map;

public interface ApiDocument<D, M> {
	D getData();
	
	default List<Object> getIncluded() {
		return null;
	}
	
	default Map<String, Object> getLinks() {
		return null;
	}
	
	default M getMeta() {
		return null;
	}
}
