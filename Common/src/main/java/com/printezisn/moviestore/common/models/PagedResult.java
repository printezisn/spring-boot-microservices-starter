package com.printezisn.moviestore.common.models;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The class that holds paged entries
 *
 * @param <T> The type of entries
 */
@RequiredArgsConstructor
@Getter
public class PagedResult<T> {

	private final List<T> entries;
	private final int pageNumber;
	private final int totalPages;
	private final String sortField;
	private final boolean isAscending;
}
