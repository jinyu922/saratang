package com.swyp.saratang.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.swyp.saratang.mapper.UserMapper;

@Service
public class UserService {
    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public List<Long> getAllUserIds() {
        return userMapper.getAllUserIds();
    }
}
