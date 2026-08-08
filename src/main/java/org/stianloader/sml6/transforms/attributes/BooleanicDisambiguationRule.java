package org.stianloader.sml6.transforms.attributes;

import org.gradle.api.attributes.AttributeDisambiguationRule;
import org.gradle.api.attributes.MultipleCandidatesDetails;

public class BooleanicDisambiguationRule implements AttributeDisambiguationRule<Boolean> {
    @Override
    public void execute(MultipleCandidatesDetails<Boolean> details) {
        details.closestMatch(details.getConsumerValue());
        
    }
}
