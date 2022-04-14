import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.ImmutableMap;

import java.util.*;

/**
 * 接口封装
 */
public class Api {

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
                System.out.println(actionName + "失败:" + "出现此问题有三个可能 1.偶发，无需处理 2.一个账号一天只能下两单  3.不要长时间运行程序，目前已知有人被风控了，暂时未确认风控的因素是ip还是用户或设备相关信息，如果要测试用单次执行模式，并发只能用于6点、8点半的前一分钟，然后执行时间不能超过2分钟，如果买不到就不要再执行程序了，切忌切忌，如果已经被风控的可以尝试过一段时间再试，或者换号");
            } else {
                System.out.println(actionName + "失败,服务器返回无法解析的内容:" + JSONUtil.toJsonStr(object));
            }
            return false;
        }
        if (success) {
            return true;
        }
        if ("您的访问已过期".equals(object.getStr("msg"))) {
            System.err.println("用户信息失效，请确保UserConfig参数准确，并且微信上的叮咚小程序不能退出登录");
//            Application.map.put("end", new HashMap<>());
            return false;
        }
        String msg = null;
        try {
            msg = object.getStr("msg");
            if (msg == null || "".equals(msg)) {
//                msg = object.getJSONObject("tips").getStr("limitMsg");
            }
        } catch (Exception ignored) {

        }
        System.err.println(actionName + "失败:" + (msg == null || "".equals(msg) ? "未解析返回数据内容，全字段输出:" + JSONUtil.toJsonStr(object) : msg));
        return false;
    }

    /**
     * 获取配送时间
     * @return
     */
    public static Map<String, Object> getCapacityData() {
        try {
            HttpRequest httpRequest = HttpUtil.createPost("https://api-sams.walmartmobile.cn/api/v1/sams/delivery/portal/getCapacityData");
            httpRequest.addHeaders(UserConfig.getHeaders());
            Map<String, Object> request = new HashMap<>();
            List<String> date = new ArrayList();
            for(int j = 0; j < 7; j++){
                date.add("2022-04-"+ (14+j));
            }
            request.put("perDateList", date);
            request.put("storeDeliveryTemplateId", "552578721878546198");
            request.put("uid", "181816233927");
            request.put("appId", "wxb344a8513eaaf849");
            request.put("saasId", "1818");
            String s = JSONUtil.toJsonStr(request);
            httpRequest.body(JSONUtil.toJsonStr(request));
            String body = httpRequest.execute().body();
            JSONObject object = JSONUtil.parseObj(body);
            if (!isSuccess(object, "更新配送时间")) {
                return null;
            }
            Map<String, Object> map = new HashMap<>();
            Boolean dateCondition = object.getJSONObject("data").getJSONArray("capcityResponseList").getJSONObject(0).getBool("dateISFull");
            if (dateCondition){
                System.out.println("【失败】全部配送时间已满");
            } else {
                JSONArray times = object.getJSONObject("data").getJSONArray("capcityResponseList").getJSONObject(0).getJSONArray("list");
                for (int i = 0; i < times.size(); i++) {
                    JSONObject time = times.getJSONObject(i);
                    if (!time.getBool("timeISFull")){
                        map.put("startRealTime",time.get("startRealTime"));
                        map.put("endRealTime",time.get("endRealTime"));
                        System.out.println("【成功】更新配送时间");
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
    public static List<GoodDto> getCart() {
        try {
            HttpRequest httpRequest = HttpUtil.createPost("https://api-sams.walmartmobile.cn/api/v1/sams/trade/cart/getUserCart");
            httpRequest.addHeaders(UserConfig.getHeaders());
            Map<String, Object> request = new HashMap<>();
            List<Map> storeList = new ArrayList();
            Map<String,Object> store = new HashMap<>();
            store.put("storeType",2);
            store.put("storeId","4807");
            store.put("areaBlockId","300145510512240918");
            store.put("storeDeliveryTemplateId","552578721878546198");
            storeList.add(store);

            Map<String,Object> store2 = new HashMap<>();
            store2.put("storeType",8);
            store2.put("storeId","9996");
            store2.put("areaBlockId","42295");
            store2.put("storeDeliveryTemplateId","1147161263885953814");
            storeList.add(store2);

            Map<String,Object> store3 = new HashMap<>();
            store3.put("storeType",32);
            store3.put("storeId","9991");
            store3.put("areaBlockId","42295");
            store3.put("storeDeliveryTemplateId","1010425035346829590");
            storeList.add(store3);

            request.put("storeList", storeList);
            request.put("uid", "181816233927");
            request.put("appId", "wxb344a8513eaaf849");
            request.put("saasId", "1818");
            httpRequest.body(JSONUtil.toJsonStr(request));
            String body = httpRequest.execute().body();
            JSONObject object = JSONUtil.parseObj(body);
            if (!isSuccess(object, "更新购物车")) {
                return null;
            }
            Integer selectedNumber = object.getJSONObject("data").getInt("selectedNumber");
            if (selectedNumber == 0){
                System.out.println("购物车为空");
                return null;
            } else {
//                JSONArray goods = object.getJSONObject("data").getJSONObject("miniProgramGoodsInfo").getJSONArray("normalGoodsList");
                JSONArray goods = object.getJSONObject("data").getJSONArray("floorInfoList").getJSONObject(0).getJSONArray("normalGoodsList");
                List<GoodDto> goodDtos = new ArrayList<>();
                for (int i = 0; i < goods.size(); i++) {
                    JSONObject good = goods.getJSONObject(i);
                    GoodDto goodDto = new GoodDto();
                    goodDto.setSpuId(good.getStr("spuId"));
                    goodDto.setQuantity(good.getStr("quantity"));
                    goodDto.setStoreId(good.getStr("storeId"));
                    goodDtos.add(goodDto);
                }
                System.out.println("【成功】更新购物车");
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
    public static void commitPay(List<GoodDto> goods,Map<String, Object> capacityData) {
        try {
            HttpRequest httpRequest = HttpUtil.createPost("https://api-sams.walmartmobile.cn/api/v1/sams/trade/settlement/commitPay");

            Map<String, String> headers = UserConfig.getHeaders();
            headers.put("track-info", "[{\"labelType\":\"push_trace\",\"attachId\":\"\"},{\"labelType\":\"systemMessage_trace\",\"attachId\":\"\"},{\"labelType\":\"apppushmsgtaskid_trace\",\"attachId\":\"\"},{\"labelType\":\"systemmsgtasksubid_trace\",\"attachId\":\"\"},{\"labelType\":\"tracking_id\",\"attachId\":\"1649869176133-01DBB03D-BC5C-4C49-896C-F05FC7688BED\"},{\"labelType\":\"tracepromotion\",\"createTime\":\"\",\"attachId\":\"\"}]");
            httpRequest.addHeaders(headers);

            Map<String, Object> request = new HashMap<>();
            request.put("goodsList", goods);
            request.put("invoiceInfo",new HashMap<>());
            request.put("cartDeliveryType",2);
            request.put("floorId",1);
            request.put("amount","13880");
            request.put("purchaserName","");
            Map<String, Object> settleDeliveryInfo = new HashMap<>();
            settleDeliveryInfo.put("expectArrivalTime",capacityData.get("startRealTime"));
            settleDeliveryInfo.put("expectArrivalEndTime",capacityData.get("endRealTime"));
            settleDeliveryInfo.put("deliveryType",0);
            request.put("settleDeliveryInfo",settleDeliveryInfo);
            request.put("tradeType","APP");
            request.put("purchaserId","");
            request.put("payType",0);
            request.put("currency","CNY");
            request.put("channel","wechat");
            request.put("shortageId",1);
            request.put("isSelfPickup",0);
            request.put("orderType",0);
            request.put("uid", "181816233927");
            request.put("appId", "wx57364320cb03dfba");//wx57364320cb03dfba  wxb344a8513eaaf849
            request.put("addressId", "145244035");
            Map<String, Object> deliveryInfoVO = new HashMap<>();
            deliveryInfoVO.put("storeDeliveryTemplateId","552578721878546198");
            deliveryInfoVO.put("deliveryModeId","1003");
            deliveryInfoVO.put("storeType","2");
            request.put("deliveryInfoVO",deliveryInfoVO);
            request.put("remark","");
            Map<String, Object> storeInfo = new HashMap<>();
            storeInfo.put("storeId","4807");
            storeInfo.put("storeType","2");
            storeInfo.put("areaBlockId","300145510512240918");
            request.put("storeInfo",storeInfo);
            request.put("shortageDesc","其他商品继续配送（缺货商品直接退款）");
            request.put("payMethodId","1486659732");
            String s = JSONUtil.toJsonStr(request);

            httpRequest.body(JSONUtil.toJsonStr(request));
            String body = httpRequest.execute().body();
            JSONObject object = JSONUtil.parseObj(body);
            if (!isSuccess(object, "提交订单")) {
                return;
            }
            Boolean success = object.getBool("success");
            if (success){
                System.out.println("下单成功!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
