package org.stianloader.sml6;

import java.io.FileOutputStream;

import javax.inject.Inject;

import org.gradle.api.file.ProjectLayout;
import org.gradle.api.internal.file.archive.TarCopyAction;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.bundling.Tar;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

@CacheableTask
public abstract class XZTarBallerTask extends Tar {
    public XZTarBallerTask() {
        // Yes, the 'base' plugin automatically sets these values, but in case
        // the 'base' plugin is absent, we might want to fall back to some defaults.
        this.getArchiveBaseName().convention(GradleUtilities.getBaseArchiveName(this.getProject()));
        this.getDestinationDirectory().convention(GradleUtilities.getDistsDirectory(this.getProject()));

        this.getArchiveExtension().unset().convention("tar.xz"); // Tar doesn't use conventions for this. That's strange, so we will change that. What can go wrong?
        this.getCompressionLevel().convention(LZMA2Options.PRESET_DEFAULT);
    }

    @Override
    protected CopyAction createCopyAction() {
        return new TarCopyAction(this.getArchiveFile().get().getAsFile(), (destination) -> {
            return new XZOutputStream(new FileOutputStream(destination), new LZMA2Options(this.getCompressionLevel().get()));
        }, this.isPreserveFileTimestamps());
    }

    @Input
    @Optional
    public abstract Property<Integer> getCompressionLevel();

    @Inject
    protected abstract ProjectLayout getLayout();
}
