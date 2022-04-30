import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试程序
 */
public class ApplicationTest {

    public static void main(String[] args) {
        //先初始化 获得必要的参数
        Api.init(UserConfig.deliveryType);
        Map<String, Object> deliveryAddressDetail = Api.getDeliveryAddressDetail();
        Map<String, Object> storeDetail = Api.getMiniUnLoginStoreList(Double.parseDouble((String) deliveryAddressDetail.get("latitude")), Double.parseDouble((String) deliveryAddressDetail.get("longitude")));
        List<GoodDto> goodDtos = Api.getPageData(storeDetail);
//        Api.barkNotice(UserConfig.barkId);
//        Api.getCapacityData(init.get("storeDetail"));
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
