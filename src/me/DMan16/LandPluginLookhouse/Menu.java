package me.DMan16.LandPluginLookhouse;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import me.wmorales01.landplugin.models.Business;
import me.wmorales01.landplugin.models.House;

public class Menu implements Listener {
	private static String menuName = "&6&lLook&b&lHouse &a &b &c &d &e &f";
	private static ItemStack empty;
	private static ItemStack close;
	private static ItemStack next;
	private static ItemStack previous;
	private static final int lines = 4; //Of houses displayed! 1 - 4
	private static final int size = (lines + 2) * 9;
	
	public Menu() {
		empty = Utils.getItem(Material.GRAY_STAINED_GLASS_PANE,"",null,null,false);
		close = Utils.getItem(Material.BARRIER,"&cClose",null,null,false);
		next = Utils.getItem(Material.ARROW,"&aNext",null,null,false);
		previous = Utils.getItem(Material.ARROW,"&cPrevious",null,null,false);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onMenuClick(InventoryClickEvent event) {
		if (event.isCancelled()) return;
		int slot = event.getRawSlot();
		if (!menuName.equals(Utils.chatColorsToString(event.getView().getTitle()))) return;
		event.setCancelled(true);
		if (slot >= size) return;
		if (!event.getClick().isRightClick() && !event.getClick().isLeftClick()) return;
		Inventory inv = event.getInventory();
		ItemStack item = inv.getItem(slot);
		if (item == null || item.getType().isAir() || item.equals(empty)) return;
		Player player = (Player) event.getWhoClicked();
		if (slot == size - 5) player.closeInventory();
		else if (slot == size - 1 && item.equals(next)) {
			setPage(inv,getPage(inv) + 1);
			update(inv,player.getWorld().getName());
		} else if (slot == size - 9 && item.equals(previous)) {
			setPage(inv,getPage(inv) - 1);
			update(inv,player.getWorld().getName());
		} else if (slot >= 9 && slot < size - 9) try {
			String name = item.getItemMeta().getPersistentDataContainer().get(Utils.key("house"),PersistentDataType.STRING);
			House house = item.getItemMeta().getPersistentDataContainer().has(Utils.key("business"),PersistentDataType.STRING) ? Business.getFromName(Utils.land(),name) :
				House.getFromName(Utils.land(),name);
			Location loc = house.getSignLocation().clone().add(0,0.25,0);
			loc.setYaw(loc.getYaw() + 180);
			player.teleport(loc);
		} catch (Exception e) {}
	}
	
	public static void openMenu(Player player, double budget) {
		Inventory inv = Bukkit.createInventory(player,size,Utils.chatColors(menuName));
		for (int i = 0; i < 9; i++) {
			inv.setItem(i,empty);
			inv.setItem(size - 1 - i,empty);
		}
		setBudgetItem(inv,budget,1);
		inv.setItem(size - 5,close);
		update(inv,player.getWorld().getName());
		player.openInventory(inv);
	}
	
	private static void update(Inventory inv, String world) {
		double budget = getBudget(inv);
		List<House> houses = getByBudget(budget,world);
		int pages = (int) Math.ceil((double)houses.size() / (size - 2 * 9));
		int page = getPage(inv);
		if (houses.isEmpty() || page < 1 || page > pages) return;
		inv.setItem(size - 1,page < pages ? next : empty);
		inv.setItem(size - 9,page > 1 ? previous : empty);
		for (int i = 0; i < size - 2 * 9 && i < houses.size(); i++) inv.setItem(i + 9,null);
		for (int i = 0; i < (size - 2 * 9) && ((page - 1) * (size - 2 * 9) + i < houses.size()); i++) {
			House house = houses.get((page - 1) * (size - 2 * 9) + i);
			ItemStack item = HouseItem.get(house instanceof Business,budget,house.getPrice()).item(house.getName(),house.getPrice());
			inv.setItem(i + 9,item);
		}
	}
	
	private static List<House> getByBudget(double budget, String world) {
		budget = Double.parseDouble((new DecimalFormat("0.00")).format(budget * 1.1));
		List<House> all = new ArrayList<House>();
		List<House> houses = new ArrayList<House>();
		for (House house : Utils.land().getHouses())
			if (house.getOwnerUUID() == null && house.getCorner1().getWorld().getName().equals(world) && house.getPrice() <= budget) houses.add(house);
		for (Business business : Utils.land().getBusinesses())
			if (business.getOwnerUUID() == null && business.getCorner1().getWorld().getName().equals(world) && business.getPrice() <= budget) houses.add(business);
		while (!houses.isEmpty()) {
			int i = getNextHouse(houses);
			all.add(houses.get(i));
			houses.remove(i);
		}
		return all;
	}
	
	private static int getNextHouse(List<House> houses) {
		House min = houses.get(0);
		int idx = 0;
		for (int i = 1; i < houses.size(); i++) {
			House house = houses.get(i);
			boolean change = false;
			if (house.getPrice() < min.getPrice()) change = true;
			else if (house.getPrice() == min.getPrice()) {
				if ((min instanceof Business) && !(house instanceof Business)) change = true;
				else if (house.getName().compareTo(min.getName()) < 0) change = true;
			}
			if (change) {
				min = house;
				idx = i;
			}
		}
		return idx;
	}
	
	private static int getPage(Inventory inv) {
		ItemStack item = inv.getItem(4);
		return item.getItemMeta().getPersistentDataContainer().get(Utils.key("page"),PersistentDataType.INTEGER);
	}
	
	private static double getBudget(Inventory inv) {
		ItemStack item = inv.getItem(4);
		return item.getItemMeta().getPersistentDataContainer().get(Utils.key("budget"),PersistentDataType.DOUBLE);
	}
	
	private static void setPage(Inventory inv, int page) {
		setBudgetItem(inv,getBudget(inv),page);
	}
	
	private static void setBudgetItem(Inventory inv, double budget, int page) {
		ItemStack item = Utils.getItem(Material.EMERALD,"&6Budget: &f" + Utils.chatColorsToString(Utils.land().getEconomy().format(budget)),null,null,false);
		ItemMeta meta = item.getItemMeta();
		meta.getPersistentDataContainer().set(Utils.key("budget"),PersistentDataType.DOUBLE,budget);
		meta.getPersistentDataContainer().set(Utils.key("page"),PersistentDataType.INTEGER,page);
		item.setItemMeta(meta);
		inv.setItem(4,item);
	}
	
	public enum HouseItem {
		house1("&aHouse","eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjNkMDJjZGMwNzViYjFjYzVmNmZlM2M3NzExYWU0OTc3ZTM4YjkxMGQ1MGVkNjAyM2RmNzM5MTNlNWU3ZmNmZiJ9fX0="),
		house2("&eHouse","eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjg1NDA2MGFhNTc3NmI3MzY2OGM4OTg2NTkwOWQxMmQwNjIyNDgzZTYwMGI2NDZmOTBjMTg2YzY1Yjc1ZmY0NSJ9fX0="),
		house3("&cHouse","eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzc0MTBjMDdiZmJiNDE0NTAwNGJmOTE4YzhkNjMwMWJkOTdjZTEzMjcwY2UxZjIyMWQ5YWFiZWUxYWZkNTJhMyJ9fX0="),
		business1("&aBusiness","eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2I1NmU0OTA4NWY1NWQ1ZGUyMTVhZmQyNmZjNGYxYWZlOWMzNDMxM2VmZjk4ZTNlNTgyNDVkZWYwNmU1ODU4YyJ9fX0="),
		business2("&eBusiness","eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDk5MTBjZjYwMGQyMWEwNDA0ZDlkZjRiMGQ2NTllZDQ4NDE4NmFlMDYxNDI3MGY3YTY0MjlmNzA0ZDBiZGJjOSJ9fX0="),
		business3("&cBusiness","eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjI1YjI3Y2U2MmNhODg3NDM4NDBhOTVkMWMzOTg2OGY0M2NhNjA2OTZhODRmNTY0ZmJkN2RkYTI1OWJlMDBmZSJ9fX0=");
		
		private String type;
		private String texture;
		
		private static double mid = 1.2;
		
		HouseItem(String type,String texture) {
			this.type = type;
			this.texture = texture;
		}
		
		public static HouseItem get(boolean business, double budget,double price) {
			if (business) return price > budget ? business3 : (price * mid >= budget ? business2 : business1);
			else return price > budget ? house3 : (price * mid >= budget ? house2 : house1);
		}
		
		public ItemStack item(String name, double cost) {
			List<String> lore = new ArrayList<String>();
			lore.add("");
			lore.add(Utils.chatColors(type));
			lore.add(Utils.chatColors("&6&lCost: &f" + Utils.chatColorsToString(Utils.land().getEconomy().format(cost))));
			boolean business = (this == business1) || (this == business2) || (this == business3);
			ItemStack item = Utils.getItem(Material.PLAYER_HEAD,"&f" + Utils.chatColorsToString(name),lore,name,business);
			item.setItemMeta(Utils.makeSkull((SkullMeta) item.getItemMeta(),texture));
			return item;
		}
	}
}