package io.rtr.dropwizard.jsonapi.sample.resources;

import io.rtr.dropwizard.jsonapi.sample.core.DataStore;
import io.rtr.dropwizard.jsonapi.sample.models.Person;
import io.rtr.jsonapi.annotation.ApiResource;

import javax.inject.Inject;
import javax.ws.rs.*;
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
	public Response getPeople() {
		return Response.ok(store.getPeople()).build();
	}

	@POST
	public Person postPerson() {
		return store.addPerson("5", "Zain Cheng", "Rent the Runway");
	}

	@DELETE
	@Path("{id}")
	public void deletePerson(@PathParam("id") String id) {
		store.removePerson(id);
	}
	
	@GET
	@Path("{id}")
	public Response getPerson(@PathParam("id") String id) {
		return Response.ok(store.getPerson(id)).build();
	}
	
	@GET
	@Path("{id}/articles")
	public Response getArticles(@PathParam("id") String id) {	    
		return Response.ok(store.getArticlesByAuthor(id)).build();
	}
}
