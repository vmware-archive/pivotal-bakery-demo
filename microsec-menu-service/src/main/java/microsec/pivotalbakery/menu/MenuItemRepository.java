package microsec.pivotalbakery.menu;

import org.springframework.data.repository.PagingAndSortingRepository;

import microsec.pivotalbakery.menu.model.v1.MenuItem;

public interface MenuItemRepository extends PagingAndSortingRepository<MenuItem, Long> {
}