package org.stianloader.sml6.tasks;

import java.io.File;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;

import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.language.jvm.tasks.ProcessResources;
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

        this.getJvmArguments().add(this.getMods().map((modUnits) -> {
            JSONArray outArray = new JSONArray();

            for (FileCollection modUnit : modUnits) {
                JSONArray modURLs = new JSONArray();
                outArray.put(modURLs);

                for (File f : modUnit) {
                    try {
                        modURLs.put(f.toURI().toURL().toExternalForm());
                    } catch (MalformedURLException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }

            return "-Dde.geolykt.starloader.launcher.IDELauncher.modURLs=" + outArray.toString();
        }));
    }

    @InputFiles
    @Classpath
    public abstract Property<FileCollection> getBootFiles();

    @Internal("Transitively covered by #getBootFiles()")
    public abstract Property<FileCollection> getBootGameDependencies();

    @Internal("Transitively covered by #getBootFiles()")
    public abstract RegularFileProperty getBootGameJar();

    @InputFiles
    @Classpath
    public abstract ListProperty<FileCollection> getMods();

    public void usingModSourceSet(Provider<AbstractCompile> classesOutput, Provider<SourceSet> resourceSet) {
        Provider<Directory> classesDir = classesOutput.flatMap(AbstractCompile::getDestinationDirectory);
        Provider<FileCollection> resourcesProvider = resourceSet.map(SourceSet::getResources).map(SourceDirectorySet::getSourceDirectories);

        this.getMods().add(classesDir.zip(resourcesProvider, (classes, resources) -> {
            return this.getObjectFactory().fileCollection().from(classes, resources);
        }));
    }

    public void usingModTasks(Provider<AbstractCompile> classesOutput, Provider<ProcessResources> resourcesDir) {
        Provider<Directory> classesDir = classesOutput.flatMap(AbstractCompile::getDestinationDirectory);
        Provider<File> resourceDir = resourcesDir.map(Copy::getDestinationDir);

        this.getMods().add(classesDir.zip(resourceDir, (classes, resources) -> {
            return this.getObjectFactory().fileCollection().from(classes, resources);
        }));
    }
}
