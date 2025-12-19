package org.stianloader.sml6.tasks;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.internal.file.copy.CopyActionProcessingStream;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.WorkResult;
import org.gradle.api.tasks.WorkResults;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.jetbrains.annotations.NotNull;
import org.stianloader.sml6.GradleUtilities;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.mappingio.tree.VisitOrder;
import net.fabricmc.mappingio.tree.VisitableMappingTree;

@CacheableTask
public abstract class AggregateMappingsTask extends AbstractArchiveTask {

    private static class AggregateMappingsCopyAction implements CopyAction {
        @NotNull
        private final MappingFormat inputFormat;

        @NotNull
        private final Path outputFile;

        @NotNull
        private final MappingFormat outputFormat;

        public AggregateMappingsCopyAction(@NotNull MappingFormat inputFormat, @NotNull Path outputFile, @NotNull MappingFormat outputFormat) {
            this.inputFormat = Objects.requireNonNull(inputFormat, "inputFormat may not be null!");
            this.outputFile = Objects.requireNonNull(outputFile, "outputFile may not be null!");
            this.outputFormat = Objects.requireNonNull(outputFormat, "outputFormat may not be null!");
        }

        @Override
        public WorkResult execute(CopyActionProcessingStream stream) {
            VisitableMappingTree mappings = new MemoryMappingTree();

            // Read inputs
            stream.process((details) -> {
                if (details.isDirectory()) {
                    return;
                }

                try {
                    MappingReader.read(details.getFile().toPath(), this.inputFormat, mappings);
                } catch (IOException e) {
                    throw new UncheckedIOException("Failed to read mappings file '" + details.getFile().toPath() + "' with input format '" + this.inputFormat + "'", e);
                }
            });

            // Write output
            try {
                MappingWriter writer = MappingWriter.create(this.outputFile, this.outputFormat);
                if (writer == null) {
                    throw new IOException("Cannot create a MappingWriter instance for output path '" + this.outputFile + "' with format '" + this.outputFormat + "'");
                }
                mappings.accept(writer, VisitOrder.createByName());
                writer.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            return WorkResults.didWork(true);
        }
    }

    public AggregateMappingsTask() {
        // Yes, the 'base' plugin automatically sets these values, but in case
        // the 'base' plugin is absent, we might want to fall back to some defaults.
        this.getArchiveBaseName().convention(GradleUtilities.getBaseArchiveName(this.getProject()));
        this.getDestinationDirectory().convention(GradleUtilities.getDistsDirectory(this.getProject()));
    }

    @Override
    protected CopyAction createCopyAction() {
        return new AggregateMappingsCopyAction(this.getInputFormat().get(), this.getArchiveFile().get().getAsFile().toPath(), this.getOutputFormat().get());
    }

    @Input
    public abstract Property<@NotNull MappingFormat> getInputFormat();

    @Input
    public abstract Property<@NotNull MappingFormat> getOutputFormat();

    public void inputFormat(@NotNull String format) {
        this.getInputFormat().set(this.toMappingFormat(format, false));
    }

    public void outputFormat(@NotNull String format) {
        this.getOutputFormat().set(this.toMappingFormat(format, false));
    }

    @NotNull
    private MappingFormat toMappingFormat(@NotNull String format, boolean asDirectory) {
        MappingFormat mFormat = null;
        String cleanFormat = format.toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        try {
            mFormat = MappingFormat.valueOf(cleanFormat);
        } catch (IllegalArgumentException e) {
            for (MappingFormat mf : MappingFormat.values()) {
                if (mf.name != null && mf.name.equalsIgnoreCase(format)) {
                    mFormat = mf;
                    break;
                }
            }
            if (cleanFormat.equalsIgnoreCase("tiny2") || cleanFormat.equalsIgnoreCase("tinyv2") || cleanFormat.equalsIgnoreCase("tiny_v2")) {
                mFormat = MappingFormat.TINY_2_FILE;
            } else if (format.equalsIgnoreCase("enigma")) {
                if (asDirectory) {
                    mFormat = MappingFormat.ENIGMA_DIR;
                } else {
                    mFormat = MappingFormat.ENIGMA_FILE;
                }
            } else if (mFormat == null) {
                for (MappingFormat mf : MappingFormat.values()) {
                    if (mf.fileExt != null && mf.fileExt.equalsIgnoreCase(format)) {
                        mFormat = mf;
                        break;
                    }
                }
            }
        }

        if (mFormat == null) {
            throw new IllegalArgumentException("No mappings format known under the following name: '" + format + "'");
        }

        return mFormat;
    }
}
