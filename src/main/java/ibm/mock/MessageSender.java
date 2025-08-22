package ibm.mock;

import ibm.mock.common.Util;
import jakarta.jms.Message;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import java.util.Random;

import static ibm.mock.Main.writeToInflux;

//import static ibm.mock.Main.writeToInflux;

@Component
public class MessageSender {
//    @Autowired
//    JmsTemplate jmsTemplate;
Logger logger = LoggerFactory.getLogger(QueueConsumer.class);
    @Autowired
    private QmProperties qmProperties;
    @Autowired
    private JmsConfigSender jmsConfigSender;


    Random random = new Random();

    public void sendMessage(String responceQueue, String message, String replyTo,String correlationId, Long start) {
        logger.info("replyTo: " + replyTo);
//        System.out.println("Responce message: " + message);



//        jmsConfigSender.jmsMapTemplates.get(replyTo).convertAndSend(responceQueue,(Object) message, new MessagePostProcessor() {
//            @SneakyThrows
//            @Override
//            public Message postProcessMessage(Message message) {
//                message.setJMSCorrelationID(correlationId); //добавление JMSCorrelationID в хэдеры ответа
//                return message;
//            }
//        });

        jmsConfigSender.jmsMapTemplates.get(replyTo).convertAndSend(responceQueue,(Object) message);


        writeToInflux(replyTo, System.currentTimeMillis() - start);
        logger.info("Message to " +replyTo+ " send for : " + (System.currentTimeMillis() - start));
//        jmsTemplate.setDeliveryDelay(random.nextLong(1000L));

//        jmsConfigSender.jmsMapTemplates.get(queueManager).convertAndSend(destination, message);

//        jmsTemplate.setDeliveryDelay(1000L);
//        jmsTemplate.convertAndSend("UCBRU.SDPMT.V5.C2BPMTDET.RESPONSE",message);

//        jmsTemplate.convertAndSend(jmsQueue, message, new MessagePostProcessor() {
//            @Override
//            public jakarta.jms.Message postProcessMessage(jakarta.jms.Message message) throws jakarta.jms.JMSException {
//                return null;
//            }
//
//            @Override
//            public Message postProcessMessage(Message message) throws JMSException {
//                message.setJMSCorrelationID(correlationId); //добавление JMSCorrelationID в хэдеры ответа
//                return message;
//            }
//        });
    }
}
