package ibm.mock;

import ibm.mock.common.Util;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import lombok.SneakyThrows;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.stereotype.Component;

//import javax.jms.JMSException;
import java.util.Random;

import static ibm.mock.Main.writeToInflux;

//import static ibm.mock.Main.writeToInflux;

@Component
@Slf4j
public class MessageSender {
    //    @Autowired
//    JmsTemplate jmsTemplate;

    @Autowired
    private QmProperties qmProperties;
    @Autowired
    private JmsConfigSender jmsConfigSender;


    Random random = new Random();

    public void sendMessage(String responceQueue, String message, String replyTo, String correlationId, Long start) {

//        System.out.println("Responce message: " + message);


//        jmsConfigSender.jmsMapTemplates.get(replyTo).convertAndSend(responceQueue,(Object) message, new MessagePostProcessor() {
//            @SneakyThrows
//            @Override
//            public Message postProcessMessage(Message message) {
//                message.setJMSCorrelationID(correlationId); //добавление JMSCorrelationID в хэдеры ответа
//                return message;
//            }`
//        });


        var template = jmsConfigSender.jmsMapTemplates.get(replyTo);
        if (template == null) {
            log.error("No JmsTemplate mapping found for replyTo {}. Skipping send.", replyTo);
            return;
        }
        Long delayMsConfig = qmProperties.getSleepFromDestination(responceQueue);
        Long methodDelay = System.currentTimeMillis() - start;
        long delayMs = methodDelay>delayMsConfig?0:(delayMsConfig-methodDelay);
        template.setDeliveryDelay(delayMs);
        template.convertAndSend(responceQueue, (Object) message, new MessagePostProcessor() {
            @SneakyThrows
            @Override
            public Message postProcessMessage(Message msg) {
                msg.setJMSCorrelationID(correlationId);
                return msg;
            }
        });
        long transactionTime = System.currentTimeMillis() - start;
        writeToInflux(replyTo, transactionTime);
//        logger.debug("Message to " + replyTo + " send for : " + (System.currentTimeMillis() - start));
        log.info("is sending message to destination {}: correlationID = {}, send time = {}", replyTo, correlationId, transactionTime);

    }
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
