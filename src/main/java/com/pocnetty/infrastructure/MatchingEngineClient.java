package com.pocnetty.infrastructure;// MatchingEngineClient.java
import com.pocnetty.domain.dto.MarketOrder;
import com.pocnetty.domain.enums.OrderType;
import com.pocnetty.infrastructure.handlers.MatchingEngineClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MatchingEngineClient {
    private final String host;
    private final int port;

    public MatchingEngineClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Unpooled.wrappedBuffer("\n".getBytes())));
                            pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
                            pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
                            pipeline.addLast(new MatchingEngineClientHandler());
                        }
                    });

            ChannelFuture f = b.connect(host, port).sync();
            MarketOrder order = new MarketOrder(OrderType.BUY, 10, "1233");
            ObjectMapper mapper = new ObjectMapper();
            String jsonOrder = mapper.writeValueAsString(order) + "\n";
            f.channel().writeAndFlush(jsonOrder);
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
