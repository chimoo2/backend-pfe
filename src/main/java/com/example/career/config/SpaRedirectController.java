package com.example.career.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Forwards SPA routes to index.html so React Router can handle client-side navigation
 * This allows refresh to work on any /profile, /signin, /signup route
 */
@Controller
public class SpaRedirectController {

    @GetMapping({"/", "/profile", "/signin", "/signup"})
    public String forwardIndex() {
        return "forward:/index.html";
    }
}
