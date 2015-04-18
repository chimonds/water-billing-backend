/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */
package ke.co.suncha.simba.admin.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletResponse;

import ke.co.suncha.simba.admin.exception.DataFormatException;
import ke.co.suncha.simba.admin.exception.ResourceNotFoundException;
import ke.co.suncha.simba.admin.request.RestResponseObject;
import ke.co.suncha.simba.admin.request.RestResponse;

/**
 * This class is meant to be extended by all REST resource "controllers". It
 * contains exception mapping and other common REST API functionality
 */
// @ControllerAdvice?
public abstract class AbstractRestHandler implements
		ApplicationEventPublisherAware {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	protected ApplicationEventPublisher eventPublisher;

	protected static final String DEFAULT_PAGE_SIZE = "100";
	protected static final String DEFAULT_PAGE_NUM = "0";
	private RestResponse restResponse;
	private RestResponseObject body;

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(DataFormatException.class)
	public @ResponseBody RestResponse handleDataStoreException(
			DataFormatException ex, WebRequest request,
			HttpServletResponse response) {
		log.info("Converting Data Store exception to RestResponse : "
				+ ex.getMessage());
		body = new RestResponseObject(ex.getLocalizedMessage(), "");
		restResponse = new RestResponse(body, HttpStatus.BAD_REQUEST);
		return restResponse;
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(ResourceNotFoundException.class)
	public @ResponseBody RestResponse handleResourceNotFoundException(
			ResourceNotFoundException ex, WebRequest request,
			HttpServletResponse response) {
		log.info("ResourceNotFoundException handler:" + ex.getMessage());

		body = new RestResponseObject(ex.getLocalizedMessage(), "");
		restResponse = new RestResponse(body,HttpStatus.NOT_FOUND);
		return restResponse;
	}

	@Override
	public void setApplicationEventPublisher(
			ApplicationEventPublisher applicationEventPublisher) {
		this.eventPublisher = applicationEventPublisher;
	}

	// todo: replace with exception mapping
	public static <T> T checkResourceFound(final T resource) {
		if (resource == null) {
			throw new ResourceNotFoundException("Resource not found");
		}
		return resource;
	}

}
