package org.stianloader.sml6.starplane.autodeobf;

import org.jetbrains.annotations.NotNull;

public interface AutodeobfRunner {
    @NotNull
    String getVersion();
    void runAll();
}
