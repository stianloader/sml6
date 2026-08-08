package org.stianloader.sml6.transforms.attributes;

import org.gradle.api.attributes.AttributeCompatibilityRule;
import org.gradle.api.attributes.CompatibilityCheckDetails;

public class BooleanicArtifactCompatibilityRule implements AttributeCompatibilityRule<Boolean> {
    @Override
    public void execute(CompatibilityCheckDetails<Boolean> details) {
        if (!details.getConsumerValue().equals(details.getProducerValue())) {
            details.incompatible();
        } else {
            details.compatible();
        }
    }
}
