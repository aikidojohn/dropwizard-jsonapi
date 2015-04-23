package io.rtr.jsonapi.filter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import jersey.repackaged.com.google.common.collect.Sets;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.jaxrs.cfg.EndpointConfigBase;
import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterModifier;

public class FilteredObjectWriterModifier extends ObjectWriterModifier {

	private UriInfo uriInfo;
	public FilteredObjectWriterModifier(UriInfo info) {
		this.uriInfo = info;
	}
	
	@Override
	public ObjectWriter modify(EndpointConfigBase<?> endpoint,
			MultivaluedMap<String, Object> responseHeaders,
			Object valueToWrite, ObjectWriter w, JsonGenerator g)
			throws IOException {
		Class<?> mixinClass = w.getConfig().findMixInClassFor(Object.class);
		System.out.println(mixinClass);
		if (uriInfo != null) {
			List<String> fields = uriInfo.getQueryParameters().get("fields");
			System.out.println ("!!!!MODIFIED!!!!!!");
			System.out.println(fields);
			if (fields != null && !fields.isEmpty()) {
				Set<String> allFields = Sets.newHashSet(fields);
				allFields.addAll(Sets.newHashSet("data", "type", "id", "links", "self"));
				FilterProvider filters = new SimpleFilterProvider()
			      .addFilter("FieldFilter", SimpleBeanPropertyFilter.filterOutAllExcept(allFields));  
				return w.with(filters);
			}
		}
		
		FilterProvider filters = new SimpleFilterProvider()
	      .addFilter("FieldFilter", SimpleBeanPropertyFilter.serializeAllExcept(new String[]{}));  
		return w.with(filters);
	}
	
}