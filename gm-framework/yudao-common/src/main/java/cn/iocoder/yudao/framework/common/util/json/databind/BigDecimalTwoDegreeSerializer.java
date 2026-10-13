package cn.iocoder.yudao.framework.common.util.json.databind;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalTwoDegreeSerializer extends JsonSerializer<Object> {

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value instanceof BigDecimal decimal) {
            // 四舍五入保留两位小数
            gen.writeNumber(decimal.setScale(2, RoundingMode.HALF_UP));
        } else if (value != null) {
            // 兜底：如果不是 BigDecimal 但进来了，按原样输出
            gen.writeObject(value);
        } else {
            gen.writeNull();
        }
    }
}