package io.rtr.jsonapi.filter;

import io.rtr.jsonapi.annotation.ApiModel;
import io.rtr.jsonapi.filter.mapping.ResourceMappingContext;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.google.common.collect.Maps;

public class ApiPropertyFilter implements PropertyFilter {
	private static final Logger log = LoggerFactory.getLogger(ApiPropertyFilter.class);
	
	private Map<Class<?>, Set<String>> filtered = Maps.newHashMap();
	private Map<String, Set<String>> filtersByModelType = Maps.newHashMap();
	
	
	private ResourceMappingContext mapping;
	public ApiPropertyFilter(ResourceMappingContext mapping) {
		this.mapping = mapping;
	}
	
	public ApiPropertyFilter(ResourceMappingContext mapping, Map<String, Set<String>> filters) {
		this.mapping = mapping;	
		this.filtersByModelType = filters;
		for (Set<String> values : filtersByModelType.values()) {
			values.add("id");
			values.add("type");
		}
	}
	
	@Override
	public void serializeAsField(Object pojo, JsonGenerator jgen,
			SerializerProvider prov, PropertyWriter writer) throws Exception {
		if (pojo == null) {
			return;
		}
		
		final Class<?> declaringClass = pojo.getClass();
		final String modelType = getModelType(declaringClass);
		final Set<String> filteredFields = filtersByModelType.get(modelType);
		if (filteredFields != null && !filteredFields.contains(writer.getName())) {
			log.debug("filtered {}.{}", modelType, writer.getName());
			return;
		}
		writer.serializeAsField(pojo, jgen, prov);		
	}
	
	private String getModelType(Class<?> type) {
		ApiModel model = type.getAnnotation(ApiModel.class);
		if (model != null) {
			String modelType = model.type();
			if ("undefined".equals(modelType)) {
				modelType = model.value();
			}
			return modelType;
		}
		return null;
	}

	@Override
	public void serializeAsElement(Object elementValue, JsonGenerator jgen,
			SerializerProvider prov, PropertyWriter writer) throws Exception {
		writer.serializeAsElement(elementValue, jgen, prov);	
	}

	@Override
	public void depositSchemaProperty(PropertyWriter writer,
			ObjectNode propertiesNode, SerializerProvider provider)
			throws JsonMappingException {
	}

	@Override
	public void depositSchemaProperty(PropertyWriter writer,
			JsonObjectFormatVisitor objectVisitor, SerializerProvider provider)
			throws JsonMappingException {
	}

}
