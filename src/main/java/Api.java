import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.SneakyThrows;

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 接口封装
 */
public class Api {

    public static final Map<String, Object> context = new ConcurrentHashMap<>();

    public static Map<String, Map<String, Object>> init(){
        try {
            Map<String, Map<String, Object>> map = new HashMap<>();
            Map<String, Object> deliveryAddressDetail = getDeliveryAddressDetail();
            Map<String, Object> storeDetail = getMiniUnLoginStoreList(Double.parseDouble((String) deliveryAddressDetail.get("latitude")), Double.parseDouble((String) deliveryAddressDetail.get("longitude")));
            map.put("deliveryAddressDetail",deliveryAddressDetail);
            map.put("storeDetail",storeDetail);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SneakyThrows
    public static void play() {
        //这里还可以使用企业微信或者钉钉的提供的webhook  自己写代码 很简单 就是按对应数据格式发一个请求到企业微信或者钉钉
        AudioClip audioClip = Applet.newAudioClip(new File("ding-dong.wav").toURL());
        audioClip.loop();
        Thread.sleep(60000);//响铃60秒
    }

    private static void print(boolean normal, String message) {
        if (Api.context.containsKey("end")) {
            return;
        }
        if (normal) {
            System.out.println(DateTime.now() + message);
        } else {
            System.err.println(DateTime.now() + message);
        }
    }


    /**
     * 验证请求是否成功
     *
     * @param object     返回体
     * @param actionName 动作名称
     * @return 是否成功
     */
    private static boolean isSuccess(JSONObject object, String actionName) {
        Boolean success = object.getBool("success");
        if (success == null) {
            if ("405".equals(object.getStr("code"))) {
                print(false,actionName + "失败:" + "出现此问题有三个可能 1.偶发，无需处理 2.一个账号一天只能下两单  3.不要长时间运行程序，目前已知有人被风控了，暂时未确认风控的因素是ip还是用户或设备相关信息，如果要测试用单次执行模式，并发只能用于6点、8点半的前一分钟，然后执行时间不能超过2分钟，如果买不到就不要再执行程序了，切忌切忌，如果已经被风控的可以尝试过一段时间再试，或者换号");
            } else {
                print(false,actionName + "失败,服务器返回无法解析的内容:" + JSONUtil.toJsonStr(object));
            }
            return false;
        }
        if (success) {
            return true;
        }
        print(false,actionName + "失败:" + object.get("msg"));
        return false;
    }

    public static Map<String, Object> getDeliveryAddressDetail() {
        try {
            HttpRequest httpRequest = HttpUtil.createPost("https://api-sams.walmartmobile.cn/api/v1/sams/trade/cart/getDeliveryAddressDetail");
            httpRequest.addHeaders(UserConfig.getHeaders());
            Map<String, Object> request = UserConfig.getIdInfo();

            httpRequest.body(JSONUtil.toJsonStr(request));
            String body = httpRequest.execute().body();
            JSONObject object = JSONUtil.parseObj(body);
            if (!isSuccess(object, "获取下单地址")) {
                return null;
            }
            Map<String, Object> map = new HashMap<>();
            JSONObject data = object.getJSONObject("data");
            map.put("addressId", data.getStr("addressId"));
            map.put("latitude", data.getStr("latitude"));
            map.put("longitude", data.getStr("longitude"));
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, Object> getMiniUnLoginStoreList(Double latitude, Double longitude) {
        try {
            HttpRequest httpRequest = HttpUtil.createPost("https://api-sams.walmartmobile.cn/api/v1/sams/merchant/storeApi/getMiniUnLoginStoreList");
            httpRequest.addHeaders(UserConfig.getHeaders());
            Map<String, Object> request = UserConfig.getIdInfo();
            request.put("latitude",latitude);
            request.put("longitude",longitude);
            request.put("requestType","location_recmd");

            httpRequest.body(JSONUtil.toJsonStr(request));
            String body = httpRequest.execute().body();
            JSONObject object = JSONUtil.parseObj(body);
            if (!isSuccess(object, "获取商店信息")) {
                return null;
            }
            Map<String, Object> map = new HashMap<>();
            JSONArray storeList = object.getJSONObject("data").getJSONArray("storeList");
            Iterator<Object> iterator = storeList.iterator();
            while (iterator.hasNext()){
                JSONObject store = (JSONObject)iterator.next();
                if (store.getInt("storeType") == 2){
                    map.put("storeType",store.getStr("storeType"));
                    map.put("storeId",store.getStr("storeId"));
                    map.put("storeDeliveryTemplateId",store.getJSONObject("storeRecmdDeliveryTemplateData").getStr("storeDeliveryTemplateId"));
                    map.put("areaBlockId",store.getJSONObject("storeAreaBlockVerifyData").getStr("areaBlockId"));
                    map.put("deliveryModeId",store.getJSONObject("storeDeliveryModeVerifyData").getStr("deliveryModeId"));
                    map.put("storeName",store.getStr("storeName"));
                }
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取配送时间
     * @return
     */
    public static Map<String, Object> getCapacityData(Map<String, Object> storeDetail) {
        try {
            HttpRequest httpRequest = HttpUtil.createPost("https://api-sams.walmartmobile.cn/api/v1/sams/delivery/portal/getCapacityData");
            httpRequest.addHeaders(UserConfig.getHeaders());
            Map<String, Object> request = UserConfig.getIdInfo();

            List<String> date = new ArrayList<>();
            DateTime dateTime = new DateTime();
            for(int j = 0; j < 7; j++) {
                date.add(dateTime.toString("yyyy-MM-dd"));
                dateTime.offset(DateField.DAY_OF_MONTH, 1);
            }
            request.put("perDateList", date);
            request.put("storeDeliveryTemplateId", storeDetail.get("storeDeliveryTemplateId"));

            httpRequest.body(JSONUtil.toJsonStr(request));
            String body = httpRequest.execute().body();
            JSONObject object = JSONUtil.parseObj(body);
            if (!isSuccess(object, "更新配送时间")) {
                return null;
            }
            Map<String, Object> map = new HashMap<>();
            Boolean dateCondition = object.getJSONObject("data").getJSONArray("capcityResponseList").getJSONObject(0).getBool("dateISFull");
            if (dateCondition){
                print(false,"【失败】全部配送时间已满");
            } else {
                JSONArray times = object.getJSONObject("data").getJSONArray("capcityResponseList").getJSONObject(0).getJSONArray("list");
                for (int i = 0; i < times.size(); i++) {
                    JSONObject time = times.getJSONObject(i);
                    if (!time.getBool("timeISFull")){
                        map.put("startRealTime",time.get("startRealTime"));
                        map.put("endRealTime",time.get("endRealTime"));
                        print(true,"【成功】更新配送时间:" + time.getStr("startTime") + " -- " + time.getStr("endTime"));
                        return map;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取购物车信息
     * @return
     */
    public static List<GoodDto> getCart(Map<String, Object> storeDetail) {
        try {
            HttpRequest httpRequest = HttpUtil.createPost("https://api-sams.walmartmobile.cn/api/v1/sams/trade/cart/getUserCart");
            httpRequest.addHeaders(UserConfig.getHeaders());
            Map<String, Object> request = UserConfig.getIdInfo();

            List<Map> storeList = new ArrayList();
            Map<String,Object> store = new HashMap<>();
            store.put("storeType",storeDetail.get("storeType"));
            store.put("storeId",storeDetail.get("storeId"));
            store.put("areaBlockId",storeDetail.get("areaBlockId"));
            store.put("storeDeliveryTemplateId",storeDetail.get("storeDeliveryTemplateId"));
            storeList.add(store);
            request.put("storeList", storeList);

            httpRequest.body(JSONUtil.toJsonStr(request));
            String body = httpRequest.execute().body();
            JSONObject object = JSONUtil.parseObj(body);
            if (!isSuccess(object, "更新购物车")) {
                return null;
            }
            Integer selectedNumber = object.getJSONObject("data").getInt("selectedNumber");
            if (selectedNumber == 0){
                print(false,"购物车为空");
                return null;
            } else {
                JSONArray goods = object.getJSONObject("data").getJSONObject("miniProgramGoodsInfo").getJSONArray("normalGoodsList");
                List<GoodDto> goodDtos = new ArrayList<>();
                Integer amount = 0;
                for (int i = 0; i < goods.size(); i++) {
                    JSONObject good = goods.getJSONObject(i);
                    GoodDto goodDto = new GoodDto();
                    goodDto.setSpuId(good.getStr("spuId"));
                    goodDto.setQuantity(good.getStr("quantity"));
                    goodDto.setStoreId(good.getStr("storeId"));
                    amount = amount + good.getInt("quantity") * good.getInt("price");
                    goodDtos.add(goodDto);
                }
                context.put("amount", amount);
                print(true,"【成功】更新购物车，总金额：" + amount);
                return goodDtos;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 下单
     */
    public static Boolean commitPay(List<GoodDto> goods,Map<String, Object> capacityData,Map<String, Object> deliveryAddressDetail,Map<String, Object> storeDetail) {
        try {
            HttpRequest httpRequest = HttpUtil.createPost("https://api-sams.walmartmobile.cn/api/v1/sams/trade/settlement/commitPay");

            Map<String, String> headers = UserConfig.getHeaders();
            headers.put("track-info", "[{\"labelType\":\"push_trace\",\"attachId\":\"\"},{\"labelType\":\"systemMessage_trace\",\"attachId\":\"\"},{\"labelType\":\"apppushmsgtaskid_trace\",\"attachId\":\"\"},{\"labelType\":\"systemmsgtasksubid_trace\",\"attachId\":\"\"},{\"labelType\":\"tracking_id\",\"attachId\":\"1649869176133-01DBB03D-BC5C-4C49-896C-F05FC7688BED\"},{\"labelType\":\"tracepromotion\",\"createTime\":\"\",\"attachId\":\"\"}]");
            httpRequest.addHeaders(headers);

            Map<String, Object> request = UserConfig.getIdInfo();
            request.put("goodsList", goods);
            request.put("invoiceInfo",new HashMap<>());
            request.put("sceneCode",1074);
            request.put("isSelectShoppingNotes",true);
            request.put("cartDeliveryType",UserConfig.cartDeliveryType);
            request.put("couponList",new ArrayList<>());
            request.put("floorId",1);
            request.put("amount",context.get("amount"));
            request.put("payType",0);
            request.put("currency","CNY");
            request.put("channel","wechat");
            request.put("shortageId",1);
            request.put("orderType",0);
            request.put("remark","");
            request.put("addressId", deliveryAddressDetail.get("addressId"));
            request.put("shortageDesc","其他商品继续配送（缺货商品直接退款）");
            request.put("labelList",UserConfig.labelList);
            request.put("payMethodId","contract");

            Map<String, Object> deliveryInfoVO = new HashMap<>();
            deliveryInfoVO.put("storeDeliveryTemplateId",storeDetail.get("storeDeliveryTemplateId"));
            deliveryInfoVO.put("deliveryModeId",storeDetail.get("deliveryModeId"));
            deliveryInfoVO.put("storeType",storeDetail.get("storeType"));

            request.put("deliveryInfoVO",deliveryInfoVO);
            Map<String, Object> settleDeliveryInfo = new HashMap<>();
            settleDeliveryInfo.put("expectArrivalTime",capacityData.get("startRealTime"));
            settleDeliveryInfo.put("expectArrivalEndTime",capacityData.get("endRealTime"));
            settleDeliveryInfo.put("deliveryType",UserConfig.deliveryType);
            request.put("settleDeliveryInfo",settleDeliveryInfo);

            Map<String, Object> storeInfo = new HashMap<>();
            storeInfo.put("storeId",storeDetail.get("storeId"));
            storeInfo.put("storeType",storeDetail.get("storeType"));
            storeInfo.put("areaBlockId",storeDetail.get("areaBlockId"));
            request.put("storeInfo",storeInfo);
            httpRequest.body(JSONUtil.toJsonStr(request));
            String body = httpRequest.execute().body();
            if (body == null || body.isEmpty()){
                System.out.println("下单失败，可能触发403限流");
                return false;
            }
            JSONObject object = JSONUtil.parseObj(body);
            if (!isSuccess(object, "提交订单")) {
                return false;
            }
            context.put("success", new HashMap<>());
            context.put("end", new HashMap<>());
            for (int i = 0; i < 10; i++) {
                System.out.println("恭喜你，已成功下单 当前下单总金额：" + context.get("amount"));
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
