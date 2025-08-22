//package ibm.mock;
//
//import com.ibm.disthub2.client.Message;
//import com.ibm.mq.spring.boot.MQConfigurationProperties;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jms.config.JmsListenerEndpointRegistrar;
//import org.springframework.jms.config.SimpleJmsListenerEndpoint;
//import javax.annotation.PostConstruct;
//
//
//
//@Configuration
//public class MqConfig {
//
//    @Autowired
//    JmsListenerEndpointRegistrar registrar;
//
//    @Autowired
//    MessageHandlerIbmMq queueController;
//
//    @Value("${ibm.queues.sampleQueues}")
//    String[] sampleQueues;
//
//    @Value("${demo.concurrency.size.low}")
//    Integer messageConcurrencyLow;
//
//    @Value("${demo.concurrency.size.high}")
//    Integer messageConcurrencyHigh;
//
//    String jmsMessageConcurrency = "";
//
//    @PostConstruct
//    public void init() {
//        jmsMessageConcurrency = String.format("%s-%s", messageConcurrencyLow, messageConcurrencyHigh);
////        System.out.println(jmsMessageConcurrency);
//        configureJmsListeners(registrar);
//    }
//
//
//    public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
//        int i = 0;
//        for (final String queueName : sampleQueues) {
//            SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
//            endpoint.setId("demo-" + i++);
//            endpoint.setDestination(queueName);
//            endpoint.setConcurrency(jmsMessageConcurrency);
//
////            System.out.println(endpoint);
////            MQConfigurationProperties properties
//
//            endpoint.setMessageListener(message -> {
//                queueController.recv(queueName, (Message) message);
//            });
//            registrar.registerEndpoint(endpoint);
//        }
//    }
//}
