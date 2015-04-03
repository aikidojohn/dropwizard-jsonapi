package io.rtr.jsonapi;

import java.util.Map;

public interface ResourceObject {
	String getId();
	String getType();
	
	//Links and Meta
	default Map<String, Object> getLinks() {
		return null;
	}
	
	default Object getMeta() {
		return null;
	}
}
