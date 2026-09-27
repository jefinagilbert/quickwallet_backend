package org.example.paymentorchestratorservice.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomFeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 422) {
            return new RuntimeException("DEBIT_FAILED_INSUFFICIENT_FUNDS");
        }
        if (response.status() == 404) {
            return new RuntimeException("ACCOUNT_NOT_FOUND");
        }
        if (response.status() >= 400 && response.status() <= 499) {
            return new RuntimeException("CLIENT_ERROR_" + response.status());
        }
        return defaultErrorDecoder.decode(methodKey, response);
    }
}
