package org.stianloader.sml6.starplane.autodeobf;

import java.io.IOException;
import java.io.Writer;

import org.jetbrains.annotations.NotNull;

public interface AutodeobfRunner {
    @NotNull
    String getVersion();
    void runAll(@NotNull Writer mappingsStream) throws IOException;
}
