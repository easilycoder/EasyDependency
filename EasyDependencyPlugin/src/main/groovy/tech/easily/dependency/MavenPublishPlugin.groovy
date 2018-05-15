package tech.easily.dependency

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.maven.MavenDeployment
import org.gradle.api.plugins.MavenPlugin
import org.gradle.plugins.signing.SigningPlugin

/**
 * a plugin use to uploadArchives
 */
class MavenPublishPlugin implements Plugin<Project> {

    @Override
    void apply(Project targetProject) {

        if (targetProject == targetProject.rootProject || targetProject.plugins.hasPlugin("com.android.application") || targetProject.name == "app") {
            targetProject.logger.warn("only the library module will apply the plugin:MavenPublishPlugin ")
            return
        }

        // add the needed plugin
        targetProject.plugins.apply(MavenPlugin)
        targetProject.plugins.apply(SigningPlugin)

        MavenPublishExt publishExt = targetProject.extensions.create("mavenPublish", MavenPublishExt)
        targetProject.afterEvaluate {
            // 添加上传构件的task，并定义task的依赖关系
            targetProject.uploadArchives {
                repositories {
                    mavenDeployer {
                        beforeDeployment {
                            if (publishExt.version == "" || publishExt.version == null) {
                                throw new IllegalArgumentException("the version property in mavenPublish must not be null")
                            }
                            { MavenDeployment deployment -> signing.signPom(deployment) }
                        }
                        pom.groupId = publishExt.groupId
                        pom.artifactId = getArtifactName(targetProject, publishExt.artifactId)
                        pom.version = publishExt.version

                        repository(url: publishExt.releaseRepo) {
                            authentication(userName: publishExt.userName, password: publishExt.password)
                        }
                        snapshotRepository(url: publishExt.snapshotRepo) {
                            authentication(userName: publishExt.userName, password: publishExt.password)
                        }

                        pom.project {
                            name getArtifactName(targetProject, publishExt.artifactId)
                            packaging getPackageType(targetProject)
                        }
                    }
                }
            }

            targetProject.signing {
                required {
                    isReleaseBuild(publishExt.version) && targetProject.gradle.taskGraph.hasTask("uploadArchives")
                }
                sign targetProject.configurations.archives
            }
        }
    }

    static def getPackageType(Project project) {
        return isAndroidLibrary(project) ? "aar" : "jar"
    }

    static def isAndroidLibrary(project) {
        return project.getPlugins().hasPlugin('com.android.application') || project.getPlugins().hasPlugin('com.android.library')
    }

    static def isReleaseBuild(String version) {
        return version != null && !version.contains("SNAPSHOT")
    }

    static def getArtifactName(Project project, String name) {
        if (name == null || name == "") {
            return project.name
        }
        return name
    }
}