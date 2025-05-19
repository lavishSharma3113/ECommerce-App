package ECommerceObjects;

import java.util.Date;

public class Order {
    private int id;
    private double totalAmount;
    private Date orderDate;

    public Order(int id, double totalAmount, Date orderDate) {
        this.id = id;
        this.totalAmount = totalAmount;
        this.orderDate = orderDate;
    }

    public int getId() {
        return id;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public Date getOrderDate() {
        return orderDate;
    }
}
