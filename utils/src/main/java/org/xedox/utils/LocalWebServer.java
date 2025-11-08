package org.xedox.utils;

import fi.iki.elonen.NanoHTTPD;
import java.io.File;
import java.io.FileInputStream;

public final class LocalWebServer extends NanoHTTPD {
    private final File rootDir;

    public LocalWebServer(int port, File rootDir) {
        super(port);
        this.rootDir = rootDir;
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            String uri = session.getUri();
            File file = new File(rootDir, uri);
            if (!file.exists()) {
                return newFixedLengthResponse(
                        Response.Status.NOT_FOUND, "text/plain", "404 Not Found");
            }
            FileInputStream fis = new FileInputStream(file);
            String mime = getMimeTypeForFile(uri);
            return newChunkedResponse(Response.Status.OK, mime, fis);
        } catch (Exception e) {
            return newFixedLengthResponse(
                    Response.Status.INTERNAL_ERROR, "text/plain", "Error: " + e.getMessage());
        }
    }
}
