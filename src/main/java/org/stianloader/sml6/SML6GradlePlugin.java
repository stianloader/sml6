package org.stianloader.sml6;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

public class SML6GradlePlugin implements Plugin<Project> {

    @NotNull
    public static final String DEFAULT_TASK_GROUP = "SML6";

    @Override
    public void apply(Project target) {
        // There is nothing to configure by default
    }
}
