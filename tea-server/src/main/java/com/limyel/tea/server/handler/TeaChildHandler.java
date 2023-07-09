package com.limyel.tea.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

public class TeaChildHandler extends ChannelInitializer<Channel> {
    @Override
    protected void initChannel(Channel channel) throws Exception {
        channel.pipeline()
                // 解码 HTTP 请求的 handler
                .addLast(new HttpRequestDecoder())
                // HTTP 对象聚合组件
                .addLast(new HttpObjectAggregator(65536))
                // 编码 HTTP 响应的 handler
                .addLast(new HttpResponseEncoder())
                .addLast(new ChunkedWriteHandler())
                .addLast(new TeaHttpServerHandler());
    }
}
