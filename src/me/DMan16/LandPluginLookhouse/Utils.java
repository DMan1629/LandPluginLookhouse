package me.DMan16.LandPluginLookhouse;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import me.wmorales01.landplugin.LandPlugin;

public class Utils {
	public static String chatColors(String str) {
		return ChatColor.translateAlternateColorCodes('&',str);
	}
	
	public static String chatColorsToString(String str) {
		return str.replace("§","&");
	}
	
	public static String chatColorsStrip(String str) {
		return ChatColor.stripColor(str);
	}
	
	public static ItemStack getItem(Material material, String name, List<String> lore, String houseName, boolean business) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		if (name != null) meta.setDisplayName(chatColors(name.isEmpty() ? " " : name));
		meta.addItemFlags(ItemFlag.values());
		if (lore != null) {
			for (int i = 0; i < lore.size(); i++) lore.set(i,chatColors(lore.get(i)));
			meta.setLore(lore);
		}
		if (houseName != null) {
			meta.getPersistentDataContainer().set(key("house"),PersistentDataType.STRING,houseName);
			if (business) meta.getPersistentDataContainer().set(key("business"),PersistentDataType.STRING,"");
		}
		item.setItemMeta(meta);
		return item;
	}
	
	public static LandPlugin land() {
		return (LandPlugin) Bukkit.getServer().getPluginManager().getPlugin("LandPlugin");
	}
	
	public static NamespacedKey key(String str) {
		return new NamespacedKey(Lookhouse.main,str);
	}
	
	public static SkullMeta makeSkull(SkullMeta meta, String texture) {
		try {
			Method metaSetProfileMethod = meta.getClass().getDeclaredMethod("setProfile",GameProfile.class);
			metaSetProfileMethod.setAccessible(true);
			UUID id = new UUID(texture.substring(texture.length() - 20).hashCode(),texture.substring(texture.length() - 10).hashCode());
			GameProfile profile = new GameProfile(id,"D");
			profile.getProperties().put("textures", new Property("textures",texture));
			metaSetProfileMethod.invoke(meta,profile);
		} catch (Exception e) {}
		return meta;
	}
}