package yopoyka.mcdev

import org.gradle.api.Project
import org.gradle.api.attributes.Attribute

import java.nio.file.Files
import java.nio.file.Paths

class MCDevExtension {
    private Project project
    public static String NAME = 'mcdev'
    public String cachePath
    public RemapperData remapper

    def curseMaven() {
        return project.repositories.maven {
            name 'curseMaven'
            url 'https://cursemaven.com/'
        }
    }

    MCDevExtension(Project project) {
        this.project = project
        cachePath = project.gradle.gradleUserHomeDir.toPath().resolve('caches').resolve('yopoyka_mcdev').toString()
        remapper = new RemapperData()
    }

    def getProject() {
        project
    }

    def applyTransform() {
        remapper.mappingsDir = remapper.mappingsDir ?: project.gradle.gradleUserHomeDir.toPath().resolve(
                project.minecraft.mappingsChannel == null
                        ? "caches/project.minecraft/net/minecraftforge/forge/$project.minecraft.apiVersion/unpacked/conf"
                        : "caches/project.minecraft/de/oceanlabs/mcp/mcp_$project.minecraft.mappingsChannel/$project.minecraft.mappingsVersion"
        )
        remapper.mappingsName = remapper.mappingsName ?: (project.minecraft.mappingsChannel == null ? project.minecraft.apiVersion : project.minecraft.mappings)
        remapper.cacheDir = Paths.get(cachePath).resolve('remapped')
        remapper.bonPath = remapper.bonPath ?: Paths.get(cachePath).resolve('bon2.jar')
        RemapTransform.project = project
        RemapTransform.data = remapper

        if (Files.notExists(remapper.bonPath)) {
            Files.createDirectories(remapper.bonPath.getParent())
            def is = remapper.bonUri.toURL().openStream()
            println 'Downloading BON2...'
            Files.copy(is, remapper.bonPath)
            println 'BON2 downloaded'
            is.close()
        }

        project.task('deleteBon', {
            doLast {
                println 'Deleting BON2'
                Files.deleteIfExists(remapper.bonPath)
            }
        })
        if (Files.exists(remapper.mappingsDir)) {
            def artifactType = Attribute.of('artifactType', String)
            def remapped = Attribute.of('remappedSrgToMapping', Boolean)
            project.dependencies {
                it
                attributesSchema {
                    attribute(remapped)
                }
                artifactTypes.getByName('jar') {
                    attributes.attribute(remapped, false)
                }
            }

            project.configurations.all {
                project.afterEvaluate {
                    if (canBeResolved)
                        attributes.attribute(remapped, true)
                }
            }

            project.dependencies {
                registerTransform {
                    artifactTransform(RemapTransform.class)
                    from.attribute(remapped, false).attribute(artifactType, 'jar')
                    to.attribute(remapped, true).attribute(artifactType, 'jar')
                }
            }
        } else {
            project.logger.error('========================================================================================')
            project.logger.error('Mappings not found. No transformations will be performed. Run setupDecompWorkspace first')
            project.logger.error('========================================================================================')
        }
    }
}
