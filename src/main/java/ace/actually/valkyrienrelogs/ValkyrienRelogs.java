package ace.actually.valkyrienrelogs;

import ace.actually.valkyrienrelogs.blocks.RelogAnchorBlock;
import ace.actually.valkyrienrelogs.blocks.RelogAnchorBlockEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.RaycastContext;
import org.joml.Vector3dc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.world.ServerShipWorld;
import org.valkyrienskies.eureka.EurekaEntitiesKt;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.DimensionIdProvider;

public class ValkyrienRelogs implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("nomad");

    public static final RelogAnchorBlock RELOG_ANCHOR_BLOCK = new RelogAnchorBlock(AbstractBlock.Settings.of(Material.AGGREGATE));
    public static final BlockEntityType<RelogAnchorBlockEntity> RELOG_ANCHOR_BLOCK_ENTITY = Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            new Identifier("pirates", "motion_invoking_block_entity"),
            FabricBlockEntityTypeBuilder.create(RelogAnchorBlockEntity::new, RELOG_ANCHOR_BLOCK).build()
    );


    @Override
    public void onInitialize() {

        Registry.register(Registry.BLOCK,new Identifier("valkyrienrelogs","relog_anchor"),RELOG_ANCHOR_BLOCK);
        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer)->
        {
            if(serverPlayNetworkHandler.player.getScoreboardTags().contains("VaRe_lo_ship"))
            {
                ServerShipWorld serverShipWorld = (ServerShipWorld) ValkyrienSkiesMod.getVsCore().getHooks().getCurrentShipServerWorld();

                long id = -1;
                for(String tag: serverPlayNetworkHandler.player.getScoreboardTags())
                {
                    if(tag.startsWith("VaReShip"))
                    {
                        id = Long.parseLong(tag.split("_")[1]);
                    }
                }
                if(id!=-1)
                {
                    LoadedServerShip ship = serverShipWorld.getLoadedShips().getById(id);
                    Vector3dc a = ship.getTransform().getPositionInWorld();

                    serverPlayNetworkHandler.player.setPosition(a.x(),a.y()+5,a.z());
                    serverPlayNetworkHandler.player.removeScoreboardTag("VaRe_lo_ship");
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
                serverPlayNetworkHandler.player.addScoreboardTag("VaRe_lo_ship");
            }

        });

        LOGGER.info("May you stay on your ships!");
    }
}
