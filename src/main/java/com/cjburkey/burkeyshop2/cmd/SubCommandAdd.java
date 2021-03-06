package com.cjburkey.burkeyshop2.cmd;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.cjburkey.burkeyshop2.BurkeyShop2;
import com.cjburkey.burkeyshop2.Util;
import net.milkbowl.vault.item.Items;

public class SubCommandAdd extends SubCommand {
	
	public SubCommandAdd() {
		super("add", false, true);
	}
	
	public String getDescription() {
		return "Adds the held item to the shop for the provided prices";
	}
	
	public int getRequiredArguments() {
		return 0;
	}
	
	public String[] getArguments() {
		return new String[] { "buy-price", "sell-price" };
	}
	
	public String onCall(SubCommandHandler commandHandler, CommandSender sender, String prefix, String[] args) {
		double buy = -1.0d;
		double sell = -1.0d;
		try {
			if (args.length > 0) {
				buy = Double.parseDouble(args[0]);
			}
			if (args.length > 1) {
				sell = Double.parseDouble(args[1]);
			}
		} catch (Exception e) {
			return Util.getLang("invalidPrices");
		}
		if (buy < 0.0f && sell < 0.0f) {
			return Util.getLang("notInShop");
		}
		Player ply = (Player) sender;
		ItemStack inHand = ply.getInventory().getItemInMainHand();
		if (inHand == null || inHand.getType().equals(Material.AIR) || inHand.getAmount() < 1) {
			return Util.getLang("holdItem");
		}
		if (BurkeyShop2.getInstance().getShop().addItem(inHand, buy, sell)) {
			String name = Items.itemByStack(inHand).name;
			return Util.getLang("itemAdded", name.substring(0, 1).toUpperCase() + name.substring(1));
		}
		return Util.getLang("itemExists");
	}
	
}