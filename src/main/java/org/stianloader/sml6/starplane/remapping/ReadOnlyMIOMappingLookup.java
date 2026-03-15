package org.stianloader.sml6.starplane.remapping;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stianloader.deobf.DescString;
import org.stianloader.remapper.MappingLookup;
import org.stianloader.remapper.MappingSink;
import org.stianloader.remapper.MemberRef;

import net.fabricmc.mappingio.tree.MappingTreeView;
import net.fabricmc.mappingio.tree.MappingTreeView.ClassMappingView;
import net.fabricmc.mappingio.tree.MappingTreeView.FieldMappingView;
import net.fabricmc.mappingio.tree.MappingTreeView.MethodArgMappingView;
import net.fabricmc.mappingio.tree.MappingTreeView.MethodMappingView;

public class ReadOnlyMIOMappingLookup implements MappingLookup, MappingSink, CommentLookup {
    private final int dstNamespace;
    @NotNull
    private final MappingTreeView mappingIOTree;
    private final int srcNamespace;

    public ReadOnlyMIOMappingLookup(@NotNull MappingTreeView mappingIOTree, int srcNamespace, int dstNamespace) {
        this(mappingIOTree, srcNamespace, dstNamespace, false);
    }

    public ReadOnlyMIOMappingLookup(@NotNull MappingTreeView mappingIOTree, int srcNamespace, int dstNamespace, boolean allowNOPMappings) {
        this.mappingIOTree = mappingIOTree;
        this.srcNamespace = srcNamespace;
        this.dstNamespace = dstNamespace;

        if (this.srcNamespace == this.dstNamespace && !allowNOPMappings) {
            throw new IllegalArgumentException("srcNamespace == dstNamespace: " + srcNamespace + ", " + dstNamespace);
        }
    }

    @Override
    @Nullable
    public String getClassComment(@NotNull String className) {
        ClassMappingView cmv = this.mappingIOTree.getClass(className, this.srcNamespace);
        return cmv == null ? null : cmv.getComment();
    }

    @Override
    @Nullable
    public String getFieldComment(@NotNull String srcOwner, @NotNull String srcName,
            @NotNull String srcDesc) {
        FieldMappingView fmv = this.mappingIOTree.getField(srcOwner, srcName, srcDesc, this.srcNamespace);
        return fmv == null ? null : fmv.getComment();
    }

    @Override
    @Nullable
    public String getMethodComment(@NotNull String srcOwner, @NotNull String srcName, @NotNull String srcDesc) {
        MethodMappingView mmv = this.mappingIOTree.getMethod(srcOwner, srcName, srcDesc, this.srcNamespace);
        return mmv == null ? null : mmv.getComment();
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
    @Nullable
    public String getRemappedParameterName(@NotNull String srcOwner, @NotNull String srcName, @NotNull String srcDesc, int paramIndex, boolean isStatic) {
        MethodMappingView mmv = this.mappingIOTree.getMethod(srcOwner, srcName, srcDesc, this.srcNamespace);
//        System.out.println(srcOwner + "; " + srcName + "; " + srcDesc);
//        System.out.println("mmv: " + mmv);

        if (mmv == null) {
            return null;
        }

        int lvIndex = -1;

        for (MethodArgMappingView arg : mmv.getArgs()) {
            assert arg != null;
            
            int argPos = arg.getArgPosition();

            if (argPos < 0) {
                if (lvIndex < 0) {
                    // convert paramIndex to lvIndex
                    lvIndex = isStatic ? 0 : 1;
                    int i = paramIndex;
                    DescString dString = new DescString(srcDesc);

                    while (i-- != 0 && dString.hasNext()) {
                        String type = dString.nextType();

                        if (type.equals("J") || type.equals("D")) {
                            lvIndex += 2;
                        } else {
                            lvIndex++;
                        }
                    }
                }

                if (lvIndex == arg.getLvIndex()) {
//                    System.out.println(arg.getArgPosition() + "; " + arg.getComment() + "; " + arg.getLvIndex() + ";" + arg.getName(this.dstNamespace) + ";@" + paramIndex + "/" + lvIndex);
                    return arg.getName(this.dstNamespace);
                }
            } else if (argPos == paramIndex) {
                return arg.getName(this.dstNamespace);
            }
        }

        return null;
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

    @Override
    @NotNull
    public ReadOnlyMIOMappingLookup remapParameter(@NotNull String srcOwner, @NotNull String srcMethodName, @NotNull String srcDesc,
            int paramIndex, @NotNull String destParamName) {
        throw new UnsupportedOperationException("Due to the complexities involved in the mapping process, this instance is read-only and only implements MappingSink for technical reasons");
    }

    @Override
    @Nullable
    public String getParameterComment(@NotNull String srcOwner, @NotNull String srcName,
            @NotNull String srcDesc, int paramIndex, boolean isStatic) {
        MethodMappingView mmv = this.mappingIOTree.getMethod(srcOwner, srcName, srcDesc, this.srcNamespace);

        if (mmv == null) {
            return null;
        }

        int lvIndex = -1;

        for (MethodArgMappingView arg : mmv.getArgs()) {
            assert arg != null;
            
            int argPos = arg.getArgPosition();

            if (argPos < 0) {
                if (lvIndex < 0) {
                    // convert paramIndex to lvIndex
                    lvIndex = isStatic ? 0 : 1;
                    int i = paramIndex;
                    DescString dString = new DescString(srcDesc);

                    while (i-- != 0 && dString.hasNext()) {
                        String type = dString.nextType();

                        if (type.equals("J") || type.equals("D")) {
                            lvIndex += 2;
                        } else {
                            lvIndex++;
                        }
                    }
                }

                if (lvIndex == arg.getLvIndex()) {
                    return arg.getComment();
                }
            } else if (argPos == paramIndex) {
                return arg.getComment();
            }
        }

        return null;
    }
}
