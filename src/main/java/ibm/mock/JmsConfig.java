//package ibm.mock;
//
//import com.ibm.mq.jakarta.jms.MQConnectionFactory;
//import com.ibm.mq.spring.boot.MQConnectionFactoryCustomizer;
//
//import com.ibm.msg.client.jakarta.jms.JmsConnectionFactory;
//import com.ibm.msg.client.jakarta.jms.JmsConstants;
//import com.ibm.msg.client.jakarta.jms.JmsFactoryFactory;
//import com.ibm.msg.client.jakarta.wmq.WMQConstants;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
//import org.springframework.jms.config.JmsListenerEndpointRegistrar;
//import org.springframework.jms.config.JmsListenerEndpointRegistry;
//import org.springframework.jms.connection.CachingConnectionFactory;
//import org.springframework.jms.core.JmsTemplate;
//
//import javax.jms.JMSException;
//import java.util.List;
//
//@Configuration
//public class JmsConfig {
//    @Value("${ibm.mq.host}")
//    private String host;
//
//    @Value("${ibm.mq.port}")
//    private Integer port;
//
//    @Value("${ibm.mq.queueManager}")
//    private String queueManager;
//
//    @Value("${ibm.mq.channel}")
//    private String channel;
//
//    @Value("${ibm.mq.user}")
//    private String user;
//
//    @Value("${ibm.mq.password}")
//    private String password;
//
////    @Value("${ibm.mq.connName}")
////    private String connName;
//
//    @Value("${ibm.queues.sampleQueues}")
//    private List<String> sampleQueues;
//
//    @Value("${demo.concurrency.size.low}")
//    private String concurrencyMin;
//
//    @Value("${demo.concurrency.size.high}")
//    private String concurrencyMax;
//
//    @Bean
//    public JmsTemplate jmsTemplate() throws JMSException, jakarta.jms.JMSException {
//        JmsTemplate jmsTemplate = new JmsTemplate();
//        jmsTemplate.setConnectionFactory(cachingConnectionFactory());
////        jmsTemplate.setDeliveryDelay(1);
//        return jmsTemplate;
//    }
//
//    @Bean
//    public CachingConnectionFactory cachingConnectionFactory() throws JMSException, jakarta.jms.JMSException {
//        CachingConnectionFactory factory = new CachingConnectionFactory();
//        factory.setSessionCacheSize(1);
//        factory.setTargetConnectionFactory(createConnectionFactory());
//        factory.setReconnectOnException(true);
//        factory.afterPropertiesSet();
//        return factory;
//    }
//
//    @Bean
//    public JmsConnectionFactory createConnectionFactory() throws JMSException, jakarta.jms.JMSException {
//        JmsFactoryFactory ff = JmsFactoryFactory.getInstance(JmsConstants.JAKARTA_WMQ_PROVIDER);
//        JmsConnectionFactory factory = ff.createConnectionFactory();
//        factory.setObjectProperty(WMQConstants.WMQ_CONNECTION_MODE, Integer.valueOf(WMQConstants.WMQ_CM_CLIENT));
//        factory.setStringProperty(WMQConstants.WMQ_HOST_NAME, host);
//        factory.setObjectProperty(WMQConstants.WMQ_PORT, port);
//        factory.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, queueManager);
//        factory.setStringProperty(WMQConstants.WMQ_CHANNEL, channel);
//        factory.setStringProperty(WMQConstants.USERID, user);
//        factory.setStringProperty(WMQConstants.PASSWORD, password);
//        return factory;
//    }
//
//
////    @Bean
////    @Primary
////    public JmsListenerEndpointRegistry createRegistry() {
////        JmsListenerEndpointRegistry registry = new JmsListenerEndpointRegistry();
////        return registry;
////    }
////
////    @Bean
////    public JmsListenerEndpointRegistrar createRegistrar() throws JMSException, jakarta.jms.JMSException {
////        JmsListenerEndpointRegistrar registrar = new JmsListenerEndpointRegistrar();
////        registrar.setEndpointRegistry(createRegistry());
////        registrar.setContainerFactory(createDefaultJmsListenerContainerFactory());
////        return registrar;
////    }
////
////    public DefaultJmsListenerContainerFactory createDefaultJmsListenerContainerFactory() throws JMSException, jakarta.jms.JMSException {
////        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
////        factory.setConnectionFactory(createConnectionFactory());
////        return factory;
////    }
//}
