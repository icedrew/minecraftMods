package com.ololee.quake.item.hammer.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.ololee.quake.item.hammer.entity.HammerEntity;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.IVanishable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import static com.ololee.quake.Utils.DESTROY_ITEM_SPEED;

public class Hammer extends Item implements IVanishable {
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public Hammer() {
        this(new Properties().tab(ItemGroup.TAB_TOOLS));
    }

    public Hammer(Item.Properties properties) {
        super(properties);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", 99999.0D, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", (double) -0.9F, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    public boolean canAttackBlock(BlockState blockState, World world, BlockPos blockPos, PlayerEntity playerEntity) {
        return true;
    }

    public UseAction getUseAnimation(ItemStack itemStack) {
        return UseAction.SPEAR;
    }

    public int getUseDuration(ItemStack itemStack) {
        return 72000;
    }

    public void releaseUsing(ItemStack itemStack, World world, LivingEntity livingEntity, int consume) {
        if (livingEntity instanceof PlayerEntity) {
            PlayerEntity playerentity = (PlayerEntity) livingEntity;
            int i = this.getUseDuration(itemStack) - consume;
            if (i >= 10) {
                /**
                 * player force
                 * loose scale
                 */
                int riptide = EnchantmentHelper.getRiptide(itemStack);
                if (riptide <= 0 || playerentity.isInWaterOrRain()) {
                    if (!world.isClientSide) {
                        itemStack.hurtAndBreak(1, playerentity, (enemy) -> {
                            enemy.broadcastBreakEvent(livingEntity.getUsedItemHand());
                        });
                        if (riptide == 0) {
                            HammerEntity hammerEntity = new HammerEntity(world, playerentity, itemStack);
                            hammerEntity.shootFromRotation(playerentity, playerentity.xRot, playerentity.yRot, 0.0F, 2.5F, 1.0F);
                            if (playerentity.abilities.instabuild) {
                                hammerEntity.pickup = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
                            }

                            world.addFreshEntity(hammerEntity);
                            world.playSound((PlayerEntity) null, hammerEntity, SoundEvents.TRIDENT_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F);
                            if (!playerentity.abilities.instabuild) {
                                playerentity.inventory.removeItem(itemStack);
                            }
                        }
                    }

                    playerentity.awardStat(Stats.ITEM_USED.get(this));
                    if (riptide > 0) {
                        float yRot = playerentity.yRot;
                        float xRot = playerentity.xRot;
                        float positionX = -MathHelper.sin(yRot * ((float) Math.PI / 180F)) * MathHelper.cos(xRot * ((float) Math.PI / 180F));
                        float positionY = -MathHelper.sin(xRot * ((float) Math.PI / 180F));
                        float positionZ = MathHelper.cos(yRot * ((float) Math.PI / 180F)) * MathHelper.cos(xRot * ((float) Math.PI / 180F));
                        float distance = MathHelper.sqrt(positionX * positionX + positionY * positionY + positionZ * positionZ);
                        float force = 3.0F * ((1.0F + (float) riptide) / 4.0F);
                        positionX = positionX * (force / distance);
                        positionY = positionY * (force / distance);
                        positionZ = positionZ * (force / distance);
                        playerentity.push((double) positionX, (double) positionY, (double) positionZ);
                        playerentity.startAutoSpinAttack(20);
                        if (playerentity.isOnGround()) {
                            float f6 = 1.1999999F;
                            playerentity.move(MoverType.SELF, new Vector3d(0.0D, f6, 0.0D));
                        }

                        SoundEvent soundevent;
                        if (riptide >= 3) {
                            soundevent = SoundEvents.TRIDENT_RIPTIDE_3;
                        } else if (riptide == 2) {
                            soundevent = SoundEvents.TRIDENT_RIPTIDE_2;
                        } else {
                            soundevent = SoundEvents.TRIDENT_RIPTIDE_1;
                        }

                        world.playSound((PlayerEntity) null, playerentity, soundevent, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    }

                }
            }
        }
    }


    public ActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
        ItemStack itemInHand = playerEntity.getItemInHand(hand);
        if (itemInHand.getDamageValue() >= itemInHand.getMaxDamage() - 1) {
            return ActionResult.fail(itemInHand);
        } else if (EnchantmentHelper.getRiptide(itemInHand) > 0 && !playerEntity.isInWaterOrRain()) {
            return ActionResult.fail(itemInHand);
        } else {
            playerEntity.startUsingItem(hand);
            return ActionResult.consume(itemInHand);
        }
    }

    public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity1, LivingEntity livingEntity2) {
        itemStack.hurtAndBreak(1, livingEntity2, (enemy) -> {
            enemy.broadcastBreakEvent(EquipmentSlotType.MAINHAND);
        });
        return true;
    }

    public boolean mineBlock(ItemStack itemStack, World world, BlockState blockState, BlockPos blockPos,
                             LivingEntity livingEntity) {
        if ((double) blockState.getDestroySpeed(world, blockPos) != 0.0D) {
            itemStack.hurtAndBreak(2, livingEntity, (enemy) -> {
                enemy.broadcastBreakEvent(EquipmentSlotType.MAINHAND);
            });
        }

        return true;
    }


    @Override
    public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
        return DESTROY_ITEM_SPEED;
    }

    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlotType equipmentSlotType) {
        return equipmentSlotType == EquipmentSlotType.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(equipmentSlotType);
    }

    public int getEnchantmentValue() {
        return 1;
    }


    @Override
    public boolean isCorrectToolForDrops(BlockState blockState) {
        return true;
    }


}

