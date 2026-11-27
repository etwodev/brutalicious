package brutalicious.gen;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class Decor {
    private Decor() {}

    public static final String LOOT_APARTMENT = "brutalicious:chests/apartment";
    public static final String LOOT_OFFICE = "brutalicious:chests/office";
    public static final String LOOT_ARCHIVE = "brutalicious:chests/archive";
    public static final String LOOT_TUNNEL = "brutalicious:chests/tunnel";
    public static final String LOOT_CISTERN = "brutalicious:chests/cistern";
    public static final String LOOT_MILITARY = "brutalicious:chests/military";
    public static final String LOOT_HOSPITAL = "brutalicious:chests/hospital";
    public static final String LOOT_INDUSTRIAL = "brutalicious:chests/industrial";
    public static final String LOOT_FARM = "brutalicious:chests/farm";
    public static final String LOOT_DEEP = "brutalicious:chests/deep_works";
    public static final String LOOT_VAULT = "brutalicious:chests/vault";
    public static final String LOOT_FOOTINGS = "brutalicious:chests/footings";
    public static final String LOOT_SEEDS = "brutalicious:chests/seed_vault";

    static final double PICKED_OVER = 0.6;
    static final double PICKED_OVER_ABANDONED = 0.9;

    static boolean deep(String lootTable) {
        return lootTable.equals(LOOT_CISTERN) || lootTable.equals(LOOT_DEEP) || lootTable.equals(LOOT_VAULT)
                || lootTable.equals(LOOT_FOOTINGS) || lootTable.equals(LOOT_SEEDS);
    }

    public static void container(Column c, int y, BlockState state, String lootTable) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", state.is(Blocks.BARREL) ? "minecraft:barrel" : "minecraft:chest");
        double picked = deep(lootTable) ? 0 : Zones.zoneAt(c.seed, c.x, c.z) == Zones.Zone.ABANDONED ? PICKED_OVER_ABANDONED : PICKED_OVER;
        if (!Rng.chance(c.hash(y, 78), picked)) {
            tag.putString("LootTable", lootTable);
            tag.putLong("LootTableSeed", c.hash(y, 77));
        }
        c.blockEntity(y, state, tag);
    }

    public static void spawner(Column c, int y, String entity) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", "minecraft:mob_spawner");
        CompoundTag data = new CompoundTag();
        CompoundTag ent = new CompoundTag();
        ent.putString("id", entity);
        data.put("entity", ent);
        tag.put("SpawnData", data);
        tag.putShort("Delay", (short) 20);
        tag.putShort("MinSpawnDelay", (short) 200);
        tag.putShort("MaxSpawnDelay", (short) 800);
        tag.putShort("SpawnCount", (short) 3);
        tag.putShort("MaxNearbyEntities", (short) 6);
        tag.putShort("RequiredPlayerRange", (short) 16);
        tag.putShort("SpawnRange", (short) 4);
        c.blockEntity(y, B.SPAWNER, tag);
    }

    public enum RoomKind { HOME, OFFICE, ARCHIVE, LOBBY, BUNKER, MILITARY, HOSPITAL, DEPOT }

    public static void bed(Column c, int y, boolean white, net.minecraft.core.Direction head, boolean isHead) {
        BlockState s = (white ? Blocks.WHITE_BED : Blocks.GRAY_BED).defaultBlockState()
                .setValue(net.minecraft.world.level.block.BedBlock.FACING, head)
                .setValue(net.minecraft.world.level.block.BedBlock.PART, isHead ? net.minecraft.world.level.block.state.properties.BedPart.HEAD : net.minecraft.world.level.block.state.properties.BedPart.FOOT);
        CompoundTag tag = new CompoundTag();
        tag.putString("id", "minecraft:bed");
        c.blockEntity(y, s, tag);
    }

    public static void floorItem(Column c, int y, RoomKind kind, double decay) {
        long h = c.hash(y, 11);
        double u = Rng.unit(h);
        double rubble = 0.002 + decay * 0.01;
        if (u < rubble) { c.set(y, (h & 16) == 0 ? B.COBBLE_SLAB : B.ANDESITE_SLAB); return; }
        u -= rubble;
        switch (kind) {
            case HOME -> {
                if (u < 0.0030) { container(c, y, B.BARREL, LOOT_APARTMENT); return; }
                if (u < 0.0036) { c.set(y, B.POTTED_DEAD_BUSH); return; }
                if (u < 0.0042) { c.set(y, B.CAULDRON); return; }
                if (u < 0.0046) { c.set(y, B.CANDLE); return; }
                if (u < 0.0047) { c.set(y, B.CANDLE_LIT); return; }
            }
            case OFFICE -> {
                if (u < 0.0025) { container(c, y, B.CHEST, LOOT_OFFICE); return; }
                if (u < 0.0060) { c.set(y, B.SMOOTH_STONE_SLAB); return; }
            }
            case ARCHIVE -> {
                if (u < 0.0035) { container(c, y, B.CHEST, LOOT_ARCHIVE); return; }
            }
            case BUNKER -> {
                if (u < 0.00015) { spawner(c, y, pick(h >>> 24, "minecraft:zombie", "minecraft:skeleton", "minecraft:spider")); return; }
                if (u < 0.0030) { container(c, y, B.CHEST, LOOT_ARCHIVE); return; }
                if (u < 0.0034) { c.set(y, B.rot16(B.SKULL, (int) (h >>> 20))); return; }
            }
            case MILITARY -> {
                if (u < 0.0040) { container(c, y, B.CHEST, LOOT_MILITARY); return; }
                if (u < 0.0070) { c.set(y, B.BARREL); return; }
                if (u < 0.0080) { c.set(y, B.ANVIL); return; }
            }
            case HOSPITAL -> {
                if (u < 0.0040) { container(c, y, B.CHEST, LOOT_HOSPITAL); return; }
                if (u < 0.0070) { c.set(y, B.BREWING_STAND); return; }
                if (u < 0.0090) { c.set(y, B.CAULDRON); return; }
            }
            case DEPOT -> {
                if (u < 0.0040) { container(c, y, B.BARREL, LOOT_INDUSTRIAL); return; }
            }
            case LOBBY -> {
            }
        }
    }

    public static void livedItem(Column c, int y, long room) {
        long h = c.hash(y, 13);
        double u = Rng.unit(h);
        BlockState roll = (room & 2) == 0 ? B.GRAY_CARPET : B.LIGHT_GRAY_CARPET;
        if (u < 0.10) { c.set(y, roll); return; }
        if (u < 0.125) { container(c, y, B.BARREL, Camp.LOOT); return; }
        if (u < 0.135) { container(c, y, B.CHEST, Camp.LOOT); return; }
        if (u < 0.150) { c.set(y, B.CRAFTING_TABLE); return; }
        if (u < 0.165) { c.set(y, B.FURNACE); return; }
        if (u < 0.180) { c.set(y, B.WATER_CAULDRON); return; }
        if (u < 0.195) { c.set(y, B.CANDLE); return; }
        if (u < 0.210) { c.set(y, B.POTTED_FERN); return; }
        if (u < 0.220) { c.set(y, B.BOOKSHELF); return; }
        if (u < 0.235) { c.set(y, B.BARREL); return; }
    }

    static final BlockState[] CONSOLE_UNITS = {
            B.facing(Blocks.OBSERVER, net.minecraft.core.Direction.NORTH), B.facing(Blocks.DISPENSER, net.minecraft.core.Direction.NORTH),
            B.facing(Blocks.DROPPER, net.minecraft.core.Direction.NORTH), B.facing(Blocks.OBSERVER, net.minecraft.core.Direction.NORTH),
            B.JUKEBOX, B.NOTE_BLOCK, B.SMOOTH_STONE_SLAB};

    public static void console(Column c, int y, int u, int v, long h) {
        int row = Math.floorMod(v, 4);
        if (Math.floorMod(u, 8) == 0) {
            if (row != 0) { c.set(y, B.JUKEBOX); c.set(y + 1, B.NOTE_BLOCK); c.set(y + 2, B.BLACK_GLASS); }
            return;
        }
        if (row == 1) {
            c.set(y, CONSOLE_UNITS[(int) Math.floorMod(h ^ (u * 31L), (long) CONSOLE_UNITS.length)]);
            long s = Rng.hash(h, u, 7);
            if (Rng.chance(s, 0.55)) c.set(y + 1, B.BLACK_GLASS);
            else if (Rng.chance(s >>> 8, 0.4)) c.set(y + 1, B.STONE_BUTTON);
        } else if (row == 2 && Rng.chance(Rng.hash(h, u, 9), 0.3)) {
            c.set(y, B.stair(Blocks.POLISHED_BLACKSTONE_STAIRS, net.minecraft.core.Direction.SOUTH));
        } else if (row == 0 && Rng.chance(Rng.hash(h, u, 11), 0.008)) {
            container(c, y, B.CHEST, LOOT_OFFICE);
        }
    }

    public static void ceiling(Column c, int y, int u, int v, double decay) {
        if (Math.floorMod(u, 6) == 3 && Math.floorMod(v, 6) == 3) {
            if (decay < 0) { c.set(y, Rng.chance(c.hash(y, 12), 0.5) ? B.LANTERN_HANGING : B.REDSTONE_LAMP); return; }
            c.set(y, Rng.chance(c.hash(y, 12), 0.015) ? B.SOUL_LANTERN_HANGING : B.REDSTONE_LAMP);
        }
    }

    public static String pick(long h, String... options) {
        return options[(int) Math.floorMod(h, (long) options.length)];
    }
}
