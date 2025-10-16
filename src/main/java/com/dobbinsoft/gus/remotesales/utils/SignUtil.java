package com.dobbinsoft.gus.remotesales.utils;

import com.dobbinsoft.gus.remotesales.client.wecom.vo.JSAPITicket;
import com.dobbinsoft.gus.remotesales.data.vo.JSSDKConfigVo;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class SignUtil {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();


    public static JSSDKConfigVo assembleConfigVo(String url, JSAPITicket apiTicketResult) {
        JSSDKConfigVo configVo = new JSSDKConfigVo();
        // 生成时间戳
        Long tstamp = System.currentTimeMillis() / 1000;
        String timestamp = String.valueOf(tstamp);
        // 生成随机字符串
        String nonceStr = SignUtil.getNonceStr();
        // 使用Ticket、时间戳、URL和随机字符串生成签名
        String sign = SignUtil.sign(apiTicketResult.getTicket(), timestamp, url, nonceStr);
        // 设置配置参数
        configVo.setNonceStr(nonceStr);
        configVo.setTimestamp(timestamp);
        configVo.setTstamp(tstamp);
        configVo.setSignature(sign);
        return configVo;
    }

    // used by jssdk and some other case
    // http://qydev.weixin.qq.com/wiki/index.php?title=%E5%BE%AE%E4%BF%A1JS-SDK%E6%8E%A5%E5%8F%A3#.E9.99.84.E5.BD.951-JS-SDK.E4.BD.BF.E7.94.A8.E6.9D.83.E9.99.90.E7.AD.BE.E5.90.8D.E7.AE.97.E6.B3.95
    public static String generateDataFromMap(Map<String, String> map) {
        Map<String, String> sorted = new TreeMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sorted.put(StringUtils.lowerCase(entry.getKey()), entry.getValue());
        }

        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            if (!result.isEmpty())
                result.append("&");
            result.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return result.toString();
    }
    /**
     * 生成微信JSSDK签名
     * 注意：虽然SHA-1不是推荐的加密哈希函数，但这是微信JSSDK的官方要求
     * 参考文档：https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/JS-SDK.html
     *
     * @param jsAPITicket JSAPI Ticket
     * @param timestamp   时间戳
     * @param url         当前网页的URL
     * @param nonceStr    随机字符串
     * @return 签名字符串
     */
    public static String sign(String jsAPITicket, String timestamp, String url, String nonceStr) {
        Map<String, String> map = new HashMap<>();
        map.put("jsapi_ticket", jsAPITicket);
        map.put("timestamp", timestamp);
        map.put("url", url);
        map.put("noncestr", nonceStr);
        String data = SignUtil.generateDataFromMap(map);
        return DigestUtils.sha1Hex(data);
    }
    public static  String getNonceStr() {
        int count = SECURE_RANDOM.nextInt(15) + 5;
        char[] chars = new char[]{
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
        };
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(chars[SECURE_RANDOM.nextInt(chars.length)]);
        }
        return builder.toString();
    }

    /**
     * 验证微信回调签名
     * 官方加密、校验流程：将token，timestamp，nonce这三个参数进行字典序排序，
     * 然后将这三个参数字符串拼接成一个字符串进行sha1加密，
     * 开发者获得加密后的字符串可以与signature对比，表示该请求来源于微信。
     *
     * @param signature 微信传递的签名
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @param token     微信配置的token
     * @return 验证是否通过
     */
    public static boolean checkWechatSignature(String signature, String timestamp, String nonce, String token) {
        if (StringUtils.isBlank(signature) || StringUtils.isBlank(timestamp) || 
            StringUtils.isBlank(nonce) || StringUtils.isBlank(token)) {
            return false;
        }
        
        // 将token，timestamp，nonce这三个参数进行字典序排序
        String[] tmpArr = {token, timestamp, nonce};
        java.util.Arrays.sort(tmpArr);
        
        // 将三个参数字符串拼接成一个字符串
        StringBuilder tmpStr = new StringBuilder();
        for (String str : tmpArr) {
            tmpStr.append(str);
        }
        
        // 进行sha1加密
        String encryptedStr = DigestUtils.sha1Hex(tmpStr.toString());
        
        // 与signature对比
        return encryptedStr.equals(signature);
    }

}
