package com.dobbinsoft.gus.remotesales.service;

import com.dobbinsoft.gus.remotesales.data.dto.auth.WecomAuthDTO;
import com.dobbinsoft.gus.remotesales.data.dto.session.WecomSessionInfoDTO;
import com.dobbinsoft.gus.remotesales.data.vo.JSSDKConfigVo;

import java.util.List;

public interface WecomAuthService {
    WecomSessionInfoDTO login(WecomAuthDTO wecomAuthDTO);

    WecomSessionInfoDTO getLoginSessionUserInfo(String userId);

    JSSDKConfigVo generateJSSDKConfig(String url);

    JSSDKConfigVo generateWechatJSSDKConfig(String url);

    WecomSessionInfoDTO switchUserInfo(String storeId, String userRole);
    List<WecomSessionInfoDTO.Store> rmStorePerformanceList();
}
