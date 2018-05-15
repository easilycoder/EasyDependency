package tech.easily.dependency

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency

/**
 * dynamic dependency resolve,using for change the dependency of the project that apply this plugin easily between Module(Project)Dependency and AAR dependency
 *
 * 1.apply this plugin to the submodule which usually is a library for other module or main application
 * 2.add the extension config with name "dynamicDependency",just like
 * dynamicDependency{*
 *     debuggable=true
 *     groupId="tech.easily"
 *     artifactId="lib"
 *     version="0.0.1"
 *}*
 * when the config of debuggable is true, all the modules/application that depends on this project may config the dependency as ProjectDependency,just like:
 *      compile (:projectPath)
 * otherwise,all the modules/application that depends on this project may config the dependency as an aar dependency,just like:
 *      compile groupId:artifactId:version
 *
 */
class DependencyResolvePlugin implements Plugin<Project> {

    @Override
    void apply(Project targetProject) {
        if (targetProject == targetProject.rootProject) {
            throw new IllegalStateException("can not applied DependencyResolvePlugin to root project")
        }
        // add the extension config
        NamedDomainObjectContainer<DependencyResolveExt> dependencyResolveContainer = targetProject.container(DependencyResolveExt.class)
        targetProject.extensions.add("dynamicDependency", dependencyResolveContainer)

        targetProject.afterEvaluate {
            Map<Project, DependencyResolveExt> resolveExtMap = new HashMap<>()
            targetProject.configurations.all { Configuration configuration ->
                if (configuration.dependencies.size() == 0) {
                    return
                }
                configuration.dependencies.all { dependency ->
                    if (dependency instanceof DefaultProjectDependency) {
                        def projectName = dependency.dependencyProject.name
                        def dependencyResolveExt = dependencyResolveContainer.find {
                            it.name == projectName
                        }
                        if (dependencyResolveExt != null && !dependencyResolveExt.debuggable) {
                            resolveExtMap.put(dependency.dependencyProject, dependencyResolveExt)
                        }
                    }
                }
            }
            targetProject.configurations.all {
                resolutionStrategy {
                    dependencySubstitution {
                        resolveExtMap.each { key, value ->
                            substitute project("${key.path}") with module("${value.groupId}:${getArtifactName(key, value.artifactId)}:${value.version}")
                        }
                    }
                }
            }
        }
    }

    static def getArtifactName(Project project, String name) {
        if (name == null || name == "") {
            return project.name
        }
        return name
    }
}