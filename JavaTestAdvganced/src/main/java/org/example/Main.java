package org.example;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {

    }
}
class Money{
    private double cents;

    public Money(double cents){
        this.cents = cents;
    }
    public double getCents(){
        return cents;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
        {
            return true;
        }
        if (obj == null){
            return false;
        }
        Money money = (Money)obj;
        return Double.compare(money.getCents(), cents) == 0;
    }
    @Override
    public int hashCode() {
        return Double.hashCode(cents);
    }
}

class Order {
    private int orderId;
    private OrderItem[] items;
    private Money total;
    private boolean isVip;
    private OrderStatus status;

    public Order(int orderId, OrderItem[] items, boolean isVip) {
        this.orderId = orderId;
        this.items = items;
        this.isVip = isVip;
        this.status = OrderStatus.New;

        if (items == null){
            this.items = new OrderItem[0];
        }
        else{
            this.items = Arrays.copyOf(items, items.length);
        }
    }

    public int getOrderId() {
        return orderId;
    }
    public OrderStatus getStatus() {
        return status;
    }
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    public boolean isVip() {
        return isVip;
    }
    public Money getTotal() {
        return total;
    }
    public void setTotal(Money total) {
        this.total = total;
    }
    public OrderItem[] getItems(){
        return Arrays.copyOf(items, items.length);
    }

    public void confirmDelivery() {
        if (OrderStatus.Shipped.equals(this.status)) {
            this.status = OrderStatus.Delivered;
        } else {
            throw new IllegalStateException("Order must be SHIPPED before it can be DELIVERED");
        }
    }
}

enum OrderStatus {
    New,
    Validated,
    Shipped,
    Delivered
}

class OrderItem {
    private String productName;
    private Money price;
    private int quantity;

    public OrderItem(String productName, Money price, int quantity) {
        if(quantity <= 0 || quantity > 20){
            throw new IllegalArgumentException("Quantity must be between 1 and 20");
        }

        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }

    public String getProductName() {
        return productName;
    }
    public Money getPrice() {
        return price;
    }
    public int getQuantity() {
        return quantity;
    }
    public double getTotalPrice() {
        return price.getCents() * quantity;
    }
}

class Email {
    private String email;

    public Email(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email mail = (Email) o;
        return Objects.equals(email, mail.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return email;
    }
}

interface PaymentMethod {
    public void payment(double amount);
}

class PayByCard implements PaymentMethod {
    @Override
    public void payment(double amount){
        if (amount > 20000) {
            throw new IllegalArgumentException("Card payment limit exceeded (max 20,000)");
        }
        System.out.println("Paid " + amount + " by Credit Card");
    }
}

class PayByPayPal implements PaymentMethod {
    @Override
    public void payment(double amount){
        if (amount < 100) {
            throw new IllegalArgumentException("PayPal payment minimum is 100");
        }
        System.out.println("Paid " + amount + " by PayPal");
    }
}
class BankTransferPayment implements PaymentMethod {
    @Override
    public void payment(double amount){
        double commission = amount * 0.01;
        double total = amount + commission;
        System.out.println("Paid " + total + " by Bank Transfer");
    }
}
abstract class OrderProcessorTemplate {
    Logger logger = Logger.getLogger(OrderProcessorTemplate.class.getName());
    PaymentMethod paymentMethod;
    protected OrderProcessorTemplate(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public final void process(Order order) throws AppException {
        try{
        logger.info("Starting process: " + order.getOrderId());
        validate(order);
        calculate(order);
        payment(order);
        confirmShipping(order);
        notifyUser(order);

        logger.info("Order " + order.getOrderId() + " processed successfully");
    } catch (ShipmentConfirmationException | IllegalArgumentException e) {
        logger.warning("Business error: " + e.getMessage());
        throw new AppException("Business process failed", e); // Exception chaining
    } catch (Exception e) {
        logger.severe("Critical infrastructure error: " + e.getMessage());
        throw new AppException("Unexpected failure", e);
    }
    }
    private void validate(Order order){
        logger.info("Validating");
        if (order.getItems().length > 5) {
            throw new IllegalArgumentException("Validation failed: Maximum 5 items allowed.");
        }
        order.setStatus(OrderStatus.Validated);
    }
    private void calculate(Order order){
        logger.info("Calculating");
        double total = 0;
        for (OrderItem item : order.getItems()) {
            total += item.getTotalPrice();
        }

        double discount = 0;
        if (total >= 15000) discount += 0.08;
        if (order.isVip()) discount += 0.02;

        double finalAmount = total * (1 - discount);
        order.setTotal(new Money(finalAmount));
        logger.info("Discount applied: " + (discount * 100) + "%. Total: " + finalAmount);
    }
    protected abstract void payment(Order order) throws Exception;
    protected abstract void confirmShipping(Order order) throws ShipmentConfirmationException;
    protected abstract void notifyUser(Order order);
}

class MyOrderProcessor extends OrderProcessorTemplate {
    public MyOrderProcessor(PaymentMethod paymentMethod) {
        super(paymentMethod);
    }

    @Override
    protected void payment(Order order) throws Exception {
        paymentMethod.payment(order.getTotal().getCents());
    }

    @Override
    protected void confirmShipping(Order order) throws ShipmentConfirmationException {
        if (order.getItems().length == 0) {
            throw new ShipmentConfirmationException("Cannot ship empty");
        }
        order.setStatus(OrderStatus.Shipped);
        logger.info("Order shipped");
    }

    @Override
    protected void notifyUser(Order order) {
        logger.info("Notification sent to user " + order.getOrderId());
    }

    public Optional<Order> findOrderById(Order[] orders, int id) {
        if (orders == null) {
            return Optional.empty();
        }

        for (int i = 0; i < orders.length; i++) {
            if (orders[i].getOrderId() == id) {
                return Optional.of(orders[i]);
            }
        }
        return Optional.empty();
    }
}



 class ShipmentConfirmationException extends AppException { // Checked exception
    public ShipmentConfirmationException(String message) {
        super(message);
    }
    public ShipmentConfirmationException(String message, Throwable cause) {
        super(message, cause);
    }
}

class AppException extends Exception { // Checked exception

    public AppException(String message) {
        super(message);
    }
    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}

