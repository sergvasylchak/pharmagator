package com.eleks.academy.pharmagator.services;

import com.eleks.academy.pharmagator.controllers.dto.AdvancedSearchRequest;
import com.eleks.academy.pharmagator.entities.AdvancedSearchView;
import com.eleks.academy.pharmagator.repositories.AdvancedSearchViewRepository;
import com.eleks.academy.pharmagator.repositories.specifications.AdvancedSearchViewSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdvancedSearchServiceImpl implements AdvancedSearchService {

    private final AdvancedSearchViewRepository repository;

    @Override
    public Page<AdvancedSearchView> search(AdvancedSearchRequest request, Pageable pageable) {
        AdvancedSearchViewSpecification specification = new AdvancedSearchViewSpecification(request);

        return repository.findAll(specification, pageable);
    }

    @Override
    public void refreshView() {
        this.repository.refreshView();
    }

}
