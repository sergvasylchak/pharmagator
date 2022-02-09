package com.eleks.academy.pharmagator.utils;

public interface QueryUtils {

    String FIND_ALL_AGGREGATED = """
            SELECT m.title AS key, $agg AS value
            FROM prices p
            JOIN medicines m ON m.id  = p.medicine_id
            JOIN pharmacies ph ON ph.id = p.pharmacy_id
            WHERE p.updated_at BETWEEN :dateFrom AND :dateTo
            $pharmacyNotFilter
            GROUP BY (m.title)
            $medicineFilter
            ORDER BY $agg $order
            """;

}
