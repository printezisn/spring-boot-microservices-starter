package com.printezisn.moviestore.website.configuration.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Contains unit tests for the DefaultResponseErrorHandler class
 */
public class DefaultResponseErrorHandlerTest {

	@Mock
	private ClientHttpResponse clientHttpResponse;
	
	private DefaultResponseErrorHandler errorHandler;
	
	/**
	 * Initializes the test class
	 */
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		errorHandler = new DefaultResponseErrorHandler();
	}
	
	/**
	 * Tests to ensure that only 5xx status codes are considered as errors
	 */
	@Test
	public void test_hasError_onlyServerErrors() {
		Arrays.stream(HttpStatus.values()).forEach(statusCode -> {
			try {
				when(clientHttpResponse.getStatusCode()).thenReturn(statusCode);
			
				final boolean result = errorHandler.hasError(clientHttpResponse);
				if(statusCode.is5xxServerError()) {
					assertTrue(result);
				}
				else {
					assertFalse(result);
				}
			}
			catch(final IOException ex) {
				throw new RuntimeException(ex);
			}
		});
	}
	
	/**
	 * Tests if the correct exception is thrown in case of an error
	 */
	@Test
	public void test_handleError_success() throws IOException {
		final HttpStatus statusCode = HttpStatus.BAD_REQUEST;
		final String statusText = "test text";
		
		when(clientHttpResponse.getStatusCode()).thenReturn(statusCode);
		when(clientHttpResponse.getStatusText()).thenReturn(statusText);
		
		try {
			errorHandler.handleError(clientHttpResponse);
			fail();
		}
		catch(final HttpClientErrorException ex) {
			assertEquals(statusCode, ex.getStatusCode());
			assertEquals(statusText, ex.getStatusText());
		}
	}
}
