package kafka;

//import kafka.admin.AdminUtils;
//import kafka.utils.ZkUtils;
//import org.apache.kafka.common.security.JaasUtils;
//
//public class KafkaTools {
//
//    public static final String ZkStr = "192.168.0.107:2181";
//
//    public static void deleteKafkaTopic(String ZkStr, String topic) {
//        ZkUtils zkUtils = ZkUtils.
//                apply(ZkStr, 30000, 30000, JaasUtils.isZkSecurityEnabled());
//
//        AdminUtils.deleteTopic(zkUtils, topic);
//        zkUtils.close();
//    }
//
//    public static void main(String[] args) {
//        deleteKafkaTopic(ZkStr, "lc.lctest");
//    }
//
//}
