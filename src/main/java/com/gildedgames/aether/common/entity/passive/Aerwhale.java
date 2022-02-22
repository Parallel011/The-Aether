package com.gildedgames.aether.common.entity.passive;

import com.gildedgames.aether.client.registry.AetherSoundEvents;
import com.gildedgames.aether.core.capability.interfaces.IAetherPlayer;
import com.mojang.math.Vector3d;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class Aerwhale extends FlyingMob {
    public float motionYaw, motionPitch;

	public Aerwhale(EntityType<? extends Aerwhale> type, Level level) {
		super(type, level);
        this.moveControl = new AerwhaleMoveControl(this);
	}

    @Override
    public void registerGoals() {
        this.goalSelector.addGoal(5, new RandomFloatAroundGoal(this));
    }

    @Nonnull
    public static AttributeSupplier.Builder createMobAttributes() {
        return FlyingMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.0D);
    }

    public static boolean checkAerwhaleSpawnRules(EntityType<? extends Aerwhale> aerwhale, LevelAccessor level, MobSpawnType spawnReason, BlockPos pos, Random random) {
        return level.getFluidState(pos).is(Fluids.EMPTY) && level.getRawBrightness(pos, 0) > 8 && Mob.checkMobSpawnRules(aerwhale, level, spawnReason, pos, random);
    }

    /**
     * The purpose of this method override is to fix the weird movement from flying mobs.
     */
    @Override
    public void travel(Vec3 positionIn) {
        if (this.isEffectiveAi() || this.isControlledByLocalInstance()) {
            List<Entity> passengers = this.getPassengers();
            if (!passengers.isEmpty()) {
                Entity entity = passengers.get(0);
                if (entity instanceof Player) {
                    Player player = (Player)entity;
                    this.setYRot(player.getYRot());
                    this.motionYaw = this.yRotO = this.getYRot();
                    this.setXRot(player.getXRot());
                    this.motionPitch = this.xRotO = this.getXRot();

                    this.motionYaw = this.yHeadRot = player.yHeadRot;

                    positionIn = new Vec3(player.xxa, 0.0, (player.zza <= 0.0F)? player.zza * 0.25F : player.zza);

                    if (IAetherPlayer.get(player).map(IAetherPlayer::isJumping).orElse(false)) {
                        this.setDeltaMovement(new Vec3(0.0, 0.0, 0.0));
                    } else {
                        double d0 = Math.toRadians(player.getYRot() - 90.0);
                        double d1 = Math.toRadians(-player.getXRot());
                        double d2 = Math.cos(d1);
                        this.setDeltaMovement(
                                0.98 * (this.getDeltaMovement().x + 0.05 * Math.cos(d0) * d2),
                                0.98 * (this.getDeltaMovement().y + 0.02 * Math.sin(d1)),
                                0.98 * (this.getDeltaMovement().z + 0.05 * Math.sin(d0) * d2)
                        );
                    }

                    this.maxUpStep = 1.0F;

                    if (!this.level.isClientSide) {
                        this.flyingSpeed = this.getSpeed() * 0.6F;
                        super.travel(positionIn);
                    }

                    this.animationSpeedOld = this.animationSpeed;
                    double d0 = this.getX() - this.xo;
                    double d1 = this.getZ() - this.zo;
                    float f4 = 4.0F * Mth.sqrt((float) (d0*d0 + d1*d1));

                    if (f4 > 1.0F) {
                        f4 = 1.0F;
                    }

                    this.animationSpeed += 0.4F * (f4 - this.animationSpeed);
                    this.animationPosition += this.animationSpeed;
                }
            } else {
                this.maxUpStep = 0.5F;
                this.flyingSpeed = 0.02F;
                super.travel(positionIn);
            }
        } else {
            this.calculateEntityAnimation(this, false);
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.clearFire();
    }

    @Override
    protected InteractionResult mobInteract(Player player, @Nonnull InteractionHand hand) {
        if (player.getUUID().getMostSignificantBits() == 220717875589366683L && player.getUUID().getLeastSignificantBits() == -7181826737698904209L) {
            player.startRiding(this);
            if (!this.level.isClientSide) {
                BaseComponent msg = new TextComponent("Serenity is the queen of W(h)ales!!");
                player.level.players().forEach(p -> p.sendMessage(msg, player.getUUID()));
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 1;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AetherSoundEvents.ENTITY_AERWHALE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AetherSoundEvents.ENTITY_AERWHALE_DEATH.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AetherSoundEvents.ENTITY_AERWHALE_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 3.0F;
    }

    static class RandomFloatAroundGoal extends Goal {
        private Aerwhale aerwhale;
        public RandomFloatAroundGoal(Aerwhale aerwhale) {
            this.aerwhale = aerwhale;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        @Override
        public boolean canUse() {
            MoveControl movecontrol = this.aerwhale.getMoveControl();
            if (!movecontrol.hasWanted()) {
                return true;
            } else {
                double d0 = movecontrol.getWantedX() - this.aerwhale.getX();
                double d1 = movecontrol.getWantedY() - this.aerwhale.getY();
                double d2 = movecontrol.getWantedZ() - this.aerwhale.getZ();
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                return d3 < 1.0D || d3 > 3600.0D;
            }
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        @Override
        public boolean canContinueToUse() {
            return false;
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        @Override
        public void start() {
            Random random = this.aerwhale.getRandom();
            double d0 = this.aerwhale.getX() + (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
            double d1 = this.aerwhale.getY() + (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
            double d2 = this.aerwhale.getZ() + (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
            this.aerwhale.getMoveControl().setWantedPosition(d0, d1, d2, 1.0);
        }
    }

    /**
     * Move controller class for the Aerwhale.
     */
    static class AerwhaleMoveControl extends MoveControl {
        private final Aerwhale aerwhale;
        private int courseChangeCooldown;

        public AerwhaleMoveControl(Aerwhale aerwhale) {
            super(aerwhale);
            this.aerwhale = aerwhale;
        }

        @Override
        public void tick() {
            if (this.operation == MoveControl.Operation.MOVE_TO) {
                if (this.courseChangeCooldown-- <= 0) {
                    this.courseChangeCooldown += this.aerwhale.getRandom().nextInt(5) + 2;
                    Vec3 vec3d = new Vec3(this.wantedX - this.aerwhale.getX(), this.wantedY - this.aerwhale.getY(), this.wantedZ - this.aerwhale.getZ());
                    double d0 = vec3d.length();
                    vec3d = vec3d.normalize();
                    if (this.canReach(vec3d, Mth.ceil(d0))) {
                        this.aerwhale.setDeltaMovement(this.aerwhale.getDeltaMovement().add(vec3d.scale(0.1)));
                    }
                    else {
                        this.operation = MoveControl.Operation.WAIT;
                    }
                }

            }
        }

        /**
         * Checks if entity bounding box is not colliding with terrain
         */
        private boolean canReach(Vec3 pos, int distance) {
            AABB axisalignedbb = this.aerwhale.getBoundingBox();

            for (int i = 1; i < distance; ++i) {
                axisalignedbb = axisalignedbb.move(pos);
                if (!this.aerwhale.level.noCollision(this.aerwhale, axisalignedbb)) {
                    return false;
                }
            }
            return true;
        }

    }
}