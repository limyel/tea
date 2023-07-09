package com.limyel.tea.server;

import ch.qos.logback.classic.LoggerContext;
import com.limyel.tea.server.handler.TeaChildHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeaServer {

    private final static Logger LOGGER = LoggerFactory.getLogger(TeaServer.class);

    private static EventLoopGroup boss;
    private static EventLoopGroup worker;

    public static final int DEFAULT_PORT = 5940;

    public static void start() {
        start(DEFAULT_PORT);
    }

    public static void start(int port) {
        disableNettyLog();
        boss = new NioEventLoopGroup();
        worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new TeaChildHandler());

            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            if (channelFuture.isSuccess()) {
                LOGGER.info("TeaServer started on port: {}", port);
            }
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    private static void disableNettyLog() {
        LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger("io.netty");
        rootLogger.setLevel(ch.qos.logback.classic.Level.OFF);
    }

    public static void main(String[] args) {
        TeaServer.start();
    }

}
