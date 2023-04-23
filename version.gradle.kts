plugins {
    java
    id("xyz.deftu.gradle.multiversion")
    id("xyz.deftu.gradle.tools")
    id("xyz.deftu.gradle.tools.blossom")
    id("xyz.deftu.gradle.tools.minecraft.loom")
    id("xyz.deftu.gradle.tools.minecraft.releases")
    id("xyz.deftu.gradle.tools.shadow")
    id("io.github.juuxel.loom-quiltflower") version "1.8.0"
}

version = "${modData.version}+${mcData.versionStr}-${mcData.loader.name}"

repositories {
    maven {
        name = "ParchmentMC"
        url = uri("https://maven.parchmentmc.org")
    }

    maven("https://maven.terraformersmc.com/releases")

    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")

    maven("https://jitpack.io")
}

val bundle: Configuration by configurations.creating {
    configurations.getByName(if (mcData.isFabric) "include" else "shade").extendsFrom(this)
}

dependencies {
    fun includeImplementation(dependency: Any) {
        implementation(dependency)
        bundle(dependency)
    }

    @Suppress("UnstableApiUsage")
    mappings(loom.layered {
        officialMojangMappings()
        when {
            mcData.version >= 1_19_03 -> "1.19.3:2023.03.12"
            mcData.version >= 1_19_00 -> "1.19.2:2022.11.27"
            mcData.version >= 1_18_00 -> "1.18.2:2022.11.06"
            mcData.version >= 1_17_00 -> "1.17.1:2021.12.12"
            mcData.version >= 1_16_00 -> "1.16.5:2022.03.06"
            else -> null
        }?.let {
            parchment("org.parchmentmc.data:parchment-$it@zip")
        }
    })

    includeImplementation("org.quiltmc:quilt-json5:1.0.2")

//    includeImplementation("com.github.LlamaLad7.MixinExtras:mixinextras-${mcData.loader.name}:0.2.0-beta.6")
//    if (mcData.isForge) {
//        implementation("com.github.LlamaLad7.MixinExtras:mixinextras-common:0.2.0-beta.6")
//    }
//    annotationProcessor("com.github.LlamaLad7.MixinExtras:mixinextras-common:0.2.0-beta.6")

    if (mcData.isFabric) {
        when (mcData.version) {
            1_19_04 -> "6.2.1"
            1_19_02 -> "4.2.0-beta.2"
            1_18_02 -> "3.2.5"
            1_16_05 -> "1.16.23"
            else -> null
        }?.let {
            modImplementation("com.terraformersmc:modmenu:$it")
        }
        if (mcData.version == 1_16_01) {
            modImplementation("io.github.prospector:modmenu:1.14.5+build.30")
        }
    }

    modRuntimeOnly("me.djtheredstoner:DevAuth-${if (mcData.isFabric) "fabric" else "forge-latest"}:1.1.2")
}

java {
    withSourcesJar()
}

loom {
    if (mcData.isForge) {
        forge {
            mixinConfig("world-host.mixins.json")
        }
    }

    @Suppress("UnstableApiUsage")
    mixin.defaultRefmapName.set("world-host.mixins.refmap.json")
}

preprocess {
    patternAnnotation.set("io.github.gaming32.worldhost.versions.Pattern")
}

releases {
    modrinth {
        projectId.set("world-host")
    }
    file("changelogs/${modData.version}.md").let {
        if (it.exists()) {
            changelogFile.set(it)
        }
    }
    val loaderName = if (mcData.isFabric) "Fabric/Quilt" else "Forge"
    releaseName.set("[${mcData.versionStr} $loaderName] ${modData.name} ${modData.version}")
    if (mcData.isFabric) {
        loaders.add("fabric")
        loaders.add("quilt")
        if (mcData.version == 1_19_04) {
            gameVersions.add("1.19.4")
            gameVersions.add("23w13a_or_b")
        }
    }
}

tasks.processResources {
    filesMatching("pack.mcmeta") {
        expand("pack_format" to when {
            mcData.version >= 1_19_04 -> 13
            mcData.version >= 1_19_03 -> 12
            mcData.version >= 1_19_00 -> 9
            mcData.version >= 1_18_00 -> 8
            mcData.version >= 1_17_00 -> 7
            mcData.version >= 1_16_02 -> 6
            mcData.version >= 1_15_00 -> 5
            mcData.version >= 1_13_00 -> 4
            mcData.version >= 1_11_00 -> 3
            mcData.version >= 1_09_00 -> 2
            mcData.version >= 1_06_01 -> 1
            else -> return@filesMatching
        })
    }

    doLast {
        val resources = "$buildDir/resources/main"
        if (mcData.isForge) {
            copy {
                from(file("$resources/assets/world-host/icon.png"))
                into(resources)
            }
            delete(file("$resources/assets/world-host/icon.png"))
        } else {
            delete(file("$resources/pack.mcmeta"))
        }
    }
}