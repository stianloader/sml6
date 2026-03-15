package org.stianloader.sml6.starplane.remapping;

import java.io.IOException;
import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;
import org.stianloader.sml6.starplane.remapping.MIOContainerFormat.MappingContainer;

import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.mappingio.tree.VisitableMappingTree;

public class MIOMappingTreeProvider {
    @NotNull
    private final MIOContainerFormat format;
    @NotNull
    private final Path path;

    public MIOMappingTreeProvider(@NotNull MappingFormat format, @NotNull Path path) {
        this.format = new MIOContainerFormat(format, MappingContainer.PLAIN);
        this.path = path;
    }

    public MIOMappingTreeProvider(@NotNull MIOContainerFormat format, @NotNull Path path) {
        this.format = format;
        this.path = path;
    }

    @NotNull
    public VisitableMappingTree get() throws IOException {
        VisitableMappingTree tree = new MemoryMappingTree();
        try {
            this.format.read(this.path, tree);
        } catch (IOException e) {
            throw new IOException("Unable to consume supplementary mappings file at " + this.path + " using format " + this.format.toString(), e);
        }
        tree.reset();
        return tree;
    }
}
