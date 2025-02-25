package com.pocnetty.infrastructure.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocnetty.domain.ExecutionReport;
import com.pocnetty.domain.MatchingEngine;
import com.pocnetty.domain.dto.MarketOrder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MatchingEngineHandler extends ChannelInboundHandlerAdapter {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final MatchingEngine matchingEngine = new MatchingEngine();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String json = (String) msg;
        try {
            MarketOrder order = mapper.readValue(json, MarketOrder.class);
            ExecutionReport report = matchingEngine.processMarketOrder(order);
            String response = mapper.writeValueAsString(report) + "\n";
            ctx.writeAndFlush(response);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ExecutionReport errorReport = new ExecutionReport(0, null, null, "unknown", "REJECTED");
            ctx.writeAndFlush(mapper.writeValueAsString(errorReport) + "\n");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(), cause);
        ctx.close();
    }
}
