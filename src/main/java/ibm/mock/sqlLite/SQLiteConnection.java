package ibm.mock.sqlLite;

import ibm.mock.QmProperties;
import ibm.mock.QueueConsumer;
import ibm.mock.common.Util;
import lombok.Data;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SQLiteConnection {

    @Autowired
    private QmProperties qmProperties;

    @Autowired
    private Util util;


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


    private boolean isValidIdentifier(String name) {
        return name != null && name.matches("[A-Za-z0-9_]+");
    }

    public String getValue(Connection connection, String id, String table, String key) {
        if (!isValidIdentifier(table) || !isValidIdentifier(key)) {
            log.error("Invalid table or column name: {}.{}", table, key);
            return null;
        }
        String sql = "SELECT " + key + " FROM " + table + " WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(key) : null;
            }
        } catch (SQLException exc) {
            throw new RuntimeException(exc);
        }
    }

    public Map<String, String> getRegexMap(Connection connection, String responceQueue) {
        long start = System.currentTimeMillis();
        Map<String, String> result = new HashMap<>();
        String table = responceQueue.replaceAll("\\.", "_");
        if (!isValidIdentifier(table)) {
            log.error("Invalid table name derived from response queue: {}", table);
            throw new RuntimeException("Invalid table name: " + table);
        }
        String sql = "SELECT name, regex FROM " + table;
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.put(rs.getString("name"), rs.getString("regex"));
            }
        } catch (SQLException exc) {
            throw new RuntimeException(exc);
        }
        log.debug("Time getRegexMap: " + (System.currentTimeMillis() - start));
        return result;
    }








//        public String getValue(Connection connection,String id, String table, String key) {
//        String result;
//        try {
//            Statement statement = connection.createStatement();
//            String query = String.format("SELECT %s FROM %s WHERE id = '%s';", key, table, id);
////            logger.fine(String.format("Executing query [%s].", query));
//            ResultSet resultSet = statement.executeQuery(query);
//            if (resultSet.next()) {
//                result = resultSet.getString(key);
//            } else {
////                logger.fine(String.format("No value for id = [%s].", id));
//                return null;
//            }
//        } catch (SQLException exc) {
////            logger.severe(String.format("Error when getting value [%s] from table [%s] for id [%s]:\n%s", key, table, id, exc));
//            throw new RuntimeException(exc);
//        }
////        logger.fine(String.format("Returned value [%s] for id [%s]", result, id));
//        return result;
//    }
//
//    public Map<String, String> getRegexMap(Connection connection,String responceQueue) {
//        long start = System.currentTimeMillis();
//        Map<String, String> result = new HashMap<>();
//        String table = responceQueue.replaceAll("\\.", "_");
//        try {
//            Statement statement = connection.createStatement();
////            Statement statement = connection.createStatement();
//            String query = String.format("SELECT name, regex FROM %s;", table);
//            ResultSet resultSet = statement.executeQuery(query);
//            while (resultSet.next()) {
//                result.put(resultSet.getString("name"), resultSet.getString("regex"));
//            }
//        } catch (SQLException exc) {
////            logger.severe(String.format("Error when getting values from table [%s]:\n%s", table, exc));
//            throw new RuntimeException(exc);
//        }
//        logger.info("Time getRegexMap: " + (System.currentTimeMillis()-start));
//        return result;
//    }


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
