import java.util.HashMap;
import java.util.Map;

/**
 * 用户信息
 * 通过小程序抓包购物车接口获取headers和body中的数据填入
 */
public class UserConfig {

    //收货地址id
    public static final String DEVICEID = "62b268f78810a0bc9f858a8100001cf15a03";
    public static final String LATITUDE = "31.230096";
    public static final String LONGITUDE = "121.514693";
    public static final String AUTHTOKEN = "740d926b981716f4a42ea734ed0c250a5c54c502a29294e5592d635eb97214d8";

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

        headers.put("device-type", "ios");
        headers.put("app-versione", "5.0.47.0");
        headers.put("apptype", "ios");
        headers.put("device-name", "iPhone14,5");
        headers.put("device-os-version", "15.4");

        headers.put("device-id", DEVICEID);
        headers.put("latitude", LATITUDE);
        headers.put("longitude", LONGITUDE);
        headers.put("auth-token", AUTHTOKEN);
    return headers;
    }

    /**
     * 抓包后参考项目中的image/body.jpeg 把信息一行一行copy到下面 没有的key不需要复制
     * <p>
     * 这里不能加泛型 有些接口是params  泛型必须要求<String,String> 有些是form表单 泛型要求<String,Object> 无法统一
     */
    public static Map getBody() {
        Map body = new HashMap<>();
        body.put("uid", "");
        body.put("longitude", "");
        body.put("latitude", "");
        body.put("station_id", "");//这个是站点id 请仔细检查和确认这个参数 进入小程序之后首页左上角是不是你所在的站点 如果不是先选择好站点再抓包 不要把站点搞错了 否则不能下单
        body.put("city_number", "");
        body.put("api_version", "");
        body.put("app_version", "");
        body.put("applet_source", "");
        body.put("channel", "applet");
        body.put("app_client_id", "4");
        body.put("sharer_uid", "");
        body.put("openid", "");
        body.put("h5_source", "");
        body.put("device_token", "");
        return body;
    }

}
