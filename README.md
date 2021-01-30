## Gradle plugin for MinecraftForge development

---

Usage:

```groovy
buildscript {
    dependencies {
        classpath 'yopoyka:mcdev:+'
    }
}

//...

apply plugin: 'forge'
// apply after forge
apply plugin: 'mcdev'

//...

minecraft {
    // minecraft configuration
}
// apply transformations after minecraft block is configured
mcdev.applyTransform()
```