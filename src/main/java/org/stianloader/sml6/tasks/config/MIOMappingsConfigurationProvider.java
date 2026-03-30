package org.stianloader.sml6.tasks.config;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.jetbrains.annotations.NotNull;
import org.stianloader.sml6.starplane.remapping.MIOContainerFormat.MappingContainer;

import net.fabricmc.mappingio.tree.MemoryMappingTree;

public abstract class MIOMappingsConfigurationProvider extends MIOMappingsProvider {
    @Input
    public abstract Property<MappingContainer> getContainerFormat();

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract Property<FileCollection> getMappingSource();

    @Inject
    protected abstract Project getProject();

    @Override
    @NotNull
    public MemoryMappingTree loadTree() throws IOException {
        MemoryMappingTree visitor = new MemoryMappingTree();

        this.getMappingSource().disallowChanges();

        for (File f : this.getMappingSource().get()) {
            if (f.isDirectory()) {
                throw new IOException("Cannot read mappings: File " + f + " is a directory.");
            }

            this.getContainerFormat().get().read(this.getMappingFormat().get(), f.toPath(), visitor);
        }

        return visitor;
    }
}
