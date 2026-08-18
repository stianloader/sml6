package org.stianloader.sml6.starplane.remapping;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stianloader.deobf.DescString;
import org.stianloader.remapper.MappingLookup;
import org.stianloader.remapper.MappingSink;
import org.stianloader.remapper.MemberRef;

import net.fabricmc.mappingio.tree.MappingTreeView;
import net.fabricmc.mappingio.tree.MappingTreeView.ClassMappingView;
import net.fabricmc.mappingio.tree.MappingTreeView.ElementMappingView;
import net.fabricmc.mappingio.tree.MappingTreeView.FieldMappingView;
import net.fabricmc.mappingio.tree.MappingTreeView.MethodArgMappingView;
import net.fabricmc.mappingio.tree.MappingTreeView.MethodMappingView;

public class ReadOnlyMIOMappingLookup implements MappingLookup, MappingSink, CommentLookup {
    @NotNull
    protected static String getName(@NotNull ElementMappingView element, int namespaceId) {
        String name = element.getName(namespaceId);

        if (name == null) {
            return Objects.requireNonNull(element.getSrcName());
        } else {
            return name;
        }
    }

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
        ClassMappingView cmv = this.getClassMappingView(className);

        return cmv == null ? null : cmv.getComment();
    }

    @Nullable
    protected ClassMappingView getClassMappingView(@NotNull String name) {
        if (this.srcNamespace == MappingTreeView.SRC_NAMESPACE_ID) {
            return this.mappingIOTree.getClass(name);
        }

        for (ClassMappingView cmv : this.mappingIOTree.getClasses()) {
            if (cmv == null) {
                continue;
            }

            if (ReadOnlyMIOMappingLookup.getName(cmv, this.srcNamespace).equals(name)) {
                return cmv;
            }
        }

        return null;
    }

    @Override
    @Nullable
    public String getFieldComment(@NotNull String srcOwner, @NotNull String srcName,
            @NotNull String srcDesc) {
        FieldMappingView fmv = this.getFieldMappingView(srcOwner, srcName, srcDesc);

        return fmv == null ? null : fmv.getComment();
    }

    @Nullable
    protected FieldMappingView getFieldMappingView(@NotNull String owner, @NotNull String name, @NotNull String descriptor) {
        if (this.srcNamespace == MappingTreeView.SRC_NAMESPACE_ID) {
            return this.mappingIOTree.getField(owner, name, descriptor);
        }

        ClassMappingView cmv = this.getClassMappingView(owner);

        if (cmv == null) {
            return null;
        }

        for (FieldMappingView fmv : cmv.getFields()) {
            if (fmv == null) {
                continue;
            }

            if (ReadOnlyMIOMappingLookup.getName(fmv, this.srcNamespace).equals(name)) {
                return fmv;
            }
        }

        return null;
    }

    @Override
    @Nullable
    public String getMethodComment(@NotNull String srcOwner, @NotNull String srcName, @NotNull String srcDesc) {
        MethodMappingView mmv = this.getMethodMappingView(srcOwner, srcName, srcDesc);

        return mmv == null ? null : mmv.getComment();
    }

    @Nullable
    protected MethodMappingView getMethodMappingView(@NotNull String owner, @NotNull String name, @NotNull String descriptor) {
        if (this.srcNamespace == MappingTreeView.SRC_NAMESPACE_ID) {
            return this.mappingIOTree.getMethod(owner, name, descriptor);
        }

        ClassMappingView cmv = this.getClassMappingView(owner);

        if (cmv == null) {
            return null;
        }

        for (MethodMappingView mmv : cmv.getMethods()) {
            if (mmv == null) {
                continue;
            }

            if (ReadOnlyMIOMappingLookup.getName(mmv, this.srcNamespace).equals(name)) {
                return mmv;
            }
        }

        return null;
    }

    @Override
    @Nullable
    public String getParameterComment(@NotNull String srcOwner, @NotNull String srcName,
            @NotNull String srcDesc, int paramIndex, boolean isStatic) {
        MethodMappingView mmv = this.getMethodMappingView(srcOwner, srcName, srcDesc);

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

    @Override
    @NotNull
    public String getRemappedClassName(@NotNull String srcName) {
        ClassMappingView cmv = this.getClassMappingView(srcName);

        if (cmv != null) {
            return ReadOnlyMIOMappingLookup.getName(cmv, this.dstNamespace);
        }

        return srcName;
    }

    @Override
    @Nullable
    public String getRemappedClassNameFast(@NotNull String srcName) {
        ClassMappingView cmv = this.getClassMappingView(srcName);

        if (cmv != null) {
            return ReadOnlyMIOMappingLookup.getName(cmv, this.dstNamespace);
        }

        return null;
    }

    @Override
    @NotNull
    public String getRemappedFieldName(@NotNull String srcOwner, @NotNull String srcName, @NotNull String srcDesc) {
        FieldMappingView fmv = this.getFieldMappingView(srcOwner, srcName, srcDesc);

        if (fmv == null) {
            return srcName;
        }

        return ReadOnlyMIOMappingLookup.getName(fmv, this.dstNamespace);
    }

    @Override
    @NotNull
    public String getRemappedMethodName(@NotNull String srcOwner, @NotNull String srcName, @NotNull String srcDesc) {
        MethodMappingView mmv = this.getMethodMappingView(srcOwner, srcName, srcDesc);

        if (mmv == null) {
            return srcName;
        }

        return ReadOnlyMIOMappingLookup.getName(mmv, this.dstNamespace);
    }

    @Override
    @Nullable
    public String getRemappedParameterName(@NotNull String srcOwner, @NotNull String srcName, @NotNull String srcDesc, int paramIndex, boolean isStatic) {
        MethodMappingView mmv = this.getMethodMappingView(srcOwner, srcName, srcDesc);

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
                    return ReadOnlyMIOMappingLookup.getName(arg, this.dstNamespace);
                }
            } else if (argPos == paramIndex) {
                return ReadOnlyMIOMappingLookup.getName(arg, this.dstNamespace);
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
}
