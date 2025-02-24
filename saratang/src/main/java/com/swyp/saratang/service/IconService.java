package com.swyp.saratang.service;

import com.swyp.saratang.mapper.IconMapper;
import com.swyp.saratang.model.IconDTO;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class IconService {

    @Autowired
    private IconMapper iconMapper;

    // ✅ 모든 아이콘 조회
    public List<IconDTO> getAllIcons() {
        return iconMapper.getAllIcons();
    }
}