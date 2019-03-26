mapOf(
        "kotlinVersion" to "1.3.10",
        "kotlinCoroutineVersion" to "1.0.1",
        "supportVersion" to "27.1.1",
        "androidXVersion" to "1.0.0",
        "androidXAppCompatVersion" to "1.0.0",
        "roomVersion" to "1.1.1",
        "frescoVersion" to "1.11.0",
        "butterKnifeVersion" to "10.0.0",
        "retrofitVersion" to "2.3.0",
        "realmVersion" to "3.0.0",
        "constraintLayoutVersion" to "1.1.3",
        "appCenterSdkVersion" to "1.11.4"
).forEach {
    project.extra[it.key] = it.value
}