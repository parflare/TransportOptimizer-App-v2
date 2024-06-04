package ua.parflare.transportoptimizerapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthorizationController {

    @GetMapping("/login")
    public String login() {
        return "login"; // Назва HTML файлу без розширення
    }

    @GetMapping("/home")
    public String index() {
        return "index"; // Назва HTML файлу без розширення
    }

    @GetMapping("/hello")
    public String hello() {
        return "hello"; // Назва HTML файлу без розширення
    }


}
