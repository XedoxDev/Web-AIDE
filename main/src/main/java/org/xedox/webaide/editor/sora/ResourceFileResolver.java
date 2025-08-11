package org.xedox.webaide.editor.sora;

import io.github.rosemoe.sora.langs.textmate.registry.provider.FileResolver;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;

// File resoulver for working with file system
public class ResourceFileResolver implements FileResolver {

    @Override
    public InputStream resolveStreamByPath(String path) {
        try {
            return new FileInputStream(path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found: " + path, e);
        }
    }
}