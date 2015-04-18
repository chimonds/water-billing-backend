/**
 * @author Maitha Manyala <maithamanyala@gmail.com>
 *
 */
package ke.co.suncha.simba.admin.exception;


/**
 * for HTTP 400 errors
 */
public final class DataFormatException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 4480555985197788801L;

	public DataFormatException() {
        super();
    }

    public DataFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataFormatException(String message) {
        super(message);
    }

    public DataFormatException(Throwable cause) {
        super(cause);
    }
}