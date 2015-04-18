/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */
package ke.co.suncha.simba.admin.exception;


/**
 * For HTTP 404 errros
 */
public class ResourceNotFoundException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 6524555779403403504L;

	public ResourceNotFoundException() {
        super();
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(Throwable cause) {
        super(cause);
    }

}
