package org.stianloader.sml6.starplane.remapping;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;

final class AccessUtil {
    private static final String[] ACCESS_STRING_CACHE_128 = new String[] {
            null,
            "ACC_TRANSIENT",
            "ACC_VARARGS",
            null
    };
    private static final String[] ACCESS_STRING_CACHE_32 = new String[] {
            "ACC_SUPER",
            "ACC_TRANSITIVE", // Somehow I want to debate that. But where would "open" go then?
            "ACC_SYNCHRONIZED",
            "ACC_OPEN"
    };
    private static final String[] ACCESS_STRING_CACHE_32768 = new String[] {
            "ACC_MODULE",
            null,
            null,
            "ACC_MANDATED"
    };
    private static final String[] ACCESS_STRING_CACHE_64 = new String[] {
            "ACC_BRIDGE",
            "ACC_VOLATILE",
            null,
            null
    };
    public static final int ANY_VISIBILITY_MODIFIER = Opcodes.ACC_PUBLIC | Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED;
    @NotNull
    private static final String @NotNull[] ERROR_CODES = new @NotNull String[] {
            "No further information",
            "Access flag already exists",
            "Access flag does not yet exist",
            "Internal transformation failure: Transform is not valid.",
            "Another visibility-controlling access flag already exists."
    };
    static final long ERROR_FLAG_EXISTS = 1L << 32;
    static final long ERROR_FLAG_NOT_EXISTS = ERROR_FLAG_EXISTS + 1;
    static final long ERROR_TRANSFORM_VALIDITY_ERROR = ERROR_FLAG_NOT_EXISTS + 1;
    static final long ERROR_VISBILITY_CONFLICT = ERROR_TRANSFORM_VALIDITY_ERROR + 1;
    static final long INT_LSB = 0xFFFFFFFFL;
    public static final int TARGET_ANY = -1;
    public static final int TARGET_CLASS = 0;
    public static final int TARGET_FIELD = 1;
    public static final int TARGET_METHOD = 2;
    public static final int TARGET_MODULE = 3;

    public static int andAccessTypes(int leftAccessType, int rightAccessType) {
        if (leftAccessType == TARGET_ANY) {
            return rightAccessType;
        } else if (rightAccessType == TARGET_ANY) {
            return leftAccessType;
        } else if (leftAccessType == rightAccessType) {
            return leftAccessType;
        } else {
            throw new IllegalArgumentException("Access categories cannot be combined with AND: " + leftAccessType + ", " + rightAccessType);
        }
    }

    public static final int getAccessCategory(String string) {
        switch (string.toUpperCase(Locale.ROOT)) {
        case "0":
            return TARGET_ANY;
        case "ACC_ABSTRACT":
        case "ABSTRACT":
            return TARGET_ANY;
        case "ACC_ANNOTATION":
        case "ANNOTATION":
            return TARGET_CLASS;
        case "ACC_DEPRECATED":
        case "DEPRECATED":
            return TARGET_ANY;
        case "ACC_ENUM":
        case "ENUM":
            return TARGET_ANY; // Note: this is also applied to enum-generated fields or something like that. So TARGET_ANY is used.
        case "ACC_FINAL":
        case "FINAL":
            return TARGET_ANY;
        case "ACC_INTERFACE":
        case "INTERFACE":
            return TARGET_CLASS;
        case "ACC_NATIVE":
        case "NATIVE":
            return TARGET_METHOD;
        case "ACC_PRIVATE":
        case "PRIVATE":
            return TARGET_ANY;
        case "ACC_PROTECTED":
        case "PROTECTED":
            return TARGET_ANY;
        case "ACC_PUBLIC":
        case "PUBLIC":
            return TARGET_ANY;
        case "ACC_RECORD":
        case "RECORD":
            return TARGET_CLASS;
        case "ACC_STATIC":
        case "STATIC":
            return TARGET_ANY;
        case "ACC_STRICT":
        case "STRICTFP":
            return TARGET_METHOD;
        case "ACC_SYNTHETIC":
        case "SYNTHETIC":
            return TARGET_ANY;
        case "ACC_SUPER":
        case "SUPER":
            return TARGET_CLASS;
        case "ACC_SYNCHRONIZED":
        case "SYNCHRONIZED":
            return TARGET_METHOD;
        case "ACC_TRANSITIVE":
        case "TRANSITIVE":
            return TARGET_FIELD;
        case "ACC_OPEN":
        case "OPEN":
            return TARGET_MODULE;
        case "ACC_BRIDGE":
        case "BRIDGE":
            return TARGET_METHOD;
        case "ACC_VOLATILE":
        case "VOLATILE":
            return TARGET_FIELD;
        case "ACC_VARARGS":
        case "VARARGS":
            return TARGET_METHOD;
        case "ACC_TRANSIENT":
        case "TRANSIENT":
            return TARGET_MODULE;
        case "ACC_MODULE":
        case "MODULE":
            return TARGET_CLASS;
        case "ACC_MANDATED":
        case "MANDATED":
            return TARGET_MODULE;
        default:
            throw new IllegalArgumentException(string);
        }
    }

    @NotNull
    static final String getErrorCode(long result) {
        return ERROR_CODES[(int) (result >> 32)];
    }

    public static final int parseAccess(String string) {
        switch (string.toUpperCase(Locale.ROOT)) {
        case "0":
            return 0;
        case "ACC_ABSTRACT":
        case "ABSTRACT":
            return Opcodes.ACC_ABSTRACT;
        case "ACC_ANNOTATION":
        case "ANNOTATION":
            return Opcodes.ACC_ANNOTATION;
        case "ACC_DEPRECATED":
        case "DEPRECATED":
            return Opcodes.ACC_DEPRECATED;
        case "ACC_ENUM":
        case "ENUM":
            return Opcodes.ACC_ENUM;
        case "ACC_FINAL":
        case "FINAL":
            return Opcodes.ACC_FINAL;
        case "ACC_INTERFACE":
        case "INTERFACE":
            return Opcodes.ACC_INTERFACE;
        case "ACC_NATIVE":
        case "NATIVE":
            return Opcodes.ACC_NATIVE;
        case "ACC_PRIVATE":
        case "PRIVATE":
            return Opcodes.ACC_PRIVATE;
        case "ACC_PROTECTED":
        case "PROTECTED":
            return Opcodes.ACC_PROTECTED;
        case "ACC_PUBLIC":
        case "PUBLIC":
            return Opcodes.ACC_PUBLIC;
        case "ACC_RECORD":
        case "RECORD":
            return Opcodes.ACC_RECORD;
        case "ACC_STATIC":
        case "STATIC":
            return Opcodes.ACC_STATIC;
        case "ACC_STRICT":
        case "STRICTFP":
            return Opcodes.ACC_STRICT;
        case "ACC_SYNTHETIC":
        case "SYNTHETIC":
            return Opcodes.ACC_SYNTHETIC;
        case "ACC_SUPER":
        case "SUPER":
            return Opcodes.ACC_SUPER;
        case "ACC_SYNCHRONIZED":
        case "SYNCHRONIZED":
            return Opcodes.ACC_SYNCHRONIZED;
        case "ACC_TRANSITIVE":
        case "TRANSITIVE":
            return Opcodes.ACC_TRANSITIVE;
        case "ACC_OPEN":
        case "OPEN":
            return Opcodes.ACC_OPEN;
        case "ACC_BRIDGE":
        case "BRIDGE":
            return Opcodes.ACC_BRIDGE;
        case "ACC_VOLATILE":
        case "VOLATILE":
            return Opcodes.ACC_VOLATILE;
        case "ACC_VARARGS":
        case "VARARGS":
            return Opcodes.ACC_VARARGS;
        case "ACC_TRANSIENT":
        case "TRANSIENT":
            return Opcodes.ACC_TRANSIENT;
        case "ACC_MODULE":
        case "MODULE":
            return Opcodes.ACC_MODULE;
        case "ACC_MANDATED":
        case "MANDATED":
            return Opcodes.ACC_MANDATED;
        default:
            throw new IllegalArgumentException(string);
        }
    }

    public static final String stringifyAccess(int access, int type) {
        switch (access) {
        case 0:
            return "0";
        case 32:
            return ACCESS_STRING_CACHE_32[type];
        case 64:
            return ACCESS_STRING_CACHE_64[type];
        case 128:
            return ACCESS_STRING_CACHE_128[type];
        case 32768:
            return ACCESS_STRING_CACHE_32768[type];
        case Opcodes.ACC_ABSTRACT:
            return "ACC_ABSTRACT";
        case Opcodes.ACC_ANNOTATION:
            return "ACC_ANNOTATION";
        case Opcodes.ACC_DEPRECATED:
            return "ACC_DEPRECATED";
        case Opcodes.ACC_ENUM:
            return "ACC_ENUM";
        case Opcodes.ACC_FINAL:
            return "ACC_FINAL";
        case Opcodes.ACC_INTERFACE:
            return "ACC_INTERFACE";
        case Opcodes.ACC_NATIVE:
            return "ACC_NATIVE";
        case Opcodes.ACC_PRIVATE:
            return "ACC_PRIVATE";
        case Opcodes.ACC_PROTECTED:
            return "ACC_PROTECTED";
        case Opcodes.ACC_PUBLIC:
            return "ACC_PUBLIC";
        case Opcodes.ACC_RECORD:
            return "ACC_RECORD";
        case Opcodes.ACC_STATIC:
            return "ACC_STATIC";
        case Opcodes.ACC_STRICT:
            return "ACC_STRICT";
        case Opcodes.ACC_SYNTHETIC:
            return "ACC_SYNTHETIC";
        default:
            return "???";
        }
    }
}
