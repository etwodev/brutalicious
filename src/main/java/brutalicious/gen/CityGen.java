package brutalicious.gen;

import brutalicious.gen.building.Building;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import static brutalicious.gen.CityPlan.*;

public final class CityGen {
    private CityGen() {}

    public static final int GROUND = Building.GROUND;
    public static final int TUNNEL_FLOOR = Building.floorY(-3);
    public static final int CISTERN_FLOOR = -33, CISTERN_CEIL = -16;

    static final Palette BRIDGE = Palette.BETON;

    public static void fill(Column c) {
        long seed = c.seed;
        int cx = Math.floorDiv(c.x, CELL), cz = Math.floorDiv(c.z, CELL);
        int lx = Math.floorMod(c.x, CELL), lz = Math.floorMod(c.z, CELL);
        int wx = roadWidth(cx), wz = roadWidth(cz);
        boolean rx = lx < wx, rz = lz < wz;
        Plot plot = plot(seed, cx, cz);
        int[] sh = Terrain.spec(seed, plot, rx, rz, lx, lz, wx, wz, cx, cz);
        c.stairs = sh[3] != 0;

        rock(c);
        Footings.fill(c);
        if (plot.cistern) cistern(c, lx, lz);

        if (rx || rz) {
            Streets.street(c, rx, rz, lx, lz, wx, wz, cx, cz);
            tunnels(c, rx, rz, lx, lz, wx, wz, cx, cz);
            Shelters.street(c, rx, rz, lx, lz, wx, wz, cx, cz);
            Shelters.hatch(c, rx, rz, lx, lz, wx, wz, cx, cz);
            DeepWorks.fill(c, rx, rz, lx, lz, cx, cz);
            Streets.groundPipe(c, rx, rz, lx, lz, wx, wz, cx, cz);
            Streets.tankWreck(c, rx, rz, lx, lz, wx, wz, cx, cz);
            roadBridges(c, rx, rz, lx, lz, wx, wz, cx, cz);
            if (rx && rz && plot.pillar != null) plot.pillar.fill(c, lx, lz);
            Zones.walls(c, plot, rx, rz, lx, lz, wx, wz, cx, cz);
        } else {
            int ux = lx - wx, uz = lz - wz;
            paving(c, plot);
            Shelters.plot(c, plot, ux, uz);
            plot.building.fill(c, ux, uz);
            plotPassages(c, plot, ux, uz);
            plotBridges(c, plot, ux, uz);
            if (plot.quarantine) quarantine(c, plot, ux, uz);
        }
        if (Zones.wet(seed, c, plot, rx, rz, lx, lz, wx, wz, cx, cz)) Zones.flood(c);
        if (sh[0] != 0) c.shift(sh[0]);
        Zones.afterShift(c, sh, rx, rz, lx, lz, wx, wz, cx, cz);
        Terrain.dress(c, sh, Zones.wallSpec(seed, plot, rx, rz, lx, lz, wx, wz, cx, cz) != null);
        Blackout.fill(c, plot, rx, rz, lx, lz, wx, wz, cx, cz, sh[0]);
        if (rx || rz) Streets.viaducts(c, rx, rz, lx, lz, cx, cz, sh[0]);
        if (rx && rz) DeepWorks.headframe(c, lx, lz, cx, cz, sh[0]);
        if (rx || rz) Streets.powerLines(c, rx, rz, lx, lz, wx, wz, cx, cz, sh[0]);
        Tier.fill(c, plot, rx, rz, lx, lz, wx, wz, cx, cz, sh[0]);
        Roof.fill(c, plot, rx, rz, lx, lz, wx, wz, cx, cz, sh[0]);
        if (rx || rz) Skyways.fill(c, plot, rx, rz, lx, lz, wx, wz, cx, cz, sh[0]);
        snow(c);
    }

    static void quarantine(Column c, Plot p, int ux, int uz) {
        boolean edge = ux == 0 || uz == 0 || ux == p.px - 1 || uz == p.pz - 1;
        if (!edge) return;
        if (uz == p.pz - 1 && Math.abs(ux - p.px / 2) <= 1) return;
        Palette pal = Palette.STONE;
        for (int y = GROUND + 1; y <= GROUND + 6; y++) c.set(y, pal.wallAt(c.seed, c.x, y, c.z, 0.3));
        c.set(GROUND + 7, (ux == 0 || ux == p.px - 1) ? B.CHAIN_Z : B.CHAIN_X);
    }

    static void snow(Column c) {
        int y = Column.MAX_Y - 1;
        while (y > GROUND - 12 && c.isAir(y)) y--;
        BlockState top = c.get(y);
        if (top == B.WATER) return;
        if (!top.canOcclude() || top.getBlock() instanceof net.minecraft.world.level.block.SlabBlock
                || top.getBlock() instanceof net.minecraft.world.level.block.StairBlock) return;
        double n = Rng.fbm(c.seed ^ 0x5EE0L, c.x / 9.0, c.z / 9.0);
        if (n < 0.42) return;
        int layers = 1 + (n > 0.5 ? 1 : 0) + (n > 0.68 ? 1 : 0);
        c.set(y + 1, B.snow(layers));
    }

    static void rock(Column c) {
        c.set(-64, B.BEDROCK);
        for (int y = -63; y <= GROUND - 1; y++) {
            BlockState s;
            if (y <= -60 && Rng.chance(c.hash(y, 1), (-59 - y) / 5.0)) s = B.BEDROCK;
            else s = y < 0 ? B.DEEPSLATE : B.STONE;
            long h = Rng.hash(c.seed ^ 0x0BE5L, c.x >> 1, y >> 1, c.z >> 1);
            double u = Rng.unit(h);
            if (u < 0.03 && Rng.chance(c.hash(y, 2), 0.65)) {
                BlockState ore = ore(y, u);
                if (ore != null) s = ore;
            }
            c.set(y, s);
        }
    }

    static BlockState ore(int y, double u) {
        if (y >= 0) {
            if (u < 0.011) return B.COAL_ORE;
            if (u < 0.018) return B.IRON_ORE;
            if (u < 0.024 && y > 8) return B.COPPER_ORE;
            if (u < 0.026 && y < 32) return B.GOLD_ORE;
            if (u < 0.028 && y < 32) return B.LAPIS_ORE;
            return null;
        }
        if (u < 0.004) return B.DS_COAL_ORE;
        if (u < 0.010) return B.DS_IRON_ORE;
        if (u < 0.018) return B.DS_REDSTONE_ORE;
        if (u < 0.021) return B.DS_GOLD_ORE;
        if (u < 0.024) return B.DS_LAPIS_ORE;
        if (u < 0.028 && y < -20) return B.DS_DIAMOND_ORE;
        return null;
    }

    static void vault(Column c) {
        int cx = Math.floorDiv(c.x, CELL), cz = Math.floorDiv(c.z, CELL);
        long h = Rng.hash(c.seed ^ 0x7A017L, cx, cz);
        if (!Rng.chance(h, 0.35)) return;
        int vx = cx * CELL + 16 * Rng.range(h >>> 8, 1, 2) + 12, vz = cz * CELL + 16 * Rng.range(h >>> 16, 1, 2) + 12;
        int dx = c.x - vx, dz = c.z - vz;
        if (Math.abs(dx) > 2 || Math.abs(dz) > 2) return;
        int f = CISTERN_FLOOR - 6;
        c.set(f, B.DEEPSLATE_TILES);
        c.fill(f + 1, f + 4, B.AIR);
        if (dx == 0 && dz == 0) { c.fill(f + 5, CISTERN_FLOOR, B.AIR); return; }
        if (dx == -2 && dz == 0 && Footings.present(c.seed, cx, cz)) {
            for (int y = Footings.FLOOR + 1; y <= f; y++) c.set(y, B.ladder(Direction.EAST));
            return;
        }
        if (Math.abs(dx) == 2 && Math.abs(dz) == 2) Decor.container(c, f + 1, B.CHEST, Decor.LOOT_VAULT);
        else if (dx == 0 && dz == 2) Decor.spawner(c, f + 1, "minecraft:skeleton");
        else if (dx == 1 && dz == 0) c.set(CISTERN_FLOOR + 1, B.SCULK_SHRIEKER);
    }

    static void cistern(Column c, int lx, int lz) {
        int mx = Math.floorMod(c.x, 16), mz = Math.floorMod(c.z, 16);
        int d = Math.max(Math.abs(mx - 7), Math.abs(mz - 7));
        boolean border = lx < 2 || lz < 2;
        boolean arch = (lx < 2 && lz >= 28 && lz <= 35) || (lz < 2 && lx >= 28 && lx <= 35);
        double sculk = Rng.fbm(c.seed ^ 0x5C01L, c.x / 11.0, c.z / 11.0);
        double pool = Rng.fbm(c.seed ^ 0xF100L, c.x / 14.0, c.z / 14.0);

        c.set(CISTERN_FLOOR, sculk > 0.58 ? B.SCULK : (Rng.chance(c.hash(CISTERN_FLOOR, 3), 0.15) ? B.CRACKED_DEEPSLATE_TILES : B.DEEPSLATE_TILES));
        c.set(CISTERN_CEIL, B.DEEPSLATE_TILES);
        for (int y = CISTERN_FLOOR + 1; y < CISTERN_CEIL; y++) {
            BlockState s;
            if (border && !(arch && y <= -22)) s = B.DEEPSLATE_BRICKS;
            else if (d <= 1) s = B.POLISHED_DEEPSLATE;
            else if ((y == CISTERN_CEIL - 1 && d <= 3) || (y == CISTERN_CEIL - 2 && d <= 2)) s = B.POLISHED_DEEPSLATE;
            else s = B.AIR;
            c.set(y, s);
        }
        int y = CISTERN_FLOOR + 1;
        if (!c.isAir(y)) return;
        if (pool > 0.6) {
            c.set(y, B.WATER);
            if (pool > 0.7) { c.set(CISTERN_FLOOR, B.WATER); c.set(CISTERN_FLOOR - 1, B.DEEPSLATE_TILES); }
        } else {
            long h = c.hash(y, 4);
            double u = Rng.unit(h);
            if (sculk > 0.58 && u < 0.010) c.set(y, B.SCULK_SHRIEKER);
            else if (sculk > 0.58 && u < 0.022) c.set(y, B.SCULK_SENSOR);
            else if (sculk > 0.58 && u < 0.023) c.set(y, B.SCULK_CATALYST);
            else if (u < 0.0006) Decor.container(c, y, B.CHEST, Decor.LOOT_CISTERN);
            else if (u < 0.0026) c.set(y, B.rot16(B.SKULL, (int) (h >>> 20)));
            else if (u < 0.0034) Decor.spawner(c, y, Decor.pick(h >>> 30, "minecraft:zombie", "minecraft:skeleton", "minecraft:drowned", "minecraft:cave_spider"));
            else if (u < 0.0046) c.set(y, B.CANDLE);
        }
        int top = CISTERN_CEIL - 1;
        if (c.isAir(top)) {
            long h = c.hash(top, 5);
            if (Rng.chance(h, 0.0008)) c.set(top, B.SOUL_LANTERN_HANGING);
            else if (Rng.chance(h >>> 12, 0.012)) c.set(top, B.STALACTITE);
        }
        vault(c);
    }

    static void plainGround(Column c) {
        long h = c.hash(GROUND, 89);
        BlockState s = (Math.floorMod(c.x, 16) == 0 || Math.floorMod(c.z, 16) == 0) ? B.GRAY_CONCRETE : B.LIGHT_GRAY_CONCRETE;
        if (Rng.noise(c.seed ^ 0x9A1EL, c.x / 9.0, c.z / 9.0) > 0.82) s = B.ANDESITE;
        c.set(GROUND, s);
        if (Rng.chance(h, 0.0015)) c.set(GROUND + 1, B.COBBLE_SLAB);
    }

    static void paving(Column c, Plot plot) {
        if (CityPlan.plain(c.seed, c.x, c.z)) { plainGround(c); return; }
        long h = c.hash(GROUND, 11);
        double moss = Rng.fbm(c.seed ^ 0x3055L, c.x / 8.0, c.z / 8.0);
        BlockState s = (Math.floorMod(c.x, 8) == 0 || Math.floorMod(c.z, 8) == 0) ? B.GRAY_CONCRETE : B.LIGHT_GRAY_CONCRETE;
        if (moss > 0.86) s = B.GRAVEL;
        else if (Rng.chance(h, 0.03)) s = B.ANDESITE;
        c.set(GROUND, s);
        if (plot.building.ruined()) {
            double n = Rng.fbm(c.seed ^ 0x2B1EL, c.x / 4.0, c.z / 4.0);
            int hgt = (int) ((n - 0.5) * 9);
            for (int i = 0; i < hgt; i++) {
                long r = c.hash(GROUND + 1 + i, 12);
                c.set(GROUND + 1 + i, Rng.chance(r, 0.5) ? B.COBBLE : (Rng.chance(r >>> 8, 0.5) ? B.ANDESITE : B.GRAVEL));
            }
        }
    }

    enum Tunnel { SERVICE, SEWER, METRO, COLLAPSED }

    static Tunnel tunnelStyle(long seed, int cx, int cz, boolean xBand) {
        double u = Rng.unit(Rng.hash(seed ^ 0x7077L, cx, cz, xBand ? 1 : 2));
        if (u < 0.38) return Tunnel.SERVICE;
        if (u < 0.65) return Tunnel.SEWER;
        if (u < 0.88) return Tunnel.METRO;
        return Tunnel.COLLAPSED;
    }

    static void tunnels(Column c, boolean rx, boolean rz, int lx, int lz, int wx, int wz, int cx, int cz) {
        int tcx = wx / 2, tcz = wz / 2;
        int tX = rx ? lx - tcx : 99, tZ = rz ? lz - tcz : 99;
        boolean inX = Math.abs(tX) <= 2, inZ = Math.abs(tZ) <= 2;
        boolean linX = Math.abs(tX) == 3, linZ = Math.abs(tZ) == 3;
        int y0 = TUNNEL_FLOOR;
        if ((linX && !inZ) || (linZ && !inX)) c.fill(y0, y0 + 4, B.STONE_BRICKS);
        if (inX || inZ) {
            boolean junction = inX && inZ;
            boolean xBand = inX && !inZ;
            Tunnel style = junction ? Tunnel.SERVICE : tunnelStyle(c.seed, cx, cz, xBand);
            int t = junction ? 0 : (xBand ? tX : tZ);
            int along = xBand ? c.z : c.x;
            int k = xBand ? lz - wz : lx - wx, len = xBand ? CELL - wz : CELL - wx;
            boolean level = Terrain.segmentDelta(c.seed, cx, cz, xBand) == 0;
            carve(c, style, t, along, xBand, junction, k == 0 || k == len - 1, level);
        }
        if (rx && !rz && lx == tcx + 2 && lz == wz + 3 && Rng.chance(Rng.hash(c.seed ^ 0x3A40L, cx, cz), 0.6)) {
            for (int y = y0 + 1; y < GROUND; y++) c.set(y, B.ladder(Direction.WEST));
            c.set(GROUND, B.MANHOLE);
        }
        if (rx && !rz) {
            int pz = CELL - wz, lzc = wz + pz / 2;
            if (lz == lzc) {
                if (lx >= tcx + 3 && plot(c.seed, cx, cz).building.basementReach(Building.W) >= 0) passage(c);
                if (lx <= tcx - 3 && plot(c.seed, cx - 1, cz).building.basementReach(Building.E) >= 0) passage(c);
            }
        } else if (rz && !rx) {
            int px = CELL - wx, lxc = wx + px / 2;
            if (lx == lxc) {
                if (lz >= tcz + 3 && plot(c.seed, cx, cz).building.basementReach(Building.N) >= 0) passage(c);
                if (lz <= tcz - 3 && plot(c.seed, cx, cz - 1).building.basementReach(Building.S) >= 0) passage(c);
            }
        }
    }

    static void passage(Column c) {
        c.set(TUNNEL_FLOOR, B.SMOOTH_STONE);
        c.fill(TUNNEL_FLOOR + 1, TUNNEL_FLOOR + 3, B.AIR);
        c.set(TUNNEL_FLOOR + 4, B.STONE_BRICKS);
    }

    static void carve(Column c, Tunnel style, int t, int along, boolean xBand, boolean junction, boolean segmentEnd, boolean level) {
        int y0 = TUNNEL_FLOOR;
        c.set(y0, Math.abs(t) == 2 ? B.STONE_BRICKS : B.SMOOTH_STONE);
        c.fill(y0 + 1, y0 + 3, B.AIR);
        c.set(y0 + 4, Math.abs(t) == 2 ? B.STONE_BRICKS : B.SMOOTH_STONE);
        long h = c.hash(y0, 13);
        switch (style) {
            case SERVICE -> {
                if (Math.abs(t) == 2 && !junction) c.set(y0 + 3, xBand ? B.BASALT_Z : B.BASALT_X);
            }
            case SEWER -> {
                if (t == 0 && level) { c.set(y0, B.WATER); c.set(y0 - 1, B.STONE_BRICKS); }
            }
            case METRO -> {
                if (t == 0) c.set(y0 + 1, xBand ? B.RAIL_NS : B.RAIL_EW);
            }
            case COLLAPSED -> {
                double n = Rng.fbm(c.seed ^ 0xC0L, c.x / 3.0, c.z / 3.0);
                if (n > 0.45) c.set(y0 + 1, B.COBBLE);
                if (n > 0.6) c.set(y0 + 2, B.GRAVEL);
                if (n > 0.75) c.set(y0 + 3, B.COBBLE);
            }
        }
        int y = y0 + 1;
        if (!c.isAir(y)) return;
        double u = Rng.unit(h);
        if (t == 0 && Math.floorMod(along, 23) == 0 && u < 0.3) c.set(y0 + 3, B.SOUL_LANTERN_HANGING);
        else if (Math.abs(t) == 2) {
            if (u < 0.0045) Decor.container(c, y, B.BARREL, Decor.LOOT_TUNNEL);
            else if (u < 0.0050) Decor.spawner(c, y, Decor.pick(h >>> 30, "minecraft:zombie", "minecraft:cave_spider", "minecraft:skeleton"));
        }
    }

    record Bridge(int f, int rA, int dA, int rB, int dB) {}

    static Bridge bridgeX(long seed, int cx, int cz) {
        if (major(cx)) return null;
        if (Zones.wallX(seed, cx, cz) || Terrain.cellH(seed, cx - 1, cz) != Terrain.cellH(seed, cx, cz)) return null;
        long h = Rng.hash(seed ^ 0xB81DL, cx, cz, 1);
        if (!Rng.chance(h, 0.5)) return null;
        Building a = plot(seed, cx - 1, cz).building, b = plot(seed, cx, cz).building;
        return bridge(h, a, b, Building.E, Building.W);
    }

    static Bridge bridgeZ(long seed, int cx, int cz) {
        if (major(cz)) return null;
        if (Zones.wallZ(seed, cx, cz) || Terrain.cellH(seed, cx, cz - 1) != Terrain.cellH(seed, cx, cz)) return null;
        long h = Rng.hash(seed ^ 0xB81DL, cx, cz, 2);
        if (!Rng.chance(h, 0.5)) return null;
        Building a = plot(seed, cx, cz - 1).building, b = plot(seed, cx, cz).building;
        return bridge(h, a, b, Building.S, Building.N);
    }

    static Bridge bridge(long h, Building a, Building b, int sideA, int sideB) {
        int n = Math.min(a.floors(), b.floors());
        if (n < 5) return null;
        int f = Rng.range(h >>> 8, 2, Math.min(n - 2, 12));
        int rA = a.reach(sideA, f), rB = b.reach(sideB, f);
        if (rA < 0 || rB < 0 || rA > 14 || rB > 14) return null;
        return new Bridge(f, rA, a.facadeDepth(sideA, f), rB, b.facadeDepth(sideB, f));
    }

    static void span(Column c, int f, int t, int along) {
        int F = Building.floorY(f);
        boolean side = Math.abs(t) == 2;
        c.set(F, side ? BRIDGE.trim : BRIDGE.floor);
        for (int r = 1; r <= 3; r++) {
            if (side) c.set(F + r, r == 2 && Math.floorMod(along, 3) == 1 ? B.TINTED_GLASS : BRIDGE.wallAt(c.seed, c.x, F + r, c.z, 0.2));
            else c.set(F + r, B.AIR);
        }
        c.set(F + 4, BRIDGE.trim);
        if (!side && Math.floorMod(along, 4) == 2) c.set(F + 3, B.REDSTONE_LAMP);
    }

    static void doorway(Column c, int f) {
        int F = Building.floorY(f);
        c.fill(F + 1, F + 3, B.AIR);
    }

    static void roadBridges(Column c, boolean rx, boolean rz, int lx, int lz, int wx, int wz, int cx, int cz) {
        if (rx && rz) return;
        if (rx) {
            int lzc = wz + (CELL - wz) / 2, t = lz - lzc;
            if (Math.abs(t) > 2) return;
            Bridge b = bridgeX(c.seed, cx, cz);
            if (b != null) span(c, b.f, t, lx);
        } else {
            int lxc = wx + (CELL - wx) / 2, t = lx - lxc;
            if (Math.abs(t) > 2) return;
            Bridge b = bridgeZ(c.seed, cx, cz);
            if (b != null) span(c, b.f, t, lz);
        }
    }

    static void plotBridges(Column c, Plot p, int ux, int uz) {
        long seed = c.seed;
        int tz = uz - p.pz / 2, tx = ux - p.px / 2;
        if (Math.abs(tz) <= 2) {
            Bridge w = bridgeX(seed, p.cx, p.cz);
            if (w != null) {
                if (ux < w.rB) span(c, w.f, tz, ux);
                else if (ux <= w.rB + w.dB + 1 && Math.abs(tz) <= 1) doorway(c, w.f);
            }
            Bridge e = bridgeX(seed, p.cx + 1, p.cz);
            if (e != null) {
                int edge = p.px - 1 - e.rA;
                if (ux > edge) span(c, e.f, tz, ux);
                else if (ux >= edge - e.dA - 1 && Math.abs(tz) <= 1) doorway(c, e.f);
            }
        }
        if (Math.abs(tx) <= 2) {
            Bridge n = bridgeZ(seed, p.cx, p.cz);
            if (n != null) {
                if (uz < n.rB) span(c, n.f, tx, uz);
                else if (uz <= n.rB + n.dB + 1 && Math.abs(tx) <= 1) doorway(c, n.f);
            }
            Bridge s = bridgeZ(seed, p.cx, p.cz + 1);
            if (s != null) {
                int edge = p.pz - 1 - s.rA;
                if (uz > edge) span(c, s.f, tx, uz);
                else if (uz >= edge - s.dA - 1 && Math.abs(tx) <= 1) doorway(c, s.f);
            }
        }
    }

    static void plotPassages(Column c, Plot p, int ux, int uz) {
        Building b = p.building;
        if (uz == p.pz / 2) {
            int r = b.basementReach(Building.W);
            if (r >= 0 && ux <= r + 1) { if (ux < r) passage(c); else c.fill(TUNNEL_FLOOR + 1, TUNNEL_FLOOR + 3, B.AIR); }
            r = b.basementReach(Building.E);
            int edge = p.px - 1 - r;
            if (r >= 0 && ux >= edge - 1) { if (ux > edge) passage(c); else c.fill(TUNNEL_FLOOR + 1, TUNNEL_FLOOR + 3, B.AIR); }
        }
        if (ux == p.px / 2) {
            int r = b.basementReach(Building.N);
            if (r >= 0 && uz <= r + 1) { if (uz < r) passage(c); else c.fill(TUNNEL_FLOOR + 1, TUNNEL_FLOOR + 3, B.AIR); }
            r = b.basementReach(Building.S);
            int edge = p.pz - 1 - r;
            if (r >= 0 && uz >= edge - 1) { if (uz > edge) passage(c); else c.fill(TUNNEL_FLOOR + 1, TUNNEL_FLOOR + 3, B.AIR); }
        }
    }

    public static String describe(long seed, int x, int z) {
        Plot p = plotAt(seed, x, z);
        return p.type.name().toLowerCase() + " / " + p.building.name() + " (" + p.building.floors() + " floors)";
    }
}
