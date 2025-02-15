package io.owenrbee.docxhtml.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class XsltRoute extends RouteBuilder {

    @Value("file:${route.docx.xml}?noop=true&fileName=document.xml")
    private String docxmlUri;

    @Value("xslt:file:${route.xslt.template}")
    private String templateUri;

    @Value("file:${route.html.folder}?fileName=out.html")
    private String htmlUri;

    @Value("${route.xslt.disable:false}")
    private boolean disable;

    @Override
    public void configure() throws Exception {
        from(docxmlUri)
                .routeId("XSLT_ROUTE")
                .autoStartup(!disable)
                .to(templateUri)
                .to(htmlUri);
    }

}
