import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // 只保留这一行。原来多出来的 kotlin("jvm") 会和它抢版本 → 报
    // "The request for this plugin could not be satisfied because the plugin is already on the classpath"
    alias(libs.plugins.kotlin.jvm)
}

java {
    // 原来这里是空的 java {}，补上真正的编译级别
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
    // 原来这里还有 jvmToolchain(8)：工具链 8 却要求编译出 17 字节码，
    // 必然报 "Inconsistent JVM-target compatibility"，已删除
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)

    // 原来还有 implementation(kotlin("stdlib-jdk8"))：Kotlin 插件会自动加 stdlib，
    // 而且 stdlib-jdk8 从 1.8 起已经合并进主 stdlib，这行是多余且可能触发版本解析失败
}

// 原来文件末尾的 repositories { mavenCentral() } 必须删掉。
// settings.gradle.kts 里设了 FAIL_ON_PROJECT_REPOS，模块自建仓库会直接让构建失败：
// "Build was configured to prefer settings repositories over project repositories
//  but repository 'MavenCentral' was added by build file 'core/domain/build.gradle.kts'"