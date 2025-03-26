package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ShopClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5000;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private BufferedReader userInput;

    public void start() {
        try {
            // Connect to server
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            userInput = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Connected to Shop Server!");

            // Main communication loop
            String serverMsg;
            while ((serverMsg = in.readLine()) != null) {
                System.out.println(serverMsg);

                if (serverMsg.endsWith(": ") || serverMsg.equals("Password: ")) {
                    String userResponse = userInput.readLine();
                    out.println(userResponse);
                }
            }

        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + SERVER_ADDRESS);
        } catch (IOException e) {
            System.err.println("I/O Error: " + e.getMessage());
        } finally {
            try {
                if (socket != null) socket.close();
                if (in != null) in.close();
                if (out != null) out.close();
                if (userInput != null) userInput.close();
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        ShopClient client = new ShopClient();
        client.start();
    }
}
