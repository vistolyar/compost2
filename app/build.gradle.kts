plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.compost2"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.compost2"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }

    // БЛОК УПАКОВКИ РЕСУРСОВ (ВАЖЕН ДЛЯ ИЗБЕЖАНИЯ ОШИБОК СБОРКИ)
    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/license.txt"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/notice.txt"
            excludes += "META-INF/ASL2.0"
            excludes += "META-INF/*.kotlin_module"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.androidx.datastore.preferences)

    // Auth & Images
    // ИСКЛЮЧАЕМ конфликтующий модуль http-client из play-services-auth
    implementation("com.google.android.gms:play-services-auth:21.0.0") {
        exclude(group = "com.google.http-client", module = "google-http-client")
    }
    implementation("io.coil-kt:coil-compose:2.5.0")

    // --- GOOGLE API CLIENTS (ЗОЛОТОЙ СТАНДАРТ 1.x) ---
    // Это гарантирует наличие класса com.google.api.client.extensions.android.http.AndroidHttp
    implementation("com.google.api-client:google-api-client-android:1.35.2")

    // Явно подключаем android-версию http клиента
    implementation("com.google.http-client:google-http-client-android:1.42.3")
    implementation("com.google.http-client:google-http-client-gson:1.42.3")

    // Сервисы - ВСЕ ВЕРСИИ СИНХРОНИЗИРОВАНЫ (1.32.1)
    implementation("com.google.apis:google-api-services-calendar:v3-rev20220715-1.32.1")
    implementation("com.google.apis:google-api-services-gmail:v1-rev20220404-1.32.1")
    // Исправленная версия Tasks (она точно есть в репозитории)
    implementation("com.google.apis:google-api-services-tasks:v1-rev20210709-1.32.1")

    // Email (JavaMail)
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}