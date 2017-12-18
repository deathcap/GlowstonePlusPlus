package net.glowstone.block.itemtype;

import net.glowstone.entity.GlowPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.TippedArrow;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

public class ItemBow extends ItemTimedUsage {
    private Class<? extends Projectile> currentArrowType;
    private PotionMeta currentTippedArrowMeta;

    @Override
    public void startUse(GlowPlayer player, ItemStack item) {
        ItemStack arrow = null;
        findArrow: for (ItemStack itemStack : player.getInventory().getContents()) {
            switch (itemStack.getType()) {
                case ARROW:
                    arrow = itemStack;
                    currentArrowType = Arrow.class;
                    break;
                case SPECTRAL_ARROW:
                    arrow = itemStack;
                    currentArrowType = SpectralArrow.class;
                    break findArrow;
                case TIPPED_ARROW:
                    arrow = itemStack;
                    currentArrowType = TippedArrow.class;
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    if (itemMeta instanceof PotionMeta) {
                        currentTippedArrowMeta = (PotionMeta) itemMeta;
                    }
                    break findArrow;
                default:
                    // do nothing
            }
        }
        if (arrow != null) {
            if (currentArrowType != Arrow.class
                    || !item.containsEnchantment(Enchantment.ARROW_INFINITE)) {
                arrow.setAmount(arrow.getAmount() - 1);
                // TODO: make atomic, delete if zero
            }
            player.setUsageItem(item);
            player.setUsageTime(20);
        }
    }

    @Override
    public void endUse(GlowPlayer player, ItemStack item) {
        Projectile launchedArrow = player.launchProjectile(currentArrowType);
        if (currentArrowType == TippedArrow.class) {
            TippedArrow launchedTippedArrow = (TippedArrow) launchedArrow;
            launchedTippedArrow.setBasePotionData(currentTippedArrowMeta.getBasePotionData());
            launchedTippedArrow.setColor(currentTippedArrowMeta.getColor());
            for (PotionEffect effect : currentTippedArrowMeta.getCustomEffects()) {
                launchedTippedArrow.addCustomEffect(effect, true);
            }
            currentTippedArrowMeta = null;
        }
        player.setUsageItem(null);
        player.setUsageTime(0);
    }
}
