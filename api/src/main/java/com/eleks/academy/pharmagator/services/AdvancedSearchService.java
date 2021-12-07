package com.eleks.academy.pharmagator.services;

import com.eleks.academy.pharmagator.controllers.dto.AdvancedSearchRequest;
import com.eleks.academy.pharmagator.entities.AdvancedSearchView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdvancedSearchService {

    Page<AdvancedSearchView> search(AdvancedSearchRequest request, Pageable pageable);

    void refreshView();

}
