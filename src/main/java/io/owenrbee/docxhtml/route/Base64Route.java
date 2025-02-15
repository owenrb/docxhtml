package io.owenrbee.docxhtml.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Base64Route extends RouteBuilder {

    @Value("file:${route.base64.folder}?noop=true&include=.*.png|.*.jpeg|.*.jpg")
    private String imageUri;

    @Value("file:${route.base64.folder}")
    private String textUri;

    @Value("${route.base64.disable:false}")
    private boolean disable;

    @Override
    public void configure() throws Exception {

        from(imageUri)
                .routeId("BASE64_ROUTE")
                .autoStartup(!disable)
                .setHeader("FileNameOnly", simple("${file:onlyname.noext}"))
                .log("Processing image: ${header.FileNameOnly}....")
                .marshal()
                .base64()
                .to(textUri + "?fileName=${header.FileNameOnly}.txt");

    }

}
