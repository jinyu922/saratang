package com.swyp.saratang.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.swyp.saratang.service.UserService;

@Controller
public class UserController {
	 private final UserService userService;

	 public UserController(UserService userService) {
	        this.userService = userService;
	}
	 
	 @GetMapping("/")
	    public String getAllUserIds(Model model) {
	        List<Long> userIds = userService.getAllUserIds();
	        model.addAttribute("userIds", userIds); 
	        return "users";
	    }
}
