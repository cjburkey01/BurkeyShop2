package com.cjburkey.burkeyshop2.gui;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.cjburkey.burkeyshop2.BurkeyShop2;
import com.cjburkey.burkeyshop2.Util;
import com.cjburkey.burkeyshop2.shop.BuyResult;
import com.cjburkey.burkeyshop2.shop.SellResult;
import com.cjburkey.burkeyshop2.shop.ShopHandler;
import com.cjburkey.burkeyshop2.shop.ShopItem;

public class GuiShop implements IGui {
	
	public static final int ITEMS_PER_PAGE = 5 * 9;
	
	private int page = 0;
	private final ShopHandler shopHandler;
	private final boolean buying;
	private Inventory inventory;
	private Player player;
	private ShopItem[] items;
	
	public GuiShop(ShopHandler shopHandler, boolean buying) {
		this.shopHandler = shopHandler;
		this.buying = buying;
	}
	
	public void onOpen(Inventory inventory, Player player) {
		this.inventory = inventory;
		this.player = player;
		refresh();
	}
	
	public void onClose(Inventory inventory, Player player) {
	}
	
	public void onClick(Inventory inventory, Player player, int slot, ClickType clickType, ItemStack stack) {
		if (!(clickType.isLeftClick() || clickType.isShiftClick() || clickType.isRightClick())) {
			return;
		}
		if (items == null) {
			Util.log("Shop received a click event before it had been built");
			Util.msgLang(player, "error");
			return;
		}
		if (slot < items.length) {
			ShopItem item = items[slot];
			if (buying) {
				if (!(clickType.isLeftClick() || clickType.isShiftClick())) {
					return;
				}
				BuyResult result = shopHandler.tryBuy(player, item, (clickType.isShiftClick()) ? item.getMaxStack() : 1);
				switch (result) {
				case CANNOT_BUY:
					Util.msgLang(player, "guiShopNotAvailable");
					break;
				case NOT_ENOUGH_MONEY:
					Util.msgLang(player, "guiNotEnoughMoney");
					break;
				case INVENTORY_FULL:
					Util.msgLang(player, "guiInventoryFull");
					break;
				default:
					break;
				}
				return;
			}
			SellResult result = shopHandler.trySell(player, item, ((clickType.isRightClick()) ? getCountOfItemOnPlayer(player, stack) : ((clickType.isShiftClick()) ? item.getMaxStack() : 1)));
			switch (result) {
			case CANNOT_SELL:
				Util.msgLang(player, "guiShopNotAvailable");
				break;
			case NOT_ENOUGH_ITEMS:
				Util.msgLang(player, "guiShopNotEnoughItems");
				break;
			default:
				break;
			}
			return;
		}
		
		// Previous Page
		if (slot == 45) {
			page --;
			refresh();
		}
		// Back
		if (slot == 49) {
			shopHandler.openShop(player);
		}
		// Next Page
		if (slot == 53) {
			page ++;
			refresh();
		}
	}
	
	private int getCountOfItemOnPlayer(Player player, ItemStack stack) {
		int count = 0;
		for (ItemStack stack1 : player.getInventory().getContents()) {
			if (stack1 != null && stack1.getType().equals(stack.getType()) && stack1.getDurability() == stack.getDurability()) {
				count += stack1.getAmount();
			}
		}
		return count;
	}
	
	public String getName() {
		return Util.getLang("guiShopName") + ((buying) ? Util.getLang("guiShopBuying") : Util.getLang("guiShopSelling"));
	}
	
	public int getRows() {
		return 6;
	}
	
	public void refresh() {
		inventory.clear();
		items = shopHandler.getItemsOnPage(buying, page, ITEMS_PER_PAGE);
		if (items == null) {
			Util.log("Shop out of bounds for player: " + player.getName() + " on page " + page + " when there can only be " + shopHandler.getPages(buying, ITEMS_PER_PAGE));
			Util.msg(player, Util.getLang("error"));
			player.closeInventory();
			return;
		}
		for (int i = 0; i < items.length; i ++) {
			if (items[i] == null) {
				Util.log("Item was null at index " + i + " which is item " + (i + page * ITEMS_PER_PAGE) + " in the shop");
				continue;
			}
			ItemStack stack = items[i].createStack(1);
			ItemMeta meta = stack.getItemMeta();
			List<String> lore = new ArrayList<>();
			if (buying) {
				lore.add(Util.getLang("guiShopBuyPrice", BurkeyShop2.getInstance().economy.format(items[i].getBuyPrice())));
				lore.add(Util.getLang("guiShopInstBuy", 1, "Left Click"));
				lore.add(Util.getLang("guiShopInstBuy", stack.getMaxStackSize(), "Shift + Left Click"));
			} else {
				lore.add(Util.getLang("guiShopSellPrice", BurkeyShop2.getInstance().economy.format(items[i].getSellPrice())));
				lore.add(Util.getLang("guiShopInstSell", 1, "Click"));
				lore.add(Util.getLang("guiShopInstSell", stack.getMaxStackSize(), "Shift + Left Click"));
				lore.add(Util.getLang("guiShopInstSell", "All", "Right Click"));
			}
			meta.setLore(lore);
			stack.setItemMeta(meta);
			inventory.setItem(i, stack);
		}
		
		// Non-shop items
		
		if (shopHandler.getHasPreviousPage(page)) {
			ItemStack prev = GuiHandler.createItem("previous", Util.getLang("guiShopPrevious"));
			inventory.setItem(45, prev);
		}
		ItemStack back = GuiHandler.createItem("exit", Util.getLang("guiShopBack"));
		inventory.setItem(49, back);
		if (shopHandler.getHasNextPage(buying, page, ITEMS_PER_PAGE)) {
			ItemStack next = GuiHandler.createItem("next", Util.getLang("guiShopNext"));
			inventory.setItem(53, next);
		}
	}
	
}