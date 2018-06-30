package net.perfectdreams.dreamcore.utils.extensions

import org.bukkit.event.player.PlayerMoveEvent

/**
 * Se o usuário realmente moveu (mudou as coordenadas x, y, z) ou se apenas mexeu a cabeça
 *
 * @return se o usuário realmente se moveu
 */
val PlayerMoveEvent.displaced: Boolean
	get() = this.from.x != this.to.x || this.from.y != this.to.y || this.from.z != this.to.z