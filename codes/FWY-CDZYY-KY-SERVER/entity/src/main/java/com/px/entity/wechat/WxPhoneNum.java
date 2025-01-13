package com.px.entity.wechat;

import lombok.Data;

/**
 * @author 品讯科技
 * @description
 * @date 2024-08
 */
@Data
public class WxPhoneNum {
    public Integer errcode;
    public String errmsg;
    public PhoneInfo phone_info;
    public WaterMark watermark;

    @Data
    public class PhoneInfo {
        String phoneNumber;
        String purePhoneNumber;
        String countryCode;
    }


    @Data
    public class WaterMark {
        Integer timestamp;
        String appid;
    }

}
