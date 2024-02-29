package ace.actually.valkyrienrelogs.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.world.ServerShipWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.DimensionIdProvider;

import java.util.List;
import java.util.Objects;

public class RelogAnchorBlock extends BlockWithEntity {
    public RelogAnchorBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(!world.isClient && hand==Hand.MAIN_HAND)
        {
            ServerShipWorld serverShipWorld = (ServerShipWorld) ValkyrienSkiesMod.getVsCore().getHooks().getCurrentShipServerWorld();
            DimensionIdProvider provider = (DimensionIdProvider) world;
            if(serverShipWorld.isBlockInShipyard(pos.getX(),pos.getY(),pos.getZ(),provider.getDimensionId()))
            {
                ChunkPos chunkPos = world.getChunk(pos).getPos();
                LoadedServerShip ship =serverShipWorld.getLoadedShips().getByChunkPos(chunkPos.x,chunkPos.z, provider.getDimensionId());
                RelogAnchorBlockEntity anchorBlockEntity = (RelogAnchorBlockEntity) world.getBlockEntity(pos);
                if(anchorBlockEntity.getOfficers().isEmpty())
                {
                    anchorBlockEntity.addOfficer(player);
                    removeOtherShip(player);
                    player.addScoreboardTag("VaReShip_"+ship.getId());
                    player.sendMessage(Text.of("you can now log off on this ship (and are an officer)!"),false);
                }
                else if(containsOfficer(anchorBlockEntity.getOfficers(),player))
                {
                    List<PlayerEntity> nearby = world.getEntitiesByClass(PlayerEntity.class,new Box(pos.add(-1,-1,-1),pos.add(1,1,1)),PlayerEntity::isPlayer);
                    for(PlayerEntity n: nearby)
                    {
                        if(!n.getScoreboardTags().contains("VaReShip_"+ship.getId()))
                        {
                            removeOtherShip(player);
                            n.addScoreboardTag("VaReShip_"+ship.getId());
                            n.sendMessage(Text.of("you can now log off on this ship!"),false);

                        }
                        if(player.isSneaking())
                        {
                            anchorBlockEntity.addOfficer(n);
                            n.sendMessage(Text.of("you are now an officer on this ship!"),false);
                        }
                    }

                }
            }
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    private boolean containsOfficer(NbtList officers,PlayerEntity player)
    {
        for (int i = 0; i < officers.size(); i++) {
            if(officers.getString(i).equals(player.getUuidAsString()))
            {
                return true;
            }
        }
        return false;
    }

    private void removeOtherShip(PlayerEntity player)
    {
        String marked = "--";
        for(String tag: player.getScoreboardTags())
        {
            if(tag.startsWith("VaReShip"))
            {
                marked=tag;
            }
        }
        if(!marked.equals("--"))
        {
            player.sendMessage(Text.of("You have unregistered from you old ship!"),false);
            player.removeScoreboardTag(marked);
        }
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RelogAnchorBlockEntity(pos, state);
    }
}
