package com.cjburkey.burkeyshop2.shop;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.cjburkey.burkeyshop2.BurkeyShop2;
import com.cjburkey.burkeyshop2.Econ;
import com.cjburkey.burkeyshop2.ShopIO;
import com.cjburkey.burkeyshop2.Util;
import com.cjburkey.burkeyshop2.gui.GuiPickShop;

public class ShopHandler {
	
	private boolean _loadedOld;
	private final List<ShopItem> shopItems = new LinkedList<ShopItem>();
	
	private ShopHandler() {
	}
	
	public ShopItem[] getItems(boolean buying) {
		List<ShopItem> items = new LinkedList<>();
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
		sortAndSave();
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
		sortAndSave();
		return true;
	}
	
	public boolean removeItem(ItemStack stack) {
		return removeItem(stack.getType(), stack.getDurability());
	}
	
	public boolean removeItem(Material item, short data) {
		for (ShopItem shopItem : shopItems) {
			if (shopItem.getMaterial().equals(item) && shopItem.getData() == data) {
				shopItems.remove(shopItem);
				sortAndSave();
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
		if (count < 1) {
			return SellResult.NOT_ENOUGH_ITEMS;
		}
		ItemStack stack = item.createStack(1);
		if (!player.getInventory().containsAtLeast(stack, stack.getAmount())) {
			return SellResult.NOT_ENOUGH_ITEMS;
		}
		
		int tmp = count;
		// This is the FIRST TIME I have used a Do-While loop...EVER
		do {
			ItemStack stmp = stack.clone();
			stmp.setAmount(Math.min(stack.getMaxStackSize(), tmp));
			tmp -= stack.getMaxStackSize();
			player.getInventory().removeItem(stmp);
		} while (tmp > 0);
		
		BurkeyShop2.getInstance().economy.addMoney(player.getUniqueId(), count * item.getSellPrice());
		return SellResult.SUCCESS;
	}
	
	// Saving and loading handling
	
	public void sortAndSave() {
		sort();
		ShopIO.writeFile(ShopIO.getDataFile(), ShopIO.getGson().toJson(this));
	}
	
	public void sort() {
		if (BurkeyShop2.getInstance().getConfig().getBoolean("shop.sort")) {
			shopItems.sort((ShopItem o1, ShopItem o2) -> o1.compareTo(o2));
			Util.log("Sorted shop");
		}
	}
	
	public static ShopHandler loadShopHandler() {
		String file = ShopIO.readFile(ShopIO.getDataFile());
		if (file != null) {
			try {
				ShopHandler handler = ShopIO.getGson().fromJson(file, ShopHandler.class);
				if (handler == null) {
					throw new Exception("The shop JSON could not be parsed");
				}
				convertOldFile(handler);
				handler.sort();
				return handler;
			} catch (Exception e) {
				Util.log("Failed to load shop items: " + e.getMessage());
				e.printStackTrace();
			}
		}
		Util.log("Creating a new shop handler");
		ShopHandler nbh = new ShopHandler();
		Util.log("Looking for old shop.txt");
		convertOldFile(nbh);
		return nbh;
	}
	
	private static void convertOldFile(ShopHandler shop) {
		if (shop._loadedOld) {
			Util.log("Already loaded the old shop.txt");
			return;
		}
		if (!ShopIO.getOldDataFile().exists()) {
			return;	// No old file present.
		}
		String file = ShopIO.readFile(ShopIO.getOldDataFile());
		if (file == null) {
			return;
		}
		Util.log("Loading old shop.txt data");
		String[] spl = file.split(Pattern.quote("\n"));
		for (String line : spl) {
			line = line.trim();
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}
			String[] item = line.split(Pattern.quote(";"));
			if (item.length != 3) {
				Util.err("Failed to parse line in old shop.txt file: " + line);
				continue;
			}
			short data = 0;
			Material itemMat = null;
			if (item[0].contains(":")) {
				String[] dat = item[0].split(Pattern.quote(":"));
				if (dat.length == 2) {
					try {
						itemMat = Material.valueOf(dat[0]);
						data = Short.parseShort(dat[1]);
					} catch (Exception e) {
						Util.err("Failed to read data in old shop.txt file: " + item[0]);
					}
					if (itemMat == null) {
						continue;
					}
				}
			} else {
				try {
					itemMat = Material.valueOf(item[0]);
				} catch (Exception e) {
					Util.err("Failed to determine material in old shop.txt: " + item[0]);
					continue;
				}
			}
			double buy = -1.0f;
			double sell = -1.0f;
			try {
				buy = Double.parseDouble(item[1]);
				sell = Double.parseDouble(item[2]);
			} catch (Exception e) {
				Util.err("Failed to read prices in old shop.txt: " + line);
				continue;
			}
			if (!shop.addItem(itemMat, data, buy, sell)) {
				Util.err("Failed to add item to shop from old shop.txt: " + line);
			}
		}
		shop._loadedOld = true;
		shop.sortAndSave();
		Util.log("Loaded old shop.txt data");
	}
	
}