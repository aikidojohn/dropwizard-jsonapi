package io.rtr.jsonapi.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.rtr.jsonapi.JsonAPIRequest;
import org.glassfish.jersey.message.internal.MediaTypes;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Provider
public class JsonApiRequestFilter implements ContainerRequestFilter
{

  public static final MediaType JSONAPI_MEDIATYPE = MediaType.valueOf("application/vnd.api+json");
  @Override
  public void filter(ContainerRequestContext requestContext)
    throws IOException
  {
    if (requestContext.hasEntity() && MediaTypes.typeEqual(JSONAPI_MEDIATYPE, requestContext.getMediaType()))
    {
      InputStream in = requestContext.getEntityStream();
      if (in.getClass() != ByteArrayInputStream.class) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonAPIRequest jsonAPIRequest = objectMapper.readValue(in, JsonAPIRequest.class);
        // Buffer input
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // add in id to make the object complete
        Map<String, Object> attributesWithId = jsonAPIRequest.getData().getAttributes();
        attributesWithId.put("id", jsonAPIRequest.getData().getId());
        objectMapper.writeValue(baos, attributesWithId);

        //set the input to be the attributes field itself
        in = new ByteArrayInputStream(baos.toByteArray());
        requestContext.setEntityStream(in);
      }
    }
  }
}