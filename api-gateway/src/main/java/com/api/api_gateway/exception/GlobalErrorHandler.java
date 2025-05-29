package com.api.api_gateway.exception;

import com.api.api_gateway.dto.ErrorResponseDTO;
import static com.api.api_gateway.util.ErrorMessages.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
@Order(-2)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalErrorHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = UNEXPECTED_ERROR;

        if (ex instanceof InvalidTokenException) {
            status = HttpStatus.UNAUTHORIZED;
            message = ex.getMessage();
        }

        ErrorResponseDTO errorAttributes = new ErrorResponseDTO();
        errorAttributes.setStatus(status.value());
        errorAttributes.setError(status.getReasonPhrase());
        errorAttributes.setMessage(message);
        errorAttributes.setTimestamp(LocalDateTime.now());
        errorAttributes.setPath(exchange.getRequest().getURI().getPath());

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(errorAttributes);
        } catch (Exception e) {
            bytes = ("{\"error\":\"" + SERIALIZATION_ERROR + "\"}").getBytes(StandardCharsets.UTF_8);
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

}
