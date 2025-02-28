package com.swyp.saratang.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/session")
public class SessionController {
	
	@Autowired
	private SessionListener sessionListener;
    
    @GetMapping("/active")
    public Set<String> getActiveSessions() {
        return SessionListener.getActiveSessions();
    }
}