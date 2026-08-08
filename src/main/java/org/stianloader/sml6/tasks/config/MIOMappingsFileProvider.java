package org.stianloader.sml6.tasks.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;

import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stianloader.sml6.starplane.remapping.MIOContainerFormat.MappingContainer;

import net.fabricmc.mappingio.tree.MemoryMappingTree;

public abstract class MIOMappingsFileProvider extends MIOMappingsProvider {

    private static boolean checksumMatches(byte @NotNull[] data, @NotNull String digestAlgorithm, @NotNull String digestHex) {
        MessageDigest digest;

        try {
            digest = MessageDigest.getInstance(digestAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false; // better safe than sorry there
        }

        byte[] messageDigest = digest.digest(data);

        final StringBuilder hex = new StringBuilder(2 * messageDigest.length);

        for (final byte b : messageDigest) {
            int x = ((int) b) & 0x00FF;

            if (x < 16) {
                hex.append('0');
            }

            hex.append(Integer.toHexString(x));
        }

        return hex.toString().equalsIgnoreCase(digestHex);
    }

    public void downloadResource(@NotNull String resourceURI, @Nullable String sha256Hash, @Nullable String sha512Hash) {
        int idx = resourceURI.lastIndexOf('/');

        if (idx < 0) {
            throw new IllegalArgumentException("Invalid uri: " + resourceURI);
        }

        String name = resourceURI.substring(idx + 1);

        Provider<RegularFile> file = this.getLayout().getBuildDirectory().file("mappings-cache/" + name).map(f -> {
            Path p = f.getAsFile().toPath();

            if (Files.exists(p)) {
                try {
                    byte[] data = Files.readAllBytes(p);
                    boolean csumMatch = sha256Hash != null || sha512Hash != null;

                    if (csumMatch && sha256Hash != null) {
                        csumMatch = MIOMappingsFileProvider.checksumMatches(data, "SHA-256", sha256Hash);
                    }

                    if (csumMatch && sha512Hash != null) {
                        csumMatch = MIOMappingsFileProvider.checksumMatches(data, "SHA-512", sha512Hash);
                    }

                    if (csumMatch) {
                        return f;
                    }
                } catch (IOException ignored) { }
            }

            URI uri = URI.create(resourceURI);

            if (!"https".equals(uri.getScheme())) {
                throw new IllegalStateException("Unsupported scheme: '" + uri.getScheme() + "' (note: only https is supported, http is not supported for security reasons) for URI: " + uri);
            }

            HttpClient client = HttpClient.newHttpClient();

            try {
                HttpResponse<byte[]> response = client.send(HttpRequest.newBuilder(uri).GET().build(), BodyHandlers.ofByteArray());

                byte[] body = response.body();

                if ((response.statusCode() / 100) != 2) {
                    String respString = null;

                    if (body.length < 500) {
                        respString = new String(body);

                        for (char c : respString.toCharArray()) {
                            if (Character.isISOControl(c) && (c != '\n' || c != '\r')) {
                                respString = null;
                                break;
                            }
                        }
                    }

                    throw new IOException("Fetching resource '" + uri + "' with GET yielded status code " + response.statusCode() + (respString != null ? ": " + respString : ""));
                }

                if (body == null) {
                    throw new IllegalStateException("Response without body?");
                }

                if (sha256Hash != null && !MIOMappingsFileProvider.checksumMatches(body, "SHA-256", sha256Hash)) {
                    throw new IllegalStateException("SHA-256 checksum does not match!");
                }

                if (sha512Hash != null && !MIOMappingsFileProvider.checksumMatches(body, "SHA-512", sha512Hash)) {
                    throw new IllegalStateException("SHA-512 checksum does not match!");
                }

                Files.createDirectory(p.getParent());
                Files.write(p, body, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException("Cannot download resource: " + resourceURI, e);
            }

            return f;
        });

        this.getMappingSource().set(file);
    }

    @Input
    public abstract Property<MappingContainer> getContainerFormat();

    @Inject
    protected abstract ProjectLayout getLayout();

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getMappingSource();

    @Override
    @NotNull
    public MemoryMappingTree loadTree() throws IOException {
        MemoryMappingTree visitor = new MemoryMappingTree();
        this.getContainerFormat().get().read(this.getMappingFormat().get(), this.getMappingSource().get().getAsFile().toPath(), visitor);
        return visitor;
    }
}
