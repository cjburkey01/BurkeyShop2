package com.cjburkey.burkeyshop2;

import org.bukkit.plugin.java.JavaPlugin;
import com.cjburkey.burkeyshop2.cmd.ShopCommandHandler;
import com.cjburkey.burkeyshop2.cmd.SubCommandAdd;
import com.cjburkey.burkeyshop2.cmd.SubCommandDelete;
import com.cjburkey.burkeyshop2.cmd.SubCommandEdit;
import com.cjburkey.burkeyshop2.cmd.SubCommandHelp;
import com.cjburkey.burkeyshop2.cmd.SubCommandReload;
import com.cjburkey.burkeyshop2.gui.GuiHandler;
import com.cjburkey.burkeyshop2.shop.ShopHandler;

public class BurkeyShop2 extends JavaPlugin {
	
	private static BurkeyShop2 instance;
	
	private GuiHandler guiHandler;
	private ShopHandler shop;
	public final Econ economy = new Econ();
	
	public BurkeyShop2() {
		instance = this;
	}
	
	public void onEnable() {
		if (!economy.setupEconomy(this)) {
			Util.err("Failed to initialize BurkeyShop2, Vault could not be initialized.");
			Util.err("Please ensure that an economy plugin such as Essentials is installed with Vault.");
			disable();
			return;
		}
		
		getConfig().options().copyDefaults(true);
		saveConfig();
		reloadConfig();
		
		guiHandler = new GuiHandler();
		
		getCommand("shop").setExecutor(new ShopCommandHandler());
		
		ShopCommandHandler.commandHandler.addSubCommand(new SubCommandHelp());
		ShopCommandHandler.commandHandler.addSubCommand(new SubCommandAdd());
		ShopCommandHandler.commandHandler.addSubCommand(new SubCommandEdit());
		ShopCommandHandler.commandHandler.addSubCommand(new SubCommandDelete());
		ShopCommandHandler.commandHandler.addSubCommand(new SubCommandReload());
		
		load();
	}
	
	public void reload() {
		reloadConfig();
		load();
	}
	
	private void load() {
		if (shop == null) {
			Util.log("Loading bank data");
		} else {
			Util.log("Reloading bank data");
		}
		shop = ShopHandler.loadBankHandler();
	}
	
	private void disable() {
		getServer().getPluginManager().disablePlugin(this);
	}
	
	public static BurkeyShop2 getInstance() {
		return instance;
	}
	
	public GuiHandler getGuiHandler() {
		return guiHandler;
	}
	
	public ShopHandler getShop() {
		return shop;
	}
	
}