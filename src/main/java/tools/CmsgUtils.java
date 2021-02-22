package tools;

import proto.ConnectorMsg;

public class CmsgUtils {

    public static ConnectorMsg.cMsgInfo generateTransMsgByMsgMark(int msgMark, int dstid) {
        ConnectorMsg.cMsgInfo.Builder builder = ConnectorMsg.cMsgInfo.newBuilder();
        ConnectorMsg.Trans.Builder builder1 = ConnectorMsg.Trans.newBuilder();
        builder1.setMsgMark(msgMark);
        builder1.setDstUserid(dstid);
        builder.setTrans(builder1);
        return builder.build();
    }
}
