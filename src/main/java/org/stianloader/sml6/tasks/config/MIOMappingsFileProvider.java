package org.stianloader.sml6.tasks.config;

import java.io.IOException;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.jetbrains.annotations.NotNull;
import org.stianloader.sml6.starplane.remapping.MIOContainerFormat.MappingContainer;

import net.fabricmc.mappingio.tree.MemoryMappingTree;

public abstract class MIOMappingsFileProvider extends MIOMappingsProvider {

    @Input
    public abstract Property<MappingContainer> getContainerFormat();

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getMappingSource();

    @Override
    @NotNull
    public MemoryMappingTree loadTree() throws IOException {
        MemoryMappingTree visitor = new MemoryMappingTree();
        this.getContainerFormat().get().read(this.getMappingFormat().get(), this.getMappingSource().get().getAsFile().toPath(), visitor);
        return visitor;
    }
}
