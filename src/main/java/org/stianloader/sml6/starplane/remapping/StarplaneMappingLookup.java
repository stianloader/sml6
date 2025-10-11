package org.stianloader.sml6.starplane.remapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.stianloader.remapper.MemberRef;
import org.stianloader.remapper.Remapper;
import org.stianloader.remapper.SimpleMappingLookup;

public class StarplaneMappingLookup extends SimpleMappingLookup {

    @NotNull
    private final Path map;

    private final boolean reverse;
    private final boolean ignoreNonExistentFiles;

    public StarplaneMappingLookup(@NotNull Path map, boolean reversed) {
        this(map, reversed, false);
    }

    public StarplaneMappingLookup(@NotNull Path map, boolean reversed, boolean ignoreNonExistentFiles) {
        this.map = map;
        this.reverse = reversed;
        this.ignoreNonExistentFiles = ignoreNonExistentFiles;
    }

    @NotNull
    @Contract(mutates = "this", pure = false, value = "-> this")
    public StarplaneMappingLookup load() throws IOException {
        if (this.ignoreNonExistentFiles && Files.notExists(this.map)) {
            return this;
        }

        // For reversed mappings to take effect correctly, we need to "delay" the application of member mappings
        // until all classes were mapped - as this could have an effect on owner name and the member descriptor
        // of the target namespace (which acts as the source namespace in reversed mappings - it's confusing, I know).
        // The contents of the string array are the same as the tiny columns. That is index 1 for owner (in the source namespace),
        // index 2 for descriptor (in the source namespace), index 3 the name of the member in the source namespace
        // and index 4 the name of the member in the destination namespace.
        // Of course there is still index 0 but that one can be disregarded as it will be FIELD or METHOD.
        // The SimpleMappingLookup is required to lookup the non-reversed class names in order to be able
        // to swap the namespaces of the owner and descriptors. Note that this lookup instance
        // will not be filled in if there is no reversal to be done (this.reverse == false).
        SimpleMappingLookup classLookup = new SimpleMappingLookup();
        List<@NotNull String[]> delayedMemberMappings = new ArrayList<>();

        int lineNr = 0;
        try (BufferedReader br = Files.newBufferedReader(this.map, StandardCharsets.UTF_8)) {
            // the first line must specify the version of tiny and the namespace.
            // we are going to ignore the namespace as they just produce too much headache
            // - instead, we assume that the user (usually a developer) knows what they are
            // doing and give them this free ticket to exploit the system to the degree tolerable.
            // This is done as in galimulator modding (especially under gsl-starplane) there is
            // no real concept of namespaces, that is each mapping contributes to a broader
            // project, with the user being able to specify infinitely more as they please
            // - as long as they are compatible with each other of course.
            // So while "vanilla" obfuscated mappings could be attributed to official,
            // slIntermediary to intermediary and spStarmap to named, this distinction makes
            // little sense as the intermediary and named channels are technically speaking
            // incomplete.
            String header = br.readLine();
            lineNr++;
            if (header == null || header.isBlank()) {
                br.close();
                throw new IOException("No tiny header present (empty file?).");
            }
            String[] headerTokens = header.split("\\s+");
            if (headerTokens.length != 3) {
                br.close();
                throw new IOException("The tiny header had " + headerTokens.length + " tokens, however it is expected to be exactly 3.");
            }
            if (!headerTokens[0].equals("v1")) {
                br.close();
                throw new IOException("This method can only read tiny v1 maps.");
            }
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                lineNr++;
                if (line.charAt(0) == '#') { // fast short-circuiting
                    continue;
                }
                line = line.split("#", 2)[0];
                if (line == null || line.isBlank()) {
                    continue;
                }
                @NotNull String[] colums = line.split("\\s+");
                String type = colums[0].toUpperCase(Locale.ROOT);
                if (type.equals("CLASS")) {
                    // Format: CLASS originalName newName
                    if (colums.length != 3) {
                        throw new IOException("Line " + lineNr + " is of type CLASS, but only " + colums.length + " colums are present, even though it expects 3.");
                    }
                    if (this.reverse) {
                        super.remapClass(colums[2], colums[1]);
                        classLookup.remapClass(colums[1], colums[2]);
                    } else {
                        super.remapClass(colums[1], colums[2]);
                    }
                } else if (type.equals("METHOD")) {
                    if (colums.length != 5) {
                        throw new IOException("Line " + lineNr + " is of type CLASS, but only " + colums.length + " colums are present, even though it expects 5.");
                    }
                    // The official tinyV1 mappings format that actually makes sense
                    // Format: METHOD owner descriptor originalName newName
                    //                11111 2222222222 333333333333 4444444
                    delayedMemberMappings.add(colums);
                } else if (type.equals("FIELD")) {
                    // Format: FIELD owner descriptor originalName newName
                    //               11111 2222222222 333333333333 4444444
                    if (colums.length != 5) {
                        throw new IOException("Line " + lineNr + " is of type CLASS, but only " + colums.length + " colums are present, even though it expects 5.");
                    }
                    delayedMemberMappings.add(colums);
                }
            }
        }

        StringBuilder descBuilder = new StringBuilder();

        for (@NotNull String[] columns : delayedMemberMappings) {
            String srcOwner = columns[1];
            String srcDesc = columns[2];
            String srcName = columns[3];
            String dstName = columns[4];

            if (this.reverse) {
                boolean field = srcDesc.codePointAt(0) != '(';
                // Swap source and destination namespace descriptors and classes
                srcOwner = classLookup.getRemappedClassName(srcOwner);
                if (field) {
                    srcDesc = Remapper.getRemappedFieldDescriptor(classLookup, srcDesc, descBuilder);
                } else {
                    srcDesc = Remapper.getRemappedMethodDescriptor(classLookup, srcDesc, descBuilder);
                }
                // Swap source and destination namespace names
                String swapStore = srcName;
                srcName = dstName;
                dstName = swapStore;
            }

            super.remapMember(new MemberRef(srcOwner, srcName, srcDesc), dstName);
        }
        return this;
    }

    @Override
    public String toString() {
        return "SP Mapping Lookup [map=" + this.map.getFileName() + ",reversed=" + this.reverse + "]";
    }
}
