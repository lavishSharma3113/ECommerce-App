package ECommerceDB;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
public class SQLQueryManagerTest {

    private static final String TEST_CONFIG_FILE = "src/main/java/Configurations/sql-queries.properties";

    @BeforeEach
    void setup() throws IOException {
        try (FileWriter writer = new FileWriter(TEST_CONFIG_FILE)) {
            writer.write("QUERY_USER_LOGIN=SELECT * FROM users WHERE username=? AND password=?;\n");
            writer.write("QUERY_USER_SIGNUP=INSERT INTO users (username, password, email) VALUES (?, ?, ?);\n");
            writer.write("QUERY_CHECK_USERNAME=SELECT * FROM users WHERE username=?;\n");
        }
    }

    @Test
    void testLoadQueries() {
        SQLQueryManager queryManager = new SQLQueryManager();

        assertNotNull(queryManager.getQuery("QUERY_USER_LOGIN"), "QUERY_USER_LOGIN should be loaded.");
        assertNotNull(queryManager.getQuery("QUERY_USER_SIGNUP"), "QUERY_USER_SIGNUP should be loaded.");
        assertNotNull(queryManager.getQuery("QUERY_CHECK_USERNAME"), "QUERY_CHECK_USERNAME should be loaded.");
    }

    @Test
    void testGetQuery_ValidKey() {
        SQLQueryManager queryManager = new SQLQueryManager();

        String loginQuery = queryManager.getQuery("QUERY_USER_LOGIN");
        assertEquals("SELECT * FROM users WHERE username=? AND password=?;", loginQuery, "The login query should match the expected value.");

        String signupQuery = queryManager.getQuery("QUERY_USER_SIGNUP");
        assertEquals("INSERT INTO users (username, password, email) VALUES (?, ?, ?);", signupQuery, "The signup query should match the expected value.");
    }

    @Test
    void testGetQuery_InvalidKey() {
        SQLQueryManager queryManager = new SQLQueryManager();

        String invalidQuery = queryManager.getQuery("INVALID_KEY");
        assertNull(invalidQuery, "Query for an invalid key should return null.");
    }

    @Test
    void testLoadQueries_FileNotFound() {
        String originalFile = TEST_CONFIG_FILE;
        String tempFile = TEST_CONFIG_FILE + ".bak";
        new java.io.File(originalFile).renameTo(new java.io.File(tempFile));

        try {
            SQLQueryManager queryManager = new SQLQueryManager();
            assertNull(queryManager.getQuery("QUERY_USER_LOGIN"), "No queries should be loaded if the file is missing.");
        } finally {
            new java.io.File(tempFile).renameTo(new java.io.File(originalFile));
        }
    }
}
