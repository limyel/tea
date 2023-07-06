package com.limyel.tea.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeaServer {

    private final static Logger LOGGER = LoggerFactory.getLogger(TeaServer.class);

    private static EventLoopGroup boss = new NioEventLoopGroup();
    private static EventLoopGroup worker = new NioEventLoopGroup();

    public static void start() throws InterruptedException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        serverBootstrap
                .group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new TeaChildHandler());

        ChannelFuture channelFuture = serverBootstrap.bind(8080).sync();
        if (channelFuture.isSuccess()) {
        }
    }

}
