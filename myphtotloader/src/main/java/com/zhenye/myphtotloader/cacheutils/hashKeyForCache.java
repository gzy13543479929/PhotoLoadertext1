package com.zhenye.myphtotloader.cacheutils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;



public class hashKeyForCache {
    /**
     * 对传入的URL进行MD5加密。
     * @param url 需要转化的原始数据
     */
    public static String translated(String url){
        String cacheKey;

        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(url.getBytes());
            cacheKey = BYTEStOhEXsTRING(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(url.hashCode());
            e.printStackTrace();
        }

        return cacheKey;
    }
    private static String BYTEStOhEXsTRING(byte[] bytes){
        StringBuilder sb = new StringBuilder();

        for (int i=0;i<bytes.length;i++){
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1){
                sb.append('0');
            }
            sb.append(hex);
        }

        return sb.toString();
    }
}
