package org.stianloader.sml6.transforms.attributes;

import org.gradle.api.attributes.AttributeDisambiguationRule;
import org.gradle.api.attributes.MultipleCandidatesDetails;

public class RASDisambiguationRule implements AttributeDisambiguationRule<RASTransform> {

    @Override
    public void execute(MultipleCandidatesDetails<RASTransform> details) {
        details.closestMatch(details.getConsumerValue());
    }

}
