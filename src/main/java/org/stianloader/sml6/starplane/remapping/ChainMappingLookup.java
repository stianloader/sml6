package org.stianloader.sml6.starplane.remapping;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stianloader.remapper.MappingLookup;
import org.stianloader.remapper.Remapper;

public class ChainMappingLookup implements MappingLookup, CommentLookup {

    private boolean debugMode = false;
    @NotNull
    private final MappingLookup @NotNull[] lookupDelegates;

    public ChainMappingLookup(@NotNull MappingLookup @NotNull... lookups) {
        this.lookupDelegates = lookups;
    }

    @NotNull
    @Contract(pure = false, mutates = "this", value = "_ -> this")
    public ChainMappingLookup enableDebugMode(boolean debug) {
        this.debugMode = debug;
        return this;
    }

    @Override
    @Nullable
    public String getClassComment(@NotNull String srcName) {
        String lastComment = null;
        for (MappingLookup lookup : this.lookupDelegates) {
            if (lookup instanceof CommentLookup) {
                String comment = ((CommentLookup) lookup).getClassComment(srcName);
                if (comment != null) {
                    lastComment = comment;
                }
            }
            srcName = lookup.getRemappedClassName(srcName);
        }
        return lastComment;
    }

    @Override
    @Nullable
    public String getFieldComment(@NotNull String srcOwner, @NotNull String srcName, @NotNull String srcDesc) {
        StringBuilder descBuilder = new StringBuilder();
        String lastComment = null;
        for (MappingLookup lookup : this.lookupDelegates) {
            if (lookup instanceof CommentLookup) {
                String comment = ((CommentLookup) lookup).getFieldComment(srcOwner, srcName, srcDesc);
                if (comment != null) {
                    lastComment = comment;
                }
            }
            if (this.debugMode) {
                String mappingName = lookup.toString();
                if (mappingName.length() < 64) {
                    mappingName += " ".repeat(64 - mappingName.length());
                }
                System.out.println("\tf\t" + lookup.toString() + "\t" + srcOwner + '.' + srcName + ' ' + srcDesc);
            }
            srcName = lookup.getRemappedFieldName(srcOwner, srcName, srcDesc);
            srcDesc = Remapper.getRemappedFieldDescriptor(lookup, srcDesc, descBuilder);
            srcOwner = lookup.getRemappedClassName(srcOwner);
        }

        if (this.debugMode) {
            System.out.println("\t-\t" + ".".repeat(64) + "\t" + srcOwner + '.' + srcName + ' '  + srcDesc + "\n");
        }

        return lastComment;
    }

    @Override
    public @Nullable String getMethodComment(@NotNull String srcOwner, @NotNull String srcName, @NotNull String  srcDesc) {
        StringBuilder descBuilder = new StringBuilder();
        String lastComment = null;
        for (MappingLookup lookup : this.lookupDelegates) {
            if (lookup instanceof CommentLookup) {
                String comment = ((CommentLookup) lookup).getMethodComment(srcOwner, srcName, srcDesc);
                if (comment != null) {
                    lastComment = comment;
                }
            }
            if (this.debugMode) {
                String mappingName = lookup.toString();
                if (mappingName.length() < 64) {
                    mappingName += " ".repeat(64 - mappingName.length());
                }
                System.out.println("\tm\t" + lookup.toString() + "\t" + srcOwner + '.' + srcName + srcDesc);
            }
            srcName = lookup.getRemappedMethodName(srcOwner, srcName, srcDesc);
            srcDesc = Remapper.getRemappedMethodDescriptor(lookup, srcDesc, descBuilder);
            srcOwner = lookup.getRemappedClassName(srcOwner);
        }
        if (this.debugMode) {
            System.out.println("\t-\t" + ".".repeat(64) + "\t" + srcOwner + '.' + srcName + srcDesc + "\n");
        }
         return lastComment;
    }

    @Override
    @Nullable
    public String getParameterComment(@NotNull String srcOwner, @NotNull String srcName, @NotNull String srcDesc, int paramIndex, boolean isStatic) {
        StringBuilder descBuilder = new StringBuilder();

        String lastComment = null;

        for (MappingLookup lookup : this.lookupDelegates) {
            if (lookup instanceof CommentLookup) {
                String comment = ((CommentLookup) lookup).getParameterComment(srcOwner, srcName, srcDesc, paramIndex, isStatic);

                if (comment != null) {
                    lastComment = comment;
                }
            }

            srcName = lookup.getRemappedMethodName(srcOwner, srcName, srcDesc);
            srcDesc = Remapper.getRemappedMethodDescriptor(lookup, srcDesc, descBuilder);
            srcOwner = lookup.getRemappedClassName(srcOwner);
        }

         return lastComment;
    }

    @Override
    @NotNull
    public String getRemappedClassName(@NotNull String srcName) {
        for (MappingLookup lookup : this.lookupDelegates) {
            srcName = lookup.getRemappedClassName(srcName);
        }
         return srcName;
    }

    @Override
    @NotNull
    public String getRemappedFieldName(@NotNull String srcOwner, @NotNull String srcName, @NotNull String srcDesc) {
        StringBuilder descBuilder = new StringBuilder();
        for (MappingLookup lookup : this.lookupDelegates) {
            if (this.debugMode) {
                String mappingName = lookup.toString();
                if (mappingName.length() < 64) {
                    mappingName += " ".repeat(64 - mappingName.length());
                }
                System.out.println("\tf\t" + lookup.toString() + "\t" + srcOwner + '.' + srcName + ' ' + srcDesc);
            }
            srcName = lookup.getRemappedFieldName(srcOwner, srcName, srcDesc);
            srcDesc = Remapper.getRemappedFieldDescriptor(lookup, srcDesc, descBuilder);
            srcOwner = lookup.getRemappedClassName(srcOwner);
        }

        if (this.debugMode) {
            System.out.println("\t-\t" + ".".repeat(64) + "\t" + srcOwner + '.' + srcName + ' '  + srcDesc + "\n");
        }

        return srcName;
    }

    @Override
    @NotNull
    public String getRemappedMethodName(@NotNull String srcOwner, @NotNull String srcName, @NotNull String srcDesc) {
        StringBuilder descBuilder = new StringBuilder();
        for (MappingLookup lookup : this.lookupDelegates) {
            if (this.debugMode) {
                String mappingName = lookup.toString();
                if (mappingName.length() < 64) {
                    mappingName += " ".repeat(64 - mappingName.length());
                }
                System.out.println("\tm\t" + lookup.toString() + "\t" + srcOwner + '.' + srcName + srcDesc);
            }
            srcName = lookup.getRemappedMethodName(srcOwner, srcName, srcDesc);
            srcDesc = Remapper.getRemappedMethodDescriptor(lookup, srcDesc, descBuilder);
            srcOwner = lookup.getRemappedClassName(srcOwner);
        }
        if (this.debugMode) {
            System.out.println("\t-\t" + ".".repeat(64) + "\t" + srcOwner + '.' + srcName + srcDesc + "\n");
        }
         return srcName;
    }

    @Override
    @Nullable
    public String getRemappedParameterName(@NotNull String srcOwner, @NotNull String srcName, @NotNull String srcDesc, int paramIndex, boolean isStatic) {
        StringBuilder descBuilder = new StringBuilder();

        String paramName = null;

        for (MappingLookup lookup : this.lookupDelegates) {
            if (this.debugMode) {
                String mappingName = lookup.toString();
                if (mappingName.length() < 64) {
                    mappingName += " ".repeat(64 - mappingName.length());
                }
                System.out.println("\tp\t" + lookup.toString() + "\t" + srcOwner + '.' + srcName + srcDesc + " -> " + paramName + "@" + paramIndex);
            }

            String v = lookup.getRemappedParameterName(srcOwner, srcName, srcDesc, paramIndex, isStatic);

            if (v != null) {
                paramName = v;
            }

            srcName = lookup.getRemappedMethodName(srcOwner, srcName, srcDesc);
            srcDesc = Remapper.getRemappedMethodDescriptor(lookup, srcDesc, descBuilder);
            srcOwner = lookup.getRemappedClassName(srcOwner);
        }

        if (this.debugMode) {
            System.out.println("\t-\t" + ".".repeat(64) + "\t" + srcOwner + '.' + srcName + srcDesc + " -> " + paramName + "@" + paramIndex + "\n");
        }

         return srcName;
    }
}
