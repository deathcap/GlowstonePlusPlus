package net.glowstone.block.blocktype;

import net.glowstone.EventFactory;
import net.glowstone.block.GlowBlock;
import net.glowstone.block.GlowBlockState;
import net.glowstone.entity.GlowPlayer;
import org.bukkit.GrassSpecies;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.LongGrass;
import org.bukkit.material.MaterialData;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

public class BlockTallGrass extends BlockNeedsAttached implements IBlockGrowable {

    public boolean canPlaceAt(GlowBlock block, BlockFace against) {
        Material typeBelow = block.getWorld()
            .getBlockTypeAt(block.getX(), block.getY() - 1, block.getZ());
        return typeBelow == Material.GRASS_BLOCK || typeBelow == Material.DIRT
            || typeBelow == Material.FARMLAND;
    }

    @NotNull
    @Override
    public Collection<ItemStack> getDrops(@NotNull GlowBlock block, ItemStack tool) {
        if (ThreadLocalRandom.current().nextFloat() < .125) {
            return Collections.singletonList(new ItemStack(Material.WHEAT_SEEDS, 1));
        }
        return Collections.emptyList();
    }

    @Override
    public boolean canAbsorb(GlowBlock block, BlockFace face, ItemStack holding) {
        return true;
    }

    @Override
    public boolean canOverride(GlowBlock block, BlockFace face, ItemStack holding) {
        return true;
    }

    @Override
    public boolean isFertilizable(GlowBlock block) {
        MaterialData data = block.getState().getData();
        if (data instanceof LongGrass) {
            if (((LongGrass) data).getSpecies() != GrassSpecies.DEAD) {
                return true;
            }
        } else {
            warnMaterialData(LongGrass.class, data);
        }
        return false;
    }

    @Override
    public boolean canGrowWithChance(GlowBlock block) {
        return true;
    }

    @Override
    public void grow(GlowPlayer player, GlowBlock block) {
        MaterialData data = block.getState().getData();
        if (data instanceof LongGrass) {
            GrassSpecies species = ((LongGrass) data).getSpecies();
            if (species == GrassSpecies.NORMAL || species == GrassSpecies.FERN_LIKE) {
                GlowBlockState blockState = block.getState();
                GlowBlockState headBlockState = block.getRelative(BlockFace.UP).getState();
                if (headBlockState.getType() == Material.AIR) {
                    headBlockState.setType(species == GrassSpecies.NORMAL ? Material.TALL_GRASS
                        : Material.LARGE_FERN);
                    BlockGrowEvent growEvent = new BlockGrowEvent(block, blockState);
                    EventFactory.getInstance().callEvent(growEvent);
                    if (!growEvent.isCancelled()) {
                        blockState.update(true);
                        headBlockState.update(true);
                    }
                }
            }
        } else {
            warnMaterialData(LongGrass.class, data);
        }
    }
}
