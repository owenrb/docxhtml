package io.owenrbee.docxhtml.route;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.freehep.graphicsio.emf.EMFInputStream;
import org.freehep.graphicsio.emf.EMFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmfToPngRoute extends RouteBuilder {

    @Value("file:${route.emf.folder}?noop=true&include=.*.emf")
    private String emfUri;

    @Value("file:${route.emf.png.folder}")
    private String pngUri;

    @Value("${route.emf.disable:false}")
    private boolean disable;

    @Override
    public void configure() throws Exception {

        from(emfUri)
                .routeId("EMF_ROUTE")
                .autoStartup(!disable)
                .setHeader("FileNameOnly", simple("${file:onlyname.noext}"))
                .log("Processing EMF to PNG: ${header.FileNameOnly}....")
                .process(exchange -> {
                    File emfFile = exchange.getIn().getBody(File.class);
                    // Read EMF file
                    try (EMFInputStream emfStream = new EMFInputStream(new FileInputStream(emfFile))) {
                        EMFRenderer emfRenderer = new EMFRenderer(emfStream);

                        // Create a BufferedImage to render EMF
                        BufferedImage image = new BufferedImage(
                                (int) emfRenderer.getSize().getWidth(),
                                (int) emfRenderer.getSize().getHeight(),
                                BufferedImage.TYPE_INT_ARGB);

                        // Render EMF to image
                        emfRenderer.paint(image.createGraphics());

                        // Convert to PNG bytes
                        ByteArrayOutputStream pngStream = new ByteArrayOutputStream();
                        ImageIO.write(image, "PNG", pngStream);

                        // Update exchange with PNG data and filename
                        exchange.getIn().setBody(pngStream.toByteArray());
                        String pngFilename = emfFile.getName().replace(".emf", ".png");
                        exchange.getIn().setHeader(Exchange.FILE_NAME, pngFilename);
                    }
                })
                .to(pngUri)
                .log("EMF -> PNG: Done");

    }

}
