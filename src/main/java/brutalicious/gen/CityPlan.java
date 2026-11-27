package brutalicious.gen;

import brutalicious.gen.building.Archetypes;
import brutalicious.gen.building.Building;
import brutalicious.gen.building.Mass;

public final class CityPlan {
    private CityPlan() {}

    public static final int CELL = 64;
    public static final int SUPER = 8;

    public static boolean major(int i) { return Math.floorMod(i, 4) == 2; }

    public static int roadWidth(int i) { return major(i) ? 16 : 10; }

    public static double density(long seed, int x, int z) {
        return Math.min(1, Math.max(0, (Rng.fbm(seed ^ 0xDE45L, x / 700.0, z / 700.0) - 0.2) / 0.6));
    }

    public static double sparse(long seed, int x, int z) {
        return Rng.fbm(seed ^ 0x5BA5EL, x / 1300.0, z / 1300.0);
    }

    public static boolean plain(long seed, int x, int z) {
        return sparse(seed, x, z) > 0.73 && Zones.zoneAt(seed, x, z) == Zones.Zone.CITY && Math.abs(x) + Math.abs(z) > 200;
    }

    public static boolean monolith(long seed, int x, int z) {
        return Rng.fbm(seed ^ 0x3070L, x / 900.0, z / 900.0) > 0.62;
    }

    public static final class Plot {
        public final int cx, cz, ox, oz, px, pz;
        public final long hash;
        public final Building building;
        public final Archetypes.Type type;
        public final boolean cistern;
        public final Mass pillar;
        public final Mass watchtower;
        public final Zones.Zone zone;
        public final boolean quarantine;

        Plot(long seed, int cx, int cz) {
            this.cx = cx;
            this.cz = cz;
            int wx = roadWidth(cx), wz = roadWidth(cz);
            this.ox = cx * CELL + wx;
            this.oz = cz * CELL + wz;
            this.px = CELL - wx;
            this.pz = CELL - wz;
            this.hash = Rng.hash(seed ^ 0x9107L, cx, cz);
            int mx = ox + px / 2, mz = oz + pz / 2;
            this.zone = Zones.zone(seed, cx, cz);
            double dens = zone == Zones.Zone.DARK ? 0 : density(seed, mx, mz);
            boolean mono = monolith(seed, mx, mz);
            Archetypes.Type t;
            int ground = Terrain.cellH(seed, cx, cz);
            double empty = Math.max(0, Math.min(1, (sparse(seed, mx, mz) - 0.5) / 0.22)) * 0.85;
            if (plain(seed, mx, mz)) empty = 0.97;
            if (zone == Zones.Zone.ABANDONED) empty = Math.max(empty, 0.4);
            boolean vacant = (zone == Zones.Zone.CITY || zone == Zones.Zone.BOMBED || zone == Zones.Zone.ABANDONED)
                    && Rng.chance(Rng.hash(seed ^ 0x7AC4L, cx, cz), empty);
            int sx = Math.floorDiv(cx, SUPER), sz = Math.floorDiv(cz, SUPER);
            long sh = Rng.hash(seed ^ 0x3171L, sx, sz);
            if (cx == sx * SUPER + Rng.range(sh, 1, SUPER - 2) && cz == sz * SUPER + Rng.range(sh >>> 16, 1, SUPER - 2)) {
                t = Archetypes.Type.MINISTRY;
            } else if (Zones.centrepiece(seed, cx, cz)) {
                t = Archetypes.Type.CENTREPIECE;
            } else if (cx == 0 && cz == 0) {
                t = Archetypes.Type.PODIUM;
            } else if (vacant) {
                t = Archetypes.Type.VACANT;
            } else {
                t = Archetypes.pick(Rng.mix(hash), dens, mono, zone);
            }
            this.type = t;
            this.building = Archetypes.build(t, hash, px, pz, dens, mono, zone, ground);
            this.cistern = building.needsCistern() || Rng.chance(Rng.hash(seed ^ 0xC157L, cx, cz), 0.8);
            int lift = ground / Building.FLOOR_H;
            if (Tier.present(seed, mx, mz)) building.openDeckDoors(Tier.FLOOR - lift);
            boolean tower = Zones.towerAt(seed, cx, cz);
            this.watchtower = tower ? Archetypes.watchtower(hash ^ 0x7A7C4L, wx, wz, Zones.towerDoorFloors(seed, cx, cz)) : null;
            if (Roof.present(seed, mx, mz)) building.openRoofDoors(Roof.FLOOR - lift);
            if (!tower && Roof.megaAt(seed, cx, cz)) this.pillar = Archetypes.megaColumn(hash ^ 0x3E6AL, wx, Roof.FLOOR - lift, Tier.FLOOR - lift, Skyways.FLOOR_F - lift);
            else this.pillar = !tower && Tier.pillarAt(seed, cx, cz) ? Archetypes.tierPillar(hash ^ 0x9111A2L, wx, Tier.FLOOR - lift) : null;
            this.quarantine = (zone == Zones.Zone.CITY || zone == Zones.Zone.FLOODED || zone == Zones.Zone.ABANDONED)
                    && building.floors() > 0 && !(cx == 0 && cz == 0)
                    && Rng.chance(Rng.hash(seed ^ 0x9A7AL, cx, cz), zone == Zones.Zone.ABANDONED ? 0.25 : 0.05);
        }
    }

    private static final int CACHE = 512;
    private static final ThreadLocal<Object[]> PLOTS = ThreadLocal.withInitial(() -> new Object[CACHE]);
    private static final ThreadLocal<long[]> KEYS = ThreadLocal.withInitial(() -> new long[CACHE * 2]);

    public static Plot plot(long seed, int cx, int cz) {
        long key = ((long) cx << 32) ^ (cz & 0xFFFFFFFFL);
        int slot = (int) Math.floorMod(Rng.mix(key), (long) CACHE);
        Object[] plots = PLOTS.get();
        long[] keys = KEYS.get();
        Plot p = (Plot) plots[slot];
        if (p != null && keys[slot * 2] == key && keys[slot * 2 + 1] == seed) return p;
        p = new Plot(seed, cx, cz);
        plots[slot] = p;
        keys[slot * 2] = key;
        keys[slot * 2 + 1] = seed;
        return p;
    }

    public static Plot plotAt(long seed, int x, int z) {
        return plot(seed, Math.floorDiv(x, CELL), Math.floorDiv(z, CELL));
    }
}
