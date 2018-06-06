package com.cjburkey.burkeyshop2.cmd;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.cjburkey.burkeyshop2.BurkeyShop2;
import com.cjburkey.burkeyshop2.Util;
import net.milkbowl.vault.item.Items;

public class SubCommandRemove extends SubCommand {
	
	public SubCommandRemove() {
		super("remove", false, true);
	}
	
	public String getDescription() {
		return "Removes the held item from the shop";
	}
	
	public int getRequiredArguments() {
		return 0;
	}
	
	public String[] getArguments() {
		return new String[0];
	}
	
	public String onCall(SubCommandHandler commandHandler, CommandSender sender, String prefix, String[] args) {
		Player ply = (Player) sender;
		ItemStack inHand = ply.getInventory().getItemInMainHand();
		if (inHand == null || inHand.getType().equals(Material.AIR) || inHand.getAmount() < 1) {
			return Util.getLang("holdItem");
		}
		if (BurkeyShop2.getInstance().getShop().removeItem(inHand)) {
			String name = Items.itemByStack(inHand).name;
			return Util.getLang("itemRemoved", name.substring(0, 1).toUpperCase() + name.substring(1));
		}
		return Util.getLang("itemNotExists");
	}
	
}