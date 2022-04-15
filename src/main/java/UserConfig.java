import java.util.HashMap;
import java.util.Map;

/**
 * 用户信息
 * 通过小程序抓包购物车接口获取headers和body中的数据填入
 */
public class UserConfig {

    //1：极速达 2：全城配送
    public static final String deliveryType = "2";
    //1：极速达 2：全城配送
    public static final String cartDeliveryType = "2";
    //commitPay接口中可以抓到
    public static final String labelList = "需要填写";

    /**
     * 抓包后参考项目中的image/headers.jpeg 把信息一行一行copy到下面 没有的key不需要复制
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
        //以上不需要修改
        headers.put("auth-token", "740d926b981716f4a42ea734ed0c250a5c54c502a29294e5592d635eb97214d8");
    return headers;
    }

    /**
     * 抓包后参考项目中的image/body.jpeg 把信息一行一行copy到下面 没有的key不需要复制
     */
    public static Map<String,Object> getIdInfo() {
        Map<String,Object> idInfo = new HashMap<>();
        idInfo.put("uid", "需要填写");
        idInfo.put("appId", "需要填写");
        idInfo.put("saasId", "需要填写");
        return idInfo;
    }

}
