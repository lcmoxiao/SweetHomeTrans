package handler;


import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import kafka.Consumer;
import kafka.Producer;
import proto.ConnectorMsg;
import service.GroupService;
import tools.CmsgUtils;
import tools.GsonTools;

import java.util.List;

import static tools.MsgChangeTools.changeTransMsgMark;


@ChannelHandler.Sharable
public class TransHandler extends ChannelHandlerAdapter {

    private final static Producer producer = new Producer();
    public static String msg;
    public static String topic;

    static {
        producer.start();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        ConnectorMsg.cMsgInfo cmsg = (ConnectorMsg.cMsgInfo) msg;
        if (cmsg.getCMsgType() == ConnectorMsg.cMsgInfo.CMsgType.TRANS) {
            ConnectorMsg.Trans trans = cmsg.getTrans();
            int msgMark = trans.getMsgMark();
            int userid = trans.getDstUserid();
            int groupid = trans.getGroupid();

            if (msgMark == 1) {
                if (!trans.getIsGroupMsg()) {
                    System.out.println("转发单体消息给" + userid);
                    ConnectorMsg.cMsgInfo cacheMsg = changeTransMsgMark(cmsg, 2);
                    singleTrans(ctx, cacheMsg);
                } else {
                    System.out.println("转发群消息");
                    groupTrans(groupid, ctx, cmsg);
                }
            } else if (msgMark == 4) {
                System.out.println("userid:" + userid + "反馈接收了消息");
                int oldOffset = trans.getMsgID();
                int newOffset = trans.getMsgSize() + trans.getMsgID();
                Consumer.updateOffset(userid, oldOffset, newOffset);
            }
        } else if (cmsg.getCMsgType() == ConnectorMsg.cMsgInfo.CMsgType.CONNECT) {
            offlineTrans(cmsg.getConnect().getUserid(), ctx);
        } else {
            ctx.fireChannelRead(msg);
        }
    }


    void groupTrans(int groupID, ChannelHandlerContext ctx, ConnectorMsg.cMsgInfo cmsg) {
        GroupService.getMatchGroupRelationsByGroupID(groupID).forEach(v -> {
            if (cmsg.getTrans().getSrcUserid() != v.getUserid()) {
                ctx.writeAndFlush(CmsgUtils.generateTransMsgByMsgMark(99, cmsg.getTrans().getDstUserid()));
            }
        });
    }


    //一律加入到消息缓冲池，待接收成功后，进行清除
    void singleTrans(ChannelHandlerContext ctx, ConnectorMsg.cMsgInfo cmsg) {
        doProduce(cmsg);
        ctx.writeAndFlush(CmsgUtils.generateTransMsgByMsgMark(99, cmsg.getTrans().getDstUserid()));
    }

    //Todo 消费时 将msgId填上offset
    private void doConsume(){

    }

    private void doProduce(ConnectorMsg.cMsgInfo cmsg) {
        ConnectorMsg.Trans trans = cmsg.getTrans();
        int srcUserid = trans.getSrcUserid();
        int dstUserid = trans.getDstUserid();
        producer.send("toUser" + dstUserid, GsonTools.getGson().toJson(cmsg));
        //如果不是匹配信息则永久储存
        if (!cmsg.getTrans().getIsMatchMsg()) {
            int max = Math.max(srcUserid, dstUserid);
            int min = Math.min(srcUserid, dstUserid);
            String topic = min + "And" + max;
            producer.send(topic, GsonTools.getGson().toJson(cmsg));
        }
    }

    //Todo 可以尝试扩展msgMark来支持 List<cMsgInfo>的集合传输
    void offlineTrans(int userID, ChannelHandlerContext ctx) {
        System.out.println("得到Connect信息,准备搜索离线消息进行转发");
        List<ConnectorMsg.cMsgInfo> cMsgInfos = Consumer.getMsgByUserid(userID);
        if (cMsgInfos.size() != 0) {
            for (ConnectorMsg.cMsgInfo cMsgInfo : cMsgInfos) {
                ctx.writeAndFlush(cMsgInfo);
            }
            System.out.println(userID + "离线消息转发完成");
        } else {
            System.out.println(userID + "没有离线消息");
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
