package org.stianloader.sml6.transforms;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.gradle.api.artifacts.transform.CacheableTransform;
import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Classpath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import software.coley.cafedude.classfile.ClassFile;
import software.coley.cafedude.classfile.ConstPool;
import software.coley.cafedude.classfile.Method;
import software.coley.cafedude.classfile.attribute.Attribute;
import software.coley.cafedude.classfile.attribute.CodeAttribute;
import software.coley.cafedude.classfile.constant.CpClass;
import software.coley.cafedude.classfile.constant.CpEntry;
import software.coley.cafedude.classfile.constant.CpFieldRef;
import software.coley.cafedude.classfile.constant.CpMethodRef;
import software.coley.cafedude.classfile.constant.CpNameType;
import software.coley.cafedude.classfile.constant.CpUtf8;
import software.coley.cafedude.classfile.instruction.CpRefInstruction;
import software.coley.cafedude.classfile.instruction.Instruction;
import software.coley.cafedude.classfile.instruction.Opcodes;
import software.coley.cafedude.io.ClassFileReader;
import software.coley.cafedude.io.ClassFileWriter;
import software.coley.llzip.ZipIO;
import software.coley.llzip.format.compression.ZipCompressions;
import software.coley.llzip.format.model.LocalFileHeader;
import software.coley.llzip.format.model.PartType;
import software.coley.llzip.format.model.ZipArchive;
import software.coley.llzip.format.model.ZipPart;
import software.coley.llzip.format.write.JavaZipWriterStrategy;
import software.coley.llzip.format.write.ZipWriterStrategy;
import software.coley.llzip.util.BufferData;
import software.coley.llzip.util.ByteData;
import software.coley.llzip.util.ByteDataUtil;

@CacheableTransform
public abstract class MigrateLWJGL3ArtifactTransform implements TransformAction<TransformParameters.None> {
    @NotNull
    private static final String APPCFG3 = "com/badlogic/gdx/backends/lwjgl3/Lwjgl3ApplicationConfiguration";

    private static final Map<String, String> DIRECT_MAPPINGS = new HashMap<>();

    @NotNull
    private static final String HELPER3 = "org/stianloader/lwjgl3ify/Helper";

    static {
        MigrateLWJGL3ArtifactTransform.DIRECT_MAPPINGS.put("com/badlogic/gdx/backends/lwjgl/LwjglApplication", "com/badlogic/gdx/backends/lwjgl3/Lwjgl3Application");
        MigrateLWJGL3ArtifactTransform.DIRECT_MAPPINGS.put("com/badlogic/gdx/backends/lwjgl/LwjglApplicationConfiguration", "com/badlogic/gdx/backends/lwjgl3/Lwjgl3ApplicationConfiguration");
    }

    public static void invoke(@NotNull Path source, @NotNull Path target) {
        try (ZipArchive archive = ZipIO.readJvm(source)) {
            MigrateLWJGL3ArtifactTransform.invoke(Objects.requireNonNull(archive, "'archive' may not be null"), target);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void invoke(@NotNull ZipArchive archive, @NotNull Path target) throws IOException {
        Iterator<ZipPart> parts = archive.getParts().iterator();

        while (parts.hasNext()) {
            ZipPart part = parts.next();

            if (part.type() != PartType.LOCAL_FILE_HEADER) {
                continue;
            }

            LocalFileHeader header = (LocalFileHeader) part;

            if (!header.getFileNameAsString().endsWith(".class")) {
                continue;
            }

            ByteData segment = ZipCompressions.decompress(header);
            ByteData data = MigrateLWJGL3ArtifactTransform.transformClassBytes(Objects.requireNonNull(segment, "'segment' may not be null"));

            if (data != null) {
                header.setFileData(data);
                header.setCompressionMethod(ZipCompressions.STORED);
                // Other values (such as the CRC or the compressed/decompressed size) are not needed right now due to the implementation of the JavaZipWriterStrategy.
            }
        }

        ZipWriterStrategy writeStrategy = new JavaZipWriterStrategy();

        try {
            writeStrategy.writeToDisk(archive, target);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static boolean remapSignature(StringBuilder signatureOut, String signature, int start, int end) {
        if (start == end) {
            return false;
        }

        int type = signature.codePointAt(start++);

        switch (type) {
        case 'T':
            // generics type parameter
            // fall-through intended as they are similar enough in format compared to objects
        case 'L':
            // object
            // find the end of the internal name of the object
            int endObject = start;

            while(true) {
                // this will skip a character, but this is not interesting as class names have to be at least 1 character long
                int codepoint = signature.codePointAt(++endObject);

                if (codepoint == ';') {
                    String name = signature.substring(start, endObject);
                    String newName = MigrateLWJGL3ArtifactTransform.DIRECT_MAPPINGS.get(name);
                    boolean modified = false;

                    if (newName != null) {
                        name = newName;
                        modified = true;
                    }

                    signatureOut.appendCodePoint(type);
                    signatureOut.append(name);
                    signatureOut.append(';');
                    modified |= MigrateLWJGL3ArtifactTransform.remapSignature(signatureOut, signature, ++endObject, end);
                    return modified;
                } else if (codepoint == '<') {
                    // generics - please no
                    // post scriptum: well, that was a bit easier than expected
                    int openingBrackets = 1;
                    int endGenerics = endObject;

                    while(true) {
                        codepoint = signature.codePointAt(++endGenerics);

                        if (codepoint == '>' ) {
                            if (--openingBrackets == 0) {
                                break;
                            }
                        } else if (codepoint == '<') {
                            openingBrackets++;
                        }
                    }

                    String name = signature.substring(start, endObject);
                    String newName = MigrateLWJGL3ArtifactTransform.DIRECT_MAPPINGS.get(name);
                    boolean modified = false;

                    if (newName != null) {
                        name = newName;
                        modified = true;
                    }

                    signatureOut.append('L');
                    signatureOut.append(name);
                    signatureOut.append('<');
                    modified |= remapSignature(signatureOut, signature, endObject + 1, endGenerics++);
                    signatureOut.append('>');
                    // apparently that can be rarely be a '.', don't ask when or why exactly this occurs
                    signatureOut.appendCodePoint(signature.codePointAt(endGenerics));
                    modified |= remapSignature(signatureOut, signature, ++endGenerics, end);
                    return modified;
                }
            }
        case '+':
            // idk what this one does - but it appears that it works good just like it does right now
        case '*':
            // wildcard - this can also be read like a regular primitive
            // fall-through intended
        case '(':
        case ')':
            // apparently our method does not break even in these cases, so we will consider them raw primitives
        case '[':
            // array - fall through intended as in this case they behave the same
        default:
            // primitive
            signatureOut.appendCodePoint(type);
            return remapSignature(signatureOut, signature, start, end); // Did not modify the signature - but following operations could
        }
    }

    private static String remapSingleDesc(String input, StringBuilder sharedBuilder) {
        int indexofL = input.indexOf('L');
        if (indexofL == -1) {
            return input;
        }
        int length = input.length();
        String internalName = input.substring(indexofL + 1, length - 1);
        String newInternalName = MigrateLWJGL3ArtifactTransform.DIRECT_MAPPINGS.get(internalName);
        if (newInternalName == null) {
            return input;
        }
        sharedBuilder.setLength(indexofL + 1);
        sharedBuilder.setCharAt(indexofL, 'L');
        while(indexofL != 0) {
            sharedBuilder.setCharAt(--indexofL, '[');
        }
        sharedBuilder.append(newInternalName);
        sharedBuilder.append(';');
        return sharedBuilder.toString();
    }

    private static boolean transformClass(ClassFile file) {
        boolean transformed = false;
        ConstPool pool = file.getPool();
        StringBuilder sharedBuilder = new StringBuilder();

        for (int i = 0; i < pool.size(); i++) {
            CpEntry entry = pool.get(i);

            if (entry instanceof CpUtf8) {
                CpUtf8 utf = (CpUtf8) entry;
                String in = utf.getText();

                if (in.length() == 0) {
                    continue;
                }

                String mapping;

                if (in.codePointAt(0) == '(') {
                    sharedBuilder.setLength(0);
                    sharedBuilder.appendCodePoint('(');

                    if (!MigrateLWJGL3ArtifactTransform.remapSignature(sharedBuilder, in, 1, in.length())) {
                        continue;
                    }

                    mapping = sharedBuilder.toString();
                } else if (in.codePointBefore(in.length()) == ';') {
                    mapping = MigrateLWJGL3ArtifactTransform.remapSingleDesc(in, sharedBuilder);

                    if (mapping == in) {
                        continue;
                    }
                } else {
                    mapping = MigrateLWJGL3ArtifactTransform.DIRECT_MAPPINGS.get(in);

                    if (mapping == null) {
                        continue;
                    }
                }
        
                utf.setText(mapping);
                transformed = true;
            }
        }

        for (Method method : file.getMethods()) {
            for (Attribute attr : method.getAttributes()) {
                if (!(attr instanceof CodeAttribute)) {
                    continue;
                }

                CodeAttribute code = (CodeAttribute) attr;
                List<Instruction> instructions = code.getInstructions();
                int insnLen = instructions.size();

                for (int i = 0 ; i < insnLen; i++) {
                    Instruction insn = instructions.get(i);

                    if (insn.getOpcode() == Opcodes.PUTFIELD) {
                        CpFieldRef fieldRef = (CpFieldRef) ((CpRefInstruction) insn).getEntry();
                        String cname = fieldRef.getClassRef().getName().getText();

                        if (cname.equals(MigrateLWJGL3ArtifactTransform.APPCFG3)) {
                            CpNameType nameType = fieldRef.getNameType();
                            String name = nameType.getName().getText();
                            String type = nameType.getType().getText();
                            String mname = "set" + ((char) Character.toUpperCase(name.codePointAt(0))) + name.substring(1);
                            String mtype; 
                            int opcode;
                            CpClass clazz;

                            if (mname.equals("setWidth") || mname.equals("setHeight")) {
                                opcode = Opcodes.INVOKESTATIC;
                                mtype = "(L" + MigrateLWJGL3ArtifactTransform.APPCFG3 + ";" + type + ")V";
                                clazz = MigrateLWJGL3ArtifactTransform.writeClass(pool, MigrateLWJGL3ArtifactTransform.writeUTF8(pool, MigrateLWJGL3ArtifactTransform.HELPER3));
                            } else if (mname.equals("setForegroundFPS")) {
                                // The method doesn't exist in libGDX 1.9.11, so we need to use the closest matching thing.
                                mname = "setIdleFPS";
                                opcode = Opcodes.INVOKEVIRTUAL;
                                mtype = "(" + type + ")V";
                                clazz = fieldRef.getClassRef();
                            } else {
                                opcode = Opcodes.INVOKEVIRTUAL;
                                mtype = "(" + type + ")V";
                                clazz = fieldRef.getClassRef();
                            }

                            assert clazz != null;

                            CpMethodRef methodRef = MigrateLWJGL3ArtifactTransform.writeMethodRef(pool,
                                    clazz,
                                    MigrateLWJGL3ArtifactTransform.writeNameType(pool,
                                            MigrateLWJGL3ArtifactTransform.writeUTF8(pool, mname),
                                            MigrateLWJGL3ArtifactTransform.writeUTF8(pool, mtype)));

                            instructions.set(i, new CpRefInstruction(opcode, methodRef));
                            transformed = true;
                        }
                    } else if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        CpMethodRef methodRef = (CpMethodRef) ((CpRefInstruction) insn).getEntry();
                        String cname = methodRef.getClassRef().getName().getText();

                        if (cname.equals(MigrateLWJGL3ArtifactTransform.APPCFG3)) {
                            CpNameType nameType = methodRef.getNameType();
                            CpUtf8 name = nameType.getName();
                            if (name.getText().equals("addIcon")) {
                                instructions.set(i, new CpRefInstruction(Opcodes.INVOKESTATIC,
                                        MigrateLWJGL3ArtifactTransform.writeMethodRef(pool, MigrateLWJGL3ArtifactTransform.writeClass(pool, MigrateLWJGL3ArtifactTransform.writeUTF8(pool, MigrateLWJGL3ArtifactTransform.HELPER3)),
                                                MigrateLWJGL3ArtifactTransform.writeNameType(pool, name, MigrateLWJGL3ArtifactTransform.writeUTF8(pool, "(L" + MigrateLWJGL3ArtifactTransform.APPCFG3 + ";Ljava/lang/String;Lcom/badlogic/gdx/Files$FileType;)V")))));

                                transformed = true;
                            }
                        }
                    }
                }
            }
        }

        return transformed;
    }

    @Nullable
    private static ByteData transformClassBytes(@NotNull ByteData data) {
        if (data.length() <= 0) {
            return null;
        }

        byte[] bytes = ByteDataUtil.toByteArray(data);

        try {
            ClassFile file = new ClassFileReader().read(bytes);
            boolean transformed = MigrateLWJGL3ArtifactTransform.transformClass(file);

            if (!transformed) {
                return null;
            }

            return BufferData.wrap(Objects.requireNonNull(new ClassFileWriter().write(file)));
        } catch (Throwable t) {
            LoggerFactory.getLogger(MigrateLWJGL3ArtifactTransform.class).warn("Unable to transform bytes", t);
            return null;
        }
    }

    @NotNull
    private static CpClass writeClass(@NotNull ConstPool pool, @NotNull CpUtf8 name) {
        for (CpEntry entry : pool) {
            if (entry instanceof CpClass cpClass
                    && cpClass.getName() == name) {
                return cpClass;
            }
        }

        CpClass cpClass = new CpClass(name);
        pool.add(cpClass);

        return cpClass;
    }

    @NotNull
    private static CpMethodRef writeMethodRef(@NotNull ConstPool pool, @NotNull CpClass clazz, @NotNull CpNameType nameType) {
        for (CpEntry entry : pool) {
            if (entry instanceof CpMethodRef mref
                    && mref.getClassRef() == clazz
                    && mref.getNameType() == nameType) {
                return mref;
            }
        }

        CpMethodRef ref = new CpMethodRef(clazz, nameType);
        pool.add(ref);
        return ref;
    }

    @NotNull
    private static CpNameType writeNameType(@NotNull ConstPool pool, @NotNull CpUtf8 name, @NotNull CpUtf8 type) {
        for (CpEntry entry : pool) {
            if (entry instanceof CpNameType cpNameType
                    && cpNameType.getName() == name
                    && cpNameType.getType() == type) {
                return cpNameType;
            }
        }

        CpNameType cpNameType = new CpNameType(name, type);
        pool.add(cpNameType);

        return cpNameType;
    }

    @NotNull
    private static CpUtf8 writeUTF8(@NotNull ConstPool pool, @NotNull String str) {
        for (CpEntry entry : pool) {
            if (entry instanceof CpUtf8 cpUtf8
                    && cpUtf8.getText().equals(str)) {
                return cpUtf8;
            }
        }

        CpUtf8 cpUtf8 = new CpUtf8(str);
        pool.add(cpUtf8);
        return cpUtf8;
    }

    @InputArtifact
    @Classpath
    public abstract Provider<FileSystemLocation> getInputArtifact();

    @Override
    public void transform(TransformOutputs outputs) {
        Path input = this.getInputArtifact().get().getAsFile().toPath();
        MigrateLWJGL3ArtifactTransform.invoke(input, outputs.file(input.getFileName().toString()).toPath());
    }
}
