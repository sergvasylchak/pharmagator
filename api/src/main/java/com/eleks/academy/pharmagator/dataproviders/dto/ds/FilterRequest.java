package com.eleks.academy.pharmagator.dataproviders.dto.ds;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterRequest {

    private Long page;
    private Long per;

    public FilterRequest next() {
        return FilterRequest.builder()
                .page(this.page + 1)
                .per(this.per)
                .build();
    }

    public static FilterRequest start(Long pageSize) {
        return FilterRequest.builder()
                .page(1L)
                .per(pageSize)
                .build();
    }

}
