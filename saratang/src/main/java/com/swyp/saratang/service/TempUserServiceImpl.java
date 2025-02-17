package com.swyp.saratang.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swyp.saratang.mapper.TempUserMapper;
import com.swyp.saratang.model.UserDTO;

@Service
public class TempUserServiceImpl implements TempUserService {

    @Autowired
    private TempUserMapper tempUserMapper;

    @Override
    public void insertTempUser(UserDTO user) {
        tempUserMapper.insertTempUser(user);
    }

    @Override
    public UserDTO findTempUserBySocialId(String socialId, String provider) {
        return tempUserMapper.findTempUserBySocialId(socialId, provider);
    }

    @Override
    public void deleteTempUser(String socialId, String provider) {
        tempUserMapper.deleteTempUser(socialId, provider);
    }
}
