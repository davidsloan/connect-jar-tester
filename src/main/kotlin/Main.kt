import org.apache.kafka.connect.runtime.isolation.*
import java.io.File
import java.net.URI
import java.net.URL
import java.nio.file.Path

fun main(args: Array<String>) {

    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel","DEBUG")

    println("Program arguments: ${args.joinToString()}")

    val jarPath = "file://${args[0]}"

    val parent = ClassLoader.getSystemClassLoader()
    val url = URI.create(jarPath).toURL()
    val jarFiles = getJarFiles(url)
    val jarFilesUrls = jarFiles.map{jF -> URI.create(jF).toURL()}.toTypedArray()
    val classLoader = PluginClassLoader(url, jarFilesUrls, parent)
    val pluginSources = pluginSources( Path.of(url.toURI()), jarFilesUrls,classLoader)

    scanPluginsAndPrint("Reflection Scanner", ReflectionScanner(), pluginSources)
    scanPluginsAndPrint("Service Loader Scanner", ServiceLoaderScanner(), pluginSources)


}

private fun scanPluginsAndPrint(
    whichScanner: String,
    scanner: PluginScanner,
    pluginSources: Set<PluginSource>
) {
    val pluginScannerResult = scanner.discoverPlugins(pluginSources)

    println("$whichScanner found Sink Connectors:")
    pluginScannerResult.sinkConnectors().forEach {
        println(it.className())
    }
    println("$whichScanner found Source Connectors:")
    pluginScannerResult.sourceConnectors().forEach {
        println(it.className())
    }
}

fun getJarFiles(directoryPath: URL): Array<String> {
    val directory = File(directoryPath.toURI())

    require(!(!directory.exists() || !directory.isDirectory)) { "The provided path$directoryPath is not a valid directory" }

    return directory.listFiles { _, name -> name.endsWith(".jar") }
        ?.map { "file://${directory.absolutePath}/${it.name}" }
        ?.toTypedArray()
        ?: emptyArray()
}

private fun pluginSources(location: Path, pathToMyJars: Array<URL>, classLoader: ClassLoader): Set<PluginSource> {
    return setOf(
        PluginSource(
            location,
            PluginSource.Type.SINGLE_JAR,
            classLoader,
            pathToMyJars
        )
    )
}


