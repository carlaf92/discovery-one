import com.discoveryone.buildsrc.Dependencies

plugins {
    id("kotlin")
    id("kotlin-kapt")
}

dependencies {
    implementation(project(":destination"))

    implementation(Dependencies.kotlinPoet)
    implementation(Dependencies.kotlinStdLib)
    implementation(Dependencies.kotlinReflect)
    implementation(Dependencies.autocommon)

    testImplementation(Dependencies.junit)
    testImplementation(Dependencies.kotlinCompileTesting)
}
