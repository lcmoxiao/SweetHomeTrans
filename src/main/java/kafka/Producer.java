package kafka;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.locks.LockSupport;

import static handler.TransHandler.msg;
import static handler.TransHandler.topic;


public class Producer extends Thread {

    private static KafkaProducer<String, String> kafkaProducer;

    public static KafkaProducer<String, String> getInstance() {
        if (kafkaProducer == null)
            synchronized (Producer.class) {
                if (kafkaProducer == null) {
                    kafkaProducer = getKafkaProducer();
                }
            }
        return kafkaProducer;
    }

    private static KafkaProducer<String, String> getKafkaProducer() {
        Properties p = new Properties();
        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.0.107:9092");//kafka地址，多个地址用逗号分割
        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new KafkaProducer<>(p);
    }

    public static void close() {
        kafkaProducer.close();
        kafkaProducer = null;
    }

    @Override
    public void run() {
        System.out.println("Producer启动成功");
        while (true) {
            LockSupport.park();
            send(topic, msg);
        }
    }

    public void send(String topic, String msg) {
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, msg);
        getInstance().send(record);
    }

}
