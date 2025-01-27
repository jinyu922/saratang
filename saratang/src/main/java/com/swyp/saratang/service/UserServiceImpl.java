package com.swyp.saratang.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swyp.saratang.mapper.UserMapper;
import com.swyp.saratang.model.UserDTO;

@Service
public class UserServiceImpl implements UserService{
	
	
	@Autowired
	private UserMapper userMapper;
	
	@Override
    public List<UserDTO> getAllUsers() {
        return userMapper.getAllUsers();
    }
}