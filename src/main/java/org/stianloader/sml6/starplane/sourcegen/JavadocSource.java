package org.stianloader.sml6.starplane.sourcegen;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructField;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.attr.StructLocalVariableTableAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructLocalVariableTableAttribute.LocalVariable;
import org.objectweb.asm.Opcodes;
import org.slf4j.LoggerFactory;
import org.stianloader.deobf.DescString;
import org.stianloader.sml6.starplane.remapping.CommentLookup;

import net.fabricmc.fernflower.api.IFabricJavadocProvider;

public class JavadocSource implements IFabricJavadocProvider {

    @NotNull
    private final CommentLookup commentLookup;

    public JavadocSource(@NotNull CommentLookup lookup) {
        this.commentLookup = lookup;
    }

    @Override
    public String getClassDoc(StructClass structClass) {
        return this.commentLookup.getClassComment(Objects.requireNonNull(structClass.qualifiedName));
    }

    @Override
    public String getFieldDoc(StructClass structClass, StructField structField) {
        return this.commentLookup.getFieldComment(Objects.requireNonNull(structClass.qualifiedName), Objects.requireNonNull(structField.getName()), Objects.requireNonNull(structField.getDescriptor()));
    }

    @Override
    public String getMethodDoc(StructClass structClass, StructMethod structMethod) {
        boolean isStatic = (structMethod.getAccessFlags() & Opcodes.ACC_STATIC) != 0;
        String owner = Objects.requireNonNull(structClass.qualifiedName);
        String name = Objects.requireNonNull(structMethod.getName());
        String descriptor = Objects.requireNonNull(structMethod.getDescriptor());

        String comment = this.commentLookup.getMethodComment(owner, name, descriptor);

        int lvIndex = isStatic ? 0 : 1;

        DescString dString = new DescString(descriptor);

        StructLocalVariableTableAttribute lvt = structMethod.getLocalVariableAttr();

        for (int i = 0; dString.hasNext(); i++) {
            String paramType = dString.nextType();

            String paramComment = this.commentLookup.getParameterComment(owner, name, descriptor, i, isStatic);

            if (paramComment != null) {
                String paramName;

                if (lvt == null) {
                    paramName = "arg" + lvIndex;
                } else {
                    paramName = lvt.matchingVars(lvIndex).min((lv1, lv2) -> Integer.compare(lv1.getStart(), lv2.getStart())).map(LocalVariable::getName).orElse(null);

                    if (paramName == null) {
                        LoggerFactory.getLogger(JavadocSource.class).warn("Param name not found for {}.{}:{} {}/{} - varversion pairs: {}", owner, name, descriptor, i, lvIndex, lvt.getMapNames());
                        paramName = "arg" + lvIndex;
                    }
                }

                if (comment == null) {
                    comment = "@param " + paramName + " " + paramComment;
                } else {
                    comment = comment + "\n@param " + paramName + " " + paramComment;
                }
            }

            lvIndex += (paramType.equals("J") || paramType.equals("D")) ? 2 : 1;
        }

        return comment;
    }
}
