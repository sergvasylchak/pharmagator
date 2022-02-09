package com.eleks.academy.pharmagator.repositories;

import com.eleks.academy.pharmagator.controllers.dto.PriceAggregationRequest;
import com.eleks.academy.pharmagator.projections.dto.PriceAggregationItem;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.RoundingMode;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.eleks.academy.pharmagator.utils.QueryUtils.FIND_ALL_AGGREGATED;
import static java.util.stream.Collectors.joining;

@Repository
@RequiredArgsConstructor
public class PriceJdbcRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<PriceAggregationItem<String>> findAllAggregated(PriceAggregationRequest request) {
        final var params = new MapSqlParameterSource();

        final var dateFrom = Optional.ofNullable(request.getDateFrom())
                .map(LocalDate::atStartOfDay)
                .orElseGet(() -> LocalDateTime.now().minusDays(30));

        final var dateTo = Optional.ofNullable(request.getDateTo())
                .map(LocalDate::atStartOfDay)
                .map(ldt -> ldt.plusDays(1))
                .orElseGet(LocalDateTime::now);


        params.addValue("dateFrom", dateFrom, Types.TIMESTAMP);
        params.addValue("dateTo", dateTo, Types.TIMESTAMP);

        final var query = FIND_ALL_AGGREGATED
                .replaceAll("\\$agg", request.getAgg().getSqlOperation())
                .replaceAll("\\$order", request.getOrder().name());

        return this.jdbcTemplate.query(PharmacyAggFilter.buildFilterQuery(query, request), params, (rs, i) ->
                PriceAggregationItem.of(
                        rs.getString("key"),
                        rs.getBigDecimal("value").setScale(3, RoundingMode.HALF_UP)
                )
        );
    }

    @RequiredArgsConstructor
    private enum PharmacyAggFilter {
        MEDICINE("\\$medicineFilter",
                PriceAggregationRequest::getMedicine,
                str -> !str.isEmpty(),
                prop -> String.format("HAVING LOWER(m.title) LIKE LOWER('%%%s%%')", prop)),
        PHARMACIES_NOT("\\$pharmacyNotFilter",
                pharmacyNotExtractor(),
                str -> !str.isEmpty(),
                prop -> String.format("AND ph.name NOT IN (%s)", prop));

        private final String placeholderMatcher;
        private final Function<PriceAggregationRequest, String> propertyExtractor;
        private final Predicate<String> propertyValid;
        private final Function<String, String> filterBuilder;

        private String applyQueryFilter(String query, PriceAggregationRequest request) {
            final var filterQuery = Optional.ofNullable(this.propertyExtractor.apply(request))
                    .filter(this.propertyValid)
                    .map(this.filterBuilder)
                    .orElseGet(String::new);

            return query.replaceAll(this.placeholderMatcher, filterQuery);
        }

        public static String buildFilterQuery(String dataQuery, PriceAggregationRequest request) {
            return Arrays.stream(values())
                    .reduce(dataQuery,
                            (query, builder) -> builder.applyQueryFilter(query, request),
                            (a, b) -> {
                                throw new IllegalStateException();
                            });
        }

        private static Function<PriceAggregationRequest, String> pharmacyNotExtractor() {
            return request ->
                    Optional.ofNullable(request.getPharmacyNot())
                            .stream()
                            .flatMap(Collection::stream)
                            .map(param -> String.format("'%s'", param))
                            .collect(joining(","));
        }
    }
}
