package com.limyel.tea.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeaHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeaHttpServerHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        LOGGER.debug(fullHttpRequest.method().name() + "fjlskjdflksjdflksjdflks");
    }
}
