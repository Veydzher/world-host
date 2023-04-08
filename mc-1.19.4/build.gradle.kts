plugins {
    id("java")
    id("fabric-loom") version "1.1.+"
    id("io.github.juuxel.loom-quiltflower") version "1.8.0"
}

group = "io.github.gaming32.world-host"
version = "0.2+1.19.4"

repositories {
    mavenCentral()

    maven("https://maven.fabricmc.net/")

    maven("https://maven.terraformersmc.com/releases")

    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }

    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")

    maven {
        name = "ParchmentMC"
        url = uri("https://maven.parchmentmc.org")
    }

    maven("https://jitpack.io")
}

dependencies {
    minecraft("com.mojang:minecraft:1.19.4")
    @Suppress("UnstableApiUsage")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.19.3:2023.03.12@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:0.14.14")

    implementation(project(":common"))
    include(project(":common"))

    modImplementation("com.terraformersmc:modmenu:6.1.0-rc.4") {
        exclude(group = "net.fabricmc.fabric-api")
        exclude(group = "net.fabricmc")
    }

    modImplementation("maven.modrinth:midnightlib:1.3.0-fabric")
    include("maven.modrinth:midnightlib:1.3.0-fabric")

    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.1.0")

    modImplementation("com.github.LlamaLad7:MixinExtras:0.2.0-beta.6")
    include("com.github.LlamaLad7:MixinExtras:0.2.0-beta.6")
    annotationProcessor("com.github.LlamaLad7:MixinExtras:0.2.0-beta.6")
}

loom {
    runs {
        runConfigs.configureEach {
            isIdeConfigGenerated = true
        }
    }
}

tasks {
    processResources {
        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }

    remapJar {
        archiveBaseName.set("world-host")
    }
}