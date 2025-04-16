package UI;

import Service.OrderService;
import ECommerceObjects.CartItem;
import ECommerceObjects.Order;
import Session.UserSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class OrderUI {
    private final OrderService orderService;
    private final PrintWriter out;
    private final BufferedReader in;

    public OrderUI(OrderService orderService, PrintWriter out, BufferedReader in) {
        this.orderService = orderService;
        this.out = out;
        this.in = in;
    }

    public void displayOrderHistory() throws IOException {
        out.println("\n=== Your Order History ===");
        List<Order> orders = orderService.getOrderHistory();

        if (orders.isEmpty()) {
            out.println("You have no orders yet.");
            return;
        }

        printOrderHistory(orders);
        displayOrderHistoryMenu(orders);
    }

    private void printOrderHistory(List<Order> orders) {
        out.println("-------------------------------------------");
        out.println(String.format("%-5s %-15s %-15s %-10s", "No.", "Order ID", "Date", "Amount"));
        out.println("-------------------------------------------");

        int index = 1;
        for (Order order : orders) {
            out.println(String.format("%-5d %-15d %-15s $%-10.2f",
                    index++, order.getId(), order.getOrderDate(), order.getTotalAmount()));
        }

        out.println("-------------------------------------------");
    }

    private void displayOrderHistoryMenu(List<Order> orders) throws IOException {
        out.println("\n1. View Order Details");
        out.println("2. Return to Main Menu");
        out.print("\nSelect an option: ");

        String choice = in.readLine();

        if ("1".equals(choice)) {
            out.print("Enter order number to view details: ");
            try {
                int orderIndex = Integer.parseInt(in.readLine()) - 1;
                if (orderIndex >= 0 && orderIndex < orders.size()) {
                    printOrderDetails(orders.get(orderIndex).getId());
                } else {
                    out.println("Invalid order number.");
                }
            } catch (NumberFormatException e) {
                out.println("Please enter a valid number.");
            }
        }
    }

    private void printOrderDetails(int orderId) {
        orderService.printOrderDetails(orderId, out);
    }

    public void checkout(List<CartItem> cartItems, double totalPrice) throws IOException {
        out.println("\n=== Checkout ===");
        out.println("Total amount: $" + String.format("%.2f", totalPrice));
        out.print("Confirm order (y/n): ");

        String confirm = in.readLine();

        if ("y".equalsIgnoreCase(confirm)) {
            orderService.processOrder(cartItems, totalPrice);
        } else {
            out.println("Order canceled.");
        }
    }
}
