// 插件仓库：限定 google/ 与 com.android/ 走 Google 仓，其余走 Maven Central
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }

}


// 依赖仓库：禁止子模块自己声明仓库，统一在这里管，避免版本漂移
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MomoCalorie"

include(":app")
include(":core:common")
include(":core:domain")