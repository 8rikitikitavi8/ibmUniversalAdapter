package ibm.mock.common;

import ibm.mock.QmProperties;
import ibm.mock.QueueConsumer;
import ibm.mock.models.Mq;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;

import java.util.Set;



@Data
@Component
@Slf4j
public class Util {
    @Autowired
    QmProperties qmProperties;

    Set<String> sqliteDataSources = new HashSet<>();

    public String getCleanReplyTo(String replyTo) {
//        String regex = "queue://([^/?]+/[^?]+)";
//        Pattern pattern = Pattern.compile(regex);
//        Matcher matcher = pattern.matcher(replyTo);
////        String result = null;
////        // Проверяем, найден ли матч
////        if (matcher.find()) {
////            result = matcher.group(1);
////            System.out.println(result);
////        } else {
////            System.out.println("Не удалось найти строку.");
////        }
//        return matcher.group(1);
        String[]strings = replyTo.split("/");
        log.debug(String.valueOf(strings.length));
        log.debug("strings[0]: " + strings[0]);
        log.debug("strings[1]: " + strings[1]);
        log.debug("strings[2]: " + strings[2]);
        log.debug("strings[3]: " + strings[3]);
        String[] words = strings[3].split("\\?");
        String respqueue = words[0];
        String result = strings[2]+"/"+respqueue;
        log.debug("result: "+ result);
        return result;
    }

    public String getResponceQueue(String replyTo){
        return replyTo.split("/")[1];
    }

    @PostConstruct
    private void fillSqliteDataSources(){
        for (Mq mq : qmProperties.getMqs()) {
            sqliteDataSources.add(mq.getDatabasePath());
        }
//        System.out.println("sqliteDataSources: " + sqliteDataSources);
    }

    public String getAliasQueue(String requestQueue){
        StringBuilder result = new StringBuilder();
        String[]strings = requestQueue.split("\\.");
        log.debug("max index strings: "+ strings.length);
        result.append(strings[0]).append(".SDPMT.V5.").append(strings[4]).append(".").append(strings[5]);
        log.debug("requestQueue from Alias: "+ result);
        return result.toString();
    }

}
