package com.swyp.saratang.mapper;

import com.swyp.saratang.model.IconDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface IconMapper {

   
    List<IconDTO> getAllIcons();
    

    Integer getUserIconId(@Param("userId") Integer userId);


    IconDTO getIconById(@Param("iconId") Integer iconId);
}