package org.stianloader.sml6.starplane.remapping;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.stianloader.remapper.MemberRef;
import org.stianloader.remapper.Remapper;
import org.stianloader.remapper.SimpleMappingLookup;

public class TinyV1MappingWriter extends SimpleMappingLookup implements AutoCloseable {

    private boolean closed = false;

    @NotNull
    private final Writer writer;

    public TinyV1MappingWriter(@NotNull Path output, @NotNull String sourceNS, @NotNull String dstNS) throws IOException {
        this(Files.newBufferedWriter(output, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE), sourceNS, dstNS);
    }

    public TinyV1MappingWriter(@NotNull Writer output, @NotNull String sourceNS, @NotNull String dstNS) throws IOException {
        this.writer = output;
        this.writer.append("v1\t").append(sourceNS).append('\t').append(dstNS).append('\n');
    }

    @Override
    public void close() throws IOException {
        this.closed = true;
        this.writer.close();
    }

    @Override
    @NotNull
    public TinyV1MappingWriter remapClass(@NotNull String srcName, @NotNull String dstName) {
        if (this.closed) {
            throw new IllegalStateException("Writer cannot accept write access anymore.");
        }

        try {
            this.writer
                .append("CLASS\t")
                .append(srcName)
                .append('\t')
                .append(dstName)
                .append('\n');
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write to output stream", e);
        }

        return (TinyV1MappingWriter) super.remapClass(srcName, dstName);
    }

    @Override
    @NotNull
    public TinyV1MappingWriter remapMember(@NotNull MemberRef srcRef, @NotNull String dstName) {
        if (this.closed) {
            throw new IllegalStateException("Writer cannot accept write access anymore.");
        }

        try {
            this.writer
                .append(srcRef.getDesc().charAt(0) == '(' ? "METHOD\t" : "FIELD\t")
                .append(srcRef.getOwner())
                .append('\t')
                .append(srcRef.getDesc())
                .append('\t')
                .append(srcRef.getName())
                .append('\t')
                .append(dstName)
                .append('\n');
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write to output stream", e);
        }

        return (TinyV1MappingWriter) super.remapMember(srcRef, dstName);
    }

    @Override
    @NotNull
    public TinyV1MappingWriter remapParameter(@NotNull String srcOwner, @NotNull String srcMethodName, @NotNull String srcDesc,
            int paramIndex, @NotNull String destParamName) {
        throw new UnsupportedOperationException("Tiny V1 does not support remapping parameters!");
    }

    /**
     * Fixes remapped {@link InnerClassNode} by remapping any child classes alongside their parent class,
     * even if only the parent class was remapped. Due to the potentially destructive properties of this action,
     * this method must be explicitly invoked for it to do anything.
     *
     * <p>More specifically if the class "OuterClass" is remapped to "RootClass", but "OuterClass$Inner" is not remapped,
     * then after {@link Remapper#remapNode(ClassNode, StringBuilder)} the classes "RootClass" and "OuterClass$Inner" will exist.
     * As the names contradict the relation given by their {@link InnerClassNode}, most decompilers will discard the
     * {@link InnerClassNode}.
     *
     * <p>To fix this issue, this method will look for such changes and renames the inner classes accordingly.
     *
     * <p>This method will only do anything if it is called before {@link Remapper#remapNode(ClassNode, StringBuilder)}
     * and will modify the internal collection of remapped classes.
     *
     * <p>The more {@link ClassNode ClassNodes} there are, the more efficient this method is at doing what it should do.
     *
     * <p>This method can be destructive as it will not check for collisions.
     *
     * <p>This variant of this method does not work correctly if the outer classes are not in the provided
     * set of class nodes.
     *
     * @param nodes A list of all {@link ClassNode} elements to remap.
     * @param sharedBuilder A shared {@link StringBuilder} to reduce memory consumption created by string operations.
     * @author Geolykt
     */
    public void synchronizeICNNames(@NotNull List<ClassNode> nodes, @NotNull StringBuilder sharedBuilder) {
        Map<@NotNull String, List<@NotNull String>> outerToInner = new TreeMap<>(); // TreeMap to guarantee stable mappings order

        for (ClassNode node : nodes) {
            int lastIndexOfDollar = node.name.lastIndexOf('$');
            if (lastIndexOfDollar != -1) {
                String outerName = node.name.substring(0, lastIndexOfDollar);
                outerToInner.compute(outerName, (key, oldVal) -> {
                    if (oldVal == null) {
                        oldVal = new ArrayList<>();
                    }
                    oldVal.add(Objects.requireNonNull(node.name));
                    return oldVal;
                });
            }
        }

        while (!outerToInner.isEmpty()) {
            int cycleChangeCount = 0;

            for (String oldName : new ArrayList<>(outerToInner.keySet())) {
                String newName = this.getRemappedClassNameFast(oldName);

                if (newName == null || newName == oldName) {
                    continue;
                }

                List<@NotNull String> innerClasses = outerToInner.remove(oldName);

                if (innerClasses != null) {
                    for (String inner : innerClasses) {
                        String dstName = this.getRemappedClassNameFast(inner);

                        if (dstName != null && dstName != inner) {
                            continue;
                        }

                        int seperatorPos = inner.lastIndexOf('$');
                        sharedBuilder.setLength(0);
                        sharedBuilder.append(newName);
                        sharedBuilder.append(inner, seperatorPos, inner.length());
                        this.remapClass(inner, sharedBuilder.toString());

                        cycleChangeCount++;
                    }
                }
            }

            if (cycleChangeCount == 0) {
                // We are done here
                break;
            }
        }
    }
}
