package org.stianloader.sml6.tasks.config;

import java.io.IOException;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.internal.metaobject.PropertyAccess;
import org.gradle.internal.metaobject.PropertyMixIn;
import org.jetbrains.annotations.NotNull;

import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MappingTreeView;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public abstract class MIOMappingsProvider implements PropertyMixIn {
    public MIOMappingsProvider() {
        this.getSrcNamespaceId().convention(MappingTreeView.SRC_NAMESPACE_ID);
        this.getDstNamespaceId().convention(0);
    }

    @Override
    @Internal(value = "gradle sugar")
    public PropertyAccess getAdditionalProperties() {
        return MappingExtraProperties.INSTANCE;
    }

    @Optional
    @Input
    public abstract Property<Integer> getDstNamespaceId();

    @Input
    public abstract Property<@NotNull MappingFormat> getMappingFormat();

    @Optional
    @Input
    public abstract Property<Integer> getSrcNamespaceId();

    @NotNull
    public abstract MemoryMappingTree loadTree() throws IOException;
}
