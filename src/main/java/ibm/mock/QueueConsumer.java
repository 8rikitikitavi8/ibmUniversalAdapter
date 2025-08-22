package ibm.mock;


import ibm.mock.common.RequestParsing;
import ibm.mock.common.ResponsePreparer;
import ibm.mock.common.Util;
import ibm.mock.sqlLite.SQLiteConnection;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;


import java.util.Map;

@Component
public class QueueConsumer implements MessageListener {
    Logger logger = LoggerFactory.getLogger(QueueConsumer.class);
    @Autowired
    private QmProperties qmProperties;

    @Autowired
    MessageSender messageSender;

    @Autowired
    JmsTemplate jmsTemplate;

    @Autowired
    private Util util;

    @Autowired
    private RequestParsing requestParsing;

    @Autowired
    private SQLiteConnection sqLiteConnection;

    @Autowired
    private ResponsePreparer responsePreparer;

//    public void sendMessage(String message){
//        System.out.println("Message send");
//        jmsTemplate.convertAndSend("UCBRU.SDPMT.V5.C2BPMTDET.RESPONSE",message);}
//
//    public void receive(String queueManeger, String text) {
//        System.out.println("Received from " + queueManeger + ": " + text);
//    }

    @SneakyThrows
    @Override
    public void onMessage(Message message) {
        Long start = System.currentTimeMillis();
        logger.info("Message recieved");
//        logger.info("message" + message.getBody(String.class));
//        logger.info("message: " + message);
        logger.info("message ReplyTo: " + message.getJMSReplyTo());
        logger.info("message getJMSMessageID: " + message.getJMSMessageID());

        String replyToRaw = String.valueOf(message.getJMSReplyTo());

        String replyTo = util.getCleanReplyTo(replyToRaw);
        String responceQueue = replyTo.split("/")[1];
        String[] requestQueueArr = responceQueue.split("RESPONSE");
        String requestQueueBeforeAliasOperation = requestQueueArr[0] + "REQUEST";
        logger.info("requestQueueBeforeAliasOperation: " + requestQueueBeforeAliasOperation);
        String requestQueue = null;
        if (!qmProperties.getRequestQueuesFromConfigFile().contains(requestQueueBeforeAliasOperation)) {
            requestQueue = util.getAliasQueue(requestQueueBeforeAliasOperation);
        } else {
            requestQueue = requestQueueBeforeAliasOperation;
        }
        String queueManager = replyTo.split("/")[0];
        String databasePath = qmProperties.databasePathFromQueueManager(queueManager);
        String correlationId = message.getJMSMessageID();
        String receivedMessageBody = message.getBody(String.class);

        Map<String, String> receivedMap = requestParsing.getMap(
                receivedMessageBody, requestQueue, databasePath, queueManager);
        String responseBody = responsePreparer.replaceKeyWords(receivedMap, databasePath, requestQueue);

//        setHeaders(sendMessage, correlationId);

//        System.out.println(message.getJMSDestination());
//        String destination = message.getJMSDestination().toString().substring(9,34) + "RESPONSE";
//        logger.info("responseBody: " + responseBody);
        messageSender.sendMessage(responceQueue, responseBody, replyTo, correlationId, start);
    }

}
