package org.stianloader.sml6;

import java.io.File;
import java.util.Date;

import javax.inject.Inject;

import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.internal.tasks.TaskDependencyFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.work.DisableCachingByDefault;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

@ApiStatus.NonExtendable
@DisableCachingByDefault(because = "Abstract super-class, not to be instantiated directly")
public abstract class AbstractArtifactTask extends ConventionTask {
    protected static class AbstractArtifactTaskPublishArtifact implements PublishArtifact {
        private final TaskDependency dependencies;
        @NotNull
        private final AbstractArtifactTask task;
        @Inject
        public AbstractArtifactTaskPublishArtifact(@NotNull TaskDependencyFactory factory, @NotNull AbstractArtifactTask task) {
            this.dependencies = factory.configurableDependency().add(task);
            this.task = task;
        }

        @Override
        public TaskDependency getBuildDependencies() {
            return this.dependencies;
        }

        @Override
        @Nullable
        public String getClassifier() {
            return this.task.getArchiveClassifier().getOrNull();
        }

        @Override
        @Nullable
        public Date getDate() {
            return this.task.getArchiveDate().getOrNull();
        }

        @Override
        public String getExtension() {
            return this.task.getArchiveExtension().get();
        }

        @Override
        public File getFile() {
            return this.task.getArchiveFile().get().getAsFile();
        }

        @Override
        public String getName() {
            return this.task.getArchiveBaseName().get();
        }

        @Override
        public String getType() {
            return this.task.getArchiveExtension().get();
        }
    }

    public AbstractArtifactTask() {
        this.getArchiveBaseName().convention(GradleUtilities.getBaseArchiveName(this.getProject()));
        this.getDestinationDirectory().convention(GradleUtilities.getDistsDirectory(this.getProject()));
        this.getArchiveVersion().convention(GradleUtilities.getVersion(this.getProject()));
        this.getArchiveDate().convention(this.getArchiveFile().getAsFile().map(f -> new Date(f.lastModified())));

        this.getArchiveExtension().convention("");
        this.getArchiveAppendix().convention("");
        this.getArchiveClassifier().convention("");

        Provider<String> defaultFilenameProvider = this.getArchiveBaseName()
                .zip(this.getArchiveAppendix(), GradleUtilities::appendFilenamePart)
                .zip(this.getArchiveVersion(), GradleUtilities::appendFilenamePart)
                .zip(this.getArchiveClassifier(), GradleUtilities::appendFilenamePart)
                .zip(this.getArchiveExtension(), GradleUtilities::appendFileExtension);
        this.getArchiveFileName().convention(defaultFilenameProvider);
        this.getArchiveFile().convention(this.getDestinationDirectory().file(this.getArchiveFileName()));
    }

    @NotNull
    public PublishArtifact asArtifact() {
        return this.getProject().getObjects().newInstance(AbstractArtifactTaskPublishArtifact.class, this);
    }

    @Internal("getArchiveFile() is the actual output")
    public abstract Property<String> getArchiveAppendix();

    @Internal("getArchiveFile() is the actual output")
    public abstract Property<String> getArchiveBaseName();

    @Internal("getArchiveFile() is the actual output")
    public abstract Property<String> getArchiveClassifier();

    @Internal("Transitively depends on getArchiveFile()")
    public abstract Property<Date> getArchiveDate();

    @Internal("getArchiveFile() is the actual output")
    public abstract Property<String> getArchiveExtension();

    @OutputFile
    public abstract RegularFileProperty getArchiveFile();

    @Internal("getArchiveFile() is the actual output")
    public abstract Property<String> getArchiveFileName();

    @Internal("getArchiveFile() is the actual output")
    public abstract Property<String> getArchiveVersion();

    @Internal("getArchiveFile() is the actual output")
    public abstract DirectoryProperty getDestinationDirectory();
}
