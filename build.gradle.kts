@file:Suppress("UnstableApiUsage")

import net.fabricmc.loom.task.RemapJarTask
import org.gradle.configurationcache.extensions.capitalized

plugins {
    `java-library`

    id("dev.architectury.loom")
    id("dev.kikugie.j52j") version "1.0.2"

    id("me.modmuss50.mod-publish-plugin")
    `maven-publish`
}

// version stuff
val mcVersion = property("mcVersion")!!.toString()
val mcSemverVersion = stonecutter.current.version

// loader stuff
val loader = loom.platform.get().name.lowercase()
val isFabric = loader == "fabric"
val isNeoforge = loader == "neoforge"
val isForge = loader == "forge"
val isForgeLike = isNeoforge || isForge

// project stuff
group = "dev.isxander"
val versionWithoutMC = property("modVersion")!!.toString()
version = "$versionWithoutMC+${stonecutter.current.project}"
val isAlpha = "alpha" in version.toString()
val isBeta = "beta" in version.toString()
base.archivesName.set(property("modName").toString())

// Conditionally include the mixins for the mods this target is compatible with
// Generates a list of mixin file names for processResources to include in the manifests
val mixins = mapOf(
    "controlify" to true,
    "controlify-compat.iris" to isPropDefined("deps.iris"),
    "controlify-compat.sodium" to isPropDefined("deps.sodium"),
    "controlify-compat.reeses-sodium-options" to isPropDefined("deps.reesesSodiumOptions"),
    "controlify-compat.yacl" to true,
    "controlify-compat.simple-voice-chat" to isPropDefined("deps.simpleVoiceChat"),
    "controlify-platform.fabric" to isFabric,
    "controlify-platform.neoforge" to isNeoforge,
    "controlify-platform.forge" to isForge,
)
    .map { (k, v) -> if (v) k else null }
    .filterNotNull()
    .map { "$it.mixins.json" }

val accessWidenerName = "controlify.accesswidener"
loom {
    accessWidenerPath.set(project.file("src/main/resources/$accessWidenerName"))

    if (stonecutter.current.isActive) { // only generate active project run config as the rest would be invalid
        runConfigs.all {
            ideConfigGenerated(true)

            // use a single run directory for all targets (targets are two folders deep from root)
            runDir("../../run")

            // Loom messes with LWJGL version. It's not the one that ships with MC and Sodium doesn't like it
            vmArgs("-Dsodium.checks.issue2561=false")
        }
    }

    mixin {
        // MixinExtras expressions do not support tiny remapper for now.
        useLegacyMixinAp.set(true)
    }

    if (isForge) {
        forge {
            convertAccessWideners.set(true)
            mixins.forEach { mixinConfig(it) }
        }
    }
}

stonecutter {
    val sodiumSemver = findProperty("deps.sodiumSemver")?.toString() ?: "0.0.0"
    dependencies(
        "fapi" to (findProperty("deps.fabricApi")?.toString() ?: "0.0.0"),
        "sodium" to sodiumSemver
    )

    swaps["sodium-package"] = if (eval(sodiumSemver, ">=0.6"))
        "net.caffeinemc.mods.sodium" else "me.jellysquid.mods.sodium"
}

dependencies {
    fun Dependency?.jij() = this?.also(::include)
    fun Dependency?.forgeRuntime() = this?.also { if (isForgeLike) "forgeRuntimeLibrary"(it) }

    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.layered {
        optionalProp("deps.parchment") {
            parchment("org.parchmentmc.data:parchment-$it@zip")
        }

        officialMojangMappings()
    })

    optionalProp("deps.mixinExtras") {
        if (isForgeLike) {
            compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:$it")!!)
            if (isNeoforge) {
                implementation(include("io.github.llamalad7:mixinextras-neoforge:$it")!!)
            } else {
                implementation(include("io.github.llamalad7:mixinextras-forge:$it")!!)
            }
        } else {
            include(implementation(annotationProcessor("io.github.llamalad7:mixinextras-fabric:$it")!!)!!)
        }
    }

    fun modDependency(id: String, artifactGetter: (String) -> String, extra: (Boolean) -> Unit = {}) {
        optionalProp("deps.$id") {
            val noRuntime = findProperty("deps.$id.noRuntime")?.toString()?.toBoolean() == true
            val configuration = if (noRuntime) "modCompileOnly" else "modImplementation"

            configuration(artifactGetter(it))

            extra(!noRuntime)
        }
    }

    if (isFabric) {
        modImplementation("net.fabricmc:fabric-loader:${property("deps.fabricLoader")}")

        val fapiVersion = property("deps.fabricApi").toString()
        listOf(
            "fabric-resource-loader-v0",
            "fabric-lifecycle-events-v1",
            "fabric-key-binding-api-v1",
            "fabric-registry-sync-v0",
            "fabric-screen-api-v1",
            "fabric-command-api-v2",
            "fabric-networking-api-v1",
            "fabric-item-group-api-v1",
            "fabric-rendering-v1",
            "fabric-transitive-access-wideners-v1",
        ).forEach {
            modImplementation(fabricApi.module(it, fapiVersion))
        }
        // so you can do `depends: fabric-api` in FMJ
        modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:$fapiVersion")

        // mod menu compat
        modDependency("modMenu", { "com.terraformersmc:modmenu:$it" })
    } else if (isNeoforge) {
        "neoForge"("net.neoforged:neoforge:${findProperty("deps.neoforge")}")
    } else if (isForge) {
        "forge"("net.minecraftforge:forge:${findProperty("deps.forge")}")
    }

    modApi("dev.isxander:yet-another-config-lib:${property("deps.yacl")}") {
        // was including old fapi version that broke things at runtime
        exclude(group = "net.fabricmc.fabric-api", module = "fabric-api")
        exclude(group = "thedarkcolour")
    }.forgeRuntime()

    // bindings for SDL3
    api("dev.isxander:libsdl4j:${property("deps.sdl3Target")}-${property("deps.sdl34jBuild")}")
        .forgeRuntime().jij()

    // steam deck bindings
    api("dev.isxander:steamdeck4j:${property("deps.steamdeck4j")}")
        .forgeRuntime().jij()

    // used to identify controller PID/VID when SDL is not available
    api("org.hid4java:hid4java:${property("deps.hid4java")}")
        .forgeRuntime().jij()

    // A json5 reader that hooks into gson
    listOf(
        "json",
        "gson",
    ).forEach {
        api("org.quiltmc.parsers:$it:${property("deps.quiltParsers")}")
            .jij().forgeRuntime()
    }

    // sodium compat
    modDependency("sodium", { "maven.modrinth:sodium:$it" })

    // RSO compat
    modDependency("reesesSodiumOptions", { "maven.modrinth:reeses-sodium-options:$it" })

    // iris compat
    modDependency("iris", { "maven.modrinth:iris:$it" }) { runtime ->
        if (runtime) {
            modRuntimeOnly("org.anarres:jcpp:1.4.14")
            modRuntimeOnly("io.github.douira:glsl-transformer:2.0.0-pre13")
        }
    }

    // immediately-fast compat
    modDependency("immediatelyFast", { "maven.modrinth:immediatelyfast:$it" }) { runtime ->
        if (runtime) {
            modRuntimeOnly("net.lenni0451:Reflect:1.1.0")
        }
    }

    // simple-voice-chat compat
    modDependency("simpleVoiceChat", { "maven.modrinth:simple-voice-chat:$it" })

    // fancy menu compat
    modDependency("fancyMenu", { "maven.modrinth:fancymenu:$it" })
}

tasks {
    processResources {
        val modId: String by project
        val modName: String by project
        val modDescription: String by project
        val githubProject: String by project
        val packFormat: String by project

        val props = buildMap {
            put("id", modId)
            put("group", project.group)
            put("name", modName)
            put("description", modDescription)
            put("version", project.version)
            put("github", githubProject)
            put("pack_format", packFormat)

            if (isFabric) {
                put("mc", findProperty("fmj.mcDep"))
                put("mixins", mixins.joinToString("\",\""))
                put("fapi", findProperty("fmj.fapiDep") ?: "*")
            }

            if (isForgeLike) {
                put("mc", findProperty("modstoml.mcDep"))
                put("loaderVersion", findProperty("modstoml.loaderVersion"))
                put("forgeId", findProperty("modstoml.forgeId"))
                put("forgeConstraint", findProperty("modstoml.forgeConstraint"))
                put("mixins", mixins.joinToString("\n\n") { """
                    [[mixins]]
                    config = "$it"
                """.trimIndent() })
            }
        }
        props.forEach(inputs::property)

        val fabricModJson = "fabric.mod.json"
        val modsToml = "META-INF/mods.toml"
        val neoforgeModsToml = "META-INF/neoforge.mods.toml"
        val metadataFiles = listOf(
            fabricModJson, modsToml, neoforgeModsToml,
        )
        val modMetadataFile = when {
            isFabric -> fabricModJson
            isNeoforge && stonecutter.eval(stonecutter.current.version, ">=1.20.5") -> neoforgeModsToml
            isForgeLike -> modsToml
            else -> error("Unknown loader")
        }

        filesMatching(listOf(modMetadataFile, "**/pack.mcmeta")) {
            expand(props)
        }

        exclude(metadataFiles - modMetadataFile)

        eachFile {
            // don't include photoshop files for the textures for development
            if (name.endsWith(".psd")) {
                exclude()
            }
        }
    }

    register("releaseModVersion") {
        group = "mod"

        dependsOn("publishMods")

        if (!project.publishMods.dryRun.get()) {
            dependsOn("publish")
        }
    }
}

// Builds a jar that bundles all required natives for use offline, or when servers go down.
val offlineRemapJar by tasks.registering(RemapJarTask::class) {
    group = "offline"

    val downloadTask = rootProject.tasks["downloadOfflineNatives"]

    dependsOn(tasks.jar)
    dependsOn(downloadTask)
    inputFile.set(tasks.jar.get().archiveFile.get().asFile)

    from(downloadTask.outputs.files)

    archiveClassifier.set("offline")
}

tasks.build { dependsOn(offlineRemapJar) }

tasks.remapJar {
    if (isNeoforge) {
        atAccessWideners.add(accessWidenerName)
    }
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
    options.release = project.property("java.version").toString().toInt()
}

publishMods {
    from(rootProject.publishMods)
    dryRun.set(rootProject.publishMods.dryRun)

    file.set(tasks.remapJar.get().archiveFile)
    additionalFiles.setFrom(offlineRemapJar.get().archiveFile)

    modLoaders.add(loader)

    displayName.set("$versionWithoutMC for $loader $mcVersion")

    fun versionList(prop: String) = findProperty(prop)?.toString()
        ?.split(',')
        ?.map { it.trim() }
        ?: emptyList()

    // modrinth and curseforge use different formats for snapshots. this can be expressed globally
    val stableMCVersions = versionList("pub.stableMC")

    val modrinthId: String by project
    if (modrinthId.isNotBlank() && hasProperty("modrinth.token")) {
        modrinth {
            projectId.set(modrinthId)
            accessToken.set(findProperty("modrinth.token")?.toString())
            minecraftVersions.addAll(stableMCVersions)
            minecraftVersions.addAll(versionList("pub.modrinthMC"))

            announcementTitle = "Download $mcVersion for ${loader.capitalized()} from Modrinth"

            requires { slug.set("yacl") }

            if (isFabric) {
                requires { slug.set("fabric-api") }
                optional { slug.set("modmenu") }
            }
        }
    }

    val curseforgeId: String by project
    if (curseforgeId.isNotBlank() && hasProperty("curseforge.token")) {
        curseforge {
            projectId = curseforgeId
            projectSlug = findProperty("curseforgeSlug")!!.toString()
            accessToken = findProperty("curseforge.token")?.toString()
            minecraftVersions.addAll(stableMCVersions)
            minecraftVersions.addAll(versionList("pub.curseMC"))

            announcementTitle = "Download $mcVersion for ${loader.capitalized()} from CurseForge"

            requires { slug.set("yacl") }

            if (isFabric) {
                requires { slug.set("fabric-api") }
                optional { slug.set("modmenu") }
            }
        }
    }

    val githubProject: String by project
    if (githubProject.isNotBlank() && hasProperty("github.token")) {
        github {
            accessToken = findProperty("github.token")?.toString()

            // will upload files to this parent task
            parent(rootProject.tasks.named("publishGithub"))
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mod") {
            groupId = "dev.isxander"
            artifactId = "controlify"

            from(components["java"])
        }
    }

    repositories {
        val username = "XANDER_MAVEN_USER".let { System.getenv(it) ?: findProperty(it) }?.toString()
        val password = "XANDER_MAVEN_PASS".let { System.getenv(it) ?: findProperty(it) }?.toString()
        if (username != null && password != null) {
            maven(url = "https://maven.isxander.dev/releases") {
                name = "XanderReleases"
                credentials {
                    this.username = username
                    this.password = password
                }
            }
        } else {
            println("Xander Maven credentials not satisfied.")
        }
    }
}

fun <T> optionalProp(property: String, block: (String) -> T?) {
    findProperty(property)?.toString()?.takeUnless { it.isBlank() }?.let(block)
}

fun isPropDefined(property: String): Boolean {
    return findProperty(property)?.toString()?.isNotBlank() ?: false
}
