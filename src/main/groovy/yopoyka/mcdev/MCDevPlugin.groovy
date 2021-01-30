package yopoyka.mcdev

import org.gradle.api.Plugin
import org.gradle.api.Project

class MCDevPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create(MCDevExtension.NAME, MCDevExtension.class, project)
    }
}
