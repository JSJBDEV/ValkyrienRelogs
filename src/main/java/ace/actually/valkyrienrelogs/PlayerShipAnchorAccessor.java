package ace.actually.valkyrienrelogs;

import net.minecraft.util.math.BlockPos;

public interface PlayerShipAnchorAccessor {

    BlockPos relativeShipPosition();
    void setRelativeShipPosition(BlockPos p);
    boolean loggedOffOnShip();
    void setLoggedOffOnShip(boolean d);

    long currentShipId();
    void setCurrentShipId(long l);
}
