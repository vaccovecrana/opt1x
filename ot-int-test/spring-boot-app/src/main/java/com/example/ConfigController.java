package com.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigController {
    @Value("${app.message:Default message}")
    private String message;

    @Value("${app.debug:false}")
    private boolean debug;

    @GetMapping("/message")
    public String getMessage() {
        return message + (debug ? " (Debug mode enabled)" : "");
    }
}