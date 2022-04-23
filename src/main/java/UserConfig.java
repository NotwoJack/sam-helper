import java.util.HashMap;
import java.util.Map;

/**
 * 用户信息
 * 通过小程序抓包购物车接口获取headers和body中的数据填入
 */
public class UserConfig {

    //0:普通抢购模式 1：保供套餐抢购模式
    public static final Integer mode = 1 ;

    //1：极速达 2：全城配送
    public static final String deliveryType = "2";
    //1：极速达 2：全城配送
    public static final String cartDeliveryType = "2";
    //commitPay接口中可以抓到，非必填项，不需要修改
    public static final String labelList = "[{\"attachId\":\"1649949934151-1a291f41-999c-4859-8f7e-f64516ac292f\",\"createTime\":1649949934287,\"labelType\":\"tracking_id\"},{\"attachId\":1074,\"createTime\":1649949934289,\"labelType\":\"scene_xcx\"}]";
    //bark通知id
    public static final String barkId = "";
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
        headers.put("auth-token", "需要填写");
    return headers;
    }

    public static Map<String,Object> getIdInfo() {
        Map<String,Object> idInfo = new HashMap<>();
        idInfo.put("uid", "181817666575");
        idInfo.put("appId", "wxb344a8666eaaf849");
        idInfo.put("saasId", "1818");
        return idInfo;
    }

}
