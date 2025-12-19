package org.stianloader.sml6;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class SML6GradlePlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        target.getConfigurations().resolvable("").configure(configuration -> {
            configuration.getTaskDependencyFromProjectDependency(false, null);
        });
        target.getDependencies().add("", target);
        target.getConfigurations().named("").configure(null);
        // There is nothing to configure by default
    }
}
