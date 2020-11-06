package org.geysermc.platform.fabric.block;

import java.lang.annotation.Annotation;

import org.geysermc.connector.GeyserConnector;
import org.geysermc.connector.network.translators.world.block.BlockTranslator;
import org.geysermc.connector.network.translators.world.block.entity.BlockEntityTranslator;
import org.geysermc.connector.utils.FileUtils;
import org.reflections.Reflections;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.registry.Registry;

public class FabricBlockRegistry {
	
	public static void registerBlocks() {
	
        for (int count = 1; count <= Registry.BLOCK.getEntries().size(); count++) {
			Block currentItem = Registry.BLOCK.get(count);
			if (!Registry.BLOCK.getId(currentItem).getNamespace().equals("minecraft")) {
				
			}
		}
	}
}
