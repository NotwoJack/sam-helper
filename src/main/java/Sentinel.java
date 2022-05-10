import cn.hutool.core.util.RandomUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 哨兵捡漏模式 可长时间运行。
 * 可以在UserConfig中设置下单类型和目标金额
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
        int sleepMillisMin = 500;
        //执行任务请求间隔时间最大值
        int sleepMillisMax = 1000;

        //单轮轮询时请求异常（服务器高峰期限流策略）尝试次数
        int loopTryCount = 10;

        //7点55分00秒时间触发 极速达
        while (UserConfig.deliveryType.equals("1") && !Api.timeTrigger(7, 55, 00)) {
        }
        //13点59分00秒时间触发 全城送
        while (UserConfig.deliveryType.equals("2") && !Api.timeTrigger(13, 59, 30)) {
        }

        Api.init(UserConfig.deliveryType);
        List<CouponDto> couponList = Api.getCouponList();

        Map<String, Object> deliveryAddressDetail = null;
        while (deliveryAddressDetail == null){
            deliveryAddressDetail = Api.getDeliveryAddressDetail();
            sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));
        }
        Map<String, Object> storeDetail = null;
        while (storeDetail == null){
            storeDetail = Api.getMiniUnLoginStoreList(Double.parseDouble((String) Api.context.get("latitude")), Double.parseDouble((String) Api.context.get("longitude")));
            sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));
        }

        while (true) {
            try {
                sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));

                List<GoodDto> goodDtos = null;
                for (int i = 0; i < loopTryCount && goodDtos == null; i++) {
                    goodDtos = Api.getCart(storeDetail);
                    sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));
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
                    capacityData = Api.getCapacityData(storeDetail);
                    sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));
                }
                if (capacityData == null) {
                    continue;
                }

                //极速达超重 拆单处理
                List<List<GoodDto>> orderGoodList = new ArrayList<>();
                if ("1".equals(Api.context.get("deliveryType"))){
                    Double totalWeight = 0.0;
                    Integer flag = 0;
                    for (int j = 0; j < goodDtos.size(); j++) {
                        totalWeight = totalWeight + goodDtos.get(j).getWeight() * Double.parseDouble(goodDtos.get(j).getQuantity());
                        List<GoodDto> orderGood;
                        if (totalWeight > 30) {
                            orderGood = goodDtos.subList(flag, j);
                            orderGoodList.add(orderGood);
                            totalWeight = 0.0;
                            flag = j;
                        } else if (j == goodDtos.size() - 1) {
                            orderGood = goodDtos.subList(flag, j + 1);
                            orderGoodList.add(orderGood);
                        }
                    }
                } else if ("2".equals(Api.context.get("deliveryType"))) {
                    orderGoodList.add(goodDtos);
                }

                for (List<GoodDto> orderGood : orderGoodList) {
                    for (int i = 0; i < loopTryCount; i++) {
                        if (Api.commitPay(orderGood, capacityData, deliveryAddressDetail, storeDetail, (List<CouponDto>) Api.context.get("couponDtoList"))) {
                            if ("1".equals(Api.context.get("deliveryType"))){
                                Api.play("极速达，下单成功,下单金额：" + Api.context.get("amount"));
                            } else if ("2".equals(Api.context.get("deliveryType"))) {
                                Api.play("全城配，下单成功,下单金额：" + Api.context.get("amount"));
                            }
                            break;
                        }
                        sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
