package microsec.pivotalbakery.order;

import java.util.Collection;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import microsec.pivotalbakery.order.model.v1.Order;

public interface OrderRepository extends PagingAndSortingRepository<Order, Long> {
    Collection<Order> findByCustomerId(@Param("customerId") String customerId);
}