package ibm.mock.common;

import com.zaxxer.hikari.HikariDataSource;
import ibm.mock.QmProperties;
import ibm.mock.QueueConsumer;
import ibm.mock.sqlLite.SQLiteConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RequestParsing {
    Logger logger = LoggerFactory.getLogger(QueueConsumer.class);
    @Autowired
    SQLiteConnection dbConnection;
    @Autowired
    QmProperties qmProperties;

    public  Map<String, String> getMap(String message, String responceQueue, String databasePath, String queueManager) {
        long start = System.currentTimeMillis();
        logger.info("Searching values in received message by regular expressions in db for "+ responceQueue);
        Map<String, String> regExpressions = dbConnection.getRegexMap(dbConnection.getSqlConnections().get(qmProperties.databasePathFromQueueManager(queueManager)),responceQueue);
//        Map<String, String> regExpressions = dbConnection.getRegexMap(dbConnection.getSqlConnections().get(databasePath),responceQueue);
        Map<String, String> result = new HashMap<>();
        for(Map.Entry<String, String> nameRegex: regExpressions.entrySet()) {
            Pattern keywordPattern = Pattern.compile(nameRegex.getValue());
            Matcher matcher = keywordPattern.matcher(message);
            if (matcher.find())
                result.put(nameRegex.getKey(), matcher.group(1));
        }
        logger.info("Found values: " + result);
        logger.info("Time getMap: " + (System.currentTimeMillis()-start));
        return result;
    }
}
