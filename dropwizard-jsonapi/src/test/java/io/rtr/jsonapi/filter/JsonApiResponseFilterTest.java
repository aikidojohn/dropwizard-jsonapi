package io.rtr.jsonapi.filter;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JsonApiResponseFilterTest {

	// Mocks
	@Mock private ContainerRequestContext mockRequestContext;
	@Mock private ContainerResponseContext mockResponseContext;

	// Class to be test
	@InjectMocks
	private JsonApiResponseFilter jsonApiResponseFilter;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		jsonApiResponseFilter = new JsonApiResponseFilter();
	}
	
	@Test
	public void testSetStatusCodeForPost() throws IOException {
		when(mockRequestContext.getMethod()).thenReturn(HttpMethod.POST);
		jsonApiResponseFilter.setStatusCode(mockRequestContext, mockResponseContext);
		verify(mockResponseContext).setStatusInfo(Response.Status.CREATED);
	}

}
