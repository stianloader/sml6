package org.stianloader.sml6.starplane.remapping;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stianloader.remapper.MappingLookup;
import org.stianloader.remapper.MappingSink;
import org.stianloader.remapper.MemberRef;

public class ReadOnlyMappingLookupSink implements MappingLookup, MappingSink {

    @NotNull
    private final MappingLookup lookupDelegate;

    public ReadOnlyMappingLookupSink(@NotNull MappingLookup lookupDelegate) {
        this.lookupDelegate = lookupDelegate;
    }

    @Override
    @NotNull
    public String getRemappedClassName(@NotNull String srcName) {
        return this.lookupDelegate.getRemappedClassName(srcName);
    }

    @Override
    @Nullable
    public String getRemappedClassNameFast(@NotNull String srcName) {
        return this.lookupDelegate.getRemappedClassNameFast(srcName);
    }

    @Override
    @NotNull
    public String getRemappedFieldName(@NotNull String srcOwner, @NotNull String srcName, @NotNull String srcDesc) {
        return this.lookupDelegate.getRemappedFieldName(srcOwner, srcName, srcDesc);
    }

    @Override
    @NotNull
    public String getRemappedMethodName(@NotNull String srcOwner, @NotNull String srcName, @NotNull String srcDesc) {
        return this.lookupDelegate.getRemappedMethodName(srcOwner, srcName, srcDesc);
    }

    @Override
    @NotNull
    public ReadOnlyMappingLookupSink remapClass(@NotNull String srcName, @NotNull String dstName) {
        throw new UnsupportedOperationException("Mutation is not permitted.");
    }

    @Override
    @NotNull
    public ReadOnlyMappingLookupSink remapMember(@NotNull MemberRef srcRef, @NotNull String dstName) {
        throw new UnsupportedOperationException("Mutation is not permitted.");
    }
}
