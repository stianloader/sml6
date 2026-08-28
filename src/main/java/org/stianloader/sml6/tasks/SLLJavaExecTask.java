package org.stianloader.sml6.tasks;

import java.io.File;
import java.io.UncheckedIOException;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.language.jvm.tasks.ProcessResources;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;
import org.gradle.process.internal.ExecAction;
import org.gradle.process.internal.ExecActionFactory;
import org.gradle.process.internal.JavaExecAction;
import org.gradle.work.DisableCachingByDefault;
import org.json.JSONArray;
import org.stianloader.sml6.SML6GradlePlugin;

@DisableCachingByDefault(because = "Caching degrades user experience")
public abstract class SLLJavaExecTask extends JavaExec {

    private final ConfigurableFileCollection bootFiles;

    public SLLJavaExecTask() {
        this.setDescription("Run the development environment.");
        this.setGroup(SML6GradlePlugin.DEFAULT_TASK_GROUP);
        this.bootFiles = this.getObjectFactory().fileCollection();
        this.getMainClass().convention("de.geolykt.starloader.launcher.IDELauncher");
        this.getPropertyExpansionSource().convention(this.getLayout().getProjectDirectory().file("gradle.properties"));
        this.setIgnoreExitValue(true);
        this.systemProperty("de.geolykt.starloader.launcher.IDELauncher.inlineStarplaneAnnotations", true);

        this.getJvmArguments().add(this.getBootFiles().getElements().map(bootFiles -> {
            JSONArray bootURLs = new JSONArray();

            for (FileSystemLocation file : bootFiles) {
                try {
                    bootURLs.put(file.getAsFile().toURI().toURL().toExternalForm());
                } catch (MalformedURLException e) {
                    throw new UncheckedIOException(e);
                }
            }

            return "-Dde.geolykt.starloader.launcher.IDELauncher.bootURLs=" + bootURLs.toString();
        }));

        this.getJvmArguments().add(this.getPropertyExpansionSource().map((propertySource) -> {
            return "-Dorg.stianloader.sll.IDELauncher.propertyExpansionSource=" + propertySource.getAsFile().getAbsolutePath();
        }).orElse("-Dorg.stianloader.sml6.noPropertyExpansionSource"));

        this.getJvmArguments().add(this.getGameMainClass().map(mainClass -> {
            // Not using #systemProperty here because that doesn't handle properties correctly (ironic, isn't it?).
            return "-Dde.geolykt.starloader.launcher.CLILauncher.mainClass=" + mainClass;
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

    @Inject
    protected abstract ExecActionFactory getActualExecActionFactory();

    @InputFiles
    @Classpath
    public ConfigurableFileCollection getBootFiles() {
        return this.bootFiles;
    }

    @Input
    public abstract Property<String> getGameMainClass();

    @Inject
    protected abstract ProjectLayout getLayout();

    @InputFiles
    @Classpath
    public abstract ListProperty<FileCollection> getMods();

    @Internal
    abstract ListProperty<FileCollection> getModSourceSets();

    @InputFile
    @Optional
    public abstract RegularFileProperty getPropertyExpansionSource();

    @InputFile
    @Optional
    public abstract RegularFileProperty getRenderdocExecutable();

    @Override
    protected ExecActionFactory getExecActionFactory() {
        ExecActionFactory originalFactory = this.getActualExecActionFactory();
        RegularFileProperty renderdocExec = this.getRenderdocExecutable();

        renderdocExec.disallowChanges();

        if (!renderdocExec.isPresent()) {
            return originalFactory;
        }

        File renderdocFile = renderdocExec.get().getAsFile();

        if (!renderdocFile.exists()) {
            this.getLogger().warn("The renderdoc executable file '{}' configured for task '{}' does not exist. Using normal java execution instead.", renderdocFile, this.getIdentityPath());
            return originalFactory;
        }

        return (ExecActionFactory) Proxy.newProxyInstance(SLLJavaExecTask.class.getClassLoader(), new Class<?>[] { ExecActionFactory.class }, (proxy, method, args) -> {
            if (method.getName().equals("newJavaExecAction")) {
                JavaExecAction originalAction = (JavaExecAction) method.invoke(originalFactory, args);

                return Proxy.newProxyInstance(SLLJavaExecTask.class.getClassLoader(), new Class<?>[] { JavaExecAction.class }, (proxy2, method2, args2) -> {
                    if (method2.getName().equals("execute")) {
                        ExecAction execAction = this.getActualExecActionFactory().newExecAction();

                        List<String> execArgs = new ArrayList<>();
                        execArgs.add("capture");
                        execArgs.add("--wait-for-exit");
                        execArgs.add("--working-dir");
                        execArgs.add(originalAction.getWorkingDirectory().get().getAsFile().toString());
                        execArgs.addAll(originalAction.getCommandLine());

                        execAction
                            .args(execArgs)
                            .workingDir(originalAction.getWorkingDirectory().get().getAsFile().toString())
                            .environment(originalAction.getEnvironment())
                            .executable(renderdocExec.get().getAsFile().toString());

                        return execAction.execute();
                    }

                    return method2.invoke(originalAction, args2);
                });
            } else {
                return method.invoke(originalFactory, args);
            }
        });
    }

    public void usingModSourceSet(Provider<AbstractCompile> classesOutput, Provider<SourceSet> resourceSet) {
        Provider<Directory> classesDir = classesOutput.flatMap(AbstractCompile::getDestinationDirectory);
        Provider<FileCollection> resourcesProvider = resourceSet.map(SourceSet::getResources).map(SourceDirectorySet::getSourceDirectories);

        this.getMods().add(classesDir.zip(resourcesProvider, (classes, resources) -> {
            return this.getObjectFactory().fileCollection().from(classes, resources);
        }));

        this.getModSourceSets().add(resourceSet.map((modSourceSet) -> {
            EclipseModel eclipseModel = (EclipseModel) this.getProject().findProperty("eclipse");

            Directory baseSourceOutputDir;

            if (eclipseModel != null) {
                baseSourceOutputDir = eclipseModel.getClasspath().getBaseSourceOutputDir().get();
            } else {
                baseSourceOutputDir = this.getLayout().getProjectDirectory().dir("bin");
            }

            return baseSourceOutputDir.files(modSourceSet.getName());
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
