package com.cjburkey.burkeyshop2.cmd;

import org.bukkit.command.CommandSender;
import com.cjburkey.burkeyshop2.BurkeyShop2;
import com.cjburkey.burkeyshop2.Util;

public class SubCommandReload extends SubCommand {
	
	public SubCommandReload() {
		super("reload", false, false);
	}
	
	public String getDescription() {
		return "Reloads the shop from the shop.json file";
	}
	
	public int getRequiredArguments() {
		return 0;
	}
	
	public String[] getArguments() {
		return new String[0];
	}
	
	public String getPermission() {
		return "burkeyshop2.admin";
	}
	
	public String onCall(SubCommandHandler commandHandler, CommandSender sender, String prefix, String[] args) {
		BurkeyShop2.getInstance().reload();
		return Util.getLang("shopReloaded");
	}
	
}