package org.stianloader.sml6;

import java.util.zip.ZipEntry;

import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.BasePluginExtension;
import org.gradle.api.provider.Provider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public class GradleUtilities {

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
        Provider<BasePluginExtension> base = GradleUtilities.getExtensionProvider(project, BasePluginExtension.class);
        Provider<String> baseName = base.flatMap(BasePluginExtension::getArchivesName);
        return baseName.orElse(project.getName());
    }

    public static Provider<Directory> getDistsDirectory(Project project) {
        Provider<BasePluginExtension> base = GradleUtilities.getExtensionProvider(project, BasePluginExtension.class);
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
        return project.getProviders().provider(project::getVersion).map(Object::toString).filter(v -> v != Project.DEFAULT_VERSION).orElse("");
    }
}
