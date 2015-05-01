package io.rtr.jsonapi.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.jaxrs.cfg.EndpointConfigBase;
import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterModifier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class FilteredObjectWriterModifier extends ObjectWriterModifier {

	private UriInfo uriInfo;
	private ResourceMappingContext mapping;
	
	public FilteredObjectWriterModifier(UriInfo info, ResourceMappingContext mapping) {
		this.uriInfo = info;
		this.mapping = mapping;
	}
	
	private Map<String, Set<String>> getFieldFilters() {
		Map<String, Set<String>> filters = Maps.newHashMap();
		for (String param : uriInfo.getQueryParameters().keySet()) {
			if (param.startsWith("fields")) {
				int start = param.indexOf('[');
				int end = param.indexOf(']');
				String modelType = param.substring(start+1, end);
				System.out.println("model: " + modelType);
				Set<String> fieldFilters = filters.get(modelType);
				if (fieldFilters == null) {
					fieldFilters = Sets.newHashSet();
					filters.put(modelType, fieldFilters);
				}
				for (String field : uriInfo.getQueryParameters().get(param)) {
					String[] modelFields = field.split(",");
					System.out.println("model fields: " + Arrays.toString(modelFields));
					fieldFilters.addAll(Sets.newHashSet(modelFields));
				}
			}
		}
		return filters;
	}
	
	@Override
	public ObjectWriter modify(EndpointConfigBase<?> endpoint,
			MultivaluedMap<String, Object> responseHeaders,
			Object valueToWrite, ObjectWriter w, JsonGenerator g)
			throws IOException {
		Class<?> mixinClass = w.getConfig().findMixInClassFor(valueToWrite.getClass());
		System.out.println("Found Mixin: " + mixinClass);
		if (uriInfo != null) {
			System.out.println ("!!!!MODIFIED!!!!!!");
			
			Map<String, Set<String>> modelFilters = getFieldFilters();
			System.out.println("Parsed Filters: " + modelFilters);
			if (modelFilters != null && !modelFilters.isEmpty()) {
				//Set<String> allFields = Sets.newHashSet(fields);
				//allFields.addAll(Sets.newHashSet("data", "type", "id", "links", "self", "included"));
				System.out.println("Adding Field Filter");
				FilterProvider filters = new SimpleFilterProvider()
			      .addFilter("FieldFilter", new ApiPropertyFilter(mapping, modelFilters));
				  //.addFilter("FieldFilter", SimpleBeanPropertyFilter.filterOutAllExcept(allFields));
				return w.with(filters);
			}
		}
		
		FilterProvider filters = new SimpleFilterProvider()
	      .addFilter("FieldFilter", SimpleBeanPropertyFilter.serializeAllExcept(new String[]{}));  
		return w.with(filters);
	}
	
}