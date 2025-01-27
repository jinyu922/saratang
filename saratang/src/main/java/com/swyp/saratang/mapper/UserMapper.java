package com.swyp.saratang.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.swyp.saratang.model.UserDTO;

@Mapper
public interface UserMapper {
    public List<UserDTO> getAllUsers(); 
}
