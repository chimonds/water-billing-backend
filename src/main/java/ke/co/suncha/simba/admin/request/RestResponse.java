/**
 * @author Maitha Manyala <maithamanyala@gmail.com>
 *
 */
package ke.co.suncha.simba.admin.request;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/*
 * Error information in the response
 */
@XmlRootElement
@JsonSerialize
public class RestResponse extends ResponseEntity<RestResponseObject>{

	/**
	 * @param body
	 * @param statusCode
	 */
	public RestResponse(RestResponseObject body, HttpStatus statusCode) {
		super(body, statusCode);
	}
}
