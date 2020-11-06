/*
 * Copyright (c) 2020 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Geyser
 */

package org.geysermc.platform.fabric;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.jar.JarEntry;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.geysermc.connector.GeyserConnector;
import org.geysermc.connector.GeyserLogger;
import org.geysermc.connector.bootstrap.GeyserBootstrap;
import org.geysermc.connector.command.CommandManager;
import org.geysermc.connector.command.GeyserCommand;
import org.geysermc.connector.common.PlatformType;
import org.geysermc.connector.configuration.GeyserConfiguration;
import org.geysermc.connector.dump.BootstrapDumpInfo;
import org.geysermc.connector.network.translators.item.ItemEntry;
import org.geysermc.connector.network.translators.item.ItemRegistry;
import org.geysermc.connector.ping.GeyserLegacyPingPassthrough;
import org.geysermc.connector.ping.IGeyserPingPassthrough;
import org.geysermc.connector.utils.FileUtils;
import org.geysermc.connector.utils.LanguageUtils;
import org.geysermc.platform.fabric.block.FabricBlockRegistry;
import org.geysermc.platform.fabric.command.GeyserFabricCommandExecutor;
import org.geysermc.platform.fabric.command.GeyserFabricCommandManager;
import org.geysermc.platform.fabric.item.FabricItemRegistry;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.nukkitx.protocol.bedrock.packet.StartGamePacket;

import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.block.SimpleReplacementPoly;
import io.github.theepicblock.polymc.api.item.CustomModelDataPoly;
import io.github.theepicblock.polymc.api.register.PolyRegistry;
import io.github.theepicblock.polymc.resource.AdvancedResourcePackMaker;
import io.github.theepicblock.polymc.resource.JsonModel;
import io.github.theepicblock.polymc.resource.ResourcePackGenerator;
import io.github.theepicblock.polymc.resource.ResourcePackMaker;
import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.blockstate.JBlockModel;
import net.devtech.arrp.json.models.JModel;
import net.devtech.arrp.json.models.JTextures;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@Environment(EnvType.SERVER)
public class GeyserFabricMod extends PolyRegistry implements ModInitializer, DedicatedServerModInitializer, GeyserBootstrap {
	
    private GeyserConnector connector;
    public static Path dataFolder;
    private List<String> playerCommands;
    private MinecraftServer server;

    private GeyserFabricCommandManager geyserCommandManager;
    private GeyserFabricConfiguration geyserConfig;
    private GeyserFabricLogger geyserLogger;
    private IGeyserPingPassthrough geyserPingPassthrough;
    public Path path;
    public Path assets;
    
    public Path output;
        
    private static Hashtable<String, String> fileCache = new Hashtable<String, String>();
    public static final RuntimeResourcePack RESOURCE_PACK = RuntimeResourcePack.create("geysermc:pack");
    public static final Item TEST_ITEM = new Item(new FabricItemSettings().group(ItemGroup.MISC));
    
    @Override
    public void onInitialize() {
    	Registry.register(Registry.ITEM, new Identifier("geysermc:test_item"), TEST_ITEM);
    }
    
    public void registerPolys(PolyRegistry registry) {
    	for (int count = 1; count <= Registry.ITEM.getEntries().size(); count++) {
			Item currentItem = Registry.ITEM.get(count);
			if (!Registry.ITEM.getId(currentItem).getNamespace().equals("minecraft")) {
		    	assets = FabricLoader.getInstance().getGameDir().resolve("assets/" + FabricLoader.getInstance().getModContainer(Registry.ITEM.getId(currentItem).toString()) + "models/" + Registry.ITEM.getId(currentItem).getPath() + ".json");
				registry.registerItemPoly(currentItem, new CustomModelDataPoly(registry.getCMDManager(), currentItem, Items.STICK));			    
				BufferedImage texture = null;
				try {
					texture = ImageIO.read(new File(FabricLoader.getInstance().getGameDir() + "resources/" + Registry.ITEM.getId(currentItem).getNamespace() + "/textures/item/" + Registry.ITEM.getId(currentItem).getPath() + ".png"));
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				RESOURCE_PACK.addTexture(Registry.ITEM.getId(currentItem), texture);
				this.getGeyserLogger().info(texture.toString());
				new JModel();
				RESOURCE_PACK.addModel(JModel.model(Registry.ITEM.getId(currentItem).getNamespace() + ":" + currentItem.getName()).parent("item/generated").textures(new JTextures().layer0(Registry.ITEM.getId(currentItem).getNamespace() + ":item/" + Registry.ITEM.getId(currentItem).getPath())), Registry.ITEM.getId(currentItem));
			}
    	}
    	    	
    	for (int count = 1; count <= Registry.BLOCK.getEntries().size(); count++) {
			Block currentBlock = Registry.BLOCK.get(count);
			if (!Registry.BLOCK.getId(currentBlock).getNamespace().equals("minecraft")) {
				registry.registerBlockPoly(currentBlock, new SimpleReplacementPoly(currentBlock));
				RESOURCE_PACK.addModel(JModel.model(Registry.BLOCK.getId(currentBlock).getNamespace() + ":" + currentBlock.getName()).parent("block/cube_all").textures(new JTextures().particle(Registry.BLOCK.getId(currentBlock).getNamespace() + ":block/" + currentBlock.getName()).var("all", Registry.BLOCK.getId(currentBlock).getNamespace() + ":block/" + Registry.BLOCK.getId(currentBlock).getPath())), Registry.BLOCK.getId(currentBlock));
			}
    	}
    	
    	RRPCallback.EVENT.register(a -> a.add(RESOURCE_PACK));
    }
    
    @Override
    public void onInitializeServer() {
        this.onEnable();
    }

    @Override
    public void onEnable() {
        dataFolder = FabricLoader.getInstance().getConfigDir().resolve("Geyser-Fabric");
        output = dataFolder.resolve("packs");
        if (!dataFolder.toFile().exists()) {
            //noinspection ResultOfMethodCallIgnored
            dataFolder.toFile().mkdir();
        }
        try {
            File configFile = FileUtils.fileOrCopiedFromResource(dataFolder.resolve("config.yml").toFile(), "config.yml",
                    (x) -> x.replaceAll("generateduuid", UUID.randomUUID().toString()));
            this.geyserConfig = FileUtils.loadConfig(configFile, GeyserFabricConfiguration.class);
            File permissionsFile = fileOrCopiedFromResource(dataFolder.resolve("permissions.yml").toFile(), "permissions.yml");
            this.playerCommands = Arrays.asList(FileUtils.loadConfig(permissionsFile, GeyserFabricPermissions.class).getCommands());
        } catch (IOException ex) {
            LogManager.getLogger("geyser-fabric").error(LanguageUtils.getLocaleStringLog("geyser.config.failed"), ex);
            ex.printStackTrace();
        }

        this.geyserLogger = new GeyserFabricLogger(geyserConfig.isDebugMode());

        GeyserConfiguration.checkGeyserConfiguration(geyserConfig, geyserLogger);
        
        //FabricItemRegistry.registerItems();
        
        //this.getGeyserLogger().info(ItemRegistry.ITEMS.toString());

        if (server == null) {
            // Server has yet to start
            // Set as an event so we can get the proper IP and port if needed
            ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
                this.server = server;
                startGeyser();
            });

            // Register onDisable so players are properly kicked
            ServerLifecycleEvents.SERVER_STOPPING.register((server) -> onDisable());
        } else {
            // Server has started and this is a reload
            startGeyser();
        }
    }
    
    public void extractMods() {
    	
    	File file = new File(FabricLoader.getInstance().getGameDir().resolve("temp").toString());
    	
    	file.mkdir();
    	
    	String source = FabricLoader.getInstance().getGameDir().resolve("mods").toString();
    	File inputDir = new File(source);
    	
    	String destination = FabricLoader.getInstance().getGameDir().resolve("temp").toString();
    	File outputDir = new File(destination);
    	
    	try {
			org.apache.commons.io.FileUtils.copyDirectory(inputDir, outputDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
    
    }
    
    /**
     * Initialize core Geyser.
     * A function, as it needs to be called in different places depending on if Geyser is being reloaded or not.
     */
    public void startGeyser() {
    	//ItemRegistry.addItem(ItemRegistry.ITEMS.size(), new ItemEntry("geysermc:test_item", Registry.ITEM.getRawId(TEST_ITEM), ItemRegistry.ITEMS.size(), 0, false), new StartGamePacket.ItemEntry("geysermc:test_item", (short)ItemRegistry.ITEMS.size()));
    	
    	//ItemRegistry.ITEMS.add(new StartGamePacket.ItemEntry("geysermc:test_item", (short) ItemRegistry.ITEMS.size()));
    	//ItemRegistry.ITEM_ENTRIES.put(ItemRegistry.ITEMS.size(), new ItemEntry("geysermc:test_item", Registry.ITEM.getRawId(TEST_ITEM), ItemRegistry.ITEMS.size(), 0, false));
    	//ItemRegistry.addToCreativeMenu(ItemRegistry.ITEMS.size(), ItemRegistry.ITEMS.size(), 1, 64);
    	    	
    	if (this.geyserConfig.getRemote().getAddress().equalsIgnoreCase("auto")) {
            this.geyserConfig.setAutoconfiguredRemote(true);
            String ip = server.getServerIp();
            int port = server.getServerPort();
            if (ip != null && !ip.isEmpty() && !ip.equals("0.0.0.0")) {
                this.geyserConfig.getRemote().setAddress(ip);
            }
            this.geyserConfig.getRemote().setPort(port);
        }

        if (geyserConfig.getBedrock().isCloneRemotePort()) {
            geyserConfig.getBedrock().setPort(geyserConfig.getRemote().getPort());
        }

        this.connector = GeyserConnector.start(PlatformType.FABRIC, this);

        this.geyserPingPassthrough = GeyserLegacyPingPassthrough.init(connector);

        this.geyserCommandManager = new GeyserFabricCommandManager(connector);

        // Start command building
        // Set just "geyser" as the help command
        LiteralArgumentBuilder<ServerCommandSource> builder = net.minecraft.server.command.CommandManager.literal("geyser")
                .executes(new GeyserFabricCommandExecutor(connector, "help", !playerCommands.contains("help")));
        for (Map.Entry<String, GeyserCommand> command : connector.getCommandManager().getCommands().entrySet()) {
            // Register all subcommands as valid
            builder.then(net.minecraft.server.command.CommandManager.literal(
                    command.getKey()).executes(new GeyserFabricCommandExecutor(connector, command.getKey(),
                    !playerCommands.contains(command.getKey()))));
            
        }
        server.getCommandManager().getDispatcher().register(builder);
    }
    
//    static {
//
//		for (int count = 1; count <= Registry.ITEM.getEntries().size(); count++) {
//			if (Registry.ITEM.getId(Registry.ITEM.get(count)).getNamespace().equalsIgnoreCase("minecraft")) {
//				ItemRegistry.ITEMS.add(new StartGamePacket.ItemEntry(Registry.ITEM.getId(Registry.ITEM.get(count)).getNamespace() + ":" + Registry.ITEM.getId(Registry.ITEM.get(count)).getPath(), (short) ((short)ItemRegistry.ITEMS.size() + 1)));
//				ItemRegistry.addToCreativeMenu(count, count, Registry.ITEM.get(count).getMaxDamage(), 64);
//			}
//		}
//   	
//    }

    @Override
    public void onDisable() {
        if (connector != null) {
            connector.shutdown();
            connector = null;
        }
    }

    @Override
    public GeyserConfiguration getGeyserConfig() {
        return geyserConfig;
    }

    @Override
    public GeyserLogger getGeyserLogger() {
        return geyserLogger;
    }

    @Override
    public CommandManager getGeyserCommandManager() {
        return geyserCommandManager;
    }

    @Override
    public IGeyserPingPassthrough getGeyserPingPassthrough() {
        return geyserPingPassthrough;
    }

    @Override
    public Path getConfigFolder() {
        return dataFolder;
    }

    @Override
    public BootstrapDumpInfo getDumpInfo() {
        return new GeyserFabricDumpInfo(server);
    }

    private File fileOrCopiedFromResource(File file, String name) throws IOException {
        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            InputStream input = GeyserFabricMod.class.getResourceAsStream("/" + name); // resources need leading "/" prefix

            byte[] bytes = new byte[input.available()];

            //noinspection ResultOfMethodCallIgnored
            input.read(bytes);

            for(char c : new String(bytes).toCharArray()) {
                fos.write(c);
            }

            fos.flush();
            input.close();
            fos.close();
        }

        return file;
    }
}
