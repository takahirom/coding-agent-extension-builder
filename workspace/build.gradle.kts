plugins {
    id("kotlin-library")
    application
}

dependencies {
    implementation(project(":coding-agent-extension-dsl"))
}

application {
    mainClass.set("MainKt")
}
