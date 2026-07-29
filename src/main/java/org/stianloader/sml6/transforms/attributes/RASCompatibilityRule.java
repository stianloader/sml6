package org.stianloader.sml6.transforms.attributes;

import org.gradle.api.attributes.AttributeCompatibilityRule;
import org.gradle.api.attributes.CompatibilityCheckDetails;

public class RASCompatibilityRule implements AttributeCompatibilityRule<RASTransform> {
    @Override
    public void execute(CompatibilityCheckDetails<RASTransform> details) {
        if (!details.getConsumerValue().equals(details.getProducerValue())) {
            details.incompatible();
        } else {
            details.compatible();
        }
    }
}
