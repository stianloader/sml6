package org.stianloader.sml6.tasks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.provider.ListProperty;
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
import org.jetbrains.annotations.Nullable;
import org.stianloader.sml6.SML6GradlePlugin;
import org.stianloader.sml6.VDFReader;

@CacheableTask
public abstract class FetchGameTask extends ConventionTask {
    private static final String OPERATING_SYSTEM = System.getProperty("os.name");
    private static final String STEAM_WINDOWS_REGISTRY_INSTALL_DIR_KEY = "InstallPath";
    private static final String STEAM_WINDOWS_REGISTRY_KEY = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Wow6432Node\\Valve\\Steam";

    @Nullable
    private static final File getOneOfExistingFiles(@NotNull String... paths) {
        for (String path : paths) {
            File file = new File(path);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    /**
     * Stupid little hack.
     *
     * @param location path in the registry
     * @param key registry key
     * @return registry value or null if not found
     * @author Oleg Ryaboy, based on work by Miguel Enriquez; Made blocking by Geolykt
     */
    private static final String readWindowsRegistry(String location, String key) {
        try {
            // Run reg query, then read it's output
            Process process = Runtime.getRuntime().exec("reg query " + '"' + location + "\" /v " + key);

            process.waitFor();
            InputStream is = process.getInputStream();
            String output = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            is.close();

            if (!output.contains(location) || !output.contains(key)) {
                return null;
            }

            // Parse out the value
            // For me this results in:
            // [, HKEY_LOCAL_MACHINE\SOFTWARE\Wow6432Node\Valve\Steam, InstallPath, REG_SZ, D:\Programmes\Steam]
            String[] parsed = output.split("\\s+");
            return parsed[parsed.length-1];
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public FetchGameTask() {
        this.setGroup(SML6GradlePlugin.DEFAULT_TASK_GROUP);
        this.setDescription("Fetch the game jar from Steam or another source.");
        this.getAggressiveCaching().convention(true);

        DirectoryProperty buildDir = this.getLayout().getBuildDirectory();
        Provider<String> taskNameProvider = this.getProviders().provider(this::getName);
        this.getOutputDirectory().convention(buildDir.dir(taskNameProvider.map(s -> "sml6/" + s)));
        this.getOutputJar().convention(this.getOutputDirectory().file("game-vanilla.jar"));
    }

    @TaskAction
    public void fetchJar() throws IOException {
        File cleanGameJar = null;
        boolean cachedGameJar = false;
        if (this.getAggressiveCaching().get()) {
            cleanGameJar = this.getOutputJar().getAsFile().getOrNull();
            if (cleanGameJar != null && cleanGameJar.exists()) {
                this.getLogger().info("Reusing cached game jar for task {}.", this.getPath());
                cachedGameJar = true;
            } else {
                cleanGameJar = null;
            }
        }

        if (cleanGameJar == null && this.getPrimaryGameJar().isPresent()) {
            cleanGameJar = this.getPrimaryGameJar().getAsFile().get();
            if (!cleanGameJar.exists()) {
                this.getLogger().warn("Primary game jar for task {} was not found at {}", this.getPath(), cleanGameJar.getAbsolutePath());
                cleanGameJar = null;
            }
        }

        found:
        if (cleanGameJar == null) {
            String propertyPath = System.getProperty("org.stianloader.sml6.gameJar");

            if (propertyPath != null) {
                cleanGameJar = this.getLayout().getProjectDirectory().getAsFile().toPath().resolve(propertyPath).toFile();
                if (cleanGameJar.exists()) {
                    break found;
                }
                this.getLogger().warn("Game jar at '{}' not found for task {}.", cleanGameJar.getAbsolutePath(), this.getPath());
                cleanGameJar = null;
            } else {
                this.getLogger().debug("System property 'org.stianloader.sml6.gameJar' not defined.");
            }

            // obtain game directory
            String applicationName = this.getSteamApplicationName().get();

            if (applicationName == null) {
                throw new AssertionError("steamApplicationName is null for task " + this.getPath());
            }

            File gameDir = this.getGameDir(this.getSteamApplicationId().getOrElse(-1), applicationName);

            if (gameDir != null && gameDir.exists()) {
                String steamJarPath = this.getSteamJarPath().get();

                if (steamJarPath == null) {
                    throw new AssertionError("steamJarPath is null for task " + this.getPath());
                }

                cleanGameJar = new File(gameDir, steamJarPath);

                // Symlink directories
                for (String dir : this.getSymlinkDirectories().getOrElse(Collections.emptyList())) {
                    Path target = this.getLayout().getProjectDirectory().getAsFile().toPath().resolve(dir);
                    Path source = gameDir.toPath().resolve(dir);

                    if (Files.exists(target) || Files.notExists(source)) {
                        continue;
                    }

                    Files.createSymbolicLink(target, source);
                }

                if (cleanGameJar.exists()) {
                    break found;
                }

                this.getLogger().error("Unable to resolve game jar file (was able to resolve the potential directory though)! Candidate path: '{}' for task '{}'", cleanGameJar, this.getPath());
                cleanGameJar = null;
            } else {
                this.getLogger().error("Unable to resolve game directory! Expected at '{}' for task '{}'", gameDir, this.getPath());
            }

            throw new IllegalStateException("Cannot resolve dependencies");
        }

        if (!cachedGameJar) {
            Files.copy(cleanGameJar.toPath(), this.getOutputJar().get().getAsFile().toPath(), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Input
    @Optional
    public abstract Property<Boolean> getAggressiveCaching();

    @Nullable
    protected File getGameDir(int steamAppId, @NotNull String game) {
        File steamExec = this.getSteamExecutableDir();

        if (steamExec == null || !steamExec.exists()) {
            if (FetchGameTask.OPERATING_SYSTEM.toLowerCase(Locale.ROOT).startsWith("win")) {
                steamExec = FetchGameTask.getOneOfExistingFiles("C:\\Steam\\", "C:\\Program Files (x86)\\Steam\\", "C:\\Program Files\\Steam\\", "D:\\Steam\\", "C:\\Programmes\\Steam\\", "D:\\Programmes\\Steam\\", "D:\\SteamLibrary\\", "E:\\SteamLibrary\\", "F:\\SteamLibrary\\", "C:\\SteamLibrary\\");
            }

            if (steamExec == null) {
                return null;
            }
        }

        if (!steamExec.isDirectory()) {
            throw new IllegalStateException("Steam executable directory not a directory.");
        }

        File appdata = new File(steamExec, "steamapps");

        readLibraryFolders:
        if (appdata.isDirectory()) {
            File libraryFoldersVDF = new File(appdata, "libraryfolders.vdf");

            if (!libraryFoldersVDF.exists()) {
                this.getLogger().warn("Library descriptor file '{}' does not exist!", libraryFoldersVDF);
                break readLibraryFolders;
            }

            Map<@NotNull String, @NotNull Object> libraries;

            try (Reader reader = Files.newBufferedReader(libraryFoldersVDF.toPath(), StandardCharsets.UTF_8)) {
                Map.Entry<@NotNull String, @NotNull Map<@NotNull String, @NotNull Object>> rootEntry = VDFReader.readVDF(reader);

                if (!rootEntry.getKey().equals("libraryfolders")) {
                    throw new IOException("Root entry key is '" + rootEntry.getKey() + "', expected 'libraryfolders'");
                }

                libraries = rootEntry.getValue();
            } catch (IOException e) {
                this.getLogger().warn("Cannot read library descriptor file '{}'.", libraryFoldersVDF, e);
                break readLibraryFolders;
            }

            List<@NotNull String> libraryPaths = new ArrayList<>();

            for (Object library : libraries.values()) {
                if (!(library instanceof Map)) {
                    this.getLogger().warn("Malformed library descriptor file '{}': Library is not a map.", libraryFoldersVDF);
                    continue;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> coercedLibrary = (Map<String, Object>) library;

                Object path = coercedLibrary.getOrDefault("path", null);

                if (path == null) {
                    this.getLogger().warn("Malformed library descriptor file '{}': Library without path.", libraryFoldersVDF);
                    continue;
                } else if (!(path instanceof String)) {
                    this.getLogger().warn("Malformed library descriptor file '{}': Library path not a string.", libraryFoldersVDF);
                    continue;
                }

                libraryPaths.add((String) path);
            }

            for (String libraryPath : libraryPaths) {
                Path rootDir = Paths.get(libraryPath);

                if (Files.notExists(rootDir)) {
                    this.getLogger().warn("Steam library at '{}' does not exist! Did a volume get unmounted?", rootDir);
                    continue;
                }

                Path manifest = rootDir.resolve("steamapps/appmanifest_" + steamAppId + ".acf");

                if (Files.notExists(manifest)) {
                    this.getLogger().debug("Missing manifest '{}' in library. Skipping library.", manifest);
                    continue;
                }

                Map<@NotNull String, @NotNull Object> appState;

                try (Reader reader = Files.newBufferedReader(manifest, StandardCharsets.UTF_8)) {
                    Map.Entry<@NotNull String, @NotNull Map<@NotNull String, @NotNull Object>> rootEntry = VDFReader.readVDF(reader);

                    if (!rootEntry.getKey().equals("AppState")) {
                        throw new IOException("Root entry key is '" + rootEntry.getKey() + "', expected 'AppState'");
                    }

                    appState = rootEntry.getValue();
                } catch (IOException e) {
                    this.getLogger().warn("Cannot read application manifest file '{}'.", manifest, e);
                    continue;
                }

                String installDir = (String) appState.get("installdir");
                Path installPath = manifest.resolveSibling("common").resolve(installDir);

                if (Files.notExists(installPath)) {
                    this.getLogger().warn("Application manifest file '{}' declares absent installation directory '{}'.", manifest, installPath);
                    continue;
                }

                this.getLogger().debug("Game installation path '{}' resolved through manifest '{}'", installPath, manifest);

                return installPath.toFile();
            }
        }

        File common = new File(appdata, "common");
        return new File(common, game);
    }

    @Inject
    protected abstract ProjectLayout getLayout();

    @Internal("Transitively affects other output locations. Not used directly.")
    public abstract DirectoryProperty getOutputDirectory();

    @OutputFile
    public abstract RegularFileProperty getOutputJar();

    @InputFile
    @Optional
    @PathSensitive(PathSensitivity.ABSOLUTE)
    public abstract RegularFileProperty getPrimaryGameJar();

    @Inject
    protected abstract ProviderFactory getProviders();

    @Input
    public abstract Property<Integer> getSteamApplicationId();

    @Input
    public abstract Property<String> getSteamApplicationName();

    @Nullable
    @Internal
    protected File getSteamExecutableDir() {
        if (FetchGameTask.OPERATING_SYSTEM.toLowerCase(Locale.ROOT).startsWith("win")) {
            String val = FetchGameTask.readWindowsRegistry(FetchGameTask.STEAM_WINDOWS_REGISTRY_KEY, FetchGameTask.STEAM_WINDOWS_REGISTRY_INSTALL_DIR_KEY);
            if (val == null) {
                return null;
            }
            return new File(val);
        } else {
            // Assuming UNIX, though for real we should check other OSes
            String homeDir = System.getProperty("user.home");
            if (homeDir == null) {
                return null;
            }
            File usrHome = new File(homeDir);
            File steamHome = new File(usrHome, ".steam");
            if (steamHome.exists()) {
                // some installs have the steam directory located in ~/.steam/debian-installation
                File debianInstall = new File(steamHome, "debian-installation");
                if (debianInstall.exists()) {
                    return debianInstall;
                } else {
                    return new File(steamHome, "steam");
                }
            }
            // Steam folder not located in ~/.steam, checking in ~/.local/share
            File local = new File(usrHome, ".local");
            if (!local.exists()) {
                return null; // Well, we tried...
            }
            File share = new File(local, "share");
            if (!share.exists()) {
                return null;
            }
            return new File(share, "Steam");
        }
    }

    @Input
    public abstract Property<String> getSteamJarPath();

    @Input
    public abstract ListProperty<String> getSymlinkDirectories();
}
