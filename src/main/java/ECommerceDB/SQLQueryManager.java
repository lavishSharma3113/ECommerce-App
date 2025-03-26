package ECommerceDB;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SQLQueryManager {
    private Map<String, String> queries;
    private Properties properties;
    private static final String CONFIG_FILE = "src/main/java/Configurations/sql-queries.properties";

    public SQLQueryManager() {
        queries = new HashMap<>();
        properties = new Properties();
        loadQueries();
    }


    private void loadQueries() {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);

            for (String key : properties.stringPropertyNames()) {
                queries.put(key, properties.getProperty(key));
            }

        } catch (IOException e) {
            System.err.println("Failed to load SQL queries from file: " + e.getMessage());
        }
    }

    public String getQuery(String queryKey) {
        return queries.get(queryKey);
    }

}
