import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact
import javax.xml.parsers.DocumentBuilderFactory

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.blueskybone.arkscreen"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.blueskybone.arkscreen"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 16
        versionName = "2.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }
    }

    buildTypes {
        release {
            // 网络 DTO 依赖 Jackson 反射反序列化。在所有远端响应模型的保留规则验证完成前，
            // Release 构建必须关闭代码和资源压缩，避免类名或字段被裁剪后线上解析失败。
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = libs.versions.jvmTarget.get()
    }

    //compose
//    buildFeatures {
//        compose = true
//    }
//
//    composeOptions {
//        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
//    }

    ksp {
        arg("room.generateKotlin", "true")
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    sourceSets.getByName("main").assets.srcDir(
        layout.buildDirectory.dir("generated/third-party-licenses/assets")
    )
}

val generateThirdPartyLicenses by tasks.registering {
    group = "documentation"
    description = "Generates the third-party license catalog bundled with the app."

    val outputFile = layout.buildDirectory.file(
        "generated/third-party-licenses/assets/third_party_licenses.json"
    )
    outputs.file(outputFile)

    doLast {
        val componentIds = configurations.getByName("releaseRuntimeClasspath")
            .incoming.resolutionResult.allComponents
            .mapNotNull { it.id as? ModuleComponentIdentifier }
            .distinctBy { "${it.group}:${it.module}:${it.version}" }

        val resolution = dependencies.createArtifactResolutionQuery()
            .forComponents(componentIds)
            .withArtifacts(MavenModule::class.java, MavenPomArtifact::class.java)
            .execute()

        fun String.jsonEscape(): String = buildString {
            this@jsonEscape.forEach { char ->
                append(
                    when (char) {
                        '\\' -> "\\\\"
                        '"' -> "\\\""
                        '\n' -> "\\n"
                        '\r' -> "\\r"
                        '\t' -> "\\t"
                        else -> char
                    }
                )
            }
        }

        fun org.w3c.dom.Element.childText(tag: String): String =
            getElementsByTagName(tag).item(0)?.textContent?.trim().orEmpty()

        val documentBuilderFactory = DocumentBuilderFactory.newInstance().apply {
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            setAttribute("http://javax.xml.XMLConstants/property/accessExternalDTD", "")
            setAttribute("http://javax.xml.XMLConstants/property/accessExternalSchema", "")
        }

        val entries = resolution.resolvedComponents.mapNotNull componentLoop@ { component ->
            val id = component.id as? ModuleComponentIdentifier ?: return@componentLoop null
            val pom = component.getArtifacts(MavenPomArtifact::class.java)
                .filterIsInstance<ResolvedArtifactResult>()
                .firstOrNull()?.file
                ?: return@componentLoop null
            val root = documentBuilderFactory.newDocumentBuilder().parse(pom).documentElement
            val licenses = root.getElementsByTagName("license")
            val licenseValues = (0 until licenses.length).mapNotNull licenseLoop@ { index ->
                val element = licenses.item(index) as? org.w3c.dom.Element
                    ?: return@licenseLoop null
                val name = element.childText("name")
                val url = element.childText("url")
                if (name.isBlank() && url.isBlank()) null else name to url
            }.distinct()

            val displayName = root.childText("name").ifBlank { id.module }
            val projectUrl = root.childText("url")
            """
                {
                  "id": "${"${id.group}:${id.module}".jsonEscape()}",
                  "name": "${displayName.jsonEscape()}",
                  "version": "${id.version.jsonEscape()}",
                  "projectUrl": "${projectUrl.jsonEscape()}",
                  "licenses": [${licenseValues.joinToString(",") { (name, url) ->
                      """{"name":"${name.jsonEscape()}","url":"${url.jsonEscape()}"}"""
                  }}]
                }
            """.trimIndent()
        }.sortedBy { it.lowercase() }

        val target = outputFile.get().asFile
        target.parentFile.mkdirs()
        target.writeText(entries.joinToString(prefix = "[\n", separator = ",\n", postfix = "\n]"))
    }
}

tasks.configureEach {
    if (
        name == "mergeDebugAssets" ||
        name == "mergeReleaseAssets" ||
        name == "lintVitalAnalyzeRelease" ||
        name == "generateReleaseLintVitalReportModel"
    ) {
        dependsOn(generateThirdPartyLicenses)
    }
}

dependencies {

    // AndroidX 核心
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.work.runtime)

    // Material Design
    implementation(libs.material)

    // 测试
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Koin DI
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.android.compat)

    // Room 数据库
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)

    // 网络
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.jackson)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.jackson.module.kotlin)

    // 图片加载
    implementation(libs.coil)

    // 其他工具库
    implementation(libs.flow.layout)
    implementation(libs.timber)
    implementation(libs.easy.window)
    implementation(libs.toaster)
    implementation(libs.androidx.webkit)
    implementation(libs.mp.android.chart)
    implementation(libs.circular.progressbar)

    implementation(libs.recyclerview)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.coroutines.android)
    implementation(libs.flexbox)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.fragment.ktx)

}
