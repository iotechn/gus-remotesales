package com.dobbinsoft.gus.remotesales.configuration;

import com.dobbinsoft.gus.remotesales.data.constant.HeaderConstants;
import com.dobbinsoft.gus.remotesales.data.dto.session.BoSessionInfoDTO;
import com.dobbinsoft.gus.remotesales.data.dto.session.WechatSessionInfoDTO;
import com.dobbinsoft.gus.remotesales.data.dto.session.WecomSessionInfoDTO;
import com.dobbinsoft.gus.remotesales.exception.RemotesalesErrorCode;
import com.dobbinsoft.gus.remotesales.service.WeChatAuthService;
import com.dobbinsoft.gus.remotesales.service.WecomAuthService;
import com.dobbinsoft.gus.remotesales.utils.SessionUtils;
import com.dobbinsoft.gus.web.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Objects;


@Configuration
@Slf4j
public class WebInterceptorsConfig implements WebMvcConfigurer {
    @Autowired
    @Lazy
    WecomAuthService wecomAuthService;
    @Autowired
    @Lazy
    WeChatAuthService weChatAuthService;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new BoWebRequestInterceptor()).addPathPatterns("/bo/**");
        registry.addInterceptor(new WecomWebRequestInterceptor(wecomAuthService)).addPathPatterns("/wecom/**").excludePathPatterns("/wecom/auth/wxlogin");
        registry.addInterceptor(new WeChatWebRequestInterceptor(weChatAuthService)).addPathPatterns("/wechat/**").excludePathPatterns("/wechat/auth/wxlogin");

    }

    public static class BoWebRequestInterceptor implements HandlerInterceptor {
        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
            SessionUtils.removeBoSession();
        }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            try {
                String employeeEmail = request.getHeader(HeaderConstants.EMPLOYEE_EMAIL);
                String userId = request.getHeader(HeaderConstants.USER_ID);
                BoSessionInfoDTO headerInfo = new BoSessionInfoDTO();
                headerInfo.setEmployeeEmail(employeeEmail);
                headerInfo.setUserId(userId);
                headerInfo.setName(employeeEmail);
                SessionUtils.putBoSession(headerInfo);
                return HandlerInterceptor.super.preHandle(request, response, handler);
            } catch (ServiceException e) {
                log.warn("[BO WEB Service Exception]", e);
                throw e;
            } catch (Exception e) {
                log.error("[BO Web] pre handle 异常", e);
                return false;
            }
        }
    }

    public static class WecomWebRequestInterceptor implements HandlerInterceptor {
        private final WecomAuthService wecomAuthService;

        public WecomWebRequestInterceptor(WecomAuthService wecomAuthService) {
            this.wecomAuthService = wecomAuthService;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
            SessionUtils.removeWecomSession();
        }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            try {
                String userId = request.getHeader(HeaderConstants.USER_ID);
                WecomSessionInfoDTO sessionInfoDTO = wecomAuthService.getLoginSessionUserInfo(userId);
                if (Objects.isNull(sessionInfoDTO)) {
                    log.warn("[Wecom Interceptor] user session not exists userId:{}", userId);
                    throw new ServiceException(RemotesalesErrorCode.UNAUTHORIZED);
                }
                SessionUtils.putWecomSession(sessionInfoDTO);
                return HandlerInterceptor.super.preHandle(request, response, handler);
            } catch (ServiceException e) {
                log.warn("[Wecom Service Exception]", e);
                throw e;
            } catch (Exception e) {
                log.error("[Wecom] pre handle 异常", e);
                return false;
            }
        }
    }


    public static class WeChatWebRequestInterceptor implements HandlerInterceptor {

        WeChatAuthService weChatAuthService;

        public WeChatWebRequestInterceptor(WeChatAuthService weChatAuthService) {
            this.weChatAuthService = weChatAuthService;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
            SessionUtils.removeWechatSession();
        }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            try {
                String userId = request.getHeader(HeaderConstants.USER_ID);
                WechatSessionInfoDTO sessionInfoDTO = weChatAuthService.getLoginSessionUserInfo(userId);
                if (Objects.isNull(sessionInfoDTO)) {
                    log.warn("[WeChat Interceptor] user session not exists userId:{}", userId);
                    throw new ServiceException(RemotesalesErrorCode.UNAUTHORIZED);
                }
                SessionUtils.putWechatSession(sessionInfoDTO);
                return HandlerInterceptor.super.preHandle(request, response, handler);
            } catch (ServiceException e) {
                log.warn("[Wechat Service Exception]", e);
                throw e;
            } catch (Exception e) {
                log.error("[Wechat] pre handle 异常", e);

                return false;
            }
        }
    }
}
