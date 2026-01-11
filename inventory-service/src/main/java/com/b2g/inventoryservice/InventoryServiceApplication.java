package com.b2g.inventoryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;

@SpringBootApplication
public class InventoryServiceApplication {



    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(InventoryServiceApplication.class, args);

        System.out.println("------ BEAN REGISTRATI ------");
        String[] beans = ctx.getBeanDefinitionNames();
        Arrays.sort(beans);
        for (String bean : beans) {
            System.out.println(bean);
        }
        System.out.println("------ FINE BEAN ------");
    }

}
