package net.perfectdreams.dreamcore.utils

import net.minecraft.server.v1_12_R1.EntityFireworks
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityStatus
import net.minecraft.server.v1_12_R1.World
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.Firework
import org.bukkit.entity.Player

class InstantFirework(world: World, vararg players: Player) : EntityFireworks(world) {
	val players: Array<Player>?
	private var gone = false

	init {
		this.players = arrayOf(*players)
		this.a(0.25f, 0.25f)
	}

	override fun B_() {
		if (gone) {
			return
		}

		if (!this.world.isClientSide) {
			gone = true

			if (players != null) {
				if (players.isNotEmpty()) {
					for (player in players) {
						(player as CraftPlayer).handle.playerConnection.sendPacket(PacketPlayOutEntityStatus(this, 17.toByte()))
					}

					this.die()
					return
				}
			}
			world.broadcastEntityEffect(this, 17.toByte())
			this.die()
		}
	}

	companion object {
		fun spawn(location: Location, effect: FireworkEffect, vararg players: Player = location.world.players.toTypedArray()) {
			try {
				val firework = InstantFirework((location.world as CraftWorld).handle, *players)
				val meta = (firework.getBukkitEntity() as Firework).fireworkMeta
				meta.addEffect(effect)
				(firework.getBukkitEntity() as Firework).fireworkMeta = meta
				firework.setPosition(location.x, location.y, location.z)

				if ((location.world as CraftWorld).handle.addEntity(firework)) {
					firework.isInvisible = true
				}
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
	}
}