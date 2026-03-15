package org.stianloader.sml6.starplane.remapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tukaani.xz.XZInputStream;

import net.fabricmc.mappingio.MappingFlag;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingUtil;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.ForwardingMappingVisitor;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.format.enigma.EnigmaFileReader;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

import kala.compress.archivers.tar.TarArchiveEntry;
import kala.compress.archivers.tar.TarArchiveInputStream;

public class MIOContainerFormat {
    private final static class ArchiveVisitor extends ForwardingMappingVisitor implements AutoCloseable {
        @NotNull
        private static MappingVisitor optionallyClone(@NotNull MappingVisitor visitor) {
            if (visitor.getFlags().contains(MappingFlag.NEEDS_ELEMENT_UNIQUENESS) || visitor.getFlags().contains(MappingFlag.NEEDS_MULTIPLE_PASSES)) {
                return new MemoryMappingTree();
            }
            return visitor;
        }

        private boolean clonedVisitor;
        @NotNull
        private MappingVisitor originalVisitor;
        @Nullable
        private Boolean visitContent = null;

        public ArchiveVisitor(@NotNull MappingVisitor visitor) throws IOException {
            super(ArchiveVisitor.optionallyClone(visitor));

            this.originalVisitor = visitor;
            this.clonedVisitor = visitor != this.next;

            if (this.visitHeader()) {
                this.visitNamespaces(MappingUtil.NS_SOURCE_FALLBACK, Collections.singletonList(MappingUtil.NS_TARGET_FALLBACK));
            }
        }

        @Override
        public void close() throws IOException {
            if (super.visitEnd()) {
                if (this.clonedVisitor) {
                    // visitEnd = true (final pass); cloned instance.
                    ((MappingTree) this.next).accept(this.originalVisitor);
                }
            } else if (this.clonedVisitor) {
                // visitEnd = false (not the final pass); cloned instance.
                ((MappingTree) this.next).accept(this.originalVisitor);
            } else {
                // visitEnd = false; no clone.
                throw new IllegalStateException("super.visitEnd() returned true without NEEDS_MULTIPLE_PASSES having been declared.");
            }
        }

        @Override
        public boolean visitContent() throws IOException {
            Boolean visitContent = this.visitContent;
            if (visitContent == null) {
                return this.visitContent = super.visitContent();
            }
            return visitContent;
        }

        @Override
        public boolean visitEnd() throws IOException {
            return true;
        }

        @Override
        public boolean visitHeader() throws IOException {
            return false;
        }
    }

    public static enum MappingContainer {
        PLAIN {
            @Override
            public void read(@NotNull MappingFormat format, @NotNull Path path, @NotNull MappingVisitor visitor) throws IOException {
                MappingReader.read(path, format, visitor);
            }
        },
        TAR_XZ {
            @Override
            public void read(@NotNull MappingFormat format, @NotNull Path path, @NotNull MappingVisitor visitor) throws IOException {
                if (format != MappingFormat.ENIGMA_DIR) {
                    throw new IllegalArgumentException("The TAR_XZ format is only supported for the ENIGMA_DIR format. Instead, the " + format + " is being used.");
                }

                try (ArchiveVisitor archiveVisitor = new ArchiveVisitor(visitor);
                        InputStream rawIn = Files.newInputStream(path);
                        XZInputStream decompressedIn = new XZInputStream(rawIn);
                        TarArchiveInputStream tarIn = new TarArchiveInputStream(decompressedIn);
                        InputStreamReader reader = new InputStreamReader(tarIn);
                        BufferedReader bufferedReader = new BufferedReader(reader)) {
                    TarArchiveEntry tarEntry;
                    while ((tarEntry = tarIn.getNextEntry()) != null) {
                        if (!tarEntry.getName().endsWith("." + MappingFormat.ENIGMA_FILE.fileExt)) {
                            continue; // Skip non-*.mapping files.
                        }
                        EnigmaFileReader.read(bufferedReader, visitor);
                    }
                }
            }
        },
        XZ {
            @Override
            public void read(@NotNull MappingFormat format, @NotNull Path path, @NotNull MappingVisitor visitor) throws IOException {
                if (format == MappingFormat.ENIGMA_DIR) {
                    throw new IllegalArgumentException("The XZ container format is not applicable to the " + format + " mapping format.");
                }

                try (InputStream rawIn = Files.newInputStream(path);
                        XZInputStream decompressedIn = new XZInputStream(rawIn);
                        InputStreamReader reader = new InputStreamReader(decompressedIn);
                        BufferedReader bufferedReader = new BufferedReader(reader)) {
                    MappingReader.read(bufferedReader, format, visitor);
                }
            }
        };

        public abstract void read(@NotNull MappingFormat format, @NotNull Path path, @NotNull MappingVisitor visitor) throws IOException;
    }

    @NotNull
    public final MappingContainer containerFormat;

    @NotNull
    public final MappingFormat coreFormat;

    public MIOContainerFormat(@NotNull MappingFormat coreFormat, @NotNull MappingContainer containerFormat) {
        this.coreFormat = coreFormat;
        this.containerFormat = containerFormat;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MIOContainerFormat) {
            MIOContainerFormat other = (MIOContainerFormat) obj;
            return this.containerFormat == other.containerFormat && this.coreFormat == other.coreFormat;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.containerFormat.hashCode() ^ this.coreFormat.hashCode();
    }

    public void read(@NotNull Path path, @NotNull MappingVisitor visitor) throws IOException {
        this.containerFormat.read(this.coreFormat, path, visitor);
    }

    @Override
    public String toString() {
        return "MIOContainerFormat[core = " + this.coreFormat + ", container = " + this.containerFormat + "]";
    }
}
