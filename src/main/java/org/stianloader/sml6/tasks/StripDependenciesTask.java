package org.stianloader.sml6.tasks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.NotNull;
import org.stianloader.sml6.SML6GradlePlugin;

@CacheableTask
public abstract class StripDependenciesTask extends ConventionTask {

    public StripDependenciesTask() {
        this.setGroup(SML6GradlePlugin.DEFAULT_TASK_GROUP);
        this.setDescription("Remove shaded dependencies in jar");

        DirectoryProperty buildDir = this.getLayout().getBuildDirectory();
        Provider<String> taskNameProvider = this.getProviders().provider(this::getName);
        this.getOutputDirectory().convention(buildDir.dir(taskNameProvider.map(s -> "sml6/" + s)));
        this.getOutputJar().convention(this.getOutputDirectory().file("game-reduced.jar"));
    }

    @Inject
    protected abstract ConfigurationContainer getConfigurations();

    @Inject
    protected abstract DependencyHandler getDependencies();

    @Input
    @Optional
    public abstract ListProperty<String> getFilteringEntryNames();

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getInputJar();

    @Inject
    protected abstract ProjectLayout getLayout();

    @Internal("Transitively affects other output locations. Not used directly.")
    public abstract DirectoryProperty getOutputDirectory();

    @OutputFile
    @Optional
    @NotNull
    public abstract RegularFileProperty getOutputJar();

    @Inject
    protected abstract ProviderFactory getProviders();

    @InputFiles
    @Classpath
    public abstract Property<FileCollection> getStrippingDependencies();

    public void stripGalimulatorDefaults502() {
        this.stripGalimulatorDefaults502((t) -> {});
    }

    /**
     * Strip the default dependencies for Galimulator 5.0.2
     */
    public void stripGalimulatorDefaults502(Action<Configuration> configureClosure) {
        this.stripGalimulatorDefaults502("sml6-" + this.getName(), configureClosure);
    }

    /**
     * Strip the default dependencies for Galimulator 5.0.2
     */
    public void stripGalimulatorDefaults502(@NotNull String configurationName, Action<Configuration> configureClosure) {
        NamedDomainObjectProvider<Configuration> configuration = this.getConfigurations().register(configurationName, (config) -> {

            config.setDescription("Default dependencies for Galimulator 5.0.2 for task '" + this.getName() + "'.");

            config.getDependencies().addAll(
                Arrays.asList(
                    this.getDependencies().create("com.badlogicgames.gdx:gdx-backend-lwjgl:1.9.11"),
                    this.getDependencies().create("com.badlogicgames.gdx:gdx-platform:1.9.11:natives-desktop"),
                    this.getDependencies().create("com.badlogicgames.gdx:gdx-tools:1.9.11"),
                    this.getDependencies().create("com.badlogicgames.gdxpay:gdx-pay-client:1.3.0"),
                    this.getDependencies().create("com.code-disaster.steamworks4j:steamworks4j-server:1.8.0"),
                    this.getDependencies().create("com.thoughtworks.xstream:xstream:1.4.7"),
                    this.getDependencies().create("xpp3:xpp3:1.1.4c"),
                    this.getDependencies().create("junit:junit:4.13.2")
                )
            );

            // The exclusions are technically not necessary, but we exclude them in order to avoid duplicate entries on the classpath
            // This is mostly only useful for other tasks using the configuration as a transient input.
            config.exclude(Map.of("group", "xmlpull", "module", "xmlpull"));
            config.exclude(Map.of("group", "xpp3", "module", "xpp3_min"));
        });

        configuration.configure(configureClosure);

        this.getFilteringEntryNames().add("XPP3_1.1.4c_MIN_VERSION");

        // simplevoronoi seems to be source-shaded into Galimulator, and there is no maven artifact out there for it.
        // hence we will treat that dependency like any other

        this.getStrippingDependencies().set(configuration);
    }

    @TaskAction
    public void stripJar() {
        Set<String> filteringFileNames = new HashSet<>(this.getFilteringEntryNames().getOrElse(Collections.emptyList()));

        boolean failing = false;

        for (File file : this.getStrippingDependencies().get()) {
            try (InputStream in = Files.newInputStream(file.toPath());
                    ZipInputStream zipIn = new ZipInputStream(in, StandardCharsets.UTF_8)) {
                for (ZipEntry entry = zipIn.getNextEntry(); entry != null; entry = zipIn.getNextEntry()) {
                    if (entry.isDirectory()
                            && entry.getName().startsWith("META-INF/versions/")
                            && entry.getName().lastIndexOf('/', entry.getName().length() - 1) == "META-INF/versions/".length() - 1) {
                        continue;
                    }

                    filteringFileNames.add(entry.getName());
                }
            } catch (IOException e) {
                this.getLogger().error("Unable to read from file {} whilst executing task '{}'", file, this.getName(), e);
                failing = true;
            }
        }

        if (failing) {
            throw new IllegalStateException("Task execution failed: Cannot read declared stripping dependencies");
        }

        filteringFileNames.remove("META-INF/MANIFEST.MF");
        filteringFileNames.remove("META-INF/");
        filteringFileNames.remove("META-INF/versions/");
        filteringFileNames.remove("META-INF/services/");
        filteringFileNames.remove("/");

        try (InputStream inStream = Files.newInputStream(this.getInputJar().get().getAsFile().toPath());
                ZipInputStream zipIn = new ZipInputStream(inStream, StandardCharsets.UTF_8);
                OutputStream outStream = Files.newOutputStream(this.getOutputJar().get().getAsFile().toPath());
                ZipOutputStream zipOut = new ZipOutputStream(outStream, StandardCharsets.UTF_8)) {
            for (ZipEntry entry = zipIn.getNextEntry(); entry != null; entry = zipIn.getNextEntry()) {
                if (filteringFileNames.contains(entry.getName())) {
                    continue;
                }

                zipOut.putNextEntry(entry);
                zipIn.transferTo(zipOut);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
