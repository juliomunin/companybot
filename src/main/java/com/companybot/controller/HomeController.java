package com.companybot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "Início");
        return "admin/index";
    }

    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("pageTitle", "Início");
        return "admin/index";
    }
}
