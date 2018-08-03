package net.perfectdreams.dreamcore.utils

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.file.FileConfiguration

object ConfigUtils {
	fun loadLocation(config: FileConfiguration, path: String): Location {
		val x = config.getDouble("x")
		val y = config.getDouble("y")
		val z = config.getDouble("z")
		val yaw = config.getDouble("yaw") // SnakeYAML não tem suporte para floats...
		val pitch = config.getDouble("pitch")
		val worldName = config.getString("world")

		return Location(
				Bukkit.getWorld(worldName),
				x,
				y,
				z,
				yaw.toFloat(),
				pitch.toFloat()
		)
	}

	fun saveLocation(location: Location, config: FileConfiguration, path: String) {
		config.set("$path.x", location.x)
		config.set("$path.y", location.y)
		config.set("$path.z", location.z)
		config.set("$path.yaw", location.yaw)
		config.set("$path.pitch", location.pitch)
		config.set("$path.world", location.world)
	}
}