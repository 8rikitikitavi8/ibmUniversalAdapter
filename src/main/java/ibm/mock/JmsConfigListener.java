package ibm.mock;

import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.mq.spring.boot.MQConfigurationProperties;
import com.ibm.mq.spring.boot.MQConnectionFactoryCustomizer;
import com.ibm.mq.spring.boot.MQConnectionFactoryFactory;
import jakarta.jms.Session;
import lombok.SneakyThrows;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
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
    @Autowired
    private TaskExecutor jmsAsyncExecutor;

    @SneakyThrows
    @Override
    public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
        for (MQConfigurationProperties properties : qmProperties.getListMQConfigurationProperties()) {
            String queueManager = properties.getQueueManager();
            List<String> destinations = qmProperties.getDest(properties.getQueueManager());
            MQConnectionFactory connectionFactory =
                    new MQConnectionFactoryFactory(properties, factoryCustomizers.getIfAvailable())
                            .createConnectionFactory(MQConnectionFactory.class);

            DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
            factory.setConcurrency("15-20");
//            factory.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);

            factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE); // Важно!
            factory.setTaskExecutor(jmsAsyncExecutor); // Используем асинхронный executor
            configurer.configure(factory, connectionFactory);

//            for (String destination : destinations) {
//                SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
//                endpoint.setId("jmsEndpoint-" + queueManager + "-" + destination);
//                endpoint.setDestination(destination);
//                endpoint.setMessageListener(queueConsumer::onMessage);
//                registrar.registerEndpoint(endpoint, factory);
//            }
            for (String destination : destinations) {
                SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
                endpoint.setId("jmsEndpoint-" + queueManager + "-" + destination);
                endpoint.setDestination(destination);
                endpoint.setMessageListener(queueConsumer);
                registrar.registerEndpoint(endpoint, factory);
            }
        }
    }
}