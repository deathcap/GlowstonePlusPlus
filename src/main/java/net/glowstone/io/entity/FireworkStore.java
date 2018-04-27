package net.glowstone.io.entity;

import java.util.UUID;
import net.glowstone.entity.passive.GlowFirework;
import net.glowstone.io.nbt.NbtSerialization;
import net.glowstone.util.nbt.CompoundTag;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public class FireworkStore extends EntityStore<GlowFirework> {

    public FireworkStore() {
        super(GlowFirework.class, EntityType.FIREWORK);
    }

    @Override
    public GlowFirework createEntity(Location location, CompoundTag compound) {
        return new GlowFirework(location, null, null, null);
    }

    @Override
    public void load(GlowFirework entity, CompoundTag tag) {
        super.load(entity, tag);
        tag.consumeInt(entity::setTicksLived, "Life");
        tag.consumeInt(entity::setLifeTime, "LifeTime");
        tag.consumeItem(entity::setFireworkItem, "FireworksItem");
        tag.consumeUuid(entity::setSpawningEntity, "SpawningEntityMost", "SpawningEntityLeast");
    }

    @Override
    public void save(GlowFirework entity, CompoundTag tag) {
        super.save(entity, tag);

        tag.putInt("Life", entity.getTicksLived());
        tag.putInt("LifeTime", entity.getLifeTime());
        CompoundTag fireworkItem = NbtSerialization.writeItem(entity.getFireworkItem(), -1);
        tag.putCompound("FireworksItem", fireworkItem);

        tag.putLong("SpawningEntityMost", entity.getSpawningEntity().getMostSignificantBits());
        tag.putLong("SpawningEntityLeast", entity.getSpawningEntity().getLeastSignificantBits());
    }
}
