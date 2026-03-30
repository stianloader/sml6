package org.stianloader.sml6.tasks;

import java.io.File;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.component.UsageContext;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.publish.internal.component.DefaultAdhocSoftwareComponent;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.JavaExec;
import org.gradle.work.DisableCachingByDefault;
import org.json.JSONArray;
import org.stianloader.sml6.SML6GradlePlugin;

@DisableCachingByDefault(because = "Caching degrades user experience")
public abstract class SLLJavaExecTask extends JavaExec {

    public SLLJavaExecTask() {
        this.setDescription("Run the development environment.");
        this.setGroup(SML6GradlePlugin.DEFAULT_TASK_GROUP);
        this.getMainClass().convention("de.geolykt.starloader.launcher.IDELauncher");
        this.setIgnoreExitValue(true);
        this.systemProperty("de.geolykt.starloader.launcher.IDELauncher.inlineStarplaneAnnotations", true);

        this.getModArtifacts().addAll(this.getModComponents().map(components -> {
            List<PublishArtifact> out = new ArrayList<>();

            for (SoftwareComponent component : components) {
                for (UsageContext usageCtx : ((DefaultAdhocSoftwareComponent) component).getUsages()) {
                    if (usageCtx == null) {
                        continue; // Better safe than sorry
                    }

                    for (PublishArtifact artifact : usageCtx.getArtifacts()) {
                        if (artifact != null) {
                            out.add(artifact);
                        }
                    }
                }
            }

            return out;
        }));

        this.getMods().convention(this.getModArtifacts().map(artifacts -> {
            ConfigurableFileCollection collection = this.getObjectFactory().fileCollection();

            for (PublishArtifact artifact : artifacts) {
                collection.from(artifact.getFile());
                collection.builtBy(artifact);
            }

            return collection;
        }));

        this.getBootFiles().convention(this.getBootGameDependencies().zip(this.getBootGameJar(), (files, f) -> {
            return this.getObjectFactory().fileCollection().from(files, f);
        }));

        this.getJvmArguments().add(this.getBootFiles().map(bootFiles -> {
            JSONArray bootURLs = new JSONArray();

            for (File file : bootFiles) {
                try {
                    bootURLs.put(file.toURI().toURL().toExternalForm());
                } catch (MalformedURLException e) {
                    throw new UncheckedIOException(e);
                }
            }

            return "-Dde.geolykt.starloader.launcher.IDELauncher.bootURLs=" + bootURLs.toString();
        }));

        this.getJvmArguments().add(this.getMods().map(modFiles -> {
            JSONArray modURLs = new JSONArray();

            for (File file : modFiles) {
                try {
                    modURLs.put(file.toURI().toURL().toExternalForm());
                } catch (MalformedURLException e) {
                    throw new UncheckedIOException(e);
                }
            }

            return "-Dde.geolykt.starloader.launcher.IDELauncher.modURLs=" + modURLs.toString();
        }));
    }

    @Internal("Transitively covered by #getBootFiles()")
    public abstract Property<FileCollection> getBootGameDependencies();

    @InputFiles
    @Classpath
    public abstract Property<FileCollection> getBootFiles();

    @Internal("Transitively covered by #getBootFiles()")
    public abstract RegularFileProperty getBootGameJar();

    @Internal("Transitively covered by #getMods()")
    public abstract ListProperty<PublishArtifact> getModArtifacts();

    @Internal("Transitively covered by #getMods()")
    public abstract ListProperty<SoftwareComponent> getModComponents();

    @InputFiles
    @Classpath
    public abstract Property<FileCollection> getMods();
}
