package org.stianloader.sml6.tasks;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarFile;

import javax.inject.Inject;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.stianloader.deobf.ClassWrapper;
import org.stianloader.deobf.IntermediaryGenerator;
import org.stianloader.deobf.MethodReference;
import org.stianloader.deobf.Oaktree;
import org.stianloader.remapper.MemberRef;
import org.stianloader.remapper.Remapper;
import org.stianloader.remapper.SimpleHierarchyAwareMappingLookup;
import org.stianloader.remapper.SimpleTopLevelLookup;
import org.stianloader.remapper.SimpleTopLevelLookup.MemberRealm;
import org.stianloader.sml6.SML6GradlePlugin;
import org.stianloader.sml6.starplane.autodeobf.Autodeobf502;
import org.stianloader.sml6.starplane.autodeobf.AutodeobfRunner;
import org.stianloader.sml6.starplane.remapping.TinyV1MappingWriter;

@CacheableTask
public abstract class DeobfuscateGameTask extends ConventionTask {

    public DeobfuscateGameTask() {
        this.setGroup(SML6GradlePlugin.DEFAULT_TASK_GROUP);
        this.setDescription("Deobfuscate the game jar using stianloader-deobf/slintermediary (if choosen) and/or autodeobf/spstarmap (if choosen)");
        this.getAutodeobfVersion().convention("5.0.2");
        this.getWithAutodeobf().convention(true);
        this.getWithSLDeobf().convention(true);
        this.getWithSLDeobfRemapping().convention(this.getWithSLDeobf());
        DirectoryProperty buildDir = this.getLayout().getBuildDirectory();
        Provider<String> taskNameProvider = this.getProviders().provider(this::getName);
        this.getOutputDirectory().convention(buildDir.dir(taskNameProvider.map(s -> "sml6/" + s)));
        this.getSlIntermediaryMappings().convention(this.getOutputDirectory().file("slintermediary.tiny"));
        this.getSpStarmapMappings().convention(this.getOutputDirectory().file("spstarmap.tiny"));
        this.getOutputJar().convention(this.getOutputDirectory().file("game-transformed.jar"));
    }

    private void addSignatures(List<ClassNode> nodes, Map<String, ClassNode> nameToNode, Map<MethodReference, ClassWrapper> signatures) {
        StringBuilder builder = new StringBuilder();
        for (ClassNode node : nodes) {
            for (MethodNode method : node.methods) {
                if (method.signature == null) {
                    ClassWrapper newSignature = signatures.get(new MethodReference(node.name, method));
                    if (newSignature == null) {
                        continue;
                    }
                    builder.append(method.desc, 0, method.desc.length() - 1);
                    builder.append("<L");
                    builder.append(newSignature.getName());
                    builder.append(";>;");
                    method.signature = builder.toString();
                    builder.setLength(0);
                }
            }
        }
    }

    @TaskAction
    public void deobfuscate() {
        Path cleanGalimJar = this.getInputJar().get().getAsFile().toPath();

        if (Files.notExists(cleanGalimJar)) {
            throw new IllegalStateException("Input file does not exist: " + cleanGalimJar.toAbsolutePath());
        } else {
            this.getLogger().debug("Task '{}' is using the game jar found at '{}'." + this.getPath(), cleanGalimJar);
        }

        Path intermediaryMappingsFile = this.getSlIntermediaryMappings().getAsFile().get().toPath();
        Oaktree deobfuscator = new Oaktree();
        oaktreeDeobf:
        try {
            if (!this.getWithSLDeobf().get() && !this.getWithAutodeobf().get()) {
                if (this.getWithSLDeobfRemapping().get()) {
                    this.getLogger().warn("Task '{}' has 'withSLDeobf' set to false, while 'withSLDeobfRemapping' is true. The latter will be skipped.");
                }
                break oaktreeDeobf;
            }

            long indexing = System.nanoTime();
            JarFile jar = new JarFile(cleanGalimJar.toFile());
            deobfuscator.index(jar);
            jar.close();
            Map<String, ClassNode> nameToNode = new HashMap<>();

            for (ClassNode node : deobfuscator.getClassNodesDirectly()) {
                nameToNode.put(node.name, node);
            }

            long startDeobf = System.nanoTime();
            this.getLogger().debug("Loaded input jar in " + (startDeobf - indexing) / 1_000_000L + " ms.");

            if (!this.getWithSLDeobf().get()) {
                if (this.getWithSLDeobfRemapping().get()) {
                    this.getLogger().warn("Task '{}' has 'withSLDeobf' set to false, while 'withSLDeobfRemapping' is true. The latter will be skipped.");
                }
                break oaktreeDeobf;
            }

            deobfuscator.fixInnerClasses();
            deobfuscator.fixParameterLVT();
            deobfuscator.guessFieldGenerics();
            this.addSignatures(deobfuscator.getClassNodesDirectly(), nameToNode, deobfuscator.analyseLikelyMethodReturnCollectionGenerics());
            Map<MethodReference, ClassWrapper> methods = new HashMap<>();
            deobfuscator.lambdaStreamGenericSignatureGuessing(null, methods);
            this.addSignatures(deobfuscator.getClassNodesDirectly(), nameToNode, methods);
            deobfuscator.inferMethodGenerics();
            deobfuscator.inferConstructorGenerics();
            deobfuscator.fixForeachOnArray();
            deobfuscator.fixComparators(false);
            deobfuscator.guessAnonymousInnerClasses();

            // sl-deobf adds ACC_SUPER as that was the observed behaviour of compilers when compiling anonymous inner classes.
            // However, asm-util's ClassCheckAdapter does not tolerate that flag on anonymous inner classes, so we shall strip it.
            // In the end, this should have absolutely no impact on runtime 90% of the time (the other 10% are when the
            // ClassCheckAdapter is being used by SLL in case a class failed to transform).
            for (ClassNode node : deobfuscator.getClassNodesDirectly()) {
                for (InnerClassNode icn : node.innerClasses) {
                    icn.access &= ~Opcodes.ACC_SUPER;
                }
            }

            long startIntermediarisation = System.nanoTime();
            this.getLogger().debug("Deobfuscated classes in " + (startIntermediarisation - startDeobf) / 1_000_000L + " ms.");

            if (this.getWithSLDeobfRemapping().get()) {
                try (TinyV1MappingWriter mappingWriter = new TinyV1MappingWriter(intermediaryMappingsFile, "official", "intermediary")) {
                    IntermediaryGenerator generator = new IntermediaryGenerator(mappingWriter, deobfuscator.getClassNodesDirectly());
                    generator.useAlternateClassNaming(!Boolean.getBoolean("de.geolykt.starplane.oldnames"));
                    generator.remapClassesV2(true);
                    deobfuscator.fixSwitchMaps();
                    generator.doProposeEnumFieldsV2();
                    generator.remapGetters();

                    // Hierarchy aware lookup is needed to remap inner enums correctly, as we need field hierarchies to be properly propagated for that.
                    @SuppressWarnings("null")
                    SimpleTopLevelLookup hierarchyLookup = new SimpleTopLevelLookup((Iterable<ClassNode>) deobfuscator.getClassNodesDirectly());
                    SimpleHierarchyAwareMappingLookup remapperLookup = new SimpleHierarchyAwareMappingLookup(mappingWriter, hierarchyLookup);

                    Remapper remapper = new Remapper(remapperLookup);
                    StringBuilder sharedBuilder = new StringBuilder();

                    for (ClassNode node : deobfuscator.getClassNodesDirectly()) {
                        remapper.remapNode(Objects.requireNonNull(node), sharedBuilder);
                    }
                }
                this.getLogger().info("Task '{}' computed sldeobf intermediaries in {} ms.", this.getPath(), (System.nanoTime() - startIntermediarisation) / 1_000_000L);
            }

            if (!this.getWithAutodeobf().get()) {
                deobfuscator.invalidateNameCaches();
                deobfuscator.applyInnerclasses();
                // TODO fix ICN names here
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to run sldeobf", e);
        }

        if (this.getWithAutodeobf().get()) {
            try (TinyV1MappingWriter mappingWriter = new TinyV1MappingWriter(this.getSpStarmapMappings().get().getAsFile().toPath(), "intermediary", "named")) {
                long startSlStarmap = System.nanoTime();
                AutodeobfRunner deobf;

                String autodeobfVersion = this.getAutodeobfVersion().get();
                if (autodeobfVersion.equals("5.0.2")) {
                    deobf = new Autodeobf502(deobfuscator.getClassNodesDirectly(), mappingWriter);
                } else {
                    throw new IllegalStateException("No Autodeobf implementation for version " + autodeobfVersion);
                }

                // Hierarchy aware lookup is needed to remap inner enums correctly, as we need field hierarchies to be properly propagated for that.
                @SuppressWarnings("null")
                SimpleTopLevelLookup hierarchyLookup = new SimpleTopLevelLookup((Iterable<ClassNode>) deobfuscator.getClassNodesDirectly());
                SimpleHierarchyAwareMappingLookup remapperLookup = new SimpleHierarchyAwareMappingLookup(mappingWriter, hierarchyLookup);

                this.getLogger().info("Task '{}' uses autodeobf version {}", this.getPath(), deobf.getVersion());

                StringBuilder sharedBuilder = new StringBuilder();
                deobf.runAll();
                mappingWriter.synchronizeICNNames(Objects.requireNonNull(deobfuscator.getClassNodesDirectly()), new StringBuilder());

                Remapper remapper = new Remapper(remapperLookup);

                for (ClassNode node : deobfuscator.getClassNodesDirectly()) {
                    remapper.remapNode(Objects.requireNonNull(node), sharedBuilder);
                }

                if (this.getWithSLDeobf().get()) {
                    deobfuscator.invalidateNameCaches();
                    deobfuscator.applyInnerclasses();
                }

                this.getLogger().info("Computed spStarmap in " + (System.nanoTime() - startSlStarmap) / 1_000_000L + " ms.");
            } catch (Exception e) {
                throw new RuntimeException("Cannot write Autodeobf-generated mappings", e);
            }
        }

        try (OutputStream os = Files.newOutputStream(this.getOutputJar().get().getAsFile().toPath())) {
            deobfuscator.write(os, cleanGalimJar);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed writing output jar", e);
        }
    }

    @Input
    @Optional
    public abstract Property<String> getAutodeobfVersion();

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getInputJar();

    @Inject
    protected abstract ProjectLayout getLayout();

    @Internal("Transitively affects other output locations. Not used directly.")
    public abstract DirectoryProperty getOutputDirectory();

    @OutputFile
    @Optional
    public abstract RegularFileProperty getOutputJar();

    @Inject
    protected abstract ProviderFactory getProviders();

    @OutputFile
    @Optional
    public abstract RegularFileProperty getSlIntermediaryMappings();

    @OutputFile
    @Optional
    public abstract RegularFileProperty getSpStarmapMappings();

    @Input
    @Optional
    public abstract Property<Boolean> getWithAutodeobf();

    @Input
    @Optional
    public abstract Property<Boolean> getWithSLDeobf();

    @Input
    @Optional
    public abstract Property<Boolean> getWithSLDeobfRemapping();
}
