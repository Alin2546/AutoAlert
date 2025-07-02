package com.autoalert.autoalert.Controller;

import com.autoalert.autoalert.Service.UserService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class StripeController {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    private static final String SUCCESS_URL = "http://localhost:8080/upgrade/success?session_id={CHECKOUT_SESSION_ID}";
    private static final String CANCEL_URL = "http://localhost:8080/upgrade/cancel";
    private final UserService userService;

    @PostMapping("/create-checkout-session")
    @ResponseBody
    public Map<String, Object> createCheckoutSession(@AuthenticationPrincipal UserDetails userDetails) throws StripeException {
        Stripe.apiKey = stripeSecretKey;

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl(SUCCESS_URL)
                        .setCancelUrl(CANCEL_URL)
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("eur")
                                                        .setUnitAmount(499L) // 4.99 EUR în cenți
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName("Upgrade cont Premium AutoAlert")
                                                                        .build())
                                                        .build())
                                        .build())
                        .build();

        Session session = Session.create(params);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("id", session.getId());
        return responseData;
    }

    @GetMapping("/upgrade/success")
    public String upgradeSuccess(@RequestParam("session_id") String sessionId, @AuthenticationPrincipal UserDetails userDetails) throws StripeException {
        Stripe.apiKey = stripeSecretKey;


        Session session = Session.retrieve(sessionId);


        if ("complete".equals(session.getStatus()) || "paid".equals(session.getPaymentStatus())) {
            userService.upgradeUserToPremium(userDetails.getUsername());
        } else {
            return "upgradeFailed";
        }

        return "upgradeSuccess";
    }

}
