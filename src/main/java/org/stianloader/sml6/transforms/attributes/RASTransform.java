package org.stianloader.sml6.transforms.attributes;

import org.gradle.api.Named;
import org.gradle.api.attributes.Attribute;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface RASTransform extends Named {
    /**
     * Alias for {@link #TRANSFORMED_BUILDTIME}.
     */
    public static final String BUILDTIME = "transformed-buildtime";
    public static final String NO_TRANSFORM = "no-transform";
    public static final Attribute<RASTransform> RAS_TRANSFORM_ATTRIBUTE = Attribute.of("org.stianloader.sml6.ras", RASTransform.class);
    /**
     * Alias for {@link #TRANSFORMED_RUNTIME}
     */
    public static final String RUNTIME = "transformed-runtime";
    public static final String TRANSFORM = "transform";
    public static final String TRANSFORMED_BUILDTIME = BUILDTIME;
    public static final String TRANSFORMED_RUNTIME = RUNTIME;
}
