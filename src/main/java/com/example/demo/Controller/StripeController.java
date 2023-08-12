package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Models.CartItem;
import com.example.demo.Repository.CartItemRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import java.util.List;

@RestController
// @RequestMapping("/api/stripe")

public class StripeController {

    @Autowired
    private CartItemRepository cartItemRepository; // Inject your CartItemRepository here
    @Value("${stripe.api.secret-key}")
    private String stripeApiKey;

    @GetMapping("/stripe")
    public ResponseEntity<?> payment() {
        Stripe.apiKey = stripeApiKey; // Replace with your Stripe secret key
        System.out.println(Stripe.apiKey);
        List<CartItem> cartItems = cartItemRepository.findAll();
        long orderAmount = calculateOrderTotal(cartItems);

        PaymentIntent paymentIntent;
        try {
            paymentIntent = PaymentIntent.create(new PaymentIntentCreateParams.Builder()
                    .setAmount(orderAmount * 100L)
                    .setCurrency("usd")
                    .build());
        } catch (StripeException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body("Payment Intent creation failed");
        }

        // return ResponseEntity.ok().body(paymentIntent.getClientSecret());
        return ResponseEntity.ok("{\"client_secret\" :\"" + paymentIntent.getClientSecret() + "\"}");
    }

    private int calculateOrderTotal(List<CartItem> cartItems) {
        int orderAmount = 0;

        for (CartItem cartItem : cartItems) {
            // Assuming each cart item corresponds to a product with a 'price' attribute
            orderAmount += cartItem.getProduct().getPrice() * cartItem.getQuantity();
        }

        return orderAmount;
    }
}
