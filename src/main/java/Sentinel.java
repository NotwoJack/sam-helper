import cn.hutool.core.util.RandomUtil;

import java.time.LocalDateTime;
import java.util.*;

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

        Api.init(UserConfig.deliveryType);
        AddressDto addressDto = null;
        while (addressDto == null) {
            List<AddressDto> addressDtoList = Api.getAddress();
            if (addressDtoList != null) {
                System.out.println("请输入收货地址序号，并回车");
                Scanner scanner = new Scanner(System.in);
                addressDto = addressDtoList.get(scanner.nextInt());
            }
            sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));
        }

        //7点58分00秒时间触发 极速达
        while ("1".equals(UserConfig.deliveryType) && !Api.timeTrigger("07:58:00")) {
        }
        //13点59分00秒时间触发 全城送
        while ("2".equals(UserConfig.deliveryType) && !Api.timeTrigger("13:59:00")) {
        }
        //11点25分00秒时间触发 保供套餐
        while ("3".equals(UserConfig.deliveryType) && !Api.timeTrigger("11:25:00")) {
        }

        List<CouponDto> couponList = null;
        while (couponList == null) {
            couponList = Api.getCouponList();
            sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));
        }

        Map<String, Object> storeDetail = null;
        while (storeDetail == null) {
            storeDetail = Api.getMiniUnLoginStoreList(Double.parseDouble(addressDto.getLatitude()), Double.parseDouble(addressDto.getLongitude()));
            sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));
        }

        Map<String, Object> capacityData = null;
        while (capacityData == null) {
            capacityData = Api.getCapacityData(storeDetail);
            sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));
        }

        while (true) {
            try {
                sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));

                List<GoodDto> goodDtos = null;
                if ("1".equals(UserConfig.deliveryType) || "2".equals(UserConfig.deliveryType)) {
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
                } else if ("3".equals(UserConfig.deliveryType)) {
                    for (int i = 0; i < loopTryCount && goodDtos == null; i++) {
                        goodDtos = Api.getPageData(storeDetail);
                        sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));
                    }
                    if (goodDtos == null) {
                        continue;
                    } else if (Api.limitGood.containsAll(goodDtos)) {
                        System.out.println("全部商品都已经下单");
                        continue;
                    }

                    Boolean addFlag = null;
                    goodDtos.removeAll(Api.limitGood);
                    if (!goodDtos.isEmpty()) {
                        for (int i = 0; i < loopTryCount && addFlag == null; i++) {
                            addFlag = Api.addCartGoodsInfo(goodDtos);
                            sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));
                        }
                    }
                    if (addFlag == null) {
                        continue;
                    }
                } else {
                    continue;
                }

//                Map<String, Object> capacityData = null;
//                for (int i = 0; i < loopTryCount && capacityData == null; i++) {
//                    capacityData = Api.getCapacityData(storeDetail);
//                    sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));
//                }
//                if (capacityData == null) {
//                    continue;
//                }

                //极速达超重 拆单处理
                List<List<GoodDto>> orderGoodList = new ArrayList<>();
                if ("1".equals(Api.context.get("deliveryType"))) {
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
                } else if ("3".equals(Api.context.get("deliveryType"))) {
                    goodDtos.forEach(goodDto -> orderGoodList.add(Arrays.asList(goodDto)));
                }

                for (List<GoodDto> orderGood : orderGoodList) {
                    for (int i = 0; i < loopTryCount; i++) {
                        if (Api.commitPay(orderGood, capacityData, addressDto, storeDetail, couponList)) {
                            if ("1".equals(Api.context.get("deliveryType"))) {
                                Api.play("极速达，下单成功,下单金额：" + Api.context.get("amount"));
                            } else if ("2".equals(Api.context.get("deliveryType"))) {
                                Api.play("全城配，下单成功,下单金额：" + Api.context.get("amount"));
                            } else if ("3".equals(Api.context.get("deliveryType"))) {
                                Api.play("保供套餐，下单成功,下单金额：" + Api.context.get("amount"));
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
