package org.stianloader.sml6.starplane.autodeobf;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import de.geolykt.starloader.deobf.LIFOQueue;
import de.geolykt.starloader.deobf.MethodReference;
import de.geolykt.starloader.deobf.StackElement;
import de.geolykt.starloader.deobf.StackWalker;
import de.geolykt.starloader.deobf.StackWalker.StackWalkerConsumer;
import de.geolykt.starloader.deobf.remapper.ConflicitingMappingException;
import de.geolykt.starloader.deobf.remapper.Remapper;
import de.geolykt.starloader.deobf.stack.source.AbstractSource;
import de.geolykt.starloader.deobf.stack.source.FieldSource;

/**
 * Automatic specialised deobfuscation for galimulator
 */
public class Autodeobf502 implements StarmappedNames502, AutodeobfRunner {

    private static final String ACTOR_CLASS = "snoddasmannen/galimulator/actors/Actor";
    private static final String ACTOR_CREATOR_CLASS = "snoddasmannen/galimulator/actors/StateActorCreator";
    private static final String ALLIANCE_CLASS = "snoddasmannen/galimulator/Alliance";
    private static final String AMBIENT_SNOWFLAKE_EFFECT_CLASS = "snoddasmannen/galimulator/effects/AmbientSnowflakeEffect";
    private static final String AMBIENT_STAR_EFFECT_CLASS = "snoddasmannen/galimulator/effects/AmbientStarEffect";
    private static final String ARTIFACT_CLASS = "snoddasmannen/galimulator/artifacts/Artifact";
    private static final String AUDIO_SAMPLE_CLASS = "snoddasmannen/galimulator/AudioManager$AudioSample";
    private static final String BITMAP_STAR_GENERATOR_CLASS = "snoddasmannen/galimulator/BitmapStarGenerator";
    private static final String DEBUG_CLASS = "snoddasmannen/galimulator/Debug";
    private static final String EMPIRE_CLASS = BASE_PACKAGE + "Empire";
    private static final String EMPIRE_SPECIAL_CLASS = BASE_PACKAGE + "EmpireSpecial";
    private static final String EMPLOYER_CLASS = BASE_PACKAGE + "Employer";
    private static final String EMPLOYMENT_AGENCY_CLASS = BASE_PACKAGE + "EmploymentAgency";
    private static final String ENUM_SETTINGS_CLASS = "snoddasmannen/galimulator/Settings$EnumSettings";
    private static final String FLOW_LAYOUT_CLASS = "snoddasmannen/galimulator/ui/FlowLayout";
    private static final String FRACTAL_STAR_GENERATOR_CLASS = "snoddasmannen/galimulator/FractalStarGenerator";
    private static final String GALCOLOR_CLASS = BASE_PACKAGE + "GalColor";
    private static final String GALFX_CLASS = "snoddasmannen/galimulator/GalFX";
    private static final String GALFX_DRAW_NINEPATCH_DESCRIPTOR = "(Lcom/badlogic/gdx/graphics/g2d/NinePatch;IIIILsnoddasmannen/galimulator/GalColor;Lcom/badlogic/gdx/graphics/Camera;)V";
    private static final String GALFX_DRAW_TEXT_DESCRIPTOR = "(FFFLcom/badlogic/gdx/math/Vector3;Ljava/lang/String;Lsnoddasmannen/galimulator/GalColor;Lsnoddasmannen/galimulator/GalFX$FONT_TYPE;FLcom/badlogic/gdx/graphics/Camera;)F";
    private static final String GALFX_DRAW_TEXT_DESCRIPTOR_2 = "(FFFFLjava/lang/String;Lsnoddasmannen/galimulator/GalColor;Lsnoddasmannen/galimulator/GalFX$FONT_TYPE;Lcom/badlogic/gdx/graphics/Camera;I)V";
    private static final String GALFX_DRAW_TEXTURE_DESCRIPTOR = "(Lcom/badlogic/gdx/graphics/g2d/TextureRegion;DDDDDLsnoddasmannen/galimulator/GalColor;ZLcom/badlogic/gdx/graphics/Camera;)V";
    private static final String GDX_CAMERA_CLASS = "com/badlogic/gdx/graphics/Camera";
    private static final String GDX_COLOR_CLASS = "com/badlogic/gdx/graphics/Color";
    private static final String GDX_GESTURE_LISTENER_CLASS = "com/badlogic/gdx/input/GestureDetector$GestureListener";
    private static final String GDX_INPUT_CLASS = "com/badlogic/gdx/Input";
    private static final String GDX_INPUT_PROCESSOR_CLASS = "com/badlogic/gdx/InputProcessor";
    private static final String GDX_POLYGON_SPRITE = "com/badlogic/gdx/graphics/g2d/PolygonSprite";
    private static final String ITEM_CLASS = BASE_PACKAGE + "Item";
    private static final String JOB_CLASS = BASE_PACKAGE + "Job";
    private static final String LOCATION_SELECTED_EFFECT_CLASS = BASE_PACKAGE + "effects/LocationSelectedEffect";
    private static final String MAIN_ENTRYPOINT_CLASS = "com/example/Main"; // I really want to know the story behind this name
    private static final String MAP_MODE_CLASS = BASE_PACKAGE + "MapMode";
    private static final String MAP_MODE_ENUM_CLASS = MAP_MODE_CLASS + "$MapModes";
    private static final String MAPDATA_CLASS = "snoddasmannen/galimulator/MapData";
    private static final String MATH_UTILS_CLASS = "com/badlogic/gdx/math/MathUtils";
    private static final String NINEPATCH_CLASS = "com/badlogic/gdx/graphics/g2d/NinePatch";
    private static final String PERSON_CLASS = BASE_PACKAGE + "Person";
    private static final String PLAYER_CLASS = "snoddasmannen/galimulator/Player";
    private static final String PROCEDURAL_STAR_GENERATOR_CLASS = BASE_PACKAGE + "ProceduralStarGenerator";
    private static final String RELIGION_CLASS = "snoddasmannen/galimulator/Religion";
    private static final String RENDER_ITEM_CLASS = RENDERSYSTEM_PACKAGE + "RenderItem";
    private static final String SETTINGS_TYPE_CLASS = "snoddasmannen/galimulator/UserSettings$SettingType";
    private static final String SPACE_CLASS = "snoddasmannen/galimulator/Space";
    private static final String SPACE_OPEN_INPUT_DIALOG_DESCRIPTOR = "(Lcom/badlogic/gdx/Input$TextInputListener;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
    private static final String SPRAWL_CLASS = "snoddasmannen/galimulator/Sprawl";
    private static final String STAR_CLASS = "snoddasmannen/galimulator/Star";
    private static final String STAR_NATIVES_CLASS = "snoddasmannen/galimulator/Native";
    private static final String STATE_ACTOR_CLASS = "snoddasmannen/galimulator/actors/StateActor";
    private static final String VANITY_HOLDER_CLASS = BASE_PACKAGE + "VanityHolder";
    private static final String WAR_CLASS = BASE_PACKAGE + "War";
    private static final String WIDGET_CLASS = "snoddasmannen/galimulator/ui/Widget";
    private static final String WIDGET_MESSAGE_CLASS = WIDGET_CLASS + "$WIDGET_MESSAGE";
    private static final String WIDGET_POSITIONING_CLASS = WIDGET_CLASS + "$WIDGET_POSITIONING";
    private static final String WORDLIST_CLASS = BASE_PACKAGE + "WordList";

    private static final AbstractInsnNode[] BITMAP_STAR_GENERATOR_GET_RESOURCES_LIST_METHOD_CONTENTS = new AbstractInsnNode[] {
            new FieldInsnNode(Opcodes.GETSTATIC, "com/badlogic/gdx/Gdx", "files", "Lcom/badlogic/gdx/Files;"),
            new VarInsnNode(Opcodes.ALOAD, 0),
            new FieldInsnNode(Opcodes.GETFIELD, BITMAP_STAR_GENERATOR_CLASS, "bitmapFile", "Ljava/lang/String;"),
            new MethodInsnNode(Opcodes.INVOKEINTERFACE, "com/badlogic/gdx/Files", "internal", "(Ljava/lang/String;)Lcom/badlogic/gdx/files/FileHandle;"),
            new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/badlogic/gdx/files/FileHandle", "file", "()Ljava/io/File;"),
            new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/File", "getAbsolutePath", "()Ljava/lang/String;"),
            new MethodInsnNode(Opcodes.INVOKESTATIC, "java/util/Collections", "singletonList", "(Ljava/lang/Object;)Ljava/util/List;"),
            new InsnNode(Opcodes.ARETURN)
    };

    @NotNull
    private final Map<String, String> enumSettingsMemberNames = new HashMap<>();
    @NotNull
    private final Map<String, ClassNode> name2Node = new HashMap<>();
    private final List<ClassNode> nodes;
    private final Remapper remapper;
    @NotNull
    private final Map<String, String> settingsTypeMemberNames = new HashMap<>();

    @Nullable
    private String textInputDialogWidgetClass = null;
    @Nullable
    private String spaceLogicalTickMethodName = null;

    public Autodeobf502(List<ClassNode> nodes, Remapper remapper) {
        this.nodes = nodes;
        this.remapper = remapper;
        for (ClassNode node : nodes) {
            name2Node.put(node.name, node);
        }
    }

    @Override
    @NotNull
    public String getVersion() {
        return "5.0.2";
    }

    private void assignAsAnonymousClass(ClassNode outerClass, ClassNode innerClass, String outerMethod, String outerMethodDesc) {
        InnerClassNode icn = new InnerClassNode(innerClass.name, outerClass.name, null, 0);
        outerClass.innerClasses.removeIf(innerClassNode -> innerClassNode.name.equals(innerClass.name));
        outerClass.innerClasses.add(icn);

        innerClass.innerClasses.removeIf(innerClassNode -> innerClassNode.name.equals(innerClass.name));
        innerClass.innerClasses.add(icn);
        innerClass.outerClass = outerClass.name;
        innerClass.outerMethod = outerMethod;
        innerClass.outerMethodDesc = outerMethodDesc;
    }

    private void assignAsAnonymousClass(ClassNode outerClass, MethodNode outerMethod, String innerClass) {
        InnerClassNode icn = new InnerClassNode(innerClass, outerClass.name, null, 0);
        outerClass.innerClasses.removeIf(innerClassNode -> innerClassNode.name.equals(innerClass));
        outerClass.innerClasses.add(icn);

        ClassNode innerNode = name2Node.get(innerClass);
        if (innerNode == null) {
            throw new OutdatedDeobfuscatorException("Unknown", innerClass, "*", "Unresolved node");
        }

        innerNode.innerClasses.removeIf(innerClassNode -> innerClassNode.name.equals(innerClass));
        innerNode.innerClasses.add(icn);
        innerNode.outerClass = outerClass.name;
        innerNode.outerMethod = outerMethod.name;
        innerNode.outerMethodDesc = outerMethod.desc;
    }

    private void assignAsInnerClass(ClassNode outerNode, ClassNode innerNode, String name) {
        InnerClassNode icn = new InnerClassNode(innerNode.name, outerNode.name, name, innerNode.access & ~Opcodes.ACC_SUPER);

        outerNode.innerClasses.removeIf(innerClassNode -> innerClassNode.name.equals(innerNode.name));
        outerNode.innerClasses.add(icn);
        innerNode.innerClasses.removeIf(innerClassNode -> innerClassNode.name.equals(innerNode.name));
        innerNode.innerClasses.add(icn);

        outerNode.outerClass = null;
        outerNode.outerMethod = null;
        outerNode.outerMethodDesc = null;
    }

    public boolean contentsEqual(MethodNode method, AbstractInsnNode... instructions) {
        AbstractInsnNode actualInsn = method.instructions.getFirst();
        for (AbstractInsnNode expectedInstruction : instructions) {
            if (expectedInstruction.getOpcode() == -1) {
                continue;
            }
            while (actualInsn != null && actualInsn.getOpcode() == -1) {
                actualInsn = actualInsn.getNext();
            }
            if (actualInsn == null) {
                return false;
            }
            if (actualInsn.getOpcode() != expectedInstruction.getOpcode()) {
                return false;
            }

            if (expectedInstruction instanceof MethodInsnNode) {
                MethodInsnNode expected = (MethodInsnNode) expectedInstruction;
                MethodInsnNode actual = (MethodInsnNode) actualInsn;
                if (!expected.owner.equals(actual.owner) || !expected.desc.equals(actual.desc) || (!expected.name.equals("*") && !expected.name.equals(actual.name))) {
                    return false;
                }
            } else if (expectedInstruction instanceof InsnNode) {
                // Nothing to compare - opcode was the most important thing of the instruction
            } else if (expectedInstruction instanceof FieldInsnNode) {
                FieldInsnNode expected = (FieldInsnNode) expectedInstruction;
                FieldInsnNode actual = (FieldInsnNode) actualInsn;
                if (!expected.owner.equals(actual.owner) || !expected.desc.equals(actual.desc) || (!expected.name.equals("*") && !expected.name.equals(actual.name))) {
                    return false;
                }
            } else if (expectedInstruction instanceof VarInsnNode) {
                if (((VarInsnNode) expectedInstruction).var != ((VarInsnNode) actualInsn).var) {
                    return false;
                }
            } else {
                throw new AssertionError("Cannot compare instances of class " + expectedInstruction.getClass().getName());
            }
            actualInsn = actualInsn.getNext();
        }
        return true;
    }

    private AbstractInsnNode getNext(AbstractInsnNode insn) {
        insn = insn.getNext();
        while (insn.getOpcode() == -1) {
            insn = insn.getNext();
        }
        return insn;
    }

    private AbstractInsnNode getNextOrNull(AbstractInsnNode insn) {
        insn = insn.getNext();
        while (insn != null && insn.getOpcode() == -1) {
            insn = insn.getNext();
        }
        return insn;
    }

    @SuppressWarnings("unchecked")
    private <T extends AbstractInsnNode> T getNext(AbstractInsnNode insn, int matchOpcode) {
        insn = insn.getNext();
        while (insn.getOpcode() != matchOpcode) {
            insn = insn.getNext();
        }
        return (T) insn;
    }

    @SuppressWarnings("all")
    private <T extends AbstractInsnNode> T getNextOrNull(AbstractInsnNode insn, int matchOpcode) {
        if (insn == null) {
            return (T) null;
        }
        insn = insn.getNext();
        while (insn != null && insn.getOpcode() != matchOpcode) {
            insn = insn.getNext();
        }
        return (T) insn;
    }

    @SuppressWarnings("all")
    private <T extends AbstractInsnNode> T getPreviousOrNull(@Nullable AbstractInsnNode insn, int matchOpcode) {
        if (insn == null) {
            return (T) null;
        }
        insn = insn.getPrevious();
        while (insn != null && insn.getOpcode() != matchOpcode) {
            insn = insn.getPrevious();
        }
        return (T) insn;
    }

    private boolean isGetter(MethodNode method, String fieldOwner, String fieldName, String fieldType, boolean staticField) {
        AbstractInsnNode insn = method.instructions.getFirst();
        while (insn.getOpcode() == -1) {
            insn = insn.getNext();
        }
        if (!staticField) {
            if (insn.getOpcode() != Opcodes.ALOAD) {
                return false;
            }
            VarInsnNode aloadThis = (VarInsnNode) insn;
            insn = insn.getNext();
            while (insn.getOpcode() == -1) {
                insn = insn.getNext();
            }
            if (aloadThis.var != 0 || insn.getOpcode() != Opcodes.GETFIELD) {
                return false;
            }
        } else if (insn.getOpcode() != Opcodes.GETSTATIC) {
            return false;
        }
        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
        insn = insn.getNext();
        while (insn.getOpcode() == -1) {
            insn = insn.getNext();
        }
        if (!isReturn(insn.getOpcode())) {
            return false;
        }
        if (fieldInsn.owner.equals(fieldOwner) && fieldInsn.name.equals(fieldName) && fieldInsn.desc.equals(fieldType)) {
            return true;
        }
        return false;
    }

    private boolean isInstanceofClass(ClassNode node, String type) {
        if (node == null) {
            return false;
        }
        if (node.name.equals(type)) {
            return true;
        }
        return isInstanceofClass(name2Node.get(node.superName), type);
    }

    private boolean isInstanceofInterface(ClassNode node, String type) {
        if (node == null) {
            return false;
        }
        if (node.name.equals(type)) {
            return true;
        }
        for (String interfaceName : node.interfaces) {
            if (isInstanceofClass(name2Node.get(interfaceName), type)) {
                return true;
            }
        }
        if (node.superName != null) {
            return isInstanceofInterface(name2Node.get(node.superName), type);
        }
        return false;
    }

    private boolean isInstanceofWidget(ClassNode node) {
        // Bit more performant than #isInstanceofClass, probably
        if (node == null) {
            return false;
        }
        if (node.name.equals(WIDGET_CLASS)) {
            return true;
        }
        return isInstanceofWidget(name2Node.get(node.superName));
    }

    private boolean isReturn(int opcode) {
        switch (opcode) {
        case Opcodes.RETURN:
        case Opcodes.ARETURN:
        case Opcodes.IRETURN:
        case Opcodes.DRETURN:
        case Opcodes.FRETURN:
        case Opcodes.LRETURN:
            return true;
        default:
            return false;
        }
    }

    private boolean isSetter(MethodNode method, String fieldOwner, String fieldName, String fieldType) {
        AbstractInsnNode insn = method.instructions.getFirst();
        while (insn.getOpcode() == -1) {
            insn = insn.getNext();
        }
        if (insn.getOpcode() != Opcodes.ALOAD) {
            return false;
        }
        VarInsnNode aloadThis = (VarInsnNode) insn;
        insn = insn.getNext();
        while (insn.getOpcode() == -1) {
            insn = insn.getNext();
        }
        if (aloadThis.var != 0 || insn.getOpcode() != Opcodes.ALOAD) {
            return false;
        }
        VarInsnNode aloadOne = (VarInsnNode) insn;
        insn = insn.getNext();
        while (insn.getOpcode() == -1) {
            insn = insn.getNext();
        }
        if (aloadOne.var != 1 || insn.getOpcode() != Opcodes.PUTFIELD) {
            return false;
        }
        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
        insn = insn.getNext();
        while (insn.getOpcode() == -1) {
            insn = insn.getNext();
        }
        if (insn.getOpcode() != Opcodes.RETURN) {
            return false;
        }
        if (fieldInsn.owner.equals(fieldOwner) && fieldInsn.name.equals(fieldName) && fieldInsn.desc.equals(fieldType)) {
            return true;
        }
        return false;
    }

    public void remapActorClasses(Writer mappingsStream) throws IOException {
        ClassNode spaceNode = name2Node.get(SPACE_CLASS);
        if (spaceNode == null) {
            throw new IllegalStateException(SPACE_CLASS + " not present.");
        }

        String spawnActorMethod = null;
        String initializeActorSpawnPredicatesMethod = null;

        for (MethodNode method : spaceNode.methods) {
            if (method.desc.equals("(L" + STAR_CLASS + ";)L" + ACTOR_CLASS + ";")) {
                boolean isSpawnActorMethod = false;
                AbstractInsnNode extendedBuildActorCall = null;

                for (AbstractInsnNode insn : method.instructions) {
                    if (insn.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                        continue;
                    }
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;
                    if (!methodInsn.name.equals("extendedBuildActor")
                            || !methodInsn.desc.equals("(L" + STAR_CLASS + ";)L" + ACTOR_CLASS + ";")) {
                        continue;
                    }
                    if (spawnActorMethod != null) {
                        throw new OutdatedDeobfuscatorException("Actor", "Space", "spawnActor", "Collision");
                    }
                    isSpawnActorMethod = true;
                    spawnActorMethod = method.name;
                    extendedBuildActorCall = insn;
                    remapClass(mappingsStream, methodInsn.owner, EMPIRE_EXTENSION_CLASS);
                    remapMethod(mappingsStream, SPACE_CLASS, method.name, "spawnActor", "(L" + STAR_CLASS + ";)L" + ACTOR_CLASS + ";");
                    break;
                }
                if (isSpawnActorMethod) {
                    for (AbstractInsnNode insn : method.instructions) {
                        if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                            MethodInsnNode methodInsn = (MethodInsnNode) insn;
                            if (!methodInsn.owner.equals(STAR_CLASS) || !methodInsn.desc.equals("()L" + EMPIRE_CLASS + ";")) {
                                throw new OutdatedDeobfuscatorException("Actor", "Star", "getOwningEmpire", "Unexpected nature method call");
                            }
                            remapMethod(mappingsStream, STAR_CLASS, methodInsn.name, "getOwningEmpire", "()L" + EMPIRE_CLASS + ";");
                            break;
                        }
                    }
                    if (extendedBuildActorCall == null) {
                        throw new NullPointerException();
                    }
                    AbstractInsnNode nextInsn = extendedBuildActorCall.getNext();
                    while (nextInsn != null && nextInsn.getOpcode() != Opcodes.GETSTATIC) {
                        nextInsn = nextInsn.getNext();
                    }
                    if (nextInsn == null) {
                        throw new OutdatedDeobfuscatorException("Actor", "Space", "actorSpawnPredicates", "Instructions exhausted");
                    }
                    FieldInsnNode fieldInsn = (FieldInsnNode) nextInsn;
                    if (!fieldInsn.owner.equals(SPACE_CLASS)) {
                        throw new OutdatedDeobfuscatorException("Actor", "Space", "actorSpawnPredicates", "Unexpected owner");
                    }
                    if (!fieldInsn.desc.equals("Ljava/util/ArrayList;")) {
                        throw new OutdatedDeobfuscatorException("Actor", "Space", "actorSpawnPredicates", "Unexpected descriptor");
                    }
                    remapField(mappingsStream, SPACE_CLASS, fieldInsn.name, "actorSpawnPredicates", "Ljava/util/ArrayList;");
                    while (nextInsn != null && nextInsn.getOpcode() != Opcodes.INVOKESTATIC) {
                        nextInsn = nextInsn.getNext();
                    }
                    if (nextInsn == null) {
                        throw new OutdatedDeobfuscatorException("Actor", "Space", "initializeActorSpawnPredicates", "Instructions exhausted");
                    }
                    MethodInsnNode methodInsn = (MethodInsnNode) nextInsn;
                    if (!methodInsn.owner.equals(SPACE_CLASS) || !methodInsn.desc.equals("()V")) {
                        throw new OutdatedDeobfuscatorException("Actor", "Space", "initializeActorSpawnPredicates", "Method call does not have the expected owner or descriptor");
                    }
                    remapMethod(mappingsStream, SPACE_CLASS, methodInsn.name, "initializeActorSpawnPredicates", "()V");
                    initializeActorSpawnPredicatesMethod = methodInsn.name;
                }
            }
        }

        if (spawnActorMethod == null) {
            throw new OutdatedDeobfuscatorException("Actor", "Space", "spawnActor");
        }
        if (initializeActorSpawnPredicatesMethod == null) { // A bit impossible here, but let's ignore that
            throw new OutdatedDeobfuscatorException("Actor", "Space", "initializeActorSpawnPredicates");
        }

        String actorSpawnPredicateClass = null;

        for (MethodNode method : spaceNode.methods) {
            if (method.name.equals(initializeActorSpawnPredicatesMethod) && method.desc.equals("()V")) {
                for (AbstractInsnNode insn : method.instructions) {
                    if (insn.getOpcode() != Opcodes.GETSTATIC) {
                        continue;
                    }
                    insn = insn.getNext();
                    if (insn == null || insn.getOpcode() != Opcodes.NEW) {
                        throw new OutdatedDeobfuscatorException("Actor", ACTOR_SPAWNING_PREDICATE_CLASS, "*", "Unexpected opcode.");
                    }
                    actorSpawnPredicateClass = ((TypeInsnNode) insn).desc;
                    break;
                }
                break;
            }
        }

        if (actorSpawnPredicateClass == null) {
            throw new OutdatedDeobfuscatorException("Actor", ACTOR_SPAWNING_PREDICATE_CLASS, "*", "Unresolved");
        }

        remapClass(mappingsStream, actorSpawnPredicateClass, ACTOR_SPAWNING_PREDICATE_CLASS);
        ClassNode actorSpawnPredicateNode = name2Node.get(actorSpawnPredicateClass);
        if (actorSpawnPredicateNode == null) {
            throw new OutdatedDeobfuscatorException("Actor", ACTOR_SPAWNING_PREDICATE_CLASS, "*", "Node unresolved");
        }

        // Line number nodes point to ActorSpawningPredicate being a local class of the Space class
        // This explains the access modifiers of the class so we are going to replicate this relationship
        {
            InnerClassNode icn = new InnerClassNode(actorSpawnPredicateClass, SPACE_CLASS, "ActorSpawningPredicate", Opcodes.ACC_STATIC);
            String actorSpawningPredicateClassFinal = actorSpawnPredicateClass; // Lambdas are nice!
            spaceNode.innerClasses.removeIf(inner -> inner.name.equals(actorSpawningPredicateClassFinal));
            spaceNode.innerClasses.add(icn);
            actorSpawnPredicateNode.innerClasses.removeIf(inner -> inner.name.equals(actorSpawningPredicateClassFinal));
            actorSpawnPredicateNode.innerClasses.add(icn);
            actorSpawnPredicateNode.outerClass = SPACE_CLASS;

            boolean religionRequirement = false;
            boolean actorFactory = false;
            boolean spawningChance = false;
            boolean specialRequirements = false;

            for (FieldNode field : actorSpawnPredicateNode.fields) {
                // We are going to cheat and just base our guess on the descriptor of the field.
                // In case this does not work due to some future change the deobfuscator internals are going to crash anyways
                if (field.desc.equals("F")) {
                    remapField(mappingsStream, actorSpawnPredicateClass, field.name, "spawningChance", "F");
                    if (spawningChance) {
                        throw new OutdatedDeobfuscatorException("Actor", ACTOR_SPAWNING_PREDICATE_CLASS, "spawningChance", "Collision");
                    }
                    spawningChance = true;
                } else if (field.desc.equals("L" + RELIGION_CLASS + ";")) {
                    remapField(mappingsStream, actorSpawnPredicateClass, field.name, "religionRequirement", "L" + RELIGION_CLASS + ";");
                    if (religionRequirement) {
                        throw new OutdatedDeobfuscatorException("Actor", ACTOR_SPAWNING_PREDICATE_CLASS, "religionRequirement", "Collision");
                    }
                    religionRequirement = true;
                } else if (field.desc.equals("L" + ACTOR_CREATOR_CLASS + ";"))  {
                    remapField(mappingsStream, actorSpawnPredicateClass, field.name, "actorFactory", "L" + ACTOR_CREATOR_CLASS + ";");
                    if (actorFactory) {
                        throw new OutdatedDeobfuscatorException("Actor", ACTOR_SPAWNING_PREDICATE_CLASS, "actorFactory", "Collision");
                    }
                    actorFactory = true;
                } else if (field.desc.equals("Ljava/util/List;")) {
                    remapField(mappingsStream, actorSpawnPredicateClass, field.name, "specialRequirements", "Ljava/util/List;");
                    if (specialRequirements) {
                        throw new OutdatedDeobfuscatorException("Actor", ACTOR_SPAWNING_PREDICATE_CLASS, "specialRequirements", "Collision");
                    }
                    specialRequirements = true;
                    field.signature = "Ljava/util/List<L" + EMPIRE_SPECIAL_CLASS + ";>;";
                } else {
                    throw new OutdatedDeobfuscatorException("Actor", ACTOR_SPAWNING_PREDICATE_CLASS, field.name, "Unmatched field descriptor: " + field.desc);
                }
            }

            if (!religionRequirement || !actorFactory || !spawningChance || !specialRequirements) {
                throw new OutdatedDeobfuscatorException("Actor", ACTOR_SPAWNING_PREDICATE_CLASS, "?", "At least one field is missing");
            }

            boolean testMethodFound = false;

            for (MethodNode method : actorSpawnPredicateNode.methods) {
                if (method.desc.equals("(L" + STAR_CLASS + ";)Z")) {
                    if (testMethodFound) {
                        throw new OutdatedDeobfuscatorException("Actor", ACTOR_SPAWNING_PREDICATE_CLASS, "test", "Collision");
                    }
                    remapMethod(mappingsStream, actorSpawnPredicateClass, method.name, "test", "(L" + STAR_CLASS + ";)Z");
                    testMethodFound = true;
                } else if (method.name.equals("<init>")
                        && method.desc.equals("(L" + ACTOR_CREATOR_CLASS + ";FLjava/util/List;L" + RELIGION_CLASS + ";)V")) {
                    method.signature = "(L" + ACTOR_CREATOR_CLASS + ";FLjava/util/List<L" + EMPIRE_SPECIAL_CLASS + ";>;L" + RELIGION_CLASS + ";)V";
                }
            }

            if (!testMethodFound) {
                throw new OutdatedDeobfuscatorException("Actor", ACTOR_SPAWNING_PREDICATE_CLASS, "test", "Method not found");
            }
        }

        String landmarkManagerClass = null;
        String regenerateLandmarksMethod = null;

        nodeLoop:
        for (ClassNode node : this.nodes) {
            if (node.name.startsWith(GUIDES_PACKAGE)) {
                for (MethodNode method : node.methods) {
                    for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                        if (insn.getOpcode() == Opcodes.LDC && ((LdcInsnNode) insn).cst.equals("Picking landmarks")) {
                            landmarkManagerClass = node.name;
                            regenerateLandmarksMethod = method.name;
                            if (!method.desc.equals("()V")) {
                                throw new OutdatedDeobfuscatorException("Guides", LANDMARK_MANAGER_CLASS, "regenerateLandmarks", "Unexpected descriptor");
                            }
                            continue nodeLoop;
                        }
                    }
                }
            }
        }

        if  (landmarkManagerClass == null) {
            throw new OutdatedDeobfuscatorException("Guides", LANDMARK_MANAGER_CLASS, "*", "Unresolved");
        }
        remapClass(mappingsStream, landmarkManagerClass, LANDMARK_MANAGER_CLASS);
        remapMethod(mappingsStream, landmarkManagerClass, regenerateLandmarksMethod, "regenerateLandmarks", "()V");
    }

    private void remapClass(Writer mappingsOut, @NotNull String oldName, @NotNull String newName) throws IOException {
        remapper.remapClassName(oldName, newName);
        mappingsOut.write("CLASS ");
        mappingsOut.write(oldName);
        mappingsOut.write(' ');
        mappingsOut.write(newName);
        mappingsOut.write('\n');
    }

    protected void remapDialogClasses(Writer mappingsStream, final @NotNull String settingsDialogClass) throws IOException {
        if (settingsTypeMemberNames.isEmpty()) {
            resolveEnumMemberNames(SETTINGS_TYPE_CLASS, settingsTypeMemberNames);
        }

        remapClass(mappingsStream, settingsDialogClass, SETTINGS_DIALOG_CLASS);
        ClassNode settingsDialog = name2Node.get(settingsDialogClass);
        if (settingsDialog == null) {
            throw new OutdatedDeobfuscatorException("Dialog", SETTINGS_DIALOG_CLASS, "*");
        }
        if (settingsDialog.interfaces.size() != 1) {
            throw new OutdatedDeobfuscatorException("Dialog", SETTINGS_DIALOG_CLASS, "*", "unexpected amount of interfaces");
        }
        remapClass(mappingsStream, settingsDialog.interfaces.get(0), DIALOG_INTERFACE);

        String blacklistButtonClass = null;
        String dialogButtonClass = null;
        String dialogButtonOnTouchMethod = null;
        String dialogComponentInterface = null;
        String dialogPackage = null;
        String labeledCheckboxComponent = null;
        String labeledStringChooserComponent = null;

        for (MethodNode method : settingsDialog.methods) {
            if (method.name.equals("getItems") && method.desc.equals("()Ljava/util/ArrayList;")) {
                for (AbstractInsnNode insn : method.instructions) {
                    if (insn.getOpcode() == Opcodes.LDC) {
                        LdcInsnNode ldcInsn = (LdcInsnNode) insn;
                        if (ldcInsn.cst.equals("Simulation")) {
                            AbstractInsnNode nextInsn = getNext(ldcInsn);
                            if (nextInsn.getOpcode() != Opcodes.INVOKESPECIAL) {
                                throw new OutdatedDeobfuscatorException("Dialog", SETTINGS_DIALOG_BLACKLIST_BUTTON_CLASS, "*", "Unexpected opcode");
                            }
                            MethodInsnNode methodInsn = (MethodInsnNode) nextInsn;
                            if (!methodInsn.name.equals("<init>")) {
                                throw new OutdatedDeobfuscatorException("Dialog", SETTINGS_DIALOG_BLACKLIST_BUTTON_CLASS, "*", "Logic error");
                            }
                            blacklistButtonClass = methodInsn.owner;

                            remapClass(mappingsStream, blacklistButtonClass, SETTINGS_DIALOG_BLACKLIST_BUTTON_CLASS);
                            ClassNode blacklistButton = name2Node.get(blacklistButtonClass);
                            if (blacklistButton == null) {
                                throw new OutdatedDeobfuscatorException("Dialog", SETTINGS_DIALOG_BLACKLIST_BUTTON_CLASS, "*", "Unresolved node");
                            }
                            assignAsAnonymousClass(settingsDialog, method, blacklistButtonClass);

                            dialogButtonClass = blacklistButton.superName;
                            remapClass(mappingsStream, dialogButtonClass, DIALOG_BUTTON_CLASS);

                            ClassNode dialogButton = name2Node.get(dialogButtonClass);
                            if (dialogButton == null) {
                                throw new OutdatedDeobfuscatorException("Dialog", DIALOG_BUTTON_CLASS, "*", "Unresolved node");
                            }
                            if (dialogButton.interfaces.size() != 1) {
                                throw new OutdatedDeobfuscatorException("Dialog", DIALOG_BUTTON_CLASS, "*", "Unexpected amount of interfaces");
                            }
                            dialogComponentInterface = dialogButton.interfaces.get(0);
                            remapClass(mappingsStream, dialogComponentInterface, DIALOG_COMPONENT_INTERFACE);

                            int packageNameLength = dialogButtonClass.lastIndexOf('/') + 1;
                            dialogPackage = dialogButtonClass.substring(0, packageNameLength);

                            String itemsSignature = "Ljava/util/ArrayList<L" + dialogComponentInterface + ";>;";
                            String getItemsSignature = "()" + itemsSignature;

                            method.signature = getItemsSignature;

                            ClassNode dialogNode = name2Node.get(settingsDialog.interfaces.get(0));
                            boolean foundDialogGetItemsMethod = false;
                            for (MethodNode method2 : dialogNode.methods) {
                                if (method2.name.equals("getItems") && method2.desc.equals("()Ljava/util/ArrayList;")) {
                                    method2.signature = getItemsSignature;
                                    foundDialogGetItemsMethod = true;
                                    break;
                                }
                            }
                            if (!foundDialogGetItemsMethod) {
                                throw new OutdatedDeobfuscatorException("Dialog", DIALOG_INTERFACE, "getItems", "methods exhausted");
                            }

                            AbstractInsnNode lastInsn = method.instructions.getLast();
                            while (lastInsn.getOpcode() == -1 || lastInsn.getOpcode() == Opcodes.ARETURN) {
                                lastInsn = lastInsn.getPrevious();
                            }
                            if (lastInsn.getOpcode() != Opcodes.ALOAD) {
                                throw new OutdatedDeobfuscatorException("Dialog", "Unepexcted opcode");
                            }
                            VarInsnNode loadItemsInsn = (VarInsnNode) lastInsn;

                            if (method.localVariables.isEmpty()) {
                                // CFR and Procyon are a bit strange and guess the signature of the items variable completely wrong
                                // That is why we are going to aid them there
                                // Problem is that CFR will now have serious issues with void declarations, but CFR is pretty bad
                                // with dealing with out remapping shenanigans anyways, so not a big issue.
                                // Link to related bug: https://github.com/leibnitz27/cfr/issues/150
                                String lvnDesc = "Ljava/java/ArrayList;";
                                AbstractInsnNode startLabelInsn = method.instructions.getFirst();
                                while (!(startLabelInsn instanceof LabelNode)) {
                                    startLabelInsn = startLabelInsn.getNext();
                                }
                                AbstractInsnNode endLabelInsn = method.instructions.getLast();
                                while (!(endLabelInsn instanceof LabelNode)) {
                                    endLabelInsn = endLabelInsn.getPrevious();
                                }
                                LabelNode startLabel = (LabelNode) startLabelInsn;
                                LabelNode endLabel = (LabelNode) endLabelInsn;
                                LocalVariableNode lvn = new LocalVariableNode("items", lvnDesc, itemsSignature, startLabel, endLabel, loadItemsInsn.var);
                                method.localVariables.add(lvn);
                            }

                            for (MethodNode method2 : dialogButton.methods) {
                                if (method2.desc.equals("()V") && (method2.access & Opcodes.ACC_ABSTRACT) != 0) {
                                    if (dialogButtonOnTouchMethod != null) {
                                        throw new OutdatedDeobfuscatorException("Dialog", DIALOG_BUTTON_CLASS, "onTouch", "Collision");
                                    }
                                    dialogButtonOnTouchMethod = method2.name;
                                }
                            }
                        }
                    } else if (insn.getOpcode() == Opcodes.GETSTATIC) {
                        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                        if (fieldInsn.owner.equals(SETTINGS_TYPE_CLASS)
                                && settingsTypeMemberNames.getOrDefault(fieldInsn.name, "").equals("BOOLEAN")) {
                            if (labeledCheckboxComponent != null) {
                                throw new OutdatedDeobfuscatorException("Dialog", SETTINGS_DIALOG_CHECKBOX_CLASS, "*", "Collision");
                            }
                            AbstractInsnNode previousInsn = fieldInsn.getPrevious();
                            if (previousInsn.getOpcode() != Opcodes.GETFIELD) {
                                throw new OutdatedDeobfuscatorException("Dialog", USER_SETTING_ENTRY_CLASS, "*", "Unexpected opcode");
                            }
                            FieldInsnNode getTypeInsn = (FieldInsnNode) previousInsn;
                            if (!getTypeInsn.desc.equals("L" + SETTINGS_TYPE_CLASS + ";")) {
                                throw new OutdatedDeobfuscatorException("Dialog", USER_SETTING_ENTRY_CLASS, "type", "Unexpected descriptor");
                            }
                            remapField(mappingsStream, getTypeInsn.owner, getTypeInsn.name, "type", "L" + SETTINGS_TYPE_CLASS + ";");
                            remapClass(mappingsStream, getTypeInsn.owner, USER_SETTING_ENTRY_CLASS);

                            AbstractInsnNode nextInsn = fieldInsn.getNext();
                            while (nextInsn != null && nextInsn.getOpcode() != Opcodes.NEW) {
                                nextInsn = nextInsn.getNext();
                            }

                            if (nextInsn == null) {
                                throw new OutdatedDeobfuscatorException("Dialog", SETTINGS_DIALOG_CHECKBOX_CLASS, "*", "Instructions exhausted");
                            }
                            TypeInsnNode newInsn = (TypeInsnNode) nextInsn;
                            String settingsCheckbox = newInsn.desc;
                            remapClass(mappingsStream, settingsCheckbox, SETTINGS_DIALOG_CHECKBOX_CLASS);

                            ClassNode settingsCheckboxNode = name2Node.get(settingsCheckbox);
                            if (settingsCheckboxNode == null) {
                                throw new OutdatedDeobfuscatorException("Dialog", SETTINGS_DIALOG_CHECKBOX_CLASS, "*", "Unresolved node");
                            }
                            labeledCheckboxComponent = Objects.requireNonNull(settingsCheckboxNode.superName, settingsCheckboxNode.name + " without superclass");
                            remapClass(mappingsStream, labeledCheckboxComponent, LABELED_CHECKBOX_COMPONENT);

                            assignAsAnonymousClass(settingsDialog, method, settingsCheckbox);
                        } else if (fieldInsn.owner.equals(SETTINGS_TYPE_CLASS)
                                && settingsTypeMemberNames.getOrDefault(fieldInsn.name, "").equals("STRING")) {

                            AbstractInsnNode nextInsn = fieldInsn.getNext();
                            while (nextInsn != null && nextInsn.getOpcode() != Opcodes.NEW) {
                                nextInsn = nextInsn.getNext();
                            }

                            if (nextInsn == null) {
                                throw new OutdatedDeobfuscatorException("Dialog", SETTINGS_DIALOG_STRING_CHOOSER_CLASS, "*", "Instructions exhausted");
                            }
                            TypeInsnNode newInsn = (TypeInsnNode) nextInsn;

                            ClassNode stringChooserSubclassNode = name2Node.get(newInsn.desc);
                            if (stringChooserSubclassNode == null) {
                                throw new OutdatedDeobfuscatorException("Dialog", SETTINGS_DIALOG_STRING_CHOOSER_CLASS, "*", "Unresolved node");
                            }
                            labeledStringChooserComponent = stringChooserSubclassNode.superName;
                            if (labeledStringChooserComponent == null) {
                                throw new IllegalStateException(stringChooserSubclassNode.name + " without superclass");
                            }
                            remapClass(mappingsStream, labeledStringChooserComponent, LABELED_STRING_CHOOSER_COMPONENT);
                            remapClass(mappingsStream, newInsn.desc, SETTINGS_DIALOG_STRING_CHOOSER_CLASS);
                            assignAsAnonymousClass(settingsDialog, method, newInsn.desc);
                        }
                    }
                }
                break;
            }
        }

        if (blacklistButtonClass == null) {
            throw new OutdatedDeobfuscatorException("Dialog", SETTINGS_DIALOG_BLACKLIST_BUTTON_CLASS, "*", "Unresolved");
        }
        if (dialogButtonOnTouchMethod == null) {
            throw new OutdatedDeobfuscatorException("Dialog", DIALOG_BUTTON_CLASS, "onTouch", "Unresolved");
        }
        if (dialogPackage == null) {
            throw new OutdatedDeobfuscatorException("Dialog", "Package \"" + DIALOG_PACKAGE + "\" not resolved.");
        }
        if (labeledCheckboxComponent == null) {
            throw new OutdatedDeobfuscatorException("Dialog", LABELED_CHECKBOX_COMPONENT, "*", "Unresolved");
        }
        if (labeledStringChooserComponent == null) {
            throw new OutdatedDeobfuscatorException("Dialog", LABELED_STRING_CHOOSER_COMPONENT, "*", "Unresolved");
        }

        Set<String> forbiddenClasses = new HashSet<>();
        forbiddenClasses.add(dialogButtonClass);
        forbiddenClasses.add(dialogComponentInterface);
        forbiddenClasses.add(labeledCheckboxComponent);
        forbiddenClasses.add(labeledStringChooserComponent);
        mappingsStream.write("# Begin dialog package relocation\n");
        int packageNameLength = dialogPackage.length();
        for (ClassNode node : nodes) {
            if (node.name.startsWith(dialogPackage) && !forbiddenClasses.contains(node.name)) {
                remapClass(mappingsStream, node.name, DIALOG_PACKAGE + node.name.substring(packageNameLength));
            }
        }
        mappingsStream.write("# End dialog package relocation\n");

        for (ClassNode node : nodes) {
            if (isInstanceofClass(node, dialogButtonClass)) {
                remapMethod(mappingsStream, node.name, dialogButtonOnTouchMethod, "onTouch", "()V");
            }
        }
    }

    /**
     * Remaps the methods and fields in the Empire class.
     * It does not need to be run after slIntermediary.
     *
     * @param mappingsStream The stream to write the mapping to
     * @throws IOException If some generic IO Exception occurred (most likely because it failed writing to the mappings stream)
     */
    public void remapEmpireClass(Writer mappingsStream) throws IOException {
        if (enumSettingsMemberNames.isEmpty()) {
            resolveEnumMemberNames(ENUM_SETTINGS_CLASS, enumSettingsMemberNames);
        }

        String shipCapacityModifierEnum = enumSettingsMemberNames.get("SHIP_NUMBER_MOD");
        if (shipCapacityModifierEnum == null) {
            throw new OutdatedDeobfuscatorException("Empire", "EnumSettings", "SHIP_NUMBER_MOD");
        }

        ClassNode empireNode = name2Node.get(EMPIRE_CLASS);
        if (empireNode == null) {
            throw new OutdatedDeobfuscatorException("Empire", "Empire", "*");
        }

        String getFlagItemsMethod = null;
        String getShipCapacityMethod = null;
        String tickEmpireMethod = null;
        String recentlyLostStarsField = null;
        String beaconsField = null;
        String allianceField = null;
        String getAllianceMethod = null;
        String setAllianceMethod = null;
        String getRelationsMethod = null;
        String vassalizeMethod = null;

        for (MethodNode method : empireNode.methods) {
            if (method.desc.equals("()Ljava/util/Vector;")) {
                AbstractInsnNode insn = getNext(method.instructions.getFirst());
                if (insn.getOpcode() != Opcodes.ALOAD) {
                    continue;
                }
                VarInsnNode aloadThis = (VarInsnNode) insn;
                insn = getNext(insn);
                if (insn.getOpcode() != Opcodes.GETFIELD || aloadThis.var != 0) {
                    continue;
                }
                FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                insn = getNext(insn);
                if (insn.getOpcode() != Opcodes.IFNONNULL || !fieldInsn.owner.equals(EMPIRE_CLASS)
                        || !fieldInsn.desc.equals("Ljava/util/Vector;") || !fieldInsn.name.equals("flagItems")) {
                    continue;
                }
                if (getFlagItemsMethod != null) {
                    throw new OutdatedDeobfuscatorException("Empire", "Empire", "getFlagItems", "Multiple candidates");
                }
                getFlagItemsMethod = method.name;
            } else if (method.desc.equals("()D")) {
                for (AbstractInsnNode insn : method.instructions) {
                    if (insn.getOpcode() == Opcodes.GETSTATIC) {
                        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                        if (fieldInsn.name.equals(shipCapacityModifierEnum)
                                && fieldInsn.owner.equals(ENUM_SETTINGS_CLASS)
                                && fieldInsn.desc.equals("L" + ENUM_SETTINGS_CLASS + ";")) {
                            if (getShipCapacityMethod != null && !getShipCapacityMethod.equals(method.name)) {
                                throw new OutdatedDeobfuscatorException("Empire", "Empire", "getCurrentShipCapacity", "Multiple candidates");
                            }
                            getShipCapacityMethod = method.name;
                        }
                    }
                }
            } else if (method.desc.equals("()V")) {
                for (AbstractInsnNode insn : method.instructions) {
                    if (insn.getOpcode() == Opcodes.LDC) {
                        LdcInsnNode ldcInsn = (LdcInsnNode) insn;
                        if (ldcInsn.cst.equals("Have now exterminated the heathens!")) {
                            if (tickEmpireMethod != null) {
                                throw new OutdatedDeobfuscatorException("Empire", "Empire", "tickEmpire", "Collision");
                            }
                            tickEmpireMethod = method.name;
                            break;
                        }
                    }
                }
            } else if (method.desc.equals("(Ljava/io/ObjectInputStream;)V")) {
                if (method.name.equals("readObject")) {
                    AbstractInsnNode insn = method.instructions.getFirst();
                    while (insn != null) {
                        if (insn.getOpcode() == Opcodes.INVOKESPECIAL) {
                            MethodInsnNode methodInsn = (MethodInsnNode) insn;
                            if (!methodInsn.name.equals("<init>")) {
                                insn = insn.getNext();
                                continue;
                            }
                            if (methodInsn.owner.equals("java/util/ArrayDeque")) {
                                if (recentlyLostStarsField != null) {
                                    throw new OutdatedDeobfuscatorException("Empire", "Empire", "recentlyLostStars", "Collision");
                                }
                                FieldInsnNode fieldInsn = (FieldInsnNode) insn.getNext();
                                if (!fieldInsn.desc.equals("Ljava/util/Deque;")) {
                                    throw new OutdatedDeobfuscatorException("Empire", "Empire", "recentlyLostStars", "Invalid descriptor");
                                }
                                if (!fieldInsn.owner.equals(EMPIRE_CLASS)) {
                                    throw new OutdatedDeobfuscatorException("Empire", "Empire", "recentlyLostStars", "Invalid owner class");
                                }
                                recentlyLostStarsField = fieldInsn.name;
                            } else if (methodInsn.owner.equals("java/util/ArrayList")) {
                                if (beaconsField != null) {
                                    throw new OutdatedDeobfuscatorException("Empire", "Empire", "beacons", "Collision");
                                }
                                FieldInsnNode fieldInsn = (FieldInsnNode) insn.getNext();
                                if (!fieldInsn.desc.equals("Ljava/util/ArrayList;")) {
                                    throw new OutdatedDeobfuscatorException("Empire", "Empire", "beacons", "Invalid descriptor");
                                }
                                if (!fieldInsn.owner.equals(EMPIRE_CLASS)) {
                                    throw new OutdatedDeobfuscatorException("Empire", "Empire", "beacons", "Invalid owner class");
                                }
                                beaconsField = fieldInsn.name;
                            }
                        }
                        insn = insn.getNext();
                    }
                }
            } else if (method.desc.equals("()L" + ALLIANCE_CLASS + ";")) {
                AbstractInsnNode insn = method.instructions.getFirst();
                insn = getNext(insn);
                if (insn.getOpcode() != Opcodes.ALOAD || ((VarInsnNode) insn).var != 0) {
                    continue;
                }
                insn = getNext(insn);
                if (insn.getOpcode() != Opcodes.GETFIELD) {
                    continue;
                }
                FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                if (getNext(insn).getOpcode() != Opcodes.ARETURN) {
                    continue;
                }
                if (!fieldInsn.desc.equals("L" + ALLIANCE_CLASS + ";")) {
                    throw new OutdatedDeobfuscatorException("Empire", "Empire", "alliance", "Nonsensical descriptor");
                }
                if (!fieldInsn.owner.equals(EMPIRE_CLASS)) {
                    throw new OutdatedDeobfuscatorException("Empire", "Empire", "alliance", "Invalid owner");
                }
                if (allianceField == null) {
                    allianceField = fieldInsn.name;
                } else if (!allianceField.equals(fieldInsn.name)) {
                    throw new OutdatedDeobfuscatorException("Empire", "Empire", "alliance", "Clash");
                }
                if (getAllianceMethod != null) {
                    throw new OutdatedDeobfuscatorException("Empire", "Empire", "getAlliance", "Collision");
                }
                getAllianceMethod = method.name;
            } else if (method.desc.equals("(L" + ALLIANCE_CLASS + ";)V")) {
                AbstractInsnNode insn = method.instructions.getFirst();
                while (insn != null) {
                    if (insn.getOpcode() == Opcodes.PUTFIELD) {
                        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                        if (fieldInsn.desc.equals("L" + ALLIANCE_CLASS + ";") && fieldInsn.owner.equals(EMPIRE_CLASS)) {
                            if (allianceField == null) {
                                allianceField = fieldInsn.name;
                            } else if (!allianceField.equals(fieldInsn.name)) {
                                throw new OutdatedDeobfuscatorException("Empire", "Empire", "alliance", "Clash");
                            }
                            if (setAllianceMethod != null) {
                                throw new OutdatedDeobfuscatorException("Empire", "Empire", "setAlliance", "Collision");
                            }
                            setAllianceMethod = method.name;
                            break;
                        }
                    }
                    insn = insn.getNext();
                }
            } else if (method.desc.equals("(L" + EMPIRE_CLASS + ";)Ljava/lang/String;")) {
                AbstractInsnNode insn = method.instructions.getFirst();
                LdcInsnNode ldcInsn = getNextOrNull(insn, Opcodes.LDC);
                if (ldcInsn != null && ldcInsn.cst.equals("NO CONTACT")) {
                    if (getRelationsMethod != null) {
                        throw new OutdatedDeobfuscatorException("Empire", "Empire", "getRelations", "Collision");
                    }
                    getRelationsMethod = method.name;
                    MethodInsnNode methodInsn = getPreviousOrNull(ldcInsn, Opcodes.INVOKEVIRTUAL);
                    if (!methodInsn.owner.equals(EMPIRE_CLASS) || !methodInsn.desc.equals("(L" + EMPIRE_CLASS + ";)Z")) {
                        throw new OutdatedDeobfuscatorException("Empire", EMPIRE_CLASS, "hasContact", "Invalid owner or descriptor");
                    }
                    remapMethod(mappingsStream, EMPIRE_CLASS, methodInsn.name, "hasContact", "(L" + EMPIRE_CLASS + ";)Z");
                    methodInsn = getNext(ldcInsn, Opcodes.INVOKEVIRTUAL);
                    if (!methodInsn.owner.equals(EMPIRE_CLASS) || !methodInsn.desc.equals("()L" + EMPIRE_CLASS + ";")) {
                        throw new OutdatedDeobfuscatorException("Empire", EMPIRE_CLASS, "getMaster", "Invalid owner or descriptor");
                    }
                    remapMethod(mappingsStream, EMPIRE_CLASS, methodInsn.name, "getMaster", "()L" + EMPIRE_CLASS + ";");
                    do {
                        ldcInsn = getNext(ldcInsn, Opcodes.LDC);
                    } while (!ldcInsn.cst.equals("ALLIED"));
                    methodInsn = getPreviousOrNull(ldcInsn, Opcodes.INVOKEVIRTUAL);
                    if (!methodInsn.owner.equals(EMPIRE_CLASS) || !methodInsn.desc.equals("(L" + EMPIRE_CLASS + ";)Z")) {
                        throw new OutdatedDeobfuscatorException("Empire", EMPIRE_CLASS, "isAllied", "Invalid owner or descriptor");
                    }
                    remapMethod(mappingsStream, EMPIRE_CLASS, methodInsn.name, "isAllied", "(L" + EMPIRE_CLASS + ";)Z");
                    methodInsn = getNext(ldcInsn, Opcodes.INVOKEVIRTUAL);
                    if (!methodInsn.owner.equals(EMPIRE_CLASS) || !methodInsn.desc.equals("(L" + EMPIRE_CLASS + ";)Z")) {
                        throw new OutdatedDeobfuscatorException("Empire", EMPIRE_CLASS, "isAtPeace", "Invalid owner or descriptor");
                    }
                    remapMethod(mappingsStream, EMPIRE_CLASS, methodInsn.name, "isAtPeace", "(L" + EMPIRE_CLASS + ";)Z");
                }
            } else if (method.desc.equals("(L" + EMPIRE_CLASS + ";)V")) {
                AbstractInsnNode insn = method.instructions.getFirst();
                LdcInsnNode ldcInsn = getNextOrNull(insn, Opcodes.LDC);
                if (ldcInsn != null && ldcInsn.cst.equals("Has been vassalized by ")) {
                    if (vassalizeMethod != null) {
                        throw new OutdatedDeobfuscatorException("Empire", EMPIRE_CLASS, "vassalize", "Collision");
                    }
                    vassalizeMethod = method.name;
                }
            }
        }

        if (getFlagItemsMethod == null) {
            throw new OutdatedDeobfuscatorException("Empire", "Empire", "getFlagItems");
        }
        if (getShipCapacityMethod == null) {
            throw new OutdatedDeobfuscatorException("Empire", "Empire", "getCurrentShipCapacity");
        }
        if (tickEmpireMethod == null) {
            throw new OutdatedDeobfuscatorException("Empire", "Empire", "tickEmpire", "Unresolved");
        }
        if (recentlyLostStarsField == null) {
            throw new OutdatedDeobfuscatorException("Empire", "Empire", "recentlyLostStars", "Unresolved");
        }
        if (beaconsField == null) {
            throw new OutdatedDeobfuscatorException("Empire", "Empire", "beacons", "Unresolved");
        }
        if (allianceField == null) {
            throw new OutdatedDeobfuscatorException("Empire", "Empire", "alliance", "Unresolved");
        }
        if (getAllianceMethod == null) {
            throw new OutdatedDeobfuscatorException("Empire", "Empire", "getAlliance", "Unresolved");
        }
        if (setAllianceMethod == null) {
            throw new OutdatedDeobfuscatorException("Empire", "Empire", "setAlliance", "Unresolved");
        }
        if (getRelationsMethod == null) {
            throw new OutdatedDeobfuscatorException("Empire", "Empire", "getRelations", "Unresolved");
        }
        if (vassalizeMethod == null) {
            throw new OutdatedDeobfuscatorException("Empire", "Empire", "vassalize", "Unresolved");
        }

        remapField(mappingsStream, EMPIRE_CLASS, recentlyLostStarsField, "recentlyLostStars", "Ljava/util/Deque;");
        remapField(mappingsStream, EMPIRE_CLASS, beaconsField, "beacons", "Ljava/util/ArrayList;");
        remapField(mappingsStream, EMPIRE_CLASS, allianceField, "alliance", "L" + ALLIANCE_CLASS + ";");
        remapMethod(mappingsStream, EMPIRE_CLASS, getAllianceMethod, "getAlliance", "()L" + ALLIANCE_CLASS + ";");
        remapMethod(mappingsStream, EMPIRE_CLASS, setAllianceMethod, "setAlliance", "(L" + ALLIANCE_CLASS + ";)V");
        remapMethod(mappingsStream, EMPIRE_CLASS, getRelationsMethod, "getRelations", "(L" + EMPIRE_CLASS + ";)Ljava/lang/String;");
        remapMethod(mappingsStream, EMPIRE_CLASS, vassalizeMethod, "vassalize", "(L" + EMPIRE_CLASS + ";)V");

        String flagOwnerInterface = null;
        for (String itf : empireNode.interfaces) {
            ClassNode node = name2Node.get(itf);
            if (node == null) {
                continue;
            }
            for (MethodNode method : node.methods) {
                if (method.name.equals(getFlagItemsMethod) && method.desc.equals("()Ljava/util/Vector;")) {
                    if (flagOwnerInterface != null) {
                        throw new OutdatedDeobfuscatorException(FLAG_OWNER_INTERFACE, "Collision");
                    }
                    flagOwnerInterface = node.name;
                    break;
                }
            }
        }

        if (flagOwnerInterface == null) {
            throw new OutdatedDeobfuscatorException(FLAG_OWNER_INTERFACE, "Unresolved");
        }

        remapClass(mappingsStream, flagOwnerInterface, FLAG_OWNER_INTERFACE);
        for (ClassNode node : nodes) {
            if (node.interfaces.contains(flagOwnerInterface)) {
                remapMethod(mappingsStream, node.name, getFlagItemsMethod, "getFlagItems", "()Ljava/util/Vector;");
            }
        }
        remapMethod(mappingsStream, flagOwnerInterface, getFlagItemsMethod, "getFlagItems", "()Ljava/util/Vector;");

        remapMethod(mappingsStream, EMPIRE_CLASS, getShipCapacityMethod, "getCurrentShipCapacity", "()D");
        remapMethod(mappingsStream, EMPIRE_CLASS, tickEmpireMethod, "tickEmpire", "()V");

        String internalSessionRandomField = null;

        for (FieldNode field : empireNode.fields) {
            if ((field.access & Opcodes.ACC_TRANSIENT) != 0 && field.desc.equals("Ljava/util/Random;")) {
                if (internalSessionRandomField != null) {
                    throw new OutdatedDeobfuscatorException("Empire", "Empire", "internalSessionRandom", "Collision");
                }
                internalSessionRandomField = field.name;
            }
        }

        if (internalSessionRandomField == null) {
            throw new OutdatedDeobfuscatorException("Empire", "Empire", "internalSessionRandom", "Unresolved");
        }
        remapField(mappingsStream, EMPIRE_CLASS, internalSessionRandomField, "internalSessionRandom", "Ljava/util/Random;");

        String starTickMethod = null;
        String canBeVassalizedByMethod = null;
        String canVassalizeMethod = null;

        ClassNode starNode = name2Node.get(STAR_CLASS);
        for (MethodNode method : starNode.methods) {
            if (method.desc.equals("()V")) {
                AbstractInsnNode insn = method.instructions.getFirst();
                LdcInsnNode ldcInsn = getNextOrNull(insn, Opcodes.LDC);
                while (ldcInsn != null && !ldcInsn.cst.equals("Diplomatic master stroke")) {
                    ldcInsn = getNextOrNull(ldcInsn, Opcodes.LDC);
                }
                if (ldcInsn != null) {
                    if (starTickMethod != null) {
                        throw new OutdatedDeobfuscatorException("Empire", "Star", "tick", "Collision");
                    }
                    starTickMethod = method.name;
                    MethodInsnNode methodInsn = getNext(ldcInsn, Opcodes.INVOKESPECIAL);
                    if (!methodInsn.owner.equals("snoddasmannen/galimulator/relationships/LinearDropRelMod")) {
                        throw new OutdatedDeobfuscatorException("Empire", "DiploymacyActor", "*", "Unexpected owner");
                    }
                    remapClass(mappingsStream, methodInsn.desc.substring(2, methodInsn.desc.indexOf(';')), BASE_PACKAGE + "relationships/DiploymacyActor");
                    methodInsn = getNext(methodInsn, Opcodes.INVOKEVIRTUAL);
                    if (!methodInsn.owner.equals("snoddasmannen/galimulator/relationships/RelManager")) {
                        throw new OutdatedDeobfuscatorException("Empire", "DiploymacyActor", "addModifier", "Unexpected owner");
                    }
                    remapMethod(mappingsStream, "snoddasmannen/galimulator/relationships/RelManager", methodInsn.name, "addModifier", methodInsn.desc);
                    methodInsn = getNext(getNext(methodInsn, Opcodes.INVOKEVIRTUAL), Opcodes.INVOKEVIRTUAL);
                    if (!methodInsn.owner.equals(EMPIRE_CLASS) || !methodInsn.desc.equals("(L" + EMPIRE_CLASS + ";)Z")) {
                        throw new OutdatedDeobfuscatorException("Empire", EMPIRE_CLASS, "canBeVassalizedBy", "Unexpected owner or descriptor");
                    }
                    canBeVassalizedByMethod = methodInsn.name;
                    remapMethod(mappingsStream, EMPIRE_CLASS, canBeVassalizedByMethod, "canBeVassalizedBy", "(L" + EMPIRE_CLASS + ";)Z");
                    methodInsn = getNext(getNext(methodInsn, Opcodes.INVOKEVIRTUAL), Opcodes.INVOKEVIRTUAL);
                    if (!methodInsn.owner.equals(EMPIRE_CLASS) || !methodInsn.desc.equals("(L" + EMPIRE_CLASS + ";)Z")) {
                        throw new OutdatedDeobfuscatorException("Empire", EMPIRE_CLASS, "canVassalize", "Unexpected owner or descriptor");
                    }
                    canVassalizeMethod = methodInsn.name;
                    remapMethod(mappingsStream, EMPIRE_CLASS, canVassalizeMethod, "canVassalize", "(L" + EMPIRE_CLASS + ";)Z");
                }
            }
        }

        if (starTickMethod == null) {
            throw new OutdatedDeobfuscatorException("Empire", "Star", "tick", "Unresolved");
        }

        for (MethodNode method : empireNode.methods) {
            if (method.desc.equals("(L" + EMPIRE_CLASS + ";)Z")) {
                if (method.name.equals(canVassalizeMethod)) {
                    MethodInsnNode methodInsn = getNext(method.instructions.getFirst(), Opcodes.INVOKEVIRTUAL);
                    if (!methodInsn.owner.equals(EMPIRE_CLASS) || !methodInsn.desc.equals("()Z")) {
                        throw new OutdatedDeobfuscatorException("Empire", EMPIRE_CLASS, "isNotDegenerating", "Unexpected owner or descriptor");
                    }
                    remapMethod(mappingsStream, EMPIRE_CLASS, methodInsn.name, "isNotDegenerating", "()Z");
                    methodInsn = getNext(methodInsn, Opcodes.INVOKEVIRTUAL);
                    if (!methodInsn.owner.equals(EMPIRE_CLASS) || !methodInsn.desc.equals("()Z")) {
                        throw new OutdatedDeobfuscatorException("Empire", EMPIRE_CLASS, "isAvoidingAlliances", "Unexpected owner or descriptor");
                    }
                    remapMethod(mappingsStream, EMPIRE_CLASS, methodInsn.name, "isAvoidingAlliances", "()Z");
                }
            }
        }

        String mottoGeneratorClass = null;

        nodeloop:
        for (ClassNode node : nodes) {
            for (MethodNode method : node.methods) {
                for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn.getOpcode() == Opcodes.LDC && ((LdcInsnNode) insn).cst.equals("data/mottopreps.txt")) {
                        if (mottoGeneratorClass != null) {
                            throw new OutdatedDeobfuscatorException("Empire", MOTTO_GENERATOR_CLASS, "*", "Collision");
                        }
                        mottoGeneratorClass = node.name;
                        remapClass(mappingsStream, node.name, MOTTO_GENERATOR_CLASS);
                        remapMethod(mappingsStream, node.name, method.name, "initialize", method.desc);
                        FieldInsnNode finsn = getNext(insn, Opcodes.PUTSTATIC);
                        if (!finsn.owner.equals(node.name) || !finsn.desc.equals("L" + WORDLIST_CLASS + ";")) {
                            throw new OutdatedDeobfuscatorException("Empire", MOTTO_GENERATOR_CLASS, "prepositions", "Unexpected owner or descriptor");
                        }
                        remapField(mappingsStream, node.name, finsn.name, "prepositions", "L" + WORDLIST_CLASS + ";");
                        finsn = getNext(finsn, Opcodes.PUTSTATIC);
                        if (!finsn.owner.equals(node.name) || !finsn.desc.equals("L" + WORDLIST_CLASS + ";")) {
                            throw new OutdatedDeobfuscatorException("Empire", MOTTO_GENERATOR_CLASS, "nouns", "Unexpected owner or descriptor");
                        }
                        remapField(mappingsStream, node.name, finsn.name, "nouns", "L" + WORDLIST_CLASS + ";");
                        if (getNextOrNull(finsn, Opcodes.PUTSTATIC) != null) {
                            throw new OutdatedDeobfuscatorException("Empire", MOTTO_GENERATOR_CLASS, "nouns", "Unexpected trailing PUTSTATIC call");
                        }
                        MethodNode candidate = null;
                        for (MethodNode method2 : node.methods) {
                            if (method2.desc.equals("()Ljava/lang/String;")) {
                                if (candidate != null) {
                                    throw new OutdatedDeobfuscatorException("Empire", MOTTO_GENERATOR_CLASS, "generateMotto", "Collision");
                                }
                                candidate = method2;
                            }
                        }
                        if (candidate == null) {
                            throw new OutdatedDeobfuscatorException("Empire", MOTTO_GENERATOR_CLASS, "generateMotto", "Not found");
                        }
                        remapMethod(mappingsStream, node.name, candidate.name, "generateMotto", "()Ljava/lang/String;");
                        insn = candidate.instructions.getFirst();
                        MethodInsnNode minsn = getNext(insn, Opcodes.INVOKEVIRTUAL);
                        if (!minsn.owner.equals(VANITY_HOLDER_CLASS) || !minsn.desc.equals("()Z")) {
                            throw new OutdatedDeobfuscatorException("Empire", VANITY_HOLDER_CLASS, "hasMotto", "Unexpected owner or descriptor");
                        }
                        remapMethod(mappingsStream, VANITY_HOLDER_CLASS, minsn.name, "hasMotto", "()Z");
                        minsn = getNext(minsn, Opcodes.INVOKEVIRTUAL);
                        if (!minsn.owner.equals(VANITY_HOLDER_CLASS) || !minsn.desc.equals("()Ljava/lang/String;")) {
                            throw new OutdatedDeobfuscatorException("Empire", VANITY_HOLDER_CLASS, "getMotto", "Unexpected owner or descriptor");
                        }
                        remapMethod(mappingsStream, VANITY_HOLDER_CLASS, minsn.name, "getMotto", "()Ljava/lang/String;");
                        minsn = getNext(minsn, Opcodes.INVOKEVIRTUAL);
                        if (!minsn.owner.equals(WORDLIST_CLASS) || !minsn.desc.equals("()Ljava/lang/String;")) {
                            throw new OutdatedDeobfuscatorException("Empire", WORDLIST_CLASS, "getRandomWord", "Unexpected owner or descriptor");
                        }
                        remapMethod(mappingsStream, WORDLIST_CLASS, minsn.name, "getRandomWord", "()Ljava/lang/String;");
                        continue nodeloop;
                    }
                }
            }
        }

        if (mottoGeneratorClass == null) {
            throw new OutdatedDeobfuscatorException("Empire", MOTTO_GENERATOR_CLASS, "*", "Not found");
        }
    }

    public void remapEmploymentAgency(Writer mappingsStream) throws IOException {
        ClassNode employmentAgency = name2Node.get(EMPLOYMENT_AGENCY_CLASS);
        ClassNode jobNode = name2Node.get(JOB_CLASS);
        if (employmentAgency == null) {
            throw new OutdatedDeobfuscatorException("EmploymentAgency", EMPLOYMENT_AGENCY_CLASS, "*", "Node not found");
        }
        if (jobNode == null) {
            throw new OutdatedDeobfuscatorException("EmploymentAgency", JOB_CLASS, "*", "Node not found");
        }

        String employmentInstance = null;
        for (FieldNode field : employmentAgency.fields) {
            if (field.desc.equals("L" + EMPLOYMENT_AGENCY_CLASS + ";") && (field.access & Opcodes.ACC_STATIC) != 0) {
                if (employmentInstance != null) {
                    throw new OutdatedDeobfuscatorException("EmploymentAgency", EMPLOYMENT_AGENCY_CLASS, "instance", "Collision");
                }
                employmentInstance = field.name;
            }
        }
        if (employmentInstance == null) {
            throw new OutdatedDeobfuscatorException("EmploymentAgency", EMPLOYMENT_AGENCY_CLASS, "instance", "Not found");
        }
        remapField(mappingsStream, EMPLOYMENT_AGENCY_CLASS, employmentInstance, "instance", "L" + EMPLOYMENT_AGENCY_CLASS + ";");

        String setInstanceMethod = null;
        String getInstanceMethod = null;

        for (MethodNode method : employmentAgency.methods) {
            if ((method.access & Opcodes.ACC_STATIC) != 0) {
                if (method.desc.equals("(L" + EMPLOYMENT_AGENCY_CLASS + ";)V")) {
                    if (setInstanceMethod != null) {
                        throw new OutdatedDeobfuscatorException("EmploymentAgency", EMPLOYMENT_AGENCY_CLASS, "setInstance", "Collision");
                    }
                    setInstanceMethod = method.name;
                } else if (method.desc.equals("()L" + EMPLOYMENT_AGENCY_CLASS + ";")) {
                    if (getInstanceMethod != null) {
                        throw new OutdatedDeobfuscatorException("EmploymentAgency", EMPLOYMENT_AGENCY_CLASS, "getInstance", "Collision");
                    }
                    getInstanceMethod = method.name;
                }
            }
        }

        if (setInstanceMethod == null) {
            throw new OutdatedDeobfuscatorException("EmploymentAgency", EMPLOYMENT_AGENCY_CLASS, "setInstance", "Not found");
        }
        if (getInstanceMethod == null) {
            throw new OutdatedDeobfuscatorException("EmploymentAgency", EMPLOYMENT_AGENCY_CLASS, "getInstance", "Not found");
        }

        remapMethod(mappingsStream, EMPLOYMENT_AGENCY_CLASS, setInstanceMethod, "setInstance", "(L" + EMPLOYMENT_AGENCY_CLASS + ";)V");
        remapMethod(mappingsStream, EMPLOYMENT_AGENCY_CLASS, getInstanceMethod, "getInstance", "()L" + EMPLOYMENT_AGENCY_CLASS + ";");

        String tickJobMethod = null;
        String vacateJobMethod = null;
        String isVacatedMethod = null;

        for (MethodNode method : jobNode.methods) {
            if (!method.desc.equals("()V") || (method.access & Opcodes.ACC_PUBLIC) == 0) {
                continue;
            }
            AbstractInsnNode insn = method.instructions.getLast();
            while (insn != null) {
                if (insn.getOpcode() == Opcodes.LDC) {
                    LdcInsnNode ldcInsn = (LdcInsnNode) insn;
                    if (ldcInsn.cst.equals("Job hazard")) {
                        break;
                    }
                }
                insn = insn.getPrevious();
            }
            if (insn == null) {
                continue;
            }
            if (tickJobMethod != null) {
                throw new OutdatedDeobfuscatorException("EmploymentAgency", JOB_CLASS, "tick", "Collision");
            }
            tickJobMethod = method.name;

            insn = getNext(insn);
            if (insn.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                throw new OutdatedDeobfuscatorException("EmploymentAgency", JOB_CLASS, "vacate", "Unexpected opcode");
            }
            MethodInsnNode vacateInsn = (MethodInsnNode) insn;
            if (!vacateInsn.desc.equals("(Ljava/lang/String;)V") || !vacateInsn.owner.equals(JOB_CLASS)) {
                throw new OutdatedDeobfuscatorException("EmploymentAgency", JOB_CLASS, "vacate", "Unexpected call specifics");
            }
            vacateJobMethod = vacateInsn.name;

            insn = method.instructions.getFirst();
            while (insn != null) {
                if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;
                    if (methodInsn.desc.equals("()Z") && methodInsn.owner.equals(JOB_CLASS)) {
                        isVacatedMethod = methodInsn.name;
                        break;
                    }
                }
                insn = insn.getNext();
            }
        }

        if (tickJobMethod == null) {
            throw new OutdatedDeobfuscatorException("EmploymentAgency", JOB_CLASS, "tick", "Not found");
        }

        if (vacateJobMethod == null) {
            throw new OutdatedDeobfuscatorException("EmploymentAgency", JOB_CLASS, "vacate", "Not found");
        }

        if (isVacatedMethod == null) {
            throw new OutdatedDeobfuscatorException("EmploymentAgency", JOB_CLASS, "isVacated", "Not found");
        }

        remapMethod(mappingsStream, JOB_CLASS, tickJobMethod, "tick", "()V");
        remapMethod(mappingsStream, JOB_CLASS, vacateJobMethod, "vacate", "(Ljava/lang/String;)V");
        remapMethod(mappingsStream, JOB_CLASS, isVacatedMethod, "isVacated", "()Z");

        String getCurrentHolder = null;

        for (MethodNode method : jobNode.methods) {
            if (method.name.equals(isVacatedMethod) && method.desc.equals("()Z")) {
                AbstractInsnNode insn = method.instructions.getFirst();
                while (insn != null && insn.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                    insn = insn.getNext();
                }
                if (insn == null) {
                    throw new OutdatedDeobfuscatorException("EmploymentAgency", JOB_CLASS, "getCurrentHolder", "Instructions exhausted");
                }
                MethodInsnNode methodInsn = (MethodInsnNode) insn;
                if (!methodInsn.owner.equals(JOB_CLASS) || !methodInsn.desc.equals("()L" + PERSON_CLASS + ";")) {
                    throw new OutdatedDeobfuscatorException("EmploymentAgency", JOB_CLASS, "getCurrentHolder", "Unexpected call specifics");
                }
                getCurrentHolder = methodInsn.name;
                break;
            }
        }

        if (getCurrentHolder == null) {
            throw new OutdatedDeobfuscatorException("EmploymentAgency", JOB_CLASS, "getCurrentHolder", "Not found");
        }

        remapMethod(mappingsStream, JOB_CLASS, getCurrentHolder, "getCurrentHolder", "()L" + PERSON_CLASS + ";");

        String currentHolder = null;
        String previousHolder = null;

        exteriorLoop:
        for (MethodNode method : jobNode.methods) {
            if (method.name.equals(getCurrentHolder) && method.desc.equals("()L" + PERSON_CLASS + ";")) {
                AbstractInsnNode insn = method.instructions.getFirst();
                while (insn != null && insn.getOpcode() != Opcodes.GETFIELD) {
                    insn = insn.getNext();
                }
                if (insn == null) {
                    throw new OutdatedDeobfuscatorException("EmploymentAgency", JOB_CLASS, "currentHolder", "Instructions exhausted");
                }
                final FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                if (!fieldInsn.desc.equals("L" + PERSON_CLASS + ";") || !fieldInsn.owner.equals(JOB_CLASS)) {
                    throw new OutdatedDeobfuscatorException("EmploymentAgency", JOB_CLASS, "currentHolder", "Unexpected GETFIELD instruction");
                }
                currentHolder = fieldInsn.name;
                while (insn != null) {
                    if (insn.getOpcode() == Opcodes.PUTFIELD) {
                        FieldInsnNode var10001 = (FieldInsnNode) insn;
                        if (var10001.desc.equals("L" + PERSON_CLASS + ";") && !var10001.name.equals(fieldInsn.name)) {
                            previousHolder = var10001.name;
                            break exteriorLoop;
                        }
                    }
                    insn = insn.getNext();
                }

                throw new OutdatedDeobfuscatorException("EmploymentAgency", JOB_CLASS, "previousHolder", "Instructions exhausted");
            }
        }

        if (currentHolder == null) {
            throw new OutdatedDeobfuscatorException("EmploymentAgency", JOB_CLASS, "currentHolder", "Not found");
        }
        if (previousHolder == null) {
            throw new OutdatedDeobfuscatorException("EmploymentAgency", JOB_CLASS, "previousHolder", "Not found");
        }

        remapField(mappingsStream, JOB_CLASS, currentHolder, "currentHolder", "L" + PERSON_CLASS + ";");
        remapField(mappingsStream, JOB_CLASS, previousHolder, "previousHolder", "L" + PERSON_CLASS + ";");

        String getPreviousHolder = null;

        for (MethodNode method : jobNode.methods) {
            if (!method.desc.equals("()L" + PERSON_CLASS + ";") || (method.access & Opcodes.ACC_PUBLIC) == 0) {
                continue;
            }
            if (isGetter(method, JOB_CLASS, previousHolder, "L" + PERSON_CLASS + ";", false)) {
                getPreviousHolder = method.name;
                break;
            }
        }

        if (getPreviousHolder == null) {
            throw new OutdatedDeobfuscatorException("EmploymentAgency", JOB_CLASS, "getPreviousHolder", "Not found");
        }

        remapMethod(mappingsStream, JOB_CLASS, getPreviousHolder, "getPreviousHolder", "()L" + PERSON_CLASS + ";");

        String tickAgencyMethod = null;

        exteriorLoop2:
        for (MethodNode method : employmentAgency.methods) {
            if (method.desc.equals("()V")) {
                AbstractInsnNode insn = method.instructions.getFirst();
                while (insn != null) {
                    if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        MethodInsnNode methodInsn = (MethodInsnNode) insn;
                        if (methodInsn.owner.equals(JOB_CLASS) && methodInsn.desc.equals("()V") && methodInsn.name.equals(tickJobMethod)) {
                            tickAgencyMethod = method.name;
                            break exteriorLoop2;
                        }
                    }
                    insn = insn.getNext();
                }
            }
        }

        if (tickAgencyMethod == null) {
            throw new OutdatedDeobfuscatorException("EmploymentAgency", EMPLOYMENT_AGENCY_CLASS, "tick", "Instructions exhausted");
        }

        remapMethod(mappingsStream, EMPLOYMENT_AGENCY_CLASS, tickAgencyMethod, "tick", "()V");
    }

    private void remapField(Writer mappingsOut, String owner, String oldName, String newName, String desc) throws IOException {
        remapper.remapField(owner, desc, oldName, newName);
        // Format: FIELD owner descriptor originalName newName
        mappingsOut.write("FIELD " + owner + " " + desc + " " + oldName + " " +  newName + "\n");
    }

    /**
     * Remap field, methods and Classes related to galaxy generation.
     *
     * @param mappingsStream Where to save the mapped values to. The stream will be in the tiny format
     * @throws IOException Exception that is raised during writes to the stream
     */
    public void remapGalaxyGeneration(Writer mappingsStream) throws IOException {
        ClassNode space = name2Node.get(SPACE_CLASS);
        if (space == null) {
            throw new IllegalStateException("Class not present: " + SPACE_CLASS);
        }

        String generateGalaxyMethod = null;
        String setBackgroundTaskDescriptionMethod = null;

        for (MethodNode method : space.methods) {
            if (method.desc.equals("(IL" + MAPDATA_CLASS + ";)V")) {
                AbstractInsnNode insn = method.instructions.getFirst();
                while (insn != null && insn.getOpcode() != Opcodes.LDC) {
                    insn = insn.getNext();
                }
                LdcInsnNode ldcInsn = (LdcInsnNode) insn;
                if (ldcInsn == null || !ldcInsn.cst.equals("Generating galaxy")) {
                    continue;
                }

                if (generateGalaxyMethod != null) {
                    throw new OutdatedDeobfuscatorException("GalaxyGen", SPACE_CLASS, "generateGalaxy", "Collision");
                }
                generateGalaxyMethod = method.name;

                insn = ldcInsn.getNext();
                if (insn.getOpcode() != Opcodes.INVOKESTATIC) {
                    throw new OutdatedDeobfuscatorException("GalaxyGen", DEBUG_CLASS, "startDebuggingSection", "Unexpected opcode");
                }
                MethodInsnNode startDebuggingSectionInsn = (MethodInsnNode) insn;
                if (!startDebuggingSectionInsn.owner.equals(DEBUG_CLASS) || !startDebuggingSectionInsn.desc.equals("(Ljava/lang/String;)V")) {
                    throw new OutdatedDeobfuscatorException("GalaxyGen", DEBUG_CLASS, "startDebuggingSection", "Unexpected specifics about method call");
                }
                remapMethod(mappingsStream, DEBUG_CLASS, startDebuggingSectionInsn.name, "startDebuggingSection", "(Ljava/lang/String;)V");

                while (insn != null && insn.getOpcode() != Opcodes.LDC) {
                    insn = insn.getNext();
                }
                if (insn == null || (insn = insn.getNext()).getOpcode() != Opcodes.INVOKESTATIC) {
                    throw new OutdatedDeobfuscatorException("GalaxyGen", SPACE_CLASS, "setBackgroundTaskDescription", "Unexpected opcode");
                }
                MethodInsnNode setBackgroundTaskDescInsn = (MethodInsnNode) insn;

                if (!setBackgroundTaskDescInsn.desc.equals("(Ljava/lang/String;)V")) {
                    throw new OutdatedDeobfuscatorException("GalaxyGen", SPACE_CLASS, "setBackgroundTaskDescription", "Unexpected descriptor");
                }
                remapMethod(mappingsStream, SPACE_CLASS, setBackgroundTaskDescInsn.name, "setBackgroundTaskDescription", "(Ljava/lang/String;)V");
                setBackgroundTaskDescriptionMethod = setBackgroundTaskDescInsn.name;
            }
        }

        if (setBackgroundTaskDescriptionMethod == null) {
            throw new OutdatedDeobfuscatorException("GalaxyGen", SPACE_CLASS, "generateGalaxy", "Unresolved");
        }

        remapMethod(mappingsStream, SPACE_CLASS, generateGalaxyMethod, "generateGalaxy", "(IL" + MAPDATA_CLASS + ";)V");

        String backgroundTaskDescription = null;

        for (MethodNode method : space.methods) {
            if (method.desc.equals("(Ljava/lang/String;)V") && method.name.equals(setBackgroundTaskDescriptionMethod)) {
                AbstractInsnNode insn = method.instructions.getFirst();
                while (insn.getOpcode() != Opcodes.PUTSTATIC) {
                    insn = insn.getNext();
                }
                AbstractInsnNode prev = insn.getPrevious();
                if (prev.getOpcode() != Opcodes.ALOAD) {
                    throw new OutdatedDeobfuscatorException("GalaxyGen", SPACE_CLASS, "backgroundTaskDescription", "Unexpected opcode (prev)");
                }
                VarInsnNode varInsn = (VarInsnNode) prev;
                if (varInsn.var != 0) {
                    throw new OutdatedDeobfuscatorException("GalaxyGen", SPACE_CLASS, "backgroundTaskDescription", "Unexpected operand");
                }
                if (insn.getOpcode() != Opcodes.PUTSTATIC) {
                    throw new OutdatedDeobfuscatorException("GalaxyGen", SPACE_CLASS, "backgroundTaskDescription", "Unexpected opcode (insn)");
                }
                FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                if (!fieldInsn.owner.equals(SPACE_CLASS)) {
                    throw new OutdatedDeobfuscatorException("GalaxyGen", SPACE_CLASS, "backgroundTaskDescription", "Unexpected owner");
                }
                backgroundTaskDescription = fieldInsn.name;
            }
        }

        if (backgroundTaskDescription == null) {
            throw new OutdatedDeobfuscatorException("GalaxyGen", SPACE_CLASS, "backgroundTaskDescription", "Unresolved");
        }
        remapField(mappingsStream, SPACE_CLASS, backgroundTaskDescription, "backgroundTaskDescription", "Ljava/lang/String;");
    }

    public void remapGenerators(Writer mappingsStream) throws IOException {

        ClassNode bitmapGenClass = name2Node.get(BITMAP_STAR_GENERATOR_CLASS);
        if (bitmapGenClass == null) {
            throw new OutdatedDeobfuscatorException("Generator", BITMAP_STAR_GENERATOR_CLASS, "*", "Not found");
        }
        if (bitmapGenClass.interfaces.size() != 2) {
            throw new OutdatedDeobfuscatorException("Generator", STAR_GENERATOR_INTERFACE, "*", "Not found (1240)");
        }
        String starGeneratorClass = bitmapGenClass.interfaces.get(0);
        if (!starGeneratorClass.startsWith(BASE_PACKAGE)) {
            starGeneratorClass = bitmapGenClass.interfaces.get(1);
            if (!starGeneratorClass.startsWith(BASE_PACKAGE)) {
                throw new OutdatedDeobfuscatorException("Generator", STAR_GENERATOR_INTERFACE, "*", "Not found (1246)");
            }
        }

        String generateStarMethod = null;
        String getResourceListMethod = null;
        String getMaxYMethod = null;
        String setupSettingsMethod = null;

        for (MethodNode method : bitmapGenClass.methods) {
            if (method.desc.equals("()L" + STAR_CLASS + ";")) {
                for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn.getOpcode() == Opcodes.LDC) {
                        LdcInsnNode ldcInsn = (LdcInsnNode) insn;
                        if (ldcInsn.cst.equals("Invalid bits file for map: ")) {
                            if (generateStarMethod != null) {
                                throw new OutdatedDeobfuscatorException("Generator", BITMAP_STAR_GENERATOR_CLASS, "generateStar", "Collision");
                            }
                            generateStarMethod = method.name;
                        }
                    }
                }
            } else if (method.desc.equals("()Ljava/util/List;")) {
                if (contentsEqual(method, BITMAP_STAR_GENERATOR_GET_RESOURCES_LIST_METHOD_CONTENTS)) {
                    if (getResourceListMethod != null) {
                        throw new OutdatedDeobfuscatorException("Generator", BITMAP_STAR_GENERATOR_CLASS, "getResources", "Collision");
                    }
                    getResourceListMethod = method.name;
                }
            } else if (method.desc.equals("()F")) {
                AbstractInsnNode insn = method.instructions.getLast();
                while (insn.getOpcode() == -1) {
                    insn = insn.getPrevious();
                }
                insn = insn.getPrevious();
                while (insn.getOpcode() == -1) {
                    insn = insn.getPrevious();
                }
                if (insn.getOpcode() == Opcodes.GETFIELD) {
                    FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                    if (fieldInsn.owner.equals(BITMAP_STAR_GENERATOR_CLASS) && fieldInsn.desc.equals("F") && fieldInsn.name.equals("yMaxCache")) {
                        if (getMaxYMethod != null) {
                            throw new OutdatedDeobfuscatorException("Generator", BITMAP_STAR_GENERATOR_CLASS, "getMaxY", "Collision");
                        }
                        getMaxYMethod = method.name;
                        continue;
                    }
                }
            } else if (method.desc.equals("()V")) {
                AbstractInsnNode insn = method.instructions.getFirst();
                while (insn.getOpcode() == -1) {
                    insn = insn.getNext();
                }
                if (insn.getOpcode() == Opcodes.GETSTATIC) {
                    FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                    if (fieldInsn.owner.equals(ENUM_SETTINGS_CLASS)) {
                        if (setupSettingsMethod != null) {
                            throw new OutdatedDeobfuscatorException("Generator", STAR_GENERATOR_INTERFACE, "setupSettings", "Collision");
                        }
                        setupSettingsMethod = method.name;
                    }
                }
            }
        }

        if (generateStarMethod == null) {
            throw new OutdatedDeobfuscatorException("Generator", BITMAP_STAR_GENERATOR_CLASS, "generateStar", "Unresolved");
        }
        if (getResourceListMethod == null) {
            throw new OutdatedDeobfuscatorException("Generator", BITMAP_STAR_GENERATOR_CLASS, "getResources", "Not resolved");
        }
        if (getMaxYMethod == null) {
            throw new OutdatedDeobfuscatorException("Generator", BITMAP_STAR_GENERATOR_CLASS, "getMaxY", "Not resolved");
        }
        if (setupSettingsMethod == null) {
            throw new OutdatedDeobfuscatorException("Generator", STAR_GENERATOR_INTERFACE, "setupSettings", "Unresolved");
        }

        String getMaxXMethod = null;

        for (MethodNode method : bitmapGenClass.methods) {
            if (method.desc.equals("()F")) {
                if (contentsEqual(method, new VarInsnNode(Opcodes.ALOAD, 0),
                        new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BITMAP_STAR_GENERATOR_CLASS, getMaxYMethod, "()F"),
                        new FieldInsnNode(Opcodes.GETSTATIC, SPACE_CLASS, "*", "F"),
                        new InsnNode(Opcodes.FMUL),
                        new InsnNode(Opcodes.FRETURN))) {
                    if (getMaxXMethod != null) {
                        throw new OutdatedDeobfuscatorException("Generator", BITMAP_STAR_GENERATOR_CLASS, "getMaxX", "Collision");
                    }
                    getMaxXMethod = method.name;
                }
            }
        }

        if (getMaxXMethod == null) {
            throw new OutdatedDeobfuscatorException("Generator", BITMAP_STAR_GENERATOR_CLASS, "getMaxX", "Not resolved");
        }

        ClassNode spaceNode = name2Node.get(SPACE_CLASS);
        if (spaceNode == null) {
            throw new AssertionError();
        }

        String spaceGetMaxXMethod = null;
        String spaceGetMaxYMethod = null;

        for (MethodNode method : spaceNode.methods) {
            if (method.desc.equals("()F")) {
                AbstractInsnNode firstNode = method.instructions.getFirst();
                while (firstNode.getOpcode() == -1) {
                    firstNode = firstNode.getNext();
                }
                if (firstNode.getOpcode() != Opcodes.GETSTATIC) {
                    continue;
                }
                FieldInsnNode fieldInsn = (FieldInsnNode) firstNode;
                if (!fieldInsn.owner.equals(SPACE_CLASS) || !fieldInsn.desc.equals("L" + MAPDATA_CLASS + ";")) {
                    continue;
                }
                AbstractInsnNode insn = fieldInsn.getNext();
                if (insn.getOpcode() != Opcodes.IFNULL) {
                    continue;
                }
                insn = getNext(insn);
                if (insn.getOpcode() != Opcodes.GETSTATIC) {
                    continue;
                }
                fieldInsn = (FieldInsnNode) insn;
                if (!fieldInsn.owner.equals(SPACE_CLASS) || !fieldInsn.desc.equals("L" + MAPDATA_CLASS + ";")) {
                    continue;
                }
                insn = getNext(insn);
                if (insn.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                    continue;
                }
                MethodInsnNode getGenInsn = (MethodInsnNode) insn;
                if (!getGenInsn.owner.equals(MAPDATA_CLASS)) {
                    continue;
                }
                insn = getNext(insn);
                if (insn.getOpcode() != Opcodes.INVOKEINTERFACE) {
                    continue;
                }
                MethodInsnNode methodInsn = (MethodInsnNode) insn;
                if (!methodInsn.desc.equals("()F")) {
                    continue;
                }
                if (methodInsn.name.equals(getMaxXMethod)) {
                    if (spaceGetMaxXMethod != null) {
                        throw new OutdatedDeobfuscatorException("Generator", SPACE_CLASS, "getMaxX", "Collision");
                    }
                    spaceGetMaxXMethod = method.name;
                } else if (methodInsn.name.equals(getMaxYMethod)) {
                    if (spaceGetMaxYMethod != null) {
                        throw new OutdatedDeobfuscatorException("Generator", SPACE_CLASS, "getMaxY", "Collision");
                    }
                    spaceGetMaxYMethod = method.name;
                } else {
                    throw new OutdatedDeobfuscatorException("Generator", SPACE_CLASS, "getMaxX", "Logic error");
                }
            }
        }

        if (spaceGetMaxXMethod == null) {
            throw new OutdatedDeobfuscatorException("Generator", SPACE_CLASS, "getMaxX", "Unresolved");
        }
        if (spaceGetMaxYMethod == null) {
            throw new OutdatedDeobfuscatorException("Generator", SPACE_CLASS, "getMaxY", "Unresolved");
        }

        String spaceMaxXCacheField = null;
        String spaceMaxYCacheField = null;
        String prepareGeneratorMethod = null;

        for (MethodNode method : spaceNode.methods) {
            if (method.desc.equals("(IL" + MAPDATA_CLASS + ";)V")) {
                for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn.getOpcode() != Opcodes.INVOKESTATIC) {
                        continue;
                    }
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;
                    if (!methodInsn.owner.equals(SPACE_CLASS) || !methodInsn.desc.equals("()F")) {
                        continue;
                    }
                    if (insn.getNext().getOpcode() != Opcodes.PUTSTATIC) {
                        continue;
                    }
                    FieldInsnNode field = (FieldInsnNode) insn.getNext();
                    if (!field.owner.equals(SPACE_CLASS)) {
                        continue;
                    }

                    if (methodInsn.name.equals(spaceGetMaxXMethod)) {
                        if (spaceMaxXCacheField != null) {
                            throw new OutdatedDeobfuscatorException("Generator", SPACE_CLASS, "maxXCache", "Collision");
                        }
                        spaceMaxXCacheField = field.name;

                        AbstractInsnNode previous = methodInsn.getPrevious();
                        while (previous.getOpcode() == -1) {
                            previous = previous.getPrevious();
                        }

                        if (previous.getOpcode() != Opcodes.INVOKEINTERFACE) {
                            throw new OutdatedDeobfuscatorException("Generator", STAR_GENERATOR_INTERFACE, "prepareGenerator", "Unexpected opcode");
                        }

                        MethodInsnNode previousMethodInsn = (MethodInsnNode) previous;

                        if (!previousMethodInsn.desc.equals("()V")) {
                            throw new OutdatedDeobfuscatorException("Generator", STAR_GENERATOR_INTERFACE, "prepareGenerator", "Unexpected opcode");
                        }
                        if (prepareGeneratorMethod != null) {
                            throw new OutdatedDeobfuscatorException("Generator", STAR_GENERATOR_INTERFACE, "prepareGenerator", "Collision");
                        }
                        prepareGeneratorMethod = previousMethodInsn.name;
                    } else if (methodInsn.name.equals(spaceGetMaxYMethod)) {
                        if (spaceMaxYCacheField != null) {
                            throw new OutdatedDeobfuscatorException("Generator", SPACE_CLASS, "maxYCache", "Collision");
                        }
                        spaceMaxYCacheField = field.name;
                    }
                }
            }
        }

        if (spaceMaxXCacheField == null) {
            throw new OutdatedDeobfuscatorException("Generator", SPACE_CLASS, "maxXCache", "Unresolved");
        }
        if (spaceMaxYCacheField == null) {
            throw new OutdatedDeobfuscatorException("Generator", SPACE_CLASS, "maxYCache", "Unresolved");
        }
        if (prepareGeneratorMethod == null) {
            throw new OutdatedDeobfuscatorException("Generator", STAR_GENERATOR_INTERFACE, "prepareGenerator", "Unresolved");
        }

        remapMethod(mappingsStream, SPACE_CLASS, spaceGetMaxXMethod, "getMaxX", "()F");
        remapMethod(mappingsStream, SPACE_CLASS, spaceGetMaxYMethod, "getMaxY", "()F");
        remapField(mappingsStream, SPACE_CLASS, spaceMaxXCacheField, "maxXCache", "F");
        remapField(mappingsStream, SPACE_CLASS, spaceMaxYCacheField, "maxYCache", "F");

        ClassNode fractalStarGenerator = name2Node.get(FRACTAL_STAR_GENERATOR_CLASS);
        if (fractalStarGenerator == null) {
            throw new OutdatedDeobfuscatorException("Generator", FRACTAL_STAR_GENERATOR_CLASS, "*", "Missing");
        }

        String getEngravingTextMethod = null;
        String onLoadMethod = null;
        String getBackgroundTextureMethod = null;

        for (MethodNode method : fractalStarGenerator.methods) {
            if (method.desc.equals("()Ljava/lang/String;")) {
                for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn.getOpcode() == Opcodes.LDC) {
                        LdcInsnNode ldcInsn = (LdcInsnNode) insn;
                        if (ldcInsn.cst.equals("Seed: ")) {
                            if (getEngravingTextMethod != null) {
                                throw new OutdatedDeobfuscatorException("Generator", STAR_GENERATOR_INTERFACE, "getEngravingText", "Collision");
                            }
                            getEngravingTextMethod = method.name;
                            break;
                        }
                    }
                }
            } else if (method.desc.equals("()V")) {
                AbstractInsnNode insn = method.instructions.getFirst();
                while (insn.getOpcode() == -1) {
                    insn = insn.getNext();
                }
                insn = getNext(insn);
                if (insn.getOpcode() == Opcodes.NEW) {
                    TypeInsnNode typeInsn = (TypeInsnNode) insn;
                    if (typeInsn.desc.equals("java/util/BitSet")) {
                        if (onLoadMethod != null) {
                            throw new OutdatedDeobfuscatorException("Generator", STAR_GENERATOR_INTERFACE, "onLoad", "Collision (1520)");
                        }
                        for (MethodNode method2 : fractalStarGenerator.methods) {
                            if (!method2.desc.equals("()V")) {
                                continue;
                            }
                            if (contentsEqual(method2, new VarInsnNode(Opcodes.ALOAD, 0),
                                    new MethodInsnNode(Opcodes.INVOKEVIRTUAL, FRACTAL_STAR_GENERATOR_CLASS, method.name, "()V"),
                                    new InsnNode(Opcodes.RETURN))) {
                                if (onLoadMethod != null) {
                                    throw new OutdatedDeobfuscatorException("Generator", STAR_GENERATOR_INTERFACE, "onLoad", "Collision (1530)");
                                }
                                onLoadMethod = method2.name;
                            }
                        }
                    }
                }
            } else if (method.desc.equals("()Lcom/badlogic/gdx/graphics/Texture;")) {
                if ((method.access & Opcodes.ACC_PUBLIC) == 0) {
                    continue;
                }
                if (getBackgroundTextureMethod != null) {
                    throw new OutdatedDeobfuscatorException("Generator", STAR_GENERATOR_INTERFACE, "getBackgroundTexture", "Collision");
                }
                getBackgroundTextureMethod = method.name;
            }
        }

        if (getEngravingTextMethod == null) {
            throw new OutdatedDeobfuscatorException("Generator", STAR_GENERATOR_INTERFACE, "getEngravingText", "Not found");
        }
        if (onLoadMethod == null) {
            throw new OutdatedDeobfuscatorException("Generator", STAR_GENERATOR_INTERFACE, "onLoad", "Not found");
        }
        if (getBackgroundTextureMethod == null) {
            throw new OutdatedDeobfuscatorException("Generator", STAR_GENERATOR_INTERFACE, "getBackgroundTexture", "Not found");
        }

        String getSettingsDialogMethod = null;
        String getSettingsDialogDesc = null;
        String hasMovingStarsMethod = null;

        for (ClassNode node : nodes) {
            if (node.name.startsWith(PROCEDURAL_STAR_GENERATOR_CLASS + "$")) {
                boolean movingSpiral = false;
                for (FieldNode field : node.fields) {
                    if (field.desc.equals("F") && field.name.equals("undulation")) {
                        movingSpiral = true;
                        break;
                    }
                }
                for (MethodNode method : node.methods) {
                    if (method.desc.startsWith("()L")) {
                        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                            if (insn.getOpcode() == Opcodes.LDC) {
                                LdcInsnNode ldcInsn = (LdcInsnNode) insn;
                                if (ldcInsn.cst.equals("Planet count")) {
                                    if (getSettingsDialogMethod != null) {
                                        throw new OutdatedDeobfuscatorException("Generator", STAR_GENERATOR_INTERFACE, "getSettingsDialog", "Collision");
                                    }
                                    getSettingsDialogMethod = method.name;
                                    getSettingsDialogDesc = method.desc;
                                }
                            }
                        }
                    } else if (movingSpiral && method.desc.equals("()Z")) {
                        if (hasMovingStarsMethod != null) {
                            throw new OutdatedDeobfuscatorException("Generator", STAR_GENERATOR_INTERFACE, "hasMovingStars", "Collision");
                        }
                        hasMovingStarsMethod = method.name;
                    }
                }
            }
        }

        if (getSettingsDialogMethod == null) {
            throw new OutdatedDeobfuscatorException("Generator", STAR_GENERATOR_INTERFACE, "getSettingsDialog", "Not found");
        }
        if (hasMovingStarsMethod == null) {
            throw new OutdatedDeobfuscatorException("Generator", STAR_GENERATOR_INTERFACE, "hasMovingStars", "Not found");
        }

        for (ClassNode node : nodes) {
            if (isInstanceofInterface(node, starGeneratorClass)) {
                remapMethod(mappingsStream, node.name, generateStarMethod, "generateStar", "()L" + STAR_CLASS + ";");
                remapMethod(mappingsStream, node.name, getResourceListMethod, "getResources", "()Ljava/util/List;");
                remapMethod(mappingsStream, node.name, getMaxXMethod, "getMaxX", "()F");
                remapMethod(mappingsStream, node.name, getMaxYMethod, "getMaxY", "()F");
                remapMethod(mappingsStream, node.name, prepareGeneratorMethod, "prepareGenerator", "()V");
                remapMethod(mappingsStream, node.name, getEngravingTextMethod, "getEngravingText", "()Ljava/lang/String;");
                remapMethod(mappingsStream, node.name, getSettingsDialogMethod, "getSettingsDialog", getSettingsDialogDesc);
                remapMethod(mappingsStream, node.name, hasMovingStarsMethod, "hasMovingStars", "()Z");
                remapMethod(mappingsStream, node.name, setupSettingsMethod, "setupSettings", "()V");
                remapMethod(mappingsStream, node.name, onLoadMethod, "onLoad", "()V");
                remapMethod(mappingsStream, node.name, getBackgroundTextureMethod, "getBackgroundTexture", "()Lcom/badlogic/gdx/graphics/Texture;");
            }
        }
    }

    /*
     * Remaps Hotkey/Keybind-related classes.
     * This method does not need to be run after intermediary.
     */
    public void remapHotkeys(Writer mappingsStream) throws IOException {
        ClassNode mainClass = this.name2Node.get(MAIN_ENTRYPOINT_CLASS);
        if (mainClass == null) {
            throw new OutdatedDeobfuscatorException("Hotkey", "Cannot find " + MAIN_ENTRYPOINT_CLASS);
        }
        String aShortcutClass = null;
        scope10001:
        for (MethodNode method : mainClass.methods) {
            if (method.desc.equals("()V") && method.name.equals("setupShortcuts")) {
                for (AbstractInsnNode insn : method.instructions) {
                    if (insn.getOpcode() == Opcodes.NEW) {
                        TypeInsnNode newInsn = (TypeInsnNode) insn;
                        aShortcutClass = newInsn.desc;
                        break scope10001;
                    }
                }
                throw new OutdatedDeobfuscatorException("Hotkey", "Hotkey", "*");
            }
        }
        if (aShortcutClass == null) {
            throw new OutdatedDeobfuscatorException("Hotkey", MAIN_ENTRYPOINT_CLASS, "setupShortcuts");
        }
        ClassNode extendsShortcut = this.name2Node.get(aShortcutClass);
        if (extendsShortcut == null) {
            throw new OutdatedDeobfuscatorException("Hotkey", "Cannot find " + aShortcutClass);
        }
        remapClass(mappingsStream, extendsShortcut.superName, BASE_PACKAGE + "Shortcut");

        String aboutWidgetClass = null;
        String showShortcutListButtonClass = null;

        for (ClassNode node : this.nodes) {
            if (node.name.startsWith(UI_PACKAGE)) {
                for (MethodNode method : node.methods) {
                    if (!method.name.equals("<init>")) {
                        continue;
                    }
                    boolean flag = false;
                    for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                        if (insn.getOpcode() != Opcodes.LDC) {
                            continue;
                        }
                        LdcInsnNode ldcInsn = (LdcInsnNode) insn;
                        if (ldcInsn.cst.equals("about")) {
                            flag = true;
                        } else if (flag && ldcInsn.cst.equals("{$1}")) {
                            aboutWidgetClass = node.name;
                            while (insn != null) {
                                if (insn.getOpcode() == Opcodes.LDC && ((LdcInsnNode) insn).cst.equals("Keyboard shortcuts")) {
                                    break;
                                }
                                insn = insn.getNext();
                            }
                            TypeInsnNode typeInsn = getPreviousOrNull(insn, Opcodes.NEW);
                            if (typeInsn == null) {
                                throw new OutdatedDeobfuscatorException("Hotkey", SHOW_SHORTCUT_LIST_BUTTON_CLASS, "*", insn == null ? "Prerequisite missing" : "No such NEW insn");
                            }
                            showShortcutListButtonClass = typeInsn.desc;
                            break;
                        } else {
                            break;
                        }
                    }
                    
                }
            } else if (node.name.startsWith(MAIN_ENTRYPOINT_CLASS + "$")
                    && node.superName.equals(extendsShortcut.superName)) {
                
            }
        }

        if (aboutWidgetClass == null) {
            throw new OutdatedDeobfuscatorException("Hotkey", ABOUT_WIDGET_CLASS, "*", "Unresolved");
        }
        if (showShortcutListButtonClass == null) {
            throw new OutdatedDeobfuscatorException("Hotkey", SHOW_SHORTCUT_LIST_BUTTON_CLASS, "*", "Unresolved");
        }

        remapClass(mappingsStream, aboutWidgetClass, ABOUT_WIDGET_CLASS);
        remapClass(mappingsStream, showShortcutListButtonClass, SHOW_SHORTCUT_LIST_BUTTON_CLASS);

        ClassNode showShortcutListButtonNode = this.name2Node.get(showShortcutListButtonClass);
        this.assignAsInnerClass(this.name2Node.get(aboutWidgetClass), showShortcutListButtonNode, SHOW_SHORTCUT_LIST_BUTTON_CLASS.substring(ABOUT_WIDGET_CLASS.length() + 1));

        MethodNode showShortcutListMethod = null;
        for (MethodNode method : showShortcutListButtonNode.methods) {
            if (method.name.codePointAt(0) == '<') {
                continue;
            }
            if (showShortcutListMethod != null) {
                throw new OutdatedDeobfuscatorException("Hotkey", SHOW_SHORTCUT_LIST_BUTTON_CLASS, "onMouseDown", "Collision");
            }
            showShortcutListMethod = method;
        }

        if (showShortcutListMethod == null) {
            throw new OutdatedDeobfuscatorException("Hotkey", SHOW_SHORTCUT_LIST_BUTTON_CLASS, "onMouseDown", "Not found");
        }

        ClassNode shortcutListWidgetClass = this.name2Node.get(((TypeInsnNode) getNext(showShortcutListMethod.instructions.getFirst())).desc);

        remapClass(mappingsStream, shortcutListWidgetClass.name, SHORTCUT_LIST_WIDGET_CLASS);
    }

    public void remapMapModes(Writer mappingsOut) throws IOException {
        ClassNode mapModeNode = name2Node.get(MAP_MODE_CLASS);
        if (mapModeNode == null) {
            throw new OutdatedDeobfuscatorException("MapMode", MAP_MODE_CLASS, "*", "Cannot resolve node");
        }

        String mapModeField = null;

        for (FieldNode field : mapModeNode.fields) {
            if (field.desc.equals("L" + MAP_MODE_ENUM_CLASS + ";")) {
                if (mapModeField != null) {
                    throw new OutdatedDeobfuscatorException("MapMode", MAP_MODE_CLASS, "currentMode", "Collision");
                }
                mapModeField = field.name;
                remapField(mappingsOut, MAP_MODE_CLASS, mapModeField, "currentMode", "L" + MAP_MODE_ENUM_CLASS + ";");
            }
        }

        if (mapModeField == null) {
            throw new OutdatedDeobfuscatorException("MapMode", MAP_MODE_CLASS, "currentMode", "Undefined");
        }

        String rotateMapModeMethod = null;
        String setMapModeMethod = null;
        String getMapModeMethod = null;

        for (MethodNode method : mapModeNode.methods) {
            if (method.desc.equals("()L" + MAP_MODE_ENUM_CLASS + ";")) {
                if (isGetter(method, MAP_MODE_CLASS, mapModeField, "L" + MAP_MODE_ENUM_CLASS + ";", true)) {
                    if (getMapModeMethod != null) {
                        throw new OutdatedDeobfuscatorException("MapMode", MAP_MODE_CLASS, "getCurrentMode", "Collision");
                    }
                    getMapModeMethod = method.name;
                    remapMethod(mappingsOut, MAP_MODE_CLASS, getMapModeMethod, "getCurrentMode", "()L" + MAP_MODE_ENUM_CLASS + ";");
                }
            } else if (method.desc.equals("(L" + MAP_MODE_ENUM_CLASS + ";)V")) {
                if (setMapModeMethod != null) {
                    throw new OutdatedDeobfuscatorException("MapMode", MAP_MODE_CLASS, "setCurrentMode", "Collision");
                }
                setMapModeMethod = method.name;
                remapMethod(mappingsOut, MAP_MODE_CLASS, setMapModeMethod, "setCurrentMode", "(L" + MAP_MODE_ENUM_CLASS + ";)V");
            } else if (method.desc.equals("()V") && method.name.codePointAt(0) != '<') {
                if (rotateMapModeMethod != null) {
                    throw new OutdatedDeobfuscatorException("MapMode", MAP_MODE_CLASS, "rotateCurrentMode", "Collision");
                }
                rotateMapModeMethod = method.name;
                remapMethod(mappingsOut, MAP_MODE_CLASS, rotateMapModeMethod, "rotateCurrentMode", "()V");
            }
        }

        if (getMapModeMethod == null) {
            throw new OutdatedDeobfuscatorException("MapMode", MAP_MODE_CLASS, "getCurrentMode", "Unresolved");
        }
        if (rotateMapModeMethod == null) {
            throw new OutdatedDeobfuscatorException("MapMode", MAP_MODE_CLASS, "rotateCurrentMode", "Unresolved");
        }
        if (setMapModeMethod == null) {
            throw new OutdatedDeobfuscatorException("MapMode", MAP_MODE_CLASS, "setCurrentMode", "Unresolved");
        }

        ClassNode starNode = name2Node.get(STAR_CLASS);
        if (starNode == null) {
            throw new OutdatedDeobfuscatorException("MapMode", STAR_CLASS, "*", "Cannot resolve node");
        }

        MethodNode renderRegionsMethod = null;

        for (MethodNode method : starNode.methods) {
            if (method.desc.equals("()V")) {
                AbstractInsnNode insn = method.instructions.getFirst();
                while (insn != null) {
                    if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        MethodInsnNode methodInsn = (MethodInsnNode) insn;
                        if (methodInsn.owner.equals(GDX_POLYGON_SPRITE)
                                && methodInsn.desc.equals("(L" + GDX_COLOR_CLASS + ";)V")
                                && methodInsn.name.equals("setColor")) {
                            if (renderRegionsMethod != null) {
                                throw new OutdatedDeobfuscatorException("MapMode", STAR_CLASS, "renderRegion", "Collision");
                            }
                            renderRegionsMethod = method;
                            remapMethod(mappingsOut, STAR_CLASS, method.name, "renderRegion", "()V");
                            break;
                        }
                    }
                    insn = insn.getNext();
                }
            }
        }

        if (renderRegionsMethod == null) {
            throw new OutdatedDeobfuscatorException("MapMode", STAR_CLASS, "renderRegion", "Unresolved");
        }

        String starRenderingRegionField;
        {
            String[] fieldName = new String[1];
            StackWalker.walkStack(starNode, renderRegionsMethod, new StackWalkerConsumer() {

                private boolean search = true;

                @Override
                public void preCalculation(AbstractInsnNode instruction, LIFOQueue<StackElement> stack) {
                    if (search && instruction.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        MethodInsnNode methodInsn = (MethodInsnNode) instruction;
                        if (methodInsn.owner.equals(GDX_POLYGON_SPRITE)
                                && methodInsn.desc.equals("(L" + GDX_COLOR_CLASS + ";)V")
                                && methodInsn.name.equals("setColor")) {
                            AbstractSource src = stack.getDelegateList().get(1).source;
                            if (!(src instanceof FieldSource)) {
                                throw new IllegalStateException("Stack walker was unable to capture source of stack element");
                            }
                            FieldInsnNode source = ((FieldSource) src).getInsn();
                            if (!source.owner.equals(STAR_CLASS)) {
                                throw new OutdatedDeobfuscatorException("MapMode", "Source of stack element is not the expected class");
                            }
                            if (!source.desc.equals("L" + GDX_POLYGON_SPRITE + ";")) {
                                throw new OutdatedDeobfuscatorException("MapMode", "Source of stack element does not have the expected signature");
                            }
                            fieldName[0] = source.name;
                            search = false;
                        }
                    }
                }

                @Override
                public void postCalculation(AbstractInsnNode instruction, LIFOQueue<StackElement> stack) {
                    // Unneeded
                }
            });

            starRenderingRegionField = fieldName[0];
            if (starRenderingRegionField == null) {
                throw new OutdatedDeobfuscatorException("MapMode", STAR_CLASS, "starRenderingRegion", "Unresolved");
            }
            remapField(mappingsOut, STAR_CLASS, starRenderingRegionField, "starRenderingRegion", "L" + GDX_POLYGON_SPRITE + ";");
        }
    }

    private void remapMethod(Writer mappingsOut, String owner, String oldName, String newName, String desc) throws IOException {
        if (oldName.equals(newName)) {
            throw new IllegalStateException("old name is equal to new name. Operation is a bit nonsensical");
        }
        if (owner == null) {
            throw new NullPointerException("owner is null");
        }
        if (desc == null) {
            throw new NullPointerException("desc is null");
        }
        try {
            this.remapper.remapMethod(owner, desc, oldName, newName);
            // Format (for valid tiny files): METHOD owner desc srcName dstName
            mappingsOut.write("METHOD " + owner + " " + desc + " " + oldName + " " +  newName + "\n");
        } catch (ConflicitingMappingException e) {
            try {
                throw new RuntimeException("Old mapping: " + this.remapper.getRemappedClassName(owner) + "." + this.remapper.getRemappedMethodName(owner, oldName, desc) + this.remapper.getRemappedMethodDescriptor(desc, new StringBuilder()) + ". Proposed: " + this.remapper.getRemappedClassName(owner) + "." + newName + desc, e);
            } catch (RuntimeException e2) {
                e2.printStackTrace();
            }
        }
    }

    public void remapNoiseGenerators(Writer mappingsString) throws IOException {
        ClassNode fractalStarGenerator = name2Node.get(FRACTAL_STAR_GENERATOR_CLASS);
        if (fractalStarGenerator == null) {
            throw new OutdatedDeobfuscatorException("Noise", FRACTAL_STAR_GENERATOR_CLASS, "*", "Node not present");
        }
        String simplexMethod = null;
        String perlinMethod = null;
        String offsetMethod = null;
        String generateMapMethod = null;
        String perlinGeneratorClass = null;
        String getPerlinNoiseMethod = null;
        for (MethodNode method : fractalStarGenerator.methods) {
            if ((method.access & Opcodes.ACC_PRIVATE) != 0 && method.desc.equals("()V")) {
                AbstractInsnNode insn = method.instructions.getFirst();
                while (insn != null && insn.getOpcode() != Opcodes.LDC) {
                    insn = insn.getNext();
                }
                if (insn == null) {
                    continue;
                }
                LdcInsnNode ldcInsn = (LdcInsnNode) insn;
                if (ldcInsn.cst.equals("FSG: generateSimplexBits")) {
                    if (simplexMethod != null) {
                        throw new OutdatedDeobfuscatorException("Noise", FRACTAL_STAR_GENERATOR_CLASS, "generateSimplex", "Collision");
                    }
                    simplexMethod = method.name;
                } else if (ldcInsn.cst.equals("FSG: generateOffsetBits")) {
                    if (offsetMethod != null) {
                        throw new OutdatedDeobfuscatorException("Noise", FRACTAL_STAR_GENERATOR_CLASS, "generateOffset", "Collision");
                    }
                    offsetMethod = method.name;
                } else if (ldcInsn.cst.equals("FSG: generatePerlinBits")) {
                    if (perlinMethod != null) {
                        throw new OutdatedDeobfuscatorException("Noise", FRACTAL_STAR_GENERATOR_CLASS, "generatePerlin", "Collision");
                    }
                    perlinMethod = method.name;
                    while (insn != null && insn.getOpcode() != Opcodes.BIPUSH) {
                        insn = insn.getNext();
                    }
                    if (insn == null) {
                        throw new OutdatedDeobfuscatorException("Noise", PERLIN_NOISE_GENERATOR_CLASS, "*", "Instructions exhausted (cannot find BIPUSH)");
                    }
                    IntInsnNode biPushInsn = (IntInsnNode) insn;
                    if (biPushInsn.operand != 10) {
                        throw new OutdatedDeobfuscatorException("Noise", PERLIN_NOISE_GENERATOR_CLASS, "*", "BIPUSH has unexpected operand");
                    }
                    insn = insn.getNext();
                    if (insn == null || insn.getOpcode() != Opcodes.INVOKESTATIC) {
                        throw new OutdatedDeobfuscatorException("Noise", PERLIN_NOISE_GENERATOR_CLASS, "*", "Unexpected insn after BIPUSH");
                    }
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;
                    if (!methodInsn.desc.equals("(III)[[F")) {
                        throw new OutdatedDeobfuscatorException("Noise", PERLIN_NOISE_GENERATOR_CLASS, "*", "Unexpected ");
                    }
                    getPerlinNoiseMethod = methodInsn.name;
                    perlinGeneratorClass = methodInsn.owner;
                }
            } else if ((method.access & Opcodes.ACC_PUBLIC) != 0 && method.desc.equals("()V")) {
                AbstractInsnNode insn = method.instructions.getFirst();
                while (insn != null && insn.getOpcode() != Opcodes.LDC) {
                    insn = insn.getNext();
                }
                if (insn == null) {
                    continue;
                }
                LdcInsnNode ldcInsn = (LdcInsnNode) insn;
                if (ldcInsn.cst.equals("FSG: generateMap called")) {
                    if (generateMapMethod != null) {
                        throw new OutdatedDeobfuscatorException("Noise", FRACTAL_STAR_GENERATOR_CLASS, "generateMap", "Collision");
                    }
                    generateMapMethod = method.name;
                }
            }
        }
        if (simplexMethod == null) {
            throw new OutdatedDeobfuscatorException("Noise", FRACTAL_STAR_GENERATOR_CLASS, "generateSimplex", "Unresolved");
        }
        if (perlinMethod == null) {
            throw new OutdatedDeobfuscatorException("Noise", FRACTAL_STAR_GENERATOR_CLASS, "generatePerlin", "Unresolved");
        }
        if (offsetMethod == null) {
            throw new OutdatedDeobfuscatorException("Noise", FRACTAL_STAR_GENERATOR_CLASS, "generateOffset", "Unresolved");
        }
        if (generateMapMethod == null) {
            throw new OutdatedDeobfuscatorException("Noise", FRACTAL_STAR_GENERATOR_CLASS, "generateMap", "Unresolved");
        }
        if (perlinGeneratorClass == null) {
            throw new OutdatedDeobfuscatorException("Noise", PERLIN_NOISE_GENERATOR_CLASS, "*", "Unresolved");
        }
        remapMethod(mappingsString, FRACTAL_STAR_GENERATOR_CLASS, simplexMethod, "generateSimplex", "()V");
        remapMethod(mappingsString, FRACTAL_STAR_GENERATOR_CLASS, perlinMethod, "generatePerlin", "()V");
        remapMethod(mappingsString, FRACTAL_STAR_GENERATOR_CLASS, offsetMethod, "generateOffset", "()V");
        remapMethod(mappingsString, FRACTAL_STAR_GENERATOR_CLASS, generateMapMethod, "generateMap", "()V");
        remapClass(mappingsString, perlinGeneratorClass, PERLIN_NOISE_GENERATOR_CLASS);
        remapMethod(mappingsString, perlinGeneratorClass, getPerlinNoiseMethod, "generatePerlinNoise", "(III)[[F");

        ClassNode perlinGeneratorNode = name2Node.get(perlinGeneratorClass);
        if (perlinGeneratorNode == null) {
            throw new OutdatedDeobfuscatorException("Noise", PERLIN_NOISE_GENERATOR_CLASS, "*", "node missing");
        }

        for (MethodNode method : perlinGeneratorNode.methods) {
            if (method.desc.equals("(III)[[F") && method.name.equals(getPerlinNoiseMethod)) {
                AbstractInsnNode insn = method.instructions.getFirst();
                while (insn != null && !(insn instanceof LabelNode)) {
                    insn = insn.getNext();
                }
                LabelNode firstLabel = (LabelNode) insn;
                while (insn != null && insn.getOpcode() != Opcodes.INVOKESTATIC) {
                    insn = insn.getNext();
                }
                MethodInsnNode firstInsn = (MethodInsnNode) insn;

                if (firstInsn == null) {
                    throw new OutdatedDeobfuscatorException("Noise", PERLIN_NOISE_GENERATOR_CLASS, "generateWhiteNoise", "Instructions exhausted");
                }
                if (!firstInsn.desc.equals("(II)[[F")) {
                    throw new OutdatedDeobfuscatorException("Noise", PERLIN_NOISE_GENERATOR_CLASS, "generateWhiteNoise", "Unexpected descriptor");
                }
                remapMethod(mappingsString, perlinGeneratorClass, firstInsn.name, "generateWhiteNoise", "(II)[[F");

                insn = firstInsn.getNext();
                while (insn != null && insn.getOpcode() != Opcodes.INVOKESTATIC) {
                    insn = insn.getNext();
                }
                MethodInsnNode secondInsn = (MethodInsnNode) insn;
                if (secondInsn == null) {
                    throw new OutdatedDeobfuscatorException("Noise", PERLIN_NOISE_GENERATOR_CLASS, "generatePerlinNoise", "Instructions exhausted");
                }
                if (!secondInsn.desc.equals("([[FI)[[F")) {
                    throw new OutdatedDeobfuscatorException("Noise", PERLIN_NOISE_GENERATOR_CLASS, "generatePerlinNoise", "Unexpected descriptor");
                }
                remapMethod(mappingsString, perlinGeneratorClass, secondInsn.name, "generatePerlinNoise", "([[FI)[[F");

                method.parameters.clear();
                method.parameters.add(new ParameterNode("width", Opcodes.ACC_FINAL));
                method.parameters.add(new ParameterNode("height", Opcodes.ACC_FINAL));
                method.parameters.add(new ParameterNode("octaveCount", Opcodes.ACC_FINAL));

                insn = secondInsn.getNext();
                if (insn == null || insn.getOpcode() != Opcodes.ARETURN) {
                    throw new OutdatedDeobfuscatorException("Noise", PERLIN_NOISE_GENERATOR_CLASS, "generatePerlinNoise", "Unexpected opcode");
                }

                LabelNode lastLabel = new LabelNode();
                method.instructions.add(lastLabel);
                method.localVariables.clear();
                method.localVariables.add(new LocalVariableNode("width", "I", null, firstLabel, lastLabel, 0));
                method.localVariables.add(new LocalVariableNode("height", "I", null, firstLabel, lastLabel, 1));
                method.localVariables.add(new LocalVariableNode("octaveCount", "I", null, firstLabel, lastLabel, 2));
                method.localVariables.add(new LocalVariableNode("baseNoise", "[[F", null, firstLabel, lastLabel, 3));
                break;
            }
        }

        for (ClassNode node : nodes) {
            if (node.superName.equals(FRACTAL_STAR_GENERATOR_CLASS)) {
                // This is faster performance-wise than actually propagating.
                // Should we need to propagate, we will know
                throw new OutdatedDeobfuscatorException("Noise", FRACTAL_STAR_GENERATOR_CLASS, "generateMap", node.name + " has unexpected superclass. Propagation might not happen");
            }
        }
    }

    public void remapPlayerMethods(Writer mappingsString) throws IOException {
        ClassNode playerClass = name2Node.get(PLAYER_CLASS);
        if (playerClass == null) {
            throw new IllegalStateException("The player class was not defined!");
        }
        boolean selectedFlagshipMethod = false;
        boolean selectedGetScoreMethod = false;
        for (MethodNode method : playerClass.methods) {
            if (method.desc.equals("()Lsnoddasmannen/galimulator/actors/Flagship;")) {
                if (selectedFlagshipMethod) {
                    throw new IllegalStateException("Found two Player#getFlagship candidates.");
                }
                selectedFlagshipMethod = true;
                remapMethod(mappingsString, PLAYER_CLASS, method.name, "getFlagship", "()Lsnoddasmannen/galimulator/actors/Flagship;");
            } else if (method.desc.equals("()I")) {
                AbstractInsnNode insn = method.instructions.getFirst();
                while (insn != null) {
                    if (insn.getOpcode() == Opcodes.GETFIELD) {
                        FieldInsnNode node = (FieldInsnNode) insn;
                        if (!node.owner.equals(PLAYER_CLASS) || !node.name.equals("extraPoints")) {
                            break;
                        }
                        AbstractInsnNode next = insn.getNext();
                        if (next.getOpcode() != Opcodes.ALOAD) {
                            break;
                        }
                        next = next.getNext();
                        if (next instanceof FieldInsnNode) {
                            next = next.getNext();
                        } else {
                            break;
                        }
                        if (next instanceof MethodInsnNode) {
                            next = next.getNext();
                        } else {
                            break;
                        }
                        if (next instanceof VarInsnNode) {
                            next = next.getNext();
                        } else {
                            break;
                        }
                        if (next instanceof FieldInsnNode) {
                            FieldInsnNode finsn = (FieldInsnNode) next;
                            if (finsn.owner.equals(PLAYER_CLASS) && finsn.name.equals("startStarCount")) {
                                next = next.getNext();
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                        if (next.getOpcode() != Opcodes.ISUB) {
                            break;
                        }
                        next = next.getNext();
                        if (next.getOpcode() != Opcodes.IADD) {
                            break;
                        }
                        next = next.getNext();
                        if (next.getOpcode() != Opcodes.I2F) {
                            break;
                        }
                        next = next.getNext();
                        if (next instanceof VarInsnNode) {
                            next = next.getNext();
                        } else {
                            break;
                        }
                        if (next instanceof FieldInsnNode) {
                            FieldInsnNode finsn = (FieldInsnNode) next;
                            if (finsn.owner.equals(PLAYER_CLASS) && finsn.name.equals("scoreModifier")) {
                                next = next.getNext();
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                        if (next.getOpcode() != Opcodes.FMUL) {
                            break;
                        }
                        next = next.getNext();
                        if (next.getOpcode() != Opcodes.F2I) {
                            break;
                        }
                        next = next.getNext();
                        if (next.getOpcode() != Opcodes.IRETURN) {
                            break;
                        }
                        if (selectedGetScoreMethod) {
                            throw new IllegalStateException("Guessed two Player#getScore methods, which is nonsensical");
                        }
                        selectedGetScoreMethod = true;
                        remapMethod(mappingsString, PLAYER_CLASS, method.name, "getScore", "()I");
                        break;
                    }
                    insn = insn.getNext();
                }
            }
        }
    }

    public void remapRendersystem(Writer mappingsStream) throws IOException {
        ClassNode renderCacheClass = null;
        Set<String> renderItemDescriptors = new HashSet<>();
        for (ClassNode node : nodes) {
            if (!node.name.startsWith(RENDERSYSTEM_PACKAGE)) {
                continue;
            }
            if (node.superName.equals(RENDER_ITEM_CLASS)) {
                MethodNode ctor = null;
                for (MethodNode method : node.methods) {
                    if (method.name.equals("<init>")) {
                        if (ctor != null) {
                            throw new OutdatedDeobfuscatorException("RenderSystem", node.name, "<init>", "Multiple present");
                        }
                        ctor = method;
                    }
                }
                if (ctor == null) {
                    throw new OutdatedDeobfuscatorException("RenderSystem", node.name, "<init>", "Not present");
                }
                if (!renderItemDescriptors.add(ctor.desc)) {
                    throw new OutdatedDeobfuscatorException("RenderSystem", node.name, "*", "Another class has it's constructor descriptor");
                }
                String deobfuscatedName;
                switch (ctor.desc) {
                case "(Lcom/badlogic/gdx/graphics/Camera;)V":
                    deobfuscatedName = RENDERSYSTEM_PACKAGE + "CameraRenderItem"; // I have 0 clue what it really does
                    break;
                case "(Lcom/badlogic/gdx/graphics/g2d/PolygonSprite;)V":
                    deobfuscatedName = RENDERSYSTEM_PACKAGE + "PolygonRenderItem";
                    break;
                case "([F)V":
                    deobfuscatedName = RENDERSYSTEM_PACKAGE + "VertexRenderItem";
                    break;
                case "(FFFLcom/badlogic/gdx/math/Vector3;Ljava/lang/String;Lsnoddasmannen/galimulator/GalColor;Lsnoddasmannen/galimulator/GalFX$FONT_TYPE;FLcom/badlogic/gdx/graphics/Camera;)V":
                    deobfuscatedName = RENDERSYSTEM_PACKAGE + "TextRenderItem";
                    break;
                case "(Lcom/badlogic/gdx/graphics/g2d/TextureRegion;DDDDDLsnoddasmannen/galimulator/GalColor;ZLcom/badlogic/gdx/graphics/Camera;)V":
                    deobfuscatedName = RENDERSYSTEM_PACKAGE + "TextureRenderItem";
                    break;
                case "(Ljava/lang/String;F)V":
                    deobfuscatedName = RENDERSYSTEM_PACKAGE + "ShaderUniformRenderItem";
                    break;
                case "(Lcom/badlogic/gdx/graphics/glutils/ShaderProgram;)V":
                    deobfuscatedName = RENDERSYSTEM_PACKAGE + "ShaderRenderItem";
                    break;
                default:
                    throw new OutdatedDeobfuscatorException("RenderSystem", node.name, "*", "Unmapped descriptor: " + ctor.desc);
                }
                remapClass(mappingsStream, node.name, deobfuscatedName);
            } else {
                for (MethodNode method : node.methods) {
                    if ((method.access & Opcodes.ACC_SYNCHRONIZED) == 0) {
                        continue;
                    }
                    for (AbstractInsnNode insn : method.instructions) {
                        if (insn.getOpcode() == Opcodes.LDC && ((LdcInsnNode) insn).cst.equals("Who is this thread??")) {
                            if (renderCacheClass != null) {
                                throw new OutdatedDeobfuscatorException("RenderSystem", RENDER_CACHE_CLASS, "*", "Duplicates resolved");
                            }
                            renderCacheClass = node;
                            remapMethod(mappingsStream, node.name, method.name, "pushItem", "(L" + RENDER_ITEM_CLASS + ";)V");
                            break;
                        }
                    }
                }
            }
        }

        if (renderCacheClass == null) {
            throw new OutdatedDeobfuscatorException("RenderSystem", RENDER_CACHE_CLASS, "*", "Not found");
        }
        remapClass(mappingsStream, renderCacheClass.name, RENDER_CACHE_CLASS);

        ClassNode spaceClass = name2Node.get(SPACE_CLASS);
        String drawToCacheMethod = null;
        String drawToCacheMethodDesc = "()L" + renderCacheClass.name + ";";

        for (MethodNode method : spaceClass.methods) {
            if (method.desc.equals(drawToCacheMethodDesc)) {
                for (AbstractInsnNode insn : method.instructions) {
                    if (insn.getOpcode() == Opcodes.INVOKESPECIAL) {
                        MethodInsnNode methodInsn = (MethodInsnNode) insn;
                        if (methodInsn.owner.equals(renderCacheClass.name) && methodInsn.name.equals("<init>")) {
                            if (drawToCacheMethod != null) {
                                throw new OutdatedDeobfuscatorException("RenderSystem", SPACE_CLASS, "drawToCache", "Duplication");
                            }
                            remapMethod(mappingsStream, SPACE_CLASS, method.name, "drawToCache", drawToCacheMethodDesc);
                            drawToCacheMethod = method.name;
                            break;
                        }
                    }
                }
            }
        }

        if (drawToCacheMethod == null) {
            throw new OutdatedDeobfuscatorException("RenderSystem", SPACE_CLASS, "drawToCache", "Not found");
        }

        ClassNode renderCacheCollectorClass = null;
        for (ClassNode node : nodes) {
            if (node.interfaces.size() != 1 || !node.interfaces.get(0).equals("java/lang/Runnable")) {
                continue;
            }
            methodLoop:
            for (MethodNode method : node.methods) {
                if (!method.name.equals("run") || !method.desc.equals("()V")) {
                    continue;
                }
                for (AbstractInsnNode insn : method.instructions) {
                    if (insn.getOpcode() != Opcodes.INVOKESTATIC) {
                        continue;
                    }
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;
                    if (!methodInsn.owner.equals(SPACE_CLASS) || !methodInsn.name.equals(drawToCacheMethod) || !methodInsn.desc.equals(drawToCacheMethodDesc)) {
                        continue;
                    }

                    remapClass(mappingsStream, node.name, RENDER_CACHE_COLLECTOR_CLASS);

                    ClassNode galemulatorClass = null;
                    for (ClassNode node2 : nodes) {
                        if (node2.interfaces.size() != 1 || !node2.interfaces.get(0).equals("com/badlogic/gdx/ApplicationListener") || !node2.name.startsWith(BASE_PACKAGE)) {
                            continue;
                        }
                        if (galemulatorClass != null) {
                            throw new OutdatedDeobfuscatorException("RenderSystem", "Two galemulator classes found");
                        }
                        galemulatorClass = node2;
                    }

                    if (galemulatorClass == null) {
                        throw new OutdatedDeobfuscatorException("RenderSystem", "No galemulator class found");
                    }

                    node.outerClass = galemulatorClass.name;
                    node.innerClasses.removeIf(icn -> icn.name.equals(node.name));
                    galemulatorClass.innerClasses.removeIf(icn -> icn.name.equals(node.name));
                    InnerClassNode icn = new InnerClassNode(node.name, galemulatorClass.name, "RenderCacheCollector", Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
                    node.innerClasses.add(icn);
                    galemulatorClass.innerClasses.add(icn);

                    if (renderCacheCollectorClass != null) {
                        throw new OutdatedDeobfuscatorException("RenderSystem", RENDER_CACHE_COLLECTOR_CLASS, "*", "Collision");
                    }
                    renderCacheCollectorClass = node;
                    break methodLoop;
                }
            }
        }

        if (renderCacheCollectorClass == null) {
            throw new OutdatedDeobfuscatorException("RenderSystem", RENDER_CACHE_COLLECTOR_CLASS, "*", "Not found");
        }
    }

    /**
     * Remap the fields of the snoddasmannen/galimulator/Space class.
     *
     * @param mappingsStream Suggested remapper mappings are written to the writer in the tiny v1 format
     */
    public void remapSpaceFields(Writer mappingsStream) throws IOException {
        ClassNode space = name2Node.get(SPACE_CLASS);
        if (space == null) {
            throw new IllegalStateException("Class not present: " + SPACE_CLASS);
        }
        MethodNode loadMethod = null;
        Map<String, String> fieldRemaps = new HashMap<>();

        String showToastMethod = null;
        String showScreenSizeResetHintMethod = null;
        String openInputDialogMethod = null;
        String addUnbufferedWidgetMethod = null;
        String galimulatorTextInputDialogClass = null;
        String saveAsyncMethod = null;
        String saveSyncMethod = null;
        MethodNode logicalTickMethod = null;
        String getStateActorCreatorsMethod = null;
        String setupBackgroundEffectsMethod = null;
        spaceLogicalTickMethodName = null;

        for (MethodNode method : space.methods) {
            if (method.desc.equals("()V")) {
                for (AbstractInsnNode insn : method.instructions) {
                    if (insn.getOpcode() == Opcodes.LDC) {
                        LdcInsnNode ldcInsn = (LdcInsnNode) insn;
                        if (ldcInsn.cst.equals("[BLACK]Hi![] If you are having issues with screen size, press 'q' to reset display settings!")) {
                            if (showToastMethod != null) {
                                throw new OutdatedDeobfuscatorException("Space", "Space", "showToast", "Collision");
                            }
                            MethodInsnNode methodInsn = (MethodInsnNode) insn.getNext();
                            if (!methodInsn.owner.equals(SPACE_CLASS)) {
                                throw new OutdatedDeobfuscatorException("Space", "Space", "showToast", "Unexpected owner");
                            }
                            if (!methodInsn.desc.equals("(Ljava/lang/String;)V")) {
                                throw new OutdatedDeobfuscatorException("Space", "Space", "showToast", "Unexpected descriptor");
                            }
                            showToastMethod = methodInsn.name;
                            showScreenSizeResetHintMethod = method.name;
                            remapMethod(mappingsStream, SPACE_CLASS, showToastMethod, "showToast", "(Ljava/lang/String;)V");
                            remapMethod(mappingsStream, SPACE_CLASS, method.name, "showScreenSizeResetHint", "()V");
                        }
                    } else if (insn.getOpcode() == Opcodes.NEW) {
                        TypeInsnNode typeInsn = (TypeInsnNode) insn;
                        if (typeInsn.desc.equals(AMBIENT_SNOWFLAKE_EFFECT_CLASS)) {
                            boolean likelyValid = false;
                            while (insn != null) {
                                if (insn.getOpcode() == Opcodes.NEW && ((TypeInsnNode) insn).desc.equals(AMBIENT_STAR_EFFECT_CLASS)) {
                                    likelyValid = true;
                                }
                                insn = insn.getNext();
                            }
                            if (!likelyValid) {
                                throw new OutdatedDeobfuscatorException("Space", "Space", "setupBackgroundEffects", "Did not find reference to " + AMBIENT_STAR_EFFECT_CLASS + " within method " + method.name + ":" + method.desc + ", which was almost selected.");
                            }
                            setupBackgroundEffectsMethod = method.name;
                            break;
                        }
                    }
                }
            } else if (method.desc.equals("()I")) {
                for (AbstractInsnNode insn : method.instructions) {
                    if (insn.getOpcode() != Opcodes.LDC) {
                        continue;
                    }
                    LdcInsnNode ldcInsn = (LdcInsnNode) insn;
                    if (ldcInsn.cst.equals("Autosave (possible to disable in settings)")) {
                        AbstractInsnNode nextInsn = ldcInsn.getNext();
                        if (nextInsn.getOpcode() != Opcodes.LDC) {
                            throw new OutdatedDeobfuscatorException("Space", "Space", "saveAsync", "Unexpected opcode");
                        }
                        nextInsn = nextInsn.getNext();
                        if (nextInsn.getOpcode() != Opcodes.INVOKESTATIC) {
                            throw new OutdatedDeobfuscatorException("Space", "Space", "saveAsync", "Unexpected opcode (2)");
                        }
                        MethodInsnNode saveAsyncInsn = (MethodInsnNode) nextInsn;
                        if (!saveAsyncInsn.owner.equals(SPACE_CLASS) || !saveAsyncInsn.desc.equals("(Ljava/lang/String;Ljava/lang/String;)V")) {
                            throw new OutdatedDeobfuscatorException("Space", "Space", "saveAsync", "Unexpected owner or descriptor");
                        }
                        if (saveAsyncMethod != null) {
                            throw new OutdatedDeobfuscatorException("Space", "Space", "saveAsync", "Collision");
                        }
                        saveAsyncMethod = saveAsyncInsn.name;
                        if (logicalTickMethod != null) {
                            throw new OutdatedDeobfuscatorException("Space", "Space", "tick", "Collision");
                        }
                        logicalTickMethod = method;
                    }
                }
            } else if (method.desc.equals("(Ljava/lang/String;)Z")) {
                AbstractInsnNode insn = method.instructions.getFirst();
                int spaceStateIndex = -1;
                while (insn != null) {
                    if (insn.getOpcode() == Opcodes.CHECKCAST) {
                        TypeInsnNode typeInsn = (TypeInsnNode) insn;
                        if (typeInsn.desc.equals("snoddasmannen/galimulator/SpaceState")) {
                            AbstractInsnNode next = typeInsn.getNext();
                            if (next.getOpcode() == Opcodes.ASTORE) {
                                VarInsnNode vnext = (VarInsnNode) next;
                                if (vnext.var != spaceStateIndex && spaceStateIndex != -1) {
                                    throw new IllegalStateException("Cannot determine the Space#loadState method (var collision)");
                                }
                                spaceStateIndex = vnext.var;
                            }
                        }
                    }
                    insn = insn.getNext();
                }

                if (spaceStateIndex != -1 && loadMethod != null) {
                    throw new IllegalStateException("Multiple Space#loadState methods theorised.");
                } else {
                    loadMethod = method;
                }

                insn = method.instructions.getFirst();
                while (insn != null) {
                    if (insn.getOpcode() == Opcodes.GETFIELD) {
                        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                        if (fieldInsn.owner.equals("snoddasmannen/galimulator/SpaceState")
                                && fieldInsn.getNext().getOpcode() == Opcodes.PUTSTATIC) {
                            FieldInsnNode destinationField = (FieldInsnNode) fieldInsn.getNext();
                            if (destinationField.owner.equals(SPACE_CLASS)) {
                                fieldRemaps.put(destinationField.name + ' ' + destinationField.desc, fieldInsn.name);
                            }
                            remapField(mappingsStream, destinationField.owner, destinationField.name, fieldInsn.name, destinationField.desc);
                        }
                    }
                    insn = insn.getNext();
                }
            } else if (method.desc.equals(SPACE_OPEN_INPUT_DIALOG_DESCRIPTOR)) {
                boolean isMethod = false;
                String firstNewClass = null;
                for (AbstractInsnNode insn : method.instructions) {
                    if (insn.getOpcode() == Opcodes.INVOKEINTERFACE) {
                        MethodInsnNode methodInsn = (MethodInsnNode) insn;
                        if (!methodInsn.owner.equals(GDX_INPUT_CLASS) || !methodInsn.name.equals("getTextInput")) {
                            continue;
                        } else if (openInputDialogMethod != null) {
                            throw new OutdatedDeobfuscatorException("Space", "Space", "openInputDialog", "collision");
                        }
                        openInputDialogMethod = method.name;
                        remapMethod(mappingsStream, SPACE_CLASS, openInputDialogMethod, "openInputDialog", SPACE_OPEN_INPUT_DIALOG_DESCRIPTOR);
                        isMethod = true;
                    } else if (insn.getOpcode() == Opcodes.NEW && firstNewClass == null) {
                        firstNewClass = ((TypeInsnNode) insn).desc;
                    }
                }
                if (isMethod) {
                    AbstractInsnNode insn = method.instructions.getLast();
                    while (insn != null && insn.getOpcode() != Opcodes.INVOKESTATIC) {
                        insn = insn.getPrevious();
                    }
                    if (insn == null) {
                        throw new OutdatedDeobfuscatorException("Space", "Space", "addUnbufferedWidget", "instructions exhausted");
                    }
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;
                    if (!methodInsn.owner.equals(SPACE_CLASS)) {
                        throw new OutdatedDeobfuscatorException("Space", "Space", "addUnbufferedWidget", "unexpected owner");
                    }
                    if (!methodInsn.desc.equals("(L" + WIDGET_CLASS + ";)V")) {
                        throw new OutdatedDeobfuscatorException("Space", "Space", "addUnbufferedWidget", "unexpected descriptor");
                    }
                    addUnbufferedWidgetMethod = methodInsn.name;
                    remapMethod(mappingsStream, SPACE_CLASS, method.name, "addUnbufferedWidget", "(L" + WIDGET_CLASS + ";)V");
                    if (firstNewClass == null) {
                        throw new OutdatedDeobfuscatorException("Space", "TextInputDialogWidget", "*", "Missing instruction");
                    }
                    if (!firstNewClass.startsWith(UI_PACKAGE)) {
                        throw new OutdatedDeobfuscatorException("Space", "TextInputDialogWidget", "*", "Unexpected start of package");
                    }
                    galimulatorTextInputDialogClass = firstNewClass;
                    remapClass(mappingsStream, galimulatorTextInputDialogClass, UI_PACKAGE + "TextInputDialogWidget");
                }
            } else if (method.desc.equals("(Ljava/lang/String;Ljava/lang/String;)V")) {
                AbstractInsnNode insn = method.instructions.getFirst();
                while (insn != null) {
                    if (insn.getOpcode() == Opcodes.LDC) {
                        LdcInsnNode ldcInsn = (LdcInsnNode) insn;
                        if (ldcInsn.cst.equals("Saving galaxy: ")) {
                            if (saveSyncMethod != null) {
                                throw new OutdatedDeobfuscatorException("Space", "Space", "saveSync", "Collision");
                            }
                            saveSyncMethod = method.name;
                            break;
                        }
                    }
                    insn = insn.getNext();
                }
            } else if (method.desc.equals("()Ljava/util/List;")) {
                for (AbstractInsnNode insn : method.instructions) {
                    if (insn.getOpcode() != Opcodes.LDC) {
                        continue;
                    }
                    LdcInsnNode ldcInsn = (LdcInsnNode) insn;
                    if (ldcInsn.cst.equals("Error while getting state actor creators")) {
                        if (getStateActorCreatorsMethod != null) {
                            throw new OutdatedDeobfuscatorException("Space", "Space", "getStateActorCreators", "Collision");
                        }
                        getStateActorCreatorsMethod = method.name;
                        break;
                    }
                }
            }
        }

        String warsField = fieldRemaps.entrySet().stream().filter((e) -> e.getValue().equals("wars")).findFirst().map((e) -> {
            return e.getKey().substring(0, e.getKey().indexOf(' '));
        }).get();

        if (loadMethod == null) {
            throw new IllegalStateException("Unable to locate Space#loadState method.");
        }
        if (showToastMethod == null) {
            throw new OutdatedDeobfuscatorException("Space", "Space", "showToast", "Not resolved");
        }
        if (showScreenSizeResetHintMethod == null) {
            throw new OutdatedDeobfuscatorException("Space", "Space", "showScreenSizeResetHint", "Not resolved");
        }
        if (openInputDialogMethod == null) {
            throw new OutdatedDeobfuscatorException("Space", "Space", "openInputDialog", "Not resolved");
        }
        if (addUnbufferedWidgetMethod == null) {
            throw new OutdatedDeobfuscatorException("Space", "Space", "addUnbufferedWidget", "Not resolved");
        }
        if (galimulatorTextInputDialogClass == null) {
            throw new OutdatedDeobfuscatorException("Space", "TextInputDialogWidget", "*", "Not resolved");
        }
        if (saveAsyncMethod == null) {
            throw new OutdatedDeobfuscatorException("Space", "Space", "saveAsync", "Not resolved");
        }
        if (saveSyncMethod == null) {
            throw new OutdatedDeobfuscatorException("Space", "Space", "saveSync", "Not resolved");
        }
        if (logicalTickMethod == null) {
            throw new OutdatedDeobfuscatorException("Space", "Space", "tick", "Not resolved");
        }
        if (getStateActorCreatorsMethod == null) {
            throw new OutdatedDeobfuscatorException("Space", "Space", "getStateActorCreators", "Not resolved");
        }
        if (setupBackgroundEffectsMethod == null) {
            throw new OutdatedDeobfuscatorException("Space", "Space", "setupBackgroundEffects", "Not resolved");
        }

        remapMethod(mappingsStream, SPACE_CLASS, saveAsyncMethod, "saveAsync", "(Ljava/lang/String;Ljava/lang/String;)V");
        remapMethod(mappingsStream, SPACE_CLASS, saveSyncMethod, "saveSync", "(Ljava/lang/String;Ljava/lang/String;)V");
        remapMethod(mappingsStream, SPACE_CLASS, logicalTickMethod.name, "tick", "()I");
        remapMethod(mappingsStream, SPACE_CLASS, getStateActorCreatorsMethod, "getStateActorCreators", "()Ljava/util/List;");
        remapMethod(mappingsStream, SPACE_CLASS, setupBackgroundEffectsMethod, "setupBackgroundEffects", "()V");
        spaceLogicalTickMethodName = logicalTickMethod.name;

        this.textInputDialogWidgetClass = galimulatorTextInputDialogClass;

        String spaceInitializeMethod = null;
        String aspectRatioField = null;

        Map<String, Map.Entry<String, String>> spstarmappedGetters = new HashMap<>();
        methodLoop:
        for (MethodNode method : space.methods) {
            if (method.desc.equals("(F)V")) {
                for (AbstractInsnNode insn : method.instructions) {
                    if (insn.getOpcode() != Opcodes.INVOKESTATIC) {
                        continue;
                    }
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;
                    if (!methodInsn.name.equals(showScreenSizeResetHintMethod) || !methodInsn.desc.equals("()V") || !methodInsn.owner.equals(SPACE_CLASS)) {
                        continue;
                    }
                    if (spaceInitializeMethod != null) {
                        throw new OutdatedDeobfuscatorException("Space", "Space", "initialize", "Collision");
                    }
                    spaceInitializeMethod = method.name;
                    remapMethod(mappingsStream, SPACE_CLASS, method.name, "initialize", "(F)V");

                    for (AbstractInsnNode insn2 = method.instructions.getFirst(); insn2 != null; insn2 = insn2.getNext()) {
                        if (insn2.getOpcode() != Opcodes.FLOAD) {
                            continue;
                        }
                        AbstractInsnNode next = insn2.getNext();
                        if (next.getOpcode() != Opcodes.PUTSTATIC) {
                            throw new OutdatedDeobfuscatorException("Space", SPACE_CLASS, "aspectRatio", "Unexpected opcode");
                        }
                        FieldInsnNode fieldInsn = (FieldInsnNode) next;
                        if (!fieldInsn.owner.equals(SPACE_CLASS)) {
                            throw new OutdatedDeobfuscatorException("Space", SPACE_CLASS, "aspectRatio", "Unexpected owner");
                        }
                        if (aspectRatioField != null) {
                            throw new OutdatedDeobfuscatorException("Space", SPACE_CLASS, "aspectRatio", "Collision");
                        }
                        aspectRatioField = fieldInsn.name;
                        remapField(mappingsStream, SPACE_CLASS, aspectRatioField, "aspectRatio", "F");
                    }
                }
            } else if (method.desc.startsWith("()") && !method.desc.equals("()V")) {
                // Follows the getter pattern
                AbstractInsnNode returnInsn = null;
                for (AbstractInsnNode insn : method.instructions) {
                    if (isReturn(insn.getOpcode())) {
                        if (returnInsn != null) {
                            continue methodLoop;
                        }
                        returnInsn = insn;
                    }
                }
                if (returnInsn == null) {
                    throw new RuntimeException("Method does not return");
                }
                AbstractInsnNode before = returnInsn.getPrevious();
                if (before.getOpcode() == Opcodes.GETSTATIC) {
                    FieldInsnNode fieldInsn = (FieldInsnNode) before;
                    if (!fieldInsn.owner.equals(SPACE_CLASS)) {
                        continue;
                    }
                    String spstarmapName = fieldRemaps.remove(fieldInsn.name + ' ' + fieldInsn.desc);
                    if (spstarmapName != null) {
                        spstarmapName = "get" + Character.toUpperCase(spstarmapName.charAt(0)) + spstarmapName.substring(1);
                        // Prevent collisions
                        if (!spstarmappedGetters.containsKey(method.name)) {
                            spstarmappedGetters.put(method.name, new AbstractMap.SimpleImmutableEntry<>(spstarmapName, method.desc));
                        } else {
                            spstarmappedGetters.put(method.name, null);
                        }
                    }
                }
            } else if (method.name.equals(addUnbufferedWidgetMethod) && method.desc.equals("(L" + WIDGET_CLASS + ";)V")) {
                AbstractInsnNode insn = method.instructions.getFirst();
                while (insn.getOpcode() != Opcodes.GETSTATIC) {
                    insn = insn.getNext();
                }
                FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                remapField(mappingsStream, SPACE_CLASS, fieldInsn.name, SPACE_OPENED_WIDGETS_FIELD, "Ljava/util/Vector;");
            }
        }

        if (spaceInitializeMethod == null) {
            throw new OutdatedDeobfuscatorException("Space", "Space", "initialize", "Not resolved");
        }
        if (aspectRatioField == null) {
            throw new OutdatedDeobfuscatorException("Space", SPACE_CLASS, "aspectRatio", "Not resolved");
        }

        for (Map.Entry<String, Map.Entry<String, String>> entry : spstarmappedGetters.entrySet()) {
            Map.Entry<String, String> signature = entry.getValue();
            if (signature == null) {
                continue;
            }
            remapMethod(mappingsStream, SPACE_CLASS, entry.getKey(), signature.getKey(), signature.getValue());
        }

        remapMethod(mappingsStream, SPACE_CLASS, loadMethod.name, "loadState", "(Ljava/lang/String;)Z");

        // We can do more - a lot more actually
        AbstractInsnNode insn = loadMethod.instructions.getFirst();
        while (insn != null) {
            if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                MethodInsnNode methodInsn = (MethodInsnNode) insn;
                if (methodInsn.name.equals("useXStream") && methodInsn.desc.equals("()Z")) {
                    remapClass(mappingsStream, methodInsn.owner, "snoddasmannen/galimulator/DeviceConfiguration");
                    break;
                }
            }
            insn = insn.getNext();
        }

        if (insn == null) {
            throw new IllegalStateException("Space#loadState lacks DeviceConfiguration#useXStream call.");
        }

        String pauseMethod = null;

        while (insn != null) {
            if (insn.getOpcode() == Opcodes.INVOKEINTERFACE) {
                MethodInsnNode postRunnableCall = (MethodInsnNode) insn;
                if (postRunnableCall.owner.equals("com/badlogic/gdx/Application")
                        && postRunnableCall.name.equals("postRunnable")
                        && postRunnableCall.desc.equals("(Ljava/lang/Runnable;)V")) {
                    AbstractInsnNode before = postRunnableCall.getPrevious();
                    if (before.getOpcode() != Opcodes.INVOKESPECIAL) {
                        throw new IllegalStateException("Unexpected opcode");
                    }
                    MethodInsnNode constructorInvokation = (MethodInsnNode) before;
                    if (!constructorInvokation.name.equals("<init>")) {
                        throw new IllegalStateException("Unexpected opcode");
                    }

                    ClassNode runnableClass = name2Node.get(constructorInvokation.owner);
                    if (runnableClass == null) {
                        throw new IllegalStateException();
                    }
                    runnableClass.outerMethod = loadMethod.name;
                    runnableClass.outerMethodDesc = loadMethod.desc;
                    runnableClass.outerClass = SPACE_CLASS;
                    if (runnableClass.innerClasses.isEmpty()) {
                        runnableClass.innerClasses.add(new InnerClassNode(runnableClass.name, SPACE_CLASS, null, 0));
                    }
                    boolean hasInnerClassNode = false;
                    for (InnerClassNode icn : space.innerClasses) {
                        if (icn.name.equals(runnableClass.name)) {
                            hasInnerClassNode = true;
                        }
                    }
                    if (!hasInnerClassNode) {
                        space.innerClasses.add(new InnerClassNode(runnableClass.name, SPACE_CLASS, null, 0));
                    }

                    for (MethodNode method : runnableClass.methods) {
                        if (method.name.equals("run") && method.desc.equals("()V")) {
                            AbstractInsnNode runnableInsn = method.instructions.getFirst();
                            while (runnableInsn != null) {
                                if (runnableInsn.getOpcode() == Opcodes.INVOKESTATIC) {
                                    MethodInsnNode pauseInsn = (MethodInsnNode) runnableInsn;
                                    if (!pauseInsn.owner.equals(SPACE_CLASS) || !pauseInsn.desc.equals("(Z)V")) {
                                        throw new IllegalStateException("Unexpected opcode");
                                    }
                                    remapMethod(mappingsStream, SPACE_CLASS, pauseInsn.name, "setPaused", "(Z)V");
                                    pauseMethod = pauseInsn.name;
                                    break;
                                }
                                runnableInsn = runnableInsn.getNext();
                            }
                        }
                    }
                    break;
                }
            }
            insn = insn.getNext();
        }

        if (pauseMethod == null) {
            throw new IllegalStateException("Unable to identify Space#setPaused.");
        }

        String pausedField = null;
        String signPeaceMethod = null;

        for (MethodNode method : space.methods) {
            if (method.name.equals(pauseMethod) && method.desc.equals("(Z)V")) {
                AbstractInsnNode pauseMethodInsn = method.instructions.getFirst();
                boolean didPutstatic = false;
                boolean didInvokestatic = false;
                while (pauseMethodInsn != null) {
                    if (pauseMethodInsn.getOpcode() == Opcodes.PUTSTATIC) {
                        if (didPutstatic) {
                            throw new IllegalStateException("Unexpected bytecode (putstatic happened twice)");
                        }
                        didPutstatic = true;
                        FieldInsnNode fieldInsn = (FieldInsnNode) pauseMethodInsn;
                        if (!fieldInsn.desc.equals("Z")) {
                            throw new IllegalStateException("The estimated Space#paused field is not a boolean.");
                        }
                        if (!fieldInsn.owner.equals(SPACE_CLASS)) {
                            throw new IllegalStateException("The esitmated Space#paused field does not belong to the space class.");
                        }
                        pausedField = fieldInsn.name;
                        remapField(mappingsStream, SPACE_CLASS, fieldInsn.name, "paused", "Z");
                    } else if (pauseMethodInsn.getOpcode() == Opcodes.INVOKESTATIC) {
                        if (didInvokestatic) {
                            throw new IllegalStateException("Unexpected bytecode (invokestatic happened twice)");
                        }
                        didInvokestatic = true;
                        MethodInsnNode methodInsn = (MethodInsnNode) pauseMethodInsn;
                        if (!methodInsn.desc.equals("()V") || !methodInsn.owner.equals(SPACE_CLASS)) {
                            throw new IllegalStateException("Asseration error: Space#displayStepButton not what was expected");
                        }
                        remapMethod(mappingsStream, SPACE_CLASS, methodInsn.name, "displayStepButton", "()V");
                    }
                    pauseMethodInsn = pauseMethodInsn.getNext();
                }
            } else if (method.desc.equals("(L" + EMPIRE_CLASS + ";L" + EMPIRE_CLASS + ";)V")) {
                insn = method.instructions.getFirst();
                LdcInsnNode ldcInsn = getNextOrNull(insn, Opcodes.LDC);
                if (ldcInsn == null || !ldcInsn.cst.equals("Signed PEACE with ")) {
                    continue;
                }
                if (signPeaceMethod != null) {
                    throw new OutdatedDeobfuscatorException("Space", "Space", "signPeace", "Collision");
                }
                signPeaceMethod = method.name;
                MethodInsnNode methodInsn = getNext(insn, Opcodes.INVOKEVIRTUAL);
                if (!methodInsn.owner.equals(EMPIRE_CLASS) || !methodInsn.desc.equals("(L" + EMPIRE_CLASS + ";)V")) {
                    throw new OutdatedDeobfuscatorException("Space", EMPIRE_CLASS, "addPeaceAgreement", "Unexpected desc or owner");
                }
                remapMethod(mappingsStream, EMPIRE_CLASS, methodInsn.name, "addPeaceAgreement", "(L" + EMPIRE_CLASS + ";)V");
                methodInsn = Objects.requireNonNull(getPreviousOrNull(ldcInsn, Opcodes.INVOKEVIRTUAL), "Null insn");
                if (!methodInsn.owner.equals(EMPIRE_CLASS) || !methodInsn.desc.equals("()Z")) {
                    throw new OutdatedDeobfuscatorException("Space", EMPIRE_CLASS, "isNotable", "Unexpected desc or owner");
                }
                remapMethod(mappingsStream, EMPIRE_CLASS, methodInsn.name, "isNotable", "()Z");
                methodInsn = (MethodInsnNode) getNext(ldcInsn, Opcodes.ALOAD).getNext();
                if (!methodInsn.owner.equals(EMPIRE_CLASS) || !methodInsn.desc.equals("()Ljava/lang/String;")) {
                    throw new OutdatedDeobfuscatorException("Space", EMPIRE_CLASS, "getDisplayName", "Unexpected desc or owner");
                }
                remapMethod(mappingsStream, EMPIRE_CLASS, methodInsn.name, "getDisplayName", "()Ljava/lang/String;");
                methodInsn = getNext(methodInsn, Opcodes.INVOKESTATIC);
                if (!methodInsn.owner.equals(SPACE_CLASS)) {
                    throw new OutdatedDeobfuscatorException("Space", SPACE_CLASS, "postBulletin", "Unexpected owner");
                }
                remapMethod(mappingsStream, SPACE_CLASS, methodInsn.name, "postBulletin", methodInsn.desc);
            }
        }

        if (pausedField == null) {
            throw new OutdatedDeobfuscatorException("Space", "Space", "paused");
        }
        if (signPeaceMethod == null) {
            throw new OutdatedDeobfuscatorException("Space", "Space", "signPeace", "Unresolved");
        }
        remapMethod(mappingsStream, SPACE_CLASS, signPeaceMethod, "signPeace", "(L" + EMPIRE_CLASS + ";L" + EMPIRE_CLASS + ";)V");

        boolean foundIsPaused = false;
        for (MethodNode method : space.methods) {
            if (!foundIsPaused && (method.access & Opcodes.ACC_PUBLIC) != 0 && method.desc.equals("()Z") && isGetter(method, SPACE_CLASS, pausedField, "Z", true)) {
                remapMethod(mappingsStream, SPACE_CLASS, method.name, "isPaused", "()Z");
                foundIsPaused = true;
            }
        }

        if (!foundIsPaused) {
            throw new OutdatedDeobfuscatorException("Space", "Space", "isPaused");
        }

        boolean foundSaveStackdepth = false;

        for (ClassNode node : nodes) {
            if (node.interfaces.size() == 1 && node.interfaces.get(0).equals("java/lang/Runnable")) {
                for (MethodNode method : node.methods) {
                    if (method.name.equals("run") && method.desc.equals("()V")) {
                        insn = method.instructions.getFirst();
                        while (insn != null) {
                            if (insn.getOpcode() == Opcodes.LDC && ((LdcInsnNode) insn).cst.equals("Restored from disk, stack depth was: ")) {
                                break;
                            }
                            insn = insn.getNext();
                        }
                        if (insn == null) {
                            break;
                        }
                        insn = insn.getNext().getNext();
                        if (insn.getOpcode() != Opcodes.GETSTATIC) {
                            throw new OutdatedDeobfuscatorException("Space", SPACE_CLASS, "saveStackdepth", "Follow-up instruction has wrong opcode");
                        }
                        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                        if (!fieldInsn.owner.equals(SPACE_CLASS) || !fieldInsn.desc.equals("I")) {
                            throw new OutdatedDeobfuscatorException("Space", SPACE_CLASS, "saveStackdepth", "Follow-up instruction has wrong owner class or descriptor");
                        }
                        remapField(mappingsStream, SPACE_CLASS, fieldInsn.name, "saveStackdepth", "I");
                        if (foundSaveStackdepth) {
                            throw new OutdatedDeobfuscatorException("Space", SPACE_CLASS, "saveStackdepth", "Collision");
                        }
                        foundSaveStackdepth = true;
                        break;
                    }
                }
            }
        }

        if (!foundSaveStackdepth) {
            throw new OutdatedDeobfuscatorException("Space", SPACE_CLASS, "saveStackdepth", "outright missing");
        }

        ClassNode warListWidgetNode = null;
        String warListEntryClass = null;
        String warListWidgetPopulateMethod = null;
        String paginatedWidgetClass = null;
        String getWarNameMethod = null;
        String getWarDisplayScoreMethod = null;
        String getWarDisplayAgeMethod = null;
        String widgetGetInnerWidthMethod = null;

        nodeLoop:
        for (ClassNode node : nodes) {
            if (!node.name.startsWith(UI_PACKAGE)) {
                continue;
            }
            for (MethodNode method : node.methods) {
                if (!method.desc.equals("()V")) {
                    continue;
                }
                for (insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn.getOpcode() != Opcodes.GETSTATIC) {
                        continue;
                    }
                    FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                    if (!fieldInsn.name.equals(warsField)
                            || !fieldInsn.owner.equals(SPACE_CLASS)
                            || !fieldInsn.desc.equals("Ljava/util/Vector;")) {
                        continue;
                    }
                    if (warListWidgetNode != null) {
                        throw new OutdatedDeobfuscatorException("Space", WAR_LIST_WIDGET_CLASS, "*", "Collision");
                    }
                    warListWidgetNode = node;
                    warListWidgetPopulateMethod = method.name;
                    while ((insn = insn.getNext()) != null) {
                        if (insn.getOpcode() != Opcodes.NEW) {
                            continue;
                        }
                        warListEntryClass = ((TypeInsnNode) insn).desc;
                        insn = insn.getNext();
                        break;
                    }
                    while (insn != null) {
                        if (insn.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                            insn = insn.getNext();
                            continue;
                        }
                        MethodInsnNode methodInsn = (MethodInsnNode) insn;
                        if (!methodInsn.owner.equals(WAR_CLASS) || !methodInsn.desc.equals("()Ljava/lang/String;")) {
                            throw new OutdatedDeobfuscatorException("Space", WAR_CLASS, "getWarName", "Invalid owner or descriptor");
                        }
                        getWarNameMethod = methodInsn.name;
                        insn = insn.getNext();
                        break;
                    }
                    while (insn != null) {
                        if (insn.getOpcode() != Opcodes.INVOKEVIRTUAL
                                || ((MethodInsnNode) insn).owner.equals("java/lang/StringBuilder")) {
                            insn = insn.getNext();
                            continue;
                        }
                        MethodInsnNode methodInsn = (MethodInsnNode) insn;
                        if (!methodInsn.owner.equals(WAR_CLASS) || !methodInsn.desc.equals("()Ljava/lang/String;")) {
                            throw new OutdatedDeobfuscatorException("Space", WAR_CLASS, "getDisplayScore", "Invalid owner or descriptor");
                        }
                        getWarDisplayScoreMethod = methodInsn.name;
                        insn = insn.getNext();
                        break;
                    }
                    while (insn != null) {
                        if (insn.getOpcode() != Opcodes.INVOKEVIRTUAL
                                || ((MethodInsnNode) insn).owner.equals("java/lang/StringBuilder")) {
                            insn = insn.getNext();
                            continue;
                        }
                        MethodInsnNode methodInsn = (MethodInsnNode) insn;
                        if (!methodInsn.owner.equals(WAR_CLASS) || !methodInsn.desc.equals("()Ljava/lang/String;")) {
                            throw new OutdatedDeobfuscatorException("Space", WAR_CLASS, "getDisplayAge", "Invalid owner or descriptor");
                        }
                        getWarDisplayAgeMethod = methodInsn.name;
                        insn = insn.getNext();
                        break;
                    }
                    while (insn != null) {
                        if (insn.getOpcode() != Opcodes.INVOKEVIRTUAL
                                || !((MethodInsnNode) insn).owner.equals(node.name)) {
                            insn = insn.getNext();
                            continue;
                        }
                        if (!((MethodInsnNode) insn).desc.equals("()I")) {
                            throw new OutdatedDeobfuscatorException("Space", WIDGET_CLASS, "getInnerWidth", "Invalid descriptor");
                        }
                        widgetGetInnerWidthMethod = ((MethodInsnNode) insn).name;
                        insn = insn.getNext();
                        break;
                    }
                    while (insn != null) {
                        if (insn.getOpcode() != Opcodes.NEW) {
                            insn = insn.getNext();
                            continue;
                        }
                        paginatedWidgetClass = ((TypeInsnNode) insn).desc;
                        break;
                    }
                    for (MethodNode method2 : node.methods) {
                        if (method2.name.equals("<init>") && method2.desc.equals("()V")) {
                            insn = method2.instructions.getFirst();
                            insn = getNext(insn, Opcodes.ICONST_0).getNext();
                            if (insn.getOpcode() != Opcodes.PUTFIELD) {
                                throw new OutdatedDeobfuscatorException("Space", WAR_LIST_WIDGET_CLASS, "__unused0", "Unexpected ocpode");
                            }
                            fieldInsn = (FieldInsnNode) insn;
                            remapField(mappingsStream, node.name, fieldInsn.name, "__unused0", "()I");
                            insn = getNext(insn, Opcodes.ICONST_3).getNext();
                            if (insn.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                                throw new OutdatedDeobfuscatorException("Space", "WidgetLayout", "setHorizontalMargin", "Unexpected ocpode");
                            }
                            MethodInsnNode methodInsn = (MethodInsnNode) insn;
                            if (!methodInsn.desc.equals("(I)V")) {
                                throw new OutdatedDeobfuscatorException("Space", "WidgetLayout", "setHorizontalMargin", "Unexpected descriptor");
                            }
                            for (ClassNode node2 : nodes) {
                                if (isInstanceofClass(node2, methodInsn.owner)) {
                                    remapMethod(mappingsStream, node2.name, methodInsn.name, "setHorizontalMargin", "(I)V");
                                }
                            }
                        }
                    }
                    continue nodeLoop;
                }
            }
        }

        if (warListWidgetNode == null) {
            throw new OutdatedDeobfuscatorException("Space", WAR_LIST_WIDGET_CLASS, "*", "Cannot be resolved");
        }
        if (paginatedWidgetClass == null) {
            throw new OutdatedDeobfuscatorException("Space", PAGINATED_WIDGET_CLASS, "*", "Cannot be resolved");
        }

        remapClass(mappingsStream, warListWidgetNode.name, WAR_LIST_WIDGET_CLASS);
        remapClass(mappingsStream, warListEntryClass, WAR_LIST_ENTRY_CLASS);
        remapClass(mappingsStream, paginatedWidgetClass, PAGINATED_WIDGET_CLASS);
        remapMethod(mappingsStream, warListWidgetNode.name, warListWidgetPopulateMethod, "populate", "()V");
        remapMethod(mappingsStream, WAR_CLASS, getWarNameMethod, "getWarName", "()Ljava/lang/String;");
        remapMethod(mappingsStream, WAR_CLASS, getWarDisplayScoreMethod, "getDisplayScore", "()Ljava/lang/String;");
        remapMethod(mappingsStream, WAR_CLASS, getWarDisplayAgeMethod, "getDisplayAge", "()Ljava/lang/String;");

        ClassNode warListEntryNode = name2Node.get(warListEntryClass);
        String flowButtonClass = name2Node.get(warListEntryNode.superName).superName;

        remapClass(mappingsStream, flowButtonClass, FLOW_BUTTON_CLASS);

        assignAsInnerClass(warListWidgetNode, warListEntryNode, WAR_LIST_ENTRY_CLASS.substring(WAR_LIST_ENTRY_CLASS.lastIndexOf('$') + 1));

        String getLazyMethodName = null;
        String getLazyMethodDesc = null;
        String getOrCreateWarMethod = null;

        for (MethodNode method : space.methods) {
            if (method.desc.equals("()V")) {
                TypeInsnNode typeInsn = getNextOrNull(method.instructions.getFirst(), Opcodes.NEW);
                if (typeInsn != null && typeInsn.desc.equals(warListWidgetNode.name)) {
                    remapMethod(mappingsStream, SPACE_CLASS, method.name, "openActiveWarList", "()V");
                }
            } else if (method.desc.equals("(L" + EMPIRE_CLASS + ";L" + EMPIRE_CLASS + ";)L" + WAR_CLASS + ";")) {
                TypeInsnNode typeInsn = getNextOrNull(method.instructions.getFirst(), Opcodes.NEW);
                if (typeInsn != null && typeInsn.desc.equals(WAR_CLASS)) {
                    remapMethod(mappingsStream, SPACE_CLASS, method.name, "getOrCreateWar", "(L" + EMPIRE_CLASS + ";L" + EMPIRE_CLASS + ";)L" + WAR_CLASS + ";");
                    MethodInsnNode methodInsn = getPreviousOrNull(typeInsn, Opcodes.INVOKEVIRTUAL);
                    if (methodInsn == null) {
                        throw new OutdatedDeobfuscatorException("Space", "Lazy", "get", "Not found");
                    }
                    getLazyMethodName = methodInsn.name;
                    getLazyMethodDesc = methodInsn.desc;
                    remapClass(mappingsStream, getLazyMethodDesc.substring(3, getLazyMethodDesc.length() - 1), BASE_PACKAGE + "Identifiable");
                    getOrCreateWarMethod = method.name;
                }
            }
        }

        if (getOrCreateWarMethod == null) {
            throw new OutdatedDeobfuscatorException("Space", SPACE_CLASS, "getOrCreateWar", "Not found");
        }

        String onTakeStarFromMethod = null;

        for (MethodNode method : space.methods) {
            if (method.desc.equals("(L" + EMPIRE_CLASS + ";L" + EMPIRE_CLASS + ";)V")) {
                MethodInsnNode methodInsn = getNextOrNull(method.instructions.getFirst(), Opcodes.INVOKESTATIC);
                if (methodInsn == null
                        || !methodInsn.owner.equals(SPACE_CLASS)
                        || !methodInsn.name.equals(getOrCreateWarMethod)
                        || !methodInsn.desc.equals("(L" + EMPIRE_CLASS + ";L" + EMPIRE_CLASS + ";)L" + WAR_CLASS + ";")) {
                    continue;
                }
                if (onTakeStarFromMethod != null) {
                    throw new OutdatedDeobfuscatorException("Space", SPACE_CLASS, "onTakeStarFrom", "Collision");
                }
                onTakeStarFromMethod = method.name;
                methodInsn = getNext(methodInsn, Opcodes.INVOKEVIRTUAL);
                if (!methodInsn.owner.equals(WAR_CLASS) || !methodInsn.desc.equals("(L" + EMPIRE_CLASS + ";)V")) {
                    throw new OutdatedDeobfuscatorException("Space", WAR_CLASS, "incrementScore", "Invalid owner or desc");
                }
                remapMethod(mappingsStream, WAR_CLASS, methodInsn.name, "incrementScore", "(L" + EMPIRE_CLASS + ";)V");
            } else if (method.desc.equals("(L" + EMPIRE_CLASS + ";)Ljava/util/List;")) {
                FieldInsnNode fInsn = getNextOrNull(method.instructions.getFirst(), Opcodes.GETSTATIC);
                if (fInsn != null && fInsn.owner.equals(SPACE_CLASS) && fInsn.name.equals(warsField) && fInsn.desc.equals("Ljava/util/Vector;")) {
                    MethodInsnNode methodInsn = getNextOrNull(fInsn, Opcodes.INVOKEVIRTUAL);
                    if (methodInsn != null && methodInsn.owner.equals("java/util/Vector") && methodInsn.name.equals("stream")) {
                        remapMethod(mappingsStream, SPACE_CLASS, method.name, "getParticipatingWars", "(L" + EMPIRE_CLASS + ";)Ljava/util/List;");
                    }
                }
            }
        }

        if (onTakeStarFromMethod == null) {
            throw new OutdatedDeobfuscatorException("Space", SPACE_CLASS, "onTakeStarFrom", "Not found");
        }

        remapMethod(mappingsStream, SPACE_CLASS, onTakeStarFromMethod, "onTakeStarFrom", "(L" + EMPIRE_CLASS + ";L" + EMPIRE_CLASS + ";)V");

        ClassNode starNode = name2Node.get(STAR_CLASS);

        String onHostileTakeoverMethod = null;

        for (MethodNode method : starNode.methods) {
            if (method.desc.equals("(L" + EMPIRE_CLASS + ";)V")) {
                MethodInsnNode methodInsn = getNextOrNull(method.instructions.getFirst(), Opcodes.INVOKESTATIC);
                if (!methodInsn.owner.equals(SPACE_CLASS)
                        || !methodInsn.name.equals(onTakeStarFromMethod)
                        || !methodInsn.desc.equals("(L" + EMPIRE_CLASS + ";L" + EMPIRE_CLASS + ";)V")) {
                    continue;
                }
                if (onHostileTakeoverMethod != null) {
                    throw new OutdatedDeobfuscatorException("Space", STAR_CLASS, "onHostileTakeover", "Collision");
                }
                onHostileTakeoverMethod = method.name;
                methodInsn = getNextOrNull(methodInsn, Opcodes.INVOKEVIRTUAL);
                if (!methodInsn.owner.equals(STAR_CLASS) || !methodInsn.desc.equals("(I)V")) {
                    throw new OutdatedDeobfuscatorException("Space", STAR_CLASS, "setDevelopment", "Invalid owner or desc");
                }
                remapMethod(mappingsStream, STAR_CLASS, methodInsn.name, "setDevelopment", "(I)V");
                LdcInsnNode ldcInsn = getNext(methodInsn, Opcodes.LDC);
                while (!(ldcInsn.cst instanceof Number)
                        || (Math.abs(((Number) ldcInsn.cst).floatValue() - 0.02F) > 0.001F)) {
                    ldcInsn = getNext(ldcInsn, Opcodes.LDC);
                }
                methodInsn = (MethodInsnNode) ldcInsn.getNext();
                if (!methodInsn.owner.equals(STAR_CLASS) || !methodInsn.desc.equals("(F)V")) {
                    throw new OutdatedDeobfuscatorException("Space", STAR_CLASS, "reduceWealthFactor", "Invalid owner or desc");
                }
                remapMethod(mappingsStream, STAR_CLASS, methodInsn.name, "reduceWealthFactor", "(F)V");
            }
        }

        if (onHostileTakeoverMethod == null) {
            throw new OutdatedDeobfuscatorException("Space", STAR_CLASS, "onHostileTakeover", "Not found");
        }

        remapMethod(mappingsStream, STAR_CLASS, onHostileTakeoverMethod, "onHostileTakeover", "(L" + EMPIRE_CLASS + ";)V");

        ClassNode warNode = name2Node.get(WAR_CLASS);
        String getScoreMethod = null;
        String isActiveMethod = null;

        for (MethodNode method : warNode.methods) {
            if (method.desc.equals("(L" + EMPIRE_CLASS + ";)I")) {
                LdcInsnNode ldcInsn = getNextOrNull(method.instructions.getFirst(), Opcodes.LDC);
                if (ldcInsn != null && ldcInsn.cst.equals("Score fetched for empire not in the war, that's weird huh")) {
                    if (getScoreMethod != null) {
                        throw new OutdatedDeobfuscatorException("Space", WAR_CLASS, "getScore", "Collision");
                    }
                    getScoreMethod = method.name;
                }
            } else if (method.name.equals("getItems") && method.desc.equals("()Ljava/util/ArrayList;")) {
                insn = method.instructions.getFirst();
                while (insn != null && !(insn.getOpcode() == Opcodes.LDC && ((LdcInsnNode) insn).cst.equals("Active"))) {
                    insn = insn.getNext();
                }
                if (insn == null) {
                    continue;
                }
                MethodInsnNode methodInsn = getNext(insn, Opcodes.INVOKEVIRTUAL);
                if (!methodInsn.owner.equals(WAR_CLASS) || !methodInsn.desc.equals("()Z")) {
                    throw new OutdatedDeobfuscatorException("Space", WAR_CLASS, "isActive", "Invalid owner or desc");
                }
                isActiveMethod = methodInsn.name;
            }
        }

        if (getScoreMethod == null) {
            throw new OutdatedDeobfuscatorException("Space", WAR_CLASS, "getScore", "Not found");
        }
        if (isActiveMethod == null) {
            throw new OutdatedDeobfuscatorException("Space", WAR_CLASS, "isActive", "Not found");
        }

        for (insn = logicalTickMethod.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                continue;
            }
            MethodInsnNode methodInsn = (MethodInsnNode) insn;
            if (!methodInsn.owner.equals(WAR_CLASS) || !methodInsn.name.equals(isActiveMethod) || !methodInsn.desc.equals("()Z")) {
                continue;
            }
            methodInsn = getNext(methodInsn, Opcodes.INVOKEVIRTUAL);
            if (!methodInsn.owner.equals(WAR_CLASS) || !methodInsn.desc.equals("()V")) {
                throw new OutdatedDeobfuscatorException("Space", WAR_CLASS, "tick", "Invalid owner or desc");
            }
            remapMethod(mappingsStream, WAR_CLASS, methodInsn.name, "tick", "()V");
            break;
        }

        remapMethod(mappingsStream, WAR_CLASS, getScoreMethod, "getScore", "(L" + EMPIRE_CLASS + ";)I");
        remapMethod(mappingsStream, WAR_CLASS, isActiveMethod, "isActive", "()Z");

        for (ClassNode node : nodes) {
            if (isInstanceofWidget(node)) {
                remapMethod(mappingsStream, node.name, widgetGetInnerWidthMethod, "getInnerWidth", "()I");
            } else if (isInstanceofClass(node, BASE_PACKAGE + "Lazy")) {
                remapMethod(mappingsStream, node.name, getLazyMethodName, "get", getLazyMethodDesc);
            }
        }
    }

    public void remapStarMethods(Writer mappingsStream) throws IOException {
        ClassNode starNode = name2Node.get(STAR_CLASS);
        MethodNode tickMethod = null;
        String connectMethod = null;
        String disconnectMethod = null;
        String setOwnerEmpireMethod = null;
        String ownerEmpireField = null;

        methodLoop0:
        for (MethodNode method : starNode.methods) {
            if (method.desc.equals("()V")) {
                AbstractInsnNode insn = method.instructions.getFirst();
                while (insn != null) {
                    if (insn.getOpcode() == Opcodes.LDC) {
                        LdcInsnNode ldcInsn = (LdcInsnNode) insn;
                        if (ldcInsn.cst instanceof Double) {
                            Double d = (Double) ldcInsn.cst;
                            // Yeah, it is beyond me why a double is used there but I couldn't care less
                            if (d.floatValue() == 0.05F) {
                                if (tickMethod != null) {
                                    throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "tick", "Collision");
                                }
                                tickMethod = method;
                                continue methodLoop0;
                            }
                        }
                    }
                    insn = insn.getNext();
                }
            } else if (method.desc.equals("(L" + STAR_CLASS + ";)V")) {
                FieldInsnNode lastGetIntLanes = null;
                for (AbstractInsnNode insn = method.instructions.getLast(); insn != null; insn = insn.getPrevious()) {
                    if (insn.getOpcode() == Opcodes.GETFIELD) {
                        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                        if (fieldInsn.owner.equals(STAR_CLASS) && fieldInsn.name.equals("intLanes")) {
                            lastGetIntLanes = fieldInsn;
                            break;
                        }
                    }
                }

                if (lastGetIntLanes == null) {
                    continue;
                }

                MethodInsnNode operation = null;
                for (AbstractInsnNode insn = lastGetIntLanes; insn != null; insn = insn.getNext()) {
                    if (insn instanceof MethodInsnNode) {
                        MethodInsnNode methodInsn = (MethodInsnNode) insn;
                        if (methodInsn.desc.equals("(Ljava/lang/Object;)Z")) {
                            operation = methodInsn;
                            break;
                        }
                    }
                }

                if (operation == null) {
                    continue;
                } else if (operation.name.equals("add")) {
                    if (connectMethod != null) {
                        throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "connect", "Collison");
                    }
                    connectMethod = method.name;
                } else if (operation.name.equals("remove")) {
                    if (disconnectMethod != null) {
                        throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "disconnect", "Collison");
                    }
                    disconnectMethod = method.name;
                }
            } else if (method.desc.equals("(L" + EMPIRE_CLASS + ";)V")) {
                AbstractInsnNode insn = method.instructions.getFirst();
                while (insn.getOpcode() == -1) {
                    insn = insn.getNext();
                }
                if (insn.getOpcode() != Opcodes.ALOAD) {
                    continue;
                }
                VarInsnNode aLoadThis = (VarInsnNode) insn;
                if (aLoadThis.var != 0 || (insn = insn.getNext()).getOpcode() != Opcodes.ALOAD) {
                    continue;
                }
                VarInsnNode aLoadEmpire = (VarInsnNode) insn;
                if (aLoadEmpire.var != 1 || (insn = insn.getNext()).getOpcode() != Opcodes.PUTFIELD) {
                    continue;
                }
                FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                if (!fieldInsn.owner.equals(STAR_CLASS) || !fieldInsn.desc.equals("L" + EMPIRE_CLASS + ";")) {
                    continue;
                }
                if (ownerEmpireField != null) {
                    throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "ownerEmpire", "Collision");
                }
                ownerEmpireField = fieldInsn.name;
                setOwnerEmpireMethod = method.name;
            }
        }

        if (tickMethod == null) {
            throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "tick", "Not resolved");
        }
        if (connectMethod == null) {
            throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "connect", "Not resolved");
        }
        if (disconnectMethod == null) {
            throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "disconnect", "Not resolved");
        }
        if (ownerEmpireField == null) {
            throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "ownerEmpire", "Not resolved");
        }

        remapMethod(mappingsStream, STAR_CLASS, tickMethod.name, "tick", "()V");
        remapMethod(mappingsStream, STAR_CLASS, connectMethod, "connect", "(L" + STAR_CLASS + ";)V");
        remapMethod(mappingsStream, STAR_CLASS, disconnectMethod, "disconnect", "(L" + STAR_CLASS + ";)V");
        remapField(mappingsStream, STAR_CLASS, ownerEmpireField, "ownerEmpire", "L" + EMPIRE_CLASS + ";");
        remapMethod(mappingsStream, STAR_CLASS, setOwnerEmpireMethod, "setOwnerEmpire", "(L" + EMPIRE_CLASS + ";)V");

        {
            AbstractInsnNode insn = tickMethod.instructions.getFirst();
            FieldInsnNode getWealth = getNext(insn, Opcodes.GETFIELD);
            insn = getWealth.getNext();
            if (insn.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                throw new OutdatedDeobfuscatorException("Star", EMPIRE_CLASS, "addTransientWealth", "Opcode mismatch");
            }
            MethodInsnNode addTransientWealth = (MethodInsnNode) insn;
            if (!addTransientWealth.owner.equals(EMPIRE_CLASS) || !addTransientWealth.desc.equals("(F)V")) {
                throw new OutdatedDeobfuscatorException("Star", EMPIRE_CLASS, "addTransientWealth", "Owner or descriptor mismatch");
            }
            remapMethod(mappingsStream, EMPIRE_CLASS, addTransientWealth.name, "addTransientWealth", "(F)V");
            MethodInsnNode random = getNext(addTransientWealth, Opcodes.INVOKESTATIC);
            if (!random.name.equals("random")) {
                throw new OutdatedDeobfuscatorException("Star", "expected random call where there isn't");
            }
            random = getNext(random, Opcodes.INVOKESTATIC);
            if (!random.owner.equals(MATH_UTILS_CLASS) || !random.name.equals("randomBoolean") || !random.desc.equals("(F)Z")) {
                throw new OutdatedDeobfuscatorException("Star", "expected randomBoolean call where there isn't");
            }
            if (((Float) ((LdcInsnNode) random.getPrevious()).cst).floatValue() != 0.1F) {
                throw new OutdatedDeobfuscatorException("Star", "randomBoolean chance mismatch");
            }
            MethodInsnNode refreshBeacon = getNext(random, Opcodes.INVOKEVIRTUAL);
            if (!refreshBeacon.owner.equals(STAR_CLASS) || !refreshBeacon.desc.equals("()V")) {
                throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "refreshBeaconState", "Owner or descriptor mismatch");
            }
            String refreshBeaconMethod = refreshBeacon.name;
            remapMethod(mappingsStream, STAR_CLASS, refreshBeaconMethod, "refreshBeaconState", "()V");

            insn = getNext(refreshBeacon, Opcodes.INVOKEVIRTUAL);
            MethodInsnNode getDevelopmentGrowthRate = (MethodInsnNode) insn.getNext();
            insn = getDevelopmentGrowthRate.getNext();
            if (insn.getOpcode() != Opcodes.IMUL) {
                throw new OutdatedDeobfuscatorException("Star", EMPIRE_CLASS, "getDevelopmentGrowthRate", "Expected an IMUL op after instruction");
            }
            if (!getDevelopmentGrowthRate.owner.equals(EMPIRE_CLASS) || !getDevelopmentGrowthRate.desc.equals("()I")) {
                throw new OutdatedDeobfuscatorException("Star", EMPIRE_CLASS, "getDevelopmentGrowthRate", "Owner or descriptor mismatch");
            }
            remapMethod(mappingsStream, EMPIRE_CLASS, getDevelopmentGrowthRate.name, "getDevelopmentGrowthRate", "()I");
            insn = insn.getNext();
            if (insn.getOpcode() != Opcodes.INVOKESPECIAL) { // Don't ask why this is INVOKESPECIAL - probably has something to do with that it is private
                throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "addDevelopment", "Unexpected Opcode (Invokevirtual: " + (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) + ")");
            }
            MethodInsnNode addDevelopment = (MethodInsnNode) insn;
            if (!addDevelopment.owner.equals(STAR_CLASS) || !addDevelopment.desc.equals("(I)V")) {
                throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "addDevelopment", "Owner or descriptor mismatch");
            }
            remapMethod(mappingsStream, STAR_CLASS, addDevelopment.name, "addDevelopment", "(I)V");
            FieldInsnNode getHeat = getNext(insn, Opcodes.GETFIELD);
            if (!getHeat.desc.equals("F")) {
                throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "heat", "Invalid descriptor");
            }
            insn = getHeat.getNext();
            if (((Float) ((LdcInsnNode) insn).cst).floatValue() != 0.99F) {
                throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "heat", "Unexpected following instruction");
            }
            remapField(mappingsStream, STAR_CLASS, getHeat.name, "heat", "F");
            insn = getNext(insn, Opcodes.INVOKEVIRTUAL).getNext();
            if (insn.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                throw new OutdatedDeobfuscatorException("Star", EMPIRE_CLASS, "getWealthDecayFactor", "Unexpected Opcode for instruction");
            }
            MethodInsnNode getWealthDecayFactor = (MethodInsnNode) insn;
            if (!getWealthDecayFactor.owner.equals(EMPIRE_CLASS) || !getWealthDecayFactor.desc.equals("()F")) {
                throw new OutdatedDeobfuscatorException("Star", EMPIRE_CLASS, "getWealthDecayFactor", "Owner or descriptor mismatch");
            }
            remapMethod(mappingsStream, EMPIRE_CLASS, getWealthDecayFactor.name, "getWealthDecayFactor", "()F");
            if ((insn = insn.getNext()).getOpcode() != Opcodes.FMUL) {
                throw new OutdatedDeobfuscatorException("Star", EMPIRE_CLASS, "getWealthDecayFactor", "Unexpected Opcode after instruction");
            }
            insn = getNext(insn, Opcodes.GETFIELD);
            FieldInsnNode getNeighbours = getNext(insn, Opcodes.GETFIELD);
            if (!getNeighbours.owner.equals(STAR_CLASS) || !getNeighbours.desc.equals("Ljava/util/Vector;")) {
                throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "neighbours", "Owner or descriptor mismatch");
            }
            remapField(mappingsStream, STAR_CLASS, getNeighbours.name, "neighbours", "Ljava/util/Vector;");
            TypeInsnNode checkcast = getNext(getNeighbours, Opcodes.CHECKCAST);
            if (!checkcast.desc.equals(STAR_CLASS)) {
                throw new OutdatedDeobfuscatorException("Star", "Checkcast not casting to expected type. Expected Star, current casting to " + checkcast.desc + " instead.");
            }
            checkcast = getNext(checkcast, Opcodes.CHECKCAST);
            if (!checkcast.desc.equals("java/lang/Boolean")) {
                throw new OutdatedDeobfuscatorException("Star", ENUM_SETTINGS_CLASS, "getValue", "Casting to invalid type. Instead casting to " + checkcast.desc);
            }
            AbstractInsnNode prev = checkcast.getPrevious();
            if (prev.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                throw new OutdatedDeobfuscatorException("Star", ENUM_SETTINGS_CLASS, "getValue", "Unexpected opcode for instruction");
            }
            MethodInsnNode getSettingValue = (MethodInsnNode) prev;
            if (!getSettingValue.owner.equals(ENUM_SETTINGS_CLASS) || !getSettingValue.desc.equals("()Ljava/lang/Object;")) {
                throw new OutdatedDeobfuscatorException("Star", ENUM_SETTINGS_CLASS, "getValue", "Unexpected owner or descriptor");
            }

            ClassNode enumSettingsNode = name2Node.get(ENUM_SETTINGS_CLASS);
            if (enumSettingsNode.interfaces.size() != 1) {
                throw new OutdatedDeobfuscatorException("Star", CONFIGURABLE_PREFERNCE_INTERFACE, "*", "Not an exactly ideal number of interfaces present");
            }
            String configurablePreferenceClass = enumSettingsNode.interfaces.get(0);
            remapClass(mappingsStream, configurablePreferenceClass, CONFIGURABLE_PREFERNCE_INTERFACE);
            for (ClassNode node : nodes) {
                if (isInstanceofInterface(node, configurablePreferenceClass)) {
                    remapMethod(mappingsStream, node.name, getSettingValue.name, "getValue", "()Ljava/lang/Object;");
                }
            }

            MethodInsnNode isCapital = getNext(checkcast.getNext(), Opcodes.INVOKEVIRTUAL);
            if (!isCapital.owner.equals(STAR_CLASS) || !isCapital.desc.equals("()Z")) {
                throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "isCapital", "Unexpected owner or descriptor");
            }
            insn = isCapital.getNext();
            if (insn.getOpcode() != Opcodes.IFEQ) {
                throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "isCapital", "Unexpected opcode after instruction");
            }
            remapMethod(mappingsStream, STAR_CLASS, isCapital.name, "isCapital", "()Z");
            String isCapitalMethod = isCapital.name;
            LdcInsnNode magicConstant = null;
            while (insn != null) {
                if (insn.getOpcode() == Opcodes.LDC) {
                    LdcInsnNode ldcInsn = (LdcInsnNode) insn;
                    if (((Float) ldcInsn.cst).floatValue() == 500F) {
                        magicConstant = ldcInsn;
                        break;
                    }
                }
                insn = insn.getNext();
            }
            if (magicConstant == null) {
                throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "getArtifact", "Unable to find magic constant");
            }
            MethodInsnNode getArtifact = getNext(magicConstant, Opcodes.INVOKEVIRTUAL);
            if (!getArtifact.owner.equals(STAR_CLASS) || !getArtifact.desc.equals("()L" + ARTIFACT_CLASS + ";")) {
                throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "getArtifact", "Unexpected owner or descriptor");
            }
            remapMethod(mappingsStream, STAR_CLASS, getArtifact.name, "getArtifact", "()L" + ARTIFACT_CLASS + ";");

            MethodInsnNode methodInsn = getNext(getArtifact, Opcodes.INVOKESPECIAL);
            if (!methodInsn.owner.equals("java/util/ArrayList")) {
                throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "addSprawl", "Unexpected owner of guidance instruction");
            }
            MethodInsnNode addSprawl = getNext(methodInsn, Opcodes.INVOKESPECIAL);
            if (!addSprawl.owner.equals(STAR_CLASS) || !addSprawl.desc.equals("()V")) {
                throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "addSprawl", "Unexpected owner or descriptor");
            }
            remapMethod(mappingsStream, STAR_CLASS, addSprawl.name, "addSprawl", "()V");
            MethodInsnNode removeSprawl = getNext(addSprawl, Opcodes.INVOKESPECIAL);
            if (!removeSprawl.owner.equals(STAR_CLASS) || !removeSprawl.desc.equals("()V")) {
                throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "removeSprawl", "Unexpected owner or descriptor");
            }
            String removeSprawlMethod = removeSprawl.name;
            remapMethod(mappingsStream, STAR_CLASS, removeSprawlMethod, "removeSprawl", "()V");

            methodInsn = getNext(removeSprawl, Opcodes.INVOKEVIRTUAL);
            while (!methodInsn.owner.equals(SPRAWL_CLASS) || !methodInsn.name.equals("activity")) {
                methodInsn = getNext(methodInsn, Opcodes.INVOKEVIRTUAL);
            }
            FieldInsnNode getOrbitingActors = getNext(methodInsn, Opcodes.GETFIELD);
            if (!getOrbitingActors.owner.equals(STAR_CLASS) || !getOrbitingActors.desc.equals("Ljava/util/Set;")) {
                throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "orbitingActors", "Unexpected owner or descriptor");
            }
            String orbitingActorsField = getOrbitingActors.name;
            remapField(mappingsStream, STAR_CLASS, orbitingActorsField, "orbitingActors", "Ljava/util/Set;");
            methodInsn = getNext(getOrbitingActors, Opcodes.INVOKEVIRTUAL);
            if (!methodInsn.owner.equals(STAR_CLASS) || !methodInsn.desc.equals("()L" + STAR_NATIVES_CLASS + ";")) {
                throw new OutdatedDeobfuscatorException("Star", STAR_NATIVES_CLASS, "spawnNativeActor", "Unexpected owner or descriptor of anchor instruction");
            }
            methodInsn = getNext(methodInsn, Opcodes.INVOKEVIRTUAL);
            while (!methodInsn.owner.equals(STAR_CLASS) || !methodInsn.desc.equals("()L" + STAR_NATIVES_CLASS + ";")) {
                methodInsn = getNext(methodInsn, Opcodes.INVOKEVIRTUAL);
            }
            MethodInsnNode spawnNativeActor = getNext(methodInsn, Opcodes.INVOKEVIRTUAL);
            if (!spawnNativeActor.owner.equals(STAR_NATIVES_CLASS) || !spawnNativeActor.desc.equals("(L" + STAR_CLASS + ";)L" + STATE_ACTOR_CLASS + ";")) {
                throw new OutdatedDeobfuscatorException("Star", STAR_NATIVES_CLASS, "spawnNativeActor", "Unexpected owner or descriptor");
            }
            remapMethod(mappingsStream, STAR_NATIVES_CLASS, spawnNativeActor.name, "spawnNativeActor", "(L" + STAR_CLASS + ";)L" + STATE_ACTOR_CLASS + ";");
            insn = spawnNativeActor.getNext();
            if (insn.getOpcode() != Opcodes.INVOKESTATIC) {
                throw new OutdatedDeobfuscatorException("Star", SPACE_CLASS, "addActor", "Unexpected opcode");
            }
            MethodInsnNode addActor = (MethodInsnNode) insn;
            if (!addActor.owner.equals(SPACE_CLASS) || !addActor.desc.equals("(L" + ACTOR_CLASS + ";)L" + ACTOR_CLASS + ";")) {
                throw new OutdatedDeobfuscatorException("Star", SPACE_CLASS, "addActor", "Unexpected owner or descriptor");
            }
            remapMethod(mappingsStream, SPACE_CLASS, addActor.name, "addActor", "(L" + ACTOR_CLASS + ";)L" + ACTOR_CLASS + ";");
            methodInsn = getNext(addActor, Opcodes.INVOKEVIRTUAL);
            while (!methodInsn.owner.equals(EMPIRE_CLASS)) {
                methodInsn = getNext(methodInsn, Opcodes.INVOKEVIRTUAL);
            }
            if (!methodInsn.desc.equals("(L" + STAR_CLASS + ";L" + STAR_CLASS + ";)Z")) {
                throw new OutdatedDeobfuscatorException("Star", EMPIRE_CLASS, "canAttack", "Unexpected descriptor");
            }
            remapMethod(mappingsStream, EMPIRE_CLASS, methodInsn.name, "canAttack", "(L" + STAR_CLASS + ";L" + STAR_CLASS + ";)Z");
            insn = methodInsn.getNext();
            if (insn.getOpcode() != Opcodes.IFEQ) {
                throw new OutdatedDeobfuscatorException("Star", EMPIRE_CLASS, "canAttack", "Following opcode should be IFEQ, but is something different");
            }
            MethodInsnNode incrementHeat = getNext(insn, Opcodes.INVOKESPECIAL);
            if (!incrementHeat.owner.equals(STAR_CLASS) || !incrementHeat.desc.equals("()V")) {
                throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "incrementHeat", "Unexpected owner or descriptor");
            }
            remapMethod(mappingsStream, STAR_CLASS, incrementHeat.name, "incrementHeat", "()V");
            TypeInsnNode typeInsn = getNext(incrementHeat, Opcodes.NEW);
            if (!typeInsn.desc.contains("snoddasmannen/galimulator/Debris")) {
                throw new OutdatedDeobfuscatorException("Star", EMPIRE_CLASS, "getColor", "Unexpected NEW instruction for anchor instruction");
            }
            MethodInsnNode getColor = getNext(typeInsn, Opcodes.INVOKEVIRTUAL);
            if (!getColor.owner.equals(EMPIRE_CLASS) || !getColor.desc.equals("()L" + GALCOLOR_CLASS + ";")) {
                throw new OutdatedDeobfuscatorException("Star", EMPIRE_CLASS, "getColor", "Unexpected owner or descriptor");
            }
            remapMethod(mappingsStream, EMPIRE_CLASS, getColor.name, "getColor", "()L" + GALCOLOR_CLASS + ";");
            methodInsn = getNext(getColor, Opcodes.INVOKESPECIAL);
            while (!methodInsn.owner.equals("snoddasmannen/galimulator/effects/AuraEffect")) {
                methodInsn = getNext(methodInsn, Opcodes.INVOKESPECIAL);
            }
            if (!methodInsn.name.equals("<init>") || !methodInsn.desc.equals("(DDDZDILsnoddasmannen/galimulator/GalColor;)V")) {
                throw new OutdatedDeobfuscatorException("Star", EMPIRE_CLASS, "getCombatPower", "Unexpected name or descriptor of anchor instruction");
            }
            MethodInsnNode getCombatPower = getNext(getNext(methodInsn, Opcodes.INVOKEVIRTUAL), Opcodes.INVOKEVIRTUAL);
            if (!getCombatPower.owner.equals(EMPIRE_CLASS) || !getCombatPower.desc.equals("(L" + STAR_CLASS + ";L" + STAR_CLASS + ";Z)I")) {
                throw new OutdatedDeobfuscatorException("Star", EMPIRE_CLASS, "getCombatPower", "Unexpected owner or descriptor");
            }
            remapMethod(mappingsStream, EMPIRE_CLASS, getCombatPower.name, "getCombatPower", "(L" + STAR_CLASS + ";L" + STAR_CLASS + ";Z)I");

            String getForeignConnectionsMethod = null;
            String setBeaconMethod = null;
            String addOrbitingActorMethod = null;

            for (MethodNode method : starNode.methods) {
                if (method.desc.equals("()V")) {
                    if (method.name.equals(refreshBeaconMethod)) {
                        MethodInsnNode getForeignConnections = getNext(method.instructions.getFirst(), Opcodes.INVOKEVIRTUAL);
                        if (!getForeignConnections.owner.equals(STAR_CLASS) || !getForeignConnections.desc.equals("(Z)Ljava/util/Vector;")) {
                            throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "getForeignConnections", "Owner or descriptor mismatch");
                        }
                        remapMethod(mappingsStream, STAR_CLASS, getForeignConnections.name, "getForeignConnections", "(Z)Ljava/util/Vector;");
                        getForeignConnectionsMethod = getForeignConnections.name;
                        MethodInsnNode setBeacon = getNext(getForeignConnections.getNext(), Opcodes.INVOKEVIRTUAL);
                        if (!setBeacon.owner.equals(STAR_CLASS) || !setBeacon.desc.equals("(Z)V")) {
                            throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "setBeacon", "Owner or descriptor mismatch");
                        }
                        remapMethod(mappingsStream, STAR_CLASS, setBeacon.name, "setBeacon", "(Z)V");
                        setBeaconMethod = setBeacon.name;
                    } else if (method.name.equals(removeSprawlMethod)) {
                        TypeInsnNode var10001 = getNext(method.instructions.getFirst(), Opcodes.CHECKCAST);
                        if (!var10001.desc.equals(SPRAWL_CLASS)) {
                            throw new OutdatedDeobfuscatorException("Star", SPRAWL_CLASS, "setAlive", "Checkcast type unexpected");
                        }
                        MethodInsnNode setAlive = getNext(var10001, Opcodes.INVOKEVIRTUAL);
                        if (!setAlive.owner.equals(SPRAWL_CLASS) || !setAlive.desc.equals("(Z)V")) {
                            throw new OutdatedDeobfuscatorException("Star", SPRAWL_CLASS, "setAlive", "Owner or descriptor mismatch");
                        }
                        remapMethod(mappingsStream, SPRAWL_CLASS, setAlive.name, "setAlive", "(Z)V");
                    }
                } else if (method.desc.equals("()Z")) {
                    if (method.name.equals(isCapitalMethod)) {
                        MethodInsnNode getCapital = getNext(getNext(method.instructions.getFirst(), Opcodes.INVOKEVIRTUAL), Opcodes.INVOKEVIRTUAL);
                        if (!getCapital.owner.equals(EMPIRE_CLASS) || !getCapital.desc.equals("()L" + STAR_CLASS + ";")) {
                            throw new OutdatedDeobfuscatorException("Star", EMPIRE_CLASS, "getCapital", "Owner or descriptor mismatch");
                        }
                        remapMethod(mappingsStream, EMPIRE_CLASS, getCapital.name, "getCapital", "()L" + STAR_CLASS + ";");
                    }
                } else if (method.desc.equals("(L" + STATE_ACTOR_CLASS + ";)V")) {
                    AbstractInsnNode var10001 = getNext(method.instructions.getFirst());
                    if (var10001.getOpcode() != Opcodes.ALOAD || ((VarInsnNode) var10001).var != 0) {
                        continue;
                    }
                    var10001 = var10001.getNext();
                    if (var10001.getOpcode() != Opcodes.GETFIELD) {
                        continue;
                    }
                    FieldInsnNode var10002 = (FieldInsnNode) var10001;
                    if (!var10002.owner.equals(STAR_CLASS) || !var10002.desc.equals("Ljava/util/Set;") || !var10002.name.equals(orbitingActorsField)) {
                        continue;
                    }
                    if (addOrbitingActorMethod != null) {
                        throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "addOrbitingActor", "Collision");
                    }
                    addOrbitingActorMethod = method.name;
                    remapMethod(mappingsStream, STAR_CLASS, addOrbitingActorMethod, "addOrbitingActor", "(L" + STATE_ACTOR_CLASS + ";)V");
                }
            }

            if (getForeignConnectionsMethod == null) {
                throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "getForeignConnections", "Unresolved");
            }
            if (addOrbitingActorMethod == null) {
                throw new OutdatedDeobfuscatorException("Star", STAR_CLASS, "addOrbitingActor", "Unresolved");
            }

            for (MethodNode method : starNode.methods) {
                if (method.desc.equals("(Z)Ljava/util/Vector;")) {
                    if (method.name.equals(getForeignConnectionsMethod)) {
                        method.signature = "(Z)Ljava/util/Vector<L" + STAR_CLASS + ";>;";
                    }
                } else if (method.desc.equals("(Z)V")) {
                    if (method.name.equals(setBeaconMethod)) {
                        VarInsnNode var10001 = getNext(method.instructions.getFirst(), Opcodes.ILOAD);
                        if (var10001.getNext().getOpcode() != Opcodes.IFEQ) {
                            throw new OutdatedDeobfuscatorException("Star", "If-block order mismatch in the Star#setBeacon method");
                        }
                        MethodInsnNode var10002 = getNext(var10001, Opcodes.INVOKEVIRTUAL);
                        if (!var10002.owner.equals(STAR_CLASS) || !var10002.desc.equals("()L" + EMPIRE_CLASS + ";")) {
                            throw new OutdatedDeobfuscatorException("Star", EMPIRE_CLASS, "addBeacon", "Preceding instruction has issues");
                        }
                        var10002 = getNext(var10002, Opcodes.INVOKEVIRTUAL);
                        if (!var10002.owner.equals(EMPIRE_CLASS) || !var10002.desc.equals("(L" + STAR_CLASS + ";)V")) {
                            throw new OutdatedDeobfuscatorException("Star", EMPIRE_CLASS, "addBeacon", "Owner or descriptor mismatch");
                        }
                        remapMethod(mappingsStream, EMPIRE_CLASS, var10002.name, "addBeacon", "(L" + STAR_CLASS + ";)V");
                        var10002 = getNext(var10002, Opcodes.INVOKEVIRTUAL);
                        if (!var10002.owner.equals(STAR_CLASS) || !var10002.desc.equals("()L" + EMPIRE_CLASS + ";")) {
                            throw new OutdatedDeobfuscatorException("Star", EMPIRE_CLASS, "removeBeacon", "Preceding instruction has issues");
                        }
                        var10002 = getNext(var10002, Opcodes.INVOKEVIRTUAL);
                        if (!var10002.owner.equals(EMPIRE_CLASS) || !var10002.desc.equals("(L" + STAR_CLASS + ";)V")) {
                            throw new OutdatedDeobfuscatorException("Star", EMPIRE_CLASS, "removeBeacon", "Owner or descriptor mismatch");
                        }
                        remapMethod(mappingsStream, EMPIRE_CLASS, var10002.name, "removeBeacon", "(L" + STAR_CLASS + ";)V");
                    }
                }
            }
        }

        ClassNode quadTreeClass = null;

        classLoop:
        for (ClassNode node : nodes) {
            for (MethodNode method : node.methods) {
                if (method.desc.equals("()Z")) {
                    for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                        if (insn.getOpcode() == Opcodes.LDC) {
                            LdcInsnNode ldcInsn = (LdcInsnNode) insn;
                            if (ldcInsn.cst.equals("Unable to insert star into quad tree!")) {
                                if (quadTreeClass != null) {
                                    throw new OutdatedDeobfuscatorException("Star", QUAD_TREE_CLASS, "*", "Collision");
                                }
                                quadTreeClass = node;
                                continue classLoop;
                            }
                        }
                    }
                }
            }
        }

        if (quadTreeClass == null) {
            throw new OutdatedDeobfuscatorException("Star", QUAD_TREE_CLASS, "*", "Unresolved");
        }

        String quadtreeInsert = null;
        String quadtreeX1 = null;
        String quadtreeX2 = null;
        String quadtreeY1 = null;
        String quadtreeY2 = null;

        methodLoop:
        for (MethodNode method : quadTreeClass.methods) {
            if (method.desc.equals("(L" + STAR_CLASS + ";)Z")) {
                for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn.getOpcode() == Opcodes.PUTFIELD) {
                        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                        if (fieldInsn.owner.equals(quadTreeClass.name) && fieldInsn.desc.equals("L" + STAR_CLASS + ";")) {
                            if (quadtreeInsert != null) {
                                throw new OutdatedDeobfuscatorException("Star", QUAD_TREE_CLASS, "insert", "Collision");
                            }
                            quadtreeInsert = method.name;
                            continue methodLoop;
                        }
                    }
                }
            } else if (method.desc.equals("(FFFF)V") && method.name.equals("<init>")) {
                if (method.parameters == null) {
                    method.parameters = new ArrayList<>();
                }
                if (method.parameters.isEmpty()) {
                    method.parameters.add(new ParameterNode("x1", Opcodes.ACC_FINAL));
                    method.parameters.add(new ParameterNode("y1", Opcodes.ACC_FINAL));
                    method.parameters.add(new ParameterNode("x2", Opcodes.ACC_FINAL));
                    method.parameters.add(new ParameterNode("y2", Opcodes.ACC_FINAL));
                }
                String[] fieldNames = new String[method.maxLocals];
                for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn.getOpcode() == Opcodes.FLOAD) {
                        VarInsnNode varInsn = (VarInsnNode) insn;
                        if (insn.getNext().getOpcode() == Opcodes.PUTFIELD) {
                            FieldInsnNode fieldInsn = (FieldInsnNode) insn.getNext();
                            if (fieldInsn.owner.equals(quadTreeClass.name)) {
                                fieldNames[varInsn.var] = fieldInsn.name;
                            }
                        }
                    }
                }

                quadtreeX1 = fieldNames[1];
                quadtreeY1 = fieldNames[2];
                quadtreeX2 = fieldNames[3];
                quadtreeY2 = fieldNames[4];
            }
        }

        if (quadtreeInsert == null) {
            throw new OutdatedDeobfuscatorException("Star", QUAD_TREE_CLASS, "insert", "Unresolved");
        }
        if (quadtreeX1 == null || quadtreeX2 == null) {
            throw new OutdatedDeobfuscatorException("Star", QUAD_TREE_CLASS, "x1/x2", "Unresolved");
        }
        if (quadtreeY1 == null || quadtreeY2 == null) {
            throw new OutdatedDeobfuscatorException("Star", QUAD_TREE_CLASS, "y1/y2", "Unresolved");
        }

        remapClass(mappingsStream, quadTreeClass.name, QUAD_TREE_CLASS);
        remapMethod(mappingsStream, quadTreeClass.name, quadtreeInsert, "insert", "(L" + STAR_CLASS + ";)Z");
        remapField(mappingsStream, quadTreeClass.name, quadtreeX1, "x1", "F");
        remapField(mappingsStream, quadTreeClass.name, quadtreeY1, "y1", "F");
        remapField(mappingsStream, quadTreeClass.name, quadtreeX2, "x2", "F");
        remapField(mappingsStream, quadTreeClass.name, quadtreeY2, "y2", "F");

        ClassNode spaceNode = name2Node.get(SPACE_CLASS);
        if (spaceNode == null) {
            throw new OutdatedDeobfuscatorException("Star", SPACE_CLASS, "*", "Unresolved");
        }

        String buildPairsMethod = null;
        String regenerateVoronoiCellsMethod = null;
        String setMottoMethod = null;
        String resetSpecialsMethod = null;
        String registerEmployerMethod = null;
        String quadTreeField = null;
        String spaceConnectStarsMethod = null;
        String spaceDisconnectStarsMethod = null;
        String restoreQuadTreeMethod = null;

        for (MethodNode method : spaceNode.methods) {
            if (method.desc.equals("(IL" + MAPDATA_CLASS + ";)V")) {
                for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn.getOpcode() == Opcodes.LDC) {
                        LdcInsnNode ldcInsn = (LdcInsnNode) insn;
                        if (ldcInsn.cst.equals("Generating galaxy: Building pairs")) {
                            MethodInsnNode methodInsn = getNext(ldcInsn, Opcodes.INVOKEVIRTUAL);
                            if (!methodInsn.owner.equals(quadTreeClass.name) || !methodInsn.desc.equals("()V")) {
                                throw new OutdatedDeobfuscatorException("Star", QUAD_TREE_CLASS, "buildPairs", "Unexpected owner or descriptor");
                            }
                            if (buildPairsMethod != null) {
                                throw new OutdatedDeobfuscatorException("Star", QUAD_TREE_CLASS, "buildPairs", "Collision");
                            }
                            buildPairsMethod = methodInsn.name;
                        } else if (ldcInsn.cst.equals("Generating galaxy: Generating star regions")) {
                            AbstractInsnNode target = getNext(ldcInsn.getNext());
                            if (target.getOpcode() != Opcodes.INVOKESTATIC) {
                                throw new OutdatedDeobfuscatorException("Star", SPACE_CLASS, "regenerateVoronoiCells", "Unexpected opcode");
                            }
                            MethodInsnNode methodInsn = (MethodInsnNode) target;
                            if (!methodInsn.owner.equals(SPACE_CLASS) || !methodInsn.desc.equals("()V")) {
                                throw new OutdatedDeobfuscatorException("Star", SPACE_CLASS, "regenerateVoronoiCells", "Unexpected owner or descriptor");
                            }
                            if (regenerateVoronoiCellsMethod != null) {
                                throw new OutdatedDeobfuscatorException("Star", SPACE_CLASS, "regenerateVoronoiCells", "Collision");
                            }
                            regenerateVoronoiCellsMethod = methodInsn.name;
                        } else if (ldcInsn.cst.equals("Leave us alone")) {
                            AbstractInsnNode target = ldcInsn.getNext();
                            if (target.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                                throw new OutdatedDeobfuscatorException("Star", EMPIRE_CLASS, "setMotto", "Unexpected opcode");
                            }
                            MethodInsnNode methodInsn = (MethodInsnNode) target;
                            if (!methodInsn.owner.equals(EMPIRE_CLASS) || !methodInsn.desc.equals("(Ljava/lang/String;)V")) {
                                throw new OutdatedDeobfuscatorException("Star", EMPIRE_CLASS, "setMotto", "Unexpected owner or descriptor");
                            }
                            if (setMottoMethod != null) {
                                throw new OutdatedDeobfuscatorException("Star", EMPIRE_CLASS, "setMotto", "Collision");
                            }
                            setMottoMethod = methodInsn.name;

                            target = ldcInsn.getPrevious().getPrevious();
                            while (target.getOpcode() == -1) {
                                target = target.getPrevious();
                            }
                            if (target.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                                throw new OutdatedDeobfuscatorException("Star", EMPIRE_CLASS, "resetSpecials", "Unexpected opcode");
                            }
                            methodInsn = (MethodInsnNode) target;
                            if (!methodInsn.owner.equals(EMPIRE_CLASS) || !methodInsn.desc.equals("()V")) {
                                throw new OutdatedDeobfuscatorException("Star", EMPIRE_CLASS, "resetSpecials", "Unexpected owner or descriptor (it is: " + new MethodReference(methodInsn) + ')');
                            }
                            if (resetSpecialsMethod != null) {
                                throw new OutdatedDeobfuscatorException("Star", EMPIRE_CLASS, "resetSpecials", "Collision");
                            }
                            resetSpecialsMethod = methodInsn.name;
                        }
                    } else if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        MethodInsnNode methodInsn = (MethodInsnNode) insn;
                        if (methodInsn.name.equals(quadtreeInsert) && methodInsn.desc.equals("(L" + STAR_CLASS + ";)Z") && methodInsn.owner.equals(quadTreeClass.name)) {
                            MethodInsnNode target = getNext(methodInsn, Opcodes.INVOKEVIRTUAL);
                            if (!target.owner.equals(EMPLOYMENT_AGENCY_CLASS)) {
                                throw new OutdatedDeobfuscatorException("Star", EMPLOYMENT_AGENCY_CLASS, "registerEmployer", "Unexpected owner");
                            } else if (!target.desc.equals("(L" + EMPLOYER_CLASS + ";)V")) {
                                throw new OutdatedDeobfuscatorException("Star", EMPLOYMENT_AGENCY_CLASS, "registerEmployer", "Unexpected descriptor");
                            } else if (registerEmployerMethod != null) {
                                throw new OutdatedDeobfuscatorException("Star", EMPLOYMENT_AGENCY_CLASS, "registerEmployer", "Collision");
                            } else {
                                registerEmployerMethod = method.name;
                            }
                            AbstractInsnNode previous = methodInsn.getPrevious().getPrevious();
                            if (previous.getOpcode() != Opcodes.GETSTATIC) {
                                throw new OutdatedDeobfuscatorException("Star", SPACE_CLASS, "starsQuadTree", "Unexpected opcode");
                            }
                            FieldInsnNode fieldInsn = (FieldInsnNode) previous;
                            if (!fieldInsn.owner.equals(SPACE_CLASS) || !fieldInsn.desc.equals("L" + quadTreeClass.name + ";")) {
                                throw new OutdatedDeobfuscatorException("Star", SPACE_CLASS, "starsQuadTree", "Unexpected owner or descriptor");
                            }
                            if (quadTreeField != null) {
                                throw new OutdatedDeobfuscatorException("Star", SPACE_CLASS, "starsQuadTree", "Collision");
                            }
                            quadTreeField = fieldInsn.name;
                        }
                    }
                }
            } else if (method.desc.equals("(L" + STAR_CLASS + ";L" + STAR_CLASS + ";)V")) {
                boolean disconnect = false;
                boolean connect = false;
                for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        MethodInsnNode methodInsn = (MethodInsnNode) insn;
                        if (methodInsn.owner.equals(STAR_CLASS) && methodInsn.desc.equals("(L" + STAR_CLASS + ";)V")) {
                            if (methodInsn.name.equals(connectMethod)) {
                                connect = true;
                            } else if (methodInsn.name.equals(disconnectMethod)) {
                                disconnect = true;
                            }
                        }
                    }
                }

                if (connect && !disconnect) {
                    if (spaceConnectStarsMethod != null) {
                        throw new OutdatedDeobfuscatorException("Star", SPACE_CLASS, "connectStars", "Collision");
                    }
                    spaceConnectStarsMethod = method.name;
                } else if (!connect && disconnect) {
                    if (spaceDisconnectStarsMethod != null) {
                        throw new OutdatedDeobfuscatorException("Star", SPACE_CLASS, "disconnectStars", "Collision");
                    }
                    spaceDisconnectStarsMethod = method.name;
                }
            } else if (method.desc.equals("()V")) {
                for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn.getOpcode() == Opcodes.LDC) {
                        LdcInsnNode ldcInsn = (LdcInsnNode) insn;
                        if (ldcInsn.cst.equals("-> Edges")) {
                            AbstractInsnNode previous = ldcInsn.getPrevious();
                            while (previous.getOpcode() == -1) {
                                previous = previous.getPrevious();
                            }
                            if (previous.getOpcode() != Opcodes.INVOKESTATIC) {
                                dumpMethod(method);
                                throw new OutdatedDeobfuscatorException("Star", SPACE_CLASS, "restoreQuadtree", "Unexpected opcode - it is a " + previous.getClass().getName() + " Opcode " + previous.getOpcode());
                            }
                            MethodInsnNode methodInsn = (MethodInsnNode) previous;
                            if (!methodInsn.owner.equals(SPACE_CLASS) || !methodInsn.desc.equals("()V")) {
                                throw new OutdatedDeobfuscatorException("Star", SPACE_CLASS, "restoreQuadtree", "Unexpected owner or descriptor");
                            }
                            boolean isValidTarget = false;
                            for (MethodNode method2 : spaceNode.methods) {
                                if (!method2.desc.equals("()V") || !method2.name.equals(methodInsn.name)) {
                                    continue;
                                }
                                for (AbstractInsnNode insn2 = method2.instructions.getFirst(); insn2 != null; insn2 = insn2.getNext()) {
                                    if (insn2.getOpcode() == Opcodes.NEW) {
                                        TypeInsnNode typeInsn = (TypeInsnNode) insn2;
                                        if (typeInsn.desc.equals(quadTreeClass.name)) {
                                            isValidTarget = true;
                                            break;
                                        }
                                    }
                                }
                                break;
                            }
                            if (!isValidTarget) {
                                throw new OutdatedDeobfuscatorException("Star", SPACE_CLASS, "restoreQuadtree", "Nonsensical contents");
                            }
                            if (restoreQuadTreeMethod != null) {
                                throw new OutdatedDeobfuscatorException("Star", SPACE_CLASS, "restoreQuadtree", "Collision");
                            }
                            restoreQuadTreeMethod = methodInsn.name;
                            break;
                        }
                    }
                }
            }
        }

        if (buildPairsMethod == null) {
            throw new OutdatedDeobfuscatorException("Star", QUAD_TREE_CLASS, "buildPairs", "Unresolved");
        }
        if (regenerateVoronoiCellsMethod == null) {
            throw new OutdatedDeobfuscatorException("Star", SPACE_CLASS, "regenerateVoronoiCells", "Unresolved");
        }
        if (setMottoMethod == null) {
            throw new OutdatedDeobfuscatorException("Star", EMPIRE_CLASS, "setMotto", "Unresolved");
        }
        if (resetSpecialsMethod == null) {
            throw new OutdatedDeobfuscatorException("Star", EMPIRE_CLASS, "resetSpecials", "Unresolved");
        }
        if (registerEmployerMethod == null) {
            throw new OutdatedDeobfuscatorException("Star", EMPLOYMENT_AGENCY_CLASS, "registerEmployer", "Unresolved");
        }
        if (quadTreeField == null) {
            throw new OutdatedDeobfuscatorException("Star", SPACE_CLASS, "starsQuadTree", "Unresolved");
        }
        if (spaceConnectStarsMethod == null) {
            throw new OutdatedDeobfuscatorException("Star", SPACE_CLASS, "connectStars", "Unresolved");
        }
        if (spaceDisconnectStarsMethod == null) {
            throw new OutdatedDeobfuscatorException("Star", SPACE_CLASS, "disconnectStars", "Unresolved");
        }
        if (restoreQuadTreeMethod == null) {
            throw new OutdatedDeobfuscatorException("Star", SPACE_CLASS, "restoreQuadtree", "Unresolved");
        }

        remapMethod(mappingsStream, quadTreeClass.name, buildPairsMethod, "buildPairs", "()V");
        remapMethod(mappingsStream, SPACE_CLASS, regenerateVoronoiCellsMethod, "regenerateVoronoiCells", "()V");
        remapMethod(mappingsStream, EMPIRE_CLASS, setMottoMethod, "setMotto", "(Ljava/lang/String;)V");
        remapMethod(mappingsStream, EMPIRE_CLASS, resetSpecialsMethod, "resetSpecials", "()V");
        remapMethod(mappingsStream, EMPLOYMENT_AGENCY_CLASS, registerEmployerMethod, "registerEmployer", "(L" + EMPLOYER_CLASS + ";)V");
        remapField(mappingsStream, SPACE_CLASS, quadTreeField, "starsQuadTree", "L" + quadTreeClass.name + ";");
        remapMethod(mappingsStream, SPACE_CLASS, spaceConnectStarsMethod, "connectStars", "(L" + STAR_CLASS + ";L" + STAR_CLASS + ";)V");
        remapMethod(mappingsStream, SPACE_CLASS, spaceDisconnectStarsMethod, "disconnectStars", "(L" + STAR_CLASS + ";L" + STAR_CLASS + ";)V");
        remapMethod(mappingsStream, SPACE_CLASS, restoreQuadTreeMethod, "restoreQuadtree", "()V");

        String naiveRestoreQuadTreeMethod = null;

        methodLoop:
        for (MethodNode method : spaceNode.methods) {
            if (method.desc.equals("()V")) {
                for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn.getOpcode() == Opcodes.PUTSTATIC) {
                        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                        if (fieldInsn.owner.equals(SPACE_CLASS) && fieldInsn.name.equals(quadTreeField) && fieldInsn.desc.equals("L" + quadTreeClass.name +";")) {
                            if (method.name.equals(restoreQuadTreeMethod)) {
                                // -> "paranoid" variant, not of relevance
                                // For those wondering: The difference between the naive and the paranoid variant is that the
                                // paranoid variant infers the map size from the star count and the aspect ratio, where as
                                // the naive variant obtains the size from the X/Y size cache.
                                continue methodLoop;
                            } else if (naiveRestoreQuadTreeMethod != null) {
                                throw new OutdatedDeobfuscatorException("Space", SPACE_CLASS, "naiveRestoreQuadtree", "Collision");
                            } else {
                                naiveRestoreQuadTreeMethod = method.name;
                            }
                        }
                    }
                }
            }
        }

        if (naiveRestoreQuadTreeMethod == null) {
            throw new OutdatedDeobfuscatorException("Space", SPACE_CLASS, "naiveRestoreQuadtree", "Unresolved");
        }

        remapMethod(mappingsStream, SPACE_CLASS, naiveRestoreQuadTreeMethod, "naiveRestoreQuadtree", "()V");
    }

    /**
     * Remaps the characteristic "this$0" field that can be found in anonymous inner classes.
     * Also remaps anonymous classes to the more characteristic naming scheme of anonymous classes
     *
     * @param remapper The remapper to use for remapping
     * @deprecated Not used, only dumped here for future use. Does work however
     */
    @Deprecated
    public void remapThisZero(Remapper remapper) {
        Map<String, String> innerClasses = new HashMap<>();
        for (ClassNode node : nodes) {
            for (InnerClassNode icn : node.innerClasses) {
                if (icn.innerName == null && icn.name.equals(node.name)) {
                    if (innerClasses.containsKey(icn.name)) {
                        innerClasses.put(icn.name, null);
                    } else {
                        innerClasses.put(icn.name, icn.outerName);
                    }
                    break;
                }
            }
        }
        for (ClassNode node : nodes) {
            String outerName = innerClasses.get(node.name);
            if (outerName == null) {
                continue;
            }

            int lastSlash = node.name.lastIndexOf('/');
            int lastDollar = node.name.lastIndexOf('$', lastSlash);
            if (lastDollar == -1) {
                int indexOfUnderscore = node.name.indexOf('_', lastSlash);
                if (node.name.indexOf('_', indexOfUnderscore) != -1) {
                    remapper.remapClassName(node.name, outerName + '$' + node.name.substring(indexOfUnderscore + 1));
                }
            }

            FieldNode fieldNode = null;
            boolean imminentCollision = false;
            for (FieldNode field : node.fields) {
                if (field.name.equals("this$0")) {
                    imminentCollision = true;
                }
                if ((field.access & Opcodes.ACC_SYNTHETIC) == 0) {
                    continue;
                }
                if ((field.desc.length() == outerName.length() + 2) && field.desc.startsWith(outerName, 1)) {
                    if (fieldNode != null) {
                        throw new IllegalStateException("Two similar nodes found");
                    }
                    fieldNode = field;
                }
            }
            if (fieldNode == null) {
                continue; // Should we remove the InnerClassNode?
            }
            if (imminentCollision) {
                if (!fieldNode.name.equals("this$0")) {
                    throw new IllegalStateException("Field node has strange name: " + node.name + " " + fieldNode.name + fieldNode.desc);
                }
            } else {
                remapper.remapField(node.name, fieldNode.desc, fieldNode.name, "this$0");
            }
        }
    }

    /**
     * Needs to be called after {@link #remapSpaceFields(Writer)}.
     */
    public void remapUIClasses(Writer mappingsStream) throws IOException {
        String textInputDialogWidget = this.textInputDialogWidgetClass;

        if (textInputDialogWidget == null) {
            throw new IllegalStateException("This method (remapUIClasses) has to be invoked after remapSpaceFields as some deobfuscation subroutines "
                    + "depend on the output of the remapSpaceFields deobfuscation run.");
        }

        String setTimelapseModifierMethod = null;
        String galemulatorClass = null;

        for (ClassNode node : nodes) {
            if (!node.name.startsWith("com/example/Main$")) {
                continue;
            }
            for (MethodNode method : node.methods) {
                if (!method.name.equals("checkAndDoStuff")) {
                    continue;
                }
                boolean isTimelapseModifierHotkey = false;
                for (AbstractInsnNode insn : method.instructions) {
                    if (insn instanceof LdcInsnNode) {
                        LdcInsnNode ldc = (LdcInsnNode) insn;
                        if (ldc.cst.equals("New speed is: ")) {
                            isTimelapseModifierHotkey = true;
                        }
                    } else if (isTimelapseModifierHotkey && insn.getOpcode() == Opcodes.INVOKESTATIC) {
                        MethodInsnNode minsn = (MethodInsnNode) insn;
                        if (!minsn.desc.equals("(I)V")) {
                            continue;
                        }
                        if (setTimelapseModifierMethod != null) {
                            throw new OutdatedDeobfuscatorException("UI", "Galemulator", "setTimelapseModifier", "Collision");
                        }
                        setTimelapseModifierMethod = minsn.name;
                        galemulatorClass = minsn.owner;
                        remapClass(mappingsStream, galemulatorClass, "snoddasmannen/galimulator/Galemulator");
                        remapMethod(mappingsStream, galemulatorClass, setTimelapseModifierMethod, "setTimelapseModifier", "(I)V");
                    }
                }
            }
        }

        if (setTimelapseModifierMethod == null) {
            throw new OutdatedDeobfuscatorException("UI", "Galemulator", "setTimelapseModifier");
        }
        if (galemulatorClass == null) {
            throw new OutdatedDeobfuscatorException("UI", "Galemulator", "*");
        }

        String widgetDrawMethod = null;
        String widgetRefreshLayoutMethod = null;
        String bufferedWidgetWrapperClass = null;
        String sidebarClass = null;
        String newDynastyWidgetClass = null;
        String galimulatorGestureListener = null;
        String settingsDialogClass = null;
        String selectActorMethod = null;
        String galaxyPreviewClass = null;
        String oddityBulletinClass = null;
        String bulletinDrawAtMethod = null;
        String textBulletinClass = null;
        String abstractBulletinClass = null;
        MethodNode sidebarInitializeMethod = null;

        for (ClassNode node : nodes) {
            boolean isGalemulator = false;
            boolean isSidebar = false;
            boolean isNewDynastyWidget = false;
            boolean isGestureListener = false;
            if (node.interfaces.size() == 1
                    && node.interfaces.get(0).equals("com/badlogic/gdx/ApplicationListener")
                    && node.name.startsWith(BASE_PACKAGE)) {
                // I know that some class is called "Galemulator" (sic) as snoddasmannen disclosed that on the discord.
                // I am pretty sure that the application listener is that class.
                // If the Space class weren't deobfuscated, I would think that the Space class would be called like that -
                // in old Starmap Space was actually called "Galimulator". So as there is precedence of me guessing names wrongly,
                // which might also apply here.

                // really, it would be an interesting experience should galimulator drop all obfuscation.
                // At this point we use the deobfuscated names too much that adapting to the actual names
                // may be a bit hard to do
                isGalemulator = true;
                if (!galemulatorClass.equals(node.name)) {
                    throw new OutdatedDeobfuscatorException("UI", "Galemulator", "*", "Collision. Previous analysis: " + galemulatorClass + ". This method: " + node.name);
                }
            } else if (node.interfaces.size() == 1
                    && node.interfaces.get(0).equals(GDX_GESTURE_LISTENER_CLASS)
                    && node.name.startsWith(BASE_PACKAGE)) {
                if (galimulatorGestureListener != null) {
                    throw new OutdatedDeobfuscatorException("GestureListener", "GalimulatorGestureListener", "*", "Multiple candidates found");
                }
                galimulatorGestureListener = node.name;
                remapClass(mappingsStream, node.name, BASE_PACKAGE + "GalimulatorGestureListener");
                isGestureListener = true;
            } else if (node.superName.equals(WIDGET_CLASS)) {
                boolean doubleClearBuffer = false;
                boolean openWindow = false;
                for (MethodNode method : node.methods) {
                    if (!method.desc.equals("()V")) {
                        continue;
                    }
                    for (AbstractInsnNode insn : method.instructions) {
                        if (insn.getOpcode() == Opcodes.LDC) {
                            LdcInsnNode ldcInsn = (LdcInsnNode) insn;
                            if (ldcInsn.cst.equals("Attempted double clear of buffer: ")) {
                                doubleClearBuffer = true;
                                break;
                            } else if (ldcInsn.cst.equals("Attempted double clear of buffer in draw(): ")) {
                                widgetDrawMethod = method.name;
                                openWindow = true;
                                break;
                            } else if (ldcInsn.cst.equals("settingsbutton.png") && node.name.startsWith(UI_PACKAGE)) {
                                sidebarInitializeMethod = method;
                                isSidebar = true;
                                break;
                            } else if (ldcInsn.cst.equals("Create a new Dynasty") && method.name.equals("<init>") && node.name.startsWith(UI_PACKAGE)) {
                                if (newDynastyWidgetClass != null) {
                                    throw new OutdatedDeobfuscatorException("Widget", "Multiple candidates for NewDynastyWidget found!");
                                }
                                newDynastyWidgetClass = node.name;
                                isNewDynastyWidget = true;
                                break;
                            } else if (ldcInsn.cst.equals("Preview not supported for this galaxy type :(")) {
                                if (galaxyPreviewClass != null) {
                                    throw new OutdatedDeobfuscatorException("UI", GALAXY_PREVIEW_WIDGET_CLASS, "*", "Collision");
                                }
                                galaxyPreviewClass = node.name;
                                remapClass(mappingsStream, galaxyPreviewClass, GALAXY_PREVIEW_WIDGET_CLASS);
                                break;
                            }
                        }
                    }
                }
                if (doubleClearBuffer != openWindow) {
                    throw new IllegalStateException("Found insufficent non-zero evidence of BufferedWidgetWrapper instance.");
                }
                if (doubleClearBuffer && openWindow) {
                    if (bufferedWidgetWrapperClass != null) {
                        throw new IllegalStateException("The deobfuscator for the BufferedWidgetWrapper class must be updated (suspecting multiple classes to be BufferedWidgetWrapper)");
                    }
                    remapClass(mappingsStream, node.name, "snoddasmannen/galimulator/ui/BufferedWidgetWrapper");
                    bufferedWidgetWrapperClass = node.name;
                    continue;
                }
            } else if ((node.access & Opcodes.ACC_INTERFACE) == 0 && node.interfaces.size() == 1) {
                for (MethodNode method : node.methods) {
                    if (method.name.equals("getTitle")) {
                        AbstractInsnNode insn = getNext(method.instructions.getFirst());
                        if (insn.getOpcode() == Opcodes.LDC) {
                            LdcInsnNode ldcInsn = (LdcInsnNode) insn;
                            if (ldcInsn.cst.equals("Settings")) {
                                if (settingsDialogClass != null) {
                                    throw new OutdatedDeobfuscatorException("Dialog", "SettingsDialog", "*", "Collision");
                                }
                                settingsDialogClass = node.name;
                            }
                        }
                    }
                }
                continue;
            } else if (node.name.equals(LOCATION_SELECTED_EFFECT_CLASS)) {
                for (MethodNode method : node.methods) {
                    if (method.name.equals("draw") && method.desc.equals("()V")) {
                        AbstractInsnNode insn = method.instructions.getFirst();
                        String projectMethod = null;
                        while (insn != null) {
                            if (insn.getOpcode() == Opcodes.INVOKESTATIC) {
                                MethodInsnNode methodInsn = (MethodInsnNode) insn;
                                if (methodInsn.owner.equals(GALFX_CLASS)) {
                                    if (methodInsn.desc.equals("(Lcom/badlogic/gdx/math/Vector3;)V")) {
                                        if (projectMethod != null) {
                                            throw new OutdatedDeobfuscatorException("GalFX", GALFX_CLASS, "projectBoardToScreen", "Collision");
                                        }
                                        projectMethod = methodInsn.name;
                                    }
                                }
                            }
                            insn = insn.getNext();
                        }
                        if (projectMethod == null) {
                            throw new OutdatedDeobfuscatorException("GalFX", GALFX_CLASS, "projectBoardToScreen", "Not found");
                        }
                        remapMethod(mappingsStream, GALFX_CLASS, projectMethod, "projectBoardToScreen", "(Lcom/badlogic/gdx/math/Vector3;)V");
                        break;
                    }
                }
                continue;
            } else if (node.methods.size() == 2 && node.fields.size() == 3) {
                for (MethodNode method : node.methods) {
                    if (!method.desc.equals("(FF)V")) {
                        continue;
                    }
                    AbstractInsnNode insn = method.instructions.getFirst();
                    while (insn != null) {
                        if (insn.getOpcode() == Opcodes.LDC && ((LdcInsnNode) insn).cst.equals("Space oddity!")) {
                            break;
                        }
                        insn = insn.getNext();
                    }
                    if (insn == null) {
                        break;
                    }
                    if (oddityBulletinClass != null) {
                        throw new OutdatedDeobfuscatorException("GalFX", ODDITY_BULLETIN_CLASS, "*", "Collision");
                    }
                    oddityBulletinClass = node.name;
                    textBulletinClass = node.superName;
                    abstractBulletinClass = name2Node.get(textBulletinClass).superName;
                    bulletinDrawAtMethod = method.name;
                    insn = insn.getNext().getNext().getNext();
                    if (insn.getOpcode() != Opcodes.INVOKESTATIC) {
                        throw new OutdatedDeobfuscatorException("GalFX", GALFX_CLASS, "drawText", "Invalid follow-up instruction");
                    }
                    MethodInsnNode drawTextInsn = (MethodInsnNode) insn;
                    if (!drawTextInsn.owner.equals(GALFX_CLASS) || !drawTextInsn.desc.equals("(FFLjava/lang/String;Lsnoddasmannen/galimulator/GalColor;Lsnoddasmannen/galimulator/GalFX$FONT_TYPE;)F")) {
                        throw new OutdatedDeobfuscatorException("GalFX", GALFX_CLASS, "drawText", "Owner or descriptor mismatch");
                    }
                    remapMethod(mappingsStream, GALFX_CLASS, drawTextInsn.name, "drawText", "(FFLjava/lang/String;Lsnoddasmannen/galimulator/GalColor;Lsnoddasmannen/galimulator/GalFX$FONT_TYPE;)F");
                }
                continue;
            } else {
                continue;
            }

            if (isGalemulator) {
                MethodNode renderMethod = null;
                MethodNode setTimelapseModifierMethodNode = null;

                for (MethodNode method : node.methods) {
                    if (method.name.equals("render") && method.desc.equals("()V")) {
                        renderMethod = method;
                    } else if (method.name.equals(setTimelapseModifierMethod) && method.desc.equals("(I)V")) {
                        setTimelapseModifierMethodNode = method;
                    }
                }
                if (renderMethod == null) {
                    throw new IllegalStateException("The deobfuscator for the Galemulator class is out of date (render method not found)");
                }
                if (setTimelapseModifierMethodNode == null) {
                    throw new OutdatedDeobfuscatorException("UI", "Galemulator", "setTimelapseSpeed", "Node cannot be resolved");
                }

                String timelapseModifierField = null;

                for (AbstractInsnNode insn : setTimelapseModifierMethodNode.instructions) {
                    if (insn.getOpcode() != Opcodes.PUTSTATIC) {
                        continue;
                    }
                    if (timelapseModifierField != null) {
                        throw new OutdatedDeobfuscatorException("UI", "Galemulator", "timelapseModifier", "Collison");
                    }
                    FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                    timelapseModifierField = fieldInsn.name;
                    if (!fieldInsn.desc.equals("I")) {
                        throw new OutdatedDeobfuscatorException("UI", "Galemulator", "timelapseModifer", "Descriptor mismatch");
                    }
                }

                if (timelapseModifierField == null) {
                    throw new IllegalStateException("The deobfuscator for the Galemulator class is out of date (No timelapseModifier field found)");
                }

                boolean firstLdcDrawPassed = false;
                boolean secondLdcDrawPassed = false;
                boolean foundSpaceDrawMethod = false;
                boolean foundSpaceRecomputeVisibleWidgets = false;

                for (AbstractInsnNode insn : renderMethod.instructions) {
                    if (insn instanceof LdcInsnNode) {
                        LdcInsnNode ldcInsn = (LdcInsnNode) insn;
                        if (ldcInsn.cst.equals("Draw")) {
                            if (firstLdcDrawPassed) {
                                if (secondLdcDrawPassed) {
                                    throw new IllegalStateException("The deobfuscator for the Galemulator class is out of date (three or more ldc draw present)");
                                }
                                AbstractInsnNode iteratedInsn = insn.getNext();
                                while (iteratedInsn.getOpcode() != Opcodes.INVOKESTATIC) {
                                    iteratedInsn = iteratedInsn.getNext();
                                }
                                MethodInsnNode methodInsn = (MethodInsnNode) iteratedInsn;
                                if (!methodInsn.owner.equals(DEBUG_CLASS) || !methodInsn.desc.equals("(Ljava/lang/String;)I")) {
                                    throw new IllegalStateException("The deobfuscator for the Galemulator class is out of date (debug end call not present)");
                                }
                                remapMethod(mappingsStream, methodInsn.owner, methodInsn.name, "endDebuggingSection", methodInsn.desc);
                                secondLdcDrawPassed = true;
                            } else {
                                AbstractInsnNode iteratedInsn = insn.getNext();
                                while (iteratedInsn.getOpcode() != Opcodes.INVOKESTATIC) {
                                    iteratedInsn = iteratedInsn.getNext();
                                }
                                MethodInsnNode methodInsn = (MethodInsnNode) iteratedInsn;
                                if (!methodInsn.owner.equals(DEBUG_CLASS) || !methodInsn.desc.equals("(Ljava/lang/String;Z)V")) {
                                    throw new IllegalStateException("The deobfuscator for the Galemulator class is out of date (debug start call not present)");
                                }
                                remapMethod(mappingsStream, methodInsn.owner, methodInsn.name, "startDebuggingSection", methodInsn.desc);
                                firstLdcDrawPassed = true;
                            }
                        }
                    } else if (insn.getOpcode() == Opcodes.INVOKESTATIC) {
                        MethodInsnNode methodInsn = (MethodInsnNode) insn;
                        if (firstLdcDrawPassed && !secondLdcDrawPassed && methodInsn.owner.equals(SPACE_CLASS)) {
                            if (methodInsn.desc.equals("()V")) {
                                if (foundSpaceRecomputeVisibleWidgets) {
                                    throw new IllegalStateException("The deobfuscator for the Galemulator class is out of date (multiple Space#recomputeVisibleWidget methods found). New one: " + methodInsn.name + methodInsn.desc);
                                }
                                remapMethod(mappingsStream, SPACE_CLASS, methodInsn.name, "recomputeVisibleWidgets", methodInsn.desc);
                                foundSpaceRecomputeVisibleWidgets = true;
                            } else {
                                // Descriptor should be something along the lines of (Lsnoddasmannen/galimulator/rendersystem/class_3;)V
                                if (foundSpaceDrawMethod) {
                                    throw new IllegalStateException("The deobfuscator for the Galemulator class is out of date (multiple Space#draw methods found). New one: " + methodInsn.name + methodInsn.desc);
                                }
                                foundSpaceDrawMethod = true;
                                remapMethod(mappingsStream, SPACE_CLASS, methodInsn.name, "draw", methodInsn.desc);
                            }
                        }
                    }
                }

                if (!firstLdcDrawPassed) {
                    throw new IllegalStateException("The deobfuscator for the Galemulator class is out of date (ldc draw not found)");
                }
                if (!foundSpaceDrawMethod || !foundSpaceRecomputeVisibleWidgets) {
                    throw new OutdatedDeobfuscatorException("UI", "One of the draw method block methods couldn't vbe resolved.");
                }

                boolean foundTimelapseSpeedGetter = false;
                boolean foundTimelapseSpeedSetter = false;
                for (MethodNode method : node.methods) {
                    if (method.desc.equals("()I")) {
                        AbstractInsnNode insn = method.instructions.getLast().getPrevious();
                        while (insn.getOpcode() == -1) {
                            insn = insn.getPrevious();
                        }
                        if (insn.getOpcode() == Opcodes.GETSTATIC) {
                            FieldInsnNode getStaticInsn = (FieldInsnNode) insn;
                            if (getStaticInsn.desc.equals("I") && getStaticInsn.owner.equals(node.name)
                                    && getStaticInsn.name.equals(timelapseModifierField)) {
                                if (foundTimelapseSpeedGetter) {
                                    throw new IllegalStateException("The deobfuscator for the Galemulator class is out of date (Found multiple getTimelapseModifier methods)");
                                }
                                foundTimelapseSpeedGetter = true;
                                remapMethod(mappingsStream, node.name, method.name, "getTimelapseModifier", "()I");
                            }
                        }
                    } else if (method.desc.equals("(I)V")) {
                        AbstractInsnNode insn = method.instructions.getFirst();
                        while (insn.getOpcode() == -1) {
                            // Filter out pseudo-opcodes
                            insn = insn.getNext();
                        }
                        if (insn.getOpcode() != Opcodes.ILOAD) {
                            continue;
                        }
                        insn = insn.getNext();
                        while (insn.getOpcode() == -1) {
                            insn = insn.getNext();
                        }
                        if (insn.getOpcode() == Opcodes.PUTSTATIC) {
                            FieldInsnNode getStaticInsn = (FieldInsnNode) insn;
                            if (getStaticInsn.desc.equals("I") && getStaticInsn.owner.equals(node.name)
                                    && getStaticInsn.name.equals(timelapseModifierField)) {
                                if (foundTimelapseSpeedSetter) {
                                    throw new IllegalStateException("The deobfuscator for the Galemulator class is out of date (Found multiple setTimelapseModifier methods)");
                                }
                                foundTimelapseSpeedSetter = true;
                                remapMethod(mappingsStream, node.name, method.name, "setTimelapseModifier", "(I)V");
                            }
                        }
                    }
                }
                if (!foundTimelapseSpeedGetter) {
                    throw new IllegalStateException("The deobfuscator for the Galemulator class is out of date (Found no getTimelapseModifier method)");
                }
                if (!foundTimelapseSpeedSetter) {
                    throw new IllegalStateException("The deobfuscator for the Galemulator class is out of date (Found no setTimelapseModifier method)");
                }
            } else if (isSidebar) {
                if (sidebarClass != null) {
                    throw new OutdatedDeobfuscatorException("Sidebar", "Multiple potential Sidebar classes detected.");
                }
                sidebarClass = node.name;
                remapClass(mappingsStream, sidebarClass, UI_PACKAGE + "SidebarWidget");
                String openGameControlMethod = null;
                for (MethodNode method : node.methods) {
                    if ((method.access & Opcodes.ACC_STATIC) == 0 || !method.desc.equals("()V")) {
                        continue;
                    }
                    AbstractInsnNode insn = method.instructions.getFirst();
                    while (insn != null) {
                        if (insn.getOpcode() == Opcodes.LDC && ((LdcInsnNode) insn).cst.equals("Game control")) {
                            if (openGameControlMethod != null) {
                                throw new OutdatedDeobfuscatorException("Sidebar", "SidebarWidget", "openGameControl", "Collision");
                            }
                            insn = insn.getNext();
                            if (insn.getOpcode() != Opcodes.LDC || !((LdcInsnNode) insn).cst.equals("Game control")) {
                                throw new OutdatedDeobfuscatorException("Sidebar", "SidebarWidget", "openGameControl", "Duplicate LDC instruction not duplicated.");
                            }
                            openGameControlMethod = method.name;
                            MethodInsnNode invokestatic = getNext(insn, Opcodes.INVOKESTATIC);
                            if (!invokestatic.owner.equals(SPACE_CLASS)) {
                                throw new OutdatedDeobfuscatorException("Sidebar", "Space", "openOptionChooser", "Wrong class");
                            }
                            String returnType = invokestatic.desc.substring(invokestatic.desc.lastIndexOf(')') + 1);
                            if (returnType.codePointAt(0) != 'L') {
                                throw new OutdatedDeobfuscatorException("Sidebar", "Space", "openOptionChooser", "Wrong return type");
                            }
                            remapMethod(mappingsStream, SPACE_CLASS, invokestatic.name, "openOptionChooser", invokestatic.desc);
                            String optionChooserWidgetClass = returnType.substring(1, returnType.length() - 1);
                            remapClass(mappingsStream, optionChooserWidgetClass, UI_PACKAGE + "OptionChooserWidget");
                            TypeInsnNode typeInsn = getNext(invokestatic, Opcodes.NEW);
                            if (!typeInsn.desc.startsWith(UI_PACKAGE)) {
                                throw new OutdatedDeobfuscatorException("Sidebar", "SidebarWidget$<anonymous class>", "*", "Package mismatch");
                            }
                            insn = typeInsn.getNext() // DUP
                                    .getNext() // INVOKESPECIAL
                                    .getNext(); // INVOKEVIRTUAL
                            if (insn.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                                throw new OutdatedDeobfuscatorException("Sidebar", "SidebarWidget$<anonymous class>", "*", "Opcode mismatch");
                            }
                            MethodInsnNode registerListenerInsn = (MethodInsnNode) insn;
                            if (!registerListenerInsn.owner.equals(optionChooserWidgetClass)) {
                                throw new OutdatedDeobfuscatorException("Sidebar", "OptionChooserWidget", "registerListener", "Owner mismatch");
                            }
                            remapMethod(mappingsStream, optionChooserWidgetClass, registerListenerInsn.name, "registerSelectionListener", registerListenerInsn.desc);
                            String listenerInterface = registerListenerInsn.desc.substring(2, registerListenerInsn.desc.length() - 3);
                            remapClass(mappingsStream, listenerInterface, UI_PACKAGE + "OptionSelectionListener");
                            ClassNode listenerImplNode = name2Node.get(typeInsn.desc);
                            if (listenerImplNode == null) {
                                throw new OutdatedDeobfuscatorException("Sidebar", "SidebarWidget$<anonymous class>", "*", "Missing as a node");
                            }
                            if (listenerImplNode.methods.size() != 2) {
                                throw new OutdatedDeobfuscatorException("Sidebare", "SidebarWidget$<anonymous class>", "*", "Too many (or too few) methods");
                            }
                            MethodNode listenerImplMethod = listenerImplNode.methods.get(0);
                            if (listenerImplMethod.name.equals("<init>")) {
                                listenerImplMethod = listenerImplNode.methods.get(1);
                            }

                            int hitCount = 0;
                            insn = getNext(listenerImplMethod.instructions.getFirst(), Opcodes.LDC);
                            while (insn != null) {
                                Object cst = ((LdcInsnNode) insn).cst;
                                if (!(cst instanceof String)) {
                                    insn = getNextOrNull(insn, Opcodes.LDC);
                                    continue;
                                }
                                switch ((String) cst) {
                                    case "Create a new galaxy": {
                                        hitCount++;
                                        MethodInsnNode createGalaxyInsn = getNext(insn, Opcodes.INVOKESTATIC);
                                        if (!createGalaxyInsn.owner.equals(SPACE_CLASS) || !createGalaxyInsn.desc.equals("()V")) {
                                            throw new OutdatedDeobfuscatorException("Sidebar", "Space", "showGalaxyCreationScreen", "Mismatching owner or descriptor.");
                                        }
                                        insn = createGalaxyInsn;
                                        remapMethod(mappingsStream, SPACE_CLASS, createGalaxyInsn.name, "showGalaxyCreationScreen", "()V");
                                        break;
                                    }
                                    case "Load autosave": {
                                        hitCount++;
                                        MethodInsnNode loadSaveMethod = getNext(insn, Opcodes.INVOKESTATIC);
                                        if (!loadSaveMethod.owner.equals(SPACE_CLASS) || !loadSaveMethod.desc.equals("(Ljava/lang/String;)V")) {
                                            throw new OutdatedDeobfuscatorException("Sidebar", "Space", "loadAsync", "Mismatching owner or descriptor.");
                                        }
                                        insn = loadSaveMethod;
                                        remapMethod(mappingsStream, SPACE_CLASS, loadSaveMethod.name, "loadAsync", "(Ljava/lang/String;)V");
                                        break;
                                    }
                                    case "Edit scenario": {
                                        hitCount++;
                                        MethodInsnNode openDialogMethod = getNext(getNext(insn, Opcodes.INVOKESTATIC), Opcodes.INVOKESTATIC);
                                        if (!openDialogMethod.owner.equals(SPACE_CLASS) || openDialogMethod.desc.codePointBefore(openDialogMethod.desc.length()) != 'V') {
                                            throw new OutdatedDeobfuscatorException("Sidebar", "Space", "showDialog", "Mismatching owner or descriptor.");
                                        }
                                        insn = openDialogMethod;
                                        remapMethod(mappingsStream, SPACE_CLASS, openDialogMethod.name, "showDialog", openDialogMethod.desc);
                                        break;
                                    }
                                    case "Online scenarios": {
                                        hitCount++;
                                        MethodInsnNode showScenarioBrowserInsn = getNext(insn, Opcodes.INVOKESTATIC);
                                        if (!showScenarioBrowserInsn.owner.equals(SPACE_CLASS) || !showScenarioBrowserInsn.desc.equals("()V")) {
                                            throw new OutdatedDeobfuscatorException("Sidebar", "Space", "showOnlineScenarioBrowser", "Mismatching owner or descriptor.");
                                        }
                                        insn = showScenarioBrowserInsn;
                                        remapMethod(mappingsStream, SPACE_CLASS, showScenarioBrowserInsn.name, "showOnlineScenarioBrowser", "()V");
                                        break;
                                    }
                                    case "Share a mod": {
                                        hitCount++;
                                        MethodInsnNode showWidgetInsn = getNext(insn, Opcodes.INVOKESTATIC);
                                        if (!showWidgetInsn.owner.equals(SPACE_CLASS) || !showWidgetInsn.desc.equals("(Ljava/lang/Class;)V")) {
                                            throw new OutdatedDeobfuscatorException("Sidebar", "Space", "showWidget", "Mismatching owner or descriptor.");
                                        }
                                        insn = showWidgetInsn;
                                        remapMethod(mappingsStream, SPACE_CLASS, showWidgetInsn.name, "showWidget", "(Ljava/lang/Class;)V");
                                        AbstractInsnNode previousInsn = showWidgetInsn.getPrevious();
                                        if (previousInsn.getOpcode() != Opcodes.LDC) {
                                            throw new OutdatedDeobfuscatorException("Sidebar", "ModUploadWidget", "*", "Opcode mismatch");
                                        }
                                        LdcInsnNode previousLdc = (LdcInsnNode) previousInsn;
                                        if (!(previousLdc.cst instanceof Type)) {
                                            throw new OutdatedDeobfuscatorException("Sidebar", "ModUploadWidget", "*", "Expected LDC TYPE, got " + previousLdc.cst.getClass().toString());
                                        }
                                        remapClass(mappingsStream, ((Type) previousLdc.cst).getInternalName(), UI_PACKAGE + "ModUploadWidget");
                                        break;
                                    }
                                }
                                insn = getNextOrNull(insn, Opcodes.LDC);
                            }
                            if (hitCount != 5) {
                                throw new OutdatedDeobfuscatorException("Sidebar", "SidebarWidget$<anonymous class>", "N/A", "hit count mismatch");
                            }
                            break;
                        }
                        insn = insn.getNext();
                    }
                }
                if (openGameControlMethod == null) {
                    throw new OutdatedDeobfuscatorException("Sidebar", "SidebarWidget", "openGameControl", "Absent");
                }
                remapMethod(mappingsStream, sidebarClass, openGameControlMethod, "openGameControl", "()V");
            } else if (isNewDynastyWidget) {
                String refreshLayoutImplMethod = null;
                for (MethodNode method : node.methods) {
                    if ((method.access & Opcodes.ACC_PRIVATE) == 0 || !method.desc.equals("()V")) {
                        continue;
                    }
                    for (AbstractInsnNode insn : method.instructions) {
                        if (insn.getOpcode() == Opcodes.LDC && ((LdcInsnNode) insn).cst.equals("Name: ")) {
                            if (refreshLayoutImplMethod != null) {
                                throw new OutdatedDeobfuscatorException("Widget", "Multiple candidates for NewDynastyWidget#refreshLayout0");
                            }
                            remapMethod(mappingsStream, node.name, method.name, "refreshLayout0", "()V");
                            refreshLayoutImplMethod = method.name;
                            break;
                        }
                    }
                }
                if (refreshLayoutImplMethod == null) {
                    throw new OutdatedDeobfuscatorException("Widget", "NewDynastyWidget", "refreshLayout0");
                }
                for (MethodNode method : node.methods) {
                    if ((method.access & Opcodes.ACC_PUBLIC) == 0 || !method.desc.equals("()V") || method.name.equals("<init>")) {
                        continue;
                    }
                    for (AbstractInsnNode insn : method.instructions) {
                        if (insn.getOpcode() == Opcodes.INVOKESPECIAL) {
                            MethodInsnNode methodInsn = (MethodInsnNode) insn;
                            if (methodInsn.name.equals(refreshLayoutImplMethod) && methodInsn.desc.equals("()V")) {
                                if (widgetRefreshLayoutMethod != null) {
                                    throw new OutdatedDeobfuscatorException("Widget", "Multiple candidates for Widget#refreshLayout");
                                }
                                widgetRefreshLayoutMethod = method.name;
                                break;
                            }
                        }
                    }
                }
                if (widgetRefreshLayoutMethod == null) {
                    throw new OutdatedDeobfuscatorException("Widget", "Widget", "refreshLayout");
                }
            } else if (isGestureListener) {
                MethodNode tapMethod = null;
                for (MethodNode method : node.methods) {
                    if (method.name.equals("tap") && method.desc.equals("(FFII)Z")) {
                        tapMethod = method;
                        break;
                    }
                }
                if (tapMethod == null) {
                    throw new OutdatedDeobfuscatorException("GestureListener", "GestureListener", "tap");
                }
                MethodInsnNode getTickLoopLockInsn = null;
                for (AbstractInsnNode insn : tapMethod.instructions) {
                    if (insn.getOpcode() == Opcodes.INVOKESTATIC) {
                        getTickLoopLockInsn = (MethodInsnNode) insn;
                        break;
                    }
                }
                if (getTickLoopLockInsn == null || !getTickLoopLockInsn.desc.equals("()Ljava/util/concurrent/Semaphore;") || !getTickLoopLockInsn.owner.equals(SPACE_CLASS)) {
                    throw new OutdatedDeobfuscatorException("GestureListener", SPACE_CLASS, "getMainTickLoopLock");
                }
                String mainTickLoopLockFieldName = null;
                ClassNode spaceNode = name2Node.get(SPACE_CLASS);
                for (MethodNode method : spaceNode.methods) {
                    if (method.name.equals(getTickLoopLockInsn.name) && method.desc.equals("()Ljava/util/concurrent/Semaphore;")) {
                        FieldInsnNode finsn = null;
                        AbstractInsnNode insn = method.instructions.getFirst();
                        while (insn != null) {
                            if (insn.getOpcode() == Opcodes.GETSTATIC) {
                                if (finsn != null) {
                                    throw new OutdatedDeobfuscatorException("GestureListener", SPACE_CLASS, "MAIN_TICK_LOOP_LOCK", "Collision");
                                }
                                finsn = (FieldInsnNode) insn;
                            }
                            insn = insn.getNext();
                        }
                        if (finsn == null) {
                            throw new OutdatedDeobfuscatorException("GestureListener", SPACE_CLASS, "MAIN_TICK_LOOP_LOCK", "GETSTATIC missing");
                        }
                        if (!finsn.desc.equals("Ljava/util/concurrent/Semaphore;")) {
                            throw new OutdatedDeobfuscatorException("GestureListener", SPACE_CLASS, "MAIN_TICK_LOOP_LOCK", "Invalid descriptor");
                        }
                        mainTickLoopLockFieldName = finsn.name;
                        break;
                    }
                }
                if (mainTickLoopLockFieldName == null) {
                    throw new OutdatedDeobfuscatorException("GestureListener", SPACE_CLASS, "MAIN_TICK_LOOP_LOCK", "Entirely missing");
                }
                remapMethod(mappingsStream, SPACE_CLASS, getTickLoopLockInsn.name, "getMainTickLoopLock", "()Ljava/util/concurrent/Semaphore;");
                remapField(mappingsStream, SPACE_CLASS, mainTickLoopLockFieldName, "MAIN_TICK_LOOP_LOCK", "Ljava/util/concurrent/Semaphore;");
                MethodInsnNode isShowingActorsMethod = null;
                for (AbstractInsnNode insn : tapMethod.instructions) {
                    if (insn.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                        continue;
                    }
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;
                    if (methodInsn.desc.equals("()Z") && methodInsn.owner.equals(MAP_MODE_ENUM_CLASS)) {
                        if (isShowingActorsMethod != null) {
                            throw new OutdatedDeobfuscatorException("GestureListener", MAP_MODE_ENUM_CLASS, "getShowsActors", "Collision");
                        }
                        isShowingActorsMethod = methodInsn;
                    }
                }
                if (isShowingActorsMethod == null) {
                    throw new OutdatedDeobfuscatorException("GestureListener", MAP_MODE_ENUM_CLASS, "getShowsActors", "Unresolved");
                }
                if (isShowingActorsMethod.getNext().getOpcode() != Opcodes.IFEQ) {
                    throw new OutdatedDeobfuscatorException("GestureListener", "Unexpected next opcode");
                }
                AbstractInsnNode nextInsn = isShowingActorsMethod.getNext().getNext();
                while (nextInsn != null && nextInsn.getOpcode() != Opcodes.INVOKESTATIC) {
                    nextInsn = nextInsn.getNext();
                }
                if (nextInsn == null) {
                    throw new OutdatedDeobfuscatorException("GestureListener", "Space", "getActorNear", "Missing instruction");
                }
                MethodInsnNode getActorNearMethodInsn = (MethodInsnNode) nextInsn;
                if (!getActorNearMethodInsn.owner.equals(SPACE_CLASS)
                        || !getActorNearMethodInsn.desc.equals("(FFL" + EMPIRE_CLASS + ";F)L" + ACTOR_CLASS + ";")) {
                    throw new OutdatedDeobfuscatorException("GestureListener", "Space", "findNearestActor", "Unexpected nature of the instruction");
                }
                remapMethod(mappingsStream, SPACE_CLASS, getActorNearMethodInsn.name, "findNearestActor", "(FFL" + EMPIRE_CLASS + ";F)L" + ACTOR_CLASS + ";");
                nextInsn = nextInsn.getNext();
                while (nextInsn != null && nextInsn.getOpcode() != Opcodes.INVOKESTATIC) {
                    nextInsn = nextInsn.getNext();
                }
                if (nextInsn == null) {
                    throw new OutdatedDeobfuscatorException("GestureListener", "Space", "selectActor", "Missing instruction");
                }
                MethodInsnNode actorSelectionInsn = (MethodInsnNode) nextInsn;
                if (!actorSelectionInsn.owner.equals(SPACE_CLASS)
                        || !actorSelectionInsn.desc.equals("(L" + ACTOR_CLASS + ";)V")) {
                    throw new OutdatedDeobfuscatorException("GestureListener", "Space", "selectActor", "Unexpected nature of the instruction");
                }
                selectActorMethod = actorSelectionInsn.name;
                remapMethod(mappingsStream, SPACE_CLASS, selectActorMethod, "selectActor", "(L" + ACTOR_CLASS + ";)V");
                nextInsn = nextInsn.getNext();
                while (nextInsn != null && (nextInsn.getOpcode() != Opcodes.GETSTATIC || !((FieldInsnNode) nextInsn).owner.equals(STAR_CLASS))) {
                    nextInsn = nextInsn.getNext();
                }
                if (nextInsn == null) {
                    throw new OutdatedDeobfuscatorException("GestureListener", "Star", "globalSizeFactor", "Missing instruction");
                }
                FieldInsnNode getSizeFactorInsn = (FieldInsnNode) nextInsn;
                if (!getSizeFactorInsn.desc.equals("F")) {
                    throw new OutdatedDeobfuscatorException("GestureListener", "Star", "globalSizeFactor", "Unexpected descriptor of field");
                }
                remapField(mappingsStream, STAR_CLASS, getSizeFactorInsn.name, "globalSizeFactor", "F");
                nextInsn = nextInsn.getNext();
                while (nextInsn != null && nextInsn.getOpcode() != Opcodes.INVOKESTATIC) {
                    nextInsn = nextInsn.getNext();
                }
                if (nextInsn == null) {
                    throw new OutdatedDeobfuscatorException("GestureListener", "Space", "findStarNear", "Instructions exhausted");
                }
                MethodInsnNode findStarNearInsn = (MethodInsnNode) nextInsn;
                if (!findStarNearInsn.owner.equals(SPACE_CLASS)
                        || !findStarNearInsn.desc.equals("(FFDL" + EMPIRE_CLASS + ";)L" + STAR_CLASS + ";")) {
                    throw new OutdatedDeobfuscatorException("GestureListener", "Space", "findStarNear", "Unexpected nature of the instruction");
                }
                remapMethod(mappingsStream, SPACE_CLASS, findStarNearInsn.name, "findStarNear", "(FFDL" + EMPIRE_CLASS + ";)L" + STAR_CLASS + ";");
            }
        }

        if (bufferedWidgetWrapperClass == null) {
            throw new IllegalStateException("Unable to find the BufferedWidgetWrapper class.");
        }
        if (newDynastyWidgetClass == null) {
            throw new OutdatedDeobfuscatorException("Widget", "NewDynastyWidget", "*");
        }
        if (galimulatorGestureListener == null) {
            throw new OutdatedDeobfuscatorException("GestureListener", "GalimulatorGestureListener", "*", "No candidates found");
        }
        if (settingsDialogClass == null) {
            throw new OutdatedDeobfuscatorException("Dialog", "SettingsDialog", "*", "No candidates found");
        }
        if (oddityBulletinClass == null) {
            throw new OutdatedDeobfuscatorException("UI", ODDITY_BULLETIN_CLASS, "*", "Not found");
        }
        if (textBulletinClass == null) {
            throw new OutdatedDeobfuscatorException("UI", TEXT_BULLETIN_CLASS, "*", "Not found");
        }
        if (abstractBulletinClass == null) {
            throw new OutdatedDeobfuscatorException("UI", ABSTRACT_BULLETIN_CLASS, "*", "Not found");
        }

        remapClass(mappingsStream, newDynastyWidgetClass, UI_PACKAGE + "NewDynastyWidget");
        remapClass(mappingsStream, oddityBulletinClass, ODDITY_BULLETIN_CLASS);
        remapClass(mappingsStream, textBulletinClass, TEXT_BULLETIN_CLASS);
        remapClass(mappingsStream, abstractBulletinClass, ABSTRACT_BULLETIN_CLASS);

        remapMethod(mappingsStream, oddityBulletinClass, bulletinDrawAtMethod, "drawAt", "(FF)V");
        remapMethod(mappingsStream, textBulletinClass, bulletinDrawAtMethod, "drawAt", "(FF)V");
        remapMethod(mappingsStream, abstractBulletinClass, bulletinDrawAtMethod, "drawAt", "(FF)V");

        String getWidgetWidthMethod = null;
        String widgetDrawHeaderMethod = null;
        String widgetHeaderColorField = null;
        String widgetHeaderTitleField = null;
        String fxDrawWindowMethod = null;
        String fxDrawTextMethod = null;
        String fxFullDrawTextureMethod = null;
        String widgetCameraField = null;
        String widgetOnDisposeMethod = null;
        String widgetHoverMethod = null;
        String closeNonPersistentWidgetsMethod = null;
        String ninepatchButtonClass = null;
        String shipConstructionWidgetClass = null;
        String basicButtonSetTextMethod = null;
        String basicButtonSetColorMethod = null;
        String empireBulletinClass = null;

        for (ClassNode node : nodes) {
            if (node.name.equals(SPACE_CLASS)) {
                boolean foundShowWidgetMethod = false;
                boolean foundTickMethod = false;
                for (MethodNode method : node.methods) {
                    if (method.desc.equals("()I")) {
                        if (method.name.equals(spaceLogicalTickMethodName)) {
                            AbstractInsnNode firstInsn = getNext(method.instructions.getFirst());
                            if (firstInsn.getOpcode() != Opcodes.GETSTATIC) {
                                throw new OutdatedDeobfuscatorException("UI", "Space", "tickCount", "Unexpected opcode");
                            }
                            FieldInsnNode fieldInsn = (FieldInsnNode) firstInsn;
                            if (!fieldInsn.desc.equals("I")) {
                                throw new OutdatedDeobfuscatorException("UI", "Space", "tickCount", "Unexpected descriptor");
                            }
                            remapField(mappingsStream, SPACE_CLASS, fieldInsn.name, "tickCount", "I");
                            foundTickMethod = true;
                        }
                        continue;
                    }
                    if (!method.desc.equals("(L" + WIDGET_CLASS + ";)V")) {
                        continue;
                    }

                    boolean wasShowWidgetMethod = false;
                    for (AbstractInsnNode insn : method.instructions) {
                        if (insn.getOpcode() == Opcodes.INVOKESPECIAL) {
                            MethodInsnNode methodInsn = (MethodInsnNode) insn;
                            if (methodInsn.name.equals("<init>") && methodInsn.owner.equals(bufferedWidgetWrapperClass)) {
                                if (foundShowWidgetMethod) {
                                    throw new IllegalStateException("The deobfuscator for the Space class is out of date (Suspecting multiple Space#showWidget methods!)");
                                }
                                foundShowWidgetMethod = true;
                                wasShowWidgetMethod = true;
                                remapMethod(mappingsStream, SPACE_CLASS, method.name, "showWidget", method.desc);
                                break;
                            }
                        }
                    }

                    if (wasShowWidgetMethod) {
                        AbstractInsnNode insn = method.instructions.getFirst();
                        MethodInsnNode invokeGetScreenWidth;
                        MethodInsnNode invokeGetWidthWidth;

                        while (insn.getOpcode() != Opcodes.INVOKESTATIC) {
                            insn = insn.getNext();
                        }

                        closeNonPersistentWidgetsMethod = ((MethodInsnNode) insn).name;
                        insn = insn.getNext();

                        while (insn.getOpcode() != Opcodes.INVOKESTATIC) {
                            insn = insn.getNext();
                        }

                        invokeGetScreenWidth = (MethodInsnNode) insn;

                        while (insn.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                            insn = insn.getNext();
                        }

                        invokeGetWidthWidth = (MethodInsnNode) insn;

                        if (invokeGetWidthWidth.desc.equals("()I") && invokeGetWidthWidth.owner.equals(WIDGET_CLASS)) {
                            if (getWidgetWidthMethod != null) {
                                throw new IllegalStateException("The deobfuscator for the Widget class is out of date (Unable to surely say which method is Widget#getWidth) (TRAP 1)");
                            }
                            getWidgetWidthMethod = invokeGetWidthWidth.name;
                        } else {
                            throw new IllegalStateException("The deobfuscator for the Widget class is out of date (Unable to surely say which method is Widget#getWidth) (TRAP 2)");
                        }

                        remapMethod(mappingsStream, GALFX_CLASS, invokeGetScreenWidth.name, "getScreenWidth", "()F");
                    }
                }
                if (!foundShowWidgetMethod) {
                    throw new IllegalStateException("The deobfuscator for the Space class is out of date (Found no Space#showWidget method)");
                }
                if (!foundTickMethod) {
                    throw new IllegalStateException("The deobfuscator for the Space class is out of date (Found no Space#tick method)");
                }
            } else if (node.name.equals(WIDGET_CLASS)) {
                for (MethodNode method : node.methods) {
                    if (method.desc.equals("()V")) {
                        boolean isDrawheaderMethod = false;
                        for (AbstractInsnNode insn : method.instructions) {
                            if (insn.getOpcode() == Opcodes.LDC) {
                                LdcInsnNode ldcInsn = (LdcInsnNode) insn;
                                if (ldcInsn.cst.equals("xbutton.png")) {
                                    if (widgetDrawHeaderMethod != null) {
                                        throw new IllegalStateException("The deobfuscator for the Widget class is out of date (Found multiple void-returning methods that make use of xbutton.png)");
                                    }
                                    isDrawheaderMethod = true;
                                    widgetDrawHeaderMethod = method.name;

                                    AbstractInsnNode nextInsn = ldcInsn.getNext();
                                    if (nextInsn.getOpcode() != Opcodes.INVOKESTATIC) {
                                        throw new IllegalStateException("The deobfuscator for the GalFX class is out of date (Unable to find GalFX#getTextureRegion)");
                                    }
                                    MethodInsnNode methodInsn = (MethodInsnNode) nextInsn;
                                    if (!methodInsn.owner.equals(GALFX_CLASS) || !methodInsn.desc.equals("(Ljava/lang/String;)Lcom/badlogic/gdx/graphics/g2d/TextureRegion;")) {
                                        throw new IllegalStateException("The deobfuscator for the GalFX class is out of date (Unable to find GalFX#getTextureRegion)");
                                    }
                                    remapMethod(mappingsStream, GALFX_CLASS, methodInsn.name, "getTextureRegion", "(Ljava/lang/String;)Lcom/badlogic/gdx/graphics/g2d/TextureRegion;");
                                    break;
                                }
                            }
                        }
                        if (!isDrawheaderMethod) {
                            continue;
                        }
                        String cameraField = null;
                        for (AbstractInsnNode insn : method.instructions) {
                            if (insn.getOpcode() == Opcodes.GETFIELD) {
                                FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                                if (!fieldInsn.owner.equals(WIDGET_CLASS)) {
                                    continue;
                                }
                                if (fieldInsn.desc.equals("Ljava/lang/String;")) {
                                    if (widgetHeaderTitleField != null) {
                                        throw new IllegalStateException("The deobfuscator for the widget class is out of date (multiple headerTitle fields suspected)");
                                    }
                                    widgetHeaderTitleField = fieldInsn.name;
                                } else if (fieldInsn.desc.equals("Lcom/badlogic/gdx/graphics/Camera;")) {
                                    if (cameraField != null && !cameraField.equals(fieldInsn.name)) {
                                        throw new IllegalStateException("The deobfuscator for the widget class is out of date (multiple internalCamera fields suspected)");
                                    }
                                    cameraField = fieldInsn.name;
                                } else if (fieldInsn.desc.equals("Lsnoddasmannen/galimulator/GalColor;")) {
                                    if (widgetHeaderColorField != null) {
                                        throw new IllegalStateException("The deobfuscator for the widget class is out of date (multiple headerColor fields suspected)");
                                    }
                                    widgetHeaderColorField = fieldInsn.name;
                                }
                            } else if (insn.getOpcode() == Opcodes.INVOKESTATIC) {
                                MethodInsnNode methodInsn = (MethodInsnNode) insn;
                                if (fxDrawWindowMethod == null) {
                                    if (!methodInsn.owner.equals(GALFX_CLASS)) {
                                        continue;
                                    }
                                    if (!methodInsn.desc.equals("(FFFFLsnoddasmannen/galimulator/GalColor;Lcom/badlogic/gdx/graphics/Camera;)V")) {
                                        continue;
                                    }
                                    fxDrawWindowMethod = methodInsn.name;
                                } else if (fxDrawTextMethod == null) {
                                    if (!methodInsn.owner.equals(GALFX_CLASS) || !methodInsn.desc.equals(GALFX_DRAW_TEXT_DESCRIPTOR)) {
                                        continue;
                                    }
                                    fxDrawTextMethod = methodInsn.name;
                                } else if (fxFullDrawTextureMethod == null) {
                                    if (!methodInsn.owner.equals(GALFX_CLASS) || !methodInsn.desc.equals(GALFX_DRAW_TEXTURE_DESCRIPTOR)) {
                                        continue;
                                    }
                                    fxFullDrawTextureMethod = methodInsn.name;
                                }
                            }
                        }

                        widgetCameraField = cameraField;
                        remapField(mappingsStream, WIDGET_CLASS, cameraField, "internalCamera", "Lcom/badlogic/gdx/graphics/Camera;");
                        remapField(mappingsStream, WIDGET_CLASS, widgetHeaderColorField, "headerColor", "Lsnoddasmannen/galimulator/GalColor;");
                        remapField(mappingsStream, WIDGET_CLASS, widgetHeaderTitleField, "headerTitle", "Ljava/lang/String;");

                        if (fxDrawWindowMethod == null) {
                            throw new IllegalStateException("The deobfuscator for the GalFX class is out of date! (Cannot resolve GalFX#drawWindow)");
                        }
                        if (fxDrawTextMethod == null) {
                            throw new OutdatedDeobfuscatorException("GalFX", "GalFX", "drawText");
                        }
                        if (fxFullDrawTextureMethod == null) {
                            throw new OutdatedDeobfuscatorException("GalFX", "GalFX", "drawTexture");
                        }
                        remapMethod(mappingsStream, GALFX_CLASS, fxDrawWindowMethod, "drawWindow", "(FFFFLsnoddasmannen/galimulator/GalColor;Lcom/badlogic/gdx/graphics/Camera;)V");
                        remapMethod(mappingsStream, GALFX_CLASS, fxDrawTextMethod, "drawText", GALFX_DRAW_TEXT_DESCRIPTOR);
                        remapMethod(mappingsStream, GALFX_CLASS, fxFullDrawTextureMethod, "drawTexture", GALFX_DRAW_TEXTURE_DESCRIPTOR);
                    }
                }
            } else if (node.name.equals(bufferedWidgetWrapperClass)) {
                methodLoop:
                for (MethodNode method : node.methods) {
                    if (method.desc.equals("()V")) {
                        AbstractInsnNode insn = method.instructions.getFirst();
                        while (insn != null) {
                            if (insn instanceof LdcInsnNode) {
                                LdcInsnNode ldcInsn = (LdcInsnNode) insn;
                                if (!ldcInsn.cst.equals("Attempted double clear of buffer: ")) {
                                    continue methodLoop;
                                }
                                if (widgetOnDisposeMethod != null) {
                                    throw new OutdatedDeobfuscatorException("Widget", "Widget", "onDispose", "Collision");
                                }
                                widgetOnDisposeMethod = method.name;
                            }
                            insn = insn.getNext();
                        }
                    }
                }
            } else if (node.interfaces.size() == 1 && node.interfaces.get(0).equals(GDX_INPUT_PROCESSOR_CLASS)) {
                boolean isAnonymousInputProcessor = false;
                for (MethodNode ctor : node.methods) {
                    if (ctor.name.equals("<init>") && ctor.desc.equals("(L" + galemulatorClass + ";)V")) {
                        isAnonymousInputProcessor = true;
                        break; // Short-circuit
                    }
                }

                if (isAnonymousInputProcessor) {
                    // Make class actually anonymous
                    remapClass(mappingsStream, node.name, GALEMULATOR_INPUT_PROCESSOR_CLASS);
                    node.outerClass = galemulatorClass;
                    node.outerMethod = "create";
                    node.outerMethodDesc = "()V";

                    boolean hasIcn = false;
                    for (InnerClassNode icn : node.innerClasses) {
                        if (icn.name.equals(node.name)) {
                            hasIcn = true;
                            break;
                        }
                    }
                    if (!hasIcn) {
                        InnerClassNode icn = new InnerClassNode(node.name, galemulatorClass, null, Opcodes.ACC_FINAL);
                        node.innerClasses.add(icn);
                        // We assume that the inner class nodes are valid both ways, so we do not bother to check the icns for
                        // duplicates in the galemulator class node
                        name2Node.get(galemulatorClass).innerClasses.add(icn);
                    }

                    // Decompilers act a bit strange if we do not remap that field
                    {
                        final String expectedDesc = 'L' + galemulatorClass + ';';
                        for (FieldNode field : node.fields) {
                            if (field.desc.equals(expectedDesc)) {
                                remapField(mappingsStream, node.name, field.name, "this$0", expectedDesc);
                                break;
                            }
                        }
                    }

                    // Actually deobf the contents of the methods
                    for (MethodNode method : node.methods) {
                        if (method.desc.equals("(II)Z") && method.name.equals("mouseMoved")) {
                            AbstractInsnNode insn = method.instructions.getFirst();
                            insn = getNext(insn, Opcodes.GETFIELD).getNext();
                            if (insn.getOpcode() != Opcodes.INVOKESTATIC) {
                                throw new OutdatedDeobfuscatorException("Widget", "Galemulator", "access$000", "Unexpected opcode");
                            }
                            MethodInsnNode invokeAccess000 = (MethodInsnNode) insn;
                            if (!invokeAccess000.owner.equals(galemulatorClass)) {
                                throw new OutdatedDeobfuscatorException("Widget", "Galemulator", "access$000", "Wrong owner");
                            }
                            remapMethod(mappingsStream, galemulatorClass, invokeAccess000.name, "access$000", invokeAccess000.desc);

                            FieldInsnNode getMouseMoveIdInsn = getNext(insn, Opcodes.GETSTATIC);
                            if (!getMouseMoveIdInsn.desc.equals("I")) {
                                throw new OutdatedDeobfuscatorException("Widget", "Galemulator", "access$000", "Wrong descriptor");
                            }
                            remapField(mappingsStream, galemulatorClass, getMouseMoveIdInsn.name, "mouseMoveId", "I");
                            insn = getMouseMoveIdInsn.getNext();

                            MethodInsnNode unprojectCoordsInsn = getNext(insn, Opcodes.INVOKESTATIC);
                            if (!unprojectCoordsInsn.desc.equals("(Lcom/badlogic/gdx/math/Vector3;)V")) {
                                throw new OutdatedDeobfuscatorException("Widget", GALFX_CLASS, "unprojectScreenToWidget", "Wrong descriptor");
                            }
                            if (!unprojectCoordsInsn.owner.equals(GALFX_CLASS)) {
                                throw new OutdatedDeobfuscatorException("Widget", GALFX_CLASS, "unprojectScreenToWidget", "Wrong owner");
                            }
                            remapMethod(mappingsStream, GALFX_CLASS, unprojectCoordsInsn.name, "unprojectScreenToWidget", "(Lcom/badlogic/gdx/math/Vector3;)V");

                            MethodInsnNode getScreenHeightInsn = getNext(unprojectCoordsInsn, Opcodes.INVOKESTATIC);
                            if (!getScreenHeightInsn.desc.equals("()I")) {
                                throw new OutdatedDeobfuscatorException("Widget", GALFX_CLASS, "getScreenHeight", "Wrong descriptor");
                            }
                            if (!getScreenHeightInsn.owner.equals(GALFX_CLASS)) {
                                throw new OutdatedDeobfuscatorException("Widget", GALFX_CLASS, "getScreenHeight", "Wrong owner");
                            }
                            remapMethod(mappingsStream, GALFX_CLASS, getScreenHeightInsn.name, "getScreenHeight", "()I");
                            insn = getScreenHeightInsn.getNext();

                            while (insn != null) {
                                if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                                    MethodInsnNode methodInsn = (MethodInsnNode) insn;
                                    if (methodInsn.owner.equals(WIDGET_CLASS) && methodInsn.desc.equals("(FFZ)V")) {
                                        widgetHoverMethod = methodInsn.name;
                                        break;
                                    }
                                }
                                insn = insn.getNext();
                            }
                        }
                    }
                }
            } else if (node.superName.equals(abstractBulletinClass)) {
                if (node.name.equals(textBulletinClass)) {
                    continue;
                }
                remapMethod(mappingsStream, node.name, bulletinDrawAtMethod, "drawAt", "(FF)V");
                if (empireBulletinClass != null) {
                    throw new OutdatedDeobfuscatorException("UI", EMPIRE_BULLETIN_CLASS, "*", "Collision");
                }
                empireBulletinClass = node.name;
            } else {
                methodLoop:
                for (MethodNode method : node.methods) {
                    if (method.desc.equals("()V")) {
                        AbstractInsnNode insn = method.instructions.getFirst();
                        while (insn != null) {
                            if (insn.getOpcode() == Opcodes.LDC) {
                                LdcInsnNode ldcInsn = (LdcInsnNode) insn;
                                if (ldcInsn.cst.equals("Tap to select location")) {
                                    ninepatchButtonClass = node.superName;
                                    for (FieldNode field : node.fields) {
                                        if (field.desc.startsWith("L" + UI_PACKAGE)) {
                                            if (shipConstructionWidgetClass != null) {
                                                throw new OutdatedDeobfuscatorException("ShipConstruction", SHIP_CONSTRUCTION_WIDGET_CLASS, "*", "Collision");
                                            }
                                            shipConstructionWidgetClass = field.desc.substring(1, field.desc.length() - 1);
                                            remapField(mappingsStream, node.name, field.name, "this$0", field.desc);
                                            ClassNode outerClassNode = name2Node.get(shipConstructionWidgetClass);
                                            if (outerClassNode == null) {
                                                throw new OutdatedDeobfuscatorException("ShipConstruction", SHIP_CONSTRUCTION_WIDGET_CLASS, "*", "Node not found");
                                            }
                                            assignAsAnonymousClass(outerClassNode, node, "<init>", "()V");
                                            remapClass(mappingsStream, node.name, SHIP_CONSTRUCTION_WIDGET_LOCATION_SELECTOR_CLASS);
                                        }
                                    }
                                    AbstractInsnNode nextInsn = insn.getNext();
                                    if (nextInsn.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                                        throw new OutdatedDeobfuscatorException("ShipConstruction", BASIC_BUTTON_CLASS, "setButtonText", "Unexpected opcode");
                                    }
                                    basicButtonSetTextMethod = ((MethodInsnNode) nextInsn).name;
                                    methodLoop2:
                                    for (MethodNode method2 : node.methods) {
                                        AbstractInsnNode instruction = method2.instructions.getFirst();
                                        while (instruction != null) {
                                            if (instruction.getOpcode() == Opcodes.LDC) {
                                                LdcInsnNode ldcInsn2 = (LdcInsnNode) instruction;
                                                if (ldcInsn2.cst.equals("Placing ...")) {
                                                    AbstractInsnNode prevInsn = instruction.getPrevious();
                                                    while (prevInsn.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                                                        prevInsn = prevInsn.getPrevious();
                                                    }
                                                    MethodInsnNode prevMethod = (MethodInsnNode) prevInsn;
                                                    if (!prevMethod.desc.equals("(L" + GALCOLOR_CLASS + ";)V")) {
                                                        throw new OutdatedDeobfuscatorException("ShipConstruction", BASIC_BUTTON_CLASS, "setButtonColor", "Unexpected descriptor");
                                                    }
                                                    basicButtonSetColorMethod = prevMethod.name;
                                                    while (instruction != null) {
                                                        if (instruction.getOpcode() == Opcodes.INVOKESTATIC) {
                                                            MethodInsnNode methodInsn = (MethodInsnNode) instruction;
                                                            if (methodInsn.owner.equals(SPACE_CLASS)) {
                                                                remapMethod(mappingsStream, SPACE_CLASS, methodInsn.name, SPACE_ADD_AUXILIARY_LISTENER, methodInsn.desc);
                                                                String auxListenerClassName = methodInsn.desc.substring(2, methodInsn.desc.length() - 3);
                                                                if (!name2Node.containsKey(auxListenerClassName)) {
                                                                    throw new OutdatedDeobfuscatorException("ShipConstruction", AUXILIARY_LISTENER_CLASS, "*", "Node not found (searched for " + auxListenerClassName + ")");
                                                                }
                                                                remapClass(mappingsStream, auxListenerClassName, AUXILIARY_LISTENER_CLASS);
                                                                ClassNode auxListenerImpl = name2Node.get(((MethodInsnNode) methodInsn.getPrevious()).owner);
                                                                if (auxListenerImpl.interfaces.size() != 1 || !auxListenerImpl.interfaces.get(0).equals(auxListenerClassName)) {
                                                                    throw new OutdatedDeobfuscatorException("ShipConstruction", "Unable to find the anonymous class that implements " + AUXILIARY_LISTENER_CLASS);
                                                                }
                                                                for (MethodNode method3: auxListenerImpl.methods) {
                                                                    if (!method3.desc.equals("(FF)Z") || !method3.name.equals("globalTap")) {
                                                                        continue;
                                                                    }
                                                                    AbstractInsnNode insn3 = method3.instructions.getFirst();
                                                                    MethodInsnNode unprojectToBoard = getNext(insn3, Opcodes.INVOKESTATIC);
                                                                    if (!unprojectToBoard.owner.equals(GALFX_CLASS)) {
                                                                        throw new OutdatedDeobfuscatorException("ShipConstruction", GALFX_CLASS, "unprojectScreenToBoard", "Wrong owner class");
                                                                    }
                                                                    MethodInsnNode findStarNear = getNext(unprojectToBoard, Opcodes.INVOKESTATIC);
                                                                    if (!findStarNear.owner.equals(SPACE_CLASS)) {
                                                                        throw new OutdatedDeobfuscatorException("ShipConstruction", SPACE_CLASS, "findStarNear(FF)", "Wrong owner class");
                                                                    }
                                                                    remapMethod(mappingsStream, SPACE_CLASS, findStarNear.name, "findStarNear", "(FF)L" + STAR_CLASS + ";");
                                                                    remapMethod(mappingsStream, GALFX_CLASS, unprojectToBoard.name, "unprojectScreenToBoard", "(FF)Lcom/badlogic/gdx/math/Vector3;");

                                                                    MethodInsnNode getOwningEmpire = getNext(findStarNear, Opcodes.INVOKEVIRTUAL);
                                                                    if (!getOwningEmpire.owner.equals(STAR_CLASS)) {
                                                                        throw new OutdatedDeobfuscatorException("ShipConstruction", STAR_CLASS, "getOwningEmpire", "Assertion failed");
                                                                    }
                                                                    MethodInsnNode getEmpireColor = getNext(getOwningEmpire, Opcodes.INVOKEVIRTUAL);

                                                                    if (!getEmpireColor.desc.equals("()L" + GALCOLOR_CLASS + ";")) {
                                                                        throw new OutdatedDeobfuscatorException("ShipConstruction", EMPIRE_CLASS, "getDarkerColor", "Wrong desc (Method actually is " + new MethodReference(getEmpireColor) + ")");
                                                                    }
                                                                    if (!getEmpireColor.owner.equals(EMPIRE_CLASS)) {
                                                                        throw new OutdatedDeobfuscatorException("ShipConstruction", EMPIRE_CLASS, "getDarkerColor", "Wrong class");
                                                                    }

                                                                    MethodInsnNode setEffectColor = (MethodInsnNode) getEmpireColor.getNext();
                                                                    if (!setEffectColor.desc.equals("(L" + GALCOLOR_CLASS + ";)V")) {
                                                                        throw new OutdatedDeobfuscatorException("ShipConstruction", LOCATION_SELECTED_EFFECT_CLASS, "setColor", "Wrong desc");
                                                                    }
                                                                    remapMethod(mappingsStream, EMPIRE_CLASS, getEmpireColor.name, "getDarkerColor", "()L" + GALCOLOR_CLASS + ";");
                                                                    remapMethod(mappingsStream, LOCATION_SELECTED_EFFECT_CLASS, setEffectColor.name, "setColor", "(L" + GALCOLOR_CLASS + ";)V");

                                                                    insn3 = setEffectColor.getNext();
                                                                    while (insn3 != null) {
                                                                        if (insn3.getOpcode() == Opcodes.INVOKESTATIC) {
                                                                            MethodInsnNode methodInsn3 = (MethodInsnNode) insn3;
                                                                            if (methodInsn3.owner.equals(SPACE_CLASS)) {
                                                                                if (!methodInsn3.desc.equals("(L" + ITEM_CLASS + ";)V")) {
                                                                                    throw new OutdatedDeobfuscatorException("ShipCosntruction", SPACE_CLASS, "showItem", "Unexpected desc");
                                                                                }
                                                                                remapMethod(mappingsStream, SPACE_CLASS, methodInsn3.name, "showItem", "(L" + ITEM_CLASS + ";)V");
                                                                                break;
                                                                            }
                                                                        }
                                                                        insn3 = insn3.getNext();
                                                                    }
                                                                    MethodInsnNode playSample = getNext(insn3, Opcodes.INVOKEVIRTUAL);
                                                                    if (!playSample.owner.equals(AUDIO_SAMPLE_CLASS) || !playSample.desc.equals("()V")) {
                                                                        throw new OutdatedDeobfuscatorException("ShipConstruction", AUDIO_SAMPLE_CLASS, "play", "Invalid descriptor or owner class");
                                                                    }
                                                                    remapMethod(mappingsStream, AUDIO_SAMPLE_CLASS, playSample.name, "play", "()V");
                                                                    MethodInsnNode removeThisInsn = getNext(playSample, Opcodes.INVOKESTATIC);
                                                                    if (!removeThisInsn.owner.equals(SPACE_CLASS)) {
                                                                        throw new OutdatedDeobfuscatorException("ShipConstruction", "Space", "removeAuxiliaryListener", "Wrong owner");
                                                                    }
                                                                    remapMethod(mappingsStream, SPACE_CLASS, removeThisInsn.name, "removeAuxiliaryListener", removeThisInsn.desc);
                                                                    break;
                                                                }
                                                                break;
                                                            }
                                                        }
                                                        instruction = instruction.getNext();
                                                    }
                                                    break methodLoop2;
                                                }
                                            }
                                            instruction = instruction.getNext();
                                        }
                                    }
                                    break methodLoop;
                                }
                            }
                            insn = insn.getNext();
                        }
                    }
                }
            }
        }

        if (sidebarInitializeMethod == null) {
            throw new IllegalStateException("logic error");
        }
        if (widgetCameraField == null) {
            throw new OutdatedDeobfuscatorException("Widget", "Widget", "internalCamera");
        }
        if (widgetOnDisposeMethod == null) {
            throw new OutdatedDeobfuscatorException("Widget", "Widget", "tick", "Not found");
        }
        if (widgetHoverMethod == null) {
            throw new OutdatedDeobfuscatorException("Widget", "Widget", "hover");
        }
        if (closeNonPersistentWidgetsMethod == null) {
            throw new OutdatedDeobfuscatorException("Space", "closeNonPersistentWidgets", "Not found");
        }
        if (ninepatchButtonClass == null) {
            throw new OutdatedDeobfuscatorException("ShipConstruction", NINEPATCH_BUTTON, "*", "Not found");
        }
        if (shipConstructionWidgetClass == null) {
            throw new OutdatedDeobfuscatorException("ShipConstruction", SHIP_CONSTRUCTION_WIDGET_CLASS, "*", "Not found");
        }
        if (galaxyPreviewClass == null) {
            throw new OutdatedDeobfuscatorException("UI", GALAXY_PREVIEW_WIDGET_CLASS, "*", "Not found");
        }
        if (empireBulletinClass == null) {
            throw new OutdatedDeobfuscatorException("UI", EMPIRE_BULLETIN_CLASS, "*", "Not found");
        }
        if (fxFullDrawTextureMethod == null) {
            throw new OutdatedDeobfuscatorException("UI", GALFX_CLASS, GALFX_DRAW_TEXTURE_DESCRIPTOR, "Not found");
        }

        ClassNode ninepatchButtonNode = name2Node.get(ninepatchButtonClass);
        String baseButtonClass = ninepatchButtonNode.superName;
        ClassNode baseButtonNode = name2Node.get(baseButtonClass);

        remapMethod(mappingsStream, SPACE_CLASS, closeNonPersistentWidgetsMethod, "closeNonPersistentWidgets", "()V");
        remapMethod(mappingsStream, sidebarClass, sidebarInitializeMethod.name, "reinitElements", "()V");
        remapClass(mappingsStream, ninepatchButtonClass, NINEPATCH_BUTTON);
        remapClass(mappingsStream, baseButtonClass, BASIC_BUTTON_CLASS);
        remapClass(mappingsStream, shipConstructionWidgetClass, SHIP_CONSTRUCTION_WIDGET_CLASS);
        remapClass(mappingsStream, empireBulletinClass, EMPIRE_BULLETIN_CLASS);

        String widgetClearChildrenMethod = null;
        String widgetAddChildMethod = null;
        String widgetDrawDefaultBackgroundMethod = null;
        final String widgetAddChildMethodDescriptor = "(L" + WIDGET_CLASS + ";)L" + WIDGET_CLASS + ";";
        String starGeneratorInterface = null;
        String fxDrawNinepatchMethod = null;
        String baseButtonRenderBackgroundMethod = null;
        String fxDrawTextMethod2 = null;
        String fxRendercacheThreadlocal = null;
        int fxAltDrawTexturesRemapCount = 0;
        String fxDrawRectMethod = null;
        ClassNode widgetClass = this.name2Node.get(WIDGET_CLASS);

        { // Let's not pollute the method with even more local variables
            AbstractInsnNode first = sidebarInitializeMethod.instructions.getFirst();
            while (first.getOpcode() == -1) {
                first = first.getNext();
            }
            if (first.getOpcode() != Opcodes.ALOAD) {
                throw new OutdatedDeobfuscatorException("Widget", "Widget#clearChildren cannot be resolved (first opcode not ALOAD)");
            }
            first = getNext(first);
            if (first.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                throw new OutdatedDeobfuscatorException("Widget", "Widget#clearChildren cannto be resolved (unexpected bytecode)");
            }
            MethodInsnNode clearChildren = (MethodInsnNode) first;
            widgetClearChildrenMethod = clearChildren.name;
            if (!clearChildren.desc.equals("()V")) {
                throw new OutdatedDeobfuscatorException("Widget", "Widget", "clearChildren", "unexpected Descriptor");
            }
            first = first.getNext();
            while (first.getOpcode() != Opcodes.INVOKESPECIAL) {
                first = first.getNext();
            }
            first = getNext(first);
            if (first.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                throw new OutdatedDeobfuscatorException("Widget", "Widget", "addChild", "Unexpected opcode");
            }
            MethodInsnNode addChild = (MethodInsnNode) first;
            if (!addChild.desc.equals(widgetAddChildMethodDescriptor)) {
                throw new OutdatedDeobfuscatorException("Widget", "Widget", "addChild", "unexpected Descriptor");
            }
            widgetAddChildMethod = addChild.name;

            ClassNode galaxyPreview = name2Node.get(galaxyPreviewClass);
            ClassNode galFXNode = name2Node.get(GALFX_CLASS);

            for (MethodNode method : galaxyPreview.methods) {
                if (method.name.equals("<init>")) {
                    if (starGeneratorInterface != null) {
                        throw new OutdatedDeobfuscatorException("Widget", STAR_GENERATOR_INTERFACE, "*", "Collision");
                    }
                    starGeneratorInterface = method.desc.substring(2, method.desc.length() - 3);
                    if (!starGeneratorInterface.startsWith(BASE_PACKAGE)) {
                        throw new AssertionError("Programmer error:" + starGeneratorInterface);
                    }
                    String generatorField = null;
                    String previewLocationsField = null;
                    for (FieldNode field : galaxyPreview.fields) {
                        if (field.desc.equals('L' + starGeneratorInterface + ';')) {
                            if (generatorField != null) {
                                throw new OutdatedDeobfuscatorException("Widget", GALAXY_PREVIEW_WIDGET_CLASS, "generator", "Collision");
                            }
                            generatorField = field.name;
                        } else if (field.desc.equals("Ljava/util/ArrayList;")) {
                            if (previewLocationsField != null) {
                                throw new OutdatedDeobfuscatorException("Widget", GALAXY_PREVIEW_WIDGET_CLASS, "previewLocations", "Collision");
                            }
                            previewLocationsField = field.name;
                        }
                    }

                    if (generatorField == null) {
                        throw new OutdatedDeobfuscatorException("Widget", GALAXY_PREVIEW_WIDGET_CLASS, "generator", "Not present");
                    }
                    if (previewLocationsField == null) {
                        throw new OutdatedDeobfuscatorException("Widget", GALAXY_PREVIEW_WIDGET_CLASS, "previewLocations", "Not present");
                    }

                    remapField(mappingsStream, galaxyPreviewClass, generatorField, "generator", 'L' + starGeneratorInterface + ';');
                    remapField(mappingsStream, galaxyPreviewClass, previewLocationsField, "previewLocations", "Ljava/util/ArrayList;");

                    String allowPreviewMethod = null;
                    String setPreviewGeneratorMethod = null;
                    for (MethodNode method2 : galaxyPreview.methods) {
                        if (method2.name.equals(widgetRefreshLayoutMethod) && method2.desc.equals("()V")) {
                            for (AbstractInsnNode insn = method2.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                                if (insn.getOpcode() != Opcodes.GETFIELD) {
                                    continue;
                                }
                                FieldInsnNode fInsn = (FieldInsnNode) insn;
                                if (!fInsn.name.equals(generatorField) || !fInsn.desc.equals('L' + starGeneratorInterface + ';')) {
                                    continue;
                                }
                                if (allowPreviewMethod != null) {
                                    throw new OutdatedDeobfuscatorException("Widget", STAR_GENERATOR_INTERFACE, "allowPreview", "Collision");
                                }
                                fInsn = getNext(fInsn, Opcodes.GETFIELD);
                                if (!fInsn.name.equals(generatorField) || !fInsn.desc.equals('L' + starGeneratorInterface + ';')) {
                                    throw new OutdatedDeobfuscatorException("Widget", STAR_GENERATOR_INTERFACE, "allowPreview", "Expected preceeding null check potentially missing");
                                }
                                AbstractInsnNode nextInsn = getNext(fInsn);
                                if (nextInsn.getOpcode() != Opcodes.INVOKEINTERFACE) {
                                    throw new OutdatedDeobfuscatorException("Widget", STAR_GENERATOR_INTERFACE, "allowPreview", "Unexpected opcode; type " + nextInsn.getClass());
                                }
                                MethodInsnNode methodInsn = (MethodInsnNode) nextInsn;
                                if (!methodInsn.owner.equals(starGeneratorInterface) || !methodInsn.desc.equals("()Z")) {
                                    throw new OutdatedDeobfuscatorException("Widget", STAR_GENERATOR_INTERFACE, "allowPreview", "Unexpected owner or descriptor");
                                }
                                allowPreviewMethod = methodInsn.name;
                                break;
                            }
                        } else if (method2.desc.equals("(L" + starGeneratorInterface + ";)V") && !method2.name.equals("<init>")) {
                            LdcInsnNode ldcInsn = getNextOrNull(method2.instructions.getFirst(), Opcodes.LDC);
                            while (ldcInsn != null && !ldcInsn.cst.equals("Setting generator to: ")) {
                                ldcInsn = getNextOrNull(ldcInsn, Opcodes.LDC);
                            }
                            if (ldcInsn == null) {
                                continue;
                            }
                            if (setPreviewGeneratorMethod != null) {
                                throw new OutdatedDeobfuscatorException("Widget", GALAXY_PREVIEW_WIDGET_CLASS, "setGenerator", "Collision");
                            }
                            setPreviewGeneratorMethod = method2.name;
                            MethodInsnNode mInsn = getNextOrNull(ldcInsn, Opcodes.INVOKEVIRTUAL);
                            while (mInsn != null && (mInsn.owner.equals("java/lang/StringBuilder") || mInsn.owner.equals("java/io/PrintStream"))) {
                                mInsn = getNextOrNull(mInsn, Opcodes.INVOKEVIRTUAL);
                            }
                            if (mInsn == null) {
                                throw new OutdatedDeobfuscatorException("Widget", GALAXY_PREVIEW_WIDGET_CLASS, "reconfigureGenerator", "Instructions exhausted");
                            }
                            if (!mInsn.owner.equals(galaxyPreviewClass)) {
                                throw new OutdatedDeobfuscatorException("Widget", GALAXY_PREVIEW_WIDGET_CLASS, "reconfigureGenerator", "Unexpected owner: " + mInsn.owner + "." + mInsn.name + mInsn.desc);
                            }
                            if (!mInsn.desc.equals("()V")) {
                                throw new OutdatedDeobfuscatorException("Widget", GALAXY_PREVIEW_WIDGET_CLASS, "reconfigureGenerator", "Unexpected descriptor: " + mInsn.owner + "." + mInsn.name + mInsn.desc);
                            }
                            remapMethod(mappingsStream, galaxyPreviewClass, mInsn.name, "reconfigureGenerator", "()V");
                            while ((mInsn = getNextOrNull(mInsn, Opcodes.INVOKEVIRTUAL)) != null) {
                                if (mInsn.owner.equals(galaxyPreviewClass) && mInsn.desc.equals("()V")) {
                                    throw new OutdatedDeobfuscatorException("Widget", GALAXY_PREVIEW_WIDGET_CLASS, "reconfigureGenerator", "Collision");
                                }
                            }
                        }
                    }

                    if (allowPreviewMethod == null) {
                        throw new OutdatedDeobfuscatorException("Widget", STAR_GENERATOR_INTERFACE, "allowPreview", "Missing/Absent reference");
                    }
                    if (setPreviewGeneratorMethod == null) {
                        throw new OutdatedDeobfuscatorException("Widget", GALAXY_PREVIEW_WIDGET_CLASS, "setGenerator", "Not present");
                    }
                    remapMethod(mappingsStream, starGeneratorInterface, allowPreviewMethod, "allowPreview", "()Z");
                    remapMethod(mappingsStream, galaxyPreviewClass, setPreviewGeneratorMethod, "setGenerator", "(L" + starGeneratorInterface + ";)V");
                } else if (method.name.equals(widgetRefreshLayoutMethod) && method.desc.equals("()V")) {
                    String targettedMethod = null;
                    for (AbstractInsnNode insn : method.instructions) {
                        if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                            MethodInsnNode methodInsn = (MethodInsnNode) insn;
                            if (methodInsn.owner.equals(galaxyPreviewClass)) {
                                if (targettedMethod != null) {
                                    throw new OutdatedDeobfuscatorException("Widget", GALAXY_PREVIEW_WIDGET_CLASS, "addGeneratedStar", "Collision");
                                }
                                targettedMethod = methodInsn.name;
                                remapMethod(mappingsStream, galaxyPreviewClass, methodInsn.name, "addGeneratedStar", "()V");
                            }
                        }
                    }
                    if (targettedMethod == null) {
                        throw new OutdatedDeobfuscatorException("Widget", GALAXY_PREVIEW_WIDGET_CLASS, "addGeneratedStar", "Not found");
                    }
                    String starCountField = null;
                    for (MethodNode method2 : galaxyPreview.methods) {
                        if (method2.name.equals(targettedMethod) && method2.desc.equals("()V")) {
                            for (AbstractInsnNode insn : method2.instructions) {
                                if (insn.getOpcode() == Opcodes.GETSTATIC) {
                                    FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                                    if (fieldInsn.owner.equals(SPACE_CLASS) && fieldInsn.desc.equals("F")) {
                                        if (starCountField != null) {
                                            throw new OutdatedDeobfuscatorException("Widget", SPACE_CLASS, "starCount", "Collision");
                                        }
                                        // Why would the star count - which should be an integer - be a float?
                                        starCountField = fieldInsn.name;
                                    }
                                }
                            }
                            break;
                        }
                    }
                    if (starCountField == null) {
                        throw new OutdatedDeobfuscatorException("Widget", SPACE_CLASS, "starCount", "Not found");
                    }
                    remapField(mappingsStream, SPACE_CLASS, starCountField, "starCount", "F");
                } else if (method.name.equals(widgetDrawMethod) && method.desc.equals("()V")) {
                    MethodInsnNode mInsn = getNextOrNull(method.instructions.getFirst(), Opcodes.INVOKEVIRTUAL);
                    MethodInsnNode drawDefaultBackgroundInsn = null;
                    while (mInsn != null) {
                        insnCheck:
                        if (mInsn.desc.equals("()V") && mInsn.getPrevious().getOpcode() == Opcodes.ALOAD && ((VarInsnNode) mInsn.getPrevious()).var == 0) {
                            for (MethodNode method2 : widgetClass.methods) {
                                if (method2.name.equals(mInsn.name) && method2.desc.equals("()V")) {
                                    FieldInsnNode fInsn = getNextOrNull(method2.instructions.getFirst(), Opcodes.GETSTATIC);
                                    if (fInsn == null || !fInsn.owner.equals(GALCOLOR_CLASS) || !fInsn.name.equals("NEAR_SOLID") || !fInsn.desc.equals("L" + GALCOLOR_CLASS + ";")) {
                                        break insnCheck;
                                    }
                                    if (widgetDrawDefaultBackgroundMethod != null) {
                                        throw new OutdatedDeobfuscatorException("Widget", WIDGET_CLASS, "drawDefaultBackground", "Collision");
                                    }
                                    widgetDrawDefaultBackgroundMethod = mInsn.name;
                                    drawDefaultBackgroundInsn = mInsn;
                                }
                            }
                        }
                        mInsn = getNextOrNull(mInsn, Opcodes.INVOKEVIRTUAL);
                    }

                    if (drawDefaultBackgroundInsn == null) {
                        throw new OutdatedDeobfuscatorException("Widget", WIDGET_CLASS, "drawDefaultBackground", "Absent/Missing instruction");
                    }

                    mInsn = getNextOrNull(drawDefaultBackgroundInsn, Opcodes.INVOKESTATIC);
                    if (!mInsn.owner.equals(GALFX_CLASS) || !mInsn.desc.equals("(FFFFL" + GALCOLOR_CLASS + ";ZL" + GDX_CAMERA_CLASS + ";)V")) {
                        throw new OutdatedDeobfuscatorException("Widget", GALFX_CLASS, "drawRectangle", "Invalid owner or descriptor");
                    }

                    fxDrawRectMethod = mInsn.name;
                    remapMethod(mappingsStream, GALFX_CLASS, fxDrawRectMethod, "drawRectangle", "(FFFFL" + GALCOLOR_CLASS + ";ZL" + GDX_CAMERA_CLASS + ";)V");
                }
            }

            // Resolve more GalFX#drawTexture methods
            methodLoop:
            for (MethodNode method : galFXNode.methods) {
                if (method.name.equals(fxDrawRectMethod) && method.desc.equals("(FFFFL" + GALCOLOR_CLASS + ";ZL" + GDX_CAMERA_CLASS + ";)V")) {
                    MethodInsnNode minsn = getNextOrNull(method.instructions.getFirst(), Opcodes.INVOKESTATIC);
                    if (getNextOrNull(minsn, Opcodes.INVOKESTATIC) != null) {
                        throw new OutdatedDeobfuscatorException("Widget", GALFX_CLASS, "drawRectangle", "Multiple delegate candidates");
                    }
                    if (!minsn.owner.equals(GALFX_CLASS)) {
                        throw new OutdatedDeobfuscatorException("Widget", GALFX_CLASS, "drawRectangle", "Invalid delegate owner");
                    }
                    remapMethod(mappingsStream, GALFX_CLASS, fxDrawRectMethod, "drawRectangle", minsn.desc);
                    methodLoop2:
                    for (MethodNode method2 : galFXNode.methods) {
                        if (method2.name.equals(minsn.name) && method2.desc.equals(minsn.desc)) {
                            MethodInsnNode mInsn2 = getNext(method2.instructions.getFirst(), Opcodes.INVOKESTATIC);
                            mInsn2 = getNext(mInsn2, Opcodes.INVOKESTATIC);
                            final MethodInsnNode witnessInsn = mInsn2;
                            if (!witnessInsn.owner.equals(GALFX_CLASS)) {
                                throw new OutdatedDeobfuscatorException("Widget", GALFX_CLASS, "drawLine", "Invalid owner");
                            }
                            for (int i = 0; i < 3; i++) {
                                mInsn2 = getNextOrNull(mInsn2, Opcodes.INVOKESTATIC);
                                if (mInsn2 == null) {
                                    throw new OutdatedDeobfuscatorException("Widget", GALFX_CLASS, "drawLine", "Repeating insn not repeating - exhausted");
                                }
                                if (!mInsn2.owner.equals(GALFX_CLASS) || !witnessInsn.name.equals(mInsn2.name) || !witnessInsn.desc.equals(mInsn2.desc)) {
                                    throw new OutdatedDeobfuscatorException("Widget", GALFX_CLASS, "drawLine", "Repeating insn not repeating - owner, name or desc mismatch");
                                }
                            }
                            remapMethod(mappingsStream, GALFX_CLASS, mInsn2.name, "drawLine", mInsn2.desc);
                            break methodLoop2;
                        }
                    }
                    continue;
                }

                for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn.getOpcode() != -1
                            && insn.getOpcode() != Opcodes.ALOAD
                            && insn.getOpcode() != Opcodes.DLOAD
                            && insn.getOpcode() != Opcodes.ILOAD
                            && insn.getOpcode() != Opcodes.ICONST_1
                            && insn.getOpcode() != Opcodes.INVOKESTATIC
                            && insn.getOpcode() != Opcodes.GETSTATIC
                            && insn.getOpcode() != Opcodes.RETURN) {
                        // The method is not matching the delegator pattern we are scanning for
                        continue methodLoop;
                    }
                }
                MethodInsnNode minsn = getNextOrNull(method.instructions.getFirst(), Opcodes.INVOKESTATIC);
                if (minsn == null
                        || getNextOrNull(getNext(minsn)) != null
                        || !minsn.owner.equals(GALFX_CLASS)
                        || !minsn.name.equals(fxFullDrawTextureMethod)
                        || !minsn.desc.equals(GALFX_DRAW_TEXTURE_DESCRIPTOR)) {
                    continue;
                }
                remapMethod(mappingsStream, GALFX_CLASS, method.name, "drawTexture", method.desc);
                fxAltDrawTexturesRemapCount++;
            }

            for (MethodNode method : ninepatchButtonNode.methods) {
                if (method.desc.equals("()V")) {
                    AbstractInsnNode insn = method.instructions.getFirst();
                    FieldInsnNode finsn = getNextOrNull(insn, Opcodes.GETFIELD);
                    if (finsn != null && finsn.desc.equals("L" + NINEPATCH_CLASS + ";") && finsn.owner.equals(ninepatchButtonClass)) {
                        if (fxDrawNinepatchMethod != null) {
                            throw new OutdatedDeobfuscatorException("UI", GALFX_CLASS, "drawNinepatch", "Collision");
                        }
                        baseButtonRenderBackgroundMethod = method.name;
                        MethodInsnNode minsn = getNext(finsn, Opcodes.INVOKESTATIC);
                        if (minsn == null || getNextOrNull(minsn, Opcodes.INVOKESTATIC) != null || !minsn.owner.equals(GALFX_CLASS)) {
                            throw new OutdatedDeobfuscatorException("UI", GALFX_CLASS, "drawNinepatch", "Unexpected method structure");
                        }
                        if (!minsn.desc.equals(GALFX_DRAW_NINEPATCH_DESCRIPTOR)) {
                            throw new OutdatedDeobfuscatorException("UI", GALFX_CLASS, "drawNinepatch", "Unexpected method descriptor");
                        }
                        fxDrawNinepatchMethod = minsn.name;
                        remapMethod(mappingsStream, GALFX_CLASS, fxDrawNinepatchMethod, "drawNinepatch", GALFX_DRAW_NINEPATCH_DESCRIPTOR);
                        continue;
                    }
                }
            }

            for (MethodNode method : baseButtonNode.methods) {
                if (method.desc.equals("()V") && method.name.equals(widgetDrawMethod)) {
                    AbstractInsnNode insn = method.instructions.getFirst();
                    while (insn.getOpcode() != Opcodes.INVOKESTATIC) {
                        insn = insn.getNext();
                    }
                    if (getNext(insn).getOpcode() != Opcodes.RETURN) {
                        throw new OutdatedDeobfuscatorException("UI", GALFX_CLASS, "drawText (with align)", "Unexpected opcode after matched opcode");
                    }
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;
                    if (!methodInsn.owner.equals(GALFX_CLASS) || !methodInsn.desc.equals(GALFX_DRAW_TEXT_DESCRIPTOR_2)) {
                        throw new OutdatedDeobfuscatorException("UI", GALFX_CLASS, "drawText (with align)", "Matched instruction invalid");
                    }
                    fxDrawTextMethod2 = methodInsn.name;
                    remapMethod(mappingsStream, GALFX_CLASS, fxDrawTextMethod2, "drawText", GALFX_DRAW_TEXT_DESCRIPTOR_2);
                    break;
                }
            }

            for (MethodNode method : galFXNode.methods) {
                if (method.name.equals(fxDrawTextMethod) && method.desc.equals(GALFX_DRAW_TEXT_DESCRIPTOR)) {
                    AbstractInsnNode insn = method.instructions.getFirst();
                    while (insn != null) {
                        if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                            MethodInsnNode minsn = (MethodInsnNode) insn;
                            if (minsn.owner.equals("java/lang/ThreadLocal") && minsn.name.equals("get") && minsn.desc.equals("()Ljava/lang/Object;")) {
                                insn = insn.getNext();
                                if (insn.getOpcode() == Opcodes.CHECKCAST) {
                                    TypeInsnNode castInsn = (TypeInsnNode) insn;
                                    if (castInsn.desc.startsWith(RENDERSYSTEM_PACKAGE)) {
                                        AbstractInsnNode gestaticInsn = minsn.getPrevious();
                                        if (gestaticInsn.getOpcode() != Opcodes.GETSTATIC) {
                                            throw new OutdatedDeobfuscatorException("UI", GALFX_CLASS, "RENDERCACHE_LOCAL", "Unexpected opcode; Expected GETSTATIC");
                                        }
                                        FieldInsnNode finsn = (FieldInsnNode) gestaticInsn;
                                        if (!finsn.owner.equals(GALFX_CLASS) || !finsn.desc.equals("Ljava/lang/ThreadLocal;")) {
                                            throw new OutdatedDeobfuscatorException("UI", GALFX_CLASS, "RENDERCACHE_LOCAL", "Instruction invalid");
                                        }
                                        fxRendercacheThreadlocal = finsn.name;
                                    }
                                }
                            }
                        }
                        insn = insn.getNext();
                    }
                    break;
                }
            }
        }

        if (widgetDrawDefaultBackgroundMethod == null) {
            throw new OutdatedDeobfuscatorException("Widget", WIDGET_CLASS, "drawDefaultBackground", "Not found");
        }

        if (widgetClearChildrenMethod == null) {
            throw new OutdatedDeobfuscatorException("Widget", "Widget", "clearChildren");
        }
        if (widgetAddChildMethod == null) {
            throw new OutdatedDeobfuscatorException("Widget", "Widget", "addChild");
        }

        if (getWidgetWidthMethod == null) {
            throw new IllegalStateException("The deobfuscator for the Widget class is out of date (Did not detect Widget#getWidth)");
        }
        if (widgetDrawHeaderMethod == null) {
            throw new IllegalStateException("The deobfuscator for the Widget class is out of date (Did not detect Widget#drawHeader)");
        }
        if (starGeneratorInterface == null) {
            throw new OutdatedDeobfuscatorException("Widget", STAR_GENERATOR_INTERFACE, "*", "Not found");
        }
        if (fxAltDrawTexturesRemapCount != 2) {
            throw new OutdatedDeobfuscatorException("UI", GALFX_CLASS, "drawTexture (alternative)", "Hit " + fxAltDrawTexturesRemapCount + ", expected 2.");
        }
        if (fxDrawNinepatchMethod == null) {
            throw new OutdatedDeobfuscatorException("UI", GALFX_CLASS, "drawNinepatch", "Not found");
        }
        if (fxDrawTextMethod2 == null) {
            throw new OutdatedDeobfuscatorException("UI", GALFX_CLASS, "drawText (with align)", "Not found");
        }
        if (fxRendercacheThreadlocal == null) {
            throw new OutdatedDeobfuscatorException("UI", GALFX_CLASS, "RENDERCACHE_LOCAL", "Not found");
        }

        remapClass(mappingsStream, starGeneratorInterface, STAR_GENERATOR_INTERFACE);
        remapField(mappingsStream, GALFX_CLASS, fxRendercacheThreadlocal, "RENDERCACHE_LOCAL", "Ljava/lang/ThreadLocal;");

        String activeWidgetsField = null;
        String widgetIsPersistentMethod = null;
        String closeWidgetMethod = null;

        ClassNode spaceClassNode = name2Node.get(SPACE_CLASS);

        for (MethodNode method : spaceClassNode.methods) {
            // Woo! Loops!
            if (method.name.equals(closeNonPersistentWidgetsMethod) && method.desc.equals("()V")) {
                AbstractInsnNode insn = method.instructions.getFirst();
                while (insn.getOpcode() != Opcodes.GETSTATIC) {
                    insn = insn.getNext();
                }
                activeWidgetsField = ((FieldInsnNode) insn).name;

                while (insn != null) {
                    if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        MethodInsnNode methodInsn = (MethodInsnNode) insn;
                        if (methodInsn.owner.equals(WIDGET_CLASS) && methodInsn.desc.equals("()Z")) {
                            if (widgetIsPersistentMethod != null) {
                                throw new OutdatedDeobfuscatorException("Widget", "Widget", "isPersistent", "Collision");
                            }
                            widgetIsPersistentMethod = methodInsn.name;
                        }
                    } else if (insn.getOpcode() == Opcodes.INVOKESTATIC) {
                        MethodInsnNode methodInsn = (MethodInsnNode) insn;
                        if (methodInsn.owner.equals(SPACE_CLASS) && methodInsn.desc.equals("(L" + WIDGET_CLASS + ";)V")) {
                            if (closeWidgetMethod != null) {
                                throw new OutdatedDeobfuscatorException("Widget", "Widget", "closeWidget", "Collision");
                            }
                            closeWidgetMethod = methodInsn.name;
                        }
                    }
                    insn = insn.getNext();
                }

                break;
            }
        }

        if (activeWidgetsField == null) {
            throw new OutdatedDeobfuscatorException("Widget", "Space", SPACE_ACTIVE_WIDGETS_FIELD, "not resolved");
        }
        if (widgetIsPersistentMethod == null) {
            throw new OutdatedDeobfuscatorException("Widget", "Widget", "isPersistent", "not resolved");
        }
        if (closeWidgetMethod == null) {
            throw new OutdatedDeobfuscatorException("Widget", "Widget", "closeWidget", "not resolved");
        }
        remapField(mappingsStream, SPACE_CLASS, activeWidgetsField, SPACE_ACTIVE_WIDGETS_FIELD, "Ljava/util/Vector;");
        remapMethod(mappingsStream, SPACE_CLASS, closeWidgetMethod, "closeWidget", "(L" + WIDGET_CLASS + ";)V");

        for (MethodNode method : spaceClassNode.methods) {
            if (method.name.equals(closeWidgetMethod) && method.desc.equals("(L" + WIDGET_CLASS + ";)V")) {
                AbstractInsnNode insn = method.instructions.getFirst();

                while (insn.getOpcode() != Opcodes.GETSTATIC) {
                    insn = insn.getNext();
                }

                FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                remapField(mappingsStream, SPACE_CLASS, fieldInsn.name, "closedWidgets", "Ljava/util/Vector;");
                break;
            }
        }

        if (widgetClass.interfaces.size() != 1) {
            throw new OutdatedDeobfuscatorException("Widget", "Widget implements more/fewer classes than expected.");
        }

        String widgetGetHeightMethod = null;
        String getXMethod = null;
        String getYMethod = null;
        String containsPointMethod = null;
        String widgetSetHeaderColorMethod = null;
        String widgetSetHeaderTitleMethod = null;
        String widgetGetHeaderTitleMethod = null;
        String widgetDrawBackgroundMethod = null;
        String widgetLayoutClass = null;
        String widgetLayoutNewlineMethod = null;
        String widgetLayoutRecomputeMethod = null;
        String widgetChildrenField = null;
        String widgetPositioningField = null;
        String widgetGetPositioningMethod = null;
        String widgetSetPositioningMethod = null;
        String widgetPropagateMessageLocally = null;
        String widgetMessageRecieverClass = widgetClass.interfaces.get(0);

        if (!widgetMessageRecieverClass.startsWith(BASE_PACKAGE) || widgetMessageRecieverClass.startsWith(UI_PACKAGE)) {
            throw new OutdatedDeobfuscatorException("Widget", "Widget implements an unexpected interface.");
        }
        remapClass(mappingsStream, widgetMessageRecieverClass, BASE_PACKAGE + "WidgetMessageReciever");

        for (FieldNode field : widgetClass.fields) {
            if (field.desc.equals("L" + WIDGET_POSITIONING_CLASS + ";")) {
                if (widgetPositioningField != null) {
                    throw new OutdatedDeobfuscatorException("Widget", "Widget", "positioning", "Collision");
                }
                widgetPositioningField = field.name;
                remapField(mappingsStream, WIDGET_CLASS, field.name, "positioning", field.desc);
            }
        }

        if (widgetPositioningField == null) {
            throw new OutdatedDeobfuscatorException("Widget", "Widget", "positioning");
        }

        String widgetRecieveMessageMethod = null;
        ClassNode widgetMessageRecieverClassNode = name2Node.get(widgetMessageRecieverClass);

        if (widgetMessageRecieverClassNode.methods.size() != 1) {
            throw new OutdatedDeobfuscatorException("Widget", "WidgetMessageReciever", "recieveMessage", "Unexpected amounts of methods in the WidgetMessageReciever class.");
        }
        if (!widgetMessageRecieverClassNode.methods.get(0).desc.equals("(L" + WIDGET_MESSAGE_CLASS + ";)V")) {
            throw new OutdatedDeobfuscatorException("Widget", "WidgetMessageReciever", "recieveMessage", "Unexpected descriptor.");
        }
        widgetRecieveMessageMethod = widgetMessageRecieverClassNode.methods.get(0).name;

        for (MethodNode method : widgetClass.methods) {
            if (method.desc.equals("(Lcom/badlogic/gdx/math/Vector2;)Z")) {
                if (containsPointMethod != null) {
                    throw new IllegalStateException("The deobfuscator for the Widget class is out of date (Detected multiple containsPoint methods)");
                }
                containsPointMethod = method.name;
                boolean loadedWidth = false;
                for (AbstractInsnNode insn : method.instructions) {
                    if (insn instanceof MethodInsnNode) {
                        MethodInsnNode methodInsn = (MethodInsnNode) insn;
                        if (methodInsn.name.equals("<init>")) {
                            break;
                        }
                        if (methodInsn.owner.equals(WIDGET_CLASS) && methodInsn.desc.startsWith("()")) {
                            // parameter of the constructor for new Rectangle(): x, y, width, height
                            if (getXMethod == null) {
                                getXMethod = methodInsn.name;
                            } else if (getYMethod == null) {
                                getYMethod = methodInsn.name;
                            } else if (!loadedWidth) {
                                loadedWidth = true;
                                if (!getWidgetWidthMethod.equals(methodInsn.name)) {
                                    throw new IllegalStateException("The deobfuscator for the Widget class is out of date (Conflicting information about Widget#getWidth)");
                                }
                            } else if (widgetGetHeightMethod == null) {
                                widgetGetHeightMethod = methodInsn.name;
                            } else {
                                throw new IllegalStateException("The deobfuscator for the Widget class is out of date (Unexpected method call in Widget#containsPoint method)");
                            }
                        }
                    }
                }
            } else if (method.desc.equals("(Lsnoddasmannen/galimulator/GalColor;)V")) {
                if (isSetter(method, WIDGET_CLASS, widgetHeaderColorField, "Lsnoddasmannen/galimulator/GalColor;")) {
                    if (widgetSetHeaderColorMethod != null) {
                        throw new IllegalStateException("The deobfuscator for the Widget class is out of date (Multiple Widget#setHeaderColor methods detected)");
                    }
                    widgetSetHeaderColorMethod = method.name;
                } else {
                    for (AbstractInsnNode insn : method.instructions) {
                        if (insn.getOpcode() == Opcodes.INVOKESTATIC) {
                            MethodInsnNode methodInsn = (MethodInsnNode) insn;
                            if (methodInsn.owner.equals(GALFX_CLASS)
                                    && methodInsn.name.equals(fxDrawWindowMethod)
                                    && methodInsn.desc.equals("(FFFFLsnoddasmannen/galimulator/GalColor;Lcom/badlogic/gdx/graphics/Camera;)V")) {
                                if (widgetDrawBackgroundMethod != null) {
                                    throw new IllegalStateException("The deobfuscator for the Widget class is out of date (Multiple Widget#drawBackground methods detected)");
                                }
                                widgetDrawBackgroundMethod = method.name;
                                break;
                            }
                        }
                    }
                }
            } else if (method.desc.equals("(Ljava/lang/String;)V")) {
                if (isSetter(method, WIDGET_CLASS, widgetHeaderTitleField, "Ljava/lang/String;")) {
                    if (widgetSetHeaderTitleMethod != null) {
                        throw new IllegalStateException("The deobfuscator for the Widget class is out of date (Multiple Widget#setHeaderTitle methods detected)");
                    }
                    widgetSetHeaderTitleMethod = method.name;
                }
            } else if (method.desc.equals("()Ljava/lang/String;")) {
                if (isGetter(method, WIDGET_CLASS, widgetHeaderTitleField, "Ljava/lang/String;", false)) {
                    if (widgetGetHeaderTitleMethod != null) {
                        throw new IllegalStateException("The deobfuscator for the Widget class is out of date (Multiple Widget#getHeaderTitle methods detected)");
                    }
                    widgetGetHeaderTitleMethod = method.name;
                }
            } else if (method.name.equals(widgetAddChildMethod) && method.desc.equals(widgetAddChildMethodDescriptor)) {
                FieldInsnNode firstField = null;
                for (AbstractInsnNode insn : method.instructions) {
                    if (insn.getOpcode() == Opcodes.PUTFIELD) {
                        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                        if (!fieldInsn.owner.equals(WIDGET_CLASS) || fieldInsn.getPrevious().getOpcode() != Opcodes.INVOKESPECIAL) {
                            continue;
                        }
                        MethodInsnNode ctor = (MethodInsnNode) fieldInsn.getPrevious();
                        if (!ctor.owner.equals(FLOW_LAYOUT_CLASS)) {
                            continue;
                        }
                        if (widgetLayoutClass != null) {
                            throw new OutdatedDeobfuscatorException("Widget", "Widget", "layout", "Multiple candidates assumed");
                        }
                        widgetLayoutClass = fieldInsn.desc.substring(1, fieldInsn.desc.length() - 1);
                        remapClass(mappingsStream, widgetLayoutClass, UI_PACKAGE + "WidgetLayout");
                        remapField(mappingsStream, WIDGET_CLASS, fieldInsn.name, "layout", fieldInsn.desc);
                    } else if (insn.getOpcode() == Opcodes.GETFIELD && firstField == null) {
                        firstField = (FieldInsnNode) insn;
                    }
                }
                if (widgetLayoutClass == null) {
                    throw new OutdatedDeobfuscatorException("Widget", "Widget", "layout", "No candidates assumed");
                }
                if (firstField == null) {
                    throw new OutdatedDeobfuscatorException("Widget", "Widget", "children", "No getfield instructions");
                }
                if (!firstField.owner.equals(WIDGET_CLASS) || !firstField.desc.equals("Ljava/util/Vector;")) {
                    throw new OutdatedDeobfuscatorException("Widget", "Widget", "children", "Descriptor or owner mismatch");
                }
                widgetChildrenField = firstField.name;
                remapField(mappingsStream, WIDGET_CLASS, firstField.name, "children", firstField.desc);
            } else if ((method.access & Opcodes.ACC_PUBLIC) != 0 && method.desc.equals("()L" + WIDGET_POSITIONING_CLASS + ";") && isGetter(method, WIDGET_CLASS, widgetPositioningField, "L" + WIDGET_POSITIONING_CLASS + ";", false)) {
                if (widgetGetPositioningMethod != null) {
                    throw new OutdatedDeobfuscatorException("Widget", "Widget", "getPositioning", "Collision");
                }
                widgetGetPositioningMethod = method.name;
            } else if ((method.access & Opcodes.ACC_PUBLIC) != 0 && method.desc.equals("(L" + WIDGET_POSITIONING_CLASS + ";)V") && isSetter(method, WIDGET_CLASS, widgetPositioningField, "L" + WIDGET_POSITIONING_CLASS + ";")) {
                if (widgetSetPositioningMethod != null) {
                    throw new OutdatedDeobfuscatorException("Widget", "Widget", "setPositioning", "Collision");
                }
                widgetSetPositioningMethod = method.name;
            } else if ((method.access & Opcodes.ACC_PUBLIC) != 0 && method.desc.equals("(L" + WIDGET_MESSAGE_CLASS + ";)V") && method.name.equals(widgetRecieveMessageMethod)) {
                for (AbstractInsnNode insn : method.instructions) {
                    if (insn.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                        continue;
                    }
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;
                    if (!methodInsn.owner.equals(WIDGET_CLASS) || !methodInsn.desc.equals("(L" + WIDGET_MESSAGE_CLASS + ";)V")) {
                        continue;
                    }
                    if (widgetPropagateMessageLocally != null && !widgetPropagateMessageLocally.equals(methodInsn.name)) {
                        throw new OutdatedDeobfuscatorException("Widget", "Widget", "propagateMessageLocally", "Name mismatch");
                    }
                    widgetPropagateMessageLocally = methodInsn.name;
                }
                if (widgetPropagateMessageLocally == null) {
                    throw new OutdatedDeobfuscatorException("Widget", "Widget", "propagateMessageLocally", "Instructions exhausted");
                }
            } else if (method.name.equals(widgetHoverMethod) && method.desc.equals("(FFZ)V")) {
                FieldInsnNode fieldInsn = getNext(method.instructions.getFirst(), Opcodes.GETFIELD);
                if (!fieldInsn.desc.equals("J")) {
                    throw new OutdatedDeobfuscatorException("Widget", "Widget", "lastRegisteredMouseMevement", "Descriptor mismatch");
                }
                remapField(mappingsStream, WIDGET_CLASS, fieldInsn.name, "lastRegisteredMouseMevement", "J");
            }
        }

        if (containsPointMethod == null) {
            throw new IllegalStateException("The deobfuscator for the Widget class is out of date (Did not detect Widget#containsPoint)");
        }
        if (widgetGetHeightMethod == null) {
            throw new IllegalStateException("The deobfuscator for the Widget class is out of date (Did not detect Widget#getHeight)");
        }
        if (widgetSetHeaderColorMethod == null) {
            throw new IllegalStateException("The deobfuscator for the Widget class is out of date (Did not detect Widget#setHeaderColor)");
        }
        if (widgetSetHeaderTitleMethod == null) {
            throw new IllegalStateException("The deobfuscator for the Widget class is out of date (Did not detect Widget#setHeaderTitle)");
        }
        if (widgetGetHeaderTitleMethod == null) {
            throw new IllegalStateException("The deobfuscator for the Widget class is out of date (Did not detect Widget#getHeaderTitle)");
        }
        if (widgetDrawBackgroundMethod == null) {
            throw new IllegalStateException("The deobfuscator for the Widget class is out of date (Did not detect Widget#drawBackground)");
        }
        if (widgetChildrenField == null) {
            throw new OutdatedDeobfuscatorException("Widget", "Widget", "children", "Not matched");
        }
        if (widgetGetPositioningMethod == null) {
            throw new OutdatedDeobfuscatorException("Widget", "Widget", "getPositioning", "Not matched");
        }
        if (widgetSetPositioningMethod == null) {
            throw new OutdatedDeobfuscatorException("Widget", "Widget", "setPositioning", "Not matched");
        }
        if (widgetPropagateMessageLocally == null) {
            throw new OutdatedDeobfuscatorException("Widget", "Widget", "propagateMessageLocally", "Widget#recieveMessage not found");
        }
        if (widgetPropagateMessageLocally.equals(widgetRecieveMessageMethod)) {
            throw new OutdatedDeobfuscatorException("Widget", "Widget", "propagateMessageLocally", "Assertion failed: propagateMessageLocally is recieveMessage");
        }

        String widgetLayoutGetHeightMethod = null;
        String widgetLayoutGetWidthMethod = null;
        String widgetDrawChildrenMethod = null;

        {
            AbstractInsnNode last = sidebarInitializeMethod.instructions.getLast();
            while (last.getOpcode() == -1 || last.getOpcode() == Opcodes.RETURN) {
                last = last.getPrevious();
            }
            if (last.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                throw new OutdatedDeobfuscatorException("Sidebar", "WidgetLayout", "recompute", "Unexpected opcode in sidebar initalize method");
            }
            MethodInsnNode recomputeLayout = (MethodInsnNode) last;
            if (!recomputeLayout.desc.equals("()V")) {
                throw new OutdatedDeobfuscatorException("Sidebar", "WidgetLayout", "recompute", "Unexpected method descriptor");
            }
            widgetLayoutRecomputeMethod = recomputeLayout.name;
            last = last.getPrevious();
            while (last != null) {
                if (last.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                    MethodInsnNode methodInsn = (MethodInsnNode) last;
                    if (methodInsn.owner.equals(widgetLayoutClass)) {
                        if (!methodInsn.desc.equals("()V")) {
                            throw new OutdatedDeobfuscatorException("Sidebar", "WidgetLayout", "newline", "Unexpected method descriptor");
                        }
                        if (widgetLayoutNewlineMethod != null && !widgetLayoutNewlineMethod.equals(methodInsn.name)) {
                            throw new OutdatedDeobfuscatorException("Sidebar", "WidgetLayout", "newline", "Conflicts found");
                        }
                        widgetLayoutNewlineMethod = methodInsn.name;
                    }
                }
                last = last.getPrevious();
            }
            ClassNode sidebarNode = name2Node.get(sidebarClass);
            for (MethodNode method : sidebarNode.methods) {
                if (method.name.equals(widgetGetHeightMethod) && method.desc.equals("()I")) {
                    MethodInsnNode delegateInsn = null;
                    for (AbstractInsnNode insn : method.instructions) {
                        if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                            if (delegateInsn != null) {
                                throw new OutdatedDeobfuscatorException("Sidebar", "WidgetLayout", "getHeight", "Name collision");
                            }
                            delegateInsn = (MethodInsnNode) insn;
                            if (!delegateInsn.owner.equals(widgetLayoutClass)) {
                                throw new OutdatedDeobfuscatorException("Sidebar", "WidgetLayout", "getHeight", "Wrong owner class");
                            }
                            if (!delegateInsn.desc.equals("()I")) {
                                throw new OutdatedDeobfuscatorException("Sidebar", "WidgetLayout", "getHeight", "Invalid descriptor");
                            }
                        }
                    }
                    if (delegateInsn == null) {
                        throw new OutdatedDeobfuscatorException("Sidebar", "WidgetLayout", "getHeight", "Not referenced");
                    }
                    widgetLayoutGetHeightMethod = delegateInsn.name;
                } else if (method.name.equals(getWidgetWidthMethod) && method.desc.equals("()I")) {
                    MethodInsnNode delegateInsn = null;
                    for (AbstractInsnNode insn : method.instructions) {
                        if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                            if (delegateInsn != null) {
                                throw new OutdatedDeobfuscatorException("Sidebar", "WidgetLayout", "getWidth", "Name collision");
                            }
                            delegateInsn = (MethodInsnNode) insn;
                            if (!delegateInsn.owner.equals(widgetLayoutClass)) {
                                throw new OutdatedDeobfuscatorException("Sidebar", "WidgetLayout", "getWidth", "Wrong owner class");
                            }
                            if (!delegateInsn.desc.equals("()I")) {
                                throw new OutdatedDeobfuscatorException("Sidebar", "WidgetLayout", "getWidth", "Invalid descriptor");
                            }
                        }
                    }
                    if (delegateInsn == null) {
                        throw new OutdatedDeobfuscatorException("Sidebar", "WidgetLayout", "getWidth", "Not referenced");
                    }
                    widgetLayoutGetWidthMethod = delegateInsn.name;
                } else if (method.name.equals(widgetDrawMethod) && method.desc.equals("()V")) {
                    MethodInsnNode delegateInsn = null;
                    for (AbstractInsnNode insn : method.instructions) {
                        if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                            if (delegateInsn != null) {
                                throw new OutdatedDeobfuscatorException("Sidebar", "Widget", "drawChildren", "Name collision");
                            }
                            delegateInsn = (MethodInsnNode) insn;
                            if (!delegateInsn.desc.equals("()V")) {
                                throw new OutdatedDeobfuscatorException("Sidebar", "Widget", "drawChildren", "Invalid descriptor");
                            }
                        }
                    }
                    if (delegateInsn == null) {
                        throw new OutdatedDeobfuscatorException("Sidebar", "Widget", "drawChildren", "Not referenced");
                    }
                    widgetDrawChildrenMethod = delegateInsn.name;
                }
            }
        }

        if (widgetLayoutRecomputeMethod == null) {
            throw new OutdatedDeobfuscatorException("Sidebar", "WidgetLayout", "recompute");
        }
        if (widgetLayoutNewlineMethod == null) {
            throw new OutdatedDeobfuscatorException("Sidebar", "WidgetLayout", "newline");
        }
        if (widgetLayoutGetWidthMethod == null) {
            throw new OutdatedDeobfuscatorException("Sidebar", "WidgetLayout", "getWidth");
        }
        if (widgetLayoutGetHeightMethod == null) {
            throw new OutdatedDeobfuscatorException("Sidebar", "WidgetLayout", "getHeight");
        }
        if (widgetDrawChildrenMethod == null) {
            throw new OutdatedDeobfuscatorException("Sidebar", "Widget", "fillDefaultBackground");
        }

        String widgetGetChildrenMethod = null;
        String widgetGetCameraMethod = null;

        for (MethodNode method : widgetClass.methods) {
            if ((method.access & Opcodes.ACC_PUBLIC) == 0) {
                continue;
            }
            if (method.desc.equals("()Ljava/util/Vector;")) {
                if (isGetter(method, WIDGET_CLASS, widgetChildrenField, "Ljava/util/Vector;", false)) {
                    if (widgetGetChildrenMethod != null) {
                        throw new OutdatedDeobfuscatorException("Widget", "Widget", "getChildren", "collision");
                    }
                    widgetGetChildrenMethod = method.name;
                }
            } else if (method.desc.equals("()L" + GDX_CAMERA_CLASS + ";")) {
                if (isGetter(method, WIDGET_CLASS, widgetCameraField, "L" + GDX_CAMERA_CLASS + ";", false)) {
                    if (widgetGetCameraMethod != null) {
                        throw new OutdatedDeobfuscatorException("Widget", "Widget", "getCamera", "collision");
                    }
                    widgetGetCameraMethod = method.name;
                }
            }
        }

        if (widgetGetChildrenMethod == null) {
            throw new OutdatedDeobfuscatorException("Widget", "Widget", "getChildren", "not resolved");
        }
        if (widgetGetCameraMethod == null) {
            throw new OutdatedDeobfuscatorException("Widget", "Widget", "getCamera", "not resolved");
        }

        String widgetInterceptMouseDownMethod = null;
        String widgetMouseDownMethod = null;
        String widgetConsiderRelayoutMethod = null;

        {
            ClassNode gestureListener = name2Node.get(galimulatorGestureListener);
            for (MethodNode method : gestureListener.methods) {
                if (method.name.equals("touchDown") && method.desc.equals("(FFII)Z")) {
                    MethodInsnNode containsPointInsn = null;
                    for (AbstractInsnNode insn : method.instructions) {
                        if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                            MethodInsnNode methodInsn = (MethodInsnNode) insn;
                            if (methodInsn.owner.equals(WIDGET_CLASS) && methodInsn.desc.equals("(Lcom/badlogic/gdx/math/Vector2;)Z")
                                    && methodInsn.name.equals(containsPointMethod)) {
                                if (containsPointInsn != null) {
                                    throw new OutdatedDeobfuscatorException("GestureListener", "Multiple Widget#containsPoint instructions found (touchDown).");
                                }
                                containsPointInsn = methodInsn;
                            }
                        }
                    }
                    if (containsPointInsn == null) {
                        throw new OutdatedDeobfuscatorException("GestureListener", "No Widget#containsPoint instructions found (touchDown).");
                    }
                    AbstractInsnNode nextInsn = containsPointInsn.getNext();
                    while (nextInsn != null) {
                        if (nextInsn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                            MethodInsnNode insn = (MethodInsnNode) nextInsn;
                            if (insn.desc.equals("(FF)Z") && insn.owner.equals(WIDGET_CLASS)) {
                                widgetInterceptMouseDownMethod = insn.name;
                                break;
                            }
                        }
                        nextInsn = nextInsn.getNext();
                    }
                    if (widgetInterceptMouseDownMethod == null) {
                        throw new OutdatedDeobfuscatorException("GestureListener", "Widget", "interceptMouseDown", "Instructions exhausted");
                    }
                    while (nextInsn != null) {
                        if (nextInsn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                            MethodInsnNode insn = (MethodInsnNode) nextInsn;
                            if (insn.desc.equals("(DD)V") && insn.owner.equals(WIDGET_CLASS)) {
                                widgetMouseDownMethod = insn.name;
                                insn = getNext(insn, Opcodes.INVOKEVIRTUAL);
                                if (!insn.owner.equals(WIDGET_CLASS) || !insn.desc.equals("()V")) {
                                    throw new OutdatedDeobfuscatorException("GestureListener", "Widget", "mouseDown", "Invalid owner or descriptor");
                                }
                                widgetConsiderRelayoutMethod = insn.name;
                                break;
                            }
                        }
                        nextInsn = nextInsn.getNext();
                    }
                    if (widgetMouseDownMethod == null) {
                        throw new OutdatedDeobfuscatorException("GestureListener", "Widget", "mouseDown", "Instructions exhausted");
                    }
                } else if (method.name.equals("tap") && method.desc.equals("(FFII)Z")) {
                    // TODO slated for removal I guess, but does it have another probable use in the (near) future?
                    MethodInsnNode containsPointInsn = null;
                    for (AbstractInsnNode insn : method.instructions) {
                        if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                            MethodInsnNode methodInsn = (MethodInsnNode) insn;
                            if (methodInsn.owner.equals(WIDGET_CLASS) && methodInsn.desc.equals("(Lcom/badlogic/gdx/math/Vector2;)Z")
                                    && methodInsn.name.equals(containsPointMethod)) {
                                if (containsPointInsn != null) {
                                    throw new OutdatedDeobfuscatorException("GestureListener", "Multiple Widget#containsPoint instructions found (tap).");
                                }
                                containsPointInsn = methodInsn;
                            }
                        }
                    }
                    if (containsPointInsn == null) {
                        throw new OutdatedDeobfuscatorException("GestureListener", "No Widget#containsPoint instructions found (tap).");
                    }
                }
            }
        }

        if (widgetInterceptMouseDownMethod == null) {
            throw new OutdatedDeobfuscatorException("GestureListener", "Widget", "onMouseDown");
        }

        mappingsStream.append("#START Remap UI-related methods across hierarchy\n");
        for (ClassNode node : nodes) {
            if (isInstanceofInterface(node, widgetMessageRecieverClass)) {
                if (isInstanceofWidget(node)) {
                    if (isInstanceofClass(node, baseButtonClass)) {
                        remapMethod(mappingsStream, node.name, basicButtonSetTextMethod, "setButtonText", "(Ljava/lang/String;)V");
                        remapMethod(mappingsStream, node.name, basicButtonSetColorMethod, "setButtonColor", "(L" + GALCOLOR_CLASS + ";)V");
                        remapMethod(mappingsStream, node.name, baseButtonRenderBackgroundMethod, "renderButtonBackground", "()V");
                    }
                    remapMethod(mappingsStream, node.name, widgetDrawMethod, "draw", "()V");
                    remapMethod(mappingsStream, node.name, widgetDrawHeaderMethod, "drawHeader", "()V");
                    remapMethod(mappingsStream, node.name, getWidgetWidthMethod, "getWidth", "()I");
                    remapMethod(mappingsStream, node.name, widgetGetHeightMethod, "getHeight", "()I");
                    remapMethod(mappingsStream, node.name, getXMethod, "getX", "()D");
                    remapMethod(mappingsStream, node.name, getYMethod, "getY", "()D");
                    remapMethod(mappingsStream, node.name, containsPointMethod, "containsPoint", "(Lcom/badlogic/gdx/math/Vector2;)Z");
                    remapMethod(mappingsStream, node.name, widgetSetHeaderColorMethod, "setHeaderColor", "(Lsnoddasmannen/galimulator/GalColor;)V");
                    remapMethod(mappingsStream, node.name, widgetSetHeaderTitleMethod, "setHeaderTitle", "(Ljava/lang/String;)V");
                    remapMethod(mappingsStream, node.name, widgetGetHeaderTitleMethod, "getHeaderTitle", "()Ljava/lang/String;");
                    remapMethod(mappingsStream, node.name, widgetDrawBackgroundMethod, "drawBackground", "(Lsnoddasmannen/galimulator/GalColor;)V");
                    remapMethod(mappingsStream, node.name, widgetClearChildrenMethod, "clearChildren", "()V");
                    remapMethod(mappingsStream, node.name, widgetAddChildMethod, "addChild", widgetAddChildMethodDescriptor);
                    remapMethod(mappingsStream, node.name, widgetRefreshLayoutMethod, "refreshLayout", "()V");
                    remapMethod(mappingsStream, node.name, widgetDrawChildrenMethod, "drawChildren", "()V");
                    remapMethod(mappingsStream, node.name, widgetInterceptMouseDownMethod, "interceptMouseDown", "(FF)Z");
                    remapMethod(mappingsStream, node.name, widgetGetChildrenMethod, "getChildWidgets", "()Ljava/util/Vector;");
                    remapMethod(mappingsStream, node.name, widgetGetCameraMethod, "getCamera", "()L" + GDX_CAMERA_CLASS + ";");
                    remapMethod(mappingsStream, node.name, widgetGetPositioningMethod, "getPositioning", "()L" + WIDGET_POSITIONING_CLASS + ";");
                    remapMethod(mappingsStream, node.name, widgetSetPositioningMethod, "setPositioning", "(L" + WIDGET_POSITIONING_CLASS + ";)V");
                    remapMethod(mappingsStream, node.name, widgetPropagateMessageLocally, "propagateMessageLocally", "(L" + WIDGET_MESSAGE_CLASS + ";)V");
                    remapMethod(mappingsStream, node.name, widgetOnDisposeMethod, "onDispose", "()V");
                    remapMethod(mappingsStream, node.name, widgetIsPersistentMethod, "isPersistent", "()Z");
                    remapMethod(mappingsStream, node.name, widgetHoverMethod, "hover", "(FFZ)V");
                    remapMethod(mappingsStream, node.name, widgetConsiderRelayoutMethod, "considerRelayout", "()V");
                    remapMethod(mappingsStream, node.name, widgetMouseDownMethod, "mouseDown", "(DD)V");
                    remapMethod(mappingsStream, node.name, widgetDrawDefaultBackgroundMethod, "drawDefaultBackground", "()V");
                }
                remapMethod(mappingsStream, node.name, widgetRecieveMessageMethod, "recieveMessage", "(L" + WIDGET_MESSAGE_CLASS + ";)V");
            } else if (isInstanceofClass(node, widgetLayoutClass)) {
                remapMethod(mappingsStream, node.name, widgetLayoutNewlineMethod, "newline", "()V");
                remapMethod(mappingsStream, node.name, widgetLayoutRecomputeMethod, "recompute", "()V");
                remapMethod(mappingsStream, node.name, widgetLayoutGetHeightMethod, "getHeight", "()I");
                remapMethod(mappingsStream, node.name, widgetLayoutGetWidthMethod, "getWidth", "()I");
            }
        }
        mappingsStream.append("#END Remap UI-related methods across hierarchy\n");

        remapDialogClasses(mappingsStream, settingsDialogClass);
    }

    private void resolveEnumMemberNames(String className, Map<String, String> memberMappings) {
        ClassNode node = name2Node.get(className);
        if (node == null) {
            throw new OutdatedDeobfuscatorException("Unknown", "Class node for " + className + " could not be resolved");
        }
        memberMappings.clear();

        for (MethodNode method : node.methods) {
            if (!method.desc.equals("()V") || !method.name.equals("<clinit>")) {
                continue;
            }
            // It's an overkill solution, but probably will not break anytime soon
            StackWalker.walkStack(node, method, new StackWalkerConsumer() {

                private boolean awaitPutstatic;
                private String lastEnumName;

                @Override
                public void postCalculation(AbstractInsnNode insn, LIFOQueue<StackElement> elem) {
                    if (lastEnumName != null) {
                        if (awaitPutstatic && insn.getOpcode() != -1) {
                            awaitPutstatic = false;
                            lastEnumName = null;
                        } else {
                            awaitPutstatic = true;
                        }
                    }
                }

                @Override
                public void preCalculation(AbstractInsnNode insn, LIFOQueue<StackElement> stack) {
                    if (insn.getOpcode() == -1) {
                        // Filter out pseudo-instructions
                        return;
                    }
                    if (insn.getOpcode() == Opcodes.INVOKESPECIAL) {
                        MethodInsnNode invoked = (MethodInsnNode) insn;
                        if (stack.getSize() < 4 || !invoked.name.equals("<init>")) {
                            // I'm sure that INVOKESPECIAL is only used for the constructor, but let's be on the safe side
                            return;
                        }
                        lastEnumName = ((LdcInsnNode) ((AbstractSource) stack.getDelegateList().get(stack.getSize() - 3).source).getInsn()).cst.toString();
                    } else if (insn.getOpcode() == Opcodes.PUTSTATIC && awaitPutstatic) {
                        FieldInsnNode fieldInsn = ((FieldInsnNode)insn);
                        if (fieldInsn.owner.equals(className)
                                && fieldInsn.desc.equals("L" + className + ";")
                                && memberMappings.put(lastEnumName, fieldInsn.name) != null) {
                            throw new OutdatedDeobfuscatorException("Unknown", "Just overwrote a mapping?");
                        }
                    }
                }
            });
        }

        if (memberMappings.isEmpty()) {
            throw new OutdatedDeobfuscatorException("Unknown", "Cannot find any member fields of the enum. Is it really an enum?");
        }
    }

    private void dumpMethod(MethodNode method) {
        Textifier textifier = new Textifier();
        TraceMethodVisitor visitor = new TraceMethodVisitor(textifier);
        method.accept(visitor);
        textifier.print(new PrintWriter(new OutputStreamWriter(System.err)));
    }

    /**
     * Runs all remapping tasks, however does NOT run the remapper itself.
     *
     * @param mappingsStream Suggested remapper mappings are written to the writer in the tiny v1 format. It appeands, so the header is not written
     */
    public void runAll(@NotNull Writer mappingsStream) throws IOException {
        remapSpaceFields(mappingsStream);
        remapPlayerMethods(mappingsStream);
        remapHotkeys(mappingsStream);
        remapEmpireClass(mappingsStream);
        remapUIClasses(mappingsStream);
        remapActorClasses(mappingsStream);
        remapMapModes(mappingsStream);
        remapNoiseGenerators(mappingsStream);
        remapGalaxyGeneration(mappingsStream);
        remapEmploymentAgency(mappingsStream);
        remapStarMethods(mappingsStream);
        remapRendersystem(mappingsStream);
        remapGenerators(mappingsStream);
    }
}
