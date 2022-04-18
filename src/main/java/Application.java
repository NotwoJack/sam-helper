import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抢单 主程序
 */
public class Application {

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }

    public static void main(String[] args) {

        //此为高峰期策略 通过同时获取或更新 购物车、配送、订单确认信息再进行高并发提交订单

        //一定要注意 并发量过高会导致被风控 请合理设置线程数、等待时间和执行时间 不要长时间的执行此程序（我配置的线程数和间隔 2分钟以内）

        //基础信息执行线程数
        int baseTheadSize = 1;

        //提交订单执行线程数
        int submitOrderTheadSize = 3;

        //请求间隔时间
        int sleepMillis = 200;

        //先初始化 获得必要的参数
        Map<String, Map<String, Object>> init = Api.init();

        for (int i = 0; i < baseTheadSize; i++) {
            new Thread(() -> {
                while (!Api.context.containsKey("end")) {
                    List<GoodDto> goods = Api.getCart(init.get("storeDetail"));
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
                    Map<String, Object> time = Api.getCapacityData(init.get("storeDetail"));
                    if (time != null) {
                        Api.context.put("time", time);
                    }
                }
            }).start();
        }

//        Map<String, Object> time = new HashMap<>();
//        time.put("startRealTime","1650351600000");
//        time.put("endRealTime","1650373200000");
//        Api.context.put("time", time);

        for (int i = 0; i < submitOrderTheadSize; i++) {
            new Thread(() -> {
                while (!Api.context.containsKey("end")) {
                    if (Api.context.get("goods") == null || Api.context.get("time") == null) {
                        sleep(sleepMillis);
                        continue;
                    }
                    if (Api.commitPay((List<GoodDto>) Api.context.get("goods"), (Map<String, Object>) Api.context.get("time"), init.get("deliveryAddressDetail"), init.get("storeDetail"))){
                        System.out.println("铃声持续1分钟，终止程序即可，如果还需要下单再继续运行程序");
                        Api.play();
                    }
                }
            }).start();
        }
    }
}
