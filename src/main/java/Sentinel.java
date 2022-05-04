import cn.hutool.core.util.RandomUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 哨兵捡漏模式 可长时间运行。
 * 用于极速达捡漏，可以在UserConfig中设置下单目标金额
 */
public class Sentinel {

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }

    public static void main(String[] args) {

        //执行任务请求间隔时间最小值
        int sleepMillisMin = 1000;
        //执行任务请求间隔时间最大值
        int sleepMillisMax = 5000;

        //单轮轮询时请求异常（服务器高峰期限流策略）尝试次数
        int loopTryCount = 10;

        Api.init("1");
        List<CouponDto> couponList = Api.getCouponList();

        while (!Api.context.containsKey("end")) {
            try {
                sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));

                for (int i = 0; i < loopTryCount && (Api.context.get("deliveryAddressDetail") == null); i++) {
                    Map<String, Object> deliveryAddressDetail = Api.getDeliveryAddressDetail();
                    sleep(RandomUtil.randomInt(500, 1000));
                    Api.context.put("deliveryAddressDetail", deliveryAddressDetail);
                }
                if (Api.context.get("deliveryAddressDetail") == null) {
                    continue;
                }

                for (int i = 0; i < loopTryCount && (Api.context.get("storeDetail") == null); i++) {
                    Map<String, Object> storeDetail = Api.getMiniUnLoginStoreList(Double.parseDouble((String) Api.context.get("latitude")), Double.parseDouble((String) Api.context.get("longitude")));
                    sleep(RandomUtil.randomInt(500, 1000));
                    Api.context.put("storeDetail", storeDetail);
                }
                if (Api.context.get("storeDetail") == null) {
                    continue;
                }

                List<GoodDto> goodDtos = null;
                for (int i = 0; i < loopTryCount && goodDtos == null; i++) {
                    goodDtos = Api.getCart((Map<String, Object>) Api.context.get("storeDetail"));
                    sleep(RandomUtil.randomInt(500, 1000));
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
                    sleep(RandomUtil.randomInt(500, 1000));
                }
                if (capacityData == null) {
                    continue;
                }

                Double totalWeight = 0.0;
                Integer flag = 0;
                for (int j = 0; j < goodDtos.size(); j++) {
                    totalWeight = totalWeight + goodDtos.get(j).getWeight() * Double.parseDouble(goodDtos.get(j).getQuantity());
                    List<GoodDto> orderGood = new ArrayList<>();
                    if (totalWeight > 30) {
                        orderGood = goodDtos.subList(flag, j - 1);
                    } else if (j == goodDtos.size() - 1) {
                        orderGood = goodDtos.subList(flag, j);
                    }
                    if (!orderGood.isEmpty()) {
                        for (int i = 0; i < loopTryCount; i++) {
                            if (Api.commitPay(orderGood, capacityData, (Map<String, Object>) Api.context.get("deliveryAddressDetail"), (Map<String, Object>) Api.context.get("storeDetail"), (List<CouponDto>) Api.context.get("couponDtoList"))) {
                                Api.play("极速达，下单成功");
                                break;
                            }
                            sleep(RandomUtil.randomInt(500, 1000));
                        }
                        totalWeight = 0.0;
                        flag = j;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
