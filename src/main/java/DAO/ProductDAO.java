package DAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ECommerceDB.DatabaseManager;
import ECommerceDB.SQLQueryManager;
import ECommerceObjects.Product;

public class ProductDAO {
    private final DatabaseManager dbManager;
    private final SQLQueryManager queryManager;

    public ProductDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.queryManager = new SQLQueryManager();
    }

    public List<Product> getAllProducts() throws SQLException {
        String query = queryManager.getQuery("Query_GET_ALL_PRODUCT");
        List<Product> products = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                products.add(new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity")
                ));
            }
        }

        return products;
    }
}

