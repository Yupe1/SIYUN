package com.yupe.siyun;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.yupe.siyun.mapper")
@SpringBootApplication
public class SiyunApplication {

    public static void main(String[] args) {
        SpringApplication.run(SiyunApplication.class, args);
    }

}
