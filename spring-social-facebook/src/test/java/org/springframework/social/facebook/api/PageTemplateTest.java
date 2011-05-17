/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.social.facebook.api;

import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.social.test.client.RequestMatchers.*;
import static org.springframework.social.test.client.ResponseCreators.*;

import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.social.BadCredentialsException;

/**
 * @author Craig Walls
 */
public class PageTemplateTest extends AbstractFacebookApiTest {
	
	@Test
	public void getPage_organization() {
		mockServer.expect(requestTo("https://graph.facebook.com/140804655931206"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/organization-page.json", getClass()), responseHeaders));

		Page page = facebook.pageOperations().getPage("140804655931206");
		assertEquals("140804655931206", page.getId());
		assertEquals("SpringSource", page.getName());
		assertEquals("http://profile.ak.fbcdn.net/static-ak/rsrc.php/v1/yr/r/fwJFrO5KjAQ.png", page.getPicture());
		assertEquals("http://www.facebook.com/pages/SpringSource/140804655931206", page.getLink());
		assertEquals(33, page.getLikes());
		assertEquals("Organization", page.getCategory());
		assertEquals("<p><b>SpringSource</b> is a division of <a href=\"http://en.wikipedia.org/wiki/VMware\" class=\"wikipedia\">VMware</a> that provides...</p>", page.getDescription());
	}

	@Test
	public void getPage_product() {
		mockServer.expect(requestTo("https://graph.facebook.com/21278871488"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/product-page.json", getClass()), responseHeaders));

		Page page = facebook.pageOperations().getPage("21278871488");
		assertEquals("21278871488", page.getId());
		assertEquals("Mountain Dew", page.getName());
		assertEquals("http://profile.ak.fbcdn.net/hprofile-ak-snc4/203494_21278871488_3106566_s.jpg", page.getPicture());
		assertEquals("http://www.facebook.com/mountaindew", page.getLink());
		assertEquals(5083988, page.getLikes());
		assertEquals("Food/beverages", page.getCategory());
		assertEquals("www.mountaindew.com\nwww.greenlabelsound.com\nwww.greenlabelart.com\nwww.honorthecode.com\nwww.dietdewchallenge.com\nwww.twitter.com/mtn_dew\nwww.youtube.com/mountaindew", page.getWebsite());
	}

	@Test
	public void getPage_place() {
		mockServer.expect(requestTo("https://graph.facebook.com/150263434985489"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/place-page.json", getClass()), responseHeaders));

		Page page = facebook.pageOperations().getPage("150263434985489");
		assertEquals("150263434985489", page.getId());
		assertEquals("Denver International Airport", page.getName());
		assertEquals("http://profile.ak.fbcdn.net/static-ak/rsrc.php/v1/yZ/r/u3l2nEuXNsK.png", page.getPicture());
		assertEquals("http://www.facebook.com/pages/Denver-International-Airport/150263434985489", page.getLink());
		assertEquals(1052, page.getLikes());
		assertEquals("Local business", page.getCategory());
		assertEquals("http://flydenver.com", page.getWebsite());
		assertEquals("Denver", page.getLocation().getCity());
		assertEquals("CO", page.getLocation().getState());
		assertEquals("United States", page.getLocation().getCountry());
		assertEquals(39.851693483111, page.getLocation().getLatitude(), 0.0001);
		assertEquals(-104.67384947947, page.getLocation().getLongitude(), 0.0001);
		assertEquals("(303) 342-2000", page.getPhone());
		assertEquals(121661, page.getCheckins());
	}

	@Test
	public void getPage_application() {
		mockServer.expect(requestTo("https://graph.facebook.com/140372495981006"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse(new ClassPathResource("testdata/application-page.json", getClass()), responseHeaders));

		Page page = facebook.pageOperations().getPage("140372495981006");
		assertEquals("140372495981006", page.getId());
		assertEquals("Greenhouse", page.getName());
		assertEquals("http://www.facebook.com/apps/application.php?id=140372495981006", page.getLink());
		assertEquals("The social destination for Spring application developers.", page.getDescription());
	}
	
	@Test
	public void isPageAdmin() {
		expectFetchAccounts();
		assertFalse(facebook.pageOperations().isPageAdmin("2468013579"));
		assertTrue(facebook.pageOperations().isPageAdmin("987654321"));
	}
	
	@Test
	public void post_message() throws Exception {
		expectFetchAccounts();
		String requestBody = "message=Hello+Facebook+World&access_token=pageAccessToken";
		mockServer.expect(requestTo("https://graph.facebook.com/987654321/feed"))
				.andExpect(method(POST))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andExpect(body(requestBody))
				.andRespond(withResponse("{\"id\":\"123456_78901234\"}", responseHeaders));
		assertEquals("123456_78901234", facebook.pageOperations().post("987654321", "Hello Facebook World"));
		mockServer.verify();
	}

	@Test(expected = BadCredentialsException.class)
	public void post_message_notAdmin() throws Exception {
		expectFetchAccounts();
		facebook.pageOperations().post("2468013579", "Hello Facebook World");
	}
	
	@Test
	public void post_link() throws Exception {
		expectFetchAccounts();
		String requestBody = "link=someLink&name=some+name&caption=some+caption&description=some+description&message=Hello+Facebook+World&access_token=pageAccessToken";
		mockServer.expect(requestTo("https://graph.facebook.com/987654321/feed")).andExpect(method(POST))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andExpect(body(requestBody))
				.andRespond(withResponse("{\"id\":\"123456_78901234\"}", responseHeaders));
		FacebookLink link = new FacebookLink("someLink", "some name", "some caption", "some description");
		assertEquals("123456_78901234", facebook.pageOperations().post("987654321", "Hello Facebook World", link));
		mockServer.verify();
	}

	@Test(expected = BadCredentialsException.class)
	public void post_link_notAdmin() throws Exception {
		expectFetchAccounts();
		FacebookLink link = new FacebookLink("someLink", "some name", "some caption", "some description");
		facebook.pageOperations().post("2468013579", "Hello Facebook World", link);
	}
	
	@Test
	public void postPhoto_noCaption() {
		expectFetchAccounts();
		mockServer.expect(requestTo("https://graph.facebook.com/192837465/photos"))
			.andExpect(method(POST))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse("{\"id\":\"12345\"}", responseHeaders));
		// TODO: Match body content to ensure fields and photo are included
		Resource photo = getUploadResource("photo.jpg", "PHOTO DATA");
		String photoId = facebook.pageOperations().postPhoto("987654321", "192837465", photo);
		assertEquals("12345", photoId);
	}

	@Test
	public void postPhoto_withCaption() {
		expectFetchAccounts();
		mockServer.expect(requestTo("https://graph.facebook.com/192837465/photos"))
			.andExpect(method(POST))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withResponse("{\"id\":\"12345\"}", responseHeaders));
		// TODO: Match body content to ensure fields and photo are included
		Resource photo = getUploadResource("photo.jpg", "PHOTO DATA");
		String photoId = facebook.pageOperations().postPhoto("987654321", "192837465", photo, "Some caption");
		assertEquals("12345", photoId);
	}
	
	// private helpers
	
	private void expectFetchAccounts() {
		mockServer.expect(requestTo("https://graph.facebook.com/me/accounts"))
				.andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
                .andRespond(withResponse(new ClassPathResource("testdata/accounts.json", getClass()), responseHeaders));
	}

	private Resource getUploadResource(final String filename, String content) {
		Resource video = new ByteArrayResource(content.getBytes()) {
			public String getFilename() throws IllegalStateException {
				return filename;
			};
		};
		return video;
	}

}
