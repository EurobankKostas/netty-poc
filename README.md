# Poc-Netty

A simple TCP-based matching engine built with Spring Boot. It processes MARKET orders (BUY/SELL) by matching them against a pre-populated order book.

## Features
- **Order Book:** Pre-set BUY and SELL orders.
- **Sorting:** BUY orders sorted by highest price, SELL by lowest; FIFO for same price.
- **Matching:** Only fully matched orders are executed; unmatched orders are rejected.

## How to Run
Start the application with:
```bash
mvn spring-boot:run
mvn test

