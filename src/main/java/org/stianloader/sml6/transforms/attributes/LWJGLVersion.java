package org.stianloader.sml6.transforms.attributes;

import org.gradle.api.attributes.Attribute;

public class LWJGLVersion {
    public static final Attribute<Integer> LWJGL_VERSION_ATTRIBUTE = Attribute.of("org.stianloader.sml6.lwjgl", Integer.class);
}
