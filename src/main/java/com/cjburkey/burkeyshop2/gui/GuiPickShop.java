package com.cjburkey.burkeyshop2.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.cjburkey.burkeyshop2.BurkeyShop2;
import com.cjburkey.burkeyshop2.Util;

public class GuiPickShop implements IGui {
	
	public void onOpen(Inventory inventory, Player player) {
		ItemStack buy = GuiHandler.createItem("buy", Util.getLang("guiShopBuy"));
		ItemStack sell = GuiHandler.createItem("sell", Util.getLang("guiShopSell"));
		ItemStack exit = GuiHandler.createItem("exit", Util.getLang("guiShopExit"));
		
		inventory.setItem(2, buy);
		inventory.setItem(3, sell);
		inventory.setItem(5, exit);
	}
	
	public void onClose(Inventory inventory, Player player) {
	}
	
	public void onClick(Inventory inventory, Player player, int slot, ClickType clickType, ItemStack stack) {
		if (slot == 2) {
			open(player, true);
			return;
		}
		if (slot == 3) {
			open(player, false);
			return;
		}
		if (slot == 5) {
			player.closeInventory();
		}
	}
	
	private void open(Player player, boolean buying) {
		BurkeyShop2.getInstance().getGuiHandler().openGui(player, new GuiShop(BurkeyShop2.getInstance().getShop(), buying));
	}
	
	public String getName() {
		return Util.getLang("guiPickShopName");
	}
	
	public int getRows() {
		return 1;
	}
	
}