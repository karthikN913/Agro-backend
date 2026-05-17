package com.agrosystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AgroSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgroSystemApplication.class, args);
    }

    @org.springframework.context.annotation.Bean
    public org.springframework.boot.CommandLineRunner dropRoleConstraint(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;");
                System.out.println("Successfully dropped users_role_check constraint to allow new enum values.");
            } catch (Exception e) {
                System.out.println("Could not drop constraint (might not exist): " + e.getMessage());
            }
        };
    }
}
