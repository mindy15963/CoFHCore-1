package cofh.core.item;

import cofh.core.util.ProxyUtils;
import cofh.lib.entity.AbstractGrenadeEntity;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.ProjectileDispenseBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.world.World;

import static cofh.lib.util.helpers.ItemHelper.cloneStack;

public class GrenadeItem extends ItemCoFH {

    protected final IGrenadeFactory<? extends AbstractGrenadeEntity> factory;

    protected int radius = 4;
    protected int cooldown = 20;

    public GrenadeItem(IGrenadeFactory<? extends AbstractGrenadeEntity> factory, Properties builder) {

        super(builder);
        this.factory = factory;

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("thrown"), (stack, world, living) -> (stack.getDamageValue() > 0 ? 1.0F : 0.0F));
        DispenserBlock.registerBehavior(this, DISPENSER_BEHAVIOR);
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {

        ItemStack stack = playerIn.getItemInHand(handIn);
        worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
        playerIn.getCooldowns().addCooldown(this, cooldown);
        if (!worldIn.isClientSide) {
            createGrenade(stack, worldIn, playerIn);
        }
        playerIn.awardStat(Stats.ITEM_USED.get(this));
        if (!playerIn.abilities.instabuild) {
            stack.shrink(1);
        }
        return ActionResult.success(stack);
    }

    protected void createGrenade(ItemStack stack, World world, PlayerEntity player) {

        AbstractGrenadeEntity grenade = factory.createGrenade(world, player);
        ItemStack throwStack = cloneStack(stack, 1);
        throwStack.setDamageValue(1);
        grenade.setItem(throwStack);
        grenade.setRadius(1 + radius);
        grenade.shootFromRotation(player, player.xRot, player.yRot, 0.0F, 1.5F, 0.5F);
        world.addFreshEntity(grenade);
    }

    // region FACTORY
    public interface IGrenadeFactory<T extends AbstractGrenadeEntity> {

        T createGrenade(World world, LivingEntity living);

        T createGrenade(World world, double posX, double posY, double posZ);

    }
    // endregion

    // region DISPENSER BEHAVIOR
    private static final ProjectileDispenseBehavior DISPENSER_BEHAVIOR = new ProjectileDispenseBehavior() {

        @Override
        protected ProjectileEntity getProjectile(World worldIn, IPosition position, ItemStack stackIn) {

            GrenadeItem grenadeItem = ((GrenadeItem) stackIn.getItem());
            AbstractGrenadeEntity grenade = grenadeItem.factory.createGrenade(worldIn, position.x(), position.y(), position.z());
            ItemStack throwStack = cloneStack(stackIn, 1);
            throwStack.setDamageValue(1);
            grenade.setItem(throwStack);
            grenade.setRadius(1 + grenadeItem.radius);
            return grenade;
        }

        @Override
        protected float getUncertainty() {

            return 3.0F;
        }
    };
    // endregion
}
