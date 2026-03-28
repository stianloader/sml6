package org.stianloader.sml6;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jetbrains.annotations.NotNull;

/**
 * Stupidly simple VDF reader
 */
public class VDFReader implements AutoCloseable {
    @NotNull
    public static Map.Entry<@NotNull String, @NotNull Map<@NotNull String, @NotNull Object>> readVDF(@NotNull Reader inputReader) throws IOException {
        try (VDFReader reader = new VDFReader(inputReader)) {
            return reader.readRoot();
        }
    }

    private final StringBuilder accumulationBuffer = new StringBuilder();

    private final Reader reader;

    protected VDFReader(@NotNull Reader delegate) {
        this.reader = delegate;
    }

    @Override
    public void close() throws IOException {
        this.reader.close();
    }

    @NotNull
    public Map.Entry<@NotNull String, @NotNull Map<@NotNull String, @NotNull Object>> readRoot() throws IOException {
        for (int ch = this.reader.read(); ch >= 0; ch = this.reader.read()) {
            if (ch == '{') {
                String key = this.accumulationBuffer.toString().strip();

                if (key.charAt(0) != '\"' || key.charAt(key.length() - 1) != '\"') {
                    throw new IOException("Key '" + key + "' is not a string.");
                }

                key = key.substring(1, key.length() - 1);

                Entry<@NotNull String, @NotNull Map<@NotNull String, @NotNull Object>> entry = Map.entry(key, this.readMap());

                assert entry != null; // Eclipse bug. Even with EEAs I can't get it to cooperate.

                return entry;
            } else {
                this.accumulationBuffer.append((char) ch);
            }
        }

        throw new EOFException();
    }

    @NotNull
    protected Map<@NotNull String, @NotNull Object> readMap() throws IOException {
        Map<@NotNull String, @NotNull Object> map = new LinkedHashMap<>();

        while (true) {
            int ch = this.reader.read();

            if (ch < 0) {
                throw new EOFException("Premature end of stream whilst attempting to read expected key.");
            } else if (!Character.isWhitespace(ch)) {
                if (ch == '\"') {
                    String key = this.readString0();

                    while (true) {
                        ch = this.reader.read();

                        if (ch < 0) {
                            throw new EOFException("Premature end of stream whilst attempting to read expected value.");
                        } else if (Character.isWhitespace(ch)) {
                            continue;
                        } else if (ch == '\"') {
                            // string value
                            map.put(key, this.readString0());
                            break;
                        } else if (ch == '{') {
                            // map value
                            map.put(key, this.readMap());
                            break;
                        } else {
                            throw new IOException("Non-whitespace character encountered before expected value began.");
                        }
                    }
                } else if (ch == '}') {
                    return map;
                } else {
                    throw new IOException("Non-whitespace character encountered before key began.");
                }
            }
        }
    }

    @NotNull
    protected String readString0() throws IOException {
        this.accumulationBuffer.setLength(0);
        // read until next quote

        while (true) {
            int ch = this.reader.read();

            if (ch < 0) {
                throw new EOFException("Premature end of input stream encountered whilst reading a string");
            } else if (ch == '\"') {
                break;
            } else {
                this.accumulationBuffer.append((char) ch);
            }
        }

        return this.accumulationBuffer.toString();
    }
}
