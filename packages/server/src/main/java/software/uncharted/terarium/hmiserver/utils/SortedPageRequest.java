package software.uncharted.terarium.hmiserver.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class SortedPageRequest {

	/**
	 * Create a Pageable object with the given sort field, sort order, page number, and page size.
	 *
	 * @param sortField the field to sort by
	 * @param sortOrder the sort order, 1 for ascending, -1 for descending
	 * @param page      the page number
	 * @param rows      the number of rows per page
	 * @return          a {@link Pageable} instance to be passed into a Spring Data repository method
	 */
	public static Pageable of(final String sortField, final Integer sortOrder, final Integer page, final Integer rows) {
		Sort sort = Sort.unsorted();
		if (sortField != null && !sortField.isEmpty()) {
			Sort.Direction direction = sortOrder < 0 ? Sort.Direction.DESC : Sort.Direction.ASC;
			sort = Sort.by(direction, sortField);
		}
		return PageRequest.of(page, rows, sort);
	}
}
