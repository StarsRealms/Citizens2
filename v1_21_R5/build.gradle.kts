import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    id("buildlogic.java-conventions")
    id("io.papermc.paperweight.userdev")
    id("com.gradleup.shadow")
}
paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION

dependencies {
    implementation(project(":citizens-main"))
    paperweight.devBundle("com.starsrealms.nylon", "1.21.8-R0.1-20250828.033405-1")
}

tasks.shadowJar {
    archiveClassifier.set("")
    relocate("net.kyori", "clib.net.kyori")
    relocate("net.byteflux.libby", "clib.net.byteflux.libby")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

description = "citizens"
