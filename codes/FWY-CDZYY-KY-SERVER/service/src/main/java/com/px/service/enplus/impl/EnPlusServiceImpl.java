package com.px.service.enplus.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.alibaba.fastjson2.JSONObject;
import com.px.common.annotation.DynamicCache;
import com.px.common.constant.ResponseEnum;
import com.px.common.enums.EnPlusApi;
import com.px.common.exception.BusinessException;
import com.px.common.exception.EnPushException;
import com.px.common.utils.AESUtil;
import com.px.common.utils.CommUtil;
import com.px.entity.common.RedisKeys;
import com.px.entity.enplus.EnRespQueryToken;
import com.px.entity.enplus.response.EnResponse;
import com.px.service.enplus.EnPlusService;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @author 品讯科技
 * @description EN+平台接口服务
 * @date 2024-08
 */
@Service
public class EnPlusServiceImpl implements EnPlusService {

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final Logger LOGGER = LoggerFactory.getLogger(EnPlusServiceImpl.class);
    static OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder().build();
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${en-plus.operatorId}")
    private String OperatorId;
    @Value("${en-plus.operatorSecret}")
    private String OperatorSecret;
    @Value("${en-plus.sigSecret}")
    private String SigSecret;

    public EnPlusServiceImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public static <T> T parse(String json, Class<T> clz) {
        return JSONObject.parseObject(json, clz);
    }

    private static String synchronizedCall(Request request) {
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * EN+ POST请求专属方法
     *
     * @param url
     * @param params
     * @return
     */
    @Override
    public EnResponse enPlusPost(String url, String params) {
        // token获取
        var token = queryToken();
        Headers headers = Headers.of("Authorization", "Bearer ".concat(token));
        RequestBody requestBody = RequestBody.create(params, JSON);
        Request request = new Request.Builder()
                .headers(headers)
                .post(requestBody)
                .url(url)
                .build();
        var response = parse(synchronizedCall(request), EnResponse.class);

        if (0 == response.getRet()) {
            return response;
        } else {
            LOGGER.error("：url:{}\n params:{}\ntoken:{}\n返回信息：{}", url, params, token, response);
            if(4002 == response.getRet() ){
                // 如果返回Ret=4002，token错误的情况下，删除redis中的token,如果是预约订单则将此订单设置为延迟启动
                redisTemplate.delete(RedisKeys.EN_PLUS_TOKEN);
                throw new BusinessException(ResponseEnum.EN_PLUS_TOKEN_EXCEPTION);
            }
            throw new BusinessException(ResponseEnum.EN_PLUS_API_EXCEPTION);
        }
    }

    /**
     * 获取en+ AccessToken
     * HMAC-MD5
     * 签名密钥对整个消息体加密，MD5摘要，参数签名要求大写
     * 签名消息体拼接顺序：OperatorID、Data、TimeStamp、Seq
     */
    @Override
    public String queryToken() {
        LOGGER.debug("查询token");
        var token = redisTemplate.opsForValue().get(RedisKeys.EN_PLUS_TOKEN);
        if (CommUtil.isNotEmptyAndNull(token)) {
            LOGGER.debug("从缓存中查询到token：{}，ttl:{}", token, redisTemplate.getExpire(RedisKeys.EN_PLUS_TOKEN));
            return token;
        }
        var data = """
                {
                    "OperatorID":"%s",
                    "OperatorSecret":"%s"
                }
                """.formatted(OperatorId, OperatorSecret);

        var requestParams = buildParams(data);

        var enResponse = enGetToken(EnPlusApi.EN_PLUS_QUERY_TOKEN.getApi(), requestParams);

        if (enResponse != null && 0 == enResponse.getRet()) {
            // 解密Data获取token
            var enRespQueryToken = JSONObject.parseObject(AESUtil.decrypt(enResponse.getData()), EnRespQueryToken.class);
            LOGGER.debug("EN+接口AccessToken:{}", enRespQueryToken.toString());
            // 缓存token,有效期7天(我们这里每次请求en+获取token的有效期并不是从7天开始，有效期是在en+的剩余有效时间)
            redisTemplate.opsForValue().set(RedisKeys.EN_PLUS_TOKEN, enRespQueryToken.getAccessToken(), enRespQueryToken.getTokenAvailableTime(), TimeUnit.SECONDS);
            return enRespQueryToken.getAccessToken();
        } else {
            // 记录错误码，返回错误信息
            LOGGER.error("EN+接口错误:接口名{}：返回信息：{}", "query_token", enResponse);
            throw new BusinessException(ResponseEnum.EN_PLUS_QUERY_TOKEN_ERROR);
        }
    }

    public EnResponse enGetToken(String url, String params) {
        // token获取
        RequestBody requestBody = RequestBody.create(params, JSON);
        Request request = new Request.Builder()
                .post(requestBody)
                .url(url)
                .build();
        var response = parse(synchronizedCall(request), EnResponse.class);

        if (response != null && 0 == response.getRet()) {
            return response;
        } else {
            LOGGER.error("EN+接口数据异常：url:{}\n params:{}\n返回信息：{}", url, params, response);
            throw new BusinessException(ResponseEnum.EN_PLUS_API_EXCEPTION);
        }
    }


    /**
     * 加密数据、签名、组装请求消息体
     *
     * @param params
     * @return
     */
    @Override
    public String buildParams(String params) {
        // 使用DataSecret对data加密
        var dataStr = AESUtil.encrypt(params);
        // 时间戳
        var timeStamp = LocalDateTimeUtil.format(LocalDateTime.now(), DatePattern.PURE_DATETIME_PATTERN);
        // 自增序列
        var seq = timeStamp.substring(timeStamp.length() - 4);
        // 使用SigSecret以HMAC-MD5算法对消息体签名（签名顺序：OperatorId、Data、TimeStamp、Seq）
        var signString = OperatorId + dataStr + timeStamp + seq;
        HMac mac = new HMac(HmacAlgorithm.HmacMD5, SigSecret.getBytes());
        // 签名（转为大写）
        var sign = mac.digestHex(signString).toUpperCase();
        return """
                {
                    "OperatorID":"%s",
                    "Data":"%s",
                    "TimeStamp":"%s",
                    "Seq":"%s",
                    "Sig":"%s"
                }
                """.formatted(OperatorId, dataStr, timeStamp, seq, sign);
    }

    /**
     * 验签、解密推送消息
     *
     * @param json
     * @return
     */
    @Override
    public String signValidation(JSONObject json) {
        // 验签 解密数据
        var OperatorID = json.getString("OperatorID");
        var Data = json.getString("Data");
        var TimeStamp = json.getString("TimeStamp");
        var Seq = json.getString("Seq");
        var Sig = json.getString("Sig");
        var signString = OperatorID + Data + TimeStamp + Seq;
        HMac mac = new HMac(HmacAlgorithm.HmacMD5, SigSecret.getBytes());
        // 签名（转为大写）
        var sign = mac.digestHex(signString).toUpperCase();
        if (sign.equals(Sig)) {
            // 解密数据
            return AESUtil.decrypt(Data);
        } else {
            // 验签失败
            EnResponse enResponse = new EnResponse();
            enResponse.setRet(4001);
            enResponse.setMsg(ResponseEnum.EN_PLUS_PUSH_SIGN_FAIL.getMessage());
            LOGGER.error("EN+推送数据验签失败，数据：{}", json);
            throw new EnPushException(ResponseEnum.EN_PLUS_PUSH_SIGN_FAIL, enResponse);
        }

    }


    /**
     * 请求设备认证
     *
     * @param connectorId  充电设备接口编码
     * @param equipAuthSeq 格式：运营商ID+唯一编码 27字符
     * @return
     */
    @Override
    public JSONObject queryEquipAuth(String connectorId, String equipAuthSeq) {
        var param = """
                {
                    "EquipAuthSeq":"%s",
                    "ConnectorID":"%s"
                }
                """.formatted(equipAuthSeq, connectorId);
        var response = enPlusPost(EnPlusApi.EN_PLUS_QUERY_EQUIP_AUTH.getApi(), buildParams(param));

        return JSONObject.parseObject(AESUtil.decrypt(response.getData()));
    }


    /**
     * 查询业务策略信息
     *
     * @param equipBizSeq 业务策略查询流水号 格式：运营商ID+唯一编码 27字符
     * @param connectorId 充电设备接口编码
     * @return
     */
    @Override
    @DynamicCache(spel = "#connectorId")
    public JSONObject queryEquipBusinessPolicy(String equipBizSeq, String connectorId) {
        var param = """
                {
                    "EquipBizSeq":"%s",
                    "ConnectorID":"%s"
                }
                """.formatted(equipBizSeq, connectorId);
        var response = enPlusPost(EnPlusApi.EN_PLUS_QUERY_EQUIP_BUSINESS_POLICY.getApi(), buildParams(param));
        return JSONObject.parseObject(AESUtil.decrypt(response.getData()));
    }

    /**
     * 请求EN+启动充电
     *
     * @param startChargeSeq
     * @param connectorId
     * @param qrCode
     * @param amount
     * @return
     */
    @Override
    public JSONObject queryStartCharge(String startChargeSeq, String connectorId, String qrCode, Integer amount) {
        var param = """
                {
                    "StartChargeSeq":"%s",
                    "ConnectorID":"%s",
                    "QRCode":"%s",
                    "amount":"%d"
                }
                """.formatted(startChargeSeq, connectorId, qrCode, amount);
        var response = enPlusPost(EnPlusApi.EN_PLUS_QUERY_START_CHARGE.getApi(), buildParams(param));
        return JSONObject.parseObject(AESUtil.decrypt(response.getData()));
    }

    /**
     * 查询充电状态
     *
     * @param startChargeSeq
     * @return
     */
    @Override
    public JSONObject queryEquipChargeStatus(String startChargeSeq) {
        var param = """
                {
                    "StartChargeSeq":"%s"
                }
                """.formatted(startChargeSeq);
        var response = enPlusPost(EnPlusApi.EN_PLUS_QUERY_EQUIP_CHARGE_STATUS.getApi(), buildParams(param));
        return JSONObject.parseObject(AESUtil.decrypt(response.getData()));
    }

    /**
     * 与en+开发约定使用该接口更新正在充电中的订单的金额信息，解决充电过程中用户充值续充场景
     *
     * @param startChargeSeq
     * @param amount
     * @return
     */
    @Override
    public JSONObject updateBalanceByQueryEquipChargeStatus(String startChargeSeq, int amount) {
        var param = """
                {
                    "StartChargeSeq":"%s",
                    "amount":%d
                }
                """.formatted(startChargeSeq, amount);
        var response = enPlusPost(EnPlusApi.EN_PLUS_QUERY_EQUIP_CHARGE_STATUS.getApi(), buildParams(param));
        return JSONObject.parseObject(AESUtil.decrypt(response.getData()));
    }

    /**
     * 请求停止充电
     *
     * @param startChargeSeq
     * @param connectorId
     * @return
     */
    @Override
    public JSONObject queryStopCharge(String startChargeSeq, String connectorId) {
        var param = """
                {
                    "StartChargeSeq":"%s",
                    "ConnectorID":"%s"
                }
                """.formatted(startChargeSeq, connectorId);
        var response = enPlusPost(EnPlusApi.EN_PLUS_QUERY_STOP_CHARGE.getApi(), buildParams(param));
        return JSONObject.parseObject(AESUtil.decrypt(response.getData()));
    }

}
