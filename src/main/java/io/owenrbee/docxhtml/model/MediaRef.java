package io.owenrbee.docxhtml.model;

import lombok.Data;

@Data
public class MediaRef {

    private String target;

    private String base64;

    private String markdown;

    public String getFilename() {

        if (target == null) {
            return null;
        }

        String[] parts = target.split("/");

        return parts[parts.length - 1];

    }

    public String getFilenameNoExt() {

        String filename = getFilename();

        return filename == null ? null : filename.split("\\.")[0];
    }

    public String getType() {
        String filename = getFilename();

        if (filename == null) {
            return null;
        }

        String ext = filename.split("\\.")[1];
        return "emf".equalsIgnoreCase(ext) ? "png" : ext;
    }

    public String getPrefix() {
        return "data:image/" + getType() + ";base64";
    }

}
