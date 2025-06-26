package org.mifos.loanrisk;

import java.util.Arrays;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.integration.config.EnableIntegration;

@SpringBootApplication
@EnableIntegration
public class LoanRiskMicroserviceApplication {

    public static void main(String[] args) {

        ConfigurableApplicationContext context = SpringApplication.run(LoanRiskMicroserviceApplication.class, args);
        Arrays.stream(context.getBeanDefinitionNames()).forEach(System.out::println);
    }

}
