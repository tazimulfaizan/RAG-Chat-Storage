package com.example.ragchatstorage;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "RAG Chat Storage Microservice API",
                version = "1.0",
                description = "Production-ready backend microservice to store chat sessions, messages, and RAG context"
        )
)
@SecurityScheme(
        name = "ApiKeyAuth",
        type = SecuritySchemeType.APIKEY,
        in = io.swagger.v3.oas.annotations.enums.SecuritySchemeIn.HEADER,
        paramName = "X-API-KEY"
)
public class RagChatStorageApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagChatStorageApplication.class, args);
    }
}
