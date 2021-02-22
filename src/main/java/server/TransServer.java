package server;


import handler.TransHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import proto.ConnectorMsg;


public class TransServer {


    static Channel channel;

    public static void main(String[] args) throws Exception {
        int port = 8082;
        if (args != null && args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }
        new TransServer().bind(port);
    }

    public void bind(int port) throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) throws Exception {
                            ChannelHandler[] handlers = new ChannelHandler[]{
                                    new ProtobufVarint32FrameDecoder(),
                                    new ProtobufDecoder(ConnectorMsg.cMsgInfo.getDefaultInstance()),
                                    new ProtobufVarint32LengthFieldPrepender(),
                                    new ProtobufEncoder(),
                                    new TransHandler()
                            };
                            sc.pipeline().addLast(handlers);
                        }
                    });
            ChannelFuture f = b.bind(port).sync();
            channel = f.channel();
            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }


}
