package io.owenrbee.docxhtml.route;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LlmVisionRoute extends RouteBuilder {

    @Value("file:${route.vision.folder}?noop=true&include=.*.txt")
    private String imageUri;

    @Value("${route.vision.ollama.chat}")
    private String ollamaChatApi;

    @Value("${route.vision.ollama.model}")
    private String visionModel;

    @Value("${route.vision.ollama.prompt}")
    private String prompt;

    @Value("file:${route.markdown.folder}")
    private String markdownUri;

    @Value("${route.vision.disable:false}")
    private boolean disable;

    @Override
    public void configure() throws Exception {

        from(imageUri)
                .routeId("LLM_ROUTE")
                .autoStartup(!disable)
                .setHeader("FileNameOnly", simple("${file:onlyname.noext}"))
                .log("Processing image for vision: ${header.FileNameOnly}....")
                // Build JSON request structure
                .process(exchange -> {
                    String base64Image = exchange.getIn().getBody(String.class);

                    Map<String, Object> request = new HashMap<>();
                    request.put("model", visionModel);
                    request.put("stream", false);

                    Map<String, Object> message = new HashMap<>();
                    message.put("role", "user");
                    message.put("content", prompt);
                    message.put("images", new String[] { base64Image });

                    request.put("messages", new Map[] { message });

                    exchange.getIn().setBody(request);
                })
                // Convert to JSON and send HTTP POST
                .marshal().json(JsonLibrary.Jackson)
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .to(ollamaChatApi)
                // Directly extract content using JSONPath
                .setBody().jsonpathUnpack("$.message.content", String.class)
                // save the response
                .to(markdownUri + "?fileName=${header.FileNameOnly}.md")
                .log("Done");

    }

}
