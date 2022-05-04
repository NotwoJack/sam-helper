import cn.hutool.core.util.RandomUtil;

import java.util.*;

/**
 * 保供套餐抢购模式 可长时间运行
 */
public class GuaranteeSentinel {

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
        int sleepMillisMax = 1500;

        //单轮轮询时请求异常（服务器高峰期限流策略）尝试次数
        int loopTryCount = 10;

        Api.init("2");
        Map<String, Object> deliveryAddressDetail = Api.getDeliveryAddressDetail();
        sleep(RandomUtil.randomInt(300, 500));
        Map<String, Object> storeDetail = Api.getMiniUnLoginStoreList(Double.parseDouble((String) Api.context.get("latitude")), Double.parseDouble((String) Api.context.get("longitude")));
        sleep(RandomUtil.randomInt(300, 500));
        Map<String, Object> capacityData = Api.getCapacityData(storeDetail);
        sleep(RandomUtil.randomInt(300, 500));
        List<CouponDto> couponList = Api.getCouponList();
        sleep(RandomUtil.randomInt(300, 500));

        List<GoodDto> saveGoodList = new ArrayList<>();
        while (!Api.context.containsKey("end")) {
            try {
                sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));

                List<GoodDto> goodDtos = null;
                for (int i = 0; i < loopTryCount && goodDtos == null; i++) {
                    goodDtos = Api.getPageData(storeDetail);
                    sleep(RandomUtil.randomInt(1000, 1500));
                }
                if (goodDtos == null) {
                    continue;
                } else if (saveGoodList.containsAll(goodDtos)) {
                    System.out.println("全部商品都已经下单");
                    continue;
                }

                Boolean addFlag = null;
                goodDtos.removeAll(saveGoodList);
                if (!goodDtos.isEmpty()) {
                    for (int i = 0; i < loopTryCount && addFlag == null; i++) {
                        addFlag = Api.addCartGoodsInfo(goodDtos);
                        sleep(RandomUtil.randomInt(1000, 1500));
                    }
                }
                if (addFlag == null) {
                    continue;
                }

                goodDtos.forEach(goodDto -> {
                    for (int i = 0; i < loopTryCount; i++) {
                        if (Api.commitPay(Arrays.asList(goodDto), capacityData, deliveryAddressDetail, storeDetail, (List<CouponDto>) Api.context.get("couponDtoList"))) {
                            Api.play("保供套餐，下单成功");
                            saveGoodList.add(goodDto);
                            break;
                        }
                        sleep(RandomUtil.randomInt(500, 1000));
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
