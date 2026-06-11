package cn.iocoder.yudao.report;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;


@SpringBootApplication(scanBasePackages = "cn.iocoder.yudao.**")
@EnableRetry
public class GMReportApp {

    public static void main(String[] args) {

        SpringApplication.run(GMReportApp.class, args);

    }

}
