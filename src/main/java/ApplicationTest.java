import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抢菜测试程序
 */
public class ApplicationTest {

    public static void main(String[] args) {
        //先初始化 获得必要的参数
        Map<String, Map<String, Object>> init = Api.init();
        List<GoodDto> goodDtos = Api.getCart(init.get("storeDetail"));
        Map<String, Object> capacityData = Api.getCapacityData(init.get("storeDetail"));
        Map<String, Object> time = new HashMap<>();
        time.put("startRealTime","1650092400000");
        time.put("endRealTime","1650114000000");
        Api.commitPay(goodDtos, time, init.get("deliveryAddressDetail"), init.get("storeDetail"));
        // 此为单次执行模式  用于在非高峰期测试下单  也必须满足3个前提条件  1.有收货地址  2.购物车有商品 3.能选择配送信息
//        List<GoodDto> goodDtos = Api.getCart();
//        if (goodDtos == null || goodDtos.isEmpty()) {
//            return;
//        }
//        Map<String, Object> capacityData = Api.getCapacityData();
//        if (capacityData == null) {
//            return;
//        }
//        Api.commitPay(goodDtos,capacityData);
    }
}
