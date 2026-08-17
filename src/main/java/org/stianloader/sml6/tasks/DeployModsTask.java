package org.stianloader.sml6.tasks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.inject.Inject;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.stianloader.sml6.SML6GradlePlugin;

@CacheableTask
public abstract class DeployModsTask extends ConventionTask {

    public static java.util.Optional<String> getExtensionName(@NotNull Path in) throws IOException {
        try (InputStream rawIn = Files.newInputStream(in);
                ZipInputStream zipIn = new ZipInputStream(rawIn)) {
            for (ZipEntry entry = zipIn.getNextEntry(); entry != null; entry = zipIn.getNextEntry()) {
                if (!entry.getName().equals("extension.json")) {
                    continue;
                }

                JSONObject extension = new JSONObject(new String(zipIn.readAllBytes(), StandardCharsets.UTF_8));

                return java.util.Optional.ofNullable(extension.getString("name"));
            }
        }

        return java.util.Optional.empty();
    }

    @OutputDirectory
    @Optional
    public abstract DirectoryProperty getModsDirectory();

    @InputFiles
    @Classpath
    public abstract Property<FileCollection> getMods();

    @Inject
    protected abstract ProjectLayout getLayout();

    public DeployModsTask() {
        this.getModsDirectory().convention(this.getLayout().getProjectDirectory().dir("mods"));
        this.setGroup(SML6GradlePlugin.DEFAULT_TASK_GROUP);
    }

    @TaskAction
    public void deploy() {
        Set<String> extensionNames = new HashSet<>();
        List<Path> mods = new ArrayList<>();

        for (File mod : this.getMods().get().getFiles()) {
            Path modPath = mod.toPath();

            // Only add valid mods
            if (Files.notExists(modPath)) {
                this.getLogger().warn("task '{}': No file at path: '{}'", this.getName(), modPath);
                continue;
            }

            try {
                java.util.Optional<String> name = DeployModsTask.getExtensionName(modPath);

                if (name.isPresent()) {
                    mods.add(modPath);
                    extensionNames.add(name.get());
                } else {
                    this.getLogger().warn("task '{}': Invalid mod at path '{}': extension.json file does not specify a mod name.", this.getName(), modPath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Path modDirectory = this.getModsDirectory().get().getAsFile().toPath();

        if (Files.notExists(modDirectory)) {
            try {
                Files.createDirectories(modDirectory);
            } catch (IOException x) { }
        }

        // Remove any older copies of the mods
        try {
            Files.walkFileTree(modDirectory, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.SKIP_SUBTREE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().endsWith(".jar")) {
                        return FileVisitResult.CONTINUE;
                    }

                    java.util.Optional<String> extensionName = DeployModsTask.getExtensionName(file);

                    if (extensionName.isPresent() && extensionNames.contains(extensionName.get())) {
                        Files.delete(file);
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    throw exc;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc != null) {
                        throw exc;
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot delete earlier deployments of the mods.", e);
        }

        // Actual deployment process
        for (Path mod : mods) {
            try {
                Path target = mod.getFileName();

                if (target == null) {
                    // shouldn't happen, but it's there just in case some logic does actually blow up.
                    target = modDirectory.resolve("extension-" + DeployModsTask.getExtensionName(mod).orElseGet(() -> Long.toString(ThreadLocalRandom.current().nextLong(0, Long.MAX_VALUE), Character.MAX_RADIX)) + "-.jar");
                } else {
                    target = modDirectory.resolve(target);
                }

                this.getLogger().info("Deploying {}", mod);
                Files.copy(mod, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
