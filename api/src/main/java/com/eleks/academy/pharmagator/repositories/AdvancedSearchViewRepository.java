package com.eleks.academy.pharmagator.repositories;

import com.eleks.academy.pharmagator.entities.AdvancedSearchView;
import com.eleks.academy.pharmagator.entities.PriceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AdvancedSearchViewRepository extends JpaRepository<AdvancedSearchView, PriceId>,
                                                      JpaSpecificationExecutor<AdvancedSearchView> {

    @Modifying
    @Transactional
    @Query(value = "REFRESH MATERIALIZED VIEW advanced_search_view;", nativeQuery = true)
    void refreshView();

}
