package org.stianloader.sml6.starplane;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.stianloader.micromixin.remapper.MemberLister;
import org.stianloader.remapper.MemberRef;
import org.stianloader.remapper.SimpleTopLevelLookup;

public class DebugableMemberLister implements MemberLister {
    private boolean debugging = false;
    @NotNull
    private final Map<String, ClassNode> libraryNodes;
    @NotNull
    private final SimpleTopLevelLookup topTevelLookup;

    public DebugableMemberLister(@NotNull SimpleTopLevelLookup topLevelLookup, @NotNull Map<String, ClassNode> libraryNodes) {
        this.topTevelLookup = topLevelLookup;
        this.libraryNodes = libraryNodes;
    }

    @Override
    @Nullable
    public Collection<MemberRef> getReportedClassMembers(@NotNull String owner) {
        ClassNode node = this.libraryNodes.get(owner);
        if (node == null) {
            return null;
        }

        List<MemberRef> collected = new ArrayList<>();
        for (MethodNode method : node.methods) {
            collected.add(new MemberRef(owner, method.name, method.desc));
        }
        for (FieldNode field : node.fields) {
            collected.add(new MemberRef(owner, field.name, field.desc));
        }
        return collected;
    }

    @Override
    public boolean hasMemberInHierarchy(@NotNull String clazz, @NotNull String name, @NotNull String desc) {
        if (this.debugging) {
            System.out.println("HMIH: " + clazz + "." + name + ":" + desc + "=" + (this.topTevelLookup.realmOf(new MemberRef(clazz, name, desc)) != null));
        }
        return this.topTevelLookup.realmOf(new MemberRef(clazz, name, desc)) != null;
    }

    public void setDebugging(boolean debugging) {
        this.debugging = debugging;
    }

    @Override
    @NotNull
    public Collection<MemberRef> tryInferMember(@NotNull String owner, @Nullable String name, @Nullable String desc) {
        ClassNode node = this.libraryNodes.get(owner);
        if (node == null) {
            return Collections.emptySet();
        }
        List<MemberRef> collected = new ArrayList<>();
        for (MethodNode method : node.methods) {
            if (name != null && !name.equals(method.name)) {
                continue;
            }
            if (desc != null && !desc.equals(method.desc)) {
                continue;
            }
            collected.add(new MemberRef(owner, method.name, method.desc));
        }
        for (FieldNode field : node.fields) {
            if (name != null && !name.equals(field.name)) {
                continue;
            }
            if (desc != null && !desc.equals(field.desc)) {
                continue;
            }
            collected.add(new MemberRef(owner, field.name, field.desc));
        }
        return collected;
    }
}
