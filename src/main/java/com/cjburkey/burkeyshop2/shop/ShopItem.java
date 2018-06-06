package com.cjburkey.burkeyshop2.shop;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ShopItem {
	
	private Material material;
	private short data;
	private double buyPrice;
	private double sellPrice;
	
	public ShopItem(Material material, short data, double buyPrice, double sellPrice) {
		this.material = material;
		this.data = data;
		update(buyPrice, sellPrice);
	}
	
	public void update(double buyPrice, double sellPrice) {
		this.buyPrice = buyPrice;
		this.sellPrice = sellPrice;
	}
	
	public boolean getIsBuyingEnabled() {
		return buyPrice >= 0.0d;
	}
	
	public boolean getIsSellingEnabled() {
		return sellPrice >= 0.0d;
	}
	
	public ItemStack createStack(int size) {
		return new ItemStack(material, size, data);
	}
	
	public Material getMaterial() {
		return material;
	}
	
	public short getData() {
		return data;
	}
	
	public double getBuyPrice() {
		return buyPrice;
	}
	
	public double getSellPrice() {
		return sellPrice;
	}
	
	public int getMaxStack() {
		return createStack(1).getMaxStackSize();
	}
	
	public ShopItem clone() {
		return new ShopItem(material, data, buyPrice, sellPrice);
	}
	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(buyPrice);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + data;
		result = prime * result + ((material == null) ? 0 : material.hashCode());
		temp = Double.doubleToLongBits(sellPrice);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ShopItem other = (ShopItem) obj;
		if (Double.doubleToLongBits(buyPrice) != Double.doubleToLongBits(other.buyPrice)) {
			return false;
		}
		if (data != other.data) {
			return false;
		}
		if (material != other.material) {
			return false;
		}
		if (Double.doubleToLongBits(sellPrice) != Double.doubleToLongBits(other.sellPrice)) {
			return false;
		}
		return true;
	}
	
	public String toString() {
		return material.toString() + ":" + data;
	}
	
}