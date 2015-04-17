package io.rtr.jsonapi.filter;

import io.rtr.jsonapi.JSONAPI;
import io.rtr.jsonapi.JSONAPI.ApiDocumentBuilder;
import io.rtr.jsonapi.JSONAPI.ResourceObjectBuilder;
import io.rtr.jsonapi.annotation.ResourceObject;
import io.rtr.jsonapi.filter.ResourceMappingContext.Mapping;
import io.rtr.jsonapi.impl.ResourceObjectImpl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.uri.UriTemplate;

import com.google.common.collect.Lists;

public class JsonApiResponseFilter implements ContainerResponseFilter {
	private static final MediaType JSONAPI_MEDIATYPE = MediaType.valueOf("application/vnd.api+json");
	
	@Context
	private UriInfo uriInfo;
	
	@Context
	private ResourceMappingContext resourceMapping;
	
	@Override
	public void filter(ContainerRequestContext requestContext,
			ContainerResponseContext responseContext) throws IOException {
		if (!isApplicable(requestContext)) {
			System.out.println("JSON API not requested");
			return;
		}
		System.out.println("HANDLING JSON API");
		
		Object entity = responseContext.getEntity();
		if (entity != null && !uriInfo.getMatchedResources().isEmpty()) {
			List<Object> resources = uriInfo.getMatchedResources();
			Object resource = resources.get(0);
			Mapping mapping = resourceMapping.getMapping(resource.getClass());
			System.out.println(mapping);
			

			if (entity.getClass().isArray()) {
				Object[] entityArray = (Object[])entity;
				List<ResourceObjectImpl<Object>> resourceObjects = Lists.newArrayList();
				for (Object obj : entityArray) {
					ResourceObject resourceAnnotation = obj.getClass().getDeclaredAnnotation(ResourceObject.class);
					Mapping m = mapping;
					if (resourceAnnotation != null) {
						try {
							m = resourceMapping.getMapping(Class.forName(resourceAnnotation.resource()));
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					}
					resourceObjects.add(buildEntity(m, obj));
				}
				
				ApiDocumentBuilder<?> docBuilder = JSONAPI.document(resourceObjects);
				docBuilder.link("self", uriInfo.getRequestUri().toString());
				responseContext.setEntity(docBuilder.build(uriInfo));
			} else if (Collection.class.isAssignableFrom(entity.getClass())) {
				Collection<?> entityCollection = (Collection<?>)entity;
				List<ResourceObjectImpl<Object>> resourceObjects = Lists.newArrayList();
				for (Object obj : entityCollection) {
					ResourceObject resourceAnnotation = obj.getClass().getDeclaredAnnotation(ResourceObject.class);
					Mapping m = mapping;
					if (resourceAnnotation != null) {
						try {
							m = resourceMapping.getMapping(Class.forName(resourceAnnotation.resource()));
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					}
					resourceObjects.add(buildEntity(m, obj));
				}
			
				ApiDocumentBuilder<?> docBuilder = JSONAPI.document(resourceObjects);
				docBuilder.link("self", uriInfo.getRequestUri().toString());
				responseContext.setEntity(docBuilder.build(uriInfo));
			} else {
				Mapping m = mapping;
				ResourceObject resourceAnnotation = entity.getClass().getDeclaredAnnotation(ResourceObject.class);
				if (resourceAnnotation != null) {
					try {
						m = resourceMapping.getMapping(Class.forName(resourceAnnotation.resource()));
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
				ResourceObjectImpl<?> data = buildEntity(m, entity);
				ApiDocumentBuilder docBuilder = JSONAPI.document(data);
				docBuilder.link("self", uriInfo.getRequestUri().toString());
				responseContext.setEntity(docBuilder.build(uriInfo));
			}
		}
	}
	
	private ResourceObjectImpl buildEntity(Mapping mapping, Object entity) {
		ResourceObjectBuilder dataBuilder = JSONAPI.data(entity);
		for (String key : mapping.getKeys()) {
			UriTemplate template = new UriTemplate(mapping.getPathTemplate(key));
			String uri = template.createURI(getId(entity));
			dataBuilder.link(key, uriInfo.getBaseUri().resolve(uri.substring(1)).toString());
		}
		ResourceObjectImpl<?> data = dataBuilder.build();
		return data;
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
	
	private void processLinks() {
		List<Object> resources = uriInfo.getMatchedResources();
		Object resource = resources.get(0);
		resource.getClass().getAnnotation(Path.class);

	}

}
