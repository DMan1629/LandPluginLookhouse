package me.DMan16.LandPluginLookhouse;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class CommandListener implements CommandExecutor,TabCompleter {
	
	public CommandListener() {
		PluginCommand command = Lookhouse.main.getCommand("lookhouse");
		command.setExecutor(this);
		command.setTabCompleter(this);
		command.setUsage("lookhouse <budget>");
		command.setDescription(Utils.chatColors("&fFind affordable Houses and Business"));
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return false;
		double budget;
		if (args.length > 0) {
			budget = Double.parseDouble(args[0]);
		} else budget =Utils.land().getEconomy().getBalance((Player) sender);
		if (budget <= 0) return true;
		Menu.openMenu((Player) sender,budget);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> reusltList = new ArrayList<String>();
		if (args.length == 1) reusltList.add("<budget>");
		return reusltList;
	}
}