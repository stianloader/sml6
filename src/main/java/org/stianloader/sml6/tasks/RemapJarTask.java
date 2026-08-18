package org.stianloader.sml6.tasks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.CompileClasspath;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.stianloader.micromixin.remapper.IllegalMixinException;
import org.stianloader.micromixin.remapper.MicromixinRemapper;
import org.stianloader.micromixin.remapper.MissingFeatureException;
import org.stianloader.remapper.HierarchyAwareMappingDelegator;
import org.stianloader.remapper.MappingLookup;
import org.stianloader.remapper.Remapper;
import org.stianloader.remapper.SimpleHierarchyAwareMappingLookup;
import org.stianloader.remapper.SimpleTopLevelLookup;
import org.stianloader.sml6.GradleUtilities;
import org.stianloader.sml6.starplane.remapping.ChainMappingLookup;
import org.stianloader.sml6.starplane.remapping.DebugableMemberLister;
import org.stianloader.sml6.starplane.remapping.RASRemapper;
import org.stianloader.sml6.starplane.remapping.ReadOnlyMIOMappingLookup;
import org.stianloader.sml6.starplane.remapping.ReadOnlyMappingLookupSink;
import org.stianloader.sml6.starplane.remapping.StarplaneAnnotationRemapper;
import org.stianloader.sml6.tasks.config.MIOMappingsConfigurationProvider;
import org.stianloader.sml6.tasks.config.MIOMappingsDirectoryProvider;
import org.stianloader.sml6.tasks.config.MIOMappingsFileProvider;
import org.stianloader.sml6.tasks.config.MIOMappingsProvider;

import net.fabricmc.mappingio.tree.MemoryMappingTree;

@CacheableTask
public abstract class RemapJarTask extends AbstractArtifactTask {

    public RemapJarTask() {
        this.getLibraryJars().convention(this.getProject().files()); // Empty collection by default
        this.getArchiveExtension().convention("jar");
    }

    public void addMappingsConfiguration(@NotNull Action<@NotNull MIOMappingsConfigurationProvider> configurationClosure) {
        Provider<MIOMappingsConfigurationProvider> provider = this.getProviders().provider(() -> {
            MIOMappingsConfigurationProvider fileProvider = this.getObjectFactory().newInstance(MIOMappingsConfigurationProvider.class);
            configurationClosure.execute(fileProvider);
            return fileProvider;
        });

        this.getMappings().add(provider);
    }

    public void addMappingsDirectory(@NotNull Action<@NotNull MIOMappingsDirectoryProvider> configurationClosure) {
        Provider<MIOMappingsDirectoryProvider> provider = this.getProviders().provider(() -> {
            MIOMappingsDirectoryProvider fileProvider = this.getObjectFactory().newInstance(MIOMappingsDirectoryProvider.class);
            configurationClosure.execute(fileProvider);
            return fileProvider;
        });

        this.getMappings().add(provider);
    }

    public void addMappingsFile(@NotNull Action<@NotNull MIOMappingsFileProvider> configurationClosure) {
        Provider<MIOMappingsFileProvider> provider = this.getProviders().provider(() -> {
            MIOMappingsFileProvider fileProvider = this.getObjectFactory().newInstance(MIOMappingsFileProvider.class);
            configurationClosure.execute(fileProvider);
            return fileProvider;
        });

        this.getMappings().add(provider);
    }

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getInputJar();

    @InputFiles
    @Optional
    @CompileClasspath
    public abstract Property<FileCollection> getLibraryJars();

    @Nested
    public abstract ListProperty<MIOMappingsProvider> getMappings();

    @Inject
    protected abstract ObjectFactory getObjectFactory();

    @Inject
    protected abstract ProviderFactory getProviders();

    @TaskAction
    public void remapJar() {
        List<@NotNull ReadOnlyMIOMappingLookup> lookups = new ArrayList<>();

        for (MIOMappingsProvider provider : this.getMappings().get()) {
            MemoryMappingTree mappingTree;

            try {
                mappingTree = provider.loadAndValidateTree();
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot read mappings from mappings file", e);
            }

            lookups.add(new ReadOnlyMIOMappingLookup(mappingTree, provider.getSrcNamespaceId().get(), provider.getDstNamespaceId().get(), false));
        }

        NavigableMap<Integer, Map<String, ClassNode>> libraryNodes = new TreeMap<>();
        Map<ClassNode, Map.Entry<File, ZipEntry>> debugInfo = new IdentityHashMap<>(); 

        for (File file : this.getLibraryJars().get()) {
            try (InputStream rawIn = Files.newInputStream(file.toPath());
                    ZipInputStream zipIn = new ZipInputStream(rawIn, StandardCharsets.UTF_8)) {
                for (ZipEntry entry = zipIn.getNextEntry(); entry != null; entry = zipIn.getNextEntry()) {
                    if (!entry.getName().endsWith(".class")) {
                        continue;
                    }

                    byte[] allData = zipIn.readAllBytes();

                    if (allData.length < 4
                            || allData[0] != (byte) 0xCA
                            || allData[1] != (byte) 0xFE
                            || allData[2] != (byte) 0xBA
                            || allData[3] != (byte) 0xBE) {
                        continue;
                    }

                    String path = entry.getName();
                    while (path.startsWith("/")) {
                        path = path.substring(1);
                    }

                    int mrjVersion = 8;

                    if (path.startsWith("META-INF/versions/")) {
                        path = path.substring(18);
                        mrjVersion = Integer.parseInt(path.substring(0, path.indexOf('/')));
                        if (mrjVersion < 9) {
                            this.getLogger().warn("Task '{}': Resource at path jar://{}!{} would fit under the multi-release jar version of {} - which makes little sense as that would be before the introduction of multi-release jars.", this.getName(), file.toURI(), entry.getName(), mrjVersion);
                            mrjVersion = 8;
                        }
                    }

                    readClass:
                    try {
                        ClassReader reader = new ClassReader(allData);

                        if (reader.getClassName().equals("module-info")) {
                            break readClass; // Module-infos have a very high chance of collision but are irrelevant during the mapping process.
                        }

                        ClassNode visitedNode = new ClassNode();
                        reader.accept(visitedNode, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);

                        Map<String, ClassNode> mrjLibNodes = libraryNodes.computeIfAbsent(mrjVersion, (ignored) -> {
                            return new TreeMap<>();
                        });

                        debugInfo.put(visitedNode, Map.entry(file, entry));
                        ClassNode previousValue;

                        if ((previousValue = mrjLibNodes.putIfAbsent(visitedNode.name, visitedNode)) != null) {
                            this.getLogger().warn("Task '{}': Resource at path jar://{}!{} defines class '{}' which was already defined through jar://{}!{}", this.getName(), file.toURI(), entry.getName(), visitedNode.name, debugInfo.get(previousValue).getKey().toURI(), debugInfo.get(previousValue).getValue().getName());
                        }
                    } catch (Exception ex) {
                        this.getLogger().warn("Task '{}': Unable to read library classfile {}; skipping it instead.", this.getName(), entry.getName(), ex);
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot read library file " + file.getAbsolutePath(), e);
            }
        }

        debugInfo = null;
        NavigableMap<Integer, Map<ZipEntry, byte[]>> resources = new TreeMap<>();

        File inputJar = this.getInputJar().get().getAsFile();
        try (InputStream rawIn = Files.newInputStream(inputJar.toPath());
                ZipInputStream zipIn = new ZipInputStream(rawIn, StandardCharsets.UTF_8)) {
            for (ZipEntry entry = zipIn.getNextEntry(); entry != null; entry = zipIn.getNextEntry()) {
                String path = entry.getName();
                while (path.startsWith("/")) {
                    path = path.substring(1);
                }

                int mrjVersion = 8;

                parseMrjVersion:
                if (path.startsWith("META-INF/versions/")) {
                    path = path.substring(18);

                    if (path.length() == 0) {
                        break parseMrjVersion;
                    }

                    int mrjIdx = path.indexOf('/');

                    if (mrjIdx < 0) {
                        mrjIdx = path.length();
                    }

                    mrjVersion = Integer.parseInt(path.substring(0, mrjIdx));

                    if (mrjVersion < 9) {
                        this.getLogger().warn("Task '{}': Resource at path jar://{}!{} would fit under the multi-release jar version of {} - which makes little sense as that would be before the introduction of multi-release jars.", this.getName(), inputJar.toURI(), entry.getName(), mrjVersion);
                        mrjVersion = 8;
                    }
                }

                resources.computeIfAbsent(mrjVersion, (ignored) -> {
                    return new HashMap<>();
                }).put(entry, zipIn.readAllBytes());
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read input file " + inputJar.getAbsolutePath(), e);
        }

        try (OutputStream os = Files.newOutputStream(this.getArchiveFile().get().getAsFile().toPath());
                ZipOutputStream zipOut = new ZipOutputStream(os, StandardCharsets.UTF_8)) {
            @SuppressWarnings("null")
            ChainMappingLookup externalLookups = new ChainMappingLookup(lookups.toArray(new @NotNull MappingLookup[0]));
            while (!resources.isEmpty()) {
                Map<ZipEntry, byte[]> versionResources;
                int mrjVersion;

                {
                    Map.Entry<Integer, Map<ZipEntry, byte[]>> var10001 = resources.pollLastEntry();
                    Objects.requireNonNull(var10001, "Polled entry cannot be null for non-empty map");
                    mrjVersion = var10001.getKey().intValue();
                    versionResources = var10001.getValue();
                }

                NavigableMap<ClassNode, ZipEntry> mainClasses = new TreeMap<>((n1, n2) -> n1.name.compareTo(n2.name));
                List<Map.Entry<ZipEntry, byte[]>> trueResources = new ArrayList<>();

                for (Map.Entry<ZipEntry, byte[]> resource : versionResources.entrySet()) {
                    byte[] resourceData = resource.getValue();

                    if (resourceData.length < 4
                            || resourceData[0] != (byte) 0xCA
                            || resourceData[1] != (byte) 0xFE
                            || resourceData[2] != (byte) 0xBA
                            || resourceData[3] != (byte) 0xBE) {
                        trueResources.add(resource);
                        continue;
                    }

                    try {
                        ClassReader reader = new ClassReader(resourceData);
                        ClassNode node = new ClassNode();
                        reader.accept(node, 0);

                        if (mainClasses.putIfAbsent(node, resource.getKey()) != null) {
                            this.getLogger().warn("Task '{}': Collision for node '{}'. Output jar may be corrupted.", this.getName(), node.name);
                        }
                    } catch (RuntimeException ignored) { }
                }

                NavigableSet<ClassNode> allClasses = new TreeSet<>(mainClasses.navigableKeySet());

                resources.entrySet()
                    .stream()
                    .sequential()
                    .map(Map.Entry::getValue)
                    .map(Map::entrySet)
                    .flatMap(Set::stream)
                    .sequential()
                    .map(Map.Entry::getValue)
                    .<java.util.Optional<ClassNode>>map(resourceData -> {
                        if (resourceData.length < 4
                                || resourceData[0] != (byte) 0xCA
                                || resourceData[1] != (byte) 0xFE
                                || resourceData[2] != (byte) 0xBA
                                || resourceData[3] != (byte) 0xBE) {
                            return java.util.Optional.empty();
                        }

                        try {
                            ClassReader reader = new ClassReader(resourceData);
                            ClassNode node = new ClassNode();
                            reader.accept(node, 0);

                            return java.util.Optional.of(node);
                        } catch (RuntimeException ignored) {
                            return java.util.Optional.empty();
                        }
                    }).filter(java.util.Optional::isPresent)
                    .map(java.util.Optional::get)
                    .filter(((Predicate<ClassNode>) allClasses::contains).negate())
                    .forEachOrdered(allClasses::add);

                Map<String, ClassNode> mrjLibraryNodes = new TreeMap<>();

                libraryNodes.headMap(mrjVersion, true)
                    .entrySet()
                    .stream()
                    .sequential()
                    .map(Map.Entry::getValue)
                    .forEachOrdered(mrjLibraryNodes::putAll);

                allClasses.addAll(mrjLibraryNodes.values());

                SimpleTopLevelLookup allTopLevelLookup = new SimpleTopLevelLookup(allClasses);
                DebugableMemberLister libraryMemberLister = new DebugableMemberLister(allTopLevelLookup, mrjLibraryNodes);

                @SuppressWarnings("null")
                SimpleHierarchyAwareMappingLookup mixinLookup = new SimpleHierarchyAwareMappingLookup(allClasses);
                ReadOnlyMappingLookupSink readOnlyExternalLookups = new ReadOnlyMappingLookupSink(externalLookups);
                MappingLookup externalHierarchyLookup = new HierarchyAwareMappingDelegator<>(readOnlyExternalLookups, allTopLevelLookup);
                ChainMappingLookup allLookup = new ChainMappingLookup(mixinLookup, externalHierarchyLookup);
                MicromixinRemapper mixinRemapper = new MicromixinRemapper(allLookup, mixinLookup, libraryMemberLister);
                Remapper coreRemaper = new Remapper(allLookup);

                StringBuilder sharedBuilder = new StringBuilder();
                for (Map.Entry<ClassNode, ZipEntry> mainEntry : mainClasses.entrySet()) {
                    ClassNode mainNode = Objects.requireNonNull(mainEntry.getKey());

//                    if (mainNode.name.equals("snoddasmannen/galimulator/class_31")) {
//                        allLookup.enableDebugMode(true);
//                        externalLookups.enableDebugMode(true);
//                        libraryMemberLister.setDebugging(true);
//                    }

                    StarplaneAnnotationRemapper.apply(mainNode, coreRemaper, sharedBuilder);

                    try {
                        mixinRemapper.remapClass(mainNode);
                    } catch (IllegalMixinException | MissingFeatureException e) {
                        throw new IOException("Unable to remap due to a problem which occured while remapping mixin " + mainNode.name + " in multi-release-jar sourceset " + mrjVersion, e);
                    }

                    coreRemaper.remapNode(mainNode, sharedBuilder);

//                    if (mainNode.name.equals("snoddasmannen/galimulator/class_31")) {
//                        allLookup.enableDebugMode(false);
//                        externalLookups.enableDebugMode(false);
//                        libraryMemberLister.setDebugging(false);
//                    }

                    ClassWriter writer = new ClassWriter(0);
                    mainNode.accept(writer);
                    String newName = (mrjVersion == 8 ? "" : "META-INF/versions/" + mrjVersion + "/") + mainNode.name.replace('.', '/') + ".class";
                    zipOut.putNextEntry(GradleUtilities.copyEntry(Objects.requireNonNull(mainEntry.getValue()), newName));
                    zipOut.write(writer.toByteArray());
                }

                for (Map.Entry<ZipEntry, byte[]> resource : trueResources) {
                    ZipEntry zipEntry = resource.getKey();
                    byte[] data = resource.getValue();

                    if (zipEntry.getName().toLowerCase(Locale.ROOT).endsWith(".ras")) {
                        data = new RASRemapper(allLookup, sharedBuilder).transform(data, "jar://<unknown>!" + resource.getKey().getName());
                        zipOut.putNextEntry(GradleUtilities.copyEntry(zipEntry));
                    } else {
                        zipOut.putNextEntry(zipEntry);
                    }

                    zipOut.write(data);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
