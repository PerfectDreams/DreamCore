package net.perfectdreams.dreamcore.utils.tags

import com.comphenix.protocol.wrappers.nbt.NbtCompound
import com.comphenix.protocol.wrappers.nbt.NbtFactory
import org.bukkit.inventory.ItemStack

object NbtTagsUtils {
	const val SERVER_DATA_COMPOUND_NAME = "PerfectDreams"

	fun getCompoundTag(item: ItemStack): NbtCompound? {
		return NbtFactory.asCompound(NbtFactory.fromItemTag(item))
	}

	fun setCompoundTag(item: ItemStack, tag: NbtCompound) {
		NbtFactory.setItemTag(item, tag)
	}
}