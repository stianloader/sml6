# SML6

While the SLL ecosystem is focused on transforming the game through
runtime bytecode patches, the SML toolchain is focused on ahead of
time recompilation of galimulator. SML6  tries to unify both
approaches whilst building on SML's historical strong points like
strong compatibility with caching and task-based execution.

However, SML6 is much more game agnostic than its predecessors,
provided the game is written in Java and is not all too complex to start.
Further, SML6 incorporates many elements that were until then
unique to starplane-based toolchains - for example running the game
straight from the IDE. As such, SML6 becomes sort of a swiss army knife
of game modding in Java.

All example code snippets in this document use groovy, unless otherwise
specified.

## Tasks

SML6 provides the following tasks:
- `org.stianloader.sml6.tasks.AggregateMappingsTask`
- `org.stianloader.sml6.tasks.DeobfuscateGameTask`
- `org.stianloader.sml6.tasks.DeployModsTask`
- `org.stianloader.sml6.tasks.FetchGameTask`
- `org.stianloader.sml6.tasks.GenerateEclipseRunTask`
- `org.stianloader.sml6.tasks.GenerateSourcesTask`
- `org.stianloader.sml6.tasks.RemapJarTask`
- `org.stianloader.sml6.tasks.SLLJavaExecTask`
- `org.stianloader.sml6.tasks.StripDependenciesTask`
- `org.stianloader.sml6.tasks.XZTarBallerTask`
- `org.stianloader.sml6.tasks.XZCompressTask`

Unlike gslStarplane, all tasks should be compatible with gradle's
configuration cache by default.

Please note that unlike gslStarplane, no tasks are configurated by default.
As such, you need to register the tasks in the way you want. Of course,
some conventions are applied for your sanity. Please refer to the
"Task configuration" section for more in-depth information on the configuration
of individual tasks.

SML6 also provides the following internal tasks; they are not meant for
direct use by API users:
- `org.stianloader.sml6.tasks.AbstractArtifactTask`

## Artifact transforms

SML6 provides the following artifact transforms:
- `org.stianloader.sml6.transforms.ApplyRASArtifactTransform`
- `org.stianloader.sml6.transforms.RemapArtifactTransform`

All artifact transforms provided by SML6 are cacheable.

Like the tasks in SML6, the artifact transformations are not configured by default,
meaning that they have to be explicitly registered. Further, some parameters need
to be set for them to work. However, some parameters will use sane defaults
that should work across the board.

## Attributes

SML6 provides the following attributes:
- `org.stianloader.sml6.transforms.attributes.RASTransform.RAS_TRANSFORM_ATTRIBUTE`: `Attribute<org.stianloader.sml6.transforms.attributes.RASTransform>`

These attributes are primarily for use in handling when artifact transforms are run.
However, SML6 will not automatically make use of those attributes and it is up to the
user to use them or not. These attributes can of course be substituted with home-brewed
attributes.

### Helper classes

SML6 provides the following miscellaneous helper classes:
- `org.stianloader.sml6.WellOfDespair`

These helper classes are meant to replace unusually high amounts of repetetive configuration work,
but their use isn't necessarily a requirement. Similarly, they might not be following best practices in the strictest sense.
Thus, if you have no idea what you are doing you probably shouldn't be using them.
If you have an idea on what you are doing, you probably know better approaches already, so don't use the helper
classes in that case.
If you are just copy-pasting a buildscript, then I suppose you can use it.

## Task configuration

### AggregateMappingsTask

The AggregateMappingsTask task defines following properties:
- `inputFormat` (**mandatory**): `Property<MappingFormat>`, defines the format in which the input files are stored in.
- `outputFormat` (**mandatory**): `Property<MappingFormat>`, defines the format of the output file.

The `AggregateMappingsTask` extends `AbstractArchiveTask`, meaning that the
task inputs and outputs can be defined as it should be expected of tasks of
that type.

The fully qualified name of `MappingFormat` is `net.fabricmc.mappingio.format.MappingFormat`
which is an enum provided by fabric's mapping-io library. For convinience, sml6 provides
the `inputFormat(String)` and `outputFormat(String)` methods to set the value of
the respective properties without having to use the fully qualified name.

In essence `AggregateMappingsTask` is a task that converts mapping files between
formats. However, the main usecase is converting an enigma directory into a single
file - usually the tinyv2 or enigma formats. Although it might be theoretically
possible to use this task to "explode" a mapping file into an enigma directory,
doing so is not particularly recommended due to the semantics of `AbstractArchiveTask`.

This task does not perform descriptor inferrence or other things that might be
required to convert from formats that might omit certain metadata.

Example task configuration:
```groovy
task aggregateToTiny(type: org.stianloader.sml6.tasks.AggregateMappingsTask) {
    from 'src/mappings'
    group = 'build'
    archiveExtension = 'tinyv2'

    inputFormat 'enigma'
    outputFormat 'tiny v2'
}
```

### DeobfuscateGameTask

The DeobfuscateGameTask task defines following properties:
- `autoDeobfVersion`: `Property<String>`, controlls the version of Autodeobf (only `"5.0.2"` supported at this point)
- `withAutodeobf`: `Property<Boolean>`, controlls whether to run Autodeobf
- `withSLDeobf`: `Property<Boolean>`, controlls whether to run slDeobf/oaktree
- `withSLDeobfMappings`: `Property<Boolean>`, controlls whether to generate slIntermediary mappings
- `outputDirectory`: `DirectoryProperty`, output files will be stored there by default
- `outputJar`: `RegularFileProperty`, the deobfuscated jar will be stored there
- `inputJar` (**mandatory**): `RegularFileProperty`, the obfuscated input jar
- `slIntermediaryMappings`: `RegularFileProperty`, output file for the slIntermediary mappings generated by sl-deobf. Uses the tinyv1 format.
- `spStarmapMappings`: `RegularFileProperty`, output file for the spStarmap mappings generated by Autodeobf. Uses the tinvyv1 format.

Unless otherwise specified, conventions exist that use sensible default
values. For non-galimulator games, you may want to change
`withAutodeobf`, `withSLDeobf`, and `withSLDeobfMappings`,
though honestly only `withAutodeobf` is really mandatory.
Keep in mind that SML6 expects games to always be a monojar,
an assumption that really does not make much sense. You are welcome
to change it (provided you ensure that it actually does what you want,
I can fix my end later :p).

Note: Although this goes against common sense, the Switchmap
classes get remapped via sl-deobf, but no mappings
file will be generated. So … just don't touch them. This issue
will be resolved eventually, but requires patches to sl-deobf.

### DeployModsTask

The `DeployModsTask` task defines following properties:
- `mods` (**mandatory**): `Property<FileCollection>`, the external mods to deploy into the mods directory.
- `modsDirectory`: `DirectoryProperty`, the path to the mods directory.

`DeployModsTask` deploys the given SLL mods jars into the mods directory. Previously installed versions of
the mods are removed from the mods directory. Mods that were previously part of the `mods` property, but aren't
anymore will continue to exist in the mods directory, so please beware of that. Neither will manually installed mods
be removed, provided they do not have the same name as any mods in the `mods` property.

This task is not intended for deploying mods you have just built, as that is usually unnecessary.
Further, unlike gsl-starplane's equivalent task, this task does not remap mod artifacts. For that, use a custom
artifact view and run transformers accordingly, see `RemapArtifactTransform` for more information on that
and on how to best set up this task.

### FetchGameTask

The FetchGameTask task defines following properties:
- `aggressiveCaching`: `Property<Boolean>`, whether to cache aggressively. This may easily result in outdated jars for games that update frequently, at the benefit of much better performance.
- `outputJar`: `RegularFileProperty`, the resolved game jar will be stored at the location provided by this property.
- `primaryGameJar`: `RegularFileProperty`, this is the primary location to look for the jar. If the jar does not exist at that location, only then it will try to find it through common steam installation directories. This property should be set to allow people to work with older versions of a game, as well as for CI/CD purposes.
- `steamApplicationId` (**mandatory**): `Property<Integer>`, the appId of the game to fetch. This is the primary method of obtaining the application installation directory. The installation directory will be resolved through Steam's application manifests.
- `steamApplicationName` (**mandatory**): `Property<String>`, the name of the steam application, or rather the name of the directory your game is located in relation to the "common" directory. This method of retrieving the installation dir is a fallback in case the appid approach does not work. Though quite frankly it is unlikely to work well. Also, keep in mind that external library folders will not get queried.
- `steamJarPath` (**mandatory**): `Property<String>`, the path of game jar in relation to the game's installation directory on steam.
- `symlinkDirectories`: `ListProperty<String>`, the path of directories in relation to the game's installation directory. These directories are symbolically linked (symlink) to the project folder under the game path. May not work depending on the OS and filesystem. Does nothing if the `primaryGameJar` or the `org.stianloader.sml6.gameJar` system property is set. Already existing target files and non-existing source files will be silently ignored.

SML6 expects games to be bundled as a single jar in the style of packr and roast.
However, many games are likely to be bundled across multiple jars. In that case, just create a
merge request to SML6 to implement the desired functionality. I unfortunately lack the resources to do the job
for you (after all, I won't be modding the same game as you).

This task is most well tested on linux, though different distros and methods of installing steam (e.g. through flatpak, ew)
may induce issues. Windows should theoretically work, but the used code is very old by now and hasn't been tested in years.
However, on MacOS this task will outright fail to function properly, though the `primaryGameJar` property can be useful to
work around this issue without compromising the experience for other users.

Example task configuration:
```groovy
task fetchGalim(type: org.stianloader.sml6.tasks.FetchGameTask) {
    steamApplicationName = 'Galimulator'
    steamApplicationId = 808100
    steamJarPath = 'jar/galimulator-desktop.jar'
}
```

### GenerateEclipseRunTask

The GenerateEclipseRunTask task defines following properties:
- `args` (**mandatory**): `ListProperty<CommandLineArgumentProvider>`, the command-line arguments passed to the main class
- `javaVersion` (**mandatory**): `Property<JavaVersion>`, the version of Java to use at runtime.
- `jvmArgs` (**mandatory**): `ListProperty<String>`, the JVM arguments to use (includes system properties).
- `mainClass` (**mandatory**): `Propert<String>`, the main class of the run.
- `mods` (**mandatory**): `ListProperty<FileCollection>`, all the present mods. A single `FileCollection` describes the classpath of a single mod 'unit', including resources.
- `moduleName`: `Property<String>`, value of the `org.eclipse.jdt.launching.MODULE_NAME` attribute. Probably best to leave it alone as this task doesn't handle JPMS well.
- `outputFile` (**mandatory**): `RegularFileProperty`, the destination file to write the launch config into.
- `projectName`: `Property<String>`, value of the `org.eclipse.jdt.launching.PROJECT_ATTR` attribute, also used for project-specific classpath entries. Automatically retrieved from the `EclipseModel`, or set to the project's name if the eclipse plugin is not present.
- `workingDir` (**mandatory**): `DirectoryProperty`, the working directory for the launch runtime. This property affects `workingDirPath`.
- `workingDirPath`: `Property<String>`, the working directory for the launch runtime. This property is automatically resolved through `workingDir`. This property is only required to prevent gradle from considering the task not up-to-date when contents within the working dir change, even though it's irrelevant for this task.

The `GenerateEclipseRunTask` generates an eclipse JDT-compatible `*.launch` file from any given `JavaExec` task.
However, the task can also be configured like any other other task if so desired, without having to use a `JavaExec` task as a base.

This task provides the `from(JavaExec)` and `from(Provider<JavaExec>)` methods to configure this task faster.
It will copy over all relevant properties. It even automatically sets the `outputFile` property to a sensible value!

The `from` methods will also copy over the `mods` property, albeit indirectory. More specifically, the given JavaExec task must
be a `SLLJavaExecTask`. Further, the location of the mods will be derived from `SLLJavaExecTask#usingModSourceSet` and it is assumed
that all those source sets are built by eclipse.

Example task configuration:
```groovy
task genEclipseRuns(type: org.stianloader.sml6.tasks.GenerateEclipseRunTask) {
    from tasks.named('runMods') // this is preferred over `from tasks['runMods']` or `from runMods` because this uses configure on demand.
}
```

### GenerateSourcesTask

The GenerateSourcesTask task defines following properties:
- `decompileOptions`: `VFDecompileOptions`, the options passed to the Vineflower decompiler
- `inputJar` (**mandatory**): `RegularFileProperty`, the obfuscated input jar
- `javadocSources`: `ListProperty<MIOMappingsProvider>`, 
- `libraryClasspath`: `Property<@Nullable FileCollection>`, a file collection that describes the library path forwarded to Vineflower. This file collection should only contain jars, other file types might not be supported.
- `lineRemappedOutputJar`: `RegularFileProperty`, the file in which the line remapped jar should be stored. If VF was configured to remap `LineNumberNode`s, then this output file's line numbers will correspond to the decompiled source code. Otherwise, this is the input jar.
- `outputDirectory`: `DirectoryProperty`, output files will be stored there by default
- `outputSourcesJar`: `RegularFileProperty`, the decompiled output jar will be stored at this location.
- `stripSourceDebug`: `Property<Boolean>`, whether to clear the SourceDebugExtension field in classes. Most specifically this clears SMAP files that would otherwise become invalid as sml6 does not yet know how to remap them. This mostly affects decompiled kotlin code. By convention, this property is set to `true`.

The GenerateSourcesTask has following subclass which is in turn used to configure the task further:
- `org.stianloader.sml6.tasks.GenerateSourcesTask.VFDecompileOptions`

The GenerateSourcesTask decompiles the given input jar, and produces a sources jar for that jar, as well as
a jar that contains line number information that corresponds to the sources jar (as well as possible that is,
the tooling makes plenty of mistakes for now).

The library classpath passed to the decompiler mostly affects `@Override` annotations, though it may affect
other small things such as casts when the type hierarchy isn't certain due to missing libraries or when
generic signatures aren't known.

This task provides the following methods to more easily configure the task:
- `addJavadocSourcesConfiguration(Action<MIOMappingsConfigurationProvider> configurationClosure)`: Register a `MIOMappingsConfigurationProvider` as javadoc source. This is probably the method you'll use the most of the addJavadocSources[…] trio.
- `addJavadocSourcesDir(Action<MIOMappingsDirectoryProvider> configurationClosure)`: Register a `MIOMappingsDirectoryProvider` as a javadoc source.
- `addJavadocSourcesFile(Action<MIOMappingsFileProvider> configurationClosure)`: Register a `MIOMappingsFileProvider` as a javadoc source.
- `decompileOptions(Action<VFDecompileOptions> action)`: Configure the decompiler options as a closure
- `getDecompileOptions()`: Get the `VFDecompileOptions` object used for configuration.

Example configuration:
```groovy
task genSources(type: org.stianloader.sml6.tasks.GenerateSourcesTask, dependsOn: stripGalim) {
    inputJar = stripGalim.outputJar
    libraryClasspath = stripGalim.strippingDependencies

    outputSourcesJar = outputDirectory.file('galimulator-' + deobfGalim.autodeobfVersion.get() + '-sources.jar')
    lineRemappedOutputJar = outputDirectory.file('galimulator-' + deobfGalim.autodeobfVersion.get() + '.jar')

    decompileOptions {
        removeSynthetic = false
        verifyAnonymousClasses = true
        setOption('include-runtime', true)
    }

    addJavadocSourcesFile {
        containerFormat = TAR_XZ
        mappingFormat = ENIGMA
        mappingSource = tarballXZ.archiveFile
        dstNamespaceId = -1 // Source namespace
    }
}
```

Most of the properties in the `VFDecompileOptions` class are automatically generated using Gradle's `PropertyMixIn`.
The automatically generated property names are case-insensitive versions of the constant names within
https://github.com/Vineflower/vineflower/blob/35f2c6e2b65746d8fc4358ced8de03d440f2b80b/src/org/jetbrains/java/decompiler/main/extern/IFernflowerPreferences.java

The `VFDecompileOptions` class also defines the `getKotlin` and `setKotlin(boolean)` method to enable the vineflower kotlin plugin,
see https://vineflower.org/usage/#plugin-kotlin - keep in mind that plugin arguments need to be set through `setOption(String, boolean)`
or `setOption(String, String)`. The only exception is the `--kt-enable` option which can be enabled/disabled using `setKotlin(boolean)`
(or, in groovy code using `kotlin = false`). The kotlin plugin is enabled by default, or rather SML6 inherits the default properties
for most arguments except for those related to line remapping infrastructure.

When decompiling classes that have the `.kt` file extension as per the debug source attribute whilst the kotlin plugin is disabled,
the `GenerateSourcesTask` will automatically change the file extension to `.java` inline to the actual vineflower output.
Keep in mind that it doesn't do that if line remapping isn't being performed however there is no rational reason to disable
line remapping anyways - at least not when working with an IDE and in that case you'll probably want to have the correct file extension.
For cases where line remapping isn't desired, the file extension probably doesn't matter.

### RemapJarTask

The RemapJarTask task defines following properties:
- `inputJar` (**mandatory**): `RegularFileProperty`, the input jar to remap
- `libraryJars`: `Property<FileCollection>`, defines a list of jars whoose contents should be used to inferr member realms. Especially required for mixin remapping.
- `mappings`: `ListProperty<MIOMappingsProvider>`, the mappings to use. See `addMappingsDir`/`addMappingsFile` for convinience methods to register a mappings directory/file.

The `RemapJarTask` extends `AbstractArtifactTask`, inheriting all it's properties.

The `RemapJarTask` is a task which remaps an input jar (it also remaps mixins and reversible-access-setters definitions), doing nothing
more. The `RemapJarTask` does not modify any of it's inputs (this is needed for caching to work in a sense).

This task provides the following methods to more easily configure the task:
- `addMappingsConfiguration(Action<MIOMappingsConfigurationProvider> configurationClosure)`: Register a `MIOMappingsConfigurationProvider` as mappings source. This is probably the method you'll use the most of the trio.
- `addMappingsDir(Action<MIOMappingsDirectoryProvider> configurationClosure)`: Register a `MIOMappingsDirectoryProvider` as a mappings source.
- `addMappingsFile(Action<MIOMappingsFileProvider> configurationClosure)`: Register a `MIOMappingsFileProvider` as a mappings source.

Example task configuration:
```groovy
task remapGalim(type: org.stianloader.sml6.tasks.RemapJarTask, dependsOn: stripGalim) {
    inputJar = stripGalim.outputJar
    libraryJars = stripGalim.strippingDependencies

    archiveBaseName = 'galimulator'
    archiveClassifier = 'remapped'
    archiveVersion = deobfGalim.autodeobfVersion
    destinationDirectory = layout.buildDirectory.dir('sml6/remapGalim')

    addMappingsFile {
        containerFormat = TAR_XZ
        mappingFormat = ENIGMA
        mappingSource = tarballXZ.archiveFile
    }
}
```

### SLLJavaExecTask

**WARNING:** This task is what one can describe as experimental. It doesn't seem to work well at this point and frankly more research is required.

The SLLJavaExecTask task extends `JavaExec`, inheriting all relevant properties and methods.

Additionally, it defines the following properties:
- `bootFiles`: `Property<FileCollection>`, list of all boot URLs. This is used for SLL to correctly handle the transforming classloader. Computed as the union of the `bootGameDependencies` and `bootGameJar` properties. Do not change directly unless necessary.
- `bootGameDependencies` (**mandatory**): `Property<FileCollection>`, dependency artifacts of `bootGameJar`. Tip: This can also be a `Configuration` (which is a `FileCollection`).
- `bootGameJar`: `RegularFileProperty`, the game jar that is the main focus of SLL's transforming classloader.
- `mods`: `ListProperty<FileCollection>`, all the present mods. A single `FileCollection` describes the classpath of a single mod 'unit', including resources.
- `gameMainClass` (**mandatory**): `Property<String>`, the main class which should be executed with the SLL root classloader after SLL finished initialization. Corresponds to the `de.geolykt.starloader.launcher.CLILauncher.mainClass` system property.
- `propertyExpansionSource`: `RegularFileProperty`, the path to a .properties file from which property expansions within the extension.json file should occur. Only affects mods declared via the `mods` property (and thus indirectly `usingModSourceSet`/`usingModTasks`). Bound to the `gradle.properties` file by convention.

The simplify the mod registration process, the `SLLJavaExecTask` provides the following methods:
- `void usingModSourceSet(Provider<AbstractCompile> classesOutput, Provider<SourceSet> resourceSet)`: Register a mod using the outputs of the `AbstractCompile` task and the resources defined by the given `SourceSet`.
- `void usingModTasks(Provider<AbstractCompile> classesOutput, Provider<ProcessResources> resourcesDir)`: Register a mod using the outputs of the `AbstractCompile` task for classes and the outputs of the `ProcessResources` task for resources.

Example task configuration:

```groovy
task runMods(type: org.stianloader.sml6.tasks.SLLJavaExecTask) {
    classpath configurations['sllLauncher']

    bootGameDependencies = stripGame.strippingDependencies
    bootGameJar = genSources.lineRemappedOutputJar
    gameMainClass = 'be.julien.particulitis.lwjgl3.Lwjgl3Launcher' // This is for Particulitix; for Galimulator use 'com.example.Main'

    usingModSourceSet(tasks.named('compileJava'), java.sourceSets.named('main'))

    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
```

### StripDependenciesTask

The DeobfuscateGameTask task defines following properties:
- `filteringEntryNames`: `ListProperty<String>`, stores a list of entry names that are stripped from the output jar. Wildcards, regex, and similar are not supported. Intended to be used to remove singular resources you don't know the dependency of (or are otherwise undesireable).
- `inputJar` (**mandatory**): `RegularFileProperty`, the fat input jar
- `outputDirectory`: `DirectoryProperty`, output files will be stored there by default
- `outputJar`: `RegularFileProperty`, the stripped down jar will be stored there
- `strippingDependencies` (**mandatory**): `Property<FileCollection>`, defines a list of jars whoose contents should be removed from the input jar. Calling any of the `stripGalimulatorDefaults502` methods will set this value.

This type provides following methods to more easily configure the task:
- `stripGalimulatorDefaults502()`: Strip galimulator dependencies using the builtin dependency list for Galimulator 5.0.2
- `stripGalimulatorDefaults502(Action<Configuration>)`: Same as above method, but the generated dependency configuration can be configured through a closure.
- `stripGalimulatorDefaults502(String, Action<Configuration>)`: Same as above method, but use a custom name for the generated configuration.

Example configuration of this task:

```groovy
task stripGalim(type: org.stianloader.sml6.tasks.StripDependenciesTask, dependsOn: deobfGalim) {
    inputJar = deobfGalim.outputJar

    stripGalimulatorDefaults502 {
        configurations['compileOnlyApi'].extendsFrom(it)
    }
}
```

The dependency notation is the same as the notation you use within the gradle `dependencies` block.

### XZTarBallerTask

The `XZTarBallerTask` class extends `Tar`.

The XZTarBallerTask task defines the following properties:
- `compressionLevel`: `Property<Integer>`, the compression level to use for XZ. Maximum value is 9, however for small-ish files the default of 6 does the same thing.

All properties of `Tar` also apply, with the exception of `compression`, which is unused.

The archive's default destination directory is by default set to
[BasePluginExtension#getDistsDirectory](https://docs.gradle.org/9.1.0/javadoc/org/gradle/api/plugins/BasePluginExtension.html#getDistsDirectory()).
The default archive name is similarly derived.
If the base plugin is absent, sane defaults will be used.

The XZTarBallerTask task is meant for shipping compressed mapping files. There is no real need
for using this exact task, and other compression methods can be used.
However, at the point of writing (2025-10-13), gslStarplane only supports .tar.xz and .xz
compression. Though that may be subject to change in future versions of gslStarplane.

XZTarBallerTask is only meant to compress engima mapping directories. For other formats,
use the XZCompressTask class instead.

Example task configuration:
```groovy
task tarballXZ(type: org.stianloader.sml6.tasks.XZTarBallerTask) {
    from 'src/mappings'
    group = 'build'
    archiveExtension = 'enigma.tar.xz'
}

assemble.dependsOn(tarballXZ)
```

### XZCompressTask

The `XZCompressTask` class extends `AbstractArtifactTask`, and thus shares many
of the properties present in `AbstractArchiveTask`, namely all the `archive`
properties, as well as the `destinationDirectory` property.

The `XZCompressTask` task defines the following properties:
- `compressionLevel`: `Property<Integer>`, the compression level to use for XZ. Maximum value is 9, however for small-ish files the default of 6 does the same thing.
- `inputFile`: `RegularFileProperty`, the input file to compress

To improve ease-of-use (or to just alleviate the pains of muscle-memory), the
`XZCompressTask` supports the `from(Object)` notation. However, keep in mind
that the method may only be called once. Further, `XZCompressTask` can only
compress a single file. To compress an entire directory or otherwise multiple
files at once, consider using `XZTarBallerTask` instead. The `into(Object)`
notation is not supported - not that it makes any sense in the first place.

The `XZCompressTask` task is meant for shipping compressed mapping files. There
is no real need for using this exact task, and other compression methods can be
used. However, at the point of writing (2025-Oct-13), gslStarplane only
supports .tar.xz and .xz compression. Though that may be subject to change in
future versions of gslStarplane.

The archive's default destination directory is set to the directory defined by
[BasePluginExtension#getDistsDirectory](https://docs.gradle.org/9.1.0/javadoc/org/gradle/api/plugins/BasePluginExtension.html#getDistsDirectory()).
The default archive name is similarly derived.
If the base plugin is absent, sane defaults will be used.

To express the results of the `XZCompressTask` as a `PublishArtifact`,
use the `asArtifact()` method provided by `AbstractArtifactTask`.
You will need to mainly use this for maven publications.

Example task configuration:
```groovy
task compressXZ(type: org.stianloader.sml6.tasks.XZCompressTask) {
    from 'tiny-file.tiny'
    group = 'build'
    archiveExtension = 'tiny.xz'
}

assemble.dependsOn(compressXZ)

publishing {
    publications {
        mavenJava(MavenPublication) {
            groupId = project.group
            artifactId = project.base.archivesName.get()
            version = project.version

            artifact compressXZ.asArtifact()
        }
    }
}
```

## Task configuration objects

### MIOMappingsProvider

Fully qualfiied name: `org.stianloader.sml6.tasks.config.MIOMappingsProvider`

The `MIOMappingsProvider` class defines following properties:
- `dstNamespaceId`: `Property<Integer>` (optional, default: 0), the destination namespace id according to MIO. Should be equal to the namespace of the input jar.
- `srcNamespaceId`: `Property<Integer>` (optional, default: -1), the source namespace id according to MIO. Not used in the javadocs process, but is used in the remapping process.
- `mappingFormat` (**mandatory**): `Property<net.fabricmc.mappingio.format.MappingFormat>`, the used mapping format. Common values: `ENIGMA_DIR`, `TINY_FILE`, or `TINY_2_FILE`. Depends on the concrete input file though.

Instances of `MIOMappingsProvider`'s subclasses need to be created through gradle's object factory (or just use helper methods).
That being said, attempting to instantiate instances of this classes' subclasses is not exactly recommended.
Creating instances of `MIOMappingsProvider` directly will fail, as for all intents and purposes it is an abstract class.

This class uses gradle's `PropertyMixIn` struct to make the `MappingFormat` and `MappingContainer` constants easily available as read-only constants.
These properties are case-insensitive. Further, following aliases are applied:
- `enigma` for `ENIGMA_DIR`
- `tiny` for `TINY_FILE`
- `tiny2` for `TINY_2_FILE`
- `tinyv2` for `TINY_2_FILE`
These aliases are also case-insensitive. Also please note that `PropertyMixIn` has little effects outside of closures,
where you'd need to use the fully qualified path (or import statements but those are malpractice in stianloader buildscripts).
Hence, one should prefer to configure things on demand using closures.

### MIOMappingsConfigurationProvider

Fully qualified name: `org.stianloader.sml6.tasks.config.MIOMappingsConfigurationProvider`

This class extends `MIOMappingsProvider`, inheriting its properties.

The `MIOMappingsFileProvider` class defines following properties:
- `mappingSource` (**mandatory**): `Property<FileCollection>`, the location of the mappings file(s) to use (each file will be treated as it's own unit and summed up).
- `containerFormat` (**mandatory**): `Property<org.stianloader.sml6.starplane.remapping.MIOContainerFormat.MappingContainer>`, the container format (either `PLAIN`, `TAR_XZ`, or `XZ`)

Keep in mind that `MappingContainer` constants are easily exposed through `PropertyMixIn`, see the documentation for `MIOMappingsProvider`.

Instances of this class need to be created through gradle's object factory, passing a `Project` instance as an argument.

This class also works for any other kind of `FileCollection`, though it is primarily intended to be used for
`Configuration`s. Also: `Configuration` extends `FileCollection`, so you can just use it as-is.
For example, using a configuration for mappings in the `RemapJarTask`:

```groovy
configurations {
    mappings
}

task remapGalim(type: org.stianloader.sml6.tasks.RemapJarTask) {
    inputJar = stripGalim.outputJar
    libraryJars = stripGalim.strippingDependencies

    archiveBaseName = 'galimulator'
    archiveClassifier = 'remapped'
    archiveVersion = deobfGalim.autodeobfVersion
    destinationDirectory = layout.buildDirectory.dir('sml6/remapGalim')

    addMappingsConfiguration {
        containerFormat = XZ
        mappingFormat = tinyV2
        mappingSource = configurations['mappings']
    }
}

dependencies {
    mappings 'de.geolykt:bstarmap:0.0.1-a20260327@tinyv2.xz'
}
```

Using a `Configuration` as an input for an artifact transform is not supported by gradle 9.7.0 due to the configuration cache. Use `MIOMappingsFileProvider` instead
for remote resources, making use of its `downloadResource` helper method.

### MIOMappingsDirectoryProvider

Fully qualified name: `org.stianloader.sml6.tasks.config.MIOMappingsDirectoryProvider`

This class extends `MIOMappingsProvider`, inheriting its properties.

The `MIOMappingsDirectoryProvider` class also defines following properties:
- `mappingSource` (**mandatory**): `DirectoryProperty` (mandatory), the location of the mappings directory

Instances of this class need to be created through gradle's object factory.

### MIOMappingsFileProvider

Fully qualified name: `org.stianloader.sml6.tasks.config.MIOMappingsFileProvider`

This class extends `MIOMappingsProvider`, inheriting its properties.

The `MIOMappingsFileProvider` class defines following properties:
- `mappingSource` (**mandatory**): `RegularFileProperty`, the location of the mappings file to use
- `containerFormat` (**mandatory**): `Property<org.stianloader.sml6.starplane.remapping.MIOContainerFormat.MappingContainer>`, the container format (either `PLAIN`, `TAR_XZ`, or `XZ`)

Keep in mind that `MappingContainer` constants are easily exposed through `PropertyMixIn`, see the documentation for `MIOMappingsProvider`.

Instances of this class need to be created through gradle's object factory.

This class provides the `downloadResource(String uri, String sha256, String sha512)` helper method. It replaces the assignment to `mappingsSource` as it already does the assignment internally.
The requested resource is only downloaded on demand and cached in a project-local directory. The file is downloaded anew on checksum mismatch, if no checksums are given, or if the file name changes.
If the downloaded file does not match the provided checksums, an exception is thrown when resolving the `mappingsSource` file (i.e. whenever the MIO mappings provider is being used).

Example use of this method:

```groovy
addMappingsFile {
    containerFormat = XZ
    mappingFormat = TINY_2
    downloadResource("https://stianloader.org/maven/de/geolykt/bstarmap/${bStarmapVersion}/bstarmap-${bStarmapVersion}.tinyv2.xz", "$bStarmapSha256", "$bStarmapSha512")
}
```

with

```properties
bStarmapVersion=0.0.1-a20260327
bStarmapSha256=5ed621df235f482dd15a391bad8a43be3644660cafe9519f180090d138dc4fe0
bStarmapSha512=fa897f99286a3edd973e4f23c7360b0dd7cd0c967b2278107120cd1c01e1439b6757ecf9a38e56561cc1f3e4ca5b392106bf3356559fc16ad615966dc8a217ce
```

being in the `gradle.properties` file.

The intended use of this method is when a remote file is used as an input for an artifact transform, as `Configuration`s cannot be used in gradle 9.7.0 due to the configuration cache. 
Due to security reasons, `http` is not supported and thus `https` is effectively the only protocol supported (as the method is using Java's `HttpClient` API)

Alternatively, the `downloadMavenResource(String dependencyNotation, String repoId, String repoURI)` helper method can be used. This method has the same use as `downloadResource`,
except that it will also query the local maven repository. This method uses picoresolve for artifact resolution, bypassing gradle's artifact resolution. The cache directory in which
artifacts are downloaded to is `~/.m2/repository`. Artifacts don't necessarily need to exist in the given remote repository. This method is most useful when testing mappings for runtime
collisions.

The `downloadMavenResource` method can be used as follows:
```groovy
    addMappingsFile {
        containerFormat = XZ
        mappingFormat = TINY_2
        downloadMavenResource("de.geolykt:bstarmap:${bStarmapVersion}@tinyv2.xz", 'stianloader-nightlies', 'https://stianloader.org/maven/')
    }
```

## Configuring artifact transforms

### ApplyRASArtifactTransform

Fully qualified name: `org.stianloader.sml6.transforms.ApplyRASArtifactTransform`

This `TransformAction` can be parametrized with the following properties:
- `fastTransform`: `Property<Boolean>`, if enabled, skip the `ClassNode` stage and write back the bytecode as-is when the class is believed to not be a target. Default value is `false`.
- `inputFile` (**mandatory**): `RegularFileProperty`, the location of your reversible access setter file that should be applied on the artifact set.
- `namespace`: `Property<String>`, the name of the ras location for debugging and error reporting purposes. The default value is derived from the `inputFile` property.
- `outputArtifactType`: `Property<String>`, the suffix that should be attached to the classifier if applicable. If the string is empty, the output file name is equal to the input file name. The default value of this property is derived from `scope`.
- `reversed`: `Property<Boolean>`, whether to inverse/undo the RAS application. Default value is `false`.
- `scope` (**mandatory**): `Property<de.geolykt.starloader.ras.ReversibleAccessSetterContext.RASTransformScope>`, the scope of the artifact transformation. Either `BUILDTIME` or `RUNTIME`. Depending on the scope certain transformations will not be applied. See RAS's documentation for more information on this behaviour.

For convinience purposes, this transform's parameter class supplies the following constants to more easily configure the task:
- `BUILDTIME`, a `de.geolykt.starloader.ras.ReversibleAccessSetterContext.RASTransformScope`
- `RUNTIME`, a `de.geolykt.starloader.ras.ReversibleAccessSetterContext.RASTransformScope`

The `ApplyRASArtifactTransform` transformation applies a reversible-access-setter transformation on the configuration.
The reversible-access-setter (RAS) standard is stianloader's equivalent of AccessWideners or AccessTransformers. However, unlike those
two formats, RAS is, as it's name implies, reversible. This matters especially for hacks concerning removing ACC_ENUM from enum classes.
Those hacks work at compile-time but don't at runtime. However, your IDE will use compile-time classes to run the application with.
This leads towards potential classloading issues unless the classloader is properly vetted and there is proper interplay between IDE,
buildscript and classloading infrastructure.

RAS also has the advantage of being able to manipulate all kinds of flags that AT/AW can't or have difficulty modifying.
Yet, there are instances where you have to access a synthetic member, override an enum class, or do other things that might at first sound
completely absurd. Yet, modding a video game at times require such brute methods to bend the ecosystem to your will.

Example transformation configuration (including registering the transform):
```groovy
// WARNING: Attribute definitions for demonstration purposes only. Use the `RASTransform` attribute if possible instead.

dependencies {
    registerTransform(org.stianloader.sml6.transforms.ApplyRASArtifactTransform) {
        from.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, 'jar')
        to.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, 'ras-compile-jar')

        parameters {
            scope = BUILDTIME
            inputFile = project.file("compile-hacks.ras")
        }
    }
}

configurations.testCompileClasspath.attributes {
    attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, 'ras-compile-jar')
}

configurations.compileClasspath.attributes {
    attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, 'ras-compile-jar')
}
```

Note: The above example transforms all incoming artifacts on a configuration level and the method
is generally incompatible with other approaches given that it sets the artifact type attribute.
Instead, consider using the `RASTransform` attribute defined by SML6 (this attribute is documented, too).

### RemapArtifactTransform

Fully qualified name: `org.stianloader.sml6.transforms.RemapArtifactTransform`

This `TransformAction` can be parametrized with the following properties:
- `outputArtifactType`: `Property<String>`, the suffix that should be attached to the classifier if applicable. If the string is empty, the output file name is equal to the input file name. The default value of this property is `null`.
- `mappings`: `ListProperty<MIOMappingsProvider>`, the mappings to use. See `addMappingsFile` for convinience methods to register a mappings file. Note: Using a `Configuration` as an input for an artifact transform is not supported by gradle and thus the relevant convinience methods have been omitted here.
- `libraryJars`: `ConfigurableFileCollection`, defines a list of jars whoose contents should be used to inferr member realms. Especially required for mixin remapping.

This transform provides the following methods to more easily configure the task:
- `addMappingsDir(Action<MIOMappingsDirectoryProvider> configurationClosure)`: Register a `MIOMappingsDirectoryProvider` as a mappings source.
- `addMappingsFile(Action<MIOMappingsFileProvider> configurationClosure)`: Register a `MIOMappingsFileProvider` as a mappings source.

Example configuration of the transform:

```groovy
// Required for properly generating member realms, as otherwise obfuscation can be an issue
task stripObfGame(type: org.stianloader.sml6.tasks.StripDependenciesTask) {
    inputJar = fetchGame.outputJar
    filteringEntryNames = stripGame.filteringEntryNames
    strippingDependencies = stripGame.strippingDependencies
}

task deployMods(type: org.stianloader.sml6.tasks.DeployModsTask) {
    dependsOn stripObfGame // Required for the artifact transform to not blow up

    // This must be an artifact view as otherwise the GAVCE notation will cause variants to not be selected as we would like.
    // Related gradle forum post: <https://discuss.gradle.org/t/52166>.
    mods = configurations.named('dependencyMods').map {
        it.incoming.artifactView {
            attributes {
                attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, 'predeploy-mod')
            }
        }.files
    }
}

dependencies {
    registerTransform(org.stianloader.sml6.transforms.RemapArtifactTransform) {
        from.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, 'jar')
        to.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, 'predeploy-mod')

        parameters {
            libraryJars.from(stripObfGame.outputJar)

            addMappingsFile {
                containerFormat = PLAIN
                mappingFormat = TINY
                mappingSource = deobfGame.slIntermediaryMappings
            }

            addMappingsFile {
                containerFormat = PLAIN
                mappingFormat = TINY
                mappingSource = deobfGame.spStarmapMappings
            }

            addMappingsFile {
                containerFormat = XZ
                mappingFormat = TINY_2
                downloadMavenResource("de.geolykt:bstarmap:${bStarmapVersion}@tinyv2.xz", 'stianloader-nightlies', 'https://stianloader.org/maven/')
            }
        }
    }
}
```

## Attribute configuration

### RASTransform

Full qualified path to attribute definition: `org.stianloader.sml6.transforms.attributes.RASTransform.RAS_TRANSFORM_ATTRIBUTE`.
The Attribute is of type: `Attribute<org.stianloader.sml6.transforms.attributes.RASTransform>`.
The id of the attribute is: `org.stianloader.sml6.ras` 

Standard values defined in `org.stianloader.sml6.transforms.attributes.RASTransform`:
- `BUILDTIME = TRANSFORMED_BUILDTIME = "transformed-buildtime"`
- `RUNTIME = TRANSFORMED_RUNTIME = "transformed-runtime"`
- `NO_TRANSFORM = "no-transform"`
- `TRANSFORM = "transform"`

To create a `RASTransform` instance, use `objects.named(org.stianloader.sml6.transforms.attributes.RASTransform, 'transform')`.

Standard disambiguation rules (**experimental** - behaviour subject to change): `org.stianloader.sml6.transforms.attributes.RASDisambiguationRule`
Standard compatibility rules (**experimental** - behaviour subject to change): `org.stianloader.sml6.transforms.attributes.RASCompatibilityRule`

This attribute is intended to have finer control over RAS artifact transformations. More specifically, whilst one
could transform all artifacts, it makes more sense to only transform artifacts that need to be altered.

Example configuration for the attribute (including the `ApplyRASArtifactTransform`):
```groovy
dependencies {
    attributesSchema { // (1)
        attribute(org.stianloader.sml6.transforms.attributes.RASTransform.RAS_TRANSFORM_ATTRIBUTE) {
            disambiguationRules.add(org.stianloader.sml6.transforms.attributes.RASDisambiguationRule)
            compatibilityRules.add(org.stianloader.sml6.transforms.attributes.RASCompatibilityRule)
        }
    }

    artifactTypes { // (2)
        named(ArtifactTypeDefinition.JAR_TYPE) {
            attributes.attribute(org.stianloader.sml6.transforms.attributes.RASTransform.RAS_TRANSFORM_ATTRIBUTE, objects.named(org.stianloader.sml6.transforms.attributes.RASTransform, 'transform'))
        }
    }

    registerTransform(org.stianloader.sml6.transforms.ApplyRASArtifactTransform) { // (3)
        from.attribute(org.stianloader.sml6.transforms.attributes.RASTransform.RAS_TRANSFORM_ATTRIBUTE, objects.named(org.stianloader.sml6.transforms.attributes.RASTransform, 'transform'))
        to.attribute(org.stianloader.sml6.transforms.attributes.RASTransform.RAS_TRANSFORM_ATTRIBUTE, objects.named(org.stianloader.sml6.transforms.attributes.RASTransform, 'transform-buildtime'))

        parameters {
            scope = BUILDTIME
            inputFile = project.file('src/main/resources/s2dmenues.ras')
            outputArtifactType = ''
        }
    }
}
```

`(1)` defines the RASTransform attribute and registers appropriate disambiguation and compatibility rules.

`(2)` defines that by default all JAR artifacts are transformable by applying the `transform` attribute on all JAR artifacts
by default.

All artifacts with the `transform` attribute can be transformed into an artifact with the `transform-buildtime` attribute by executing the
`ApplyRASArtifactTransform` defined through `(3)`.

An artifact can be transformed by setting the requested artifacts attributes accordingly. For example:
```groovy
dependencies {
    compileOnly(':galimulator-remapped:5.0.2') {
        attributes {
            attribute(org.stianloader.sml6.transforms.attributes.RASTransform.RAS_TRANSFORM_ATTRIBUTE, objects.named(org.stianloader.sml6.transforms.attributes.RASTransform, 'transform-buildtime'))
        }
    }
}
```

Note: Artifacts will not get transformed unless requested. Further, source and javadoc jars will not get transformed
using this trick due to gradle reasons.

Note 2: It is not possible to add an attribute to an artifact without the attribute having been there previously,
nor is it possible to remove an attribute. This is another gradle issue (see <https://github.com/gradle/gradle/issues/30786>).
This behaviour might however get changed in the future with future releases of gradle, so make sure to follow that issue.

Note 3: When specifying the attributes of the dependencies, the attribute view of a configuration doesn't necessarily
need to be touched. Though both approaches can be used together, it generally shows that something is wrong when doing so.

## Helper classes

### WellOfDespair

Fully qualified name: `org.stianloader.sml6.WellOfDespair`

The well of eternal torment, agony, and despair - short, the well of despair - is a helper class for dealing with the
most agonizing part of conventional build tools: Registering ficticious dependencies and consuming them.

That would be already difficult enough on its own, but IDEs are a thing people probably want to use and those need to also consume
the dependencies and handle them properly. Usually the IDE is the biggest joykill there.

Historically, gslStarplane and early SML6-based projects used `flatDir` repositories and thus conviniently side-stepped the issue
by making the dependencies real in some sense. Unfortunately there's another joykill out there: The gradle configuration cache.
While the `flatDir` approach isn't outright forbidden, it won't work if you have the configuration cache enabled and the
flatfile repository is absent. A problem you'll only realize once you're trying to build the project on another computer or in a CI environment.
Also `flatDir` is a dead end and isn't supported by gradle. Instead, you should use own-project variants for this usecase,
but … IDEs don't support them! So yeah, `WellOfDespair` exposes quick workarounds to hammer the support into the IDEs.

Oh, and also - this class only supports Eclipse. IJ probably works decently enough, but probably with a few bugs (see [IDEA-379056](https://youtrack.jetbrains.com/issue/IDEA-379056/Auxiliary-artifacts-not-used-for-non-module-backed-project-dependencies) )

This class provides following static methods:
- `registerDependency(Project, NamedDomainObjectProvider<Configuration>, Action<VariantArtifactConfigurator>)`

`VariantArtifactConfigurator` exposes following properties:
- `artifactJar` (**mandatory**): `RegularFileProperty`
- `capabilityName` (**mandatory**, write-only): `String`
- `configurationName` (**mandatory**, write-only): `String`
- `injectEclipseClasspath`: `boolean`, whether to inject the dependency into the project classpath of the eclipse model. The default value is `true`.
- `sourceJar` (**mandatory**): `RegularFileProperty`

`VariantArtifactConfigurator` also exposes following methods:
- `configuration(Action<ConsumableConfiguration>)` - sets the closure to be executed when the variant configuration is registered
- `dependency(Action<ModuleDependency>)` - sets the closure to be executed when the own-project variant dependency is registered

All methods (static or not) that consume an `Action` also come with a variant consuming `Closure` instead.

The `NamedDomainObjectProvider<Configuration>` argument of the `registerDependency` method is the `Configuration` to which the
dependency should be registered to.

Example use of this method:
```groovy
org.stianloader.sml6.WellOfDespair.registerDependency(project, configurations.named('compileOnly')) {
    configurationName = 'galimulatorElements'
    capabilityName = 'org.stianloader:galimulator:5.0.2'
    artifactJar = genSources.lineRemappedOutputJar
    sourcesJar = genSources.outputSourcesJar

    it.dependency {
        attributes {
            attribute(org.stianloader.sml6.transforms.attributes.RASTransform.RAS_TRANSFORM_ATTRIBUTE, objects.named(org.stianloader.sml6.transforms.attributes.RASTransform, 'transform-buildtime'))
        }
    }
}
```

This roughly corresponds to the following groovy code (minus IDE behaviour workaround logic):
```groovy
configurations {
    consumable('galimulatorElements') {
        attributes {
            attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.JAVA_API))
            attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling, Bundling.EXTERNAL))
            attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.JAR))
        }

        artifacts {
            add('galimulatorElements', genSources.lineRemappedOutputJar)
        }

        outgoing {
            capability('org.stianloader:galimulator:5.0.2')
        }
    }
}

dependencies {
    compileOnly(project(project.path)) {
        attributes {
            attribute(org.stianloader.sml6.transforms.attributes.RASTransform.RAS_TRANSFORM_ATTRIBUTE, objects.named(org.stianloader.sml6.transforms.attributes.RASTransform, 'transform-buildtime'))
        }

        capabilities {
            requireCapability('org.stianloader:galimulator:5.0.2')
        }
    }
}
```
