package ace.actually.valkyrienrelogs;

import ace.actually.valkyrienrelogs.blocks.RelogAnchorBlock;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.RaycastContext;
import org.joml.Vector3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.world.ServerShipWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.DimensionIdProvider;

public class ValkyrienRelogs implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("nomad");

    public static final RelogAnchorBlock RELOG_ANCHOR_BLOCK = new RelogAnchorBlock(AbstractBlock.Settings.of(Material.AGGREGATE));


    @Override
    public void onInitialize() {

        Registry.register(Registry.BLOCK,new Identifier("valkyrienrelogs","relog_anchor"),RELOG_ANCHOR_BLOCK);
        Registry.register(Registry.ITEM,new Identifier("valkyrienrelogs","relog_anchor"),new BlockItem(RELOG_ANCHOR_BLOCK,new Item.Settings()));

        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer)->
        {
            ServerShipWorld serverShipWorld = (ServerShipWorld) ValkyrienSkiesMod.getVsCore().getHooks().getCurrentShipServerWorld();

            PlayerShipAnchorAccessor anchorAccessor = (PlayerShipAnchorAccessor) serverPlayNetworkHandler.player;
            if(anchorAccessor.currentShipId()!=-1 && anchorAccessor.loggedOffOnShip())
            {
                LoadedServerShip ship = serverShipWorld.getLoadedShips().getById(anchorAccessor.currentShipId());

                if(ship!=null)
                {
                    Vector3d go = VSGameUtilsKt.toWorldCoordinates(ship,anchorAccessor.relativeShipPosition());
                    serverPlayNetworkHandler.player.setPosition(go.x,go.y+1,go.z);
                    anchorAccessor.setLoggedOffOnShip(false);
                }

            }

        });
        ServerPlayConnectionEvents.DISCONNECT.register((serverPlayNetworkHandler,minecraftServer)->
        {
            Vec3d pos = serverPlayNetworkHandler.player.getPos();
            ServerShipWorld serverShipWorld = (ServerShipWorld) ValkyrienSkiesMod.getVsCore().getHooks().getCurrentShipServerWorld();
            DimensionIdProvider provider = (DimensionIdProvider) serverPlayNetworkHandler.player.world;

            RaycastContext context = new RaycastContext(pos,pos.add(0,-2,0), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE,null);

            BlockHitResult result = serverPlayNetworkHandler.player.world.raycast(context);

            if(serverShipWorld.isBlockInShipyard(result.getBlockPos().getX(),result.getBlockPos().getY(),result.getBlockPos().getZ(),provider.getDimensionId()))
            {
                //check it's the right ship
                PlayerShipAnchorAccessor anchorAccessor = (PlayerShipAnchorAccessor) serverPlayNetworkHandler.player;
                anchorAccessor.setRelativeShipPosition(result.getBlockPos());
                anchorAccessor.setLoggedOffOnShip(true);
            }

        });

        LOGGER.info("May you stay on your ships!");
    }
}
