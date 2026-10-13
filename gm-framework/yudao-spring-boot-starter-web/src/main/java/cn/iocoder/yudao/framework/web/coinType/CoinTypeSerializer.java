package cn.iocoder.yudao.framework.web.coinType;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;

@Component
public class CoinTypeSerializer extends JsonSerializer<Object> implements ContextualSerializer {

    @Autowired(required = false)
    private ExchangeRateService exchangeRateService; // 若无汇率转换服务可置空

    private String baseCurrency = "CNY";

    public CoinTypeSerializer() {}

    public CoinTypeSerializer(String baseCurrency, ExchangeRateService exchangeRateService) {
        this.baseCurrency = baseCurrency;
        this.exchangeRateService = exchangeRateService;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        String targetCurrency = CoinTypeContextHolder.getCurrency(); // 从 ThreadLocal 获取前端传入的币种

        if(ObjectUtil.isNotEmpty(targetCurrency)) {

            if (exchangeRateService != null && value instanceof BigDecimal) {
                BigDecimal converted = exchangeRateService.convert((BigDecimal) value, baseCurrency, targetCurrency);
                gen.writeNumber(converted);
                return;
            }

        }
        // 不做汇率转换，直接原样写出金额/数据
        gen.writeObject(value);
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (property != null) {
            // 读取组合注解 @CurrencyField
            CoinType annotation = property.getAnnotation(CoinType.class);
            if (annotation != null) {
                return new CoinTypeSerializer("CNY", this.exchangeRateService);
            }
        }
        return prov.findValueSerializer(property.getType(), property);
    }
}