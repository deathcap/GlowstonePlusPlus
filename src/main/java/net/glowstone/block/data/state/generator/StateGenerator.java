package net.glowstone.block.data.state.generator;

import net.glowstone.block.data.state.value.BooleanStateValue;
import net.glowstone.block.data.state.value.EnumStateValue;
import net.glowstone.block.data.state.value.StateValue;
import org.bukkit.Axis;
import org.bukkit.Instrument;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.type.*;

import java.util.AbstractMap;
import java.util.Map;

public interface StateGenerator<T> {

    EnumStateGenerator<RedstoneWire.Connection> REDSTONE_CONNECTION_NORTH = new EnumStateGenerator<>("north", RedstoneWire.Connection.NONE, RedstoneWire.Connection.values());
    EnumStateGenerator<RedstoneWire.Connection> REDSTONE_CONNECTION_EAST = new EnumStateGenerator<>("east", RedstoneWire.Connection.NONE, RedstoneWire.Connection.values());
    EnumStateGenerator<RedstoneWire.Connection> REDSTONE_CONNECTION_SOUTH = new EnumStateGenerator<>("south", RedstoneWire.Connection.NONE, RedstoneWire.Connection.values());
    EnumStateGenerator<RedstoneWire.Connection> REDSTONE_CONNECTION_WEST = new EnumStateGenerator<>("west", RedstoneWire.Connection.NONE, RedstoneWire.Connection.values());
    EnumStateGenerator<BlockFace> FOUR_FACING = new EnumStateGenerator<>("facing", 0, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST);
    EnumStateGenerator<BlockFace> SIX_FACING_DEFAULT_UP = new EnumStateGenerator<>("facing", BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST, BlockFace.UP, BlockFace.DOWN);
    EnumStateGenerator<BlockFace> SIX_FACING_DEFAULT_DOWN = new EnumStateGenerator<>("facing", BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST, BlockFace.UP, BlockFace.DOWN);
    EnumStateGenerator<BlockFace> SIX_FACING_DEFAULT_NORTH = new EnumStateGenerator<>("facing", BlockFace.NORTH, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST, BlockFace.UP, BlockFace.DOWN);
    EnumStateGenerator<BlockFace> SIX_FACING_DEFAULT_SOUTH = new EnumStateGenerator<>("facing", BlockFace.SOUTH, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST, BlockFace.UP, BlockFace.DOWN);
    EnumStateGenerator<Bed.Part> BED_PART = new EnumStateGenerator<>("part", Bed.Part.FOOT, Bed.Part.values());
    EnumStateGenerator<Axis> AXIS = new EnumStateGenerator<>("axis", Axis.Y, Axis.values());
    EnumStateGenerator<Axis> TWO_AXIS = new EnumStateGenerator<>("axis", Axis.X, Axis.X, Axis.Z);
    EnumStateGenerator<Instrument> INSTRUMENT = new EnumStateGenerator<>("instrument", Instrument.PIANO, Instrument.values(), new AbstractMap.SimpleEntry("harp", Instrument.PIANO));
    EnumStateGenerator<Bisected.Half> HALF = new EnumStateGenerator<>("half", Bisected.Half.BOTTOM, Bisected.Half.values());
    EnumStateGenerator<Bisected.Half> HALF_ALT_NAME = new EnumStateGenerator<>("half", Bisected.Half.BOTTOM, new AbstractMap.SimpleEntry<>("lower", Bisected.Half.BOTTOM), new AbstractMap.SimpleEntry<>("upper", Bisected.Half.TOP));
    EnumStateGenerator<Stairs.Shape> STAIRS_SHAPE = new EnumStateGenerator<>("shape", 0, Stairs.Shape.values());
    EnumStateGenerator<Rail.Shape> RAIL_SHAPE = new EnumStateGenerator<Rail.Shape>("shape", 0, Rail.Shape.values());
    EnumStateGenerator<Door.Hinge> HINGE = new EnumStateGenerator<>("hinge", 0, Door.Hinge.values());
    EnumStateGenerator<Chest.Type> CHEST_TYPE = new EnumStateGenerator<>("type", 0, Chest.Type.values());
    EnumStateGenerator<Comparator.Mode> COMPARATOR_MODE = new EnumStateGenerator<>("mode", 0, Comparator.Mode.values());
    EnumStateGenerator<Slab.Type> SLAB_TYPE  = new EnumStateGenerator("type", Slab.Type.BOTTOM, Slab.Type.values());
    EnumStateGenerator<StructureBlock.Mode> STRUCTURE_MODE = new EnumStateGenerator("mode", 0, StructureBlock.Mode.values());
    EnumStateGenerator<Switch.Face> SWITCH_FACE = new EnumStateGenerator<>("face", Switch.Face.WALL, Switch.Face.values());
    EnumStateGenerator<TechnicalPiston.Type> PISTON_NECK_TYPE  = new EnumStateGenerator<>("type", TechnicalPiston.Type.NORMAL, TechnicalPiston.Type.values());

    BooleanStateGenerator DISARMED = new BooleanStateGenerator("disarmed", false);
    BooleanStateGenerator UNSTABLE = new BooleanStateGenerator("unstable", false);
    BooleanStateGenerator BOOLEAN_NORTH = new BooleanStateGenerator("north", false);
    BooleanStateGenerator BOOLEAN_NORTH_INVERTED = new BooleanStateGenerator("north", true);
    BooleanStateGenerator BOOLEAN_EAST = new BooleanStateGenerator("east", false);
    BooleanStateGenerator BOOLEAN_EAST_INVERTED = new BooleanStateGenerator("east", true);
    BooleanStateGenerator BOOLEAN_SOUTH = new BooleanStateGenerator("south", false);
    BooleanStateGenerator BOOLEAN_SOUTH_INVERTED = new BooleanStateGenerator("south", true);
    BooleanStateGenerator BOOLEAN_WEST = new BooleanStateGenerator("west", false);
    BooleanStateGenerator BOOLEAN_WEST_INVERTED = new BooleanStateGenerator("west", true);
    BooleanStateGenerator BOOLEAN_UP = new BooleanStateGenerator("up", false);
    BooleanStateGenerator BOOLEAN_UP_INVERTED = new BooleanStateGenerator("up", true);
    BooleanStateGenerator BOOLEAN_DOWN = new BooleanStateGenerator("down", false);
    BooleanStateGenerator BOOLEAN_DOWN_INVERTED = new BooleanStateGenerator("down", true);
    BooleanStateGenerator IN_WALL = new BooleanStateGenerator("in_wall", false);
    BooleanStateGenerator ENABLED = new BooleanStateGenerator("enabled", true);
    BooleanStateGenerator HAS_RECORD = new BooleanStateGenerator("has_record", false);
    BooleanStateGenerator OPEN = new BooleanStateGenerator("open", false);
    BooleanStateGenerator WATER_LOGGED = new BooleanStateGenerator("waterlogged", false);
    BooleanStateGenerator WATER_LOGGED_INVERTED = new BooleanStateGenerator("waterlogged", true);
    BooleanStateGenerator SHORT = new BooleanStateGenerator("short", false);
    BooleanStateGenerator POWERED = new BooleanStateGenerator("powered", false);
    BooleanStateGenerator EXTENDED = new BooleanStateGenerator("extended", false);
    BooleanStateGenerator OCCUPIED = new BooleanStateGenerator("occupied", false);
    BooleanStateGenerator SNOWY = new BooleanStateGenerator("snowy", false);
    BooleanStateGenerator DRAG = new BooleanStateGenerator("drag", true);
    BooleanStateGenerator HAS_BOTTLE_0 = new BooleanStateGenerator("has_bottle_0", false);
    BooleanStateGenerator HAS_BOTTLE_1 = new BooleanStateGenerator("has_bottle_1", false);
    BooleanStateGenerator HAS_BOTTLE_2 = new BooleanStateGenerator("has_bottle_2", false);
    BooleanStateGenerator PERSISTENT = new BooleanStateGenerator("persistent", false);
    BooleanStateGenerator TRIGGERED = new BooleanStateGenerator("triggered", false);
    BooleanStateGenerator LIT = new BooleanStateGenerator("lit", false);
    BooleanStateGenerator LIT_INVERTED = new BooleanStateGenerator("lit", true);
    BooleanStateGenerator LOCKED = new BooleanStateGenerator("locked", false);
    BooleanStateGenerator EYE = new BooleanStateGenerator("eye", false);
    BooleanStateGenerator ATTACHED = new BooleanStateGenerator("attached", false);
    BooleanStateGenerator CONDITIONAL = new BooleanStateGenerator("conditional", false);
    BooleanStateGenerator INVERTED = new BooleanStateGenerator("inverted", false);

    IntegerStateGenerator.Ranged EIGHT_LAYERS = new IntegerStateGenerator.Ranged("layers", 1, 1, 8);
    IntegerStateGenerator.Ranged NOTE = new IntegerStateGenerator.Ranged("note", 0, 25);
    IntegerStateGenerator.Ranged EGGS = new IntegerStateGenerator.Ranged("eggs", 1, 1, 5);
    IntegerStateGenerator.Ranged HATCH = new IntegerStateGenerator.Ranged("hatch", 0, 3);
    IntegerStateGenerator.Ranged PICKLES = new IntegerStateGenerator.Ranged("pickles", 1, 1, 5);
    IntegerStateGenerator.Ranged DELAY = new IntegerStateGenerator.Ranged("delay", 1, 1, 5);
    IntegerStateGenerator.Ranged MOISTURE = new IntegerStateGenerator.Ranged("moisture", 0, 8);
    IntegerStateGenerator.Ranged REDSTONE_POWER = new IntegerStateGenerator.Ranged("power", 0, 16);
    IntegerStateGenerator.Ranged TWO_STAGE = new IntegerStateGenerator.Ranged("stage", 0, 2);
    IntegerStateGenerator.Ranged THREE_AGE = new IntegerStateGenerator.Ranged("age", 0, 3);
    IntegerStateGenerator.Ranged FOUR_AGE = new IntegerStateGenerator.Ranged("age", 0, 4);
    IntegerStateGenerator.Ranged SIX_AGE = new IntegerStateGenerator.Ranged("age", 0, 6);
    IntegerStateGenerator.Ranged FOUR_LEVEL = new IntegerStateGenerator.Ranged("level", 0, 4);
    IntegerStateGenerator.Ranged SIXTEEN_LEVEL = new IntegerStateGenerator.Ranged("level", 0, 16);
    IntegerStateGenerator.Ranged EIGHT_DISTANCE  = new IntegerStateGenerator.Ranged("distance", 7, 8);
    IntegerStateGenerator.Ranged EIGHT_AGE = new IntegerStateGenerator.Ranged("age", 0, 8);
    IntegerStateGenerator.Ranged SEVEN_BITES = new IntegerStateGenerator.Ranged("bites", 0, 7);
    IntegerStateGenerator.Ranged SIXTEEN_AGE = new IntegerStateGenerator.Ranged("age", 0, 16);
    IntegerStateGenerator.Ranged SIXTEEN_ROTATION = new IntegerStateGenerator.Ranged("rotation", 0, 16);
    IntegerStateGenerator.Ranged TWENTY_SIX = new IntegerStateGenerator.Ranged("age", 0, 26);

    interface EnumGenerator<T> extends StateGenerator<T> {

        T[] getValues();

    }

    int serialize(T id);
    T deserialize(int serial);
    String getId();
    T getDefaultValue();
    StateValue<T> createStateValue(T value);

    default StateValue<T> createDefaultStateValue(){
        return createStateValue(null);
    }
}
