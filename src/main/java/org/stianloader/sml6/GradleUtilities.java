package org.stianloader.sml6;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;
import java.util.zip.ZipEntry;

import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.internal.file.archive.TarCopyAction;
import org.gradle.api.internal.file.archive.compression.ArchiveOutputStreamFactory;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.plugins.BasePluginExtension;
import org.gradle.api.provider.Provider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public class GradleUtilities {

    @NotNull
    private static final MethodHandle MH_TAR_COPY_ACTION_CTOR;

    static {
        MethodHandle tarCopyActionCtor;
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        try {
            tarCopyActionCtor = lookup.findConstructor(TarCopyAction.class, MethodType.methodType(void.class, File.class, ArchiveOutputStreamFactory.class, boolean.class, Provider.class));
        } catch (ReflectiveOperationException e1) {
            try {
                tarCopyActionCtor = lookup.findConstructor(TarCopyAction.class, MethodType.methodType(void.class, File.class, ArchiveOutputStreamFactory.class, boolean.class));
                tarCopyActionCtor = MethodHandles.dropArguments(tarCopyActionCtor, 3, Provider.class);
            } catch (ReflectiveOperationException e2) {
                e2.addSuppressed(e1);

                throw new RuntimeException("Unable to initialize compatibility shims", e2);
            }
        }

        MH_TAR_COPY_ACTION_CTOR = tarCopyActionCtor;
    }

    public static String appendFileExtension(String filename, String extension) {
        if (extension.isEmpty()) {
            return filename;
        }
        return filename + "." + extension;
    }

    public static String appendFilenamePart(String currentPart, String appendPart) {
        if (appendPart.isEmpty()) {
            return currentPart;
        }
        return currentPart + "-" + appendPart;
    }

    @NotNull
    @Contract(pure = true)
    public static ZipEntry copyEntry(@NotNull ZipEntry entry) {
        return GradleUtilities.copyEntry(entry, entry.getName());
    }

    @NotNull
    @Contract(pure = true)
    public static ZipEntry copyEntry(@NotNull ZipEntry entry, @NotNull String name) {
        ZipEntry copyEntry = new ZipEntry(name);
        copyEntry.setComment(entry.getComment());
        java.util.Optional.ofNullable(entry.getCreationTime()).ifPresent(copyEntry::setCreationTime);
        copyEntry.setExtra(entry.getExtra());
        java.util.Optional.ofNullable(entry.getLastAccessTime()).ifPresent(copyEntry::setLastAccessTime);
        java.util.Optional.ofNullable(entry.getTime()).ifPresent(copyEntry::setTime);

        return copyEntry;
    }

    public static Provider<String> getBaseArchiveName(Project project) {
        Provider<@NotNull BasePluginExtension> base = GradleUtilities.getExtensionProvider(project, BasePluginExtension.class);
        Provider<String> baseName = base.flatMap(BasePluginExtension::getArchivesName);
        return baseName.orElse(project.getName());
    }

    public static Provider<Directory> getDistsDirectory(Project project) {
        Provider<@NotNull BasePluginExtension> base = GradleUtilities.getExtensionProvider(project, BasePluginExtension.class);
        Provider<Directory> baseDistsDir = base.flatMap(BasePluginExtension::getDistsDirectory);
        return baseDistsDir.orElse(project.getLayout().getBuildDirectory().dir("distributions"));
    }

    private static <T> Provider<T> getExtensionProvider(Project project, @NotNull Class<T> clazz) {
        Provider<T> provider = project.getProviders().provider(() -> {
            return project.getExtensions().findByType(clazz);
        });

        return provider;
    }

    public static Provider<String> getVersion(Project project) {
        return project.getProviders().provider(project::getVersion).map(Objects::toString).filter(v -> v != Project.DEFAULT_VERSION).orElse("");
    }

    @NotNull
    public static CopyAction createTarCopyAction(File archiveFile, @NotNull ArchiveOutputStreamFactory compressor, boolean preserveTimestamps, Provider<Long> reproducibleFileTimestamp) {
        try {
            return (@NotNull TarCopyAction) GradleUtilities.MH_TAR_COPY_ACTION_CTOR.invokeExact(archiveFile, compressor, preserveTimestamps, reproducibleFileTimestamp);
        } catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else if (e instanceof Error) {
                throw (Error) e;
            } else if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }
}
