package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import ECommerceDB.DatabaseManager;

import Session.UserSession;
import Server.NetworkAuthenticationService;
import Server.NetworkCartService;
import Server.NetworkOrderService;
import Server.NetworkProductService;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final DatabaseManager dbManager;
    private PrintWriter out;
    private BufferedReader in;
    private UserSession session;

    public ClientHandler(Socket socket, DatabaseManager dbManager) {
        this.clientSocket = socket;
        this.dbManager = dbManager;
        this.session = new UserSession();
    }

    @Override
    public void run() {
        try {
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            printWelcomeMessage();

            handleAuthentication();

            if (session.isAuthenticated()) {
                handleMainMenu();
            }

        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("Client disconnected: " + clientSocket.getInetAddress().getHostAddress());
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private void handleAuthentication() throws IOException {
        boolean authComplete = false;

        while (!authComplete) {
            printAuthenticationOptions();

            String choice = in.readLine();

            switch (choice) {
                case "1":
                    if (handleLogin()) {
                        authComplete = true;
                    }
                    break;
                case "2":
                    if (handleSignUp()) {
                        authComplete = true;
                    }
                    break;
                case "3":
                    out.println("Thank you for visiting. Goodbye!");
                    authComplete = true;
                    break;
                default:
                    out.println("Invalid option. Please try again.");
            }
        }
    }

    private boolean handleLogin() throws IOException {
        out.println("\n=== Login ===");
        out.println("Username: ");
        String username = in.readLine();

        out.println("Password: ");
        String password = in.readLine();

        NetworkAuthenticationService authService = new NetworkAuthenticationService(dbManager);
        boolean loggedIn = authService.login(username, password);

        if (loggedIn) {
            session = authService.getCurrentSession();
            out.println("Login successful! Welcome, " + session.getUsername() + "!");
            return true;
        } else {
            out.println("Login failed. Invalid username or password.");
            return false;
        }
    }

    private boolean handleSignUp() throws IOException {
        out.println("\n=== Sign Up ===");
        out.println("Enter username: ");
        String username = in.readLine();

        out.println("Enter password: ");
        String password = in.readLine();

        out.println("Enter email: ");
        String email = in.readLine();

        NetworkAuthenticationService authService = new NetworkAuthenticationService(dbManager);
        NetworkAuthenticationService.SignUpResult result = authService.signUp(username, password, email);

        switch (result) {
            case SUCCESS:
                session = authService.getCurrentSession();
                out.println("Sign up successful! You are now logged in as " + session.getUsername() + "!");
                return true;
            case USERNAME_TAKEN:
                out.println("Sign up failed. Username already exists. Please choose a different username.");
                return false;
            case ERROR:
            default:
                out.println("Sign up failed. There was an error processing your request.");
                return false;
        }
    }

    private void handleMainMenu() throws IOException {
        boolean exit = false;

        while (!exit && session.isAuthenticated()) {
            printMainMenu();

            String choice = in.readLine();

            switch (choice) {
                case "1":
                    handleViewProducts();
                    break;
                case "2":
                    handleViewCart();
                    break;
                case "3":
                    handleViewOrderHistory();
                    break;
                case "4":
                    exit = true;
                    out.println("Logging out. Goodbye, " + session.getUsername() + "!");
                    session.clearSession();
                    break;
                default:
                    out.println("Invalid option. Please try again.");
            }
        }
    }

    private void handleViewProducts() throws IOException {
        NetworkProductService productService = new NetworkProductService(dbManager, out, in);
        NetworkCartService cartService = new NetworkCartService(dbManager, session, out);
        productService.viewProducts(cartService);
    }

    private void handleViewCart() throws IOException {
        NetworkCartService cartService = new NetworkCartService(dbManager, session, out);
        NetworkOrderService orderService = new NetworkOrderService(dbManager, session, out, in);
        cartService.viewCart(in, orderService);
    }

    private void handleViewOrderHistory() throws IOException {
        NetworkOrderService orderService = new NetworkOrderService(dbManager, session, out, in);
        orderService.viewOrderHistory();
    }

    private void printWelcomeMessage() {
        out.println("===================================");
        out.println("Welcome to the Online Shop");
        out.println("===================================");
    }

    private void printAuthenticationOptions() {
        out.println("\n=== Authentication ===");
        out.println("1. Login");
        out.println("2. Sign Up");
        out.println("3. Exit");
        out.println("\nSelect an option: ");
    }

    private void printMainMenu() {
        out.println("\n=== Main Menu ===");
        out.println("1. View Products");
        out.println("2. View Cart");
        out.println("3. View Order History");
        out.println("4. Logout");
        out.println("\nSelect an option: ");
    }
}