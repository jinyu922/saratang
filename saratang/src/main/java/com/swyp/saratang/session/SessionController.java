package com.swyp.saratang.session;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/session")
public class SessionController {
	
	@Autowired
	private SessionListener sessionListener;
    
    @GetMapping("/active")
    public Map<String, LocalDateTime> getActiveSessions() {
        return SessionListener.getActiveSessions();
    }
}