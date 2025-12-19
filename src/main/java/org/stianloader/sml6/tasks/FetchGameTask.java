package org.stianloader.sml6.tasks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

import javax.inject.Inject;

import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@DisableCachingByDefault(because = "Already caches internally")
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
        this.setGroup("SML6");
        this.getSteamApplicationName().convention("Galimulator");
        this.getSteamApplicationId().convention(808100);
        this.getSteamJarPath().convention("jar/galimulator-desktop.jar");
        this.getAggressiveCaching().convention(true);
        DirectoryProperty buildDir = this.getLayout().getBuildDirectory();
        Provider<String> taskNameProvider = this.getProviders().provider(this::getName);
        Provider<Directory> cacheDir = buildDir.dir(taskNameProvider.map(s -> "sml6-" + s));
        this.getOutputJar().convention(cacheDir.map(d -> d.file("galimulator-clean.jar")));
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
            File gameDir = this.getGameDir(applicationName);

            if (gameDir != null && gameDir.exists()) {
                String steamJarPath = this.getSteamJarPath().get();
                if (steamJarPath == null) {
                    throw new AssertionError("steamJarPath is null for task " + this.getPath());
                }
                cleanGameJar = new File(gameDir, steamJarPath);
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
    protected File getGameDir(@NotNull String game) {
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
        File common = new File(appdata, "common");
        return new File(common, game);
    }

    @Inject
    protected abstract ProjectLayout getLayout();

    @OutputFile
    public abstract RegularFileProperty getOutputJar();

    @InputFile
    @Optional
    public abstract RegularFileProperty getPrimaryGameJar();

    @Inject
    protected abstract ProviderFactory getProviders();

    @Input
    @Optional
    @Deprecated // Not yet used
    public abstract Property<Integer> getSteamApplicationId();

    @Input
    @Optional
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
    @Optional
    public abstract Property<String> getSteamJarPath();
}
