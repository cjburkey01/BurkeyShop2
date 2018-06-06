package com.cjburkey.burkeyshop2.cmd;

import org.bukkit.command.CommandSender;
import com.cjburkey.burkeyshop2.Util;

public class SubCommandHelp extends SubCommand {
	
	public SubCommandHelp() {
		super("help", false, false);
	}
	
	public String getDescription() {
		return "Displays information about commands";
	}
	
	public int getRequiredArguments() {
		return 0;
	}
	
	public String[] getArguments() {
		return new String[] { "sub-command" };
	}
	
	public String onCall(SubCommandHandler commandHandler, CommandSender sender, String prefix, String[] args) {
		if (args.length == 0) {
			StringBuilder builder = new StringBuilder();
			for (SubCommand command : commandHandler.getSubCommands()) {
				builder.append(command.getUsage(prefix));
				builder.append('\n');
			}
			builder.deleteCharAt(builder.length() - 1);
			return builder.toString();
		}
		SubCommand cmd = commandHandler.getSubCommand(args[0]);
		if (cmd == null) {
			return Util.getLang("cmdNotFound");
		}
		return cmd.getUsage(prefix) + "\n  &6" + cmd.getDescription();
	}
	
}