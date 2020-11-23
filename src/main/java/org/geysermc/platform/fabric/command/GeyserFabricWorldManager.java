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

package org.geysermc.platform.fabric.command;

import com.github.steveice10.mc.protocol.data.game.chunk.Chunk;
import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode;
import com.github.steveice10.mc.protocol.data.game.setting.Difficulty;
import net.minecraft.block.Block;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkSection;
import org.geysermc.connector.bootstrap.GeyserBootstrap;
import org.geysermc.connector.network.session.GeyserSession;
import org.geysermc.connector.network.translators.world.WorldManager;
import org.geysermc.connector.network.translators.world.block.BlockTranslator;
import org.geysermc.connector.utils.GameRule;

public class GeyserFabricWorldManager extends WorldManager {
    private final MinecraftServer server;

    public GeyserFabricWorldManager(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public int getBlockAt(GeyserSession session, int x, int y, int z) {
        ServerPlayerEntity entity = server.getPlayerManager().getPlayer(session.getPlayerEntity().getUuid());
        if (entity == null) {
            return BlockTranslator.JAVA_AIR_ID;
        }
        BlockPos pos = new BlockPos(x, y, z);
        return Block.getRawIdFromState(entity.getServerWorld().getBlockState(pos));
    }

    @Override
    public void getBlocksInSection(GeyserSession session, int x, int y, int z, Chunk chunk) {
        ServerPlayerEntity entity = server.getPlayerManager().getPlayer(session.getPlayerEntity().getUuid());
        if (entity == null) {
            return;
        }
        ChunkSection section = entity.getServerWorld().getChunk(x, z).getSectionArray()[y];
        for (int blockY = 0; blockY < 16; blockY++) { // Cache-friendly iteration order
            for (int blockZ = 0; blockZ < 16; blockZ++) {
                for (int blockX = 0; blockX < 16; blockX++) {
                    chunk.set(blockX, blockY, blockZ, Block.getRawIdFromState(section.getBlockState(blockX, blockY, blockZ)));
                }
            }
        }
    }

    @Override
    public boolean hasMoreBlockDataThanChunkCache() {
        return true;
    }

    @Override
    public int[] getBiomeDataAt(GeyserSession session, int x, int z) {
        ServerPlayerEntity entity = server.getPlayerManager().getPlayer(session.getPlayerEntity().getUuid());
        if (entity == null) {
            return new int[1024];
        }
        return entity.getServerWorld().getChunk(x, z).getBiomeArray().toIntArray();
    }

    @Override
    public void setGameRule(GeyserSession session, String name, Object value) {
        GeyserBootstrap.DEFAULT_CHUNK_MANAGER.setGameRule(session, name, value);
    }

    @Override
    public Boolean getGameRuleBool(GeyserSession session, GameRule gamerule) {
        return GeyserBootstrap.DEFAULT_CHUNK_MANAGER.getGameRuleBool(session, gamerule);
    }

    @Override
    public int getGameRuleInt(GeyserSession session, GameRule gameRule) {
        return GeyserBootstrap.DEFAULT_CHUNK_MANAGER.getGameRuleInt(session, gameRule);
    }

    @Override
    public void setPlayerGameMode(GeyserSession session, GameMode gamemode) {
        GeyserBootstrap.DEFAULT_CHUNK_MANAGER.setPlayerGameMode(session, gamemode);
    }

    @Override
    public void setDifficulty(GeyserSession session, Difficulty difficulty) {
        GeyserBootstrap.DEFAULT_CHUNK_MANAGER.setDifficulty(session, difficulty);
    }

    @Override
    public boolean hasPermission(GeyserSession session, String permission) {
        return GeyserBootstrap.DEFAULT_CHUNK_MANAGER.hasPermission(session, permission);
    }
}
