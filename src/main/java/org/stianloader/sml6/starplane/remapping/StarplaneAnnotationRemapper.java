package org.stianloader.sml6.starplane.remapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypeReference;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeAnnotationNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stianloader.remapper.MemberRef;
import org.stianloader.remapper.Remapper;

public class StarplaneAnnotationRemapper {
    @NotNull
    private static final String INLINED_REFERENCE_SOURCE_MARKER_ANNOTATION = "Lorg/stianloader/starplane/annotations/InlinedReferenceSourceMarker;";

    private static final Logger LOGGER = LoggerFactory.getLogger(StarplaneAnnotationRemapper.class);

    public static void apply(@NotNull ClassNode node, @NotNull Remapper remapper, @NotNull StringBuilder sharedBuilder) {
        MethodNode clinitMethod = null;
        for (MethodNode method : node.methods) {
            if (method.name.equals("<clinit>")) {
                clinitMethod = method;
                break;
            }
        }

        Map<MemberRef, MemberRef> memberMapRequests = new HashMap<>();
        Map<MemberRef, String> memberMapFormat = new HashMap<>();
        Map<MemberRef, String> classMapRequests = new HashMap<>();

        for (FieldNode field : node.fields) {
            Iterable<AnnotationNode> annotations = field.invisibleAnnotations;
            if (annotations == null) {
                continue;
            }
            for (AnnotationNode annotation : annotations) {
                if (annotation.desc.equals("Lde/geolykt/starloader/starplane/annotations/StarplaneReobfuscateReference;")) {
                    if (clinitMethod == null) {
                        throw new IllegalStateException("Illegal bytecode: " + node.name + '.' + field.name + ' ' + field.desc + ": No clinit found");
                    }
                    for (AbstractInsnNode insn : clinitMethod.instructions) {
                        if (insn.getOpcode() == Opcodes.PUTSTATIC) {
                            FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                            if (!fieldInsn.desc.equals("Ljava/lang/String;") || !field.name.equals(fieldInsn.name)) {
                                continue;
                            }
                            LdcInsnNode ldc = (LdcInsnNode) fieldInsn.getPrevious();
                            String oldValue = ldc.cst.toString();
                            if (oldValue == null) {
                                throw new InternalError();
                            }
                            ldc.cst = StarplaneAnnotationRemapper.remapReference(remapper, oldValue, sharedBuilder);
                        }
                    }
                    break;
                } else if (annotation.desc.equals("Lde/geolykt/starloader/starplane/annotations/RemapClassReference;")) {
                    if (annotation.values == null || annotation.values.size() == 0) {
                        StarplaneAnnotationRemapper.LOGGER.error("Field {}.{}:{} is annotated with de/geolykt/starloader/starplane/annotations/RemapClassReference, but neither the 'name' nor the 'type' value of the annotation is set.", node.name, field.name, field.desc);
                        break;
                    }
                    if (annotation.values.size() == 4) {
                        StarplaneAnnotationRemapper.LOGGER.error("Field {}.{}:{} is annotated with de/geolykt/starloader/starplane/annotations/RemapClassReference, but both the 'name' and the 'type' value of the annotation is set. Consider only setting one of these values.", node.name, field.name, field.desc);
                        break;
                    }
                    String typeName;
                    if (annotation.values.get(0).equals("name")) {
                        typeName = ((String) annotation.values.get(1)).replace('.', '/');
                        annotation.values.set(1, Remapper.remapInternalName(remapper.getLookup(), typeName, sharedBuilder));
                    } else if (annotation.values.get(0).equals("type")) {
                        typeName = ((Type) annotation.values.get(1)).getInternalName();
                        // Type remapping is thankfully performed by stianloader-remapper already
                    } else {
                        StarplaneAnnotationRemapper.LOGGER.error("Erroneous annotation key: " + annotation.values.get(0) + " for RemapClassReference. Are you depending on the wrong starplane-annotations version?");
                        break;
                    }
                    classMapRequests.put(new MemberRef(node.name, field.name, field.desc), typeName);
                } else if (annotation.desc.equals("Lde/geolykt/starloader/starplane/annotations/RemapMemberReference;")) {
                    if (annotation.values == null) {
                        StarplaneAnnotationRemapper.LOGGER.error("Field {}.{}:{} is annotated with de/geolykt/starloader/starplane/annotations/RemapMemberReference, but does not define any of the required values.", node.name, field.name, field.desc);
                        break;
                    }
                    if (annotation.values.size() >= 10) {
                        StarplaneAnnotationRemapper.LOGGER.error("Field {}.{}:{} is annotated with de/geolykt/starloader/starplane/annotations/RemapMemberReference, but more than the required values of the annotation is set. Consider removing duplicates.", node.name, field.name, field.desc);
                        break;
                    }

                    String typeName = null;
                    String memberName = null;
                    String memberDesc = null;
                    String format = null;

                    int ordinalTypeName = 0;
                    int ordinalMemberName = 0;
                    int ordinalMemberDesc = 0;

                    for (int i = 0; i < annotation.values.size(); i += 2) {
                        String valueName = ((String) annotation.values.get(i));
                        if (valueName.equals("ownerType")) {
                            typeName = ((Type) annotation.values.get(i + 1)).getInternalName();
                        } else if (valueName.equals("owner")) {
                            typeName = ((String) annotation.values.get(i + 1)).replace('.', '/');
                            ordinalTypeName = i + 1;
                        } else if (valueName.equals("name")) {
                            memberName = (String) annotation.values.get(i + 1);
                            ordinalMemberName = i + 1;
                        } else if (valueName.equals("desc")) {
                            if (memberDesc != null) {
                                StarplaneAnnotationRemapper.LOGGER.error("Field {}.{}:{} is annotated with de/geolykt/starloader/starplane/annotations/RemapMemberReference, but multiple values contain descriptor-giving values. Consider removing duplicated.", node.name, field.name, field.desc);
                                break;
                            }
                            memberDesc = (String) annotation.values.get(i + 1);
                            ordinalMemberDesc = i + 1;
                        } else if (valueName.equals("descType")) {
                            if (memberDesc != null) {
                                StarplaneAnnotationRemapper.LOGGER.error("Field {}.{}:{} is annotated with de/geolykt/starloader/starplane/annotations/RemapMemberReference, but multiple values contain descriptor-giving values. Consider removing duplicated.", node.name, field.name, field.desc);
                                break;
                            }
                            memberDesc = ((Type) annotation.values.get(i + 1)).getDescriptor();
                        } else if (valueName.equals("methodDesc")) {
                            AnnotationNode methodDesc = (AnnotationNode) annotation.values.get(i + 1);
                            int args;
                            int ret;
                            if (methodDesc.values.get(0).equals("args")) {
                                args = 1;
                                ret = 3;
                            } else {
                                ret = 1;
                                args = 3;
                            }
                            String argDesc = "";
                            List<?> arglist = (List<?>) methodDesc.values.get(args);
                            for (int j = 0; j < arglist.size(); j++) {
                                Type arg = (Type) arglist.get(j);
                                if (arg == null) {
                                    throw new AssertionError();
                                }
                                argDesc += arg.getDescriptor();
                            }
                            if (memberDesc != null) {
                                StarplaneAnnotationRemapper.LOGGER.error("Field {}.{}:{} is annotated with de/geolykt/starloader/starplane/annotations/RemapMemberReference, but multiple values contain descriptor-giving values. Consider removing duplicated.", node.name, field.name, field.desc);
                                break;
                            }
                            memberDesc = "(" + argDesc + ")" + ((Type) methodDesc.values.get(ret)).getDescriptor();
                        } else if (valueName.equals("format")) {
                            format = ((String[]) annotation.values.get(i + 1))[1];
                        } else {
                            StarplaneAnnotationRemapper.LOGGER.error("Erroneous annotation value: {} for RemapMemberReference. Are you depending on the wrong starplane-annotations version?", valueName);
                            break;
                        }
                    }
                    if (typeName == null) {
                        StarplaneAnnotationRemapper.LOGGER.error("Field {}.{}:{} is annotated with de/geolykt/starloader/starplane/annotations/RemapMemberReference, but neither the 'owner' nor the 'ownerType' value of the annotation is set. Consider setting one of these values.", node.name, field.name, field.desc);
                        break;
                    }

                    MemberRef targetTriple = new MemberRef(typeName, Objects.requireNonNull(memberName, "memberName == null"), Objects.requireNonNull(memberDesc, "memberDesc == null"));
                    MemberRef fieldTriple = new MemberRef(node.name, field.name, field.desc);
                    memberMapFormat.put(fieldTriple, format);
                    memberMapRequests.put(fieldTriple, targetTriple);

                    if (ordinalTypeName != 0) {
                        annotation.values.set(ordinalTypeName, remapper.getLookup().getRemappedClassName(typeName));
                    }

                    if (ordinalMemberName != 0) {
                        if (memberDesc.codePointAt(0) == '(') {
                            annotation.values.set(ordinalMemberName, remapper.getLookup().getRemappedMethodName(typeName, memberName, memberDesc));
                        } else {
                            annotation.values.set(ordinalMemberName, remapper.getLookup().getRemappedFieldName(typeName, memberName, memberDesc));
                        }
                    }

                    if (ordinalMemberDesc != 0) {
                        if (memberDesc.codePointAt(0) == '(') {
                            annotation.values.set(ordinalMemberDesc, Remapper.getRemappedMethodDescriptor(remapper.getLookup(), memberDesc, sharedBuilder));
                        } else {
                            annotation.values.set(ordinalMemberDesc, Remapper.getRemappedFieldDescriptor(remapper.getLookup(), memberDesc, sharedBuilder));
                        }
                    }
                }
            }
        }

        for (MethodNode method : node.methods) {
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn.getOpcode() == Opcodes.INVOKESTATIC) {
                    MethodInsnNode minsn = (MethodInsnNode) insn;
                    if (!minsn.owner.equals("de/geolykt/starloader/starplane/annotations/ReferenceSource")) {
                        continue;
                    }
                    if (!minsn.name.equals("getStringValue") || !minsn.desc.equals("()Ljava/lang/String;")) {
                        continue;
                    }
                } else if (insn.getOpcode() == Opcodes.LDC) {
                    List<TypeAnnotationNode> insnAnnots = insn.invisibleTypeAnnotations;
                    if (insnAnnots == null || insnAnnots.isEmpty()) {
                        continue;
                    }

                    insnSkipBlock: {
                        for (TypeAnnotationNode insnAnnot : insnAnnots) {
                            if (insnAnnot.desc.equals(StarplaneAnnotationRemapper.INLINED_REFERENCE_SOURCE_MARKER_ANNOTATION)) {
                                break insnSkipBlock;
                            }
                        }
                        continue;
                    }
                } else {
                    continue;
                }

                AbstractInsnNode nextInsn = insn.getNext();
                while (nextInsn != null && (nextInsn.getOpcode() == -1 || nextInsn.getOpcode() == Opcodes.ALOAD)) {
                    nextInsn = nextInsn.getNext();
                }
                if (nextInsn == null) {
                    StarplaneAnnotationRemapper.LOGGER.error("Method {}.{} {} contains a rouge ReferenceSource.getStringValue() call.", node.name, method.name, method.desc);
                    break;
                }
                if (nextInsn.getOpcode() != Opcodes.PUTSTATIC && nextInsn.getOpcode() != Opcodes.PUTFIELD) {
                    StarplaneAnnotationRemapper.LOGGER.error("Method {}.{} {} contains a call to ReferenceSource.getStringValue() that is not immediately assigned to a field.", node.name, method.name, method.desc);
                    continue;
                }
                MemberRef assignmentTriple = new MemberRef(((FieldInsnNode) nextInsn).owner, ((FieldInsnNode) nextInsn).name, ((FieldInsnNode) nextInsn).desc);
                String cl = classMapRequests.get(assignmentTriple);
                String replacementLdc;
                if (cl != null) {
                    replacementLdc = Remapper.remapInternalName(remapper.getLookup(), cl, sharedBuilder);
                } else {
                    MemberRef member = memberMapRequests.get(assignmentTriple);
                    String format = memberMapFormat.get(assignmentTriple);
                    if (member == null || format == null) {
                        StarplaneAnnotationRemapper.LOGGER.error("Method {}.{} {} contains a call to ReferenceSource.getStringValue() that is assigned to {}.{} {} which is not annotated with a starplane remapping annotation. (Note: this feature does not work across classes!)", node.name, method.name, method.desc, assignmentTriple.getOwner(), assignmentTriple.getName(), assignmentTriple.getDesc());
                        continue;
                    }
                    String remappedOwner = Remapper.remapInternalName(remapper.getLookup(), member.getOwner(), sharedBuilder);
                    String remappedName;
                    String remappedDesc;
                    boolean isMethod;
                    if (member.getDesc().codePointAt(0) == '(') {
                        // Method
                        remappedName = remapper.getLookup().getRemappedMethodName(member.getOwner(), member.getName(), member.getDesc());
                        remappedDesc = Remapper.getRemappedMethodDescriptor(remapper.getLookup(), member.getDesc(), sharedBuilder);
                        isMethod = true;
                    } else {
                        // Field
                        remappedName = remapper.getLookup().getRemappedFieldName(member.getOwner(), member.getName(), member.getDesc());
                        remappedDesc = Remapper.getRemappedFieldDescriptor(remapper.getLookup(), member.getDesc(), sharedBuilder);
                        isMethod = false;
                    }
                    if (format.equals("OWNER")) {
                        replacementLdc = remappedOwner;
                    } else if (format.equals("NAME")) {
                        replacementLdc = remappedName;
                    } else if (format.equals("DESCRIPTOR")) {
                        replacementLdc = remappedDesc;
                    } else if (format.equals("COMBINED_LEGACY")) {
                        if (isMethod) {
                            replacementLdc = remappedOwner + "." + remappedName + remappedDesc;
                        } else {
                            replacementLdc = remappedOwner + "." + remappedName + " " + remappedDesc;
                        }
                    } else {
                        StarplaneAnnotationRemapper.LOGGER.error("Method {}.{} {} contains a call to ReferenceSource.getStringValue() that is assigned to {}.{} {} which uses an unsupported format. (Are you using the right version of starplane-annotations?)", node.name, method.name, method.desc, assignmentTriple.getOwner(), assignmentTriple.getName(), assignmentTriple.getDesc());
                        continue;
                    }
                }

                LdcInsnNode newInsn = new LdcInsnNode(replacementLdc);
                newInsn.invisibleTypeAnnotations = new ArrayList<>();
                newInsn.invisibleTypeAnnotations.add(new TypeAnnotationNode(TypeReference.CAST << 24, null, StarplaneAnnotationRemapper.INLINED_REFERENCE_SOURCE_MARKER_ANNOTATION));
                method.instructions.set(insn, newInsn);
                insn = nextInsn;
            }
        }
    }

    @NotNull
    private static String remapReference(@NotNull Remapper remapper, @NotNull String string, @NotNull StringBuilder sharedBuilder) {
        int indexofDot = string.indexOf('.');
        if (indexofDot == -1) {
            return Remapper.remapInternalName(remapper.getLookup(), string, sharedBuilder);
        } else {
            StringBuilder builder = new StringBuilder();
            String methodOrField = string.substring(indexofDot + 1);
            String ownerName = string.substring(0, indexofDot);
            builder.append(Remapper.remapInternalName(remapper.getLookup(), ownerName, sharedBuilder));
            builder.append('.');
            int indexofSpace = methodOrField.indexOf(' ');
            if (indexofSpace == -1) {
                // Method
                int indexofBracket = methodOrField.indexOf('(');
                String methodName = methodOrField.substring(0, indexofBracket);
                String methodDesc = methodOrField.substring(indexofBracket);
                methodName = remapper.getLookup().getRemappedMethodName(ownerName, methodName, methodDesc);
                methodDesc = Remapper.getRemappedMethodDescriptor(remapper.getLookup(), methodDesc, sharedBuilder);
                builder.append(methodName);
                builder.append(methodDesc);
            } else {
                // Field
                String fieldName = methodOrField.substring(0, indexofSpace);
                String fieldDesc = methodOrField.substring(++indexofSpace);
                fieldName = remapper.getLookup().getRemappedFieldName(ownerName, fieldName, fieldDesc);
                fieldDesc = Remapper.getRemappedFieldDescriptor(remapper.getLookup(), fieldDesc, sharedBuilder);
                builder.append(fieldName);
                builder.append(' ');
                builder.append(fieldDesc);
            }
            return builder.toString();
        }
    }
}
