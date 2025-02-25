package com.pocnetty.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExecutionReport {

    private static final String REPORT_TYPE = "exe_report";
    private int initialQuantity;
    private Double executedPrice;
    private Integer executedQuantity;
    private String accountId;
    private String status;

    @JsonProperty("type")
    public String getType() {
        return REPORT_TYPE;
    }
}
