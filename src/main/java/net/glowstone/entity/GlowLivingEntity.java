package net.glowstone.entity;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

import com.flowpowered.network.Message;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.Getter;
import net.glowstone.EventFactory;
import net.glowstone.GlowWorld;
import net.glowstone.block.GlowBlock;
import net.glowstone.block.ItemTable;
import net.glowstone.block.blocktype.BlockType;
import net.glowstone.constants.GlowPotionEffect;
import net.glowstone.entity.AttributeManager.Key;
import net.glowstone.entity.ai.MobState;
import net.glowstone.entity.ai.TaskManager;
import net.glowstone.entity.meta.MetadataIndex;
import net.glowstone.entity.objects.GlowExperienceOrb;
import net.glowstone.entity.objects.GlowLeashHitch;
import net.glowstone.entity.projectile.GlowProjectile;
import net.glowstone.inventory.EquipmentMonitor;
import net.glowstone.net.GlowSession;
import net.glowstone.net.message.play.entity.AnimateEntityMessage;
import net.glowstone.net.message.play.entity.EntityEffectMessage;
import net.glowstone.net.message.play.entity.EntityEquipmentMessage;
import net.glowstone.net.message.play.entity.EntityHeadRotationMessage;
import net.glowstone.net.message.play.entity.EntityRemoveEffectMessage;
import net.glowstone.net.message.play.player.InteractEntityMessage;
import net.glowstone.net.message.play.player.InteractEntityMessage.Action;
import net.glowstone.util.ExperienceSplitter;
import net.glowstone.util.InventoryUtil;
import net.glowstone.util.Position;
import net.glowstone.util.RayUtil;
import net.glowstone.util.SoundUtil;
import net.glowstone.util.loot.LootData;
import net.glowstone.util.loot.LootingManager;
import org.bukkit.EntityAnimation;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Criterias;
import org.bukkit.scoreboard.Objective;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

/**
 * A GlowLivingEntity is a {@link Player} or {@link Monster}.
 *
 * @author Graham Edgecombe.
 */
public abstract class GlowLivingEntity extends GlowEntity implements LivingEntity {

    /**
     * The entity's AI task manager.
     */
    protected final TaskManager taskManager;
    /**
     * Potion effects on the entity.
     */
    private final Map<PotionEffectType, PotionEffect> potionEffects = new HashMap<>();
    /**
     * The LivingEntity's AttributeManager.
     */
    private final AttributeManager attributeManager;
    /**
     * The entity's health.
     */
    protected double health;
    /**
     * The entity's max health.
     */
    protected double maxHealth;
    /**
     * The LivingEntity's number of ticks since death.
     */
    @Getter
    protected int deathTicks;
    /**
     * <p>The entity's movement as a unit vector, applied each tick according to the entity's speed.
     * </p><p>
     * The y value is not used. X is used for forward movement and z is used for sideways movement.
     * These values are relative to the entity's current yaw.</p>
     */
    protected Vector movement = new Vector();
    /**
     * The speed multiplier of the entity.
     */
    protected double speed = 1;
    /**
     * The magnitude of the last damage the entity took.
     */
    private double lastDamage;
    /**
     * How long the entity has until it runs out of air.
     */
    private int airTicks = 300;
    /**
     * The maximum amount of air the entity can hold.
     */
    private int maximumAir = 300;
    /**
     * The number of ticks remaining in the invincibility period.
     */
    private int noDamageTicks;
    /**
     * The default length of the invincibility period.
     */
    private int maxNoDamageTicks = 10;
    /**
     * Whether the entity should be removed if it is too distant from players.
     */
    private boolean removeDistance;
    /**
     * Whether the (non-Player) entity can pick up armor and tools.
     */
    private boolean pickupItems;
    /**
     * Monitor for the equipment of this entity.
     */
    @Getter
    private EquipmentMonitor equipmentMonitor = new EquipmentMonitor(this);
    /**
     * Whether the entity can automatically glide when falling with an Elytra equipped. This value
     * is ignored for players.
     */
    private boolean fallFlying;
    /**
     * Ticks until the next ambient sound roll.
     */
    private int nextAmbientTime = 1;
    /**
     * The last entity which damaged this living entity.
     */
    private Entity lastDamager;
    /**
     * The head rotation of the living entity, if applicable.
     */
    private float headYaw;
    /**
     * Whether the headYaw value should be updated.
     */
    private boolean headRotated;
    /**
     * The entity's current state.
     */
    private MobState aiState = MobState.NO_AI;
    /**
     * If this entity has swam in lava (for fire application).
     */
    private boolean swamInLava;

    /**
     * Creates a mob within the specified world.
     *
     * @param location The location.
     */
    public GlowLivingEntity(Location location) {
        this(location, 20);
    }

    /**
     * Creates a mob within the specified world.
     *
     * @param location The location.
     * @param maxHealth The max health of this mob.
     */
    protected GlowLivingEntity(Location location, double maxHealth) {
        super(location);
        attributeManager = new AttributeManager(this);
        this.maxHealth = maxHealth;
        attributeManager.setProperty(Key.KEY_MAX_HEALTH, maxHealth);
        health = maxHealth;
        taskManager = new TaskManager(this);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Internals

    @Override
    public void pulse() {
        super.pulse();

        if (isDead()) {
            deathTicks++;
            if (deathTicks >= 20 && getClass() != GlowPlayer.class) {
                remove();
            }
        }

        // invulnerability
        if (noDamageTicks > 0) {
            --noDamageTicks;
        }

        Material mat = getEyeLocation().getBlock().getType();
        // breathing
        if (mat == Material.WATER || mat == Material.STATIONARY_WATER) {
            if (canTakeDamage(DamageCause.DROWNING)) {
                --airTicks;
                if (airTicks <= -20) {
                    airTicks = 0;
                    damage(1, DamageCause.DROWNING);
                }
            }
        } else {
            airTicks = maximumAir;
        }

        if (isTouchingMaterial(Material.CACTUS)) {
            damage(1, DamageCause.CONTACT);
        }
        if (location.getY() < -64) { // no canTakeDamage call - pierces through game modes
            damage(4, DamageCause.VOID);
        }

        if (isWithinSolidBlock()) {
            damage(1, DamageCause.SUFFOCATION);
        }

        if (getLocation().getBlock().getType() == Material.LAVA
            || getLocation().getBlock().getType() == Material.STATIONARY_LAVA) {
            damage(4, DamageCause.LAVA);
            if (swamInLava) {
                setFireTicks(getFireTicks() + 2);
            } else {
                setFireTicks(getFireTicks() + 300);
                swamInLava = true;
            }
        } else {
            swamInLava = false;
            if (getLocation().getBlock().getType() == Material.WATER
                || getLocation().getBlock().getType() == Material.STATIONARY_WATER) {
                setFireTicks(0);
            }
        }

        // potion effects
        List<PotionEffect> effects = new ArrayList<>(potionEffects.values());
        for (PotionEffect effect : effects) {
            // pulse effect
            PotionEffectType type = effect.getType();
            GlowPotionEffect glowType = GlowPotionEffect.getEffect(type);
            if (glowType != null) {
                glowType.pulse(this, effect);
            }

            if (effect.getDuration() > 0) {
                // reduce duration and re-add
                addPotionEffect(
                    new PotionEffect(type, effect.getDuration() - 1, effect.getAmplifier(),
                        effect.isAmbient()), true);
            } else {
                // remove
                removePotionEffect(type);
            }
        }

        if (getFireTicks() > 0 && getFireTicks() % 20 == 0) {
            damage(1, DamageCause.FIRE_TICK);
        }

        GlowBlock under = (GlowBlock) getLocation().getBlock().getRelative(BlockFace.DOWN);
        BlockType type = ItemTable.instance().getBlock(under.getType());
        if (type != null) {
            type.onEntityStep(under, this);
        }
        nextAmbientTime--;
        if (!isDead() && getAmbientSound() != null && nextAmbientTime == 0 && !isSilent()) {
            double v = ThreadLocalRandom.current().nextDouble();
            if (v <= 0.2) {
                world
                    .playSound(getLocation(), getAmbientSound(), getSoundVolume(), getSoundPitch());
            }
        }
        if (nextAmbientTime == 0) {
            nextAmbientTime = getAmbientDelay();
        }
    }

    @Override
    protected void pulsePhysics() {
        // drag application
        movement.multiply(airDrag);
        // convert movement x/z to a velocity
        Vector velMovement = getVelocityFromMovement();
        velocity.add(velMovement);
        super.pulsePhysics();
    }

    protected Vector getVelocityFromMovement() {
        // ensure movement vector is in correct format
        movement.setY(0);

        double mag = movement.getX() * movement.getX() + movement.getZ() * movement.getZ();
        // don't do insignificant movement
        if (mag < 0.01) {
            return new Vector();
        }
        // unit vector of movement
        movement.setX(movement.getX() / mag);
        movement.setZ(movement.getZ() / mag);

        // scale to how fast the entity can go
        mag *= speed;
        Vector movement = this.movement.clone();
        movement.multiply(mag);

        // make velocity vector relative to where the entity is facing
        double yaw = Math.toRadians(location.getYaw());
        double z = Math.sin(yaw);
        double x = Math.cos(yaw);
        movement.setX(movement.getZ() * x - movement.getX() * z);
        movement.setZ(movement.getX() * x + movement.getZ() * z);

        // apply the movement multiplier
        if (!isOnGround() || location.getBlock().isLiquid()) {
            // constant multiplier in liquid or not on ground
            movement.multiply(0.02);
        } else {
            this.slipMultiplier = ((GlowBlock) location.getBlock()).getMaterialValues()
                .getSlipperiness();
            double slipperiness = slipMultiplier * 0.91;
            movement.multiply(0.1 * (0.1627714 / Math.pow(slipperiness, 3)));
        }

        return movement;
    }

    public Vector getMovement() {
        return movement.clone();
    }

    public void setMovement(Vector movement) {
        this.movement = movement;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    protected void jump() {
        if (location.getBlock().isLiquid()) {
            // jump out more when you breach the surface of the liquid
            if (location.getBlock().getRelative(BlockFace.UP).isEmpty()) {
                velocity.setY(velocity.getY() + 0.3);
            }
            // less jumping in liquid
            velocity.setY(velocity.getY() + 0.04);
        } else {
            // jump normally
            velocity.setY(velocity.getY() + 0.42);
        }
    }

    @Override
    public void reset() {
        super.reset();
        equipmentMonitor.resetChanges();
        headRotated = false;
    }

    @Override
    public List<Message> createUpdateMessage(GlowSession session) {
        List<Message> messages = super.createUpdateMessage(session);

        messages.addAll(equipmentMonitor.getChanges().stream()
            .map(change -> new EntityEquipmentMessage(id, change.slot, change.item))
            .collect(Collectors.toList()));
        if (headRotated) {
            messages.add(new EntityHeadRotationMessage(id, Position.getIntHeadYaw(headYaw)));
        }
        attributeManager.applyMessages(messages);

        return messages;
    }

    public AttributeManager getAttributeManager() {
        return attributeManager;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Properties

    @Override
    public double getEyeHeight() {
        return 0;
    }

    @Override
    public double getEyeHeight(boolean ignoreSneaking) {
        return getEyeHeight();
    }

    @Override
    public Location getEyeLocation() {
        return getLocation().add(0, getEyeHeight(), 0);
    }

    @Override
    public Player getKiller() {
        return null;
    }

    @Override
    public void setKiller(Player player) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public boolean hasLineOfSight(Entity other) {
        return false;
    }

    public float getHeadYaw() {
        return headYaw;
    }

    public void setHeadYaw(float headYaw) {
        this.headYaw = headYaw;
        this.headRotated = true;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Properties

    @Override
    public EntityEquipment getEquipment() {
        return null;
    }

    @Override
    public int getNoDamageTicks() {
        return noDamageTicks;
    }

    @Override
    public void setNoDamageTicks(int ticks) {
        noDamageTicks = ticks;
    }

    @Override
    public int getMaximumNoDamageTicks() {
        return maxNoDamageTicks;
    }

    @Override
    public void setMaximumNoDamageTicks(int ticks) {
        maxNoDamageTicks = ticks;
    }

    @Override
    public int getRemainingAir() {
        return airTicks;
    }

    @Override
    public void setRemainingAir(int ticks) {
        airTicks = Math.min(ticks, maximumAir);
    }

    @Override
    public int getMaximumAir() {
        return maximumAir;
    }

    @Override
    public void setMaximumAir(int ticks) {
        maximumAir = Math.max(0, ticks);
    }

    @Override
    public boolean getRemoveWhenFarAway() {
        return removeDistance;
    }

    @Override
    public void setRemoveWhenFarAway(boolean remove) {
        removeDistance = remove;
    }

    @Override
    public boolean getCanPickupItems() {
        return pickupItems;
    }

    @Override
    public void setCanPickupItems(boolean pickup) {
        pickupItems = pickup;
    }

    /**
     * Get the hurt sound of this entity, or null for silence.
     *
     * @return the hurt sound if available
     */
    protected Sound getHurtSound() {
        return null;
    }

    /**
     * Get the death sound of this entity, or null for silence.
     *
     * @return the death sound if available
     */
    protected Sound getDeathSound() {
        return null;
    }

    /**
     * Get the ambient sound this entity makes randomly, or null for silence.
     *
     * @return the ambient sound if available
     */
    protected Sound getAmbientSound() {
        return null;
    }

    /**
     * Get the minimal delay until the entity can produce an ambient sound.
     *
     * @return the minimal delay until the entity can produce an ambient sound
     */
    protected int getAmbientDelay() {
        return 80;
    }

    /**
     * The volume of the sounds this entity makes.
     *
     * @return the volume of the sounds
     */
    protected float getSoundVolume() {
        return 1.0F;
    }

    /**
     * The pitch of the sounds this entity makes.
     *
     * @return the pitch of the sounds
     */
    protected float getSoundPitch() {
        return SoundUtil.randomReal(0.2F) + 1F;
    }

    /**
     * Get whether this entity should take damage from the specified source.
     *
     * <p>Usually used to check environmental sources such as drowning.
     *
     * @param damageCause the damage source to check
     * @return whether this entity can take damage from the source
     */
    public boolean canTakeDamage(DamageCause damageCause) {
        return true;
    }

    /**
     * Get whether of not this entity is an arthropod.
     *
     * @return true if this entity is an arthropod, false otherwise
     */
    public boolean isArthropod() {
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Line of Sight

    /**
     * Get whether or not this entity is undead.
     *
     * @return true if this entity is undead, false otherwise
     */
    public boolean isUndead() {
        return false;
    }

    private List<Block> getLineOfSight(Set<Material> transparent, int maxDistance, int maxLength) {
        // same limit as CraftBukkit
        if (maxDistance > 120) {
            maxDistance = 120;
        }

        LinkedList<Block> blocks = new LinkedList<>();
        Iterator<Block> itr = new BlockIterator(this, maxDistance);
        while (itr.hasNext()) {
            Block block = itr.next();
            blocks.add(block);
            if (maxLength != 0 && blocks.size() > maxLength) {
                blocks.removeFirst();
            }
            Material material = block.getType();
            if (transparent == null) {
                if (material != Material.AIR) {
                    break;
                }
            } else {
                if (!transparent.contains(material)) {
                    break;
                }
            }
        }
        return blocks;
    }

    private List<Block> getLineOfSight(HashSet<Byte> transparent, int maxDistance, int maxLength) {
        Set<Material> materials = transparent.stream().map(Material::getMaterial)
            .collect(Collectors.toSet());
        return getLineOfSight(materials, maxDistance, maxLength);
    }

    @Override
    public List<Block> getLineOfSight(Set<Material> transparent, int maxDistance) {
        return getLineOfSight(transparent, maxDistance, 0);
    }

    @Deprecated
    public Block getTargetBlock(HashSet<Byte> transparent, int maxDistance) {
        return getLineOfSight(transparent, maxDistance, 0).get(0);
    }

    @Override
    public Block getTargetBlock(Set<Material> materials, int maxDistance) {
        return getLineOfSight(materials, maxDistance).get(0);
    }

    @Deprecated
    public List<Block> getLastTwoTargetBlocks(HashSet<Byte> transparent, int maxDistance) {
        return getLineOfSight(transparent, maxDistance, 2);
    }

    @Override
    public List<Block> getLastTwoTargetBlocks(Set<Material> materials, int maxDistance) {
        return getLineOfSight(materials, maxDistance, 2);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Projectiles

    /**
     * Returns whether the entity's eye location is within a solid block.
     *
     * @return if the entity is in a solid block
     */
    public boolean isWithinSolidBlock() {
        return getEyeLocation().getBlock().getType().isOccluding();
    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> projectile) {
        return launchProjectile(projectile,
            getLocation().getDirection());  // todo: multiply by some speed
    }

    ////////////////////////////////////////////////////////////////////////////
    // Health

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> clazz, Vector vector) {
        float offset = 0.0F;
        float velocity = 1.5F;
        if (Arrow.class.isAssignableFrom(clazz)) {
            velocity = 3.0F;
            if (this instanceof GlowPlayer) {
                GlowPlayer player = (GlowPlayer) this;
                if (player.getUsageItem() != null
                        && player.getUsageItem().getType() == Material.BOW) {
                    int timeUsed = (int)
                            (20 - (player.getUsageTime() <= 0 ? 0 : player.getUsageTime()));
                    velocity = 3.0F * ((float) timeUsed / 20.0F);
                }
            }
        }
        T projectile = launchProjectile(clazz, vector, offset, velocity);
        if (Arrow.class.isAssignableFrom(clazz) && velocity == 3.0F) {
            ((Arrow) projectile).setCritical(true);
        }
        return projectile;
    }

    /**
     * Launches a projectile from this entity.
     *
     * @param clazz the projectile class
     * @param vector TODO: document this parameter
     * @param offset TODO: document this parameter
     * @param velocity the velocity for the first flight tick
     * @param <T> the projectile class
     * @return the launched projectile
     */
    public <T extends Projectile> T launchProjectile(Class<? extends T> clazz, Vector vector,
            float offset, float velocity) {
        if (vector == null) {
            vector = getVelocity();
        }

        T projectile = throwProjectile(clazz, getEyeLocation().clone(), vector, offset, velocity);
        projectile.setShooter(this);
        return projectile;
    }

    protected <T extends Projectile> T throwProjectile(Class<? extends T> type, Location location,
            Vector originalVector, float offset, float velocity) {
        double k = Math.toRadians(-1);
        double x = cos(k * location.getPitch()) * sin(k * location.getYaw());
        double y = sin(k * (location.getPitch() - offset));
        double z = cos(location.getPitch() * k) * cos(location.getYaw() * k);
        T projectile = throwProjectile(type, location, x, y, z, velocity);
        projectile.getVelocity().add(originalVector);
        return projectile;
    }

    private <T extends Projectile> T throwProjectile(Class<? extends T> clazz, Location location,
            double x, double y, double z, float velocity) {
        double k = Math.sqrt(x * x + y * y + z * z);
        x += (x * (velocity - k)) / k;
        y += (y * (velocity - k)) / k;
        z += (z * (velocity - k)) / k;

        location.add(location.getDirection());
        location.setPitch(0);
        location.setYaw(0);

        T projectile = ((GlowWorld) location.getWorld()).spawn(location, clazz);
        projectile.setVelocity(new Vector(x, y, z));
        ((GlowProjectile) projectile).setRawLocation(location);
        return projectile;
    }

    @Override
    public double getHealth() {
        return health;
    }

    @Override
    public void setHealth(double health) {
        if (health < 0) {
            health = 0;
        }
        if (health > getMaxHealth()) {
            health = getMaxHealth();
        }
        this.health = health;
        metadata.set(MetadataIndex.HEALTH, (float) health);

        for (Objective objective : getServer().getScoreboardManager().getMainScoreboard()
            .getObjectivesByCriteria(Criterias.HEALTH)) {
            objective.getScore(getName()).setScore((int) health);
        }

        if (health <= 0) {
            active = false;
            Sound deathSound = getDeathSound();
            if (deathSound != null && !isSilent()) {
                world.playSound(location, deathSound, getSoundVolume(), getSoundPitch());
            }
            playEffect(EntityEffect.DEATH);
            if (this instanceof GlowPlayer) {
                GlowPlayer player = (GlowPlayer) this;
                ItemStack mainHand = player.getInventory().getItemInMainHand();
                ItemStack offHand = player.getInventory().getItemInOffHand();
                if (!InventoryUtil.isEmpty(mainHand) && mainHand.getType() == Material.TOTEM) {
                    player.getInventory().setItemInMainHand(InventoryUtil.createEmptyStack());
                    player.setHealth(1.0);
                    active = true;
                    return;
                } else if (!InventoryUtil.isEmpty(offHand) && offHand.getType() == Material.TOTEM) {
                    player.getInventory().setItemInOffHand(InventoryUtil.createEmptyStack());
                    player.setHealth(1.0);
                    active = true;
                    return;
                }
                List<ItemStack> items = new ArrayList<>();
                if (!world.getGameRuleMap().getBoolean("keepInventory")) {
                    items = Arrays.stream(player.getInventory().getContents())
                        .filter(stack -> !InventoryUtil.isEmpty(stack))
                        .collect(Collectors.toList());
                    player.getInventory().clear();
                }
                PlayerDeathEvent event = new PlayerDeathEvent(player, items, 0,
                    player.getDisplayName() + " died.");
                EventFactory.callEvent(event);
                server.broadcastMessage(event.getDeathMessage());
                for (ItemStack item : items) {
                    world.dropItemNaturally(getLocation(), item);
                }
                player.setShoulderEntityRight(null);
                player.setShoulderEntityLeft(null);
                player.incrementStatistic(Statistic.DEATHS);
            } else {
                EntityDeathEvent deathEvent = new EntityDeathEvent(this, new ArrayList<>());
                if (world.getGameRuleMap().getBoolean("doMobLoot")) {
                    LootData data = LootingManager.generate(this);
                    deathEvent.getDrops().addAll(data.getItems());
                    if (data.getExperience() > 0) {
                        // split experience
                        Integer[] values = ExperienceSplitter.cut(data.getExperience());
                        for (Integer exp : values) {
                            double xmod = ThreadLocalRandom.current().nextDouble() - 0.5;
                            double zmod = ThreadLocalRandom.current().nextDouble() - 0.5;
                            Location xpLocation = new Location(world,
                                location.getBlockX() + 0.5 + xmod, location.getY(),
                                location.getBlockZ() + 0.5 + zmod);
                            GlowExperienceOrb orb = (GlowExperienceOrb) world
                                .spawnEntity(xpLocation, EntityType.EXPERIENCE_ORB);
                            orb.setExperience(exp);
                        }
                    }
                }
                deathEvent = EventFactory.callEvent(deathEvent);
                for (ItemStack item : deathEvent.getDrops()) {
                    world.dropItemNaturally(getLocation(), item);
                }
            }
        }
    }

    @Override
    public void damage(double amount, Entity source, DamageCause cause) {
        // invincibility timer
        if (noDamageTicks > 0 || health <= 0 || !canTakeDamage(cause) || isInvulnerable()) {
            return;
        } else {
            noDamageTicks = maxNoDamageTicks;
        }

        // fire resistance
        if (cause != null && hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
            boolean isFireDamage = false;
            if (source instanceof Fireball) {
                isFireDamage = true;
            } else {
                switch (cause) {
                    case FIRE:
                    case FIRE_TICK:
                    case HOT_FLOOR:
                    case LAVA:
                        isFireDamage = true;
                        break;
                    default:
                        // do nothing
                }
            }
            if (isFireDamage) {
                return;
            }
        }

        // armor damage protection
        // formula source: http://minecraft.gamepedia.com/Armor#Damage_Protection
        double defensePoints = getAttributeManager().getPropertyValue(Key.KEY_ARMOR);
        double toughness = getAttributeManager().getPropertyValue(Key.KEY_ARMOR_TOUGHNESS);
        amount = amount * (1 - Math.min(20.0,
            Math.max(defensePoints / 5.0, defensePoints - amount / (2.0 + toughness / 4.0))) / 25);

        // fire event
        EntityDamageEvent event;
        if (source == null) {
            event = EventFactory.onEntityDamage(new EntityDamageEvent(this, cause, amount));
        } else {
            event = EventFactory
                .onEntityDamage(new EntityDamageByEntityEvent(source, this, cause, amount));
        }
        if (event.isCancelled()) {
            return;
        }

        // apply damage
        amount = event.getFinalDamage();
        lastDamage = amount;
        setHealth(health - amount);
        playEffect(EntityEffect.HURT);

        if (cause == DamageCause.ENTITY_ATTACK && source != null) {
            Vector distance = RayUtil
                .getRayBetween(getLocation(), ((LivingEntity) source).getEyeLocation());

            Vector rayLength = RayUtil.getVelocityRay(distance).normalize();

            Vector currentVelocity = getVelocity();
            currentVelocity.add(rayLength.multiply(((amount + 1) / 2d)));
            setVelocity(currentVelocity);
        }

        // play sounds, handle death
        if (health > 0) {
            Sound hurtSound = getHurtSound();
            if (hurtSound != null && !isSilent()) {
                world.playSound(location, hurtSound, getSoundVolume(), getSoundPitch());
            }
        }
        setLastDamager(source);
    }

    @Override
    public double getMaxHealth() {
        return attributeManager.getPropertyValue(Key.KEY_MAX_HEALTH);
    }

    @Override
    public void setMaxHealth(double health) {
        attributeManager.setProperty(Key.KEY_MAX_HEALTH, health);
    }

    @Override
    public void resetMaxHealth() {
        setMaxHealth(maxHealth);
    }

    @Override
    public double getLastDamage() {
        return lastDamage;
    }

    @Override
    public void setLastDamage(double damage) {
        lastDamage = damage;
    }

    public Entity getLastDamager() {
        return lastDamager;
    }

    public void setLastDamager(Entity lastDamager) {
        this.lastDamager = lastDamager;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Potion effects

    @Override
    public boolean addPotionEffect(PotionEffect effect) {
        return addPotionEffect(effect, false);
    }

    @Override
    public boolean addPotionEffect(PotionEffect effect, boolean force) {
        if (potionEffects.containsKey(effect.getType())) {
            if (force) {
                removePotionEffect(effect.getType());
            } else {
                return false;
            }
        }

        potionEffects.put(effect.getType(), effect);

        EntityEffectMessage msg = new EntityEffectMessage(getEntityId(), effect.getType().getId(),
            effect.getAmplifier(), effect.getDuration(), effect.isAmbient());
        for (GlowPlayer player : world.getRawPlayers()) {
            if (player == this) {
                // special handling for players having a different view of themselves
                player.getSession().send(
                    new EntityEffectMessage(0, effect.getType().getId(), effect.getAmplifier(),
                        effect.getDuration(), effect.isAmbient()));
            } else if (player.canSeeEntity(this)) {
                player.getSession().send(msg);
            }
        }
        return true;
    }

    @Override
    public boolean addPotionEffects(Collection<PotionEffect> effects) {
        boolean result = true;
        for (PotionEffect effect : effects) {
            if (!addPotionEffect(effect)) {
                result = false;
            }
        }
        return result;
    }

    @Override
    public boolean hasPotionEffect(PotionEffectType type) {
        return potionEffects.containsKey(type);
    }

    @Override
    public PotionEffect getPotionEffect(PotionEffectType potionEffectType) {
        return null;
    }

    @Override
    public void removePotionEffect(PotionEffectType type) {
        if (!hasPotionEffect(type)) {
            return;
        }
        potionEffects.remove(type);

        EntityRemoveEffectMessage msg = new EntityRemoveEffectMessage(getEntityId(), type.getId());
        for (GlowPlayer player : world.getRawPlayers()) {
            if (player == this) {
                // special handling for players having a different view of themselves
                player.getSession().send(new EntityRemoveEffectMessage(0, type.getId()));
            } else if (player.canSeeEntity(this)) {
                player.getSession().send(msg);
            }
        }
    }

    @Override
    public Collection<PotionEffect> getActivePotionEffects() {
        return Collections.unmodifiableCollection(potionEffects.values());
    }

    @Override
    public void setOnGround(boolean onGround) {
        float fallDistance = getFallDistance();
        if (onGround && fallDistance > 3f) {
            float damage = fallDistance - 3f;
            damage = Math.round(damage);
            if (damage > 0f) {
                Material standingType = location.getBlock().getRelative(BlockFace.DOWN).getType();
                // todo: only when bouncing
                if (standingType == Material.SLIME_BLOCK) {
                    damage = 0f;
                }

                if (standingType == Material.HAY_BLOCK) {
                    damage *= 0.2f;
                }

                damage(damage, DamageCause.FALL);
            }
        }
        super.setOnGround(onGround);
    }

    public boolean isFallFlying() {
        return fallFlying;
    }

    public void setFallFlying(boolean fallFlying) {
        this.fallFlying = fallFlying;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Leashes

    @Override
    public boolean isGliding() {
        return metadata.getBit(MetadataIndex.STATUS, MetadataIndex.StatusFlags.GLIDING);
    }

    @Override
    public void setGliding(boolean gliding) {
        if (EventFactory.callEvent(new EntityToggleGlideEvent(this, gliding)).isCancelled()) {
            return;
        }

        metadata.setBit(MetadataIndex.STATUS, MetadataIndex.StatusFlags.GLIDING, gliding);
    }

    /**
     * Returns the AI state.
     *
     * @return the AI state
     */
    public MobState getState() {
        return aiState;
    }

    /**
     * Sets the AI state.
     *
     * @param state the new AI state
     */
    public void setState(MobState state) {
        if (aiState != state) {
            aiState = state;
            getTaskManager().updateState();
        }
    }

    @Override
    public void setAI(boolean ai) {
        if (ai) {
            if (aiState == MobState.NO_AI) {
                setState(MobState.IDLE);
            }
        } else {
            setState(MobState.NO_AI);
        }
    }

    @Override
    public boolean hasAI() {
        return aiState != MobState.NO_AI;
    }

    @Override
    public boolean isCollidable() {
        // todo: 1.11
        return true;
    }

    @Override
    public void setCollidable(boolean collidable) {
        // todo: 1.11
    }

    @Override
    public int getArrowsStuck() {
        // todo: 1.11
        return 0;
    }

    @Override
    public void setArrowsStuck(int arrowsStuck) {
        // todo: 1.11
    }

    @Override
    public void playAnimation(EntityAnimation animation) {
        AnimateEntityMessage message = new AnimateEntityMessage(getEntityId(), animation.ordinal());
        getWorld().getRawPlayers().stream()
            .filter(observer -> observer != this && observer.canSeeEntity(this))
            .forEach(observer -> observer.getSession().send(message));
    }

    @Override
    public AttributeInstance getAttribute(Attribute attribute) {
        // todo: 1.11
        return null;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    @Override
    public boolean entityInteract(GlowPlayer player, InteractEntityMessage message) {
        super.entityInteract(player, message);

        if (message.getAction() != Action.INTERACT.ordinal()) {
            return false;
        }

        ItemStack handItem = InventoryUtil
            .itemOrEmpty(player.getInventory().getItem(message.getHandSlot()));
        if (isLeashed() && player.equals(this.getLeashHolder())
            && message.getHandSlot() == EquipmentSlot.HAND) {
            if (EventFactory.callEvent(new PlayerUnleashEntityEvent(this, player)).isCancelled()) {
                return false;
            }

            setLeashHolder(null);
            if (player.getGameMode() != GameMode.CREATIVE) {
                world.dropItemNaturally(this.location, new ItemStack(Material.LEASH));
            }
            return true;
        } else if (!InventoryUtil.isEmpty(handItem) && handItem.getType() == Material.LEASH) {
            if (!GlowLeashHitch.isAllowedLeashHolder(this.getType()) || this.isLeashed()
                || EventFactory.callEvent(new PlayerLeashEntityEvent(this, player, player))
                .isCancelled()) {
                return false;
            }

            if (player.getGameMode() != GameMode.CREATIVE) {
                if (handItem.getAmount() > 1) {
                    handItem.setAmount(handItem.getAmount() - 1);
                } else {
                    handItem = InventoryUtil.createEmptyStack();
                }
                player.getInventory().setItem(message.getHandSlot(), handItem);
            }

            setLeashHolder(player);
            return true;
        }

        return false;
    }
}

