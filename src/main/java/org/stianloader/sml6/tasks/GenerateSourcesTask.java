package org.stianloader.sml6.tasks;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.metaobject.DynamicInvokeResult;
import org.gradle.internal.metaobject.PropertyAccess;
import org.gradle.internal.metaobject.PropertyMixIn;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.api.DecompilerOption;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger.Severity;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.slf4j.LoggerFactory;
import org.stianloader.remapper.MappingLookup;
import org.stianloader.sml6.GradleUtilities;
import org.stianloader.sml6.SML6GradlePlugin;
import org.stianloader.sml6.starplane.remapping.ChainMappingLookup;
import org.stianloader.sml6.starplane.remapping.ReadOnlyMIOMappingLookup;
import org.stianloader.sml6.starplane.sourcegen.EnhancedJarSaver;
import org.stianloader.sml6.starplane.sourcegen.FernflowerLoggerAdapter;
import org.stianloader.sml6.starplane.sourcegen.JavadocSource;
import org.stianloader.sml6.tasks.config.MIOMappingsDirectoryProvider;
import org.stianloader.sml6.tasks.config.MIOMappingsFileProvider;
import org.stianloader.sml6.tasks.config.MIOMappingsProvider;

import net.fabricmc.fernflower.api.IFabricJavadocProvider;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public abstract class GenerateSourcesTask extends ConventionTask {

    public static abstract class VFDecompileOptions implements PropertyMixIn, PropertyAccess {
        @NotNull
        private Map<String, String> vfOptions = new HashMap<>();

        @Nullable
        private transient Map<String, Map.Entry<String, DecompilerOption.Type>> canonicalPropertyNames;

        public VFDecompileOptions() {
            this.vfOptions.put(IFernflowerPreferences.LOG_LEVEL, "WARN");
            this.vfOptions.put(IFernflowerPreferences.DUMP_CODE_LINES, "1");
            this.vfOptions.put(IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1");
            this.vfOptions.put(IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1");
        }

        @NotNull
        @Input
        public Map<String, String> getVfOptions() {
            return this.vfOptions;
        }

        @NotNull
        @Contract(pure = false, mutates = "this", value = "_ -> this")
        public VFDecompileOptions setOption(@NotNull String key, boolean value) {
            this.vfOptions.put(key, value ? "1" : "0");
            return this;
        }

        @Override
        @Internal("Gradle sugar")
        public PropertyAccess getAdditionalProperties() {
            return this;
        }

        private Map<String, Map.Entry<String, DecompilerOption.Type>> getCanonicalPropertyNames() {
            Map<String, Map.Entry<String, DecompilerOption.Type>> canonicalPropertyNames = this.canonicalPropertyNames;

            if (canonicalPropertyNames != null) {
                return canonicalPropertyNames;
            }

            canonicalPropertyNames = new HashMap<>();

            for (Field field : IFernflowerPreferences.class.getDeclaredFields()) {
                if (field.getType() != String.class
                        || !Modifier.isStatic(field.getModifiers())
                        || field.getName().equals("LINE_SEPARATOR_WIN")
                        || field.getName().equals("LINE_SEPARATOR_UNX")) {
                    continue;
                }

                try {
                    IFernflowerPreferences.Type type = field.getAnnotation(IFernflowerPreferences.Type.class);
                    Map.Entry<String, DecompilerOption.Type> entry = Map.entry((String) field.get(null), type == null ? DecompilerOption.Type.BOOLEAN : type.value());
                    canonicalPropertyNames.put(field.getName().replaceAll("_", "").toLowerCase(Locale.ROOT), entry);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new IllegalStateException("Unexpected reflective error", e);
                }
            }

            this.canonicalPropertyNames = canonicalPropertyNames;

            return canonicalPropertyNames;
        }

        @Override
        public boolean hasProperty(String name) {
            return this.getCanonicalPropertyNames().containsKey(name.toLowerCase(Locale.ROOT));
        }

        @Override
        @Internal("Does not affect task")
        public Map<String, ? extends @org.jspecify.annotations.Nullable Object> getProperties() {
            Map<String, Object> values = new HashMap<>();

            for (Map.Entry<String, Map.Entry<String, DecompilerOption.Type>> entry : this.getCanonicalPropertyNames().entrySet()) {
                Object value = this.vfOptions.get(entry.getValue().getKey());

                if (value == null) {
                    value = IFernflowerPreferences.DEFAULTS.get(entry.getValue().getKey());
                }

                switch (entry.getValue().getValue()) {
                case BOOLEAN:
                    value = Integer.valueOf(value.toString()) != 0;
                    break;
                case INTEGER:
                    value = Integer.valueOf(value.toString());
                    break;
                default:
                    LoggerFactory.getLogger(GenerateSourcesTask.class).warn("Unknown decompiler option kind: {}", entry);
                case STRING:
                    break;
                }

                values.put(entry.getKey(), value);
            }

            return values;
        }

        @Override
        public DynamicInvokeResult tryGetProperty(String name) {
            Map.Entry<String, DecompilerOption.Type> canonicalName = this.getCanonicalPropertyNames().get(name.toLowerCase(Locale.ROOT));

            if (canonicalName == null) {
                return DynamicInvokeResult.notFound();
            }

            Object value = this.vfOptions.get(canonicalName.getKey());

            if (value == null) {
                value = IFernflowerPreferences.DEFAULTS.get(canonicalName.getKey());
            }

            switch (canonicalName.getValue()) {
            case BOOLEAN:
                value = Integer.valueOf(value.toString()) != 0;
                break;
            case INTEGER:
                value = Integer.valueOf(value.toString());
                break;
            default:
                LoggerFactory.getLogger(GenerateSourcesTask.class).warn("Unknown decompiler option kind: {}", canonicalName);
            case STRING:
                break;
            }

            return DynamicInvokeResult.found(value);
        }

        @Override
        public DynamicInvokeResult trySetProperty(String name, @org.jspecify.annotations.Nullable Object value) {
            Map.Entry<String, DecompilerOption.Type> canonicalName = this.getCanonicalPropertyNames().get(name.toLowerCase(Locale.ROOT));

            if (canonicalName == null) {
                return DynamicInvokeResult.notFound();
            }

            String vfValue;

            switch (canonicalName.getValue()) {
            case BOOLEAN:
                vfValue = ((Boolean) value) ? "1" : "0";
                break;
            default:
                LoggerFactory.getLogger(GenerateSourcesTask.class).warn("Unknown decompiler option kind: {}", canonicalName);
            case INTEGER:
            case STRING:
                if (value instanceof Boolean) {
                    vfValue = ((Boolean) value) ? "1" : "0";
                } else {
                    vfValue = value.toString();
                }
                break;
            }

            this.vfOptions.put(canonicalName.getKey(), vfValue);

            return DynamicInvokeResult.found();
        }

        @Override
        public DynamicInvokeResult trySetPropertyWithoutInstrumentation(String name, @org.jspecify.annotations.Nullable Object value) {
            return this.trySetProperty(name, value);
        }
    }

    private static void replaceLineNumbers(@NotNull Path unmappedInput, @NotNull Path linemappedOutput, Map<String, int[]> lineMappings) throws IOException {
        Map<String, ClassNode> nameToNode = new LinkedHashMap<>();

        try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(unmappedInput), StandardCharsets.UTF_8)) {
            for (ZipEntry entry = zipIn.getNextEntry(); entry != null; entry = zipIn.getNextEntry()) {
                if (entry.getName().endsWith(".class")) {
                    ClassNode node = new ClassNode();
                    ClassReader reader = new ClassReader(zipIn);
                    reader.accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);
                    nameToNode.put(node.name, node);
                }
            }
        }

        try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(unmappedInput), StandardCharsets.UTF_8);
                ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(linemappedOutput), StandardCharsets.UTF_8)) {
            for (ZipEntry entry = zipIn.getNextEntry(); entry != null; entry = zipIn.getNextEntry()) {
                zipOutputStream.putNextEntry(GradleUtilities.copyEntry(entry));

                if (!entry.getName().endsWith(".class")) {
                    byte[] buffer = new byte[4096];
                    for (int read = zipIn.read(buffer); read != -1; read = zipIn.read(buffer)) {
                        zipOutputStream.write(buffer, 0, read);
                    }
                    continue;
                }

                ClassReader reader = new ClassReader(zipIn);

                ClassNode node = nameToNode.get(reader.getClassName());
                ClassNode outermostClassnode = node;
                outermostNodeFinderLoop:
                while (true) {
                    if (outermostClassnode.outerClass != null) {
                        outermostClassnode = nameToNode.get(outermostClassnode.outerClass);
                        continue;
                    }
                    for (InnerClassNode icn : outermostClassnode.innerClasses) {
                        if (icn.name.equals(outermostClassnode.name) && icn.outerName != null) {
                            outermostClassnode = nameToNode.get(icn.outerName);
                            continue outermostNodeFinderLoop;
                        }
                    }
                    break;
                }

                if (node.sourceFile == null || node.sourceFile.equals("SourceFile")) {
                    int startName = outermostClassnode.name.lastIndexOf('/') + 1;
                    int innerSeperator = outermostClassnode.name.indexOf('$');
                    String baseName;
                    if (innerSeperator == -1) {
                        baseName = outermostClassnode.name.substring(startName);
                    } else {
                        baseName = outermostClassnode.name.substring(startName, innerSeperator);
                    }
                    node.sourceFile = baseName + ".java";
                }

                // mapping[i * 2] -> original line number; mapping[i * 2 + 1] -> new line number
                int[] mapping = lineMappings.get(outermostClassnode.name);
                Map<Integer, Integer> lineNumberConversion;

                if (mapping == null) {
                    lineNumberConversion = null;
                } else {
                    lineNumberConversion = new HashMap<>();
                    for (int i = 0; i < mapping.length;) {
                        lineNumberConversion.put(mapping[i++], mapping[i++]);
                    }
                }

                ClassWriter writer = new ClassWriter(reader, 0);
                reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                            String[] exceptions) {
                        return new MethodVisitor(this.api, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                            @Override
                            public void visitLineNumber(int line, Label start) {
                                if (lineNumberConversion == null) {
                                    super.visitLineNumber(line, start);
                                } else {
                                    Integer newLineNumber = lineNumberConversion.get(line);
                                    if (newLineNumber == null) {
                                        super.visitLineNumber(line, start);
                                    } else {
                                        super.visitLineNumber(newLineNumber.intValue(), start);
                                    }
                                }
                            }
                        };
                    }

                    @Override
                    public void visitSource(String source, String debug) {
                        super.visitSource(node.sourceFile, debug);
                    }
                }, 0);
                zipOutputStream.write(writer.toByteArray());
            }
        }

        nameToNode.clear();
    }

    private final VFDecompileOptions decompileOptions;

    public GenerateSourcesTask() {
        this.setGroup(SML6GradlePlugin.DEFAULT_TASK_GROUP);
        this.setDescription("Remove shaded dependencies in jar");

        this.decompileOptions = this.getObjectFactory().newInstance(VFDecompileOptions.class);

        DirectoryProperty buildDir = this.getLayout().getBuildDirectory();
        Provider<String> taskNameProvider = this.getProviders().provider(this::getName);
        this.getOutputDirectory().convention(buildDir.dir(taskNameProvider.map(s -> "sml6/" + s)));
        this.getOutputSourcesJar().convention(this.getOutputDirectory().file("decompiled-sources.jar"));
        this.getLineRemappedOutputJar().convention(this.getOutputDirectory().file("line-remapped.jar"));

        this.getLibraryClasspath().convention(this.getTransitiveDependencies().map(config -> {
            return this.getProject().files(config.resolve());
        }));
    }

    public void addJavadocSourcesDir(@NotNull Action<@NotNull MIOMappingsDirectoryProvider> configurationClosure) {
        Provider<MIOMappingsDirectoryProvider> provider = this.getProviders().provider(() -> {
            MIOMappingsDirectoryProvider fileProvider = this.getObjectFactory().newInstance(MIOMappingsDirectoryProvider.class);
            configurationClosure.execute(fileProvider);
            return fileProvider;
        });

        this.getJavadocSources().add(provider);
    }

    public void addJavadocSourcesFile(@NotNull Action<@NotNull MIOMappingsFileProvider> configurationClosure) {
        Provider<MIOMappingsFileProvider> provider = this.getProviders().provider(() -> {
            MIOMappingsFileProvider fileProvider = this.getObjectFactory().newInstance(MIOMappingsFileProvider.class);
            configurationClosure.execute(fileProvider);
            return fileProvider;
        });

        this.getJavadocSources().add(provider);
    }

    @TaskAction
    public void decompile() {
        Map<String, Object> args = new HashMap<>(this.getDecompileOptions().getVfOptions());
        Map<String, int[]> lineMappings = new HashMap<>();

        List<@NotNull ReadOnlyMIOMappingLookup> lookups = new ArrayList<>();

        for (MIOMappingsProvider provider : this.getJavadocSources().get()) {
            MemoryMappingTree mappingTree;

            try {
                mappingTree = provider.loadTree();
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot read javadocs from mappings file", e);
            }

            lookups.add(new ReadOnlyMIOMappingLookup(mappingTree, provider.getDstNamespaceId().get(), provider.getDstNamespaceId().get(), true));
        }

        if (!lookups.isEmpty()) {
            args.put(IFabricJavadocProvider.PROPERTY_NAME, new JavadocSource(new ChainMappingLookup(Objects.requireNonNull(lookups.toArray(new @NotNull MappingLookup[0])))));
        }

        try (EnhancedJarSaver jarSaver = new EnhancedJarSaver(this.getOutputSourcesJar().get().getAsFile(), lineMappings)) {
            Fernflower qf = new Fernflower(jarSaver, args, new FernflowerLoggerAdapter(this.getLogger(), Severity.WARN));
            qf.addSource(this.getInputJar().get().getAsFile());

            FileCollection libraryClasspath = this.getLibraryClasspath().getOrNull();

            if (libraryClasspath != null) {
                libraryClasspath.forEach(qf::addLibrary);
            }

            qf.decompileContext();
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot decompile input for task '" + this.getName() + "'", e);
        }

        if (lineMappings.isEmpty()) {
            try {
                Files.copy(this.getInputJar().get().getAsFile().toPath(), this.getLineRemappedOutputJar().get().getAsFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new UncheckedIOException("Failure to copy input jar to line remapped output jar (no line mappings available)", e);
            }
        } else {
            try {
                GenerateSourcesTask.replaceLineNumbers(this.getInputJar().get().getAsFile().toPath(), this.getLineRemappedOutputJar().get().getAsFile().toPath(), lineMappings);
            } catch (IOException e) {
                throw new UncheckedIOException("Unable to remap lines to match sources jar", e);
            }
        }
    }

    public void decompileOptions(@NotNull Action<VFDecompileOptions> action) {
        action.execute(this.getDecompileOptions());
    }

    @Nested
    public VFDecompileOptions getDecompileOptions() {
        return this.decompileOptions;
    }

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getInputJar();

    @Nested
    public abstract ListProperty<MIOMappingsProvider> getJavadocSources();

    @Inject
    protected abstract ProjectLayout getLayout();

    @Optional
    @InputFiles
    @Classpath
    public abstract Property<FileCollection> getLibraryClasspath();

    @OutputFile
    @Optional
    public abstract RegularFileProperty getLineRemappedOutputJar();

    @Inject
    protected abstract ObjectFactory getObjectFactory();

    @Internal("Transitively affects other output locations. Not used directly.")
    public abstract DirectoryProperty getOutputDirectory();

    @OutputFile
    @Optional
    public abstract RegularFileProperty getOutputSourcesJar();

    @Inject
    protected abstract ProviderFactory getProviders();

    @Internal("Source of #getVFLibraryClasspath, not used directly as it is not fingerprintable.")
    public abstract Property<Configuration> getTransitiveDependencies();
}
