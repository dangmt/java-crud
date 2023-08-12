package com.example.demo.Controller;

import com.example.demo.Models.CartItem;
import com.example.demo.Models.Product;
import com.example.demo.Repository.CartItemRepository;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
public class PaypalController {

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Autowired
    private CartItemRepository cartItemRepository;

    @GetMapping("/paypal/complete")
    public String complete() {
        // Implement the logic for completing the PayPal payment
        // For example, you can redirect to a success page or process the payment
        // confirmation
        return "complete"; // Return the name of a view/template to be rendered
    }

    @GetMapping("/paypal/cancel")
    public String cancel() {
        // Implement the logic for handling a canceled PayPal payment
        // For example, you can redirect to a cancellation page or notify the user
        return "cancel"; // Return the name of a view/template to be rendered
    }

    @GetMapping("/paypal")
    public ResponseEntity<String> createPayment() {
        try {
            APIContext apiContext = new APIContext(clientId, clientSecret, "sandbox");

            List<CartItem> cartItems = cartItemRepository.findAll();
            double orderAmount = calculateOrderTotal(cartItems);

            Payer payer = new Payer();
            payer.setPaymentMethod("paypal");

            List<Item> itemList = new ArrayList<>();

            for (CartItem cartItem : cartItems) {
                Item item = new Item();
                item.setName(cartItem.getProduct().getName())
                        .setCurrency("USD")
                        .setQuantity(String.valueOf(cartItem.getQuantity()))
                        .setPrice(String.valueOf(cartItem.getProduct().getPrice()));
                itemList.add(item);
            }

            ItemList items = new ItemList();
            items.setItems(itemList);

            Amount amount = new Amount();
            amount.setTotal(String.valueOf(orderAmount))
                    .setCurrency("USD");

            Transaction transaction = new Transaction();
            transaction.setAmount(amount)
                    .setItemList(items)
                    .setDescription("Purchase from Your Store");

            RedirectUrls redirectUrls = new RedirectUrls();
            redirectUrls.setReturnUrl("http://localhost:8080/paypal/complete")
                    .setCancelUrl("http://localhost:8080/paypal/cancel");

            Payment payment = new Payment();
            payment.setIntent("sale")
                    .setPayer(payer);

            List<Transaction> transactionsList = new ArrayList<>();
            transactionsList.add(transaction); // Add the transaction to the list

            payment.setTransactions(transactionsList)
                    .setRedirectUrls(redirectUrls);

            String result = payment.create(apiContext).getLinks().get(1)
                    .getHref();
            return ResponseEntity.ok().body("{\"approvalUrl\": \"" + result + "\"}");

        } catch (PayPalRESTException ex) {
            return ResponseEntity.badRequest().body("PayPal API error");
        }
    }

    private double calculateOrderTotal(List<CartItem> cartItems) {
        double orderAmount = 0;

        for (CartItem cartItem : cartItems) {
            orderAmount += cartItem.getProduct().getPrice() * cartItem.getQuantity();
        }

        return orderAmount;
    }
}
