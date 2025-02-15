package io.owenrbee.docxhtml.route;

import java.util.Map;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.owenrbee.docxhtml.model.MediaRef;
import io.owenrbee.docxhtml.service.RelationshipParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Component
public class EmbedStandaloneRoute extends RouteBuilder {

    @Value("file:${route.embed.html.folder}?noop=true&include=.*.html")
    private String htmlUri;

    @Value("${route.embed.reference.file}")
    private String referenceMappingFile;

    @Value("file:${route.embed.html.standalone.folder}")
    private String standaloneUri;

    @Value("${route.embed.html.standalone.disable:false}")
    private boolean disable;

    @Autowired
    private RelationshipParser relationshipParser;

    @Override
    public void configure() throws Exception {

        from(htmlUri)
                .routeId("STANDALONE_ROUTE")
                .autoStartup(!disable)
                .process(exchange -> {
                    String html = exchange.getIn().getBody(String.class);

                    Map<String, MediaRef> map = relationshipParser.parseRelationships(referenceMappingFile);

                    Document doc = Jsoup.parse(html);
                    Elements images = doc.select("img");

                    for (Element img : images) {
                        String id = img.attr("id");
                        MediaRef ref = map.get(id);
                        img.attr("src", ref.getPrefix() + "," + ref.getBase64());
                    }

                    exchange.getIn().setBody(doc.html());
                })
                .to(standaloneUri);

    }

}
