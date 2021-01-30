package yopoyka.mcdev

import org.gradle.api.Project
import org.gradle.api.artifacts.transform.ArtifactTransform

import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicInteger

class RemapTransform extends ArtifactTransform {
    public static Project project
    public static RemapperData data
    private static AtomicInteger count = new AtomicInteger(0)

    @Override
    List<File> transform(File file) {
        if (!data.shouldRemap.test(file, this))
            return Collections.singletonList(file)

        def sum = hash(file.toPath())
        def cachePath = data.cacheDir.resolve("${data.mappingsName}_${sum}").toAbsolutePath()
        println("cachePath $file $cachePath")
        if (Files.notExists(cachePath)) {
            println("notExists $cachePath $file")
            Files.createDirectories(data.cacheDir)

            def t = project.tasks.create('__remap__' + count.getAndIncrement(), org.gradle.api.tasks.JavaExec.class)
            t.classpath(data.bonPath)
            t.setMain('com.github.parker8283.bon2.BON2Impl')
            t.setArgs([
                    file.toPath().toAbsolutePath().toString(),
                    cachePath.toString(),
                    data.mappingsName,
                    data.mappingsDir.toAbsolutePath().toString(),
            ])
            t.execute()
        }

        def link = outputDirectory.toPath().resolve("${data.mappingsName}_${file.name}")
        return Collections.singletonList(Files.createSymbolicLink(link, cachePath).toFile())
    }

    String hash(Path file) {
        def digest = MessageDigest.getInstance("sha1")
        digest.update(Files.readAllBytes(file))
        return new BigInteger(1, digest.digest()).toString(16)
    }
}
