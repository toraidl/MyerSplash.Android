val file = rootProject.file("local.properties")

file?.forEachLine { line->
    println("Local properties: $line")

    val split = line.split('=')
    if (split.size != 2) return@forEachLine

    val name = split[0]
    val value = split[1]
    rootProject.extra[name] = value
}