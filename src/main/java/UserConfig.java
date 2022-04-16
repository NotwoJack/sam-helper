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
    public static final String labelList = "[{\\\"attachId\\\":\\\"1649949934151-1a291f41-226d-4859-8f7e-f64516ac292f\\\",\\\"createTime\\\":1649949934287,\\\"labelType\\\":\\\"tracking_id\\\"},{\\\"attachId\\\":1074,\\\"createTime\\\":1649949934289,\\\"labelType\\\":\\\"scene_xcx\\\"}]";

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
        idInfo.put("uid", "181816233927");
        idInfo.put("appId", "wxb344a8513eaaf849");
        idInfo.put("saasId", "1818");
        return idInfo;
    }

}
