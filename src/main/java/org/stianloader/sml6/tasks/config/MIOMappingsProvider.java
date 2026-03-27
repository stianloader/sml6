package org.stianloader.sml6.tasks.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.internal.metaobject.PropertyAccess;
import org.gradle.internal.metaobject.PropertyMixIn;
import org.jetbrains.annotations.NotNull;

import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MappingTree.ClassMapping;
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

    @NotNull
    public MemoryMappingTree loadAndValidateTree() throws IOException {
        MemoryMappingTree tree = this.loadTree();

        int srcNamespaceId = this.getSrcNamespaceId().get();
        int dstNamespaceId = this.getDstNamespaceId().get();

        Set<String> mioSrcNames = new HashSet<>();
        Set<String> srcNames = new HashSet<>();
        Map<String, String> dstNames = new HashMap<>();

        for (ClassMapping classMapping : tree.getClasses()) {
            if (classMapping == null) {
                throw new IOException("Mapping tree contains null class mapping");
            }

            String srcName = srcNamespaceId == MappingTreeView.SRC_NAMESPACE_ID ? classMapping.getSrcName() : classMapping.getDstName(srcNamespaceId);
            String dstName = dstNamespaceId == MappingTreeView.SRC_NAMESPACE_ID ? classMapping.getSrcName() : classMapping.getDstName(dstNamespaceId);

            if (!mioSrcNames.add(classMapping.getSrcName())) {
                throw new IOException("Mapping tree contains duplicate MIO source name: " + classMapping.getSrcName());
            } else if (!srcNames.add(srcName)) {
                throw new IOException("Mapping tree contains duplicate source name: " + srcName);
            } else if (dstNames.putIfAbsent(dstName, srcName) != null) {
                throw new IOException("Mapping tree contains duplicate destination name: " + dstName + ", already mapped by " + dstNames.get(dstName) + ", also mapped by " + srcName);
            }
        }

        return tree;
    }
}
