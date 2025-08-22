package ibm.mock;

import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.mq.spring.boot.MQConfigurationProperties;
import com.ibm.mq.spring.boot.MQConnectionFactoryCustomizer;
import com.ibm.mq.spring.boot.MQConnectionFactoryFactory;
import lombok.SneakyThrows;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;


import java.util.List;

@Configuration
@EnableJms
public class JmsConfigListener implements JmsListenerConfigurer {

    @Autowired
    private ObjectProvider<List<MQConnectionFactoryCustomizer>> factoryCustomizers;

    @Autowired
    private DefaultJmsListenerContainerFactoryConfigurer configurer;

    @Autowired
    private QmProperties qmProperties;

    @Autowired
    private QueueConsumer queueConsumer;

    @SneakyThrows
    @Override
    public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
        for (MQConfigurationProperties properties : qmProperties.getListMQConfigurationProperties()) {
            String queueManager = properties.getQueueManager();
            List<String> destinations = qmProperties.getDest(properties.getQueueManager());
            MQConnectionFactory connectionFactory = new MQConnectionFactoryFactory(properties, factoryCustomizers.getIfAvailable()).createConnectionFactory(MQConnectionFactory.class);
            connectionFactory.getAsyncExceptions();
//            if (mq.getSslConnect()) {
//                // Set SSL properties
//                connectionFactory.setStringProperty(WMQConstants.WMQ_SSL_CIPHER_SUITE,"*TLS12");  // *TLS12
//                System.setProperty("com.ibm.mq.cfg.useIBMCipherMappings", "false"); // for Oracle Java
//                System.setProperty("javax.net.ssl.trustStore", mq.getSslTrustStore());
//                System.setProperty("javax.net.ssl.trustStorePassword", mq.getSslTrustStorePassword());
//            }

            DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
            configurer.configure(factory, connectionFactory);
            SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
            for (int i = 0; i < destinations.size(); i++) {
                endpoint.setId("jmsEndpoint-" + queueManager);
                endpoint.setDestination(destinations.get(i));
                endpoint.setMessageListener(message -> {

                    queueConsumer.onMessage(message);

                });
            }

            registrar.registerEndpoint(endpoint, factory);

        }
    }

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
//        return jmsTemplate;
//    }
//
//    @Bean
//    public JmsListenerContainerFactory jmsListenerContainerFactory(MQQueueConnectionFactory mqQueueConnectionFactory) {
//        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
//        factory.setConnectionFactory(mqQueueConnectionFactory);
//        return factory;
//    }


}