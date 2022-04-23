import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.SneakyThrows;

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 接口封装
 */
public class Api {

    public static final Map<String, Object> context = new ConcurrentHashMap<>();

    /**
     * 获取用户初始化信息，收货地址信息和匹配商店信息。为app上设定的默认值
     *
     * @return 信息集合
     */
    public static Map<String, Map<String, Object>> init() {
        try {
            Map<String, Map<String, Object>> map = new HashMap<>();
            Map<String, Object> deliveryAddressDetail = getDeliveryAddressDetail();
            Map<String, Object> storeDetail = getMiniUnLoginStoreList(Double.parseDouble((String) deliveryAddressDetail.get("latitude")), Double.parseDouble((String) deliveryAddressDetail.get("longitude")));
            map.put("deliveryAddressDetail", deliveryAddressDetail);
            map.put("storeDetail", storeDetail);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SneakyThrows
    public static void play() {
        // bark推送
        if (!UserConfig.barkId.isEmpty()) {
            barkNotice(UserConfig.barkId);
        }

        // Server 酱推送
        if (!UserConfig.ftqqSendKey.isEmpty()) {
            ftqqNotice(UserConfig.ftqqSendKey);
        }

        //这里还可以使用企业微信或者钉钉的提供的webhook  自己写代码 很简单 就是按对应数据格式发一个请求到企业微信或者钉钉
        AudioClip audioClip = Applet.newAudioClip(new File("ding-dong.wav").toURL());
        audioClip.loop();
        Thread.sleep(60000);//响铃60秒
    }

    private static void print(boolean normal, String message) {
        if (Api.context.containsKey("end")) {
            return;
        }
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        if (normal) {
            System.out.println(time + message);
        } else {
            System.err.println(time + message);
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
            print(false, "【失败】" + actionName + " 服务器返回无法解析的内容:" + JSONUtil.toJsonStr(object));
            return false;
        }
        if (success) {
            return true;
        }
        print(false, "【失败】" + actionName + " 原因:" + object.get("msg"));
        return false;
    }

    /**
     * 获取默认的下单地址信息
     *
     * @return 地址信息Map
     */
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

    /**
     * 获取匹配的商店信息
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 商店信息Map
     */
    public static Map<String, Object> getMiniUnLoginStoreList(Double latitude, Double longitude) {
        try {
            HttpRequest httpRequest = HttpUtil.createPost("https://api-sams.walmartmobile.cn/api/v1/sams/merchant/storeApi/getMiniUnLoginStoreList");
            httpRequest.addHeaders(UserConfig.getHeaders());
            Map<String, Object> request = UserConfig.getIdInfo();
            request.put("latitude", latitude);
            request.put("longitude", longitude);
            request.put("requestType", "location_recmd");

            httpRequest.body(JSONUtil.toJsonStr(request));
            String body = httpRequest.execute().body();
            JSONObject object = JSONUtil.parseObj(body);
            if (!isSuccess(object, "获取商店信息")) {
                return null;
            }
            Map<String, Object> map = new HashMap<>();
            JSONArray storeList = object.getJSONObject("data").getJSONArray("storeList");
            Iterator<Object> iterator = storeList.iterator();
            while (iterator.hasNext()) {
                JSONObject store = (JSONObject) iterator.next();
                if (store.getInt("storeType") == 2) {
                    map.put("storeType", store.getStr("storeType"));
                    map.put("storeId", store.getStr("storeId"));
                    map.put("storeDeliveryTemplateId", store.getJSONObject("storeRecmdDeliveryTemplateData").getStr("storeDeliveryTemplateId"));
                    map.put("areaBlockId", store.getJSONObject("storeAreaBlockVerifyData").getStr("areaBlockId"));
                    map.put("deliveryModeId", store.getJSONObject("storeDeliveryModeVerifyData").getStr("deliveryModeId"));
                    map.put("storeName", store.getStr("storeName"));
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
     *
     * @param storeDetail 商店信息
     * @return 配送信息Map
     */
    public static Map<String, Object> getCapacityData(Map<String, Object> storeDetail) {
        try {
            HttpRequest httpRequest = HttpUtil.createPost("https://api-sams.walmartmobile.cn/api/v1/sams/delivery/portal/getCapacityData");
            httpRequest.addHeaders(UserConfig.getHeaders());
            Map<String, Object> request = UserConfig.getIdInfo();

            List<String> date = new ArrayList<>();
            DateTime dateTime = new DateTime();
            for (int j = 0; j < 7; j++) {
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
            JSONArray capcityResponseList = object.getJSONObject("data").getJSONArray("capcityResponseList");
            for (int i = 0; i < capcityResponseList.size(); i++){
                JSONObject capcityResponse = capcityResponseList.getJSONObject(i);
                if (!capcityResponse.getBool("dateISFull")){
                    JSONArray times = capcityResponse.getJSONArray("list");
                    for (int j = 0; j < times.size(); j++) {
                        JSONObject time = times.getJSONObject(j);
                        if (!time.getBool("timeISFull")) {
                            map.put("startRealTime", time.get("startRealTime"));
                            map.put("endRealTime", time.get("endRealTime"));
                            print(true, "【成功】更新配送时间:" + time.getStr("startTime") + " -- " + time.getStr("endTime"));
                            return map;
                        }
                    }
                }
            }
            print(false, "【失败】全部配送时间已满");
        } catch (JSONException e) {
            print(false, "【失败】并发过高被风控，请调整参数");
            e.printStackTrace();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取预估的配送时间
     *
     * @param storeDetail 商店信息
     * @return 配送信息Map
     */
    public static Map<String, Object> getGuessData(Map<String, Object> storeDetail) {
        try {
            HttpRequest httpRequest = HttpUtil.createPost("https://api-sams.walmartmobile.cn/api/v1/sams/delivery/portal/getCapacityData");
            httpRequest.addHeaders(UserConfig.getHeaders());
            Map<String, Object> request = UserConfig.getIdInfo();

            List<String> date = new ArrayList<>();
            DateTime dateTime = new DateTime();
            for (int j = 0; j < 7; j++) {
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
            JSONArray times = object.getJSONObject("data").getJSONArray("capcityResponseList").getJSONObject(0).getJSONArray("list");
            if (times.size() > 1) {
                //直接拿下一个时间
                JSONObject time = times.getJSONObject(1);
                map.put("startRealTime", time.get("startRealTime"));
                map.put("endRealTime", time.get("endRealTime"));
            } else {
                //计算明天的时间
                JSONObject time = times.getJSONObject(0);
                map.put("startRealTime", new BigDecimal(time.getStr("startRealTime")).add(new BigDecimal(86400000)).toString());
                map.put("endRealTime", new BigDecimal(time.getStr("endRealTime")).add(new BigDecimal(86400000)).toString());
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取购物车信息
     *
     * @param storeDetail 商店信息
     * @return 购物车商品列表
     */
    public static List<GoodDto> getCart(Map<String, Object> storeDetail) {
        try {
            HttpRequest httpRequest = HttpUtil.createPost("https://api-sams.walmartmobile.cn/api/v1/sams/trade/cart/getUserCart");
            httpRequest.addHeaders(UserConfig.getHeaders());
            Map<String, Object> request = UserConfig.getIdInfo();

            List<Map> storeList = new ArrayList();
            Map<String, Object> store = new HashMap<>();
            store.put("storeType", storeDetail.get("storeType"));
            store.put("storeId", storeDetail.get("storeId"));
            store.put("areaBlockId", storeDetail.get("areaBlockId"));
            store.put("storeDeliveryTemplateId", storeDetail.get("storeDeliveryTemplateId"));
            storeList.add(store);
            request.put("storeList", storeList);

            httpRequest.body(JSONUtil.toJsonStr(request));
            String body = httpRequest.execute().body();
            JSONObject object = JSONUtil.parseObj(body);
            if (!isSuccess(object, "更新购物车")) {
                return null;
            }
            Integer selectedNumber = object.getJSONObject("data").getInt("selectedNumber");
            if (selectedNumber == 0) {
                print(false, "购物车为空");
                return null;
            } else {
                double amount = object.getJSONObject("data").getDouble("selectedAmount") / 100;
                JSONArray goods = object.getJSONObject("data").getJSONObject("miniProgramGoodsInfo").getJSONArray("normalGoodsList");
                List<GoodDto> goodDtos = new ArrayList<>();
                for (int i = 0; i < goods.size(); i++) {
                    JSONObject good = goods.getJSONObject(i);
                    if (good.getBool("isSelected")) {
                        GoodDto goodDto = new GoodDto();
                        goodDto.setSpuId(good.getStr("spuId"));
                        goodDto.setQuantity(good.getStr("quantity"));
                        goodDto.setStoreId(good.getStr("storeId"));
                        goodDtos.add(goodDto);
                    }
                }
                context.put("amount", amount);
                print(true, "【成功】更新购物车，总金额：" + amount + "元");
                return goodDtos;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void barkNotice(String barkId) {
        // sound=minuet 这里可在bark app选择自己喜爱的铃声
        HttpRequest httpRequest = HttpUtil.createGet("https://api.day.app/" + barkId + "/抢购成功，请及时付款?sound=minuet");
        String body = httpRequest.execute().body();
        System.out.println(body);
    }

    public static void ftqqNotice(String sendKey) {
        HttpRequest httpRequest = HttpUtil.createPost("https://sctapi.ftqq.com/" + sendKey + ".send?title=【山姆sam-helper】提醒&desp=抢购成功，请及时付款！");
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        httpRequest.addHeaders(headers);

        String body = httpRequest.execute().body();
        System.out.println(body);
    }

    /**
     * 提交订单
     *
     * @param goods                 商品信息
     * @param capacityData          配送信息
     * @param deliveryAddressDetail 配送地址信息
     * @param storeDetail           商店信息
     * @return 下单成功与否
     */
    public static Boolean commitPay(List<GoodDto> goods, Map<String, Object> capacityData, Map<String, Object> deliveryAddressDetail, Map<String, Object> storeDetail) {
        try {
            HttpRequest httpRequest = HttpUtil.createPost("https://api-sams.walmartmobile.cn/api/v1/sams/trade/settlement/commitPay");

            Map<String, String> headers = UserConfig.getHeaders();
            headers.put("track-info", "[{\"labelType\":\"push_trace\",\"attachId\":\"\"},{\"labelType\":\"systemMessage_trace\",\"attachId\":\"\"},{\"labelType\":\"apppushmsgtaskid_trace\",\"attachId\":\"\"},{\"labelType\":\"systemmsgtasksubid_trace\",\"attachId\":\"\"},{\"labelType\":\"tracking_id\",\"attachId\":\"1649869176133-01DBB03D-BC5C-4C49-896C-F05FC7688BED\"},{\"labelType\":\"tracepromotion\",\"createTime\":\"\",\"attachId\":\"\"}]");
            httpRequest.addHeaders(headers);

            Map<String, Object> request = UserConfig.getIdInfo();
            request.put("goodsList", goods);
            request.put("invoiceInfo", new HashMap<>());
            request.put("sceneCode", 1074);
            request.put("isSelectShoppingNotes", true);
            request.put("cartDeliveryType", UserConfig.cartDeliveryType);
            request.put("couponList", new ArrayList<>());
            request.put("floorId", 1);
            request.put("amount", context.get("amount"));
            request.put("payType", 0);
            request.put("currency", "CNY");
            request.put("channel", "wechat");
            request.put("shortageId", 1);
            request.put("orderType", 0);
            request.put("remark", "");
            request.put("addressId", deliveryAddressDetail.get("addressId"));
            request.put("shortageDesc", "其他商品继续配送（缺货商品直接退款）");
            request.put("labelList", UserConfig.labelList);
            request.put("payMethodId", "contract");

            Map<String, Object> deliveryInfoVO = new HashMap<>();
            deliveryInfoVO.put("storeDeliveryTemplateId", storeDetail.get("storeDeliveryTemplateId"));
            deliveryInfoVO.put("deliveryModeId", storeDetail.get("deliveryModeId"));
            deliveryInfoVO.put("storeType", storeDetail.get("storeType"));

            request.put("deliveryInfoVO", deliveryInfoVO);
            Map<String, Object> settleDeliveryInfo = new HashMap<>();
            settleDeliveryInfo.put("expectArrivalTime", capacityData.get("startRealTime"));
            settleDeliveryInfo.put("expectArrivalEndTime", capacityData.get("endRealTime"));
            settleDeliveryInfo.put("deliveryType", UserConfig.deliveryType);
            request.put("settleDeliveryInfo", settleDeliveryInfo);

            Map<String, Object> storeInfo = new HashMap<>();
            storeInfo.put("storeId", storeDetail.get("storeId"));
            storeInfo.put("storeType", storeDetail.get("storeType"));
            storeInfo.put("areaBlockId", storeDetail.get("areaBlockId"));
            request.put("storeInfo", storeInfo);
            httpRequest.body(JSONUtil.toJsonStr(request));
            String body = httpRequest.execute().body();
            if (body == null || body.isEmpty()) {
                print(false, "下单失败，可能触发403限流");
                return false;
            }
            JSONObject object = JSONUtil.parseObj(body);
            if (!isSuccess(object, "提交订单")) {
                return false;
            }
            context.put("success", new HashMap<>());
//            context.put("end", new HashMap<>());
            for (int i = 0; i < 10; i++) {
                print(true, "恭喜你，已成功下单 当前下单总金额：" + context.get("amount") + "元");
            }
            return true;
        } catch (JSONException e) {
            print(false, "【失败】并发过高被风控，请调整参数");
            e.printStackTrace();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取保供套餐信息
     *
     * @param storeDetail 商店信息
     * @return 购物车商品列表
     */
    public static List<GoodDto> getGoodsListByCategoryId(Map<String, Object> storeDetail) {
        try {
            HttpRequest httpRequest = HttpUtil.createPost("https://api-sams.walmartmobile.cn/api/v1/sams/goods-portal/grouping/list");
            httpRequest.addHeaders(UserConfig.getHeaders());
            Map<String, Object> request = UserConfig.getIdInfo();

            request.put("pageSize", 20);
            request.put("isReversOrder", false);
            request.put("useNewPage", true);
            request.put("frontCategoryIds", Arrays.asList("10012335", "10012336"));
            request.put("isFastDelivery", false);
            request.put("addressVO", new HashMap<>());
            request.put("secondCategoryId", "10012335");
            request.put("pageNum", 1);

            List<Map> storeList = new ArrayList();
            Map<String, Object> store = new HashMap<>();
            store.put("storeType", storeDetail.get("storeType"));
            store.put("storeId", storeDetail.get("storeId"));
            storeList.add(store);
            request.put("storeInfoVOList", storeList);

            httpRequest.body(JSONUtil.toJsonStr(request));
            String body = httpRequest.execute().body();
            JSONObject object = JSONUtil.parseObj(body);
            if (!isSuccess(object, "获取保供套餐列表")) {
                return null;
            }
            JSONArray goods = object.getJSONObject("data").getJSONArray("dataList");
            List<GoodDto> goodDtos = new ArrayList<>();
            for (int i = 0; i < goods.size(); i++) {
                JSONObject good = goods.getJSONObject(i);
                if (good.getJSONObject("stockInfo").getInt("stockQuantity") > 0) {
                    GoodDto goodDto = new GoodDto();
                    goodDto.setSpuId(good.getStr("spuId"));
                    goodDto.setQuantity("1");
                    goodDto.setStoreId(good.getStr("storeId"));
                    goodDtos.add(goodDto);
                    System.out.println(good.getStr("title") + "----" +good.getStr("subTitle"));
                }
            }
            if (goodDtos.isEmpty()){
                print(false, "【失败】暂未获取到保供套餐");
                return null;
            }
            print(true, "【成功】获取到保供套餐");
            context.put("amount", 100);
            return goodDtos;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将商品添加到购物车
     *
     * @param goodDtos 商品信息
     * @return
     */
    public static Boolean addCartGoodsInfo(List<GoodDto> goodDtos) {
        try {
            HttpRequest httpRequest = HttpUtil.createPost("https://api-sams.walmartmobile.cn/api/v1/sams/trade/cart/addCartGoodsInfo");
            httpRequest.addHeaders(UserConfig.getHeaders());
            Map<String, Object> request = UserConfig.getIdInfo();

            List<Map> cartGoodsInfoList = new ArrayList();
            goodDtos.forEach(goodDto -> {
                Map<String, Object> cartGoodsInfo = new HashMap<>();
                cartGoodsInfo.put("componentPath", "1");
                cartGoodsInfo.put("goodsName", "1");
                cartGoodsInfo.put("price", "1");
                cartGoodsInfo.put("event_tracking_id", "sam_app_cart_category_buy");
                cartGoodsInfo.put("increaseQuantity", goodDto.getQuantity());
                cartGoodsInfo.put("storeId", goodDto.getStoreId());
                cartGoodsInfo.put("spuId",goodDto.getSpuId());
                cartGoodsInfoList.add(cartGoodsInfo);
            });
            request.put("cartGoodsInfoList", cartGoodsInfoList);

            httpRequest.body(JSONUtil.toJsonStr(request));
            String body = httpRequest.execute().body();
            JSONObject object = JSONUtil.parseObj(body);
            if (!isSuccess(object, "添加商品至购物车")) {
                return false;
            }
            print(true, "【成功】添加至购物车");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
