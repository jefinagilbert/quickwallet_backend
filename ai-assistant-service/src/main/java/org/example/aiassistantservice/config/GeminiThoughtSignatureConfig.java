package org.example.aiassistantservice.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class GeminiThoughtSignatureConfig {

    private static final Logger log = LoggerFactory.getLogger(GeminiThoughtSignatureConfig.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<String, JsonNode> toolCallSignatures = new ConcurrentHashMap<>();

    @Bean
    public RestClientCustomizer geminiThoughtSignatureCustomizer() {
        return restClientBuilder -> restClientBuilder.requestInterceptor(new GeminiThoughtSignatureInterceptor());
    }

    public static class GeminiThoughtSignatureInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            byte[] outgoingBody = body;
            boolean modified = false;
            try {
                if (body != null && body.length > 0) {
                    byte[] newBody = injectSignatures(body);
                    if (newBody != body) {
                        outgoingBody = newBody;
                        modified = true;
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to inject Gemini thought_signatures: {}", e.getMessage());
            }

            HttpRequest requestToExecute = request;
            if (modified) {
                final byte[] finalBody = outgoingBody;
                requestToExecute = new HttpRequestWrapper(request) {
                    private final HttpHeaders headers = new HttpHeaders();
                    {
                        headers.putAll(request.getHeaders());
                        headers.setContentLength(finalBody.length);
                    }

                    @Override
                    public HttpHeaders getHeaders() {
                        return headers;
                    }
                };
            }

            ClientHttpResponse response = execution.execute(requestToExecute, outgoingBody);

            try {
                byte[] responseBytes = StreamUtils.copyToByteArray(response.getBody());
                captureSignatures(responseBytes);
                return new BufferingClientHttpResponse(response, responseBytes);
            } catch (Exception e) {
                log.warn("Failed to capture Gemini thought_signatures: {}", e.getMessage());
                return response;
            }
        }

        private byte[] injectSignatures(byte[] body) {
            try {
                JsonNode root = objectMapper.readTree(body);
                if (!root.has("messages") || !root.get("messages").isArray()) {
                    return body;
                }

                boolean modified = false;
                ArrayNode messages = (ArrayNode) root.get("messages");
                for (JsonNode message : messages) {
                    if (message.has("role") && "assistant".equals(message.get("role").asText())) {
                        if (message.has("tool_calls") && message.get("tool_calls").isArray()) {
                            ArrayNode toolCalls = (ArrayNode) message.get("tool_calls");
                            for (JsonNode toolCall : toolCalls) {
                                if (toolCall instanceof ObjectNode toolCallObj) {
                                    if (!toolCallObj.has("extra_content")) {
                                        String toolCallId = toolCallObj.has("id") ? toolCallObj.get("id").asText() : null;
                                        if (toolCallId != null && toolCallSignatures.containsKey(toolCallId)) {
                                            toolCallObj.set("extra_content", toolCallSignatures.get(toolCallId));
                                            modified = true;
                                            log.info("Re-injected thought_signature for tool call id: {}", toolCallId);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (modified) {
                    return objectMapper.writeValueAsBytes(root);
                }
            } catch (Exception e) {
                log.warn("Error inspecting outgoing request for thought signatures: {}", e.getMessage());
            }
            return body;
        }

        private void captureSignatures(byte[] responseBytes) {
            try {
                if (responseBytes == null || responseBytes.length == 0) {
                    return;
                }
                JsonNode root = objectMapper.readTree(responseBytes);
                if (!root.has("choices") || !root.get("choices").isArray()) {
                    return;
                }

                ArrayNode choices = (ArrayNode) root.get("choices");
                for (JsonNode choice : choices) {
                    if (choice.has("message")) {
                        JsonNode message = choice.get("message");
                        if (message.has("tool_calls") && message.get("tool_calls").isArray()) {
                            ArrayNode toolCalls = (ArrayNode) message.get("tool_calls");
                            for (JsonNode toolCall : toolCalls) {
                                if (toolCall.has("id") && toolCall.has("extra_content")) {
                                    String id = toolCall.get("id").asText();
                                    JsonNode extraContent = toolCall.get("extra_content");
                                    toolCallSignatures.put(id, extraContent);
                                    log.info("Captured thought_signature for tool call id: {}", id);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Error capturing thought signatures from response: {}", e.getMessage());
            }
        }
    }

    private static class BufferingClientHttpResponse implements ClientHttpResponse {
        private final ClientHttpResponse delegate;
        private final byte[] body;

        public BufferingClientHttpResponse(ClientHttpResponse delegate, byte[] body) {
            this.delegate = delegate;
            this.body = body != null ? body : new byte[0];
        }

        @Override
        public HttpStatusCode getStatusCode() throws IOException {
            return delegate.getStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return delegate.getStatusText();
        }

        @Override
        public void close() {
            delegate.close();
        }

        @Override
        public InputStream getBody() throws IOException {
            return new ByteArrayInputStream(body);
        }

        @Override
        public HttpHeaders getHeaders() {
            return delegate.getHeaders();
        }
    }
}
