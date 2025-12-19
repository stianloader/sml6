package org.stianloader.sml6.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

@CacheableTask
public abstract class XZCompressTask extends AbstractArtifactTask {

    public XZCompressTask() {
        this.getArchiveExtension().convention("xz");
        this.getCompressionLevel().convention(LZMA2Options.PRESET_DEFAULT);
    }

    @TaskAction
    public void compress() throws IOException {
        try (InputStream in = Files.newInputStream(this.getInput().get().getAsFile().toPath());
                OutputStream out = Files.newOutputStream(this.getArchiveFile().get().getAsFile().toPath());
                OutputStream compressedOut = new XZOutputStream(out, new LZMA2Options(this.getCompressionLevel().get()))) {
            in.transferTo(compressedOut);
        }
    }

    public void from(Object notation) {
        this.getInput().fileProvider(this.getProject().getProviders().provider(() -> {
            return this.getProject().file(notation);
        }));
        this.getInput().disallowChanges();
    }

    @Input
    @Optional
    public abstract Property<Integer> getCompressionLevel();

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getInput();
}
