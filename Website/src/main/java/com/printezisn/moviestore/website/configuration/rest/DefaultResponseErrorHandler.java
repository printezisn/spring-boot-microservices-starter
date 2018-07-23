package com.printezisn.moviestore.website.configuration.rest;

import java.io.IOException;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResponseErrorHandler;

/**
 * The default response error handler for the inner rest services
 */
public class DefaultResponseErrorHandler implements ResponseErrorHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasError(final ClientHttpResponse response) throws IOException {
		return response.getStatusCode().is5xxServerError();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleError(final ClientHttpResponse response) throws IOException {
		throw new HttpClientErrorException(response.getStatusCode(), response.getStatusText());
	}

}
