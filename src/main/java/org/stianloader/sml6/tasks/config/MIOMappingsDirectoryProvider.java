package org.stianloader.sml6.tasks.config;

import java.io.IOException;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;
import org.jetbrains.annotations.NotNull;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public abstract class MIOMappingsDirectoryProvider extends MIOMappingsProvider {

    @InputDirectory
    public abstract DirectoryProperty getMappingSource();

    @Override
    @NotNull
    public MemoryMappingTree loadTree() throws IOException {
        MemoryMappingTree visitor = new MemoryMappingTree();
        MappingReader.read(this.getMappingSource().get().getAsFile().toPath(), this.getMappingFormat().get(), visitor);
        return visitor;
    }
}
