package com.swyp.saratang.service;

import com.swyp.saratang.model.UserDTO;

public interface TempUserService {
    void insertTempUser(UserDTO user);
    UserDTO findTempUserBySocialId(String socialId, String provider);
    void deleteTempUser(String socialId, String provider);
}

