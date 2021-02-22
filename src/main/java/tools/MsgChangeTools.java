package tools;

import proto.ConnectorMsg;

public class MsgChangeTools {

    public static ConnectorMsg.cMsgInfo changeTransMsgMark(ConnectorMsg.cMsgInfo cmsg, int msgMark) {
        ConnectorMsg.cMsgInfo.Builder builder = cmsg.toBuilder();
        ConnectorMsg.Trans.Builder transBuilder = builder.getTransBuilder();
        transBuilder.setMsgMark(msgMark);
        builder.setTrans(transBuilder);
        return builder.build();
    }
}



