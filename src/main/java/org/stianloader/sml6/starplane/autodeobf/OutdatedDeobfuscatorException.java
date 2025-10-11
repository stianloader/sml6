package org.stianloader.sml6.starplane.autodeobf;

public class OutdatedDeobfuscatorException extends IllegalStateException {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 4918448241521596428L;

    public OutdatedDeobfuscatorException(String deobfuscator, String detailMessage) {
        super("The deobfuscator for " + deobfuscator + " is outdated (" + detailMessage + ")");
    }

    public OutdatedDeobfuscatorException(String deobfuscator, String owner, String field) {
        super("The deobfuscator for " + deobfuscator + " is outdated (cannot resolve " + owner + "#" + field + ")");
    }

    public OutdatedDeobfuscatorException(String deobfuscator, String owner, String field, String detail) {
        super("The deobfuscator for " + deobfuscator + " is outdated (cannot resolve " + owner + "#" + field + "): " + detail);
    }
}
