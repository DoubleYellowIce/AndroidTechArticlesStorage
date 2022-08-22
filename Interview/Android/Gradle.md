### Setting.gradle

- pluginManagement{}

  pluginManagement{}有两个用途。

  - 用来指定所有module的Gradle插件仓库的。

  - 用来预定义子module可能要用到的Gradle插件的类型和版本。

    ```groovy
    pluginManagement {
        repositories {
          	//指定仓库
            gradlePluginPortal()
            google()
            mavenCentral()
        }
        plugins {
            id 'com.android.application' version '7.1.0-rc01'
            ..
        }
    }
    ```

    这样在子module里想要添加依赖就可以直接使用。

    ```groovy
    plugins {
        id 'com.android.application'
    }
    ```

- dependencyResolutionManagement {}

  dependencyResolutionManagement {}用于指定所有module中的dependencies{}依赖的仓库。

  

