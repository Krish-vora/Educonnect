package com.project.educonnect;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;



@Controller
public class myController {
    @GetMapping("/krish")
    public String krishPrint() {
        return "Krish prajapati";
    }

    @GetMapping("/")
    public String HomePage(Model model) {
        model.addAttribute("name", "Krish");
        return "home";
    }

      @GetMapping("/register")
    public String registerPage() {
        return "register";
    }
}
