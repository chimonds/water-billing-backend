/**
 * @author Maitha Manyala <maithamanyala@gmail.com>
 */
package ke.co.suncha.simba.admin.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.xml.bind.annotation.XmlRootElement;

/*
 * Error information in the response
 */
@XmlRootElement
@JsonSerialize
public class RestResponse extends ResponseEntity<RestResponseObject> {

    /**
     * @param body
     * @param statusCode
     */
    public RestResponse(RestResponseObject body, HttpStatus statusCode) {
        super(body, statusCode);
    }

    public static RestResponse getExpectationFailed(String message) {
        RestResponseObject responseObject = new RestResponseObject();
        responseObject.setMessage(message);
        responseObject.setPayload("");
        return new RestResponse(responseObject, HttpStatus.EXPECTATION_FAILED);
    }

    public static RestResponse getOk(String message, Object object) {
        RestResponseObject responseObject = new RestResponseObject();
        responseObject.setMessage(message);
        responseObject.setPayload(object);
        return new RestResponse(responseObject, HttpStatus.OK);
    }


}
