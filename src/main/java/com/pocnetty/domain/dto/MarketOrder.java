package com.pocnetty.domain.dto;

import com.pocnetty.domain.enums.OrderType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MarketOrder {

    private OrderType type;
    private int quantity;
    private String accountId;
}
