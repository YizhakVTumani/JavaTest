package org.example;

import org.junit.jupiter.api.Assertions;
import org.testng.annotations.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class TestJava {
    @Test
    void testOrderSuccess() throws AppException {
        MyOrderProcessor processor = new MyOrderProcessor(new PayByCard());
        OrderItem[] items = { new OrderItem("Apple", new Money(100), 1) };
        Order order = new Order(1, items, false);

        processor.process(order);
        Assertions.assertEquals(OrderStatus.Shipped, order.getStatus());
    }


    @ParameterizedTest
    @CsvSource({
            "1000, true, 980.0",
            "20000, false, 18400.0",
            "20000, true, 18000.0"
    })
    void testDiscounts(double price, boolean isVip, double expected) throws AppException {
        MyOrderProcessor processor = new MyOrderProcessor(new PayByCard());
        OrderItem[] items = { new OrderItem("Item", new Money(price), 1) };
        Order order = new Order(100, items, isVip);

        processor.process(order);
        Assertions.assertEquals(expected, order.getTotal().getCents(), 0.001);
    }

    @Test
    void testMoneyEquals() {
        Money m1 = new Money(500);
        Money m2 = new Money(500);
        Assertions.assertEquals(m1, m2);
        Assertions.assertEquals(m1.hashCode(), m2.hashCode());
    }

    @Test
    void testOptionalFound() {
        MyOrderProcessor processor = new MyOrderProcessor(new PayByCard());
        Order o1 = new Order(10, null, false);
        Order[] orders = { o1 };
        assertTrue(processor.findOrderById(orders, 10).isPresent());
    }

    @Test
    void testDefensiveCopy() {
        OrderItem item = new OrderItem("Test", new Money(10), 1);
        OrderItem[] original = { item };
        Order order = new Order(1, original, false);

        original[0] = null;
        Assertions.assertNotNull(order.getItems()[0]);
    }

    @Test
    void testErrorTooManyItems() {
        MyOrderProcessor processor = new MyOrderProcessor(new PayByCard());
        OrderItem item = new OrderItem("A", new Money(1), 1);
        OrderItem[] items = {item, item, item, item, item, item};
        Order order = new Order(2, items, false);

        Assertions.assertThrows(AppException.class, () -> processor.process(order));
    }

    @Test
    void testErrorQuantityOver20() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new OrderItem("Bread", new Money(20), 21)
        );
    }

    @Test
    void testErrorCardLimit() {
        MyOrderProcessor processor = new MyOrderProcessor(new PayByCard());
        OrderItem[] items = { new OrderItem("PC", new Money(25000), 1) };
        Order order = new Order(3, items, false);

        Assertions.assertThrows(AppException.class, () -> processor.process(order));
    }

    @Test
    void testErrorPayPalMin() {
        MyOrderProcessor processor = new MyOrderProcessor(new PayByPayPal());
        OrderItem[] items = { new OrderItem("Match", new Money(50), 1) };
        Order order = new Order(4, items, false);

        Assertions.assertThrows(AppException.class, () -> processor.process(order));
    }

    @Test
    void testErrorIllegalState() {
        Order order = new Order(5, null, false);
        Assertions.assertThrows(IllegalStateException.class, () -> order.confirmDelivery());
    }

    @Test
    void testOptionalNotFound() {
        MyOrderProcessor processor = new MyOrderProcessor(new PayByCard());
        Order o1 = new Order(10, null, false);
        Order[] orders = { o1 };

        Optional<Order> result = processor.findOrderById(orders, 99);

        assertFalse(result.isPresent());
        assertTrue(result.isEmpty());
    }
}

