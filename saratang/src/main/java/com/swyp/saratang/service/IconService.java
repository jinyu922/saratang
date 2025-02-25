package com.swyp.saratang.service;

import com.swyp.saratang.mapper.IconMapper;
import com.swyp.saratang.model.IconDTO;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


public interface IconService {

	


    public List<IconDTO> getAllIcons();
   

    public Integer getUserIconId(Integer userId);


    public IconDTO getIconById(Integer iconId);
}