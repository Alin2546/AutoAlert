package com.autoalert.autoalert.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping()
    public String home() {
        return "home";
    }

    @GetMapping("/privacyPolicy")
    public String privacyPolicy() {
        return "privacyPolicy";
    }

    @GetMapping("/terms")
    public String termsAndConditions() {
        return "termsAndConditions";
    }
}
