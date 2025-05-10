package Server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ECommerceDB.DatabaseManager;
import Client.ClientHandler;

public class ShopServer {
    private static final int PORT = 5000;
    private final DatabaseManager dbManager;
    private final ExecutorService threadPool;

    public ShopServer(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.threadPool = Executors.newCachedThreadPool();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            log("Shop Server started on port " + PORT);
            log("Waiting for clients...");

            while (true) {
                handleClientConnection(serverSocket);
            }
        } catch (IOException e) {
            logError("Server error", e);
        } finally {
            shutdownThreadPool();
        }
    }

    private void handleClientConnection(ServerSocket serverSocket) throws IOException {
        Socket clientSocket = serverSocket.accept();
        log("New client connected: " + clientSocket.getInetAddress().getHostAddress());
        threadPool.submit(new ClientHandler(clientSocket, dbManager));
    }

    private void shutdownThreadPool() {
        log("Shutting down thread pool...");
        threadPool.shutdown();
    }

    private static void log(String message) {
        System.out.println(message);
    }

    private static void logError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        e.printStackTrace();
    }

    public static void main(String[] args) {
        Properties config = loadDatabaseConfig();
        if (config == null) return;

        DatabaseManager dbManager = initializeDatabaseManager(config);
        if (dbManager == null) return;

        new ShopServer(dbManager).start();
    }

    private static Properties loadDatabaseConfig() {
        Properties config = new Properties();
        try (InputStream input = new FileInputStream("src/main/java/Configurations/db-config.properties")) {
            if (input == null) {
                log("Database configuration file not found!");
                return null;
            }
            config.load(input);
        } catch (IOException e) {
            logError("Error loading database configuration", e);
            return null;
        }
        return config;
    }

    private static DatabaseManager initializeDatabaseManager(Properties config) {
        DatabaseManager dbManager = new DatabaseManager(
                config.getProperty("db.url"),
                config.getProperty("db.user"),
                config.getProperty("db.password")
        );

        if (!dbManager.testConnection()) {
            log("Failed to connect to the database. Check your settings.");
            return null;
        }
        return dbManager;
    }
}
