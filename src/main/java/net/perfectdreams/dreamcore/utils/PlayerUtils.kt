package net.perfectdreams.dreamcore.utils

import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player

object PlayerUtils {
	/**
	 * Deixa o jogador com a vida máxima possível e enche a barrinha de comida do jogdaor
	 *
	 * @param player o jogador
	 */
	fun healAndFeed(player: Player) {
		player.health = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).value
		player.foodLevel = 20
	}
}