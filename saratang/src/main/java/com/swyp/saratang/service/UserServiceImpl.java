package com.swyp.saratang.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.swyp.saratang.mapper.UserMapper;
import com.swyp.saratang.model.UserDTO;
import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.PointDTO;
import com.swyp.saratang.model.SafeUserDTO;


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
    public UserDTO getUserById(Integer id) {
    	return userMapper.findById(id);
    }
    
    @Override 
    public SafeUserDTO getSafeUserById(Integer id) {
    	return userMapper.findSafeById(id);
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
    
    @Override
    public void changeUserColor(int Id, String newNicknameColor, int updatedCredits) {
        userMapper.changeUserColor(Id, newNicknameColor, updatedCredits);
    }
    
    @Override
    public List<PointDTO> getCreditHistoryByUserId(Integer userId) {
        return userMapper.getCreditHistoryByUserId(userId);
    }

    @Override
    public Integer getTotalCreditsByUserId(Integer userId) {
        return userMapper.getTotalCreditsByUserId(userId);
    }
    
    @Override
    public void changeUserColor(Integer userId, String newNicknameColor) {
        userMapper.changeUserColor(userId, newNicknameColor);
    }

    @Override
    public void insertCreditHistory(Integer userId, String type, Integer credits, String description) {
        userMapper.insertCreditHistory(userId, type, credits, description);
    }
    
    
    @Override
    public void changeUserIcon(Integer userId, Integer newIconId) {
        userMapper.updateUserIcon(userId, newIconId);
    }
}
