package com.ololee.quake.constants;

import com.ololee.quake.item.hammer.entity.HammerEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;

import static com.ololee.quake.Utils.register;

public interface Constants {
   String MOD_ID = "union";
    EntityType<HammerEntity> HAMMER = register("hammer", EntityType.Builder.<HammerEntity>
            of(HammerEntity::new, EntityClassification.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20));


}
