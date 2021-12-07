package com.eleks.academy.pharmagator.services;

import com.eleks.academy.pharmagator.controllers.dto.AdvancedSearchRequest;
import com.eleks.academy.pharmagator.repositories.AdvancedSearchViewRepository;
import com.eleks.academy.pharmagator.repositories.specifications.AdvancedSearchViewSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class AdvancedSearchServiceImplTest {

    private AdvancedSearchServiceImpl subject;

    private AdvancedSearchViewRepository repositoryMock;

    @BeforeEach
    public void setUp() {
        repositoryMock = mock(AdvancedSearchViewRepository.class);

        subject = new AdvancedSearchServiceImpl(repositoryMock);
    }

    @Test
    void search_verifyCorrespondingRepositoryMethodCalled_ok() {
        AdvancedSearchRequest request = new AdvancedSearchRequest();

        Pageable pageable = Pageable.ofSize(2);

        subject.search(request, pageable);

        AdvancedSearchViewSpecification specification = new AdvancedSearchViewSpecification(request);

        verify(repositoryMock, only()).findAll(specification, pageable);
    }

    @Test
    void refreshView_verifyCorrespondingRepositoryMethodCalled_ok() {
        subject.refreshView();

        verify(repositoryMock, only()).refreshView();
    }

}
