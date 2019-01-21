package com.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.URI;
import java.text.MessageFormat;
import java.util.UUID;

/**
 * @Author: baozi
 * @Description:
 * @Date: Created in 11:08 2018/8/15
 */
@Component
public class LeadClientHandler {

    @Value("${server.nettyHost}")
    private String host;

    @Value("${server.nettyPort}")
    private int port;

    private EventLoopGroup group = new NioEventLoopGroup(10);
    private Channel channel = null;

    public Channel start() throws Exception{
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpClientCodec());
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            pipeline.addLast(new WebSocketClientProtocolHandler(
                                    WebSocketClientHandshakerFactory.newHandshaker(
                                            new URI("ws://" + host + ":" + port), WebSocketVersion.V13, null, true, EmptyHttpHeaders.INSTANCE, 6553600)));
                            pipeline.addLast(new EchoClientHandler());
                        }
                    })
                    .option(ChannelOption.ALLOW_HALF_CLOSURE,true);
            ChannelFuture f = bootstrap.connect().syncUninterruptibly();
            channel = f.channel();
            if (f.isDone() && f.isSuccess()) {
                return channel;
            } else {
                return null;
            }
        }catch (Exception ex){
            System.out.println("exception: " + ex);
            group.shutdownGracefully().sync();
        }
        return channel;
    }

    public boolean send(String msg){
        var future = channel.writeAndFlush(new TextWebSocketFrame(msg));
        future.addListener((ChannelFutureListener) future1 -> System.out.print(MessageFormat.format("success send message {0}",msg)));
        return true;
    }

    public String buildAck(){
        return MessageFormat.format("ACK_MESSAGE:{0}", UUID.randomUUID());
    }
}
