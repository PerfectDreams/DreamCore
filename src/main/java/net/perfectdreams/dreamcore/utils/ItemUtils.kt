package net.perfectdreams.dreamcore.utils

import net.minecraft.server.v1_13_R2.NBTCompressedStreamTools
import net.minecraft.server.v1_13_R2.NBTTagCompound
import net.minecraft.server.v1_13_R2.NBTTagList
import net.perfectdreams.dreamcore.utils.extensions.getCompoundTag
import net.perfectdreams.dreamcore.utils.extensions.setCompoundTag
import net.perfectdreams.dreamcore.utils.tags.NbtTagsUtils
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.*
import java.math.BigInteger
import java.util.*

object ItemUtils {
	fun getStoredMetadata(itemStack: ItemStack, key: String): String? {
		val tag = itemStack.getCompoundTag() ?: return null
		if (!hasStoredMetadataWithKey(itemStack, NbtTagsUtils.SERVER_DATA_COMPOUND_NAME)) return null
		val compound = tag.getCompound(NbtTagsUtils.SERVER_DATA_COMPOUND_NAME)
		return compound.getString(key)
	}

	fun storeMetadata(itemStack: ItemStack, key: String, value: String): ItemStack {
		val tag = itemStack.getCompoundTag() ?: return itemStack
		val compound = tag.getCompoundOrDefault(NbtTagsUtils.SERVER_DATA_COMPOUND_NAME)
		compound.put(key, value)

		if (!tag.containsKey("PerfectDreams"))
			tag.put(compound)

		return itemStack.setCompoundTag(tag)
	}

	fun hasStoredMetadataWithKey(itemStack: ItemStack, key: String): Boolean {
		val tag = itemStack.getCompoundTag() ?: return false
		return tag.containsKey(key)
	}
}

fun ItemStack.rename(name: String): ItemStack {
	val meta = this.itemMeta
	meta.displayName = name
	this.itemMeta = meta
	return this
}

fun ItemStack.lore(vararg lore: String): ItemStack {
	val meta = this.itemMeta
	meta.lore = Arrays.asList(*lore)
	this.itemMeta = meta
	return this
}

fun ItemStack.lore(lore: List<String>): ItemStack {
	val meta = this.itemMeta
	meta.lore = lore
	this.itemMeta = meta
	return this
}

fun ItemStack.addFlag(vararg flag: ItemFlag): ItemStack {
	val meta = this.itemMeta
	meta.addItemFlags(*flag)
	this.setItemMeta(meta)
	return this
}

fun ItemStack.removeFlag(vararg flag: ItemFlag): ItemStack {
	val meta = this.itemMeta
	meta.removeItemFlags(*flag)
	this.itemMeta = meta
	return this
}

@Deprecated(message = "Please use ItemExtensions.setCompoundTag(...)")
fun ItemStack.setStorageData(data: String, key: UUID): ItemStack {
	val tag = this.getCompoundTag() ?: return this
	val compound = tag.getCompoundOrDefault(NbtTagsUtils.SERVER_DATA_COMPOUND_NAME)
	compound.put(key.toString(), data)

	if (!tag.containsKey("PerfectDreams"))
		tag.put(compound)

	return this.setCompoundTag(tag)
}

@Deprecated(message = "Please use ItemExtensions.getCompoundTag()")
fun ItemStack.getStorageData(key: UUID): String? {
	val tag = this.getCompoundTag() ?: return null
	val compound = tag.getCompound(NbtTagsUtils.SERVER_DATA_COMPOUND_NAME) ?: return null

	return compound.getString(key.toString())
}

fun ItemStack.toBase64(): String {
	val outputStream = ByteArrayOutputStream();
	val dataOutput = DataOutputStream(outputStream)
	val nbtTagListItems = NBTTagList()
	val nbtTagCompoundItem = NBTTagCompound()
	val nmsItem = CraftItemStack.asNMSCopy(this)
	nmsItem.save(nbtTagCompoundItem)
	nbtTagListItems.add(nbtTagCompoundItem)
	NBTCompressedStreamTools.a(nbtTagCompoundItem, dataOutput as DataOutput)
	return BigInteger(1, outputStream.toByteArray()).toString(32);
}

fun String.fromBase64Item(): ItemStack {
	val inputStream = ByteArrayInputStream(BigInteger(this, 32).toByteArray())
	var nbtTagCompoundRoot: NBTTagCompound? = null
	try {
		nbtTagCompoundRoot = NBTCompressedStreamTools.a(DataInputStream(inputStream))
	} catch (e: IOException) {
		e.printStackTrace()
	}

	val nmsItem = net.minecraft.server.v1_13_R2.ItemStack.a(nbtTagCompoundRoot)
	return CraftItemStack.asBukkitCopy(nmsItem)
}

fun Array<ItemStack>.toBase64(): String {
	val outputStream = ByteArrayOutputStream()
	try {
		val dataOutput = BukkitObjectOutputStream(outputStream as OutputStream)
		dataOutput.writeInt(this.size)
		for ((index, itemStack) in this.withIndex()) {
			if (itemStack != null && itemStack.type != Material.AIR) {
				dataOutput.writeObject(itemStack.toBase64())
			} else {
				dataOutput.writeObject(null)
			}
			dataOutput.writeInt(index)
		}
		dataOutput.close()
		return Base64Coder.encodeLines(outputStream.toByteArray())
	} catch (e: Exception) {
		throw IllegalStateException("Unable to save item stacks.", e)
	}

}

fun String.fromBase64ItemList(): Array<ItemStack?> {
	try {
		val inputStream = ByteArrayInputStream(Base64Coder.decodeLines(this))
		val dataInput = BukkitObjectInputStream(inputStream as InputStream)
		val size = dataInput.readInt()
		val list = arrayOfNulls<ItemStack>(size)
		for (i in 0 until size) {
			val utf = dataInput.readObject()
			val slot = dataInput.readInt()
			if (utf != null) {
				list[slot] = (utf as String).fromBase64Item()
			}
		}
		dataInput.close()
		return list
	} catch (e: Exception) {
		throw IllegalStateException("Unable to load item stacks.", e)
	}

}