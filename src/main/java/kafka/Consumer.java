package kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.jetbrains.annotations.NotNull;
import pojo.Offset;
import proto.ConnectorMsg;
import service.OffsetService;
import tools.GsonTools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class Consumer {

    // 存储在kafka中好友的信息是按照好友id大小进行存储的，小的id在左端。大的在右端
    // 形如 1And2    999And10000
    public static List<ConnectorMsg.cMsgInfo> getMsgByFriend(int userid1, int userid2) {
        int max = Math.max(userid1, userid2);
        int min = Math.min(userid1, userid2);
        String topic = min + "And" + max;
        return getMsgByOffset(topic, 0);
    }

    public static List<ConnectorMsg.cMsgInfo> getMsgByUserid(int userid) {
        int offset = OffsetService.getOffset(userid);
        String topic = "toUser" + userid;
        if (offset != -1 && offset != -2) {
            System.out.println("成功查找到userid" + userid + " 的offset：" + offset);
            ArrayList<ConnectorMsg.cMsgInfo> msgByOffset = getMsgByOffset(topic, offset);
            offset += msgByOffset.size();
            return msgByOffset;
        } else {
            System.out.println("未查找到userid" + userid + " 的offset，将其初期化为0，并返回目标的所有消息");
            OffsetService.insertOffset(new Offset(userid, 0));
            return getMsgByOffset(topic, 0);
        }
    }

    public static void updateOffset(int userid, int oldOffset, int newOffset) {
        int offset = OffsetService.getOffset(userid);
        if (offset == oldOffset) {
            System.out.println("已更新userid" + userid + "的offset为" + offset);
            OffsetService.insertOffset(new Offset(userid, newOffset));
        } else {
            System.out.println("更新userid" + userid + "的offset失败" + oldOffset + "不等于" + offset);
        }
    }


    @NotNull
    private static Properties getProperties() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.0.107:9092");
        props.put("group.id", "test-group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("heartbeat.interval.ms", 1000); // 这个尽量时间可以短一点
        props.put("session.timeout.ms", 10 * 1000); // 如果说kafka broker在10秒内感知不到一个consumer心跳
        props.put("max.poll.interval.ms", 30 * 1000); // 如果30秒才去执行下一次poll
        // 就会认为那个consumer挂了，此时会触发rebalance
        // 如果说某个consumer挂了，kafka broker感知到了，会触发一个rebalance的操作，就是分配他的分区
        // 给其他的cosumer来消费，其他的consumer如果要感知到rebalance重新分配分区，就需要通过心跳来感知
        // 心跳的间隔一般不要太长，1000，500
        props.put("fetch.max.bytes", 10485760);
        props.put("max.poll.records", 500); // 如果说你的消费的吞吐量特别大，此时可以适当提高一些
        // 开启自动提交，他只会每隔一段时间去提交一次offset
        // 如果你每次要重启一下consumer的话，他一定会把一些数据重新消费一遍
        props.put("enable.auto.commit", "false");
        // 每次自动提交offset的一个时间间隔
        props.put("auto.commit.interval.ms", "1000");
        // 每次重启都是从最早的offset开始读取，不是接着上一次
        props.put("auto.offset.reset", "earliest");
        return props;
    }

    public static ArrayList<ConnectorMsg.cMsgInfo> getMsgByOffset(String topic, int offset) {
        ArrayList<ConnectorMsg.cMsgInfo> resp = new ArrayList<>();
        Properties props = getProperties();
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            seekFrom(consumer, offset, topic);
            // 100 是超时时间（ms），在该时间内 poll 会等待服务器返回数据
            ConsumerRecords<String, String> records = consumer.poll(200);
            // poll 返回一个记录列表。
            // 每条记录都包含了记录所属主题的信息、记录所在分区的信息、记录在分区里的偏移量，以及记录的键值对。
            for (ConsumerRecord<String, String> record : records) {
                resp.add(GsonTools.getGson().fromJson(record.value(), ConnectorMsg.cMsgInfo.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resp;
    }

    static void seekFrom(KafkaConsumer<String, String> consumer, int offset, String topic) {
        consumer.assign(Collections.singletonList(new TopicPartition(topic, 0)));
        consumer.seek(new TopicPartition(topic, 0), offset);//不改变当前offset
    }
}