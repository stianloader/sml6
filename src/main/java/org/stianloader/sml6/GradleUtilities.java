package org.stianloader.sml6;

import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.BasePluginExtension;
import org.gradle.api.provider.Provider;
import org.jetbrains.annotations.NotNull;

class GradleUtilities {
    private static <T> Provider<T> getExtensionProvider(Project project, @NotNull Class<T> clazz) {
        Provider<T> provider = project.getProviders().provider(() -> {
            return project.getExtensions().findByType(clazz);
        });
        return provider;
    }

    static Provider<String> getBaseArchiveName(Project project) {
        Provider<BasePluginExtension> base = GradleUtilities.getExtensionProvider(project, BasePluginExtension.class);
        Provider<String> baseName = base.flatMap(BasePluginExtension::getArchivesName);
        return baseName.orElse(project.getName());
    }

    static Provider<String> getVersion(Project project) {
        return project.getProviders().provider(project::getVersion).map(Object::toString).filter(Project.DEFAULT_STATUS::equals).orElse("");
    }

    static Provider<Directory> getDistsDirectory(Project project) {
        Provider<BasePluginExtension> base = GradleUtilities.getExtensionProvider(project, BasePluginExtension.class);
        Provider<Directory> baseDistsDir = base.flatMap(BasePluginExtension::getDistsDirectory);
        return baseDistsDir.orElse(project.getLayout().getBuildDirectory().dir("distributions"));
    }

    static String appendFilenamePart(String currentPart, String appendPart) {
        if (appendPart.isEmpty()) {
            return currentPart;
        }
        return currentPart + "-" + appendPart;
    }

    static String appendFileExtension(String filename, String extension) {
        if (extension.isEmpty()) {
            return filename;
        }
        return filename + "." + extension;
    }
}
