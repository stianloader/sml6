package org.stianloader.sml6.starplane.sourcegen;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.jetbrains.java.decompiler.main.decompiler.SingleFileSaver;

public class EnhancedJarSaver extends SingleFileSaver {

    // mapping[i * 2] -> original line number; mapping[i * 2 + 1] -> new line number
    private Map<String, int[]> lineMappings;

    public EnhancedJarSaver(File target, Map<String, int[]> lineMappings) {
        super(target);
        this.lineMappings = lineMappings;
    }

    @Override
    public synchronized void saveClassEntry(String path, String archiveName, String qualifiedName,
            String entryName, String content, int[] mapping) {
        super.saveClassEntry(path, archiveName, qualifiedName, entryName, content, mapping);
        this.lineMappings.put(qualifiedName, mapping);
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            } else {
                throw new IOException(e);
            }
        }
    }
}
