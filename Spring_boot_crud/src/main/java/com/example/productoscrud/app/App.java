package com.example.productoscrud.app;

import com.example.productoscrud.application.ports.ProductRepository;
import com.example.productoscrud.application.service.ProductService;
import com.example.productoscrud.application.ports.MongoProductQueryPort;
import com.example.productoscrud.application.service.MongoProductQueryService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Qualifier;

@SpringBootApplication(scanBasePackages = "com.example.productoscrud")
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean("sqlProductService")
    public ProductService sqlProductService(
        @Qualifier("mysqlProductRepository") ProductRepository productRepository
    ) {
        return new ProductService(productRepository);
    }

    @Bean("mongoProductService")
    public ProductService mongoProductService(
        @Qualifier("mongoProductRepository") ProductRepository productRepository
    ) {
        return new ProductService(productRepository);
    }

    @Bean
    public MongoProductQueryService mongoProductQueryService(MongoProductQueryPort productRepository) {
        return new MongoProductQueryService(productRepository);
    }
}
