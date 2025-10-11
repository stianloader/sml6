package org.stianloader.sml6.starplane.remapping;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stianloader.remapper.MappingLookup;
import org.stianloader.remapper.MappingSink;
import org.stianloader.remapper.MemberRef;

import net.fabricmc.mappingio.tree.MappingTreeView;
import net.fabricmc.mappingio.tree.MappingTreeView.ClassMappingView;
import net.fabricmc.mappingio.tree.MappingTreeView.FieldMappingView;
import net.fabricmc.mappingio.tree.MappingTreeView.MethodMappingView;

public class ReadOnlyMIOMappingLookup implements MappingLookup, MappingSink {
    private final int dstNamespace;
    @NotNull
    private final MappingTreeView mappingIOTree;
    private final int srcNamespace;

    public ReadOnlyMIOMappingLookup(@NotNull MappingTreeView mappingIOTree, int srcNamespace, int dstNamespace) {
        this.mappingIOTree = mappingIOTree;
        this.srcNamespace = srcNamespace;
        this.dstNamespace = dstNamespace;
        if (this.srcNamespace == this.dstNamespace) {
            throw new IllegalArgumentException("srcNamespace == dstNamespace: " + srcNamespace + ", " + dstNamespace);
        }
    }

    @Override
    @NotNull
    public String getRemappedClassName(@NotNull String srcName) {
        String dst = this.getRemappedClassNameFast(srcName);
        return dst == null ? srcName : dst;
    }

    @Override
    @Nullable
    public String getRemappedClassNameFast(@NotNull String srcName) {
        ClassMappingView cmv = this.mappingIOTree.getClass(srcName, this.srcNamespace);
        if (cmv != null) {
            return cmv.getName(this.dstNamespace);
        }
        return null;
    }

    @Override
    @NotNull
    public String getRemappedFieldName(@NotNull String srcOwner, @NotNull String srcName, @NotNull String srcDesc) {
        FieldMappingView fmv = this.mappingIOTree.getField(srcOwner, srcName, srcDesc, this.srcNamespace);
        if (fmv == null) {
            return srcName;
        }
        String dst = fmv.getName(this.dstNamespace);
        return dst == null ? srcName : dst;
    }

    @Override
    @NotNull
    public String getRemappedMethodName(@NotNull String srcOwner, @NotNull String srcName, @NotNull String srcDesc) {
        MethodMappingView mmv = this.mappingIOTree.getMethod(srcOwner, srcName, srcDesc, this.srcNamespace);
        if (mmv == null) {
            return srcName;
        }
        String dst = mmv.getName(this.dstNamespace);
        return dst == null ? srcName : dst;
    }

    @Override
    @NotNull
    public ReadOnlyMIOMappingLookup remapClass(@NotNull String srcName, @NotNull String dstName) {
        throw new UnsupportedOperationException("Due to the complexities involved in the mapping process, this instance is read-only and only implements MappingSink for technical reasons");
    }

    @Override
    @NotNull
    public ReadOnlyMIOMappingLookup remapMember(@NotNull MemberRef srcRef, @NotNull String dstName) {
        throw new UnsupportedOperationException("Due to the complexities involved in the mapping process, this instance is read-only and only implements MappingSink for technical reasons");
    }
}
