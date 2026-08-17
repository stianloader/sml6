package org.stianloader.sml6.tasks.config;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.gradle.internal.metaobject.DynamicInvokeResult;
import org.gradle.internal.metaobject.PropertyAccess;
import org.jspecify.annotations.Nullable;
import org.stianloader.sml6.starplane.remapping.MIOContainerFormat.MappingContainer;

import net.fabricmc.mappingio.format.MappingFormat;

final class MappingExtraProperties implements PropertyAccess {
    static final MappingExtraProperties INSTANCE = new MappingExtraProperties();

    private final Map<String, Object> properties = new HashMap<>();

    {
        for (MappingFormat format : MappingFormat.values()) {
            this.properties.put(format.name().toLowerCase(Locale.ROOT), format);
        }

        // Define commonly used aliases
        this.properties.put("enigma", MappingFormat.ENIGMA_DIR);
        this.properties.put("tiny", MappingFormat.TINY_FILE);
        this.properties.put("tiny_2", MappingFormat.TINY_2_FILE);
        this.properties.put("tiny_v2", MappingFormat.TINY_2_FILE);
        this.properties.put("tiny2", MappingFormat.TINY_2_FILE);
        this.properties.put("tinyv2", MappingFormat.TINY_2_FILE);

        for (MappingContainer container : MappingContainer.values()) {
            this.properties.put(container.name().toLowerCase(Locale.ROOT), container);
        }
    }

    @Override
    public Map<String, ? extends @Nullable Object> getProperties() {
        return this.properties;
    }

    @Override
    public boolean hasProperty(String name) {
        return this.properties.containsKey(name.toLowerCase(Locale.ROOT));
    }

    @Override
    public DynamicInvokeResult tryGetProperty(String name) {
        Object o = this.properties.get(name.toLowerCase(Locale.ROOT));
        if (o == null) {
            return DynamicInvokeResult.notFound();
        } else {
            return DynamicInvokeResult.found(o);
        }
    }

    @Override
    public DynamicInvokeResult trySetProperty(String name, @Nullable Object value) {
        return DynamicInvokeResult.notFound();
    }

    @Override
    public DynamicInvokeResult trySetPropertyWithoutInstrumentation(String name, @Nullable Object value) {
        return DynamicInvokeResult.notFound();
    }
}
