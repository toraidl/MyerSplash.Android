mapOf(
        "kotlinVersion" to "1.3.10",
        "kotlinCoroutineVersion" to "1.0.1",
        "compileVersion" to 28,
        "minVersion" to 21,
        "targetVersion" to 28,
        "buildToolVersion" to "28.0.3",
        "supportVersion" to "27.1.1",
        "androidXVersion" to "1.0.0",
        "androidXAppCompatVersion" to "1.0.0",
        "roomVersion" to "1.1.1",
        "frescoVersion" to "1.11.0",
        "butterKnifeVersion" to "10.0.0",
        "retrofitVersion" to "2.3.0",
        "realmVersion" to "3.0.0",
        "constraintLayoutVersion" to "1.1.3"
).forEach {
    project.extra[it.key] = it.value
}

mapOf(
        "appVersionCode" to 310,
        "appVersionName" to "3.1.0"
).forEach {
    project.extra[it.key] = it.value
}