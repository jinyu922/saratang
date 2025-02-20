package com.swyp.saratang.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.swyp.saratang.mapper.UserMapper;
import com.swyp.saratang.model.UserDTO;
import com.swyp.saratang.model.ApiResponseDTO;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

 
    @Override
    public void insertUser(UserDTO user) {
        userMapper.insertUser(user);
    }

    @Override
    public UserDTO getUserBySocialId(String socialId, String provider) {
        return userMapper.findBySocialId(socialId, provider);
    }
    
    @Override
    public void newProfile(UserDTO user) {
        userMapper.newUserProfile(user);
    }
    
    @Override
    public void editProfile(UserDTO user) {
        userMapper.editUserProfile(user);
    }
    
    @Override
    public void deleteUser(String socialId, String authProvider, String email) {
        userMapper.deleteUser(socialId, authProvider, email);
    }
}
