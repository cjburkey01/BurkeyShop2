package com.cjburkey.burkeyshop2;

import org.bukkit.plugin.java.JavaPlugin;
import com.cjburkey.burkeyshop2.cmd.ShopCommandHandler;
import com.cjburkey.burkeyshop2.cmd.SubCommandAdd;
import com.cjburkey.burkeyshop2.cmd.SubCommandRemove;
import com.cjburkey.burkeyshop2.cmd.SubCommandUpdate;
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
		Util.log("Setting up economy");
		if (!economy.setupEconomy(this)) {
			Util.err("Failed to initialize BurkeyShop2, Vault could not be initialized.");
			Util.err("Please ensure that an economy plugin such as Essentials is installed with Vault.");
			disable();
			return;
		}
		
		Util.log("Setting up config");
		getConfig().options().copyDefaults(true);
		saveConfig();
		reloadConfig();
		
		Util.log("Setting up GUIHandler");
		guiHandler = new GuiHandler();
		
		Util.log("Setting up commands");
		getCommand("shop").setExecutor(new ShopCommandHandler());
		
		Util.log("Setting up sub-commands");
		ShopCommandHandler.commandHandler.addSubCommand(new SubCommandHelp());
		ShopCommandHandler.commandHandler.addSubCommand(new SubCommandAdd());
		ShopCommandHandler.commandHandler.addSubCommand(new SubCommandUpdate());
		ShopCommandHandler.commandHandler.addSubCommand(new SubCommandRemove());
		ShopCommandHandler.commandHandler.addSubCommand(new SubCommandReload());
		
		Util.log("Setting up ShopHandler");
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
		shop = ShopHandler.loadShopHandler();
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