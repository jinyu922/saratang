package com.swyp.saratang.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.swyp.saratang.model.UserDTO;
import com.swyp.saratang.service.UserService;

@Controller
public class UserController {

	 @Autowired
      private UserService userService; 
	  
	 @GetMapping("/") 
	    public String getAllUserIds(Model model) {
	        List<UserDTO> users = userService.getAllUsers();
	        model.addAttribute("users", users); 
	        return "users";
	    }
	 
	
}
