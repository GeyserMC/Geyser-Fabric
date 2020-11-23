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

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.data.SoundEvent;
import com.nukkitx.protocol.bedrock.packet.LevelSoundEventPacket;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.geysermc.connector.GeyserConnector;
import org.geysermc.connector.network.session.GeyserSession;
import org.geysermc.connector.network.translators.world.block.BlockTranslator;

public class GeyserFabricEvents {

    public static ActionResult onPlayerBreak(PlayerEntity player, World world, Hand hand, BlockHitResult result) {
        // Send the place sound to the client
        GeyserSession session = null;
        // getInstance() needed for reloads to work
        for (GeyserSession sessions : GeyserConnector.getInstance().getPlayers()) {
            if (sessions.getPlayerEntity().getUuid().equals(player.getUuid())) {
                session = sessions;
                break;
            }
        }
        if (session == null) {
            return ActionResult.PASS;
        }

        LevelSoundEventPacket placeBlockSoundPacket = new LevelSoundEventPacket();
        placeBlockSoundPacket.setSound(SoundEvent.PLACE);
        BlockPos pos = result.getBlockPos().offset(result.getSide());
        placeBlockSoundPacket.setPosition(Vector3f.from(pos.getX(), pos.getY(), pos.getZ()));
        placeBlockSoundPacket.setBabySound(false);
        placeBlockSoundPacket.setExtraData(BlockTranslator.getBedrockBlockId(Block.getRawIdFromState(world.getBlockState(pos))));
        placeBlockSoundPacket.setIdentifier(":");
        session.sendUpstreamPacket(placeBlockSoundPacket);
        session.setLastBlockPlacePosition(null);
        session.setLastBlockPlacedId(null);
        return ActionResult.PASS;
    }

}
