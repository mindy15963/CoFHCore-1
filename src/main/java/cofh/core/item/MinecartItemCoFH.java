package cofh.core.item;

import cofh.lib.entity.AbstractMinecartEntityCoFH;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MinecartItemCoFH extends ItemCoFH {

    protected final IMinecartFactory<? extends AbstractMinecartEntityCoFH> factory;

    public MinecartItemCoFH(IMinecartFactory<? extends AbstractMinecartEntityCoFH> factory, Properties builder) {

        super(builder);
        this.factory = factory;
        DispenserBlock.registerBehavior(this, DISPENSER_BEHAVIOR);
    }

    public ActionResultType useOn(ItemUseContext context) {

        World world = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        BlockState blockstate = world.getBlockState(blockpos);

        if (!blockstate.is(BlockTags.RAILS)) {
            return ActionResultType.FAIL;
        }
        ItemStack stack = context.getItemInHand();
        if (!world.isClientSide) {
            RailShape railshape = blockstate.getBlock() instanceof AbstractRailBlock ? ((AbstractRailBlock) blockstate.getBlock()).getRailDirection(blockstate, world, blockpos, null) : RailShape.NORTH_SOUTH;
            double d0 = 0.0D;
            if (railshape.isAscending()) {
                d0 = 0.5D;
            }
            createMinecart(stack, world, (double) blockpos.getX() + 0.5D, (double) blockpos.getY() + 0.0625D + d0, (double) blockpos.getZ() + 0.5D);
        }
        stack.shrink(1);
        return ActionResultType.SUCCESS;
    }

    protected void createMinecart(ItemStack stack, World world, double posX, double posY, double posZ) {

        AbstractMinecartEntityCoFH minecart = factory.createMinecart(world, posX, posY, posZ);
        if (stack.hasCustomHoverName()) {
            minecart.setCustomName(stack.getHoverName());
        }
        minecart.setEnchantments(stack.getEnchantmentTags());
        world.addFreshEntity(minecart);
    }

    // region FACTORY
    public interface IMinecartFactory<T extends AbstractMinecartEntity> {

        T createMinecart(World world, double posX, double posY, double posZ);

    }
    // endregion

    // region DISPENSER BEHAVIOR
    private static final IDispenseItemBehavior DISPENSER_BEHAVIOR = new DefaultDispenseItemBehavior() {

        private final DefaultDispenseItemBehavior behaviourDefaultDispenseItem = new DefaultDispenseItemBehavior();

        /**
         * Dispense the specified stack, play the dispense sound and spawn particles.
         */
        public ItemStack execute(IBlockSource source, ItemStack stack) {

            Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
            World world = source.getLevel();

            double d0 = source.x() + (double) direction.getStepX() * 1.125D;
            double d1 = Math.floor(source.y()) + (double) direction.getStepY();
            double d2 = source.z() + (double) direction.getStepZ() * 1.125D;

            BlockPos blockpos = source.getPos().relative(direction);
            BlockState blockstate = world.getBlockState(blockpos);
            RailShape railshape = blockstate.getBlock() instanceof AbstractRailBlock ? ((AbstractRailBlock) blockstate.getBlock()).getRailDirection(blockstate, world, blockpos, null) : RailShape.NORTH_SOUTH;
            double d3;
            if (blockstate.is(BlockTags.RAILS)) {
                if (railshape.isAscending()) {
                    d3 = 0.6D;
                } else {
                    d3 = 0.1D;
                }
            } else {
                if (!blockstate.isAir(world, blockpos) || !world.getBlockState(blockpos.below()).is(BlockTags.RAILS)) {
                    return this.behaviourDefaultDispenseItem.dispense(source, stack);
                }
                BlockState state = world.getBlockState(blockpos.below());
                RailShape shape = state.getBlock() instanceof AbstractRailBlock ? ((AbstractRailBlock) state.getBlock()).getRailDirection(state, world, blockpos.below(), null) : RailShape.NORTH_SOUTH;
                if (direction != Direction.DOWN && shape.isAscending()) {
                    d3 = -0.4D;
                } else {
                    d3 = -0.9D;
                }
            }
            if (stack.getItem() instanceof MinecartItemCoFH) {
                ((MinecartItemCoFH) stack.getItem()).createMinecart(stack, world, d0, d1 + d3, d2);
                stack.shrink(1);
            }
            return stack;
        }
    };
    // endregion
}
