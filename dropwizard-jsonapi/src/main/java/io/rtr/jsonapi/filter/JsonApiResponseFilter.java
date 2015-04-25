package io.rtr.jsonapi.filter;

import io.rtr.jsonapi.JSONAPI;
import io.rtr.jsonapi.JSONAPI.ApiDocumentBuilder;
import io.rtr.jsonapi.JSONAPI.ResourceObjectBuilder;
import io.rtr.jsonapi.filter.ResourceMappingContext.Mapping;
import io.rtr.jsonapi.impl.ResourceObjectImpl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.message.internal.OutboundJaxrsResponse;
import org.glassfish.jersey.uri.UriTemplate;

import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterInjector;
import com.google.common.collect.Lists;

public class JsonApiResponseFilter implements ContainerResponseFilter {
	private static final MediaType JSONAPI_MEDIATYPE = MediaType.valueOf("application/vnd.api+json");
	
	@Context
	private UriInfo uriInfo;
	
	@Context
	private ResourceMappingContext resourceMapping;
	
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		if (!isApplicable(requestContext)) {
			System.out.println("JSON API not requested");
			return;
		}
		System.out.println("HANDLING JSON API");
		//Necessary for field filtering
		ObjectWriterInjector.set(new FilteredObjectWriterModifier(uriInfo, resourceMapping));
		
		final Object entity = responseContext.getEntity();
		if (entity != null && !uriInfo.getMatchedResources().isEmpty()) {
			List<Object> resources = uriInfo.getMatchedResources();
			Object resource = resources.get(0);
			Mapping requestResourceMapping= resourceMapping.getMapping(resource.getClass());

			List<Object> inculdeObjects = Lists.newArrayList();
			List<String> includeKeys = Lists.newArrayList();
			if (uriInfo.getQueryParameters().containsKey("include")) {
				includeKeys.addAll(uriInfo.getQueryParameters().get("include"));
			}
			
			ApiDocumentBuilder<Object> docBuilder = null;
			if (entity.getClass().isArray()) {
				final Object[] entityArray = (Object[])entity;
				final List<ResourceObjectImpl<Object>> resourceObjects = buildEntityList(Arrays.stream(entityArray), requestResourceMapping);

				docBuilder = JSONAPI.document(resourceObjects);
			} 
			else if (Collection.class.isAssignableFrom(entity.getClass())) {
				final Collection<?> entityCollection = (Collection<?>)entity;
				final List<ResourceObjectImpl<Object>> resourceObjects = buildEntityList(entityCollection.stream(), requestResourceMapping);
			
				docBuilder = JSONAPI.document(resourceObjects);
			} 
			else {
				final ResourceObjectImpl<Object> data = buildEntity(requestResourceMapping, entity, resource, includeKeys, inculdeObjects);
				docBuilder = JSONAPI.document(data);
			}
			
			docBuilder.link("self", uriInfo.getRequestUri().toString());
			for (Object inc : inculdeObjects) {
				docBuilder.include(buildEntity(requestResourceMapping, inc));
			}
			responseContext.setEntity(docBuilder.build(uriInfo));
		}
	}
	
	private ResourceObjectImpl buildEntity(final Mapping mapping, final Object entity, final Object resource, Collection<String> includeKeys, List<Object> includes) {
		final Mapping entityMapping = resourceMapping.getMappingByModel(entity.getClass());
		Mapping m = mapping;
		if (entityMapping != null) {
			m = entityMapping;
		}
		
		final ResourceObjectBuilder dataBuilder = JSONAPI.data(entity);
		if (m != null) {
			for (String key : m.getKeys()) {
				UriTemplate template = new UriTemplate(m.getPathTemplate(key));
				String id = getId(entity);
				if (id != null) {
					String uri = template.createURI(id);
					dataBuilder.link(key, uriInfo.getBaseUri().resolve(uri.substring(1)).toString());
					if (includeKeys.contains(key)) {
						includes.addAll(resolveIncludes(resource, entity, id, key, m));
					}
				}
			}
		}
		return dataBuilder.build();
	}
	
	private List<Object> resolveIncludes(Object resource, Object entity, String id, String key, Mapping m) {
		List<Object> includes = Lists.newArrayList();
		try {
			Object included = m.getPathMethod(key).invoke(resource, id);
			if (included instanceof OutboundJaxrsResponse) {
				included = ((OutboundJaxrsResponse)included).getEntity();
			} 
			System.out.println("Possible Include: " + key + " - " + included);
			
			if (included.getClass().isArray()) {
				includes.addAll(Lists.newArrayList((Object[])included));
			}
			else if (Collection.class.isAssignableFrom(included.getClass())) {
				includes.addAll((Collection)included);
			}
			else {
				includes.add(included);
			}
			
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return includes;
	}
	
	private ResourceObjectImpl buildEntity(final Mapping mapping, final Object entity) {
		final Mapping entityMapping = resourceMapping.getMappingByModel(entity.getClass());
		Mapping m = mapping;
		if (entityMapping != null) {
			m = entityMapping;
		}
		
		final ResourceObjectBuilder dataBuilder = JSONAPI.data(entity);
		if (m != null) {
			for (String key : m.getKeys()) {
				UriTemplate template = new UriTemplate(m.getPathTemplate(key));
				String id = getId(entity);
				if (id != null) {
					String uri = template.createURI(id);
					dataBuilder.link(key, uriInfo.getBaseUri().resolve(uri.substring(1)).toString());
				}
			}
		}
		return dataBuilder.build();
	}
	
	private List<ResourceObjectImpl<Object>> buildEntityList(Stream<?> source, Mapping requestResourceMapping) {
		final List<ResourceObjectImpl<Object>> resourceObjects = Lists.newArrayList();
		source.forEach(obj -> {
			resourceObjects.add(buildEntity(requestResourceMapping, obj));
		});
		return resourceObjects;
	}
	
	private String getId(Object obj) {
		try {
			Field field = obj.getClass().getDeclaredField("id");
			field.setAccessible(true);
			Object id = field.get(obj);
			if (id instanceof String) {
				return (String)id;
			}
			return String.valueOf(id);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private boolean isApplicable(ContainerRequestContext requestContext) {
		return requestContext.getAcceptableMediaTypes()
				.stream()
				.anyMatch(m -> JSONAPI_MEDIATYPE.equals(m));
	}
}
