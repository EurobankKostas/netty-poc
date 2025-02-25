package com.pocnetty;

import com.pocnetty.domain.ExecutionReport;
import com.pocnetty.domain.MatchingEngine;
import com.pocnetty.domain.dto.MarketOrder;
import com.pocnetty.domain.enums.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MatchingEngineTest {

    private MatchingEngine matchingEngine;

    @BeforeEach
    public void setUp() {
        matchingEngine = new MatchingEngine();
    }

    /**
     * Test that a BUY market order with a quantity matching an available SELL limit order is FILLED.
     * Expected: The order should match the sell order with quantity 10 (price 106.0) and be filled.
     */
    @Test
    public void testBuyMarketOrderFilled() {
        MarketOrder marketOrder = new MarketOrder(OrderType.BUY, 10, "1233");
        ExecutionReport report = matchingEngine.processMarketOrder(marketOrder);

        assertEquals("FILLED", report.getStatus(), "Order should be FILLED");
        assertEquals(10, report.getInitialQuantity());
        assertEquals(10, report.getExecutedQuantity());
        assertEquals(106.0, report.getExecutedPrice());
        assertEquals("1233", report.getAccountId());
    }

    /**
     * Test that a SELL market order with a quantity matching an available BUY limit order is FILLED.
     * Expected: The order should match the buy order with quantity 10 (price 105.0) and be filled.
     */
    @Test
    public void testSellMarketOrderFilled() {
        MarketOrder marketOrder = new MarketOrder(OrderType.SELL, 10, "5678");
        ExecutionReport report = matchingEngine.processMarketOrder(marketOrder);

        assertEquals("FILLED", report.getStatus(), "Order should be FILLED");
        assertEquals(10, report.getInitialQuantity());
        assertEquals(10, report.getExecutedQuantity());
        assertEquals(105.0, report.getExecutedPrice());
        assertEquals("5678", report.getAccountId());
    }

    /**
     * Test that a BUY market order with a quantity that does not match any SELL limit order is REJECTED.
     * Expected: With no sell order having quantity 7, the order should be rejected.
     */
    @Test
    public void testBuyMarketOrderRejected() {
        MarketOrder marketOrder = new MarketOrder(OrderType.BUY, 7, "1111");
        ExecutionReport report = matchingEngine.processMarketOrder(marketOrder);

        assertEquals("REJECTED", report.getStatus(), "Order should be REJECTED");
        assertEquals(7, report.getInitialQuantity());
        assertNull(report.getExecutedPrice());
        assertNull(report.getExecutedQuantity());
        assertEquals("1111", report.getAccountId());
    }

    /**
     * Test that a SELL market order with a quantity that does not match any BUY limit order is REJECTED.
     * Expected: With no buy order having quantity 7, the order should be rejected.
     */
    @Test
    public void testSellMarketOrderRejected() {
        MarketOrder marketOrder = new MarketOrder(OrderType.SELL, 7, "2222");
        ExecutionReport report = matchingEngine.processMarketOrder(marketOrder);

        assertEquals("REJECTED", report.getStatus(), "Order should be REJECTED");
        assertEquals(7, report.getInitialQuantity());
        assertNull(report.getExecutedPrice());
        assertNull(report.getExecutedQuantity());
        assertEquals("2222", report.getAccountId());
    }

    /**
     * Test that a BUY market order with quantity matching an available SELL limit order of quantity 5 is FILLED.
     * Expected: Should match the sell order with quantity 5 (S1002 with price 107.0).
     */
    @Test
    public void testBuyMarketOrderFilledWithQuantity5() {
        MarketOrder marketOrder = new MarketOrder(OrderType.BUY, 5, "3333");
        ExecutionReport report = matchingEngine.processMarketOrder(marketOrder);

        assertEquals("FILLED", report.getStatus(), "Order should be FILLED");
        assertEquals(5, report.getInitialQuantity());
        assertEquals(5, report.getExecutedQuantity());
        assertEquals(107.0, report.getExecutedPrice());
        assertEquals("3333", report.getAccountId());
    }

    /**
     * Test that a SELL market order with quantity matching an available BUY limit order of quantity 5 is FILLED.
     * Expected: Should match the buy order with quantity 5 (B1002 with price 104.0).
     */
    @Test
    public void testSellMarketOrderFilledWithQuantity5() {
        MarketOrder marketOrder = new MarketOrder(OrderType.SELL, 5, "4444");
        ExecutionReport report = matchingEngine.processMarketOrder(marketOrder);

        assertEquals("FILLED", report.getStatus(), "Order should be FILLED");
        assertEquals(5, report.getInitialQuantity());
        assertEquals(5, report.getExecutedQuantity());
        assertEquals(104.0, report.getExecutedPrice());
        assertEquals("4444", report.getAccountId());
    }

    /**
     * Test sequential BUY market orders:
     * First, process a BUY market order for quantity 10 to fill the available sell order (S1001).
     * Then, process another BUY market order for quantity 10, which should be rejected as the matching order has been removed.
     */
    @Test
    public void testSequentialBuyMarketOrders() {
        MarketOrder firstOrder = new MarketOrder(OrderType.BUY, 10, "5555");
        ExecutionReport firstReport = matchingEngine.processMarketOrder(firstOrder);
        assertEquals("FILLED", firstReport.getStatus(), "First order should be FILLED");

        MarketOrder secondOrder = new MarketOrder(OrderType.BUY, 10, "6666");
        ExecutionReport secondReport = matchingEngine.processMarketOrder(secondOrder);
        assertEquals("REJECTED", secondReport.getStatus(), "Second order should be REJECTED");
    }

    /**
     * Test sequential SELL market orders:
     * First, process a SELL market order for quantity 5 to fill the available buy order (B1002).
     * Then, process another SELL market order for quantity 5, which should be rejected as the matching order has been removed.
     */
    @Test
    public void testSequentialSellMarketOrders() {
        MarketOrder firstOrder = new MarketOrder(OrderType.SELL, 5, "7777");
        ExecutionReport firstReport = matchingEngine.processMarketOrder(firstOrder);
        assertEquals("FILLED", firstReport.getStatus(), "First order should be FILLED");

        MarketOrder secondOrder = new MarketOrder(OrderType.SELL, 5, "8888");
        ExecutionReport secondReport = matchingEngine.processMarketOrder(secondOrder);
        assertEquals("REJECTED", secondReport.getStatus(), "Second order should be REJECTED");
    }

    /**
     * Test concurrent processing of multiple SELL market orders.
     * Only one SELL market order should be FILLED (matching the one BUY order with quantity 10) and the rest rejected.
     */
    @Test
    public void testConcurrentSellMarketOrders() throws InterruptedException, ExecutionException {
        int numThreads = 5;
        try (ExecutorService executor = Executors.newFixedThreadPool(numThreads)) {
            List<Future<ExecutionReport>> futures = new ArrayList<>();
            Callable<ExecutionReport> task = () -> {
                MarketOrder order = new MarketOrder(OrderType.SELL, 10, "concurrentSell");
                return matchingEngine.processMarketOrder(order);
            };

            for (int i = 0; i < numThreads; i++) {
                futures.add(executor.submit(task));
            }
            executor.shutdown();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), "Executor did not terminate in time");

            int filledCount = 0;
            int rejectedCount = 0;
            for (Future<ExecutionReport> future : futures) {
                ExecutionReport report = future.get();
                if ("FILLED".equals(report.getStatus())) {
                    filledCount++;
                } else if ("REJECTED".equals(report.getStatus())) {
                    rejectedCount++;
                }
            }

            assertEquals(1, filledCount, "Only one SELL market order should be filled.");
            assertEquals(numThreads - 1, rejectedCount, "The remaining SELL market orders should be rejected.");
        }
    }
}
