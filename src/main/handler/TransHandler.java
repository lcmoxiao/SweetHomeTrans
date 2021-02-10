package main.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import main.proto.ConnectorMsg;
import main.userstat.GroupService;
import main.userstat.UserStateService;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


@ChannelHandler.Sharable
public class TransHandler extends ChannelHandlerAdapter {

    final static HashMap<Integer, LinkedList<ConnectorMsg.cMsgInfo>> msgCaches = new HashMap<>();
    final static UserStateService userStateService = new UserStateService();

    public static void main(String[] args) {
        boolean online = userStateService.isOnline(1);
        System.out.println(online);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ConnectorMsg.cMsgInfo cmsg = (ConnectorMsg.cMsgInfo) msg;
        if (cmsg.getCMsgType() == ConnectorMsg.cMsgInfo.CMsgType.TRANS) {
            System.out.println("得到Trans信息");
            int userid = cmsg.getTrans().getDstUserid();
            int groupid = cmsg.getTrans().getDstGroupid();

            if (groupid == 0) {
                singleTrans(userid, ctx, cmsg);
            } else {
                groupTrans(groupid, ctx, cmsg);
            }


        } else if (cmsg.getCMsgType() == ConnectorMsg.cMsgInfo.CMsgType.CONNECT) {
            offlineTrans(cmsg.getTrans().getDstUserid(), ctx);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    void groupTrans(int groupID, ChannelHandlerContext ctx, ConnectorMsg.cMsgInfo cmsg) {
        ConnectorMsg.cMsgInfo.Builder b1 = cmsg.toBuilder();
        ConnectorMsg.Trans.Builder b2 = cmsg.getTrans().toBuilder();
        GroupService.getMatchGroupRelationsByGroupID(groupID).forEach(v -> {
            b2.setDstUserid(v.getUserid());
            b1.setTrans(b2);
            ctx.writeAndFlush(b1.build());
        });
    }

    void singleTrans(int userid, ChannelHandlerContext ctx, ConnectorMsg.cMsgInfo cmsg) {
        if (userStateService.isOnline(userid)) {
            System.out.println(userid + "在线，所以直接返回connector进行转发");
            ctx.writeAndFlush(cmsg);
        } else {
            System.out.println(userid + "离线，所以存入消息队列,等待上线进行转发");
            if (msgCaches.containsKey(userid)) {
                if (cmsg.getTrans().getMsgMark() == 1)
                    msgCaches.get(userid).addFirst(cmsg);
                else msgCaches.get(userid).addLast(cmsg);
            } else {
                LinkedList<ConnectorMsg.cMsgInfo> l = new LinkedList<>();
                l.add(cmsg);
                msgCaches.put(userid, l);
            }
        }
    }

    void offlineTrans(int userID, ChannelHandlerContext ctx) {
        System.out.println("得到Connect信息,准备搜索离线消息进行转发");
        List<ConnectorMsg.cMsgInfo> cMsgInfos = msgCaches.get(userID);
        if (cMsgInfos != null) {
            while (!cMsgInfos.isEmpty()) {
                ConnectorMsg.cMsgInfo cMsgInfo = cMsgInfos.get(0);
                if (ctx.writeAndFlush(cMsgInfo).isSuccess()) cMsgInfos.remove(0);
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
