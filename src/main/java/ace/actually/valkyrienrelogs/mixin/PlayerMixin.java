package ace.actually.valkyrienrelogs.mixin;

import ace.actually.valkyrienrelogs.PlayerShipAnchorAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerMixin extends LivingEntity implements PlayerShipAnchorAccessor {

	private static final TrackedData<NbtCompound> ANCHOR_DATA = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);

	protected PlayerMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Override
	public BlockPos relativeShipPosition() {
		return NbtHelper.toBlockPos(dataTracker.get(ANCHOR_DATA).getCompound("relativeShipPosition"));
	}

	@Override
	public boolean loggedOffOnShip() {
		return dataTracker.get(ANCHOR_DATA).getBoolean("loggedOffOnShip");
	}

	@Override
	public void setRelativeShipPosition(BlockPos p) {
		NbtCompound tag = dataTracker.get(ANCHOR_DATA);
		tag.put("relativeShipPosition",NbtHelper.fromBlockPos(p));
		dataTracker.set(ANCHOR_DATA,tag);
	}

	@Override
	public void setLoggedOffOnShip(boolean d) {
		NbtCompound tag = dataTracker.get(ANCHOR_DATA);
		tag.putBoolean("loggedOffOnShip",d);
		dataTracker.set(ANCHOR_DATA,tag);
	}

	@Override
	public long currentShipId() {
		return dataTracker.get(ANCHOR_DATA).getLong("shipId");
	}

	@Override
	public void setCurrentShipId(long l) {
		NbtCompound tag = dataTracker.get(ANCHOR_DATA);
		tag.putLong("shipId",l);
		dataTracker.set(ANCHOR_DATA,tag);
	}

	@Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
	public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
		dataTracker.set(ANCHOR_DATA,nbt.getCompound("anchor_data"));

	}

	@Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
	public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
		nbt.put("anchor_data",dataTracker.get(ANCHOR_DATA));

	}

	@Inject(method = "initDataTracker", at = @At("TAIL"))
	protected void initDataTracker(CallbackInfo ci) {
		NbtCompound a = new NbtCompound();
		a.putLong("shipId",-1);
		dataTracker.startTracking(ANCHOR_DATA,a.copy());

	}
}