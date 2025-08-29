package ibm.mock.common;


import ibm.mock.QmProperties;
import ibm.mock.QueueConsumer;
import ibm.mock.models.DestinationWithProperties;
import ibm.mock.models.Mq;
import ibm.mock.sqlLite.SQLiteConnection;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class ResponsePreparer {

    @Autowired
    SQLiteConnection dbConnection;

    @Autowired
    private QmProperties qmProperties;

    Map<String, String> templateMap = new ConcurrentHashMap<>();
    private String responseText;
    private List<String> keywordsList;
    private static final Pattern keywordsPattern = Pattern.compile("\\$\\{((?:\\w|\\.|:|-)*?)\\}");  // ${table.field:-4}

    private static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    private static final String dateSimpleFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private static final String DATE_NOW = "dateNow";
    private static final String DATE_NOW_WITHOUT_TIMEZONE = "dateNowWithoutTimezone";
    private static final String GENERATE = "generate";
    private static final String RECEIVE = "receive";
    private static final String ID = "id";
    private static final String ALL = "ALL";

    private static final int MAX_RECURSION_DEPTH = 32;

//    public ResponsePreparer(String message) {
//        responseText = message;
//        findKeywords();
//    }

    public String getText() {
        return responseText;
    }

    /**
     * Find keywords in responseText.
     */
    private List findKeywords(String responseText) {
        long start = System.currentTimeMillis();
        log.debug("Searching keywords in response text.");
        Matcher matcher = keywordsPattern.matcher(responseText);
        Set<String> set = new HashSet<>();
        while (matcher.find()) {
            String key = matcher.group(1);
            set.add(key);
        }
        List keywordsList = set.stream().toList();
        log.debug("Found keywords = " + keywordsList);
        log.debug("Time findKeywords: " + (System.currentTimeMillis() - start));
        return keywordsList;
    }

    /**
     * return new responseText, where keywords have replaced with values from DB, receivedMap or inner values.
     */
//    public String replaceKeyWords(Map<String, String> receivedMap, String databasePath, String requestQueue) {
//        long start = System.currentTimeMillis();
//        logger.info("Replacing all keywords in response message.");
//        String outputText = templateMap.get(requestQueue);
//        List<String> keywordsList = findKeywords(outputText);
//        for (String keyword : keywordsList) {
//            outputText = outputText.replaceAll("\\$\\{" + keyword + "\\}",
//                    getValueRecursive(keyword, receivedMap, dbConnection.getSqlConnections().get(databasePath)));
//        }
//        logger.info("Time replaceKeyWords: " + (System.currentTimeMillis()-start));
//        return outputText;
//    }
    public String replaceKeyWords(Map<String, String> receivedMap, String databasePath, String requestQueue) {
        long start = System.currentTimeMillis();
        log.debug("Replacing all keywords in response message.");
        String outputText = templateMap.get(requestQueue);
        List<String> keywordsList = findKeywords(outputText);
        for (String keyword : keywordsList) {
            String value = getValueRecursiveInternal(keyword, receivedMap, dbConnection.getSqlConnections().get(databasePath), 0);
            if (value == null) value = "";
            outputText = outputText.replace("${" + keyword + "}", value);
        }
        log.debug("Time replaceKeyWords: " + (System.currentTimeMillis() - start));
        return outputText;
    }


    private String getValueRecursiveInternal(String key, Map<String, String> receivedMap, Connection connection, int depth) {
        if (depth > MAX_RECURSION_DEPTH) {
            log.error("Max recursion depth reached while resolving key {}", key);
            return "";
        }

        long start = System.currentTimeMillis();
        log.debug("Getting value for key " + key);

        String[] tableField = key.split("\\.", 2);
        if (tableField.length < 2) {
            log.debug("Keyword in response template must be like 'table.field'. Got " + key);
            return "";
        }

        String table = tableField[0];
        String field = tableField[1];

        // Support substring syntax like ${table.field:2:5} or ${table.field:-4}
        String[] substringValues = field.split(":", 3);
        field = substringValues[0];

        String value;
        switch (table) {
            case GENERATE:
                value = generateValue(field);
                break;
            case RECEIVE:
                value = getReceivedValue(field, receivedMap);
                break;
            default:
                String id = (receivedMap.get(ID) != null) ? receivedMap.get(ID) : ALL;
                value = dbConnection.getValue(connection, id, table, field);
                if (value == null) {
                    value = dbConnection.getValue(connection, ALL, table, field);
                }
        }

        if (value == null) {
            value = "";
        }

        // Resolve nested placeholders inside the obtained value
        Matcher matcher = keywordsPattern.matcher(value);
        while (matcher.find()) {
            String inKey = matcher.group(1);
            String resolved = getValueRecursiveInternal(inKey, receivedMap, connection, depth + 1);
            if (resolved == null) resolved = "";
            value = value.replace("${" + inKey + "}", resolved);
            matcher = keywordsPattern.matcher(value); // re-scan after replacement
        }

        // Apply substring if requested
        substringValues[0] = value;
        log.debug("Time getValueRecursive: " + (System.currentTimeMillis() - start));
        return substring(substringValues);
    }

//    private String getValueRecursive(String key, Map<String, String> receivedMap, Connection connection) {
//        long start = System.currentTimeMillis();
//        logger.info("Getting value for key " + key);
//        String[] tableField = key.split("\\.", 2);
//        if (tableField.length < 2) logger.info("Keyword in response template must be like 'table.field'. Got " + key);
//        String table = tableField[0];
//        String field = tableField[1];
//        String value;
//
//        String[] substringValues = field.split(":", 3);
//        field = substringValues[0]; // in string "key:2:5" field is "key", 2 and 5 - values for substring
//
//        switch (table) {
//            case GENERATE:
//                value = generateValue(field);
//                break;
//            case RECEIVE:
//                value = getReceivedValue(field, receivedMap);
//                break;
//            default:
//                String id = (receivedMap.get(ID) != null) ? receivedMap.get(ID) : ALL;
//                value = dbConnection.getValue(connection, id, table, field);
//                if (value == null) {
//                    value = dbConnection.getValue(connection, ALL, table, field);
//                }
//        }
//        Matcher matcher = keywordsPattern.matcher(value);
//        while (matcher.find()) {
//            String inKey = matcher.group(1);
//            value = value.replaceAll("\\$\\{" + inKey + "\\}", getValueRecursive(inKey, receivedMap, connection));
//        }
//
//        substringValues[0] = value;
//        logger.info("Time getValueRecursive: " + (System.currentTimeMillis()-start));
//        return substring(substringValues);
//    }

    private String substring(String[] values) {
        long start = System.currentTimeMillis();
        log.debug("Getting substring for values: " + Arrays.toString(values));
        if (values[0].equals("")) {
            log.debug("Getting string for making substring is empty. Got \"\".");
            return "";
        }
        int size = values.length;
        String result;
        switch (size) {
            case 1:
                result = values[0];
                break;
            case 2:
                int i = Integer.parseInt(values[1]);
                if (i >= 0) {
                    result = values[0].substring(0, i);
                } else {
                    result = values[0].substring(values[0].length() + i, values[0].length()); // i < 0
                }
                break;
            case 3:
                int j = Integer.parseInt(values[1]);
                int k = Integer.parseInt(values[2]);
                result = values[0].substring(j, k);
                break;
            default:
                result = null;
        }
        log.debug("Got substring " + result);
        log.debug("Time substring: " + (System.currentTimeMillis() - start));
        return result;
    }

    private String generateValue(String innerKey) {
        log.debug("Getting value for key " + innerKey);
        String result = null;
        switch (innerKey) {
            case DATE_NOW:
                result = new SimpleDateFormat(dateFormat).format(new Date());
                break;
            case DATE_NOW_WITHOUT_TIMEZONE:
                result = new SimpleDateFormat(dateSimpleFormat).format(new Date());
                break;
            default:
                log.debug("Key" + innerKey + " doesn't exist.");
                throw new RuntimeException("Key doesn't exist.");
        }
        log.debug("Returning value" + result + " for key " + innerKey);
        return result;
    }

    private String getReceivedValue(String receivedKey, Map<String, String> receivedMap) {
        log.debug("Getting value for key " + receivedKey);
        String result = receivedMap.get(receivedKey);
        if (result == null) {
            log.debug("No value in received map for key " + receivedKey);
            result = "";
        } else {
            log.debug("Returning value" + result + " for key " + receivedKey);
        }
        return result;
    }

    @PostConstruct
    private void loadTemplates() {

        for (Mq mq : qmProperties.getMqs()) {
            for (DestinationWithProperties dwp : mq.getDestinationWithProperties()) {
                String nameTemplate = dwp.getTemplateName();
                Path templatePath = Path.of("responseTemplates/" + nameTemplate);
                StringBuilder textTemplate = new StringBuilder();

                try (BufferedReader reader = Files.newBufferedReader(templatePath, Charset.forName(mq.getCharset()))) {
                    while (reader.ready()) {
                        textTemplate.append(reader.readLine()).append("\n");
                    }
                } catch (IOException e) {
                    log.error("No responce template for this queue " + dwp.getRequestQueue() + e.toString());
                    throw new RuntimeException("No responce template for this queue " + dwp.getRequestQueue());
                }
                templateMap.put(dwp.getRequestQueue(), textTemplate.toString());
            }
        }
        if (templateMap == null) {
            log.error("Templates didn't load.");
            throw new RuntimeException("Templates didn't load.");
        }
        log.debug("Loaded all templates in memory.");
//        logger.info("1st template: " + templateMap.entrySet().toArray()[0]);
//        logger.info("template C2BPMTDET.xml: " + templateMap.get("C2BPMTDET.xml"));

    }

}
