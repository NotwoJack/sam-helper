import cn.hutool.core.date.LocalDateTimeUtil;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

/**
 * 测试程序
 */
public class ApplicationTest {

    public static void main(String[] args) {
        //先初始化 获得必要的参数
        Api.init("1");
        List<AddressDto> addressDtoList = Api.getAddress();
        AddressDto addressDto = addressDtoList.get(0);
        Map<String, Object> storeDetail = Api.getMiniUnLoginStoreList(Double.parseDouble(addressDto.getLatitude()), Double.parseDouble(addressDto.getLongitude()));
        Map<String, Object> capacityData = Api.getCapacityData(storeDetail);
        List<CouponDto> couponDtoList = Api.getCouponList();
        List<GoodDto> cart = Api.getCart(storeDetail);
//        Map<String, Object> map = new HashMap<>();
//        map.put("startRealTime", "1651539600000");
//        map.put("endRealTime", "1651545000000");
        Api.commitPay(cart, capacityData, addressDto, storeDetail, couponDtoList);

//        List<GoodDto> goodDtos = Api.getPageData(storeDetail);
//        List<GoodDto> goodDtos = new ArrayList<>();
//        GoodDto goodDto = new GoodDto();
//        goodDto.setQuantity("1");
//        goodDto.setSpuId("40233105");
//        goodDto.setStoreId("4807");
//        goodDtos.add(goodDto);
//        Api.addCartGoodsInfo(goodDtos);
//        Api.play();
    }
}
