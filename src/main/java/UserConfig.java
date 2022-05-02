import java.util.HashMap;
import java.util.Map;

/**
 * 用户信息
 * 通过小程序抓包购物车接口获取headers和body中的数据填入
 */
public class UserConfig {

    //1：极速达 2：全城配送
    public static final String deliveryType = "1";
    //下单目标金额
    public static final Integer targetAmount = 500;
    //是否使用优惠券
    public static final Boolean coupon = true;
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
        headers.put("Connection", "keep-alive");
        headers.put("Accept", "*/*");
        headers.put("Content-Type", "application/json;charset=UTF-8");
        headers.put("Content-Length", "221");
        headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9");
        headers.put("User-Agent", "SamClub/5.0.45 (iPhone; iOS 15.4; Scale/3.00)");
        headers.put("device-type", "mini_program");
        headers.put("auth-token", "740d926b981716f4a42ea734ed0c250a5c54c502a29294e5592d635eb97214d8");
//        headers.put("auth-token", "740d926b981716f47b1cb146b04a11ba9dfcdb914034d2fe0fec2953aa9b1c33");
        return headers;
    }

    public static Map<String,Object> getIdInfo() {
        Map<String,Object> idInfo = new HashMap<>();
        idInfo.put("uid", "181817999575");
        idInfo.put("appId", "wxb344a8999eaaf849");
        idInfo.put("saasId", "1818");
        return idInfo;
    }

}
