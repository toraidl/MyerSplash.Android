val file = rootProject.file("local.properties")
file?.useLines { lines ->
    val sb = StringBuilder()

    lines.forEach { lineString ->
        sb.append(lineString + "\n")

        val split = lineString.split('=')
        if (split.size != 2) return@useLines

        val name = split[0]
        val value = split[1]
        rootProject.extra[name] = value
    }

    task("printLocal") {
        println("Local properties are: \n$sb")
    }
}