package me.DMan16.LandPluginLookhouse;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Lookhouse extends JavaPlugin {
	static Lookhouse main;
	
	public void onEnable() {
		main = this;
		new CommandListener();
		Bukkit.getPluginManager().registerEvents(new Menu(),this);
	}
}