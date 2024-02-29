package ace.actually.valkyrienrelogs.blocks;

import ace.actually.valkyrienrelogs.ValkyrienRelogs;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.math.BlockPos;

public class RelogAnchorBlockEntity extends BlockEntity {

    private NbtList officers = new NbtList();
    public RelogAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(ValkyrienRelogs.RELOG_ANCHOR_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.put("officers",officers);
        super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        officers= (NbtList) nbt.get("officers");
    }

    public void setOfficers(NbtList officers) {
        this.officers = officers;
        markDirty();
    }

    public void addOfficer(PlayerEntity player)
    {
        if(this.officers==null)
        {
            this.officers=new NbtList();
        }
        this.officers.add(NbtString.of(player.getUuidAsString()));
        markDirty();
    }

    public NbtList getOfficers() {
        return officers;
    }
}
