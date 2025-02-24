package com.swyp.saratang.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swyp.saratang.mapper.IconMapper;
import com.swyp.saratang.model.IconDTO;
@Service
public class IconServiceImpl implements IconService{
	
	@Autowired
    IconMapper iconMapper;

    // ✅ 모든 아이콘 조회
    @Override
    public List<IconDTO> getAllIcons() {
        return iconMapper.getAllIcons();
    }
    
    
 // ✅ 특정 사용자 아이콘 ID 조회 (users 테이블에서 icon_id 가져오기)
    @Override
    public Integer getUserIconId(Integer userId) {
        return iconMapper.getUserIconId(userId);
    }

    // ✅ 특정 아이콘 ID에 해당하는 아이콘 정보 조회
    @Override
    public IconDTO getIconById(Integer iconId) {
        return iconMapper.getIconById(iconId);
    }
}
