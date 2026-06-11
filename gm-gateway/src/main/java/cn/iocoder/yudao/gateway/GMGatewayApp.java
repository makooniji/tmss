package cn.iocoder.yudao.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;


@SpringBootApplication(scanBasePackages = "cn.iocoder.yudao.**")
@EnableRetry
public class GMGatewayApp {

    public static void main(String[] args) {

        SpringApplication.run(GMGatewayApp.class, args);

    }

}
