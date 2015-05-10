package io.rtr.jsonapi.filter;

import com.google.common.annotations.VisibleForTesting;
import io.rtr.jsonapi.JSONAPI;
import io.rtr.jsonapi.JSONAPI.ApiDocumentBuilder;
import io.rtr.jsonapi.JSONAPI.ResourceObjectBuilder;
import io.rtr.jsonapi.JsonLink;
import io.rtr.jsonapi.Linkage;
import io.rtr.jsonapi.annotation.ApiModel;
import io.rtr.jsonapi.filter.mapping.ResourceMappingContext;
import io.rtr.jsonapi.filter.mapping.ResourceMappingContext.Mapping;
import io.rtr.jsonapi.impl.ResourceObjectImpl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.uri.UriTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterInjector;
import com.google.common.collect.Lists;

public class JsonApiResponseFilter implements ContainerResponseFilter {
	private static final Logger log = LoggerFactory.getLogger(JsonApiResponseFilter.class);
	public static final MediaType JSONAPI_MEDIATYPE = MediaType.valueOf("application/vnd.api+json");
	
	@Context
	private UriInfo uriInfo;
	
	@Context
	private ResourceMappingContext resourceMapping;
	
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		//Necessary for field filtering
		ObjectWriterInjector.set(new FilteredObjectWriterModifier(uriInfo, resourceMapping));
				
		if (!isApplicable(requestContext)) {
			log.trace("JSON API not requested");
			return;
		}
		log.trace("HANDLING JSON API");
		
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
				final List<ResourceObjectImpl<Object>> resourceObjects = buildEntityList(Arrays.stream(entityArray), requestResourceMapping,  resource, includeKeys, inculdeObjects);

				docBuilder = JSONAPI.document(resourceObjects);
			} 
			else if (Collection.class.isAssignableFrom(entity.getClass())) {
				final Collection<?> entityCollection = (Collection<?>)entity;
				final List<ResourceObjectImpl<Object>> resourceObjects = buildEntityList(entityCollection.stream(), requestResourceMapping,  resource, includeKeys, inculdeObjects);
			
				docBuilder = JSONAPI.document(resourceObjects);
			} 
			else {
				final ResourceObjectImpl<Object> data = buildEntity(requestResourceMapping, entity, resource, includeKeys, inculdeObjects);
				docBuilder = JSONAPI.document(data);
			}
			
			docBuilder.link("self", uriInfo.getRequestUri().toString());
			for (Object inc : inculdeObjects) {
				docBuilder.include(buildIncludeEntity(requestResourceMapping, inc,  resource, includeKeys, inculdeObjects));
			}
			setStatusCode(requestContext, responseContext);
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
					final String uri = template.createURI(id);
					final String linkSelfUri = uriInfo.getBaseUri().resolve(uri.substring(1)).toString();
					if (includeKeys.contains(key)) {
						final List<Object> included = resolveIncludes(resource, entity, id, key, m); 
						final List<Linkage> includeLinkage = Lists.newLinkedList();
						for (Object inc : included) {
							final String incId = getId(inc);
							final String type = getModelType(inc.getClass());
							includeLinkage.add(new Linkage(type, incId));
						}
						dataBuilder.link(key,  new JsonLink(null, linkSelfUri, includeLinkage));
						includes.addAll(included);
					} else {
						dataBuilder.link(key, uriInfo.getBaseUri().resolve(uri.substring(1)).toString());
					}
				}
			}
		}
		return dataBuilder.build();
	}
	
	private ResourceObjectImpl buildIncludeEntity(final Mapping mapping, final Object entity, final Object resource, Collection<String> includeKeys, List<Object> includes) {
		final Mapping entityMapping = resourceMapping.getMappingByModel(entity.getClass());
		Mapping m = mapping;
		if (entityMapping != null) {
			m = entityMapping;
		}
		
		final ResourceObjectBuilder dataBuilder = JSONAPI.data(entity);
		if (m != null) {
			String key = "self";
			UriTemplate template = new UriTemplate(m.getPathTemplate(key));
			String id = getId(entity);
			if (id != null) {
				String uri = template.createURI(id);
				dataBuilder.link(key, uriInfo.getBaseUri().resolve(uri.substring(1)).toString());
				//recursive includes?
				//if (includeKeys.contains(key)) {
				//	includes.addAll(resolveIncludes(resource, entity, id, key, m));
				//}
			}
		}
		return dataBuilder.build();
	}
	
	private List<Object> resolveIncludes(Object resource, Object entity, String id, String key, Mapping m) {
		List<Object> includes = Lists.newArrayList();
	    Object included = m.getValue(resource, key, id);
	    if (included != null) {
		    if (included instanceof Response) { // OutboundJaxrsResponse
				included = ((Response)included).getEntity();
			} 
			log.debug("Possible Include: {} - {}", key, included);
			
			if (included.getClass().isArray()) {
				includes.addAll(Lists.newArrayList((Object[])included));
			}
			else if (Collection.class.isAssignableFrom(included.getClass())) {
				includes.addAll((Collection)included);
			}
			else {
				includes.add(included);
			}
			
		}
		return includes;
	}
	
	private List<ResourceObjectImpl<Object>> buildEntityList(Stream<?> source, Mapping requestResourceMapping, final Object resource, Collection<String> includeKeys, List<Object> includes) {
		final List<ResourceObjectImpl<Object>> resourceObjects = Lists.newArrayList();
		source.forEach(obj -> {
			resourceObjects.add(buildEntity(requestResourceMapping, obj, resource, includeKeys, includes));
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
	
	private String getModelType(Class<?> type) {
		ApiModel model = type.getDeclaredAnnotation(ApiModel.class);
		if (model != null) {
			String modelType = model.type();
			if ("undefined".equals(modelType)) {
				modelType = model.value();
			}
			return modelType;
		}
		return null;
	}
	
	private boolean isApplicable(ContainerRequestContext requestContext) {
		return requestContext.getAcceptableMediaTypes()
				.stream()
				.anyMatch(m -> JSONAPI_MEDIATYPE.equals(m));
	}

	@VisibleForTesting
	protected void setStatusCode(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
		if(HttpMethod.POST.equals(requestContext.getMethod())) {
			responseContext.setStatusInfo(Response.Status.CREATED);
		}
	}
}
