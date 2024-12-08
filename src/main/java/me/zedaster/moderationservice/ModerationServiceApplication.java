package me.zedaster.moderationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("me.zedaster.moderationservice.configuration")
public class ModerationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModerationServiceApplication.class, args);
    }

}
