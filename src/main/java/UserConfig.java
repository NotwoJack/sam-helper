import cn.hutool.core.util.RandomUtil;

import java.util.*;

/**
 * 用户信息
 * 通过小程序抓包购物车接口获取headers和body中的数据填入
 */
public class UserConfig {

    //1：极速达 2：全城配送 3：保供套餐
    public static final String deliveryType = "3";
    //下单目标金额
    public static final Integer targetAmount = 0;
    //是否使用优惠券
    public static final Boolean coupon = true;
    //保供套餐名称白名单，可以自行编辑。例子("鲜食","食品")，名称中含有这些字段的套餐会被筛选出来
    public static final List<String> whitelist = Arrays.asList("套餐");
    //bark通知id
    public static final String barkId = "";
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
        headers.put("auth-token", "需要填写");
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
