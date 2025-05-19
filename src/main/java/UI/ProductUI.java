package UI;

import java.io.PrintWriter;
import java.util.List;
import ECommerceObjects.Product;

public class ProductUI {
    private final PrintWriter out;

    public ProductUI(PrintWriter out) {
        this.out = out;
    }

    public void displayProducts(List<Product> products) {
        out.println("\n=== Available Products ===");
        out.println("-------------------------------------------");
        out.println(String.format("%-5s %-20s %-10s %-10s", "No.", "Product Name", "Price", "Stock"));
        out.println("-------------------------------------------");

        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            out.println(String.format("%-5d %-20s $%-9.2f %-10d",
                    i + 1, p.getName(), p.getPrice(), p.getStock()));
        }

        out.println("-------------------------------------------");
        if (products.isEmpty()) {
            out.println("No products available.");
        }
    }
}

