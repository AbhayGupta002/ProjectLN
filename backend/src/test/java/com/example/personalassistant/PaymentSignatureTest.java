package com.example.personalassistant;

import com.example.personalassistant.service.PaymentService;
import org.apache.commons.codec.digest.HmacUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentSignatureTest {

    private PaymentService paymentService;
    private final String testSecret = "test_razorpay_secret_key_123";

    @BeforeEach
    public void setup() {
        paymentService = new PaymentService();
        ReflectionTestUtils.setField(paymentService, "apiSecret", testSecret);
        ReflectionTestUtils.setField(paymentService, "apiKey", "rzp_test_key");
    }

    @Test
    public void testValidPaymentSignature() {
        String orderId = "order_9A33XWu170gUtm";
        String paymentId = "pay_29Mo3dru4Ggahg";
        String expectedSignature = HmacUtils.hmacSha256Hex(testSecret, orderId + "|" + paymentId);

        boolean isValid = paymentService.verifySignature(orderId, paymentId, expectedSignature);
        assertTrue(isValid, "Signature should be valid for authentic Razorpay payload");
    }

    @Test
    public void testTamperedPaymentSignature() {
        String orderId = "order_9A33XWu170gUtm";
        String paymentId = "pay_29Mo3dru4Ggahg";
        String tamperedSignature = "invalidsignature1234567890abcdef";

        boolean isValid = paymentService.verifySignature(orderId, paymentId, tamperedSignature);
        assertFalse(isValid, "Tampered signature must be rejected");
    }
}
