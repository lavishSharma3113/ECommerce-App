package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

import DAO.ProductDAO;
import ECommerceDB.DatabaseManager;
import ECommerceObjects.Product;
import UI.ProductUI;

import static java.lang.System.out;

public class NetworkProductService {
    private final ProductDAO productRepo;
    private final ProductUI productUI;
    private final BufferedReader in;

    public NetworkProductService(DatabaseManager dbManager, PrintWriter out, BufferedReader in) {
        this.productRepo = new ProductDAO(dbManager);
        this.productUI = new ProductUI(out);

        this.in = in;

    }

    public void viewProducts(NetworkCartService cartService) throws IOException {
        try {
            List<Product> products = productRepo.getAllProducts();
            productUI.displayProducts(products);

            if (products.isEmpty()) return;

            out.println("\n1. Add to Cart");
            out.println("2. Return to Main Menu");
            out.print("Select an option: ");
            String choice = in.readLine();

            if ("1".equals(choice)) {
                addToCart(products,cartService);
            }
        } catch (SQLException e) {
            out.println("Error retrieving products: " + e.getMessage());
        }
    }

    private void addToCart(List<Product> products,  NetworkCartService cartService) throws IOException {
        out.print("Enter product number to add to cart: ");
        try {
            int productIndex = Integer.parseInt(in.readLine()) - 1;

            if (productIndex < 0 || productIndex >= products.size()) {
                out.println("Invalid product number.");
                return;
            }

            Product selectedProduct = products.get(productIndex);

            out.print("Enter quantity: ");
            int quantity = Integer.parseInt(in.readLine());

            if (quantity <= 0) {
                out.println("Quantity must be greater than zero.");
                return;
            }

            if (quantity > selectedProduct.getStock()) {
                out.println("Not enough stock. Available: " + selectedProduct.getStock());
                return;
            }

            cartService.addToCart(selectedProduct.getId(), selectedProduct.getName(), quantity);
        } catch (NumberFormatException e) {
            out.println("Please enter a valid number.");
        }
    }
}

