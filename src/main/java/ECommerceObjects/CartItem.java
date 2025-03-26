package ECommerceObjects;

public class CartItem {
    private int cartId;
    private int productId;
    private String name;
    private double price;
    private int quantity;

    public CartItem(int cartId, int productId, String name, double price, int quantity) {
        this.cartId = cartId;
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public int getCartId() {
        return cartId;
    }

    public int getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }
}

