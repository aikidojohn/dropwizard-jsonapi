package io.rtr.jsonapi.filter;

import io.rtr.jsonapi.annotation.ApiModel;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

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
import com.google.common.collect.Sets;

public class ApiPropertyFilter implements PropertyFilter {

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
		if (writer instanceof BeanPropertyWriter) {
			final BeanPropertyWriter w = (BeanPropertyWriter)writer;
			final AnnotatedMember member = w.getMember();
			final Class<?> declaringClass = member.getDeclaringClass();
			final String modelType = getModelType(declaringClass);
			final Set<String> filteredFields = filtersByModelType.get(modelType);
			if (filteredFields != null && !filteredFields.contains(w.getName())) {
				System.out.println("filtered " + modelType + "." + w.getName());
				return;
			}
		}
		writer.serializeAsField(pojo, jgen, prov);		
	}
	
	private String getModelType(Class<?> type) {
		ApiModel model = type.getDeclaredAnnotation(ApiModel.class);
		if (model != null) {
			return model.value();
		}
		return null;
	}

	@Override
	public void serializeAsElement(Object elementValue, JsonGenerator jgen,
			SerializerProvider prov, PropertyWriter writer) throws Exception {
		System.out.println("Element: " + writer.getFullName().toString());
		writer.serializeAsElement(elementValue, jgen, prov);	
	}

	@Override
	public void depositSchemaProperty(PropertyWriter writer,
			ObjectNode propertiesNode, SerializerProvider provider)
			throws JsonMappingException {
		System.out.println("Deposit Schema:" + writer);
		
	}

	@Override
	public void depositSchemaProperty(PropertyWriter writer,
			JsonObjectFormatVisitor objectVisitor, SerializerProvider provider)
			throws JsonMappingException {
		System.out.println("Deposit Schema2:" + writer);
		
	}

}
