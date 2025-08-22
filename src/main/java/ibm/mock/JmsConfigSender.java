package ibm.mock;

import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.mq.jakarta.jms.MQQueueConnectionFactory;
import com.ibm.mq.spring.boot.MQConfigurationProperties;
import com.ibm.mq.spring.boot.MQConnectionFactoryCustomizer;
import com.ibm.mq.spring.boot.MQConnectionFactoryFactory;
import com.ibm.msg.client.jakarta.wmq.WMQConstants;
import ibm.mock.models.Mq;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableJms
public class JmsConfigSender {

    Logger logger = LoggerFactory.getLogger(QueueConsumer.class);

    @Autowired
    private ObjectProvider<List<MQConnectionFactoryCustomizer>> factoryCustomizers;

    @Autowired
    private QmProperties qmProperties;

    Map<String, JmsTemplate> jmsMapTemplates = new ConcurrentHashMap<>();

//    @SneakyThrows
//    @Bean
//    public MQQueueConnectionFactory getConnectionFactory() {
//        MQQueueConnectionFactory mqQueueConnectionFactory = new MQQueueConnectionFactory();
////        mqQueueConnectionFactory.setHostName(connName);
////        mqQueueConnectionFactory.setConnectionNameList();
//
//        mqQueueConnectionFactory.setHostName("vs2513.imb.ru");
//        mqQueueConnectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
//        mqQueueConnectionFactory.setChannel("SYSTEM.ADMIN.SVRCONN");
//        mqQueueConnectionFactory.setPort(1415);
//        mqQueueConnectionFactory.setQueueManager("QM_BSSADAP61");
//
////        mqQueueConnectionFactory.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
//        mqQueueConnectionFactory.setStringProperty(WMQConstants.USERID, "testperfeuc");
//        mqQueueConnectionFactory.setStringProperty(WMQConstants.PASSWORD, "kBBS4N6uU5szWSkMIaA333o7");
//
//        return mqQueueConnectionFactory;
//    }
//
//    @Bean
//    @Primary
//    public JmsTemplate jmsTemplateFactory(MQQueueConnectionFactory mqQueueConnectionFactory) {
//        JmsTemplate jmsTemplate = new JmsTemplate(mqQueueConnectionFactory);
////        jmsTemplate.setReceiveTimeout(1000L);
//        return jmsTemplate;
//    }

//    @Bean
//    public List<MQQueueConnectionFactory> factories() {
//        List<MQConnectionFactory> factories = new ArrayList<>();
//        for (MQConfigurationProperties properties : qmProperties.getListMQConfigurationProperties()) {
//            String queueManager = properties.getQueueManager();
//            List<String> destinations = qmProperties.getDest(properties.getQueueManager());
//            MQConnectionFactory connectionFactory = new MQConnectionFactoryFactory(properties, factoryCustomizers.getIfAvailable()).createConnectionFactory(MQConnectionFactory.class);
//        }
//    }

//    @Bean
//    public List<JmsTemplate> jmsTemplateFactory() {
//        List<JmsTemplate> jmsTemplates = new ArrayList<>();
//        for (MQConfigurationProperties properties : qmProperties.getListMQConfigurationProperties()) {
//            String queueManager = properties.getQueueManager();
//            List<String> destinations = qmProperties.getDest(properties.getQueueManager());
////            MQConnectionFactory connectionFactory = new MQConnectionFactoryFactory(properties, factoryCustomizers.getIfAvailable()).createConnectionFactory(MQConnectionFactory.class);
////            JmsTemplate jmsTemplate = new JmsTemplate(MQConnectionFactory);
//            JmsTemplate jmsTemplate = new JmsTemplate(new MQConnectionFactoryFactory(properties, factoryCustomizers.getIfAvailable()).createConnectionFactory(MQConnectionFactory.class));
//            jmsMapTemplates.put(queueManager, jmsTemplate);
//            jmsTemplates.add(jmsTemplate);
//        }
//        return jmsTemplates;
//    }

    @SneakyThrows
    @Bean
    public List<JmsTemplate> jmsTemplateFactory() {
        List<JmsTemplate> jmsTemplates = new ArrayList<>();
        for (Mq mq : qmProperties.getMqs()) {
            for (int i = 0; i < mq.getDestinationWithProperties().size(); i++) {
//                System.out.println(mq.getDestinationWithProperties().get(i).getRequestQueue());
                MQQueueConnectionFactory mqQueueConnectionFactory = new MQQueueConnectionFactory();

                mqQueueConnectionFactory.setHostName(mq.getHost());
                mqQueueConnectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
                mqQueueConnectionFactory.setChannel(mq.getChannel());
                mqQueueConnectionFactory.setPort(mq.getPort());
                mqQueueConnectionFactory.setQueueManager(mq.getQueueManager());

                mqQueueConnectionFactory.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
                mqQueueConnectionFactory.setStringProperty(WMQConstants.USERID, mq.getUser());
                mqQueueConnectionFactory.setStringProperty(WMQConstants.PASSWORD, mq.getPassword());
                mqQueueConnectionFactory.setStringProperty(WMQConstants.JMS_IBM_CHARACTER_SET, mq.getCharset());

                if (mq.getSslConnect()!=null) {
                    // Set SSL properties
                    mqQueueConnectionFactory.setStringProperty(WMQConstants.WMQ_SSL_CIPHER_SUITE,"*TLS12");  // *TLS12
                    System.setProperty("com.ibm.mq.cfg.useIBMCipherMappings", "false"); // for Oracle Java
                    System.setProperty("javax.net.ssl.trustStore", mq.getSslTrustStore());
                    System.setProperty("javax.net.ssl.trustStorePassword", mq.getSslTrustStorePassword());
                }
//                mqQueueConnectionFactory.setAsyncExceptions(1);
                JmsTemplate jmsTemplate = new JmsTemplate(mqQueueConnectionFactory);
//                jmsTemplate.setDeliveryDelay(mq.getDestinationWithProperties().get(i).getSleep()*1000);
                jmsTemplates.add(jmsTemplate);

                jmsMapTemplates.put(mq.getQueueManager() + "/" + mq.getDestinationWithProperties().get(i).getResponseQueue(), jmsTemplate);
            }
        }
//        System.out.println("!!!!!!!!!!!!!!!!!!!");
//        jmsMapTemplates.keySet().stream().forEach(System.out::println);
        return jmsTemplates;
    }
}