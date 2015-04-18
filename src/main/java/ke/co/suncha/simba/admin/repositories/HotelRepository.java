/**
 * @author Maitha Manyala <maithamanyala@gmail.com>
 *
 */
package ke.co.suncha.simba.admin.repositories;

import ke.co.suncha.simba.admin.models.Hotel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;


/**
 * Repository can be used to delegate CRUD operations against the data source: http://goo.gl/P1J8QH
 */
public interface HotelRepository extends PagingAndSortingRepository<Hotel, Long> {
    Hotel findHotelByCity(String city);
	Page<Hotel> findAll(Pageable pageable);
}