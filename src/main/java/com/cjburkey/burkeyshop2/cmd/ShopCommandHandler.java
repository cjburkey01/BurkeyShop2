package com.cjburkey.burkeyshop2.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.cjburkey.burkeyshop2.BurkeyShop2;
import com.cjburkey.burkeyshop2.Util;

public class ShopCommandHandler implements CommandExecutor {
	
	public static final SubCommandHandler commandHandler = new SubCommandHandler();
	
	private static SubCommand helpCommand;
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!command.getName().equalsIgnoreCase("shop")) {
			return false;
		}
		if (helpCommand == null) {
			helpCommand = commandHandler.getSubCommand("help");
			if (helpCommand == null) {
				Util.log("Failed to locate the help command");
				Util.msgLang(sender, "error");
				return true;
			}
		}
		if (sender instanceof Player) {
			Player ply = (Player) sender;
			if (!ply.hasPermission("burkeyshop2.use")) {
				Util.msgLang(sender, "noPermission");
				return true;
			}
			if (args.length == 0) {
				BurkeyShop2.getInstance().getShop().openShop((Player) sender);
				return true;
			}
		} else if (args.length == 0) {
			Util.msg(sender, Util.getLang("usagePrefix") + "/shop help");
		}
		String out = commandHandler.onCall(sender, command.getName(), args);
		if (out != null) {
			Util.msg(sender, out);
		}
		return true;
	}
	
}