package ace.actually.valkyrienrelogs.blocks;

import ace.actually.valkyrienrelogs.PlayerShipAnchorAccessor;
import ace.actually.valkyrienrelogs.ShipOfficersAttachment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
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
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.world.ServerShipWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.DimensionIdProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RelogAnchorBlock extends Block {
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

                PlayerShipAnchorAccessor anchorAccessor = (PlayerShipAnchorAccessor) player;

                if(!hasOfficers(ship))
                {
                    anchorAccessor.setCurrentShipId(ship.getId());
                    player.sendMessage(Text.of("you can now log off on this ship (and are an officer)!"),false);
                    addOfficerToShip(ship,player);
                }
                else if(containsOfficer(ship,player))
                {
                    List<PlayerEntity> nearby = world.getEntitiesByClass(PlayerEntity.class,new Box(pos.add(-2,-2,-2),pos.add(2,2,2)),PlayerEntity::isPlayer);
                    for(PlayerEntity n: nearby)
                    {
                        PlayerShipAnchorAccessor nAccess = (PlayerShipAnchorAccessor) n;
                        nAccess.setCurrentShipId(ship.getId());
                        n.sendMessage(Text.of("you can now log off on this ship!"),false);
                        if(player.isSneaking())
                        {
                            addOfficerToShip(ship,n);
                            n.sendMessage(Text.of("you are now an officer on this ship!"),false);
                        }
                    }

                }
            }
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    private void addOfficerToShip(LoadedServerShip ship, PlayerEntity officer)
    {
        ShipOfficersAttachment attachment;
        if(ship.getAttachment(ShipOfficersAttachment.class)!=null)
        {
            attachment = ship.getAttachment(ShipOfficersAttachment.class);
        }
        else
        {
            attachment = new ShipOfficersAttachment();
        }
        String[] old = attachment.getOfficers();

        String[] n = Arrays.copyOf(old,old.length+1);

        n[old.length]=officer.getUuidAsString();

        ShipOfficersAttachment att = new ShipOfficersAttachment(n);
        ship.setAttachment(ShipOfficersAttachment.class,att);


    }

    private boolean containsOfficer(LoadedServerShip ship,PlayerEntity player)
    {
        ShipOfficersAttachment attachment;
        if(ship.getAttachment(ShipOfficersAttachment.class)!=null)
        {
            attachment = ship.getAttachment(ShipOfficersAttachment.class);
        }
        else
        {
            attachment = new ShipOfficersAttachment();
        }
        String[] officers = attachment.getOfficers();

        for(String officer: officers)
        {
            if(player.getUuidAsString().equals(officer))
            {
                return true;
            }
        }
        return false;
    }

    private boolean hasOfficers(LoadedServerShip ship)
    {
        ShipOfficersAttachment attachment;
        if(ship.getAttachment(ShipOfficersAttachment.class)!=null)
        {
            attachment = ship.getAttachment(ShipOfficersAttachment.class);
        }
        else
        {
            attachment = new ShipOfficersAttachment();
        }
        String[] officers = attachment.getOfficers();

        return officers.length>0;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return Block.createCuboidShape(1,0,1,15,2,15);
    }
}
