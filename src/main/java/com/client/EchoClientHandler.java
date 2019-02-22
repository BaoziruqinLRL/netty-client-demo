package com.client;

import com.alibaba.fastjson.JSON;
import com.data.TransferData;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: baozi
 * @Description:
 * @Date: Created in 11:04 2018/8/15
 */
@ChannelHandler.Sharable
public class EchoClientHandler extends ChannelInboundHandlerAdapter {

    private static final int LOST_CONNECT = 3;

    private final AtomicInteger heartCount = new AtomicInteger(0);

    private static final Logger log = LoggerFactory.getLogger(EchoClientHandler.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE){
            System.out.println("WebSocket Client connected! response headers[sec-websocket-extensions]\r\n");
        } else if (evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.ALL_IDLE){
                if (heartCount.get() == LOST_CONNECT){
                    ctx.close().sync();
                    log.warn(MessageFormat.format("client {0} lost connect because heartbeat not response\r\n",ctx.channel()));
                }else{
                    ctx.writeAndFlush(new TextWebSocketFrame("ping"));
                    heartCount.incrementAndGet();
                }
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        FullHttpResponse response;
        if (msg instanceof FullHttpResponse) {
            response = (FullHttpResponse) msg;
            throw new IllegalStateException("Unexpected FullHttpResponse (getStatus=" + response.status() + ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        } else if (msg instanceof TextWebSocketFrame){
            TextWebSocketFrame textFrame = (TextWebSocketFrame) msg;
            String text = textFrame.text();
            if ("ping".equals(text)){
                ctx.writeAndFlush(new TextWebSocketFrame("pong"));
            }else if ("pong".equals(text)){
                heartCount.set(0);
            }else if (text.startsWith("ACK_MESSAGE")){
                System.out.print(MessageFormat.format("i receive ack {0}\r\n",textFrame.text()));
            }else{
                var transferData = JSON.parseObject(textFrame.text(), TransferData.class);
                if (transferData.getAck() != null){
                    ctx.writeAndFlush(new TextWebSocketFrame(transferData.getAck()));
                }
                System.out.print(MessageFormat.format("I receive message {0}\r\n",transferData.getContent()));
            }
        }
    }
}
