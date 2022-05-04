import lombok.Getter;
import lombok.Setter;

/**
 * 优惠券DTO
 * @author zhangyibo
 */
@Getter
@Setter
public class CouponDto {

    Integer condition;

    Integer discount;

    String ruleId;

}
