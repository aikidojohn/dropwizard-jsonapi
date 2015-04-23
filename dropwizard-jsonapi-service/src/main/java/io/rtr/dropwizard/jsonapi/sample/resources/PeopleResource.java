package io.rtr.dropwizard.jsonapi.sample.resources;

import io.rtr.dropwizard.jsonapi.sample.core.DataStore;
import io.rtr.dropwizard.jsonapi.sample.models.Person;
import io.rtr.jsonapi.annotation.ApiResource;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@ApiResource(model = Person.class)
@Path("people")
@Produces({"application/vnd.api+json", MediaType.APPLICATION_JSON})
@Consumes({"application/vnd.api+json", MediaType.APPLICATION_JSON})
public class PeopleResource {

	@Inject
	private DataStore store;

	@GET
	public Response getArticles() {
		return Response.ok(store.getPeople()).build();
	}
	
	@GET
	@Path("{id}")
	public Response getArticle(@PathParam("id") String id) {
		return Response.ok(store.getPerson(id)).build();
	}
	
	@GET
	@Path("{id}/articles")
	public Response getAuthor(@PathParam("id") String id) {	    
		return Response.ok(store.getArticlesByAuthor(id)).build();
	}
}
