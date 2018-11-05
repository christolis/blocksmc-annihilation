package me.blocksmc.annihilation;

import org.bukkit.inventory.ItemStack;

public class CustomMap {

    private ItemStack mapItem;

    public CustomMap(ItemStack mapItem) {
        this.mapItem = mapItem;
    }

    public ItemStack getMapItem() {
        return mapItem;
    }

    public void setMapItem(ItemStack newItem) {
        this.mapItem = newItem;

    }
}