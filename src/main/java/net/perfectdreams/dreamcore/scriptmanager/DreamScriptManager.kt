package net.perfectdreams.dreamcore.scriptmanager

import net.perfectdreams.dreamcore.DreamCore
import org.bukkit.Bukkit
import org.graalvm.polyglot.Context
import java.io.File

class DreamScriptManager(val m: DreamCore) {
	val scripts = mutableListOf<DreamScript>()

	fun loadScripts() {
		m.logger.info("Carregando DreamScripts...")
		val scriptsFolder = File(m.dataFolder, "scripts")

		scriptsFolder.mkdirs()
		scriptsFolder.listFiles().forEach {
			if (it.extension == "js") {
				loadScript(it)
			}
		}
		m.logger.info("DreamScripts carregados com sucesso! ${scripts.size} scripts foram carregados!")
	}

	fun loadScript(file: File) {
		m.logger.info("Carregando DreamScript \"${file.name}\"...")
		val content = file.readText()

		val graalContext = Context.newBuilder()
				.allowAllAccess(true)
				.allowHostAccess(true) // Permite usar coisas da JVM dentro do GraalJS
				.option("js.nashorn-compat", "true")
				.build()

		try {
			val dreamScript = DreamScript(file.name)
			val bindings = graalContext.getBindings("js")
			bindings.putMember("server", Bukkit.getServer())
			bindings.putMember("script", dreamScript)
			graalContext.eval("js", content)

			dreamScript.enable()

			scripts.add(dreamScript)

			m.logger.info("DreamScript \"${file.name}\" carregado com sucesso!")
		} catch (e: Exception) {
			m.logger.warning("Erro ao carregar o script  \"${file.name}\"!")
			e.printStackTrace()
		}
	}
}