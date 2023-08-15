package com.example.demo.Controller;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Models.CartItem;
import com.example.demo.Models.Order;
import com.example.demo.Models.OrderItem;
import com.example.demo.Repository.CartItemRepository;
import com.example.demo.Repository.OrderItemRepository;
import com.example.demo.Repository.OrderRepository;

import jakarta.transaction.Transactional;

@RestController
public class OrderController {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderController(OrderRepository orderRepository, CartItemRepository cartItemRepository,
            OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @GetMapping({ "orders/", "orders" })
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return ResponseEntity.ok(orders);
    }

    @PostMapping("orders")
    @Transactional
    public ResponseEntity<?> createOrder() {
        // Create a new order
        Order order = new Order();
        orderRepository.save(order);

        // Move cart items to order items
        List<CartItem> cartItems = cartItemRepository.findAll();

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItemRepository.save(orderItem);
            // order.getOrderItems().add(orderItem);
        }

        return ResponseEntity.ok("Order created successfully");
    }

    @GetMapping("orders/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }

    @PutMapping("orders/{orderId}")
    @Transactional
    public ResponseEntity<?> updateOrder(@PathVariable Long orderId) {
        Order existingOrder = orderRepository.findById(orderId).orElse(null);
        if (existingOrder == null) {
            return ResponseEntity.notFound().build();
        }

        // Update order properties if needed
        // Example: existingOrder.setStatus(updatedOrder.getStatus());
        // orderRepository.save(existingOrder);

        return ResponseEntity.ok("Order updated successfully");
    }

    @DeleteMapping("orders/{orderId}")
    @Transactional
    public ResponseEntity<?> deleteOrder(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        // Delete associated order items
        // OrderItem::where('order_id', $orderId)->delete();

        // Delete the order
        orderRepository.delete(order);

        return ResponseEntity.ok("Order deleted successfully");
    }

}
