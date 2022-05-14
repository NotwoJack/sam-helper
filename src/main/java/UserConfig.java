import cn.hutool.core.util.RandomUtil;

import java.util.*;

/**
 * 用户信息
 * 通过小程序抓包购物车接口获取headers和body中的数据填入
 */
public class UserConfig {

    //1：极速达 2：全城配送
    public static final String deliveryType = "2";
    //下单目标金额
    public static final Integer targetAmount = 100;
    //是否使用优惠券（目前仅支持积分换的满减卷）
    public static final Boolean coupon = false;
    //保供套餐名称白名单，可以自行编辑。例子("鲜食","食品")，名称中含有这些字段的套餐会被筛选出来
    public static final List<String> whitelist = Arrays.asList("鲜食","食品");
    //bark通知id
    public static final String barkId = "wrkU4Qz9VHZuf7Pv7hco2S";
    //Server酱 用户 Token，可选参数，获取方式：https://sct.ftqq.com/sendkey
    public static final String ftqqSendKey = "";
    /**
     * 抓包小程序，在headers中找到auth-token
     */
    public static Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", "api-sams.walmartmobile.cn");
        headers.put("Content-Type", "application/json;charset=UTF-8");
        headers.put("User-Agent", "SamClub/5.0.45 (iPhone; iOS 15.4; Scale/3.00)");
        headers.put("device-type", "mini_program");
//        headers.put("auth-token", "740d926b981716f4a42ea734ed0c250a5c54c502a29294e5592d635eb97214d8");//1号主卡 自己 主卡
//        headers.put("auth-token", "740d926b981716f4ed02ced09db92ccec63cf4cbe3c3f2b023cfb7c5c1c4d8d9");//2号主卡 小弟
//        headers.put("auth-token", "740d926b981716f4b051704c3a27202630da6b9729f4ed2c15a276abc7d7855c");//2号副卡 小弟副卡
//        headers.put("auth-token", "740d926b981716f4c16302eff370ddfa4a811619fce85bd26b65280e8e651e48");//3号主卡 zfl
//        headers.put("auth-token", "740d926b981716f4d7e02e601272a2c9a9bf9b11d79a6bcb40b3c39f92aa20936");//3号副卡 zp
//        headers.put("auth-token", "740d926b981716f4b728beded2a85c055f0a3e7b44bb53d1c0897dbbe9f00581");//4号主卡 ysh
        headers.put("auth-token", "740d926b981716f4ec756cf323e99e72b006365536ebe3c2a34ade54f8962b82");//5号主卡 taozi
        return headers;
    }

    private static final Integer NUM = RandomUtil.randomInt(10,99);

    public static Map<String,Object> getIdInfo() {
        Map<String,Object> idInfo = new HashMap<>();
        idInfo.put("uid", "181866"+NUM+"3575");
        idInfo.put("appId", "wxb344a6"+NUM+"43eaaf849");
        idInfo.put("saasId", "1818");
        return idInfo;
    }

}
