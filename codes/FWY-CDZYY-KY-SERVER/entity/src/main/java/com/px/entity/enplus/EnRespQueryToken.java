package com.px.entity.enplus;

import lombok.Data;

/**
 * @author 品讯科技
 * @description query_token 返回值
 * @date 2024-08
 */
@Data
public class EnRespQueryToken {
    /**
     * 运营商ID
     */
    private String OperatorID;
    /**
     * 成功状态
     * 0：成功
     * 1：失败
     */
    private int SuccStat;
    /**
     * 全局唯一凭证
     */
    private String AccessToken;
    /**
     * 凭证有效期（秒）
     */
    private int TokenAvailableTime;
    /**
     * 失败原因:
     * 0：无
     * 1：无此运营商
     * 2：密钥错误
     * 3~99：自定义
     */
    private String FailReason;

}
