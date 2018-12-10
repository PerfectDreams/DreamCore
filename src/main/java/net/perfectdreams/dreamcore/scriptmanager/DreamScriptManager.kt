package net.perfectdreams.dreamcore.scriptmanager

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.KtsObjectLoader
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.graalvm.polyglot.Context
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest

class DreamScriptManager(val m: DreamCore) {
	val scripts = mutableListOf<DreamScript>()
	val scriptsFolder = File(m.dataFolder, "scripts")
	val scriptTemplateFile = File(m.dataFolder, "template.kts")

	fun loadScripts() {
		m.logger.info("Carregando DreamScripts...")
		val scriptsFolder = File(m.dataFolder, "scripts")

		scriptsFolder.mkdirs()
		scriptsFolder.listFiles().forEach {
			if (it.extension == "kt" || it.extension == "kts") {
				loadScript(it)
			}
		}
		m.logger.info("DreamScripts carregados com sucesso! ${scripts.size} scripts foram carregados!")
	}

	fun loadScript(file: File) {
		System.setProperty("idea.use.native.fs.for.win", "false") // Necessário para não ficar dando problemas no Windows

		m.logger.info("Carregando DreamScript \"${file.name}\"...")

		if (!scriptTemplateFile.exists()) {
			m.logger.warning("Arquivo \"template.kts\" não existe!")
			return
		}

		val templateContent = scriptTemplateFile.readText()
		val content = file.readText()
		val className = file.nameWithoutExtension.replace("_", "").toLowerCase().capitalize()
		val script = templateContent.replace("{{ code }}", content).replace("{{ className }}", className)

		val cl = m.javaClass.classLoader
		Thread.currentThread().contextClassLoader = cl

		val graalContext = Context.newBuilder()
				.allowAllAccess(true) // Permite usar coisas da JVM dentro do GraalJS (e várias outras coisas)
				.build()

		try {
			val dreamScript = DreamScript(file.name)
			val bindings = graalContext.getBindings("js")
			bindings.putMember("server", Bukkit.getServer())
			bindings.putMember("script", dreamScript)
			graalContext.eval("js", script)

			dreamScript.enable()

			scripts.add(dreamScript)

			m.logger.info("DreamScript \"${file.name}\" carregado com sucesso!")
		} catch (e: Exception) {
			m.logger.warning("Erro ao carregar o script  \"${file.name}\"!")
			e.printStackTrace()
		}
	}

	fun unloadScripts() {
		scripts.forEach {
			unloadScript(it, false)
		}
		scripts.clear()
	}

	fun unloadScript(script: DreamScript, removeFromList: Boolean = true) {
		script.disable()
		if (removeFromList)
			scripts.remove(script)
	}

	companion object {
		inline fun <reified T> evaluate(plugin: Plugin, code: String): T {
			// Necessário para encontrar as classes
			val cl = plugin.javaClass.classLoader
			Thread.currentThread().contextClassLoader = cl

			val pluginsFolder = File("./plugins").listFiles().filter { it.extension == "jar" }.joinToString(File.pathSeparator, transform = { "plugins/${it.name}" })
			val propClassPath = "cache/patched_1.13.2.jar${File.pathSeparator}$pluginsFolder"

			System.setProperty("kotlin.script.classpath", propClassPath)

			return KtsObjectLoader().load<T>(code)
		}
	}
}