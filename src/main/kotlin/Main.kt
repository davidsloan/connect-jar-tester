import org.apache.kafka.connect.runtime.isolation.*
import java.net.URI
import java.net.URL
import java.nio.file.Path
import kotlin.io.path.exists

val classLoader = DelegatingClassLoader()

fun main(args: Array<String>) {

    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel","DEBUG")

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")


    val jarPath = "file://${args.get(0)}"
    val pluginSources = pluginSources(jarPath)

    val scanner = ReflectionScanner()

    val pluginScannerResult  = scanner.discoverPlugins(pluginSources)

    println("Source Connectors:")
    pluginScannerResult.sourceConnectors().forEach{
        println(it.className())
    }


}

private fun pluginSources(pathToMyJar: String): Set<PluginSource> {
    val path: Path = Path.of(URI.create(pathToMyJar))
    require(path.exists())


    val url = URI.create(pathToMyJar).toURL()

    val urls = arrayOf<URL>(url)

    val pluginSource = PluginSource(
        path,
        PluginSource.Type.SINGLE_JAR,
        classLoader,
        urls
    )
    return setOf(pluginSource)
}