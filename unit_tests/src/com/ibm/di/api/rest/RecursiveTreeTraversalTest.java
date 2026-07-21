package com.ibm.di.api.rest;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.ibm.di.test.http.WinkHttpClientContext;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class RecursiveTreeTraversalTest extends WinkHttpClientContext {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	@Override
	protected Class<? extends Application> getApplicationClass() {
		return TestApp.class;
	}

	@Path("/root")
	public static class TestApp extends Application {

		private String name;

		private TestApp child;

		public TestApp() {
		}

		public TestApp(String name, TestApp child) {
			this.name = name;
			this.child = child;
		}

		@Override
		public Set<Class<?>> getClasses() {
			return null;
		}

		@Override
		public Set<Object> getSingletons() {
			Set<Object> set = new HashSet<Object>();
			TestApp test1 = new TestApp("test1", new TestApp("level1", new TestApp("level2", new TestApp("level3", null))));

			set.add(test1);
			return set;
		}

		@GET
		@Produces(MediaType.TEXT_PLAIN)
		public String getNextChild() {
			return child == null ? null : child.name;
		}

		@Path("{child}")
		public Object getChild(@PathParam("child") String c) {
			if (name.equals(c)) {
				return this;
			} else if (child != null && child.name.equals(c)) {
				return child;
			} else {
				return new NotFound();
			}
		}
	}

	public static class NotFound {
		@GET
		public Response notFound() {
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	@Test
	public void test_Traversal() throws ServletException, IOException {
		String url = "/root";

		// level1 check
		url = getNextChildUrl(url);
		assertThat(url, is(notNullValue()));
		assertThat(url.endsWith("level1"), is(true));

		// level2 check
		url = getNextChildUrl(url);
		assertThat(url, is(notNullValue()));
		assertThat(url.endsWith("level2"), is(true));

		// level3 check
		url = getNextChildUrl(url);
		assertThat(url, is(notNullValue()));
		assertThat(url.endsWith("level3"), is(true));
	}

	@Test
	public void test_Direct_Traversal() throws ServletException, IOException {
		String url = "/root/level1/level2";
		getNextChildUrl(url);
	}

	private String getNextChildUrl(String url) throws ServletException, IOException {
		MockHttpServletRequest request = constructMockRequest(HttpMethod.GET, url, MediaType.WILDCARD);
		MockHttpServletResponse response = invoke(request);
		if (response.getStatus() > 299) {
			throw new RuntimeException("Code: " + response.getStatus() + " \n" + response.getContentAsString());
		}
		return response.getContentAsString() == null ? null : url + "/" + response.getContentAsString();
	}
}
