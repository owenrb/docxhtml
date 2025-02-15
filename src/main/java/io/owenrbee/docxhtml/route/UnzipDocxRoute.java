package io.owenrbee.docxhtml.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.zipfile.ZipSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UnzipDocxRoute extends RouteBuilder {

    @Value("file:${route.docx.folder}?noop=true")
    private String docxUri;

    @Value("file:${route.unzip.folder}")
    private String unzipUri;

    @Value("${route.docx.disable:false}")
    private boolean disable;

    @Override
    public void configure() throws Exception {
        from(docxUri)
                .routeId("UNZIP_ROUTE")
                .autoStartup(!disable)
                .log("Staring Unzip Route")
                .split(new ZipSplitter())
                .streaming()
                .to(unzipUri);
    }

}
