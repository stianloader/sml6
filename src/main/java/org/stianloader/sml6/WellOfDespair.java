package org.stianloader.sml6;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConsumableConfiguration;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.artifacts.ivyservice.TypedResolveException;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.plugins.ide.eclipse.model.Classpath;
import org.gradle.plugins.ide.eclipse.model.ClasspathEntry;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;
import org.gradle.plugins.ide.eclipse.model.FileReference;
import org.gradle.plugins.ide.eclipse.model.Library;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import groovy.lang.Closure;

public class WellOfDespair {

    public static class VariantArtifactConfigurator {
        private final RegularFileProperty artifactJar;
        @Nullable
        private String capabilityName;

        @Nullable
        private Action<ConsumableConfiguration> configurationClosure;

        @Nullable
        private String configurationName;

        @Nullable
        private Action<ModuleDependency> dependencyClosure;

        public boolean injectEclipseClasspath = true;

        private final RegularFileProperty sourcesJar;

        public VariantArtifactConfigurator(ObjectFactory factory) {
            this.artifactJar = factory.fileProperty();
            this.sourcesJar = factory.fileProperty();
        }

        public void configuration(@NotNull Action<ConsumableConfiguration> closure) {
            if (this.configurationClosure != null) {
                throw new IllegalStateException("The Configuration closure was already defined (this method cannot be called twice for the same object).");
            }

            this.configurationClosure = Objects.requireNonNull(closure);
        }

        public void configuration(@NotNull Closure<Void> closure) {
            this.configuration((configuration) -> {
                closure.setDelegate(configuration);
                closure.call(configuration);
            });
        }

        public void dependency(@NotNull Action<ModuleDependency> dependencyClosure) {
            if (this.dependencyClosure != null) {
                throw new IllegalStateException("The Dependency closure was already defined (this method cannot be called twice for the same object).");
            }

            this.dependencyClosure = Objects.requireNonNull(dependencyClosure);
        }

        public void dependency(@NotNull Closure<Void> dependencyClosure) {
            this.dependency((moduleDependency) -> {
                dependencyClosure.setDelegate(moduleDependency);
                dependencyClosure.call(moduleDependency);
            });
        }

        public RegularFileProperty getArtifactJar() {
            return this.artifactJar;
        }

        public RegularFileProperty getSourcesJar() {
            return this.sourcesJar;
        }

        public void setArtifactJar(Provider<? extends RegularFile> property) {
            this.artifactJar.set(property);
        }

        public void setCapabilityName(@NotNull String capabilityName) {
            if (this.configurationClosure != null) {
                throw new IllegalStateException("The Capability name was already defined (this method cannot be called twice for the same object).");
            }

            this.capabilityName = Objects.requireNonNull(capabilityName);
        }

        public void setConfigurationName(@NotNull String configurationName) {
            if (this.configurationClosure != null) {
                throw new IllegalStateException("The Configuration name was already defined (this method cannot be called twice for the same object).");
            }

            this.configurationName = Objects.requireNonNull(configurationName);
        }

        public void setSourcesJar(Provider<? extends RegularFile> property) {
            this.sourcesJar.set(property);
        }

    }

    public static void registerDependency(Project project, NamedDomainObjectProvider<Configuration> targetConfiguration, Action<VariantArtifactConfigurator> configureClosure) {
        ObjectFactory objects = project.getObjects();
        VariantArtifactConfigurator configurator = new VariantArtifactConfigurator(objects);

        configureClosure.execute(configurator);

        String capabilityName = configurator.capabilityName;
        String configurationName = configurator.configurationName;

        if (capabilityName == null) {
            throw new IllegalStateException("The Capability name is not set by the configurator closure.");
        }

        if (configurationName == null) {
            throw new IllegalStateException("The Configuration name is not set by the configurator closure.");
        }

        project.getConfigurations().consumable(configurationName, (configuration) -> {
            configuration.attributes((attributes) -> {
                // Note: The seemingly redundant casts are there to appease the eclipse null analysis gods
                attributes.attribute(Usage.USAGE_ATTRIBUTE, (Usage) objects.named(Usage.class, Usage.JAVA_API));
                attributes.attribute(Category.CATEGORY_ATTRIBUTE, (Category) objects.named(Category.class, Category.LIBRARY));
                attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, (Bundling) objects.named(Bundling.class, Bundling.EXTERNAL));
                attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, (LibraryElements) objects.named(LibraryElements.class, LibraryElements.JAR));
            });

            configuration.outgoing((publications) -> {
                publications.capability(capabilityName);
                publications.artifact(configurator.getArtifactJar());
            });

            Action<ConsumableConfiguration> configurationClosure = configurator.configurationClosure;

            if (configurationClosure != null) {
                configurationClosure.execute(configuration);
            }
        });

        DependencyHandler dependencies = project.getDependencies();

        ProjectDependency dependency = dependencies.project(project.getPath());

        Closure<Void> projectDependencyConfigureClosure = new Closure<>(dependency, dependency) {
            @Override
            public Void call() {
                return this.call(this.getDelegate());
            }

            @Override
            public Void call(Object arguments) {
                ProjectDependency projectDependency = (ProjectDependency) arguments;

                projectDependency.capabilities((capabilityHandler) -> {
                    capabilityHandler.requireCapability(capabilityName);
                });

                Action<ModuleDependency> dependencyClosure = configurator.dependencyClosure;

                if (dependencyClosure != null) {
                    dependencyClosure.execute(projectDependency);
                }

                return null;
            }

            @Override
            public Void call(Object... arguments) {
                throw new UnsupportedOperationException();
            }
        };

        dependencies.add(targetConfiguration.getName(), dependency, projectDependencyConfigureClosure);

        EclipseModel eclipseModel = (EclipseModel) project.findProperty("eclipse");

        if (configurator.injectEclipseClasspath && eclipseModel != null) {
            eclipseModel.classpath((classpath) -> {
                classpath.file(xmlContentMerger -> {
                    xmlContentMerger.whenMerged((xmlContent) -> {
                        Classpath xmlClasspath = (Classpath) xmlContent;

                        FileReference artifact = null;

                        try {
                            Configuration detachedConfiguration = project.getConfigurations().detachedConfiguration(dependency);
                            Set<File> resolvedConfiguration = detachedConfiguration.resolve();

                            if (resolvedConfiguration.size() != 1) {
                                throw new IllegalStateException("Configuration " + detachedConfiguration + " resolved to a non-singular amount of files: " + resolvedConfiguration);
                            }

                            artifact = xmlClasspath.fileReference(resolvedConfiguration.iterator().next());
                        } catch (TypedResolveException e) {
                            artifact = xmlClasspath.fileReference(configurator.artifactJar.get().getAsFile());

                            project.getLogger().debug("SML6's well eternal torment, agony, and despair: Failed to resolve configuration", e);
                            project.getLogger().warn("SML6's well eternal torment, agony, and despair: Unable to resolve artifact jar of configuration '{}'. This can happen on the initial build; you might need to synchronize the gradle project again. Falling back to the untransformed artifact. Should the fallback also fail, try building the project from the command line using gradlew. For more information, execute with '--debug'.", configurationName);
                        }

                        FileReference source = xmlClasspath.fileReference(configurator.sourcesJar.get().getAsFile());

                        Library injectedLibrary = new Library(artifact);
                        injectedLibrary.setSourcePath(source);

                        List<ClasspathEntry> classpathEntries = new ArrayList<>(xmlClasspath.getEntries());
                        classpathEntries.add(injectedLibrary);
                        xmlClasspath.setEntries(classpathEntries);
                    });
                });
            });
        }
    }

    public static void registerDependency(Project project, NamedDomainObjectProvider<Configuration> targetConfiguration, Closure<Void> configureClosure) {
        WellOfDespair.registerDependency(project, targetConfiguration, (configurator) -> {
            configureClosure.setDelegate(configurator);
            configureClosure.call(configurator);
        });
    }
}
