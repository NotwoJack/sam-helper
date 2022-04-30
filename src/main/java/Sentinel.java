import cn.hutool.core.util.RandomUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 哨兵捡漏模式 可长时间运行。
 * 用于极速达捡漏，可以在UserConfig中设置下单目标金额
 */
public class  Sentinel {

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }

    public static void main(String[] args) {

        //执行任务请求间隔时间最小值
        int sleepMillisMin = 5000;
        //执行任务请求间隔时间最大值
        int sleepMillisMax = 10000;

        //单轮轮询时请求异常（服务器高峰期限流策略）尝试次数
        int loopTryCount = 8;

        //60次以后长时间等待10分钟左右
        int longWaitCount = 0;

        Api.init("1");

        boolean first = true;
        while (!Api.context.containsKey("end")) {
            try {
                if (first) {
                    first = false;
                } else {
                    if (longWaitCount++ > 60) {
                        longWaitCount = 0;
                        sleep(RandomUtil.randomInt(50000, 70000));
                    } else {
                        sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));
                    }
                }

                for (int i = 0; i < loopTryCount && (Api.context.get("deliveryAddressDetail") == null); i++) {
                    Map<String, Object> deliveryAddressDetail = Api.getDeliveryAddressDetail();
                    if (deliveryAddressDetail == null) {
                        sleep(RandomUtil.randomInt(500, 1000));
                        continue;
                    }
                    Api.context.put("latitude", deliveryAddressDetail.get("latitude"));
                    Api.context.put("longitude", deliveryAddressDetail.get("longitude"));
                    Api.context.put("deliveryAddressDetail", deliveryAddressDetail);
                }
                if (Api.context.get("deliveryAddressDetail") == null) {
                    continue;
                }

                for (int i = 0; i < loopTryCount && (Api.context.get("storeDetail") == null); i++) {
                    Map<String, Object> storeDetail = Api.getMiniUnLoginStoreList(Double.parseDouble((String) Api.context.get("latitude")), Double.parseDouble((String) Api.context.get("longitude")));
                    if (storeDetail == null) {
                        sleep(RandomUtil.randomInt(500, 1000));
                        continue;
                    }
                    Api.context.put("storeDetail", storeDetail);
                }
                if (Api.context.get("storeDetail") == null) {
                    continue;
                }

                List<GoodDto> goodDtos = null;
                for (int i = 0; i < loopTryCount && goodDtos == null; i++) {
                    goodDtos = Api.getCart((Map<String, Object>) Api.context.get("storeDetail"));
                    if (goodDtos == null) {
                        sleep(RandomUtil.randomInt(500, 1000));
                    }
                }
                if (goodDtos == null) {
                    continue;
                }
                if ((Double) Api.context.get("amount") < UserConfig.targetAmount) {
                    Api.print(false, "【失败】购物车未达到目标金额");
                    continue;
                }

                Map<String, Object> capacityData = null;
                for (int i = 0; i < loopTryCount && capacityData == null; i++) {
                    capacityData = Api.getCapacityData((Map<String, Object>) Api.context.get("storeDetail"));
                    if (capacityData == null) {
                        sleep(RandomUtil.randomInt(500, 1000));
                    }
                }
                if (capacityData == null) {
                    continue;
                }

                for (int i = 0; i < 50; i++) {
                    if (Api.commitPay(goodDtos, capacityData, (Map<String, Object>) Api.context.get("deliveryAddressDetail"), (Map<String, Object>) Api.context.get("storeDetail"))) {
                        Api.play("下单成功");
                        break;
                    }
                    sleep(RandomUtil.randomInt(50, 100));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
