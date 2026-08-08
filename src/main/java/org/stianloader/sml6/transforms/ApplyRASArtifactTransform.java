package org.stianloader.sml6.transforms;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.gradle.api.artifacts.transform.CacheableTransform;
import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.stianloader.sml6.GradleUtilities;

import de.geolykt.starloader.ras.ReversibleAccessSetterContext;
import de.geolykt.starloader.ras.ReversibleAccessSetterContext.RASTransformFailure;
import de.geolykt.starloader.ras.ReversibleAccessSetterContext.RASTransformScope;

@CacheableTransform
public abstract class ApplyRASArtifactTransform implements TransformAction<ApplyRASArtifactTransform.RASInputSpec> {
    public static abstract class RASInputSpec implements TransformParameters {
        // Aliases for valid scope definitions for the 'scope' property. 
        public static final RASTransformScope BUILDTIME = RASTransformScope.BUILDTIME;
        public static final RASTransformScope RUNTIME = RASTransformScope.RUNTIME;

        public RASInputSpec() {
            this.getReversed().convention(Boolean.FALSE);
            this.getOutputArtifactType().convention(this.getScope().map(v -> v.name().toLowerCase(Locale.ROOT)));
            this.getNamespace().convention(this.getInputFile().map(RegularFile::getAsFile).map(File::getPath));
            this.getFastTransform().convention(Boolean.FALSE);
        }

        @Input
        public abstract Property<Boolean> getFastTransform();

        @InputFile
        @PathSensitive(PathSensitivity.NONE)
        public abstract RegularFileProperty getInputFile();

        @Input
        @Optional
        public abstract Property<@NotNull String> getNamespace();

        @Input
        @Optional
        public abstract Property<String> getOutputArtifactType();

        @Input
        public abstract Property<Boolean> getReversed();

        @Input
        public abstract Property<@NotNull RASTransformScope> getScope();
    }

    @InputArtifact
    @Classpath
    public abstract Provider<FileSystemLocation> getInputArtifact();

    @Override
    public void transform(TransformOutputs outputs) {
        if (!this.getParameters().getInputFile().isPresent()) {
            throw new IllegalStateException("Cannot run artifact transform because the input file is not configured.");
        }

        Path inputRasFile = this.getParameters().getInputFile().get().getAsFile().toPath();
        Path inputJarFile = this.getInputArtifact().get().getAsFile().toPath();

        if (!inputJarFile.getFileName().toString().endsWith(".jar")) {
            throw new IllegalStateException("Unsupported file extension: " + inputJarFile.getFileName().toString());
        }

        String type = this.getParameters().getOutputArtifactType().getOrNull();
        String fileName;

        if (type != null && !type.isEmpty()) {
            fileName = inputJarFile.getFileName().toString().replace(".jar", "-" + type + ".jar");
        } else {
            fileName = inputJarFile.getFileName().toString();
        }

        Path outputFile = outputs.file(fileName).toPath();

        ReversibleAccessSetterContext rasInfo = new ReversibleAccessSetterContext(this.getParameters().getScope().get(), false);

        try (BufferedReader reader = Files.newBufferedReader(inputRasFile, StandardCharsets.UTF_8)) {
            rasInfo.read(this.getParameters().getNamespace().get(), reader, this.getParameters().getReversed().get());
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read RAS file.", e);
        }

        try (InputStream rawIn = Files.newInputStream(inputJarFile);
                ZipInputStream zipIn = new ZipInputStream(rawIn, StandardCharsets.UTF_8);
                OutputStream rawOut = Files.newOutputStream(outputFile);
                ZipOutputStream zipOut = new ZipOutputStream(rawOut, StandardCharsets.UTF_8)) {

            boolean fastTransform = this.getParameters().getFastTransform().get();
            ZipEntry e;

            while ((e = zipIn.getNextEntry()) != null) {
                if (!e.getName().endsWith(".class")) {
                    zipOut.putNextEntry(e);
                    zipIn.transferTo(zipOut);
                    continue;
                }

                ClassReader reader = new ClassReader(zipIn);

                if (fastTransform && !rasInfo.isTarget(Objects.requireNonNull(reader.getClassName()))) {
                    ClassWriter cw = new ClassWriter(0);
                    reader.accept(cw, 0);
                    zipOut.putNextEntry(GradleUtilities.copyEntry(e));
                    zipOut.write(cw.toByteArray());
                } else {
                    ClassNode node = new ClassNode();
                    ClassWriter cw = new ClassWriter(0);
                    reader.accept(node, 0);

                    try {
                        rasInfo.accept(node);
                    } catch (RASTransformFailure e1) {
                        throw new IOException("Cannot apply RAS for node " + node.name + " (located in entry '" + e.getName() + "')", e1);
                    }

                    node.accept(cw);
                    zipOut.putNextEntry(GradleUtilities.copyEntry(e));
                    zipOut.write(cw.toByteArray());
                }
            }

        } catch (IOException e) {
            throw new UncheckedIOException("Cannot apply RAS file.", e);
        }
    }
}
