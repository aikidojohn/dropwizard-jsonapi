package io.rtr.dropwizard.jsonapi.sample.core;

import io.rtr.dropwizard.jsonapi.sample.models.Article;
import io.rtr.dropwizard.jsonapi.sample.models.Person;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DataStore {
	private Map<String, Article> articles = Maps.newHashMap();
	private Map<String, Person> people = Maps.newHashMap();
	private Map<String, List<Article>> authorArticles = Maps.newHashMap();
	private Map<String, Person> articleAuthors = Maps.newHashMap();
	
	public DataStore() {
		addArticle("1", "My Amazing Article about JSON API", "Lorem ipsum dolor sit amet.");
		addArticle("2", "I, Fashionator", "Lorem ipsum dolor sit amet.");
		addArticle("3", "Dropwizard Awsomeness", "Lorem ipsum dolor sit amet.");
		addPerson("2", "John Hite", "Rent the Runway");
		addPerson("4", "Eric Weinstein", "Rent the Runway");
		addAuthor("2", "1");
		addAuthor("2", "3");
		addAuthor("4", "2");
	}
	
	public List<Article> getArticles() {
		return Lists.newArrayList(articles.values());
	}
	
	public List<Person> getPeople() {
		return Lists.newArrayList(people.values());
	}
	
	public Article getArticle(String id) {
		return articles.get(id);
	}
	
	public Person getPerson(String id) {
		return people.get(id);
	}
	
	public List<Article> getArticlesByAuthor(String authorId) {
		return authorArticles.get(authorId);
	}
	
	public Person getAuthor(String articleId) {
		return articleAuthors.get(articleId);
	}
	
	private void addAuthor(String personId, String articleId) {
		List<Article> articles = authorArticles.get(personId);
		if (articles == null) {
			articles = Lists.newArrayList();
			authorArticles.put(personId, articles);
		}
		articles.add(getArticle(articleId));
		
		articleAuthors.put(articleId, getPerson(personId));
	}
	
	public Person addPerson(String id ,String name, String company) {
		Person p = createPerson(id, name, company);
		people.put(id, p);
		return p;
	}

	public void removePerson(String id) {
		people.remove(id);
	}
	
	private void addArticle(String id, String title, String body) {
		Article a = createArticle(id, title, body);
		articles.put(id, a);
	}
	
	private static Article createArticle(String id, String title, String body) {
		Article article = new Article();
		article.setId(id);
		article.setTitle(title);
		article.setBody(body);
		return article;
	}
	
	private Person createPerson(String id, String name, String company) {
		Person person = new Person();
		person.setId(id);
		person.setName(name);
		person.setCompany(company);
		return person;
	}
}
