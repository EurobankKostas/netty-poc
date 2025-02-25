package com.pocnetty.domain.dto;

import com.pocnetty.domain.enums.OrderType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LimitOrder {

    private OrderType type;
    private int quantity;
    private double price;
    private String accountId;
    private long timestamp;

    public LimitOrder(OrderType type, int quantity, double price, String accountId) {
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.accountId = accountId;
        this.timestamp = System.nanoTime();
    }
}
