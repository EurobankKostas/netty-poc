package com.pocnetty.domain;

import com.pocnetty.domain.dto.LimitOrder;
import com.pocnetty.domain.dto.MarketOrder;
import com.pocnetty.domain.enums.OrderType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

public class MatchingEngine {

    private final PriorityBlockingQueue<LimitOrder> buyOrders = new PriorityBlockingQueue<>(
            11,
            Comparator.comparingDouble(LimitOrder::getPrice).reversed()
                    .thenComparingLong(LimitOrder::getTimestamp)
    );


    private final PriorityBlockingQueue<LimitOrder> sellOrders = new PriorityBlockingQueue<>(
            11,
            Comparator.comparingDouble(LimitOrder::getPrice)
                    .thenComparingLong(LimitOrder::getTimestamp)
    );

    //enable fine tuning in locking for specific buys and sells
    private final Object buyLock = new Object();
    private final Object sellLock = new Object();

    public MatchingEngine() {
        buyOrders.offer(new LimitOrder(OrderType.BUY, 10, 105.0, "B1001"));
        buyOrders.offer(new LimitOrder(OrderType.BUY, 5, 104.0, "B1002"));
        sellOrders.offer(new LimitOrder(OrderType.SELL, 10, 106.0, "S1001"));
        sellOrders.offer(new LimitOrder(OrderType.SELL, 5, 107.0, "S1002"));
    }

    /**
     * Process an incoming MARKET order.
     * <p>
     * For a BUY market order, attempts to match it against the sell order book.
     * For a SELL market order, attempts to match it against the buy order book.
     *
     * @param marketOrder the market order received from the client
     * @return an execution report indicating whether the order was filled or rejected
     */
    public ExecutionReport processMarketOrder(MarketOrder marketOrder) {
        return switch (marketOrder.getType()) {
            case BUY -> {
                synchronized (sellLock) {
                    yield matchOrder(marketOrder, sellOrders);
                }
            }
            case SELL -> {
                synchronized (buyLock) {
                    yield matchOrder(marketOrder, buyOrders);
                }
            }
        };
    }

    /**
     * Attempts to match a market order against the provided limit order queue.
     * <p>
     * The method iterates through the orders in best-to-worst priority (using poll)
     * and skips orders whose quantity does not exactly match the market order.
     * If a matching order is found, it is removed from the queue and a FILLED report is returned.
     * If no match is found, the market order is rejected.
     *
     * @param marketOrder the incoming market order
     * @param orderQueue  the corresponding limit order book (either buy or sell)
     * @return an ExecutionReport indicating FILLED or REJECTED status
     */
    private ExecutionReport matchOrder(MarketOrder marketOrder, PriorityBlockingQueue<LimitOrder> orderQueue) {
        for (Iterator<LimitOrder> iterator = orderQueue.iterator(); iterator.hasNext();) {
            LimitOrder currentOrder = iterator.next();
            if (currentOrder.getQuantity() == marketOrder.getQuantity()) {
                iterator.remove();
                return new ExecutionReport(
                        marketOrder.getQuantity(),
                        currentOrder.getPrice(),
                        marketOrder.getQuantity(),
                        marketOrder.getAccountId(),
                        "FILLED"
                );
            }
        }
        return new ExecutionReport(
                marketOrder.getQuantity(),
                null,
                null,
                marketOrder.getAccountId(),
                "REJECTED"
        );
    }
}
