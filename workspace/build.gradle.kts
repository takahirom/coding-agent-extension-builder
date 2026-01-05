plugins {
    id("kotlin-library")
    application
}

dependencies {
    implementation(project(":coding-agent-extension-builder"))
}

application {
    mainClass.set("MainKt")
}
