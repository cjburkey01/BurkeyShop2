package com.cjburkey.burkeyshop2.shop;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.cjburkey.burkeyshop2.BurkeyShop2;
import com.cjburkey.burkeyshop2.Econ;
import com.cjburkey.burkeyshop2.ShopIO;
import com.cjburkey.burkeyshop2.Util;
import com.cjburkey.burkeyshop2.gui.GuiPickShop;

public class ShopHandler {
	
	private final Set<ShopItem> shopItems = new HashSet<ShopItem>();
	
	private ShopHandler() {
	}
	
	public ShopItem[] getItems(boolean buying) {
		Set<ShopItem> items = new HashSet<>();
		for (ShopItem item : shopItems) {
			if ((buying && item.getIsBuyingEnabled()) || (!buying && item.getIsSellingEnabled())) {
				items.add(item);
			}
		}
		return items.toArray(new ShopItem[0]);
	}
	
	public ShopItem[] getItemsOnPage(boolean buying, int page, int perPage) {
		ShopItem[] items = getItems(buying);
		if (page * perPage >= items.length) {
			return null;
		}
		ShopItem[] outItems = new ShopItem[Math.min(items.length - (page * perPage), perPage)];
		for (int i = page * perPage; i < ((page + 1) * perPage) && (i < items.length); i ++) {
			outItems[i - (page * perPage)] = items[i].clone();
		}
		return outItems;
	}
	
	public boolean getHasNextPage(boolean buying, int page, int perPage) {
		return ((page + 1) * perPage) < getItems(buying).length;
	}
	
	public boolean getHasPreviousPage(int page) {
		return page > 0;
	}
	
	public int getPages(boolean buying, int perPage) {
		return (int) Math.ceil(getItems(buying).length / (float) perPage);
	}
	
	public ShopItem getItem(Material item, short data) {
		for (ShopItem shopItem : shopItems) {
			if (shopItem.getMaterial().equals(item) && shopItem.getData() == data) {
				return shopItem;
			}
		}
		return null;
	}
	
	public boolean updateItem(ItemStack stack, double buyPrice, double sellPrice) {
		return updateItem(stack.getType(), stack.getDurability(), buyPrice, sellPrice);
	}
	
	public boolean updateItem(Material item, short data, double buyPrice, double sellPrice) {
		ShopItem sitem = getItem(item, data);
		if (sitem == null) {
			return false;
		}
		sitem.update(buyPrice, sellPrice);
		save();
		return true;
	}
	
	public boolean addItem(ItemStack stack, double buyPrice, double sellPrice) {
		return addItem(stack.getType(), stack.getDurability(), buyPrice, sellPrice);
	}
	
	public boolean addItem(Material item, short data, double buyPrice, double sellPrice) {
		if (getItem(item, data) != null) {
			return false;
		}
		shopItems.add(new ShopItem(item, data, buyPrice, sellPrice));
		save();
		return true;
	}
	
	public boolean removeItem(ItemStack stack) {
		return removeItem(stack.getType(), stack.getDurability());
	}
	
	public boolean removeItem(Material item, short data) {
		for (ShopItem shopItem : shopItems) {
			if (shopItem.getMaterial().equals(item) && shopItem.getData() == data) {
				shopItems.remove(shopItem);
				save();
				return true;
			}
		}
		return false;
	}
	
	public void openShop(Player player) {
		BurkeyShop2.getInstance().getGuiHandler().openGui(player, new GuiPickShop());
	}
	
	public BuyResult tryBuy(Player player, ShopItem item, int count) {
		if (!item.getIsBuyingEnabled()) {
			return BuyResult.CANNOT_BUY;
		}
		ItemStack stack = item.createStack(count);
		if (stack.getAmount() > stack.getMaxStackSize()) {
			stack.setAmount(stack.getMaxStackSize());
		}
		Econ econ = BurkeyShop2.getInstance().economy;
		if (!econ.buy(player.getUniqueId(), stack.getAmount() * item.getBuyPrice())) {
			return BuyResult.NOT_ENOUGH_MONEY;
		}
		HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(stack.clone());
		if (remaining.size() > 0) {
			econ.addMoney(player.getUniqueId(), remaining.entrySet().iterator().next().getValue().getAmount() * item.getBuyPrice());		// Refund the items that didn't fit
			return BuyResult.INVENTORY_FULL;
		}
		return BuyResult.SUCCESS;
	}
	
	public SellResult trySell(Player player, ShopItem item, int count) {
		if (!item.getIsSellingEnabled()) {
			return SellResult.CANNOT_SELL;
		}
		ItemStack stack = item.createStack(count);
		if (stack.getAmount() > stack.getMaxStackSize()) {
			stack.setAmount(stack.getMaxStackSize());
		}
		if (!player.getInventory().containsAtLeast(stack, stack.getAmount())) {
			return SellResult.NOT_ENOUGH_ITEMS;
		}
		player.getInventory().removeItem(stack);
		BurkeyShop2.getInstance().economy.addMoney(player.getUniqueId(), stack.getAmount() * item.getSellPrice());
		return SellResult.SUCCESS;
	}
	
	// Saving and loading handling
	
	public void save() {
		ShopIO.writeFile(ShopIO.getDataFile(), ShopIO.getGson().toJson(this));
	}
	
	public static ShopHandler loadBankHandler() {
		String file = ShopIO.readFile(ShopIO.getDataFile());
		if (file != null) {
			try {
				ShopHandler handler = ShopIO.getGson().fromJson(file, ShopHandler.class);
				if (handler == null) {
					throw new Exception("The shop file could not be parsed");
				}
				return handler;
			} catch (Exception e) {
				Util.log("Failed to load shop items: " + e.getMessage());
				e.printStackTrace();
			}
		}
		Util.log("Creating a new shop handler");
		ShopHandler nbh = new ShopHandler();
		nbh.save();
		return nbh;
	}
	
}