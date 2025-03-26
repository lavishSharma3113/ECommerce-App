package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ECommerceDB.DatabaseManager;
import ECommerceDB.SQLQueryManager;
import ECommerceObjects.Product;

public class NetworkProductService {
    private final DatabaseManager dbManager;
    private final PrintWriter out;
    private final BufferedReader in;
    private final SQLQueryManager queryManager;

    public NetworkProductService(DatabaseManager dbManager, PrintWriter out, BufferedReader in) {
        this.dbManager = dbManager;
        this.out = out;
        this.in = in;
        this.queryManager = new SQLQueryManager();
    }

    public void viewProducts(NetworkCartService cartService) throws IOException {
        out.println("\n=== Available Products ===");
        String query = queryManager.getQuery("Query_GET_ALL_PRODUCT");

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            List<Product> products = new ArrayList<>();
            int index = 1;

            out.println("-------------------------------------------");
            out.println(String.format("%-5s %-20s %-10s %-10s", "No.", "Product Name", "Price", "Stock"));
            out.println("-------------------------------------------");

            while (rs.next()) {
                Product product = new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity")
                );
                products.add(product);

                out.println(String.format("%-5d %-20s $%-9.2f %-10d",
                        index++,
                        product.getName(),
                        product.getPrice(),
                        product.getStock()
                ));
            }

            out.println("-------------------------------------------");

            if (products.isEmpty()) {
                out.println("No products available.");
                return;
            }

            out.println("\n1. Add to Cart");
            out.println("2. Return to Main Menu");
            out.println("\nSelect an option: ");

            String choice = in.readLine();

            if (choice.equals("1")) {
                addToCart(products, cartService);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            out.println("Error retrieving products from database."+ e.getMessage());
        }
    }

    private void addToCart(List<Product> products, NetworkCartService cartService) throws IOException {
        out.println("Enter product number to add to cart: ");
        try {
            int productIndex = Integer.parseInt(in.readLine()) - 1;

            if (productIndex >= 0 && productIndex < products.size()) {
                Product selectedProduct = products.get(productIndex);

                out.println("Enter quantity: ");
                int quantity = Integer.parseInt(in.readLine());

                if (quantity <= 0) {
                    out.println("Quantity must be greater than zero.");
                    return;
                }

                if (quantity > selectedProduct.getStock()) {
                    out.println("Not enough stock available. Available: " + selectedProduct.getStock());
                    return;
                }

                cartService.addToCart(selectedProduct.getId(), selectedProduct.getName(), quantity);

            } else {
                out.println("Invalid product number.");
            }
        } catch (NumberFormatException e) {
            out.println("Please enter a valid number.");
        }
    }
}
