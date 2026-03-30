package org.stianloader.sml6.tasks;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.gradle.api.JavaVersion;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;
import org.gradle.process.CommandLineArgumentProvider;
import org.jetbrains.annotations.NotNull;

@CacheableTask
public abstract class GenerateEclipseRunTask extends ConventionTask {

    private static void writeListAttr(@NotNull Writer writer, @NotNull String indent, @NotNull String key, @NotNull List<@NotNull String> values) throws IOException {
        writer.write(indent);
        writer.write("<listAttribute key=\"");
        writer.write(key);

        if (values.isEmpty()) {
            writer.write("\"/>\n");
            return;
        } else {
            writer.write("\">\n");
        }

        for (String value : values) {
            writer.write(indent);
            writer.write("  <listEntry value=\"");
            writer.write(value);
            writer.write("\"/>\n");
        }

        writer.write(indent);
        writer.write("</listAttribute>\n");
    }

    private static void writeStringAttr(@NotNull Writer writer, @NotNull String indent, @NotNull String key, @NotNull String value) throws IOException {
        writer.write(indent);
        writer.write("<stringAttribute key=\"");
        writer.write(key);
        writer.write("\" value=\"");
        writer.write(value.replace("\"", "\\&quot;"));
        writer.write("\"/>\n");
    }

    public GenerateEclipseRunTask() {
        this.getProjectName().convention(this.getProviders().provider(() -> {
            EclipseModel eclipseModel = (EclipseModel) this.getProject().getProperties().get("eclipse");
            if (eclipseModel == null) {
                return this.getProject().getName();
            } else {
                return eclipseModel.getProject().getName();
            }
        }));

        this.getModuleName().convention(this.getProjectName());
    }

    public void from(@NotNull JavaExec task) {
        this.from(this.getProviders().provider(() -> task));
    }

    public void from(Provider<@NotNull JavaExec> task) {
        this.getArgs().set(task.flatMap(exec -> { // .flatMap(…) required else it will use a TaskProducer which isn't great.
            return this.getProviders().provider(exec::getArgumentProviders);
        }));

        this.getJvmArgs().set(task.flatMap(JavaExec::getJvmArguments).map((jvmArgs) -> {
            List<String> outArgs = new ArrayList<>(jvmArgs);

            task.get().getSystemProperties().forEach((key, value) -> {
                outArgs.add("-D" + key + (value != null ? "=" + value.toString() : ""));
            });

            return outArgs;
        }));

        this.getMainClass().set(task.flatMap(JavaExec::getMainClass));

        this.getJavaVersion().set(task.flatMap(exec -> { // .flatMap(…) required else it will use a TaskProducer which isn't great.
            return this.getProviders().provider(exec::getJavaVersion);
        }));

        this.getWorkingDir().set(this.getLayout().dir(task.flatMap(exec -> {
            return this.getProviders().provider(exec::getWorkingDir);
        })));

        this.getOutputFile().set(this.getLayout().getProjectDirectory().file(task.flatMap(exec -> {
            return this.getProviders().provider(() -> exec.getProject().getName() + "-" + exec.getName().substring(exec.getName().lastIndexOf(':') + 1) + ".launch");
        })));

        this.getWorkingDirPath().set(this.getWorkingDir().map(Directory::getAsFile).map(File::toPath).map(Path::toAbsolutePath).map(Path::toString));
    }

    @TaskAction
    public void generateRuns() {
        try (Writer writer = Files.newBufferedWriter(this.getOutputFile().get().getAsFile().toPath(), StandardCharsets.UTF_8)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
            writer.write("<launchConfiguration type=\"org.eclipse.jdt.launching.localJavaApplication\">\n");

            JavaVersion javaVersion = this.getJavaVersion().get();
            String jvmVersionString = (javaVersion.isJava9Compatible() ? "" : "1.") + javaVersion.getMajorVersion();

            List<@NotNull String> classpathElements = new ArrayList<>();

            // Project source sets
            classpathElements.add("&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;no&quot;?&gt;&#10;&lt;runtimeClasspathEntry path=&quot;5&quot; projectName=&quot;" + this.getProjectName().get() + "&quot; type=&quot;1&quot;/&gt;&#10;");
            // Gradle classpath
            classpathElements.add("&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;no&quot;?&gt;&#10;&lt;runtimeClasspathEntry containerPath=&quot;org.eclipse.buildship.core.gradleclasspathcontainer&quot; javaProject=&quot;" + this.getProjectName().get() + "&quot; path=&quot;5&quot; type=&quot;4&quot;/&gt;&#10;");
            // JVM
            classpathElements.add("&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;no&quot;?&gt;&#10;&lt;runtimeClasspathEntry containerPath=&quot;org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-" + jvmVersionString + "/&quot; path=&quot;5&quot; type=&quot;4&quot;/&gt;&#10;");

            GenerateEclipseRunTask.writeListAttr(writer, "  ", "org.eclipse.jdt.launching.CLASSPATH", classpathElements);
            GenerateEclipseRunTask.writeStringAttr(writer, "  ", "org.eclipse.jdt.launching.MAIN_TYPE", Objects.requireNonNull(this.getMainClass().get()));
            GenerateEclipseRunTask.writeListAttr(writer, "  ", "org.eclipse.jdt.launching.MODULEPATH", Collections.emptyList());
            GenerateEclipseRunTask.writeStringAttr(writer, "  ", "org.eclipse.jdt.launching.MODULE_NAME", Objects.requireNonNull(this.getModuleName().get()));
            GenerateEclipseRunTask.writeStringAttr(writer, "  ", "org.eclipse.jdt.launching.PROJECT_ATTR", Objects.requireNonNull(this.getProjectName().get()));
            GenerateEclipseRunTask.writeStringAttr(writer, "  ", "org.eclipse.jdt.launching.WORKING_DIRECTORY", Objects.requireNonNull(this.getWorkingDirPath().get()));
            GenerateEclipseRunTask.writeStringAttr(writer, "  ", "org.eclipse.jdt.launching.VM_ARGUMENTS", String.join(" ", Objects.requireNonNull(this.getJvmArgs().get())));
            writer.write("</launchConfiguration>");
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write to launch file", e);
        }
    }

    @Input
    public abstract ListProperty<CommandLineArgumentProvider> getArgs();

    @Input
    public abstract Property<JavaVersion> getJavaVersion();

    @Input
    public abstract ListProperty<String> getJvmArgs();

    @Inject
    protected abstract ProjectLayout getLayout();

    @Input
    public abstract Property<String> getMainClass();

    @Input
    public abstract Property<String> getModuleName();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @Input
    public abstract Property<String> getProjectName();

    @Inject
    protected abstract ProviderFactory getProviders();

    @Internal("Actual consumed input is #getWorkingDirPath()")
    public abstract DirectoryProperty getWorkingDir();

    @Input
    public abstract Property<String> getWorkingDirPath();
}
