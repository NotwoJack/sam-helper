import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 高峰抢单主程序
 */
public class Application {

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }

    public static void main(String[] args) {

        //此为高峰期策略 通过同时获取或更新 购物车、配送时间信息再进行高并发提交订单
        //使用前先添加商品至购物车，并设置好默认地址。抢到订单后，去app端付款即可

        //一定要注意 使用该程序不要超过2分钟。并发量过高会导致被风控 请合理设置线程数、等待时间和执行时间

        //基础信息执行线程数
        int baseTheadSize = 1;

        //提交订单执行线程数
        int submitOrderTheadSize = 3;

        //请求间隔时间
        int sleepMillis = 200;

        //先初始化 获得必要的参数
        Api.init(UserConfig.deliveryType);
        Map<String, Object> deliveryAddressDetail = Api.getDeliveryAddressDetail();
        Map<String, Object> storeDetail = Api.getMiniUnLoginStoreList(Double.parseDouble((String) deliveryAddressDetail.get("latitude")), Double.parseDouble((String) deliveryAddressDetail.get("longitude")));

        for (int i = 0; i < baseTheadSize; i++) {
            new Thread(() -> {
                while (!Api.context.containsKey("end")) {
                    List<GoodDto> goods = Api.getCart(storeDetail);
                    if (goods != null) {
                        Api.context.put("goods", goods);
                    }
                    sleep(sleepMillis);
                }
            }).start();
        }
        for (int i = 0; i < submitOrderTheadSize; i++) {
            new Thread(() -> {
                while (!Api.context.containsKey("end")) {
                    sleep(sleepMillis);
                    if (Api.context.get("goods") == null) {
                        continue;
                    }
                    Map<String, Object> time = Api.getCapacityData(storeDetail);
                    if (time != null) {
                        Api.context.put("time", time);
                    }
                }
            }).start();
        }

        for (int i = 0; i < submitOrderTheadSize; i++) {
            new Thread(() -> {
                while (!Api.context.containsKey("end")) {
                    if (Api.context.get("goods") == null || Api.context.get("time") == null) {
                        sleep(sleepMillis);
                        continue;
                    }
                    if (Api.commitPay((List<GoodDto>) Api.context.get("goods"), (Map<String, Object>) Api.context.get("time"), deliveryAddressDetail, storeDetail)){
                        System.out.println("铃声持续1分钟，终止程序即可，如果还需要下单再继续运行程序");
                        Api.context.put("end", new HashMap<>());
                        Api.play();
                    }
                }
            }).start();
        }
    }
}
