/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */
package ke.co.suncha.simba.admin.service;

import org.springframework.context.ApplicationEvent;

/**
 * This is an optional class used in publishing application events. This can be
 * used to inject events into the Spring Boot audit management endpoint.
 */
public class HotelServiceEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HotelServiceEvent(Object source) {
		super(source);
	}

	public String toString() {
		return "My HotelService Event";
	}
}