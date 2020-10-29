package org.geysermc.platform.fabric.item;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class TestItemRegistry implements ModInitializer {
	
	 public static final Item TEST_ITEM = new Item(new FabricItemSettings().group(ItemGroup.MISC));
	    
	 @Override
	 public void onInitialize() {
		 Registry.register(Registry.ITEM, new Identifier("geysermc:test_item"), TEST_ITEM);
	 }

}
