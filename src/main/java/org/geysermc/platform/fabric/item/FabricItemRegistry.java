package org.geysermc.platform.fabric.item;

import org.geysermc.connector.network.translators.item.ItemEntry;
import org.geysermc.connector.network.translators.item.ItemRegistry;

import com.nukkitx.protocol.bedrock.packet.StartGamePacket;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public class FabricItemRegistry {
	
	public static void registerItems() {
		for (int count = 1; count <= Registry.ITEM.getEntries().size(); count++) {
			Item currentItem = Registry.ITEM.get(count);
			if (!Registry.ITEM.getId(currentItem).getNamespace().equals("minecraft")) {
				if (!(currentItem instanceof BlockItem)) {
					ItemRegistry.ITEMS.add(new StartGamePacket.ItemEntry(Registry.ITEM.getId(currentItem).getNamespace() + ":" + Registry.ITEM.getId(currentItem).getPath(), (short) ((short)Registry.ITEM.getRawId(currentItem))));
					ItemRegistry.ITEM_ENTRIES.put(count, new ItemEntry(Registry.ITEM.getId(currentItem).getNamespace() + ":" + Registry.ITEM.getId(currentItem).getPath(), Registry.ITEM.getRawId(currentItem), Registry.ITEM.getRawId(currentItem), 0, false));
					ItemRegistry.addToCreativeMenu(count, Registry.ITEM.getRawId(currentItem), currentItem.getMaxDamage(), 0);		
				}
				else {
					ItemRegistry.ITEMS.add(new StartGamePacket.ItemEntry(Registry.ITEM.getId(currentItem).getNamespace() + ":" + Registry.ITEM.getId(currentItem).getPath(), (short) ((short)Registry.ITEM.getRawId(currentItem))));
					ItemRegistry.ITEM_ENTRIES.put(count, new ItemEntry(Registry.ITEM.getId(currentItem).getNamespace() + ":" + Registry.ITEM.getId(currentItem).getPath(), Registry.ITEM.getRawId(currentItem), Registry.ITEM.getRawId(currentItem), 0, true));
					ItemRegistry.addToCreativeMenu(count, Registry.ITEM.getRawId(currentItem), currentItem.getMaxDamage(), 0);		
				}
			}
		}
	}
}