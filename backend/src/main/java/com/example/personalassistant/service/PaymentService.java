package com.example.personalassistant.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.codec.digest.HmacUtils;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    @Value("${razorpay.key-id}")
    private String apiKey;

    @Value("${razorpay.key-secret}")
    private String apiSecret;

    @Autowired(required = false)
    private RazorpayClient razorpayClient;

    public Map<String, Object> createOrder(double amount) throws Exception {
        RazorpayClient client = razorpayClient;
        if (client == null) {
            client = new RazorpayClient(apiKey, apiSecret);
        }

        JSONObject options = new JSONObject();
        // Razorpay expects amount in paise (1 INR = 100 paise)
        options.put("amount", (int) (amount * 100));
        options.put("currency", "INR");
        options.put("receipt", "txn_" + System.currentTimeMillis());

        Order order = client.orders.create(options);

        Map<String, Object> response = new HashMap<>();
        response.put("id", order.get("id"));
        response.put("amount", order.get("amount"));
        response.put("currency", order.get("currency"));
        response.put("key", apiKey);

        return response;
    }

    public boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String generatedSignature = HmacUtils.hmacSha256Hex(
                    apiSecret,
                    orderId + "|" + paymentId
            );
            return generatedSignature.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }
}
