package ibm.mock.sqlLite;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ibm.mock.QmProperties;
import ibm.mock.QueueConsumer;
import ibm.mock.common.Util;
import lombok.Data;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Component
public class SQLiteConnection {
    Logger logger = LoggerFactory.getLogger(QueueConsumer.class);
    @Autowired
    private QmProperties qmProperties;

    @Autowired
    private Util util;

    Map<String, HikariDataSource> hikariDataSource = new ConcurrentHashMap<>();
    Map<String, Connection> sqlConnections = new ConcurrentHashMap<>();
//    private final Connection connection;

//    //TODO: method close(hikari,sqlite)
//    @Bean
////    @Bean(destroyMethod = "close")
//    public List<DataSource> getListdataSources(){
//        List<DataSource> dataSources = new ArrayList<>();
//        for (Object sqliteDataSource : util.getSqliteDataSources().toArray()) {
////            System.out.println("sqliteDataSources: " + sqliteDataSource);
//            HikariConfig hikariConfig = new HikariConfig();
//            hikariConfig.setDataSourceClassName("org.sqlite.SQLiteDataSource");
////            hikariConfig.setDataSourceClassName("org.sqlite.JDBC");
//            hikariConfig.addDataSourceProperty("url","jdbc:sqlite:" + sqliteDataSource);
////            hikariConfig.addDataSourceProperty("url","jdbc:sqlite:C:\\Users\\er38226\\IdeaProjects\\IbmQueueEmulatorSpring\\SQLite.db");
//            hikariConfig.setMaximumPoolSize(100);
//            hikariConfig.setConnectionTimeout(1000);
////            hikariConfig.setConnectionTestQuery("SELECT 1");
//            hikariConfig.setPoolName(sqliteDataSource.toString());
//            HikariDataSource dataSource = new HikariDataSource(hikariConfig);
//            dataSources.add(dataSource);
//            hikariDataSource.put(String.valueOf(sqliteDataSource),dataSource);
//        }
//
//        return dataSources;
//    }

    @SneakyThrows
    @Bean
    public List<Connection> dataSource(){
        List<Connection> dataSources = new ArrayList<>();
        for (Object sqliteDataSource : util.getSqliteDataSources().toArray()) {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteDataSource);
            dataSources.add(connection);
            sqlConnections.put(String.valueOf(sqliteDataSource),connection);
        }

        return dataSources;
    }

        public String getValue(Connection connection,String id, String table, String key) {
        String result;
        try {
            Statement statement = connection.createStatement();
            String query = String.format("SELECT %s FROM %s WHERE id = '%s';", key, table, id);
//            logger.fine(String.format("Executing query [%s].", query));
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                result = resultSet.getString(key);
            } else {
//                logger.fine(String.format("No value for id = [%s].", id));
                return null;
            }
        } catch (SQLException exc) {
//            logger.severe(String.format("Error when getting value [%s] from table [%s] for id [%s]:\n%s", key, table, id, exc));
            throw new RuntimeException(exc);
        }
//        logger.fine(String.format("Returned value [%s] for id [%s]", result, id));
        return result;
    }

    public Map<String, String> getRegexMap(Connection connection,String responceQueue) {
        long start = System.currentTimeMillis();
        Map<String, String> result = new HashMap<>();
        String table = responceQueue.replaceAll("\\.", "_");
        try {
            Statement statement = connection.createStatement();
//            Statement statement = connection.createStatement();
            String query = String.format("SELECT name, regex FROM %s;", table);
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                result.put(resultSet.getString("name"), resultSet.getString("regex"));
            }
        } catch (SQLException exc) {
//            logger.severe(String.format("Error when getting values from table [%s]:\n%s", table, exc));
            throw new RuntimeException(exc);
        }
        logger.info("Time getRegexMap: " + (System.currentTimeMillis()-start));
        return result;
    }


//    public SQLiteConnection(String path) {
//        try {
//            Class.forName("org.sqlite.JDBC");
//        } catch (ClassNotFoundException exc) {
//            throw new RuntimeException(exc);
//        }
//        try {
////            logger.info(String.format("Connecting SQLite DB [%s]", path));
//            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
//        } catch (SQLException exc) {
////            logger.severe("Error when creating connection SQLite DB.");
//            throw new RuntimeException(exc);
//        }
//    }


//

}
