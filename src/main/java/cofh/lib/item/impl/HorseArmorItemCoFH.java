package cofh.lib.item.impl;

import cofh.lib.item.ICoFHItem;
import net.minecraft.item.HorseArmorItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static cofh.lib.util.constants.Constants.TRUE;

public class HorseArmorItemCoFH extends HorseArmorItem implements ICoFHItem {

    protected BooleanSupplier showInGroups = TRUE;

    protected Supplier<ItemGroup> displayGroup;

    protected int enchantability;

    public HorseArmorItemCoFH(int protection, String texture, Properties builder) {

        super(protection, texture, builder);
    }

    public HorseArmorItemCoFH(int protection, ResourceLocation texture, Properties builder) {

        super(protection, texture, builder);
    }

    public HorseArmorItemCoFH setDisplayGroup(Supplier<ItemGroup> displayGroup) {

        this.displayGroup = displayGroup;
        return this;
    }

    public HorseArmorItemCoFH setShowInGroups(BooleanSupplier showInGroups) {

        this.showInGroups = showInGroups;
        return this;
    }

    public HorseArmorItemCoFH setEnchantability(int enchantability) {

        this.enchantability = enchantability;
        return this;
    }

    @Override
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {

        if (!showInGroups.getAsBoolean() || displayGroup != null && displayGroup.get() != null && displayGroup.get() != group) {
            return;
        }
        super.fillItemCategory(group, items);
    }

    @Override
    public Collection<ItemGroup> getCreativeTabs() {

        return displayGroup != null && displayGroup.get() != null ? Collections.singletonList(displayGroup.get()) : super.getCreativeTabs();
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {

        return enchantability > 0;
    }

    @Override
    public int getEnchantmentValue() {

        return enchantability;
    }

}
