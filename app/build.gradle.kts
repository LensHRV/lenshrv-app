plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.androidx.room)
}

val supabaseUrl = providers.gradleProperty("SUPABASE_URL")
val supabaseKey = providers.gradleProperty("SUPABASE_KEY")
val samsungSecretsPresent =
    supabaseUrl.map { it.isNotBlank() }.orElse(false).get() &&
            supabaseKey.map { it.isNotBlank() }.orElse(false).get()

android {
    namespace = "com.lenshrv.app"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        targetSdk = 36
        versionCode = 3
        versionName = "1.0.2"
        resValue("string", "app_name_variant", "Lens HRV")
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt")
            )
        }
    }
    flavorDimensions += "version"

    productFlavors {
        create("samsung") {
            dimension = "version"
            applicationId = "com.lenshrv.samsung"
            if (samsungSecretsPresent) {
                buildConfigField("String", "SUPABASE_URL", "\"${supabaseUrl.get()}\"")
                buildConfigField("String", "SUPABASE_KEY", "\"${supabaseKey.get()}\"")
            }
        }

        create("foss") {
            dimension = "version"
            isDefault = true
            applicationId = "com.lenshrv.foss"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        resValues = true
        buildConfig = true
    }
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

androidComponents {
    beforeVariants(selector().withFlavor("version" to "samsung")) { variant ->
        variant.enable = samsungSecretsPresent
    }
}

room {
    schemaDirectory("samsung", "$projectDir/schemas/samsung")
    schemaDirectory("foss", "$projectDir/schemas/foss")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.kotlinx.coroutines.guava)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.datastore.preferences)
    ksp(libs.androidx.hilt.compiler)
    "samsungImplementation"(platform(libs.supabase.bom))
    "samsungImplementation"(libs.supabase.postgrest)
    "samsungImplementation"(libs.ktor.client.android)
    "samsungImplementation"(libs.androidx.work.runtime.ktx)
    "samsungImplementation"(libs.androidx.hilt.work)
    "samsungImplementation"(libs.samsung.iap)
}
