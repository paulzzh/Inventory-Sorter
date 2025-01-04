package net.kyrptonaught.inventorysorter;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortCases {
    static String getStringForSort(ItemStack stack, SortType sortType) {
        Item item = stack.getItem();
        String itemName = specialCases(stack);
        switch (sortType) {
            case CATEGORY -> {
                ItemGroup group = getFirstItemGroup(stack);
                return (group != null ? group.getDisplayName().getString() : "zzz") + itemName;
            }
            case MOD -> {
                return Registries.ITEM.getId(item).getNamespace() + itemName;
            }
            case NAME -> {
                return itemName;
            }
            default -> {
                return item.getName().toString();
            }
        }
    }

    private static ItemGroup getFirstItemGroup(ItemStack stack) {
        List<ItemGroup> groups = ItemGroups.getGroups();
        for (ItemGroup group : groups) {
            if (group.contains(new ItemStack(stack.getItem())))
                return group;

        }
        return null;
    }

    private static String specialCases(ItemStack stack) {
        Item item = stack.getItem();

        if (stack.hasChangedComponent(DataComponentTypes.PROFILE))
            return playerHeadCase(stack);
        if (stack.getCount() != stack.getMaxCount())
            return stackSize(stack);
        if (stack.getComponentChanges().get(DataComponentTypes.ENCHANTMENTS) != null)
            return enchantedItemCase(stack);
        if (item instanceof MiningToolItem || item instanceof SwordItem || item instanceof HoeItem || item instanceof ShearsItem || item instanceof BrushItem)
            return toolDuribilityCase(stack);
        return item.getName().toString();
    }

    private static String playerHeadCase(ItemStack stack) {
        ProfileComponent profileComponent = stack.getComponentChanges().get(DataComponentTypes.PROFILE).get();
        String ownerName = profileComponent.name().isPresent() ? profileComponent.name().get() : stack.getItem().toString();

        // this is duplicated logic, so we should probably refactor
        String count = "";
        if (stack.getCount() != stack.getMaxCount()) {
            count = Integer.toString(stack.getCount());
        }

        return stack.getItem().toString() + " " + ownerName.toLowerCase() + count;
    }

    private static String stackSize(ItemStack stack) {
        return stack.getItem().getName().toString() + stack.getCount();
    }

    private static String enchantedItemCase(ItemStack stack) {
        ItemEnchantmentsComponent enchantmentsComponent = stack.getComponentChanges().get(DataComponentTypes.ENCHANTMENTS).get();
        List<String> names = new ArrayList<>();
        StringBuilder enchantNames = new StringBuilder();
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> enchant : enchantmentsComponent.getEnchantmentEntries()) {
            names.add(Enchantment.getName(enchant.getKey(), enchant.getIntValue()).getString());
        }
        Collections.sort(names);
        for (String enchant : names) {
            enchantNames.append(enchant).append(" ");
        }
        return stack.getItem().getName().toString() + " " + enchantmentsComponent.getSize() + " " + enchantNames;
    }

    private static String toolDuribilityCase(ItemStack stack) {
        return stack.getItem().getName().toString() + stack.getDamage();
    }

    public enum SortType {
        NAME, CATEGORY, MOD, ID;

        public String getTranslationKey() {
            return "key." + InventorySorterMod.MOD_ID + ".sorttype." + this.toString().toLowerCase();
        }
    }
}
