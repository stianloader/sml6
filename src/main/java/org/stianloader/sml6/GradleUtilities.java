package org.stianloader.sml6;

import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.BasePluginExtension;
import org.gradle.api.provider.Provider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public class GradleUtilities {
    private static <T> Provider<T> getExtensionProvider(Project project, @NotNull Class<T> clazz) {
        Provider<T> provider = project.getProviders().provider(() -> {
            return project.getExtensions().findByType(clazz);
        });
        return provider;
    }

    public static Provider<String> getBaseArchiveName(Project project) {
        Provider<BasePluginExtension> base = GradleUtilities.getExtensionProvider(project, BasePluginExtension.class);
        Provider<String> baseName = base.flatMap(BasePluginExtension::getArchivesName);
        return baseName.orElse(project.getName());
    }

    public static Provider<String> getVersion(Project project) {
        return project.getProviders().provider(project::getVersion).map(Object::toString).filter(v -> v != Project.DEFAULT_VERSION).orElse("");
    }

    public static Provider<Directory> getDistsDirectory(Project project) {
        Provider<BasePluginExtension> base = GradleUtilities.getExtensionProvider(project, BasePluginExtension.class);
        Provider<Directory> baseDistsDir = base.flatMap(BasePluginExtension::getDistsDirectory);
        return baseDistsDir.orElse(project.getLayout().getBuildDirectory().dir("distributions"));
    }

    public static String appendFilenamePart(String currentPart, String appendPart) {
        if (appendPart.isEmpty()) {
            return currentPart;
        }
        return currentPart + "-" + appendPart;
    }

    public static String appendFileExtension(String filename, String extension) {
        if (extension.isEmpty()) {
            return filename;
        }
        return filename + "." + extension;
    }
}
