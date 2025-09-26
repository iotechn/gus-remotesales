package com.dobbinsoft.gus.remotesales.utils;

import com.dobbinsoft.gus.remotesales.data.dto.session.BoSessionInfoDTO;
import com.dobbinsoft.gus.remotesales.data.dto.session.WechatSessionInfoDTO;
import com.dobbinsoft.gus.remotesales.data.dto.session.WecomSessionInfoDTO;

public class SessionUtils {

    private SessionUtils() {}

    private static final ThreadLocal<BoSessionInfoDTO> BO_USER_INFO_THREAD_LOCAL = new InheritableThreadLocal<>();
    private static final ThreadLocal<WecomSessionInfoDTO> WECOM_USER_INFO_THREAD_LOCAL = new InheritableThreadLocal<>();
    private static final ThreadLocal<WechatSessionInfoDTO> WECHAT_USER_INFO_THREAD_LOCAL = new InheritableThreadLocal<>();

    public static void putBoSession(BoSessionInfoDTO infoDTO) {
        BO_USER_INFO_THREAD_LOCAL.set(infoDTO);
    }

    public static BoSessionInfoDTO getBoSession() {
        return BO_USER_INFO_THREAD_LOCAL.get();
    }

    public static void removeBoSession() {
        BO_USER_INFO_THREAD_LOCAL.remove();
    }

    public static void putWechatSession(WechatSessionInfoDTO infoDTO) {
        WECHAT_USER_INFO_THREAD_LOCAL.set(infoDTO);
    }

    public static WechatSessionInfoDTO getWechatSession() {
        return WECHAT_USER_INFO_THREAD_LOCAL.get();
    }

    public static void removeWechatSession() {
        WECHAT_USER_INFO_THREAD_LOCAL.remove();
    }


    public static void putWecomSession(WecomSessionInfoDTO infoDTO) {
        WECOM_USER_INFO_THREAD_LOCAL.set(infoDTO);
    }

    public static WecomSessionInfoDTO getWecomSession() {
        return WECOM_USER_INFO_THREAD_LOCAL.get();
    }

    public static void removeWecomSession() {
        WECOM_USER_INFO_THREAD_LOCAL.remove();
    }

}
