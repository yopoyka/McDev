package yopoyka.mcdev

import org.gradle.api.artifacts.transform.ArtifactTransform

import java.nio.file.Path
import java.util.function.BiPredicate

class RemapperData {
    public Path mappingsDir
    public String mappingsName
    public Path cacheDir
    public Path bonPath
    public String bonUri = 'https://github.com/yopoyka/BON2/releases/download/custom/BON-2.4.0.CUSTOM-all.jar'
    public BiPredicate<File, ArtifactTransform> shouldRemap = { File file, ArtifactTransform t ->
        // exclude forge dev
        !(file.toPath().toAbsolutePath().toString() =~ /.*[\/\\]net[\/\\]minecraftforge[\/\\]forge[\/\\].*[\/\\]forgeSrc.*\.jar$/)
    }
}
