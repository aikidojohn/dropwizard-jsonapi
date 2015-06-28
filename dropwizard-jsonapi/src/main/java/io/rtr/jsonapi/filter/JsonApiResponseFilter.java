package io.rtr.jsonapi.filter;

import io.rtr.jsonapi.JSONAPI;
import io.rtr.jsonapi.JSONAPI.ApiDocumentBuilder;
import io.rtr.jsonapi.JSONAPI.ResourceObjectBuilder;
import io.rtr.jsonapi.JsonLink;
import io.rtr.jsonapi.Linkage;
import io.rtr.jsonapi.ResponseData;
import io.rtr.jsonapi.annotation.ApiModel;
import io.rtr.jsonapi.filter.mapping.ResourceMappingContext;
import io.rtr.jsonapi.filter.mapping.ResourceMappingContext.Mapping;
import io.rtr.jsonapi.impl.IncludesResourceObjectImpl;
import io.rtr.jsonapi.impl.ResourceObjectImpl;
import io.rtr.jsonapi.util.EntityUtil;
import io.rtr.jsonapi.util.FieldUtil;

import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterInjector;
import com.google.common.collect.Lists;

import org.glassfish.jersey.uri.UriTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class JsonApiResponseFilter implements ContainerResponseFilter {
  private static final Logger log = LoggerFactory.getLogger(JsonApiResponseFilter.class);
  public static final MediaType JSONAPI_MEDIATYPE = MediaType.valueOf("application/vnd.api+json");

  @Context
  private UriInfo uriInfo;

  @Context
  private ResourceMappingContext resourceMapping;

  @Override
  public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) throws IOException {
    // Necessary for field filtering
    ObjectWriterInjector.set(new FilteredObjectWriterModifier(uriInfo, resourceMapping));

    if (!isApplicable(requestContext)) {
      log.trace("JSON API not requested");
      return;
    }
    log.trace("HANDLING JSON API");

    final Object entity = responseContext.getEntity();
    if (entity != null && !uriInfo.getMatchedResources().isEmpty()) {
      final List<Object> resources = uriInfo.getMatchedResources();
      final Object resource = resources.get(0);
      final Mapping requestResourceMapping = resourceMapping.getMapping(resource.getClass());

      final List<Object> inculdeObjects = Lists.newArrayList();
      final List<String> includeKeys = Lists.newArrayList();
      if (uriInfo.getQueryParameters().containsKey("include")) {
        includeKeys.addAll(uriInfo.getQueryParameters().get("include"));
      }

      ApiDocumentBuilder docBuilder = null;
      if (entity.getClass().isArray()) {
        final Object[] entityArray = (Object[]) entity;
        final List<ResourceObjectImpl> resourceObjects = buildEntityList(Arrays.stream(entityArray), requestResourceMapping, resource,
            includeKeys, inculdeObjects);

        docBuilder = JSONAPI.document(resourceObjects);
      } else if (Collection.class.isAssignableFrom(entity.getClass())) {
        final Collection<?> entityCollection = (Collection<?>) entity;
        final List<ResourceObjectImpl> resourceObjects = buildEntityList(entityCollection.stream(), requestResourceMapping, resource,
            includeKeys, inculdeObjects);

        docBuilder = JSONAPI.document(resourceObjects);
      } else {
        final ResourceObjectImpl data = buildEntity(requestResourceMapping, entity, resource, includeKeys, inculdeObjects);
        docBuilder = JSONAPI.document(data);
      }

      docBuilder.link("self", uriInfo.getRequestUri().toString());
      for (final Object inc : inculdeObjects) {
        docBuilder.include(buildIncludeEntity(requestResourceMapping, inc, resource, includeKeys, inculdeObjects));
      }
      setStatusCode(requestContext, responseContext);
      responseContext.setEntity(docBuilder.build(uriInfo));
    }
  }

  private ResourceObjectImpl buildEntity(final Mapping mapping, final Object entity, final Object resource, final Collection<String> includeKeys,
      final List<Object> includes) {
    final Mapping entityMapping = resourceMapping.getMappingByModel(entity.getClass());
    Mapping m = mapping;
    if (entityMapping != null) {
      m = entityMapping;
    }
    final ResponseData responseData = new ResponseData();
    responseData.setAttributes(entity);
    responseData.setType(entity.getClass().getTypeName());
    responseData.setId(getId(entity));
    final ResourceObjectBuilder dataBuilder = JSONAPI.data(responseData);
    if (m != null) {
      for (final String key : m.getKeys()) {
        final UriTemplate template = new UriTemplate(m.getPathTemplate(key));
        final String id = getId(entity);
        if (id != null) {
          final String uri = template.createURI(id);
          final String linkSelfUri = uriInfo.getBaseUri().resolve(uri.substring(1)).toString();
          if (includeKeys.contains(key)) {
            final List<Object> included = resolveIncludes(resource, entity, id, key, m);
            final List<Linkage> includeLinkage = Lists.newLinkedList();
            for (final Object inc : included) {
              final String incId = getId(inc);
              final String type = getModelType(inc.getClass());
              includeLinkage.add(new Linkage(type, incId));
            }
            dataBuilder.link(key, new JsonLink(null, linkSelfUri, includeLinkage));
            includes.addAll(included);
          } else {
            if (!key.equals("self") && !key.equals(EntityUtil.getType(entity))) {
              dataBuilder.link(key, uriInfo.getBaseUri().resolve(uri.substring(1)).toString());
            }
          }
        }
      }
    }
    return dataBuilder.build();
  }

  private IncludesResourceObjectImpl buildIncludeEntity(final Mapping mapping, final Object entity, final Object resource,
      final Collection<String> includeKeys, final List<Object> includes) {
    final Mapping entityMapping = resourceMapping.getMappingByModel(entity.getClass());
    Mapping m = mapping;
    if (entityMapping != null) {
      m = entityMapping;
    }
    final ResponseData responseData = new ResponseData();
    responseData.setAttributes(entity);
    responseData.setType(entity.getClass().getTypeName());
    responseData.setId(getId(entity));
    final JSONAPI.IncludesResourceObjectBuilders dataBuilder = JSONAPI.includesData(responseData);
    if (m != null) {
      final String key = "self";
      // use the template for the entity that is included, not the base entity
      final UriTemplate template = new UriTemplate(m.getPathTemplate(EntityUtil.getType(entity)));
      final String id = getId(entity);
      if (id != null) {
        final String uri = template.createURI(id);
        dataBuilder.link(key, uriInfo.getBaseUri().resolve(uri.substring(1)).toString());
        // recursive includes?
        // if (includeKeys.contains(key)) {
        // includes.addAll(resolveIncludes(resource, entity, id, key, m));
        // }
      }
    }
    return dataBuilder.build();
  }

  private List<Object> resolveIncludes(final Object resource, final Object entity, final String id, final String key, final Mapping m) {
    final List<Object> includes = Lists.newArrayList();
    Object included = m.getValue(resource, key, id);
    if (included != null) {
      if (included instanceof Response) { // OutboundJaxrsResponse
        included = ((Response) included).getEntity();
      }
      log.debug("Possible Include: {} - {}", key, included);

      if (included.getClass().isArray()) {
        includes.addAll(Lists.newArrayList((Object[]) included));
      } else if (Collection.class.isAssignableFrom(included.getClass())) {
        includes.addAll((Collection) included);
      } else {
        includes.add(included);
      }

    }
    return includes;
  }

  @SuppressWarnings("checkstyle:indentation")
  private List<ResourceObjectImpl> buildEntityList(final Stream<?> source, final Mapping requestResourceMapping, final Object resource,
      final Collection<String> includeKeys, final List<Object> includes) {
    final List<ResourceObjectImpl> resourceObjects = Lists.newArrayList();
    source.forEach(obj -> {
      resourceObjects.add(buildEntity(requestResourceMapping, obj, resource, includeKeys, includes));
    });
    return resourceObjects;
  }

  private String getId(final Object obj) {
    try {
      final Field field = FieldUtil.findDeclaredField(obj, "id");
      field.setAccessible(true);
      final Object id = field.get(obj);
      if (id instanceof String) {
        return (String) id;
      }
      return String.valueOf(id);
    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  private String getModelType(final Class<?> type) {
    final ApiModel model = type.getDeclaredAnnotation(ApiModel.class);
    if (model != null) {
      String modelType = model.type();
      if ("undefined".equals(modelType)) {
        modelType = model.value();
      }
      return modelType;
    }
    return null;
  }

  private boolean isApplicable(final ContainerRequestContext requestContext) {
    return requestContext.getAcceptableMediaTypes().stream().anyMatch(m -> JSONAPI_MEDIATYPE.equals(m));
  }

  private void setStatusCode(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) {
    if (HttpMethod.POST.equals(requestContext.getMethod())) {
      responseContext.setStatusInfo(Response.Status.CREATED);
    } else if (HttpMethod.DELETE.equals(requestContext.getMethod())) {
      responseContext.setStatusInfo(Response.Status.NO_CONTENT);
    }
  }
}
