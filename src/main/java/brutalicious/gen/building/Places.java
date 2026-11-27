package brutalicious.gen.building;

import brutalicious.gen.B;
import brutalicious.gen.Column;
import brutalicious.gen.Decor;
import brutalicious.gen.Models;
import brutalicious.gen.Palette;
import brutalicious.gen.Rng;
import brutalicious.gen.Voxels;
import brutalicious.gen.Zones;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import static brutalicious.gen.building.Building.GROUND;

public final class Places implements Building {
    public enum Kind {
        SUB_PEN, MISSILE_SILO, RADAR_ARRAY, ROUNDHOUSE, OBSERVATORY, CONCERT_HALL, LIBRARY, FUNFAIR, COLOSSUS,
        SARCOPHAGUS, DECON, SEED_VAULT, WAR_ROOM, TANK_YARD, COMPUTER_HALL, WATERWORKS, CREMATORIUM, BELL_TOWER,
        MAUSOLEUM, AIR_PLANT, LABYRINTH, REACTOR, GASHOLDER
    }

    final Kind kind;
    final long seed;
    final int px, pz, mx, mz;
    final boolean abandoned;
    final Palette pal;

    public Places(Kind kind, long seed, int px, int pz, Zones.Zone zone) {
        this.kind = kind;
        this.seed = seed;
        this.px = px;
        this.pz = pz;
        this.mx = px / 2;
        this.mz = pz / 2;
        this.abandoned = zone == Zones.Zone.ABANDONED;
        this.pal = switch (kind) {
            case LIBRARY, CONCERT_HALL, OBSERVATORY -> Palette.BETON;
            case BELL_TOWER, CREMATORIUM -> Palette.STONE;
            default -> Palette.MONOLITH;
        };
    }

    @Override public String name() { return kind.name().toLowerCase(); }

    @Override
    public int floors() {
        return switch (kind) {
            case LIBRARY -> 5;
            case CONCERT_HALL, COMPUTER_HALL, SARCOPHAGUS -> 4;
            case BELL_TOWER, AIR_PLANT, GASHOLDER -> 10;
            default -> 1;
        };
    }

    static double dist(int dx, int dz) { return Math.sqrt(dx * dx + dz * dz); }

    BlockState wall(Column c, int y) { return pal.wallAt(c.seed, c.x, y, c.z, 0.25); }

    static BlockState hfacing(net.minecraft.world.level.block.Block b, Direction d) {
        return b.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, d);
    }

    @Override
    public boolean occupied(int ux, int uz, int y) {
        int dx = ux - mx, dz = uz - mz;
        boolean rect3 = ux >= 3 && ux <= px - 4 && uz >= 3 && uz <= pz - 4;
        return switch (kind) {
            case SUB_PEN -> rect3 && y <= PEN_ROOF + 6;
            case MISSILE_SILO -> dist(dx, dz) <= 8 && y <= GROUND + 32;
            case RADAR_ARRAY -> Math.abs(dz) <= 3 && y <= GROUND + 45;
            case ROUNDHOUSE -> { double d = dist(dx, uz - (pz - 6)); yield d >= 12 && d <= 23 && uz <= pz - 6 && y <= GROUND + 9; }
            case OBSERVATORY -> dist(dx, dz) <= 11.5 && y <= GROUND + 25;
            case CONCERT_HALL -> rect3 && y <= GROUND + 31;
            case LIBRARY -> ux >= 4 && ux <= px - 5 && uz >= 4 && uz <= pz - 5 && y <= Building.floorY(5) + 1;
            case FUNFAIR -> Math.abs(ux - (px - 10)) <= 6 && Math.abs(dz) <= 20 && y <= GROUND + 44;
            case COLOSSUS -> ux <= 17 && uz <= 17 && y <= GROUND + 17;
            case SARCOPHAGUS -> (ux >= 5 && ux <= px - 6 && uz >= 7 && y <= GROUND + 31) || (dist(ux - (px - 8), uz - 3) <= 4 && y <= GROUND + 71);
            case SEED_VAULT -> Math.abs(dx) <= 3 && uz >= 6 && uz <= 16 && y <= GROUND + 12;
            case WAR_ROOM -> Math.abs(dx) <= 7 && Math.abs(dz) <= 5 && y <= GROUND + 7;
            case TANK_YARD -> (Math.abs(uz - 12) <= 7 || Math.abs(uz - (pz - 13)) <= 7) && y <= GROUND + 8;
            case COMPUTER_HALL -> rect3 && y <= GROUND + 17;
            case WATERWORKS -> Math.abs(dx) <= 3 && Math.abs(dz) <= 3 && y <= GROUND + 5;
            case CREMATORIUM -> (ux >= 5 && ux <= px - 6 && uz >= 8 && uz <= pz - 7 && y <= GROUND + 11) || (dist(ux - (px - 9), uz - 5) <= 3 && y <= GROUND + 41);
            case BELL_TOWER -> Math.max(Math.abs(dx), Math.abs(dz)) <= 4 && y <= GROUND + 52;
            case MAUSOLEUM -> Math.max(Math.abs(dx), Math.abs(dz)) <= 15 && y <= GROUND + 16;
            case AIR_PLANT -> {
                int qx = ux < mx ? px / 4 + 1 : 3 * px / 4 - 1, qz = uz < mz ? pz / 4 + 1 : 3 * pz / 4 - 1;
                yield Math.max(Math.abs(ux - qx), Math.abs(uz - qz)) <= 4 ? y <= AIR_TOP + 1 : y <= GROUND + 10;
            }
            case LABYRINTH -> y <= GROUND + 8 && ux >= 2 && ux <= px - 3 && uz >= 2 && uz <= pz - 3;
            case REACTOR -> (dist(dx, dz) <= 13.5 && y <= GROUND + 32) || (dist(ux - (px - 5), uz - 4) <= 3 && y <= GROUND + 51);
            case GASHOLDER -> dist(dx, dz) <= 19.5 && y <= GROUND + 43;
            default -> false;
        };
    }

    @Override
    public void fill(Column c, int ux, int uz) {
        switch (kind) {
            case SUB_PEN -> subPen(c, ux, uz);
            case MISSILE_SILO -> silo(c, ux, uz);
            case RADAR_ARRAY -> radar(c, ux, uz);
            case ROUNDHOUSE -> roundhouse(c, ux, uz);
            case OBSERVATORY -> observatory(c, ux, uz);
            case CONCERT_HALL -> concert(c, ux, uz);
            case LIBRARY -> library(c, ux, uz);
            case FUNFAIR -> funfair(c, ux, uz);
            case COLOSSUS -> colossus(c, ux, uz);
            case SARCOPHAGUS -> sarcophagus(c, ux, uz);
            case DECON -> decon(c, ux, uz);
            case SEED_VAULT -> seedVault(c, ux, uz);
            case WAR_ROOM -> warRoom(c, ux, uz);
            case TANK_YARD -> tankYard(c, ux, uz);
            case COMPUTER_HALL -> computer(c, ux, uz);
            case WATERWORKS -> waterworks(c, ux, uz);
            case CREMATORIUM -> crematorium(c, ux, uz);
            case BELL_TOWER -> bellTower(c, ux, uz);
            case MAUSOLEUM -> mausoleum(c, ux, uz);
            case AIR_PLANT -> airPlant(c, ux, uz);
            case LABYRINTH -> labyrinth(c, ux, uz);
            case REACTOR -> reactor(c, ux, uz);
            case GASHOLDER -> gasholder(c, ux, uz);
        }
    }

    static final int PEN_QUAY = GROUND + 13, PEN_ROOF = GROUND + 22;

    void subPen(Column c, int ux, int uz) {
        int x0 = 3, z0 = 3, x1 = px - 4, z1 = pz - 4;
        if (ux == x0 - 1 && Math.abs(uz - mz) == 0) { for (int y = GROUND + 1; y <= PEN_QUAY + 1; y++) c.set(y, B.ladder(Direction.WEST)); return; }
        if (ux < x0 || ux > x1 || uz < z0 || uz > z1) return;
        int bw = (x1 - x0 + 1) / 3;
        int b = Math.min(2, (ux - x0) / bw), bx = x0 + b * bw + bw / 2;
        int lat = Math.abs(ux - bx);
        boolean channel = lat <= bw / 2 - 3 && uz > z0 + 3;
        int e = Math.min(Math.min(ux - x0, x1 - ux), Math.min(uz - z0, z1 - uz));
        for (int y = PEN_ROOF; y <= PEN_ROOF + 4; y++) c.set(y, wall(c, y));
        if (Math.floorMod(ux, 4) == 0) c.fill(PEN_ROOF + 5, PEN_ROOF + 6, B.GRAY_CONCRETE);
        if (e == 0) {
            boolean mouth = uz == z1 && channel;
            for (int y = GROUND - 7; y < PEN_ROOF; y++) {
                if (mouth && y <= GROUND + 17) { c.set(y, B.AIR); continue; }
                boolean door = ux == x0 && uz == mz && (y == PEN_QUAY + 1 || y == PEN_QUAY + 2);
                c.set(y, door ? B.AIR : wall(c, y));
            }
            return;
        }
        if (channel) {
            c.set(GROUND - 7, B.POLISHED_DEEPSLATE);
            for (int y = GROUND - 6; y < PEN_ROOF; y++) c.set(y, B.AIR);
            int boat = (int) Math.floorMod(Rng.hash(seed, 500 + b), 4L);
            int sz = (z0 + 4 + z1) / 2;
            Voxels sub = boat <= 1 ? Models.SUBMARINE : boat == 2 ? Models.SUBMARINE_SUNK : null;
            int base = boat <= 1 ? Zones.FLOOD : GROUND - 3;
            if (sub != null && sub.covers(ux - bx, uz - sz)) sub.apply(c, ux - bx, uz - sz, base);
            return;
        }
        for (int y = GROUND - 7; y <= PEN_QUAY; y++) c.set(y, y == PEN_QUAY ? pal.floor : pal.wall);
        for (int y = PEN_QUAY + 1; y < PEN_ROOF; y++) c.set(y, B.AIR);
        if ((ux - x0) % bw == 0 && ux != x0 && Math.abs(uz - mz) > 2) for (int y = PEN_QUAY + 1; y < PEN_ROOF; y++) c.set(y, wall(c, y));
        if (lat == bw / 2 - 2 && Math.floorMod(uz, 6) == 0) c.set(PEN_QUAY + 1, B.POLISHED_BLACKSTONE);
        else if (Rng.chance(c.hash(PEN_QUAY, 200), 0.004)) Decor.container(c, PEN_QUAY + 1, B.BARREL, Decor.LOOT_MILITARY);
        if (lat == bw / 2 - 2) c.set(PEN_ROOF - 1, B.POLISHED_DEEPSLATE);
    }

    static final int SILO_F = 48;

    void silo(Column c, int ux, int uz) {
        int dx = ux - mx, dz = uz - mz;
        double d = dist(dx, dz);
        if (d <= 13) c.set(GROUND, Math.floorMod(ux + uz, 7) == 0 ? B.GRAY_CONCRETE : B.LIGHT_GRAY_CONCRETE);
        for (int s : new int[]{-1, 1}) {
            int ex = dx - s * 15;
            if (dist(ex, dz) <= 7.5 && s * ex >= 0) c.fill(GROUND + 1, GROUND + 2, B.POLISHED_DEEPSLATE);
            if (Math.abs(dz) == 4 && s * dx >= 6 && s * dx <= 22) c.set(GROUND, B.POLISHED_BLACKSTONE);
        }
        if (d <= 5.5) {
            c.set(SILO_F, B.POLISHED_DEEPSLATE);
            c.fill(SILO_F + 1, GROUND, B.AIR);
            boolean ladder = dx == 0 && dz == -5;
            if (!ladder && d >= 4) for (int y = SILO_F + 6; y < GROUND; y += 6) c.set(y, B.SMOOTH_STONE_SLAB_TOP);
            if (ladder) for (int y = SILO_F + 1; y <= GROUND; y++) c.set(y, B.ladder(Direction.SOUTH));
            if (Models.MISSILE.covers(dx, dz)) Models.MISSILE.apply(c, dx, dz, SILO_F + 4);
            return;
        }
        if (d <= 7.5) {
            c.fill(SILO_F - 1, GROUND - 1, B.GRAY_CONCRETE);
            c.fill(GROUND, GROUND + 1, B.POLISHED_ANDESITE);
            if (dx > 0 && Math.abs(dz) <= 1) c.fill(SILO_F + 1, SILO_F + 3, B.AIR);
            return;
        }
        if (Math.abs(dz) <= 1 && dx >= 7 && dx <= 11) {
            c.set(SILO_F, B.POLISHED_DEEPSLATE);
            c.fill(SILO_F + 1, SILO_F + 3, dx == 9 && dz != 0 ? B.IRON_BARS : B.AIR);
            c.set(SILO_F + 4, B.GRAY_CONCRETE);
            return;
        }
        if (dx >= 11 && dx <= 19 && Math.abs(dz) <= 4) {
            boolean shell = dx == 11 || dx == 19 || Math.abs(dz) == 4;
            c.set(SILO_F, B.POLISHED_DEEPSLATE);
            for (int y = SILO_F + 1; y <= SILO_F + 4; y++)
                c.set(y, shell && !(dx == 11 && Math.abs(dz) <= 1 && y <= SILO_F + 3) ? B.GRAY_CONCRETE : B.AIR);
            c.set(SILO_F + 5, B.GRAY_CONCRETE);
            if (!shell) {
                if (dx >= 16) Decor.console(c, SILO_F + 1, dz + 4, dx - 16, Rng.hash(seed, 0x5110));
                else if (dx == 12 && dz == 3) Decor.container(c, SILO_F + 1, B.CHEST, Decor.LOOT_MILITARY);
            }
            if (dx == 18 && dz == -3) {
                for (int y = SILO_F + 1; y < GROUND; y++) c.set(y, B.ladder(Direction.SOUTH));
                c.set(GROUND, B.IRON_TRAPDOOR);
            }
            return;
        }
        if (d >= 13 && d < 14 && !(dz > 0 && Math.abs(dx) <= 2)) { c.setConnected(GROUND + 1, B.IRON_BARS); c.setConnected(GROUND + 2, B.IRON_BARS); }
    }

    void radar(Column c, int ux, int uz) {
        int H = GROUND + 44, a = uz - mz;
        if (ux < 2 || ux > px - 3) return;
        boolean mast = Math.floorMod(ux, 6) == 2;
        if (a == -2 || a == 2 || a == 0) {
            c.set(GROUND, B.POLISHED_ANDESITE);
            for (int y = GROUND + 1; y <= H; y++) {
                if (a == 0) { if (mast && Math.floorMod(y, 10) == 5) c.setConnected(y, B.IRON_BARS); continue; }
                if (mast) c.set(y, Math.floorMod(y, 8) == 0 ? B.POLISHED_DEEPSLATE : B.POLISHED_ANDESITE);
                else if (Math.floorMod(y - GROUND, 5) == 0) c.setConnected(y, B.IRON_BARS);
            }
            return;
        }
        if (a == -3 && Math.floorMod(ux, 3) == 1) {
            for (int y = GROUND + 7; y <= H; y += 5) c.set(y, B.facing(Blocks.END_ROD, Direction.NORTH));
            return;
        }
        if (a >= 6 && a <= 13 && ux >= 6 && ux <= 21) {
            boolean shell = a == 6 || a == 13 || ux == 6 || ux == 21;
            for (int y = GROUND + 1; y <= GROUND + 4; y++) {
                BlockState s = shell ? (y == GROUND + 3 && a == 6 && Math.floorMod(ux, 3) == 0 ? B.TINTED_GLASS : B.GRAY_CONCRETE) : B.AIR;
                if (a == 6 && (ux == 13 || ux == 14) && y <= GROUND + 2) s = B.AIR;
                c.set(y, s);
            }
            c.set(GROUND + 5, B.POLISHED_DEEPSLATE);
            if (!shell && a >= 10) Decor.console(c, GROUND + 1, ux - 7, a - 10, Rng.hash(seed, 0x4AD));
            if (ux == 20 && a == 7 && Rng.chance(Rng.hash(seed, 0x4AE), 0.5)) Decor.container(c, GROUND + 1, B.CHEST, Decor.LOOT_MILITARY);
        }
    }

    void warRoom(Column c, int ux, int uz) {
        int dx = ux - mx, dz = uz - mz;
        int hf = GROUND - 14, hc = GROUND - 5;
        if (Math.abs(dx) <= 13 && Math.abs(dz) <= 9) {
            boolean shell = Math.abs(dx) == 13 || Math.abs(dz) == 9;
            c.set(hf, shell ? B.GRAY_CONCRETE : B.POLISHED_DEEPSLATE);
            for (int y = hf + 1; y < hc; y++) {
                boolean screen = dz == -9 && Math.abs(dx) <= 8 && y >= hf + 4 && y <= hf + 7;
                c.set(y, screen ? B.BLACK_GLASS : shell ? B.GRAY_CONCRETE : B.AIR);
            }
            c.set(hc, B.GRAY_CONCRETE);
            if (!shell) {
                boolean gallery = Math.abs(dx) >= 10 || dz <= -6;
                if (gallery) {
                    c.fill(hf + 1, hf + 3, B.GRAY_CONCRETE);
                    if (Math.abs(dx) == 10 || dz == -6) c.setConnected(hf + 4, B.IRON_BARS);
                    else Decor.console(c, hf + 4, dx + 13, dz + 9, Rng.hash(seed, 0x3A2));
                } else if (Math.abs(dx) <= 6 && Math.abs(dz) <= 3) {
                    c.set(hf + 1, B.POLISHED_ANDESITE);
                    if (Math.abs(dx) == 6 || Math.abs(dz) == 3) c.set(hf + 2, B.SMOOTH_STONE_SLAB);
                    else {
                        double n = Rng.fbm(seed ^ 0x3A9L, ux / 3.0, uz / 3.0);
                        c.set(hf + 2, Math.abs(n - 0.5) < 0.03 ? B.LIGHT_GRAY_CARPET : n > 0.5 ? B.GRAY_CARPET : B.BLACK_CARPET);
                    }
                } else if (Rng.chance(c.hash(hf, 230), 0.004)) Decor.container(c, hf + 1, B.CHEST, Decor.LOOT_MILITARY);
            }
        }
        if (Math.abs(dx) <= 7 && Math.abs(dz) <= 5) {
            boolean shell = Math.abs(dx) == 7 || Math.abs(dz) == 5;
            for (int y = GROUND + 1; y <= GROUND + 5; y++) {
                BlockState s = shell ? (y == GROUND + 3 && Math.abs(dz) == 5 && Math.floorMod(ux, 3) == 0 ? B.TINTED_GLASS : Palette.MONOLITH.wallAt(c.seed, c.x, y, c.z, 0.1)) : B.AIR;
                if (dz == 5 && Math.abs(dx) <= 1 && y <= GROUND + 2) s = B.AIR;
                c.set(y, s);
            }
            c.fill(GROUND + 6, GROUND + 7, B.GRAY_CONCRETE);
            if (dx == -5 && dz == -4) for (int y = hf + 1; y <= GROUND; y++) c.set(y, B.ladder(Direction.SOUTH));
        }
    }

    void tankYard(Column c, int ux, int uz) {
        c.set(GROUND, Math.floorMod(ux, 9) == 0 || Math.floorMod(uz, 9) == 0 ? B.GRAY_CONCRETE : B.LIGHT_GRAY_CONCRETE);
        int[] rows = {12, pz - 13};
        for (int i = 0; i < 2; i++) {
            int row = rows[i], a = Math.abs(uz - row);
            if (a <= 7 && ux >= 3 && ux <= px - 4) {
                c.set(GROUND + 8, B.SMOOTH_STONE_SLAB);
                if (a == 7 && Math.floorMod(ux - 3, 8) == 0) c.fill(GROUND + 1, GROUND + 7, B.POLISHED_ANDESITE);
                else c.set(GROUND + 7, a == 7 ? B.GRAY_CONCRETE : B.AIR);
            }
            int[] xs = {10, mx, px - 11};
            for (int k = 0; k < 3; k++) {
                long h = Rng.hash(seed, 600 + i * 3 + k);
                if (Rng.chance(h, 0.15)) continue;
                Voxels t = Models.tank(i == 0 ? 0 : 2, Rng.chance(h >>> 8, 0.3));
                if (t.covers(ux - xs[k], uz - row)) t.apply(c, ux - xs[k], uz - row, GROUND + 1);
            }
        }
        if ((uz == 2 || uz == pz - 3) && Math.floorMod(ux, 2) == 0 && ux > 2 && ux < px - 3) {
            long h = c.hash(GROUND, 231);
            if (Rng.chance(h, 0.5)) {
                if (Rng.chance(h >>> 7, 0.05)) Decor.container(c, GROUND + 1, B.BARREL, Decor.LOOT_MILITARY);
                else c.set(GROUND + 1, B.BARREL);
            }
        }
    }

    void roundhouse(Column c, int ux, int uz) {
        int cz0 = pz - 6;
        int dx = ux - mx, dz = uz - cz0;
        double d = dist(dx, dz);
        if (d <= 24) c.set(GROUND, B.GRAVEL);
        if (d <= 9.5) {
            c.set(GROUND - 1, B.SMOOTH_STONE);
            c.set(GROUND, d > 8.7 ? B.POLISHED_ANDESITE : B.AIR);
            if (Math.abs(dz) <= 1 && d <= 8.7) { c.set(GROUND, B.POLISHED_DEEPSLATE); if (dz == 0) c.set(GROUND + 1, B.RAIL_EW); }
            if (d < 1) c.set(GROUND, B.POLISHED_BLACKSTONE);
            return;
        }
        if (dz > 0) return;
        double a = Math.atan2(-dz, dx);
        int stalls = 9;
        double w = Math.PI / stalls;
        int s = Math.min(stalls - 1, (int) (a / w));
        double lat = d * Math.sin(a - (s + 0.5) * w);
        double fs = a / w - Math.floor(a / w);
        double dd = d * Math.sin(Math.min(fs, 1 - fs) * w);
        if (d < 12) { if (Math.abs(lat) < 0.6) c.set(GROUND, B.POLISHED_ANDESITE); return; }
        if (d > 23) return;
        int roof = GROUND + 9;
        c.set(GROUND, Math.abs(lat) < 0.6 ? B.POLISHED_ANDESITE : B.SMOOTH_STONE);
        c.set(roof, d > 17 && d < 18 ? B.TINTED_GLASS : B.GRAY_CONCRETE);
        if (d > 22) {
            for (int y = GROUND + 1; y < roof; y++) c.set(y, y >= GROUND + 4 && y <= GROUND + 6 && Math.abs(lat) < 2 ? B.GRAY_GLASS : wall(c, y));
            return;
        }
        if ((dd < 0.6 && d > 13.5) || (d < 12.8 && dd < 1.0)) { for (int y = GROUND + 1; y < roof; y++) c.set(y, wall(c, y)); return; }
        for (int y = GROUND + 1; y < roof; y++) c.set(y, B.AIR);
        if (d < 12.8) { c.set(roof - 1, B.GRAY_CONCRETE); return; }
        if (Rng.chance(Rng.hash(seed, 300 + s), 0.6) && Math.abs(lat) <= 1.5 && d >= 14 && d <= 21) {
            boolean cab = d >= 18.5;
            c.set(GROUND + 1, B.POLISHED_BLACKSTONE);
            c.set(GROUND + 2, B.GRAY_CONCRETE);
            c.set(GROUND + 3, B.BLACK_CONCRETE);
            c.set(GROUND + 4, cab ? (Math.abs(lat) > 0.8 ? B.TINTED_GLASS : B.GRAY_CONCRETE) : (Math.abs(lat) > 1.0 ? B.AIR : B.GRAY_CONCRETE));
            if (cab) c.set(GROUND + 5, B.SMOOTH_STONE_SLAB);
            if (d < 15 && Math.abs(lat) < 0.6) c.fill(GROUND + 5, GROUND + 6, B.POLISHED_BLACKSTONE);
        } else if (Rng.chance(c.hash(GROUND, 210), 0.004)) Decor.container(c, GROUND + 1, B.BARREL, Decor.LOOT_INDUSTRIAL);
    }

    void gasholder(Column c, int ux, int uz) {
        int dx = ux - mx, dz = uz - mz;
        double d = dist(dx, dz), a = Math.atan2(dz, dx);
        int top = GROUND + 42;
        double R = 18, Bell = 14;
        if (d <= R + 1) c.set(GROUND, B.POLISHED_ANDESITE);
        double step = Math.PI / 6;
        double k = Math.round(a / step) * step;
        if (dist((int) Math.round(dx - R * Math.cos(k)), (int) Math.round(dz - R * Math.sin(k))) <= 0.8) {
            for (int y = GROUND + 1; y <= top; y++) c.set(y, Math.floorMod(y, 7) == 0 ? B.POLISHED_DEEPSLATE : B.POLISHED_ANDESITE);
            return;
        }
        if (Math.abs(d - R) < 0.7) for (int y : new int[]{GROUND + 14, GROUND + 28, top}) c.setConnected(y, B.IRON_BARS);
        boolean crushed = a > 0.3 && a < 1.4;
        if (Math.abs(d - 15.5) < 0.6) { c.fill(GROUND - 4, GROUND + 1, B.GRAY_CONCRETE); return; }
        if (d < 15) {
            c.set(GROUND - 4, B.MUD);
            for (int y = GROUND - 3; y <= GROUND; y++) c.set(y, B.AIR);
        }
        if (Math.abs(d - Bell) < 0.6) {
            int bt = crushed ? GROUND + 2 + (int) Math.floorMod(c.hash(GROUND, 260), 4L) : GROUND + 16;
            for (int y = GROUND - 3; y <= bt; y++) c.set(y, Math.floorMod(y, 4) == 0 ? B.POLISHED_DEEPSLATE : B.GRAY_CONCRETE);
        } else if (d < Bell - 0.6) {
            if (crushed) { if (Rng.chance(c.hash(GROUND, 261), 0.3)) c.set(GROUND - 3, B.GRAY_CONCRETE); }
            else c.set(GROUND + 16 + (int) ((Bell - d) / 3), B.GRAY_CONCRETE);
        }
    }

    void waterworks(Column c, int ux, int uz) {
        int qx = ux < mx ? px / 4 : 3 * px / 4, qz = uz < mz ? pz / 4 : 3 * pz / 4;
        int dx = ux - qx, dz = uz - qz;
        double d = dist(dx, dz);
        int r = Math.min(px, pz) / 4 - 2;
        c.set(GROUND, Math.abs(ux - mx) <= 1 || Math.abs(uz - mz) <= 1 ? B.POLISHED_ANDESITE : B.GRAVEL);
        int pdx = ux - mx, pdz = uz - mz;
        if (Math.abs(pdx) <= 3 && Math.abs(pdz) <= 3) {
            boolean shell = Math.abs(pdx) == 3 || Math.abs(pdz) == 3;
            for (int y = GROUND + 1; y <= GROUND + 4; y++) c.set(y, shell && !(pdz == 3 && pdx == 0 && y <= GROUND + 2) ? B.GRAY_CONCRETE : B.AIR);
            c.set(GROUND + 5, B.SMOOTH_STONE_SLAB);
            if (!shell && pdz <= -1) Decor.console(c, GROUND + 1, pdx + 3, pdz + 3, Rng.hash(seed, 0x3A7));
            return;
        }
        if (d > r + 0.5) return;
        if (d > r - 0.5) { c.fill(GROUND, GROUND + 2, B.GRAY_CONCRETE); c.set(GROUND + 3, B.SMOOTH_STONE_SLAB); return; }
        int bottom = GROUND - 1 - (int) ((r - d) / 2.0);
        double n = Rng.fbm(seed ^ 0x3A8L, ux / 4.0, uz / 4.0);
        c.set(bottom, d < 1.5 ? B.GRAY_CONCRETE : n > 0.5 ? B.MUD : B.GRAY_CONCRETE);
        for (int y = bottom + 1; y <= GROUND; y++) c.set(y, B.AIR);
        if (d < 1.5) c.fill(bottom, GROUND + 3, B.POLISHED_ANDESITE);
        if (Math.abs(dz) <= 1 && dx >= 0) {
            c.set(GROUND + 3, B.SMOOTH_STONE_SLAB_TOP);
            if (Math.abs(dz) == 1) c.setConnected(GROUND + 4, B.IRON_BARS);
            if (dz == 0 && dx >= 2 && c.isAir(bottom + 1)) c.setConnected(bottom + 1, B.IRON_BARS);
        }
    }

    void reactor(Column c, int ux, int uz) {
        int dx = ux - mx, dz = uz - mz;
        double d = dist(dx, dz);
        int R = 13, wallTop = GROUND + 22;
        double sd = dist(ux - (px - 5), uz - 4);
        if (sd <= 2.6) {
            for (int y = GROUND + 1; y <= GROUND + 50; y++) c.set(y, sd > 1.5 ? (Math.floorMod(y, 10) == 0 ? B.POLISHED_DEEPSLATE : B.GRAY_CONCRETE) : B.AIR);
            return;
        }
        if (d > R + 0.5) {
            if (d < R + 3) c.set(GROUND, B.POLISHED_ANDESITE);
            if (dx == -R - 1 && dz == 0) for (int y = GROUND + 1; y <= GROUND + 8; y++) c.set(y, B.ladder(Direction.WEST));
            return;
        }
        boolean airlock = dx <= -R + 1 && Math.abs(dz) <= 1;
        for (int y = GROUND + 1; y <= wallTop + 10; y++) {
            boolean shell;
            if (y <= wallTop) shell = d > R - 0.8;
            else { double r3 = Math.sqrt(d * d + Math.pow((y - wallTop) / 0.7, 2)); shell = r3 > R - 0.9 && r3 <= R + 0.3; if (r3 > R + 0.3) continue; }
            if (shell) { c.set(y, airlock && y >= GROUND + 7 && y <= GROUND + 9 ? B.AIR : (Math.floorMod(y, 6) == 0 ? B.GRAY_CONCRETE : B.LIGHT_GRAY_CONCRETE)); continue; }
            if (y <= GROUND + 5) c.set(y, B.GRAY_CONCRETE);
            else if (y == GROUND + 6) c.set(y, d <= 6 ? ((Math.floorMod(ux, 2) + Math.floorMod(uz, 2)) % 2 == 0 ? B.POLISHED_BLACKSTONE : B.GRAY_CONCRETE) : pal.floor);
            else c.set(y, B.AIR);
        }
        if (d >= R - 2.5 && d < R - 0.8) c.set(GROUND + 20, B.SMOOTH_STONE_SLAB_TOP);
        if (Math.abs(dz) <= 1 && d < R - 0.8) c.set(GROUND + 21, B.POLISHED_DEEPSLATE);
        if (dx == 0 && dz == 0) for (int y = GROUND + 12; y <= GROUND + 20; y++) c.set(y, B.CHAIN);
        if (d >= R - 3 && d < R - 1.5 && dz > 5) Decor.console(c, GROUND + 7, ux, uz, Rng.hash(seed, 0x4EA));
    }

    void observatory(Column c, int ux, int uz) {
        int dx = ux - mx, dz = uz - mz;
        double d = dist(dx, dz);
        int R = 11, base = GROUND + 12, P = base + 5;
        if (d > R + 4) return;
        if (d > R + 0.5) {
            c.set(GROUND, B.POLISHED_ANDESITE);
            if (d > R + 3.2 && !(dz > 0 && Math.abs(dx) <= 1)) c.setConnected(GROUND + 1, B.IRON_BARS);
            return;
        }
        boolean wallCol = d > R - 0.5;
        boolean door = dz > 0 && Math.abs(dx) <= 1;
        boolean ladder = dx == -R + 1 && dz == 0;
        boolean pier = Math.abs(dx) <= 1 && Math.abs(dz) <= 1;
        c.set(GROUND, pal.floor);
        for (int y = GROUND + 1; y <= base; y++) {
            if (wallCol) c.set(y, door && y <= GROUND + 3 ? B.AIR : (y == GROUND + 9 || y == GROUND + 10) ? B.TINTED_GLASS : wall(c, y));
            else if (ladder) c.set(y, B.ladder(Direction.EAST));
            else if (pier) c.set(y, B.POLISHED_ANDESITE);
            else c.set(y, (y == GROUND + 6 && d > 4) || y == base ? pal.floor : B.AIR);
        }
        if (pier) c.fill(base + 1, P - 1, B.POLISHED_ANDESITE);
        for (int y = base + 1; y <= base + R + 1; y++) {
            double r3 = Math.sqrt(d * d + (y - base) * (y - base));
            boolean slit = Math.abs(dz) <= 1 && dx >= -1;
            if (r3 > R - 0.8 && r3 <= R + 0.3 && !slit) c.set(y, B.LIGHT_GRAY_CONCRETE);
        }
        if (Math.abs(dz) <= 1 && dx >= -5 && dx <= 7) {
            int yc = P + (int) Math.round(dx * 0.8);
            for (int y = yc - 1; y <= yc + 1; y++) c.set(y, dx == 7 || dx == -5 ? B.BLACK_CONCRETE : B.GRAY_CONCRETE);
        }
    }

    void concert(Column c, int ux, int uz) {
        int x0 = 3, z0 = 3, x1 = px - 4, z1 = pz - 4;
        if (ux < x0 || ux > x1 || uz < z0 || uz > z1) return;
        int stage0 = z1 - 11, top = GROUND + 20, fly = GROUND + 30;
        int roof = uz >= stage0 - 1 ? fly : top;
        int e = Math.min(Math.min(ux - x0, x1 - ux), Math.min(uz - z0, z1 - uz));
        c.set(GROUND, pal.floor);
        c.set(roof, B.GRAY_CONCRETE);
        int rake0 = z0 + 1, rake1 = stage0 - 3;
        if (e == 0) {
            for (int y = GROUND + 1; y < roof; y++) {
                boolean side = ux == x0 || ux == x1;
                boolean door = side && uz >= rake1 - 2 && uz <= rake1 && y <= GROUND + 3;
                boolean slit = side && Math.floorMod(uz, 6) == 0 && y >= GROUND + 8 && y <= GROUND + 14;
                c.set(y, door ? B.AIR : slit ? B.TINTED_GLASS : Math.floorMod(ux + uz, 4) == 0 ? pal.trim : wall(c, y));
            }
            c.set(roof + 1, pal.parapet);
            return;
        }
        for (int y = GROUND + 1; y < roof; y++) c.set(y, B.AIR);
        if (Math.floorMod(ux, 4) == 0 && uz < stage0 - 1) c.set(top - 1, B.SMOOTH_STONE_SLAB_TOP);
        if (uz >= rake0 && uz <= rake1) {
            int sy = GROUND + (rake1 - uz) / 2;
            for (int y = GROUND + 1; y <= sy; y++) c.set(y, y == sy ? pal.floor : pal.wall);
            boolean aisle = Math.abs(ux - mx) <= 1 || e == 1;
            boolean row = (rake1 - uz) % 2 == 0;
            if (!aisle && row) c.set(sy + 1, B.stair(Blocks.POLISHED_ANDESITE_STAIRS, Direction.NORTH));
            if (aisle && !row && sy > GROUND) c.set(sy, B.stair(Blocks.POLISHED_ANDESITE_STAIRS, Direction.NORTH));
            return;
        }
        if (uz == stage0 - 1) {
            boolean open = Math.abs(ux - mx) <= (x1 - x0) / 2 - 4;
            for (int y = GROUND + 1; y < fly; y++) if (!open || y > GROUND + 14) c.set(y, wall(c, y));
            return;
        }
        if (uz >= stage0) {
            c.fill(GROUND + 1, GROUND + 2, pal.floor);
            if (uz == z1 - 1 && Math.floorMod(ux, 2) == 0) {
                int h = 6 + (int) Math.floorMod(Rng.hash(seed, ux), 5L) + Math.max(0, 8 - Math.abs(ux - mx) / 2);
                for (int y = GROUND + 3; y <= GROUND + 3 + h; y++) c.set(y, B.ANDESITE_WALL);
            }
            if (uz == stage0 + 3 && Math.floorMod(ux, 3) == 0) for (int y = GROUND + 12; y < fly; y++) c.set(y, B.CHAIN);
            if (Math.floorMod(ux + uz, 3) == 0) c.setConnected(fly - 2, B.IRON_BARS);
        }
    }

    void library(Column c, int ux, int uz) {
        int x0 = 4, z0 = 4, x1 = px - 5, z1 = pz - 5;
        if (ux < x0 || ux > x1 || uz < z0 || uz > z1) return;
        int floors = 5;
        int dx = ux - mx, dz = uz - mz;
        int e = Math.min(Math.min(ux - x0, x1 - ux), Math.min(uz - z0, z1 - uz));
        boolean atrium = Math.abs(dx) <= 7 && Math.abs(dz) <= 7;
        boolean ladderA = ux == x0 + 1 && uz == z0 + 1, ladderB = ux == x1 - 1 && uz == z1 - 1;
        for (int f = 0; f <= floors; f++) {
            int fy = Building.floorY(f);
            if (f == floors) { c.set(fy, atrium ? B.GLASS : B.GRAY_CONCRETE); if (e == 0) c.set(fy + 1, pal.parapet); break; }
            if (ladderA || ladderB) {
                if (f > 0) c.set(fy, B.ladder(ladderA ? Direction.SOUTH : Direction.NORTH));
                else c.set(fy, pal.floor);
            } else if (!(atrium && f > 0)) c.set(fy, f == 0 ? pal.floor : B.SMOOTH_STONE);
            for (int r = 1; r <= 3; r++) {
                int y = fy + r;
                if (e == 0) {
                    boolean along = uz == z0 || uz == z1;
                    boolean window = Math.floorMod(along ? ux : uz, 4) == 2 && r <= 2;
                    boolean door = f == 0 && uz == z0 && Math.abs(dx) <= 1 && r <= 2;
                    c.set(y, door ? B.AIR : window ? pal.window : Math.floorMod(along ? ux : uz, 4) == 0 ? pal.trim : wall(c, y));
                    continue;
                }
                if (ladderA || ladderB) { c.set(y, B.ladder(ladderA ? Direction.SOUTH : Direction.NORTH)); continue; }
                if (atrium) {
                    boolean table = f == 0 && r == 1 && Math.floorMod(dx, 4) == 0 && Math.floorMod(dz, 3) != 0 && Math.abs(dx) <= 5 && Math.abs(dz) <= 5;
                    c.set(y, table ? B.POLISHED_ANDESITE : B.AIR);
                    continue;
                }
                boolean rail = f > 0 && Math.max(Math.abs(dx), Math.abs(dz)) == 8;
                if (rail) { if (r == 1) c.setConnected(y, B.IRON_BARS); else c.set(y, B.AIR); continue; }
                boolean stack = e >= 2 && ((Math.abs(dx) >= 10 && Math.floorMod(uz, 3) == 0 && Math.abs(dz) >= 2)
                        || (Math.abs(dz) >= 10 && Math.floorMod(ux, 3) == 0 && Math.abs(dx) >= 2));
                if (stack) {
                    long h = c.hash(y, 270);
                    c.set(y, Rng.chance(h, 0.12) ? B.AIR : Rng.chance(h >>> 9, 0.25) ? B.CHISELED_BOOKSHELF_EMPTY : B.BOOKSHELF);
                } else {
                    c.set(y, B.AIR);
                    if (r == 1 && Rng.chance(c.hash(y, 271), 0.002)) Decor.container(c, y, B.CHEST, Decor.LOOT_ARCHIVE);
                }
            }
        }
    }

    void funfair(Column c, int ux, int uz) {
        c.set(GROUND, Math.floorMod(ux * 7 + uz * 3, 11) == 0 ? B.ANDESITE : B.LIGHT_GRAY_CONCRETE);
        boolean fallen = abandoned && Rng.chance(Rng.hash(seed, 400), 0.5);
        Voxels w = fallen ? Models.WHEEL_FALLEN : Models.WHEEL;
        int ox = fallen ? 3 : px - 10, oz = mz;
        if (w.covers(ux - ox, uz - oz)) w.apply(c, ux - ox, uz - oz, GROUND + 1);
        if (fallen) return;
        int dx = ux - 12, dz = uz - 12;
        double d = dist(dx, dz);
        if (d <= 8.4) {
            if (d <= 6.5) c.set(GROUND + 1, B.SMOOTH_STONE_SLAB);
            int ry = GROUND + 8 - (int) (d / 3);
            c.set(ry, ((int) Math.round(Math.atan2(dz, dx) / (Math.PI / 8)) & 1) == 0 ? B.GRAY_CONCRETE : B.LIGHT_GRAY_CONCRETE);
            if (d < 1.5) c.fill(GROUND + 2, ry - 1, B.POLISHED_ANDESITE);
            double a = Math.atan2(dz, dx), q = Math.PI / 4;
            if (d > 4.5 && d < 5.5 && Math.abs(a - Math.round(a / q) * q) * d < 0.6) {
                c.fill(GROUND + 2, ry - 1, B.ANDESITE_WALL);
                c.set(GROUND + 3, B.POLISHED_ANDESITE);
            }
            return;
        }
        int kx = ux - 9, kz = uz - (pz - 8);
        if (Math.abs(kx) <= 2 && Math.abs(kz) <= 2) {
            boolean shell = Math.abs(kx) == 2 || Math.abs(kz) == 2;
            for (int y = GROUND + 1; y <= GROUND + 3; y++) c.set(y, shell ? (y == GROUND + 2 && kz == -2 && kx == 0 ? B.AIR : B.LIGHT_GRAY_CONCRETE) : B.AIR);
            c.set(GROUND + 4, B.SMOOTH_STONE_SLAB);
        }
    }

    void colossus(Column c, int ux, int uz) {
        long gh = c.hash(GROUND, 280);
        c.set(GROUND, Rng.chance(gh, 0.12) ? B.CRACKED_STONE_BRICKS : B.POLISHED_ANDESITE);
        if (Rng.chance(gh >>> 9, 0.012)) c.set(GROUND + 1, Rng.chance(gh >>> 20, 0.5) ? B.COBBLE_SLAB : B.ANDESITE);
        int bx = 10, bz = 10;
        int px0 = ux - bx, pz0 = uz - bz;
        if (Math.abs(px0) <= 6 && Math.abs(pz0) <= 6) {
            for (int y = GROUND + 1; y <= GROUND + 6; y++) c.set(y, y == GROUND + 6 ? pal.trim : wall(c, y));
            for (int s : new int[]{-1, 1}) {
                if (Math.abs(px0 - 3 * s) <= 1 && Math.abs(pz0) <= 2) {
                    int h = 8 + (int) Math.floorMod(c.hash(GROUND, 281), 3L);
                    c.fill(GROUND + 7, GROUND + 7 + h, B.LIGHT_GRAY_CONCRETE);
                    if (Rng.chance(c.hash(GROUND, 282), 0.3)) c.setConnected(GROUND + 8 + h, B.IRON_BARS);
                }
            }
            return;
        }
        if (Math.abs(px0) <= 2 && pz0 >= 7 && pz0 <= 12) {
            int top = GROUND + 6 - (pz0 - 7);
            for (int y = GROUND + 1; y < top; y++) c.set(y, pal.wall);
            if (top > GROUND) c.set(top, B.stair(Blocks.POLISHED_ANDESITE_STAIRS, Direction.NORTH));
            return;
        }
        Voxels hd = Models.HEAD_FALLEN;
        int hx = px - 20, hz = 8;
        if (hd.covers(ux - hx, uz - hz)) hd.apply(c, ux - hx, uz - hz, GROUND + 1);
        Voxels f = Models.FIST;
        int fx = 4, fz = pz - 8;
        if (f.covers(ux - fx, uz - fz)) f.apply(c, ux - fx, uz - fz, GROUND + 1);
    }

    void bellTower(Column c, int ux, int uz) {
        int dx = ux - mx, dz = uz - mz, ad = Math.max(Math.abs(dx), Math.abs(dz));
        int top = GROUND + 42, belfry = GROUND + 36;
        if (ad <= 8) c.set(GROUND, ad >= 7 ? B.POLISHED_ANDESITE : pal.floor);
        if (ad > 4) return;
        if (ad == 4) {
            for (int y = GROUND + 1; y <= top; y++) {
                boolean opening = y > belfry && y < top - 1 && Math.min(Math.abs(dx), Math.abs(dz)) <= 1;
                boolean door = dz == 4 && Math.abs(dx) <= 1 && y <= GROUND + 3;
                boolean slit = Math.min(Math.abs(dx), Math.abs(dz)) == 0 && Math.floorMod(y, 8) == 4 && y < belfry;
                c.set(y, opening || door ? B.AIR : slit ? B.TINTED_GLASS : Math.floorMod(y, 6) == 0 ? pal.trim : wall(c, y));
            }
        } else {
            for (int y = GROUND + 1; y < top; y++) c.set(y, B.AIR);
            boolean ladder = dx == 0 && dz == -3;
            if (ladder) for (int y = GROUND + 1; y <= belfry; y++) c.set(y, B.ladder(Direction.SOUTH));
            else c.set(belfry, pal.floor);
            c.set(top, pal.trim);
            if (dx == 0 && dz == 0) c.set(top - 1, B.BELL_CEILING);
        }
        for (int k = 1; k <= 5; k++) if (ad <= 5 - k) c.set(top + k, B.GRAY_CONCRETE);
        if (ad == 0) c.fill(top + 6, top + 9, B.IRON_BARS);
    }

    void crematorium(Column c, int ux, int uz) {
        int x0 = 5, z0 = 8, x1 = px - 6, z1 = pz - 12, top = GROUND + 10;
        double ch = dist(ux - (px - 9), uz - 5);
        if (ch <= 2.6) {
            for (int y = GROUND + 1; y <= GROUND + 40; y++) c.set(y, ch > 1.5 ? (Math.floorMod(y, 8) == 0 ? pal.trim : pal.wall) : B.AIR);
            return;
        }
        if (uz > z1 && uz <= z1 + 5 && ux >= x0 && ux <= x1) {
            if (uz == z1 + 4 && Math.floorMod(ux - x0, 4) == 0) c.fill(GROUND + 1, top - 1, pal.trim);
            c.set(top, B.SMOOTH_STONE_SLAB);
            c.set(GROUND, pal.floor);
            return;
        }
        if (ux < x0 || ux > x1 || uz < z0 || uz > z1) return;
        int dx = ux - mx, dz = uz - (z0 + z1) / 2;
        int e = Math.min(Math.min(ux - x0, x1 - ux), Math.min(uz - z0, z1 - uz));
        c.set(GROUND, pal.floor);
        if (e == 0) {
            for (int y = GROUND + 1; y < top; y++) {
                boolean door = uz == z1 && Math.abs(dx) <= 1 && y <= GROUND + 4;
                boolean window = (ux == x0 || ux == x1) && Math.floorMod(uz, 4) == 2 && y >= GROUND + 3 && y <= GROUND + 8;
                c.set(y, door ? B.AIR : window ? B.GRAY_GLASS : wall(c, y));
            }
            c.set(top, pal.trim);
            return;
        }
        c.set(top, Math.abs(dx) <= 2 && Math.abs(dz) <= 2 ? B.GLASS : B.GRAY_CONCRETE);
        for (int y = GROUND + 1; y < top; y++) c.set(y, B.AIR);
        if (uz == z0 + 1) {
            if (Math.floorMod(ux - x0, 4) == 2) {
                c.set(GROUND + 1, hfacing(Blocks.BLAST_FURNACE, Direction.SOUTH));
                c.fill(GROUND + 2, top - 1, B.POLISHED_DEEPSLATE);
            } else c.fill(GROUND + 1, GROUND + 2, B.POLISHED_ANDESITE);
            return;
        }
        if ((ux == x0 + 1 || ux == x1 - 1) && uz > z0 + 2) {
            for (int y = GROUND + 2; y <= GROUND + 6; y += 2) {
                c.set(y, B.SMOOTH_STONE_SLAB_TOP);
                if (Rng.chance(c.hash(y, 290), 0.4)) c.set(y + 1, B.FLOWER_POT);
            }
            return;
        }
        if (Math.abs(dx) <= 1 && Math.abs(dz) <= 2) {
            c.set(GROUND + 1, B.POLISHED_ANDESITE);
            if (Math.abs(dx) == 1 && Math.abs(dz) == 2) c.set(GROUND + 2, B.CANDLE);
            return;
        }
        if (dz >= 4 && uz <= z1 - 2 && Math.floorMod(uz, 2) == 0 && Math.abs(dx) >= 2 && e >= 2)
            c.set(GROUND + 1, B.stair(Blocks.SPRUCE_STAIRS, Direction.SOUTH));
    }

    void mausoleum(Column c, int ux, int uz) {
        int dx = ux - mx, dz = uz - mz, ad = Math.max(Math.abs(dx), Math.abs(dz));
        int hf = GROUND - 8;
        if (ad > 15) return;
        c.set(GROUND, B.POLISHED_ANDESITE);
        int ht = GROUND + Math.min(5, (15 - ad) / 3 + 1) * 3;
        for (int y = GROUND + 1; y <= ht; y++) c.set(y, (y - GROUND) % 3 == 0 ? B.POLISHED_DEEPSLATE : B.BLACK_CONCRETE);
        if (Math.abs(dx) <= 1 && dz >= 7 && dz <= 15) {
            int fl = GROUND - (15 - dz);
            c.set(fl, dz < 15 ? B.stair(Blocks.POLISHED_DEEPSLATE_STAIRS, Direction.SOUTH) : B.POLISHED_DEEPSLATE);
            for (int y = fl + 1; y <= fl + 3; y++) c.set(y, B.AIR);
        }
        if (Math.abs(dx) <= 6 && Math.abs(dz) <= 6) {
            boolean shell = Math.abs(dx) == 6 || Math.abs(dz) == 6;
            c.set(hf, B.POLISHED_DEEPSLATE);
            for (int y = hf + 1; y <= hf + 5; y++) c.set(y, shell && !(dz == 6 && Math.abs(dx) <= 1 && y <= hf + 3) ? B.BLACK_CONCRETE : B.AIR);
            c.set(hf + 6, B.BLACK_CONCRETE);
            if (shell) return;
            if (Math.abs(dx) <= 1 && Math.abs(dz) <= 2) {
                c.set(hf + 1, B.POLISHED_ANDESITE);
                boolean edge = Math.abs(dx) == 1 || Math.abs(dz) == 2;
                c.set(hf + 2, edge ? B.GLASS : B.GRAY_WOOL);
                c.set(hf + 3, B.GLASS);
            }
            if (Math.abs(dx) == 4 && Math.abs(dz) == 4) { c.set(hf + 1, B.POLISHED_ANDESITE); c.set(hf + 2, B.CANDLE); }
        }
    }

    void computer(Column c, int ux, int uz) {
        int x0 = 3, z0 = 3, x1 = px - 4, z1 = pz - 4;
        if (ux < x0 || ux > x1 || uz < z0 || uz > z1) return;
        int e = Math.min(Math.min(ux - x0, x1 - ux), Math.min(uz - z0, z1 - uz));
        int roof = GROUND + 16;
        c.set(roof, B.GRAY_CONCRETE);
        if (e == 0) c.set(roof + 1, pal.parapet);
        for (int fl = 0; fl < 2; fl++) {
            int f = GROUND + fl * 8;
            boolean ladder = ux == x0 + 1 && uz == z0 + 1;
            c.set(f, fl == 0 ? pal.floor : (ladder ? B.ladder(Direction.SOUTH) : B.SMOOTH_STONE));
            if (e == 0) {
                for (int y = f + 1; y < f + 8; y++) {
                    boolean door = fl == 0 && uz == z1 && Math.abs(ux - mx) <= 1 && y <= f + 3;
                    c.set(y, door ? B.AIR : Math.floorMod(ux + uz, 5) == 0 ? pal.trim : wall(c, y));
                }
                continue;
            }
            if (ladder) { for (int y = f + 1; y < f + 8; y++) c.set(y, B.ladder(Direction.SOUTH)); continue; }
            c.set(f + 1, B.SMOOTH_STONE_SLAB);
            for (int y = f + 2; y < f + 8; y++) c.set(y, B.AIR);
            if (e == 1) {
                for (int y = f + 2; y <= f + 4; y++) c.set(y, Math.floorMod(ux + uz + y, 3) == 0 ? B.BOOKSHELF : B.CHISELED_BOOKSHELF_EMPTY);
                continue;
            }
            if (uz <= z0 + 3) { Decor.console(c, f + 2, ux - x0, uz - z0, Rng.hash(seed, 0xC0 + fl)); continue; }
            int row = Math.floorMod(uz - z0 - 5, 5);
            if (row <= 1 && e >= 3 && Math.abs(ux - mx) > 1) {
                BlockState unit = switch (Math.floorMod(ux, 6)) {
                    case 0 -> B.OBSERVER_N;
                    case 1 -> B.DISPENSER_N;
                    case 2 -> B.DROPPER_N;
                    case 3 -> B.JUKEBOX;
                    case 4 -> B.NOTE_BLOCK;
                    default -> B.BLACK_GLASS;
                };
                c.set(f + 2, B.POLISHED_DEEPSLATE);
                c.set(f + 3, unit);
                c.set(f + 4, row == 0 ? B.BLACK_GLASS : unit);
                c.set(f + 5, B.SMOOTH_STONE_SLAB);
            } else if (row == 3) c.setConnected(f + 6, B.IRON_BARS);
            else if (Rng.chance(c.hash(f, 240), 0.0015)) Decor.container(c, f + 2, B.CHEST, Decor.LOOT_OFFICE);
        }
    }

    static final int AIR_TOP = GROUND + 64;
    static final int LID_WALK = brutalicious.gen.Blackout.TOP + 1;

    void airPlant(Column c, int ux, int uz) {
        int qx = ux < mx ? px / 4 + 1 : 3 * px / 4 - 1, qz = uz < mz ? pz / 4 + 1 : 3 * pz / 4 - 1;
        int dx = ux - qx, dz = uz - qz, ad = Math.max(Math.abs(dx), Math.abs(dz));
        int pdx = ux - mx, pdz = uz - mz;
        if (ad <= 4) {
            if (ad == 4) {
                for (int y = GROUND + 1; y <= AIR_TOP; y++) {
                    boolean mouth = Math.min(Math.abs(dx), Math.abs(dz)) <= 1 && y >= GROUND + 3 && y <= GROUND + 5;
                    boolean louvre = Math.floorMod(y - GROUND, 3) == 0 && y > GROUND + 8 && Math.min(Math.abs(dx), Math.abs(dz)) <= 2;
                    boolean lidDoor = ux < mx && uz < mz && dx == -4 && dz == 0 && y >= LID_WALK && y <= LID_WALK + 1;
                    if (mouth || lidDoor) c.set(y, B.AIR);
                    else if (louvre) c.setConnected(y, B.IRON_BARS);
                    else c.set(y, Math.floorMod(y, 8) == 0 ? B.POLISHED_DEEPSLATE : B.GRAY_CONCRETE);
                }
                c.set(AIR_TOP + 1, B.GRAY_CONCRETE);
                return;
            }
            c.set(GROUND - 6, B.POLISHED_DEEPSLATE);
            for (int y = GROUND - 5; y < AIR_TOP; y++) c.set(y, B.AIR);
            c.setConnected(AIR_TOP, B.IRON_BARS);
            boolean blade = Math.abs(dx) == Math.abs(dz) || dx == 0 || dz == 0;
            c.set(GROUND - 3, ad == 0 ? B.POLISHED_DEEPSLATE : blade ? B.POLISHED_BLACKSTONE : B.AIR);
            c.set(GROUND + 2, ad >= 3 ? B.SMOOTH_STONE_SLAB_TOP : B.AIR);
            if (ux < mx && uz < mz && dx == -3 && dz == 0) for (int y = GROUND - 5; y < AIR_TOP; y++) c.set(y, B.ladder(Direction.EAST));
            return;
        }
        if (Math.abs(pdx) <= 4 && Math.abs(pdz) <= 4) {
            boolean shell = Math.abs(pdx) == 4 || Math.abs(pdz) == 4;
            for (int y = GROUND + 1; y <= GROUND + 10; y++) {
                boolean mouth = Math.min(Math.abs(pdx), Math.abs(pdz)) <= 1 && y >= GROUND + 3 && y <= GROUND + 5;
                c.set(y, shell && !mouth ? B.GRAY_CONCRETE : B.AIR);
            }
            if (!shell) {
                double d = dist(pdx, pdz);
                boolean blade = Math.abs(pdx) == Math.abs(pdz) || pdx == 0 || pdz == 0;
                c.set(GROUND + 1, d < 1 ? B.POLISHED_DEEPSLATE : blade ? B.POLISHED_BLACKSTONE : B.AIR);
            }
            return;
        }
        boolean ductX = Math.abs(uz - qz) <= 2 && Math.abs(pdz) > 4 && ux > px / 4 + 5 && ux < 3 * px / 4 - 5;
        boolean ductZ = Math.abs(ux - qx) <= 2 && Math.abs(pdx) > 4 && uz > pz / 4 + 5 && uz < 3 * pz / 4 - 5;
        boolean spine = (Math.abs(pdx) <= 2 && Math.abs(uz - qz) > 2) || (Math.abs(pdz) <= 2 && Math.abs(ux - qx) > 2);
        if (ductX || ductZ || (spine && (Math.abs(pdx) <= 2 ? Math.abs(pdz) < pz / 4 : Math.abs(pdx) < px / 4))) {
            int across = ductX ? Math.abs(uz - qz) : ductZ ? Math.abs(ux - qx) : Math.abs(pdx) <= 2 ? Math.abs(pdx) : Math.abs(pdz);
            for (int y = GROUND + 2; y <= GROUND + 6; y++) c.set(y, across == 2 || y == GROUND + 2 || y == GROUND + 6 ? B.GRAY_CONCRETE : B.AIR);
            if (Math.floorMod(ux + uz, 6) == 0 && across == 2) c.set(GROUND + 1, B.POLISHED_ANDESITE);
        }
    }

    void labyrinth(Column c, int ux, int uz) {
        int x0 = 2, z0 = 2, x1 = px - 3, z1 = pz - 3;
        if (ux < x0 || ux > x1 || uz < z0 || uz > z1) return;
        int wallTop = GROUND + 7, ceil = GROUND + 8;
        c.set(GROUND, B.DEEPSLATE_TILES);
        c.set(ceil, B.POLISHED_DEEPSLATE);
        int nI = (x1 - x0 - 1) / 4, nJ = (z1 - z0 - 1) / 4;
        int ix = ux - (x0 + 1), iz = uz - (z0 + 1);
        int ci = nI / 2, cj = nJ / 2;
        int gx = x0 + 1 + 4 * ci + 1;
        boolean gate = ux == gx && (uz == z1 || iz >= nJ * 4 - 1);
        boolean edge = ux == x0 || ux == x1 || uz == z0 || uz == z1;
        boolean wallHere;
        if (gate) wallHere = false;
        else if (edge) wallHere = true;
        else {
            int i = ix / 4, j = iz / 4, a = ix % 4, b = iz % 4;
            if (i >= nI || j >= nJ) wallHere = true;
            else if (Math.abs(i - ci) <= 1 && Math.abs(j - cj) <= 1 && !(a == 3 && b == 3)
                    && !(a == 3 && i == ci + 1) && !(b == 3 && j == cj + 1)) wallHere = false;
            else if (a == 3 && b == 3) wallHere = true;
            else if (a == 3) wallHere = mazeChoice(i, j, nI) != 1;
            else if (b == 3) wallHere = !(j + 1 < nJ && mazeChoice(i, j + 1, nI) == 2);
            else wallHere = false;
            if (!wallHere && a == 1 && b == 1) {
                if (i == ci && j == cj) { c.fill(GROUND + 1, wallTop, B.AIR); Decor.container(c, GROUND + 1, B.CHEST, Decor.LOOT_VAULT); return; }
                if (Math.abs(i - ci) == 1 && Math.abs(j - cj) == 1) { c.fill(GROUND + 1, wallTop, B.AIR); Decor.spawner(c, GROUND + 1, "minecraft:skeleton"); return; }
                if (Rng.chance(Rng.hash(seed, i, j, 7), 0.04)) { c.fill(GROUND + 1, wallTop, B.AIR); Decor.spawner(c, GROUND + 1, "minecraft:zombie"); return; }
            }
        }
        c.fill(GROUND + 1, wallTop, wallHere ? B.DEEPSLATE_BRICKS : B.AIR);
        if (!wallHere && Rng.chance(c.hash(GROUND, 250), 0.006)) c.set(GROUND + 1, B.rot16(B.SKULL, (int) c.hash(GROUND, 251)));
    }

    int mazeChoice(int i, int j, int nI) {
        if (i == nI - 1 && j == 0) return 0;
        if (i == nI - 1) return 2;
        if (j == 0) return 1;
        return Rng.chance(Rng.hash(seed, i, j, 3), 0.5) ? 1 : 2;
    }

    void sarcophagus(Column c, int ux, int uz) {
        int x0 = 5, z0 = 7, x1 = px - 6, z1 = pz - 12, top = GROUND + 30;
        double sd = dist(ux - (px - 8), uz - 3);
        if (sd <= 3.6) {
            for (int y = GROUND + 1; y <= GROUND + 70; y++) {
                if (sd <= 2.6) c.set(y, sd > 1.5 ? (Math.floorMod(y, 10) == 0 ? B.POLISHED_DEEPSLATE : B.GRAY_CONCRETE) : B.AIR);
                else if (sd >= 3 && (Math.floorMod(y, 8) == 0 || Math.abs(Math.abs(ux - (px - 8)) - Math.abs(uz - 3)) == 0) && y <= GROUND + 60) c.setConnected(y, B.IRON_BARS);
            }
            return;
        }
        if (ux >= x0 && ux <= x1 && uz > z1 && uz <= pz - 3) {
            int h = top - ((uz - z1 + 1) / 2) * 4;
            for (int y = GROUND + 1; y <= h; y++) c.set(y, Math.floorMod(y, 4) == 0 ? B.GRAY_CONCRETE : wall(c, y));
            return;
        }
        if (ux < x0 || ux > x1 || uz < z0 || uz > z1) {
            if (Rng.chance(c.hash(GROUND, 300), 0.02)) c.set(GROUND + 1, B.BLACK_CONCRETE);
            return;
        }
        int e = Math.min(Math.min(ux - x0, x1 - ux), Math.min(uz - z0, z1 - uz));
        int dx = ux - mx, dz = uz - (z0 + z1) / 2;
        if (e == 0) {
            int breachH = GROUND + 6 + (int) Math.floorMod(c.hash(GROUND, 301), 4L);
            boolean breach = ux == x0 && Math.abs(dz) <= 2;
            for (int y = GROUND + 1; y <= top; y++) c.set(y, breach && y <= breachH ? B.AIR : wall(c, y));
            return;
        }
        boolean hole = Math.abs(dx) <= 3 && Math.abs(dz) <= 2;
        c.set(top, hole ? B.AIR : Math.floorMod(ux, 3) == 0 ? B.POLISHED_DEEPSLATE : B.GRAY_CONCRETE);
        for (int y = GROUND + 1; y < top; y++) c.set(y, B.AIR);
        c.set(GROUND, B.GRAY_CONCRETE);
        if (Math.abs(dx) <= 6 && Math.abs(dz) <= 6) {
            if (Math.abs(dx) == 6 || Math.abs(dz) == 6) { c.fill(GROUND - 14, GROUND, B.GRAY_CONCRETE); return; }
            c.set(GROUND - 15, B.BASALT);
            for (int y = GROUND - 14; y <= GROUND; y++) c.set(y, B.AIR);
            double n = Rng.fbm(seed ^ 0x5A4CL, ux / 2.5, uz / 2.5);
            c.set(GROUND - 14, n > 0.55 ? B.MAGMA : n > 0.4 ? B.BLACKSTONE : B.BASALT);
            if (n > 0.62) c.set(GROUND - 13, B.BASALT);
            if (dx == 4 && dz == 4) { c.set(GROUND - 14, B.POLISHED_DEEPSLATE); Decor.container(c, GROUND - 13, B.CHEST, Decor.LOOT_VAULT); }
            if (dist(dx, dz) <= 5.5) {
                int y = GROUND + 8 + dx;
                c.set(y, B.POLISHED_DEEPSLATE);
                if (Math.floorMod(dx + dz, 2) == 0) c.setConnected(y + 1, B.IRON_BARS);
            }
            return;
        }
        long h = c.hash(GROUND, 302);
        if (Rng.chance(h, 0.04)) c.set(GROUND + 1, Rng.chance(h >>> 8, 0.5) ? B.BLACK_CONCRETE : B.COAL_BLOCK);
        else if (Rng.chance(h >>> 16, 0.0015)) Decor.spawner(c, GROUND + 1, Decor.pick(h >>> 30, "minecraft:zombie", "minecraft:husk"));
        if (Rng.chance(h >>> 24, 0.004)) {
            int len = 3 + (int) Math.floorMod(h >>> 40, 12L);
            for (int y = top - 1; y >= top - len; y--) c.set(y, B.CHAIN);
        }
    }

    void decon(Column c, int ux, int uz) {
        boolean fence = ux == 1 || ux == px - 2 || uz == 1 || uz == pz - 2;
        if (fence) {
            if (!(uz == pz - 2 && Math.abs(ux - mx) <= 2)) { c.setConnected(GROUND + 1, B.IRON_BARS); c.setConnected(GROUND + 2, B.IRON_BARS); }
            return;
        }
        c.set(GROUND, B.SMOOTH_STONE);
        int kx = ux - mx, kz = uz - (pz - 6);
        if (Math.abs(kx) <= 2 && Math.abs(kz) <= 2) {
            boolean shell = Math.abs(kx) == 2 || Math.abs(kz) == 2;
            for (int y = GROUND + 1; y <= GROUND + 3; y++) c.set(y, shell ? (y == GROUND + 2 && kz == 2 && kx == 0 ? B.TINTED_GLASS : B.LIGHT_GRAY_CONCRETE) : B.AIR);
            c.set(GROUND + 4, B.SMOOTH_STONE_SLAB);
            if (!shell) Decor.console(c, GROUND + 1, kx + 2, kz + 2, Rng.hash(seed, 0xDEC));
            return;
        }
        if (ux < 4 || uz < 6 || uz > pz - 10) return;
        int i = (ux - 4) / 10, a = (ux - 4) % 10;
        if (i >= 4 || a >= 7) {
            if (a == 8 && i < 4) c.set(GROUND + 1, B.SPRUCE_SLAB);
            return;
        }
        boolean side = a == 0 || a == 6, end = uz == 6 || uz == pz - 10;
        c.set(GROUND + 5, B.SMOOTH_STONE_SLAB);
        if (side) { c.fill(GROUND + 1, GROUND + 4, B.LIGHT_GRAY_CONCRETE); return; }
        for (int y = GROUND + 1; y <= GROUND + 4; y++) c.set(y, B.AIR);
        if (end) { if (Rng.chance(c.hash(GROUND, 310), 0.6)) c.set(GROUND + 4, B.CHAIN); return; }
        if (Math.floorMod(uz - 6, 4) == 0 && a != 3) { c.setConnected(GROUND + 1, B.IRON_BARS); c.setConnected(GROUND + 2, B.IRON_BARS); return; }
        if ((a == 1 || a == 5) && Math.floorMod(uz - 6, 4) == 2) { c.set(GROUND + 1, B.CAULDRON); c.set(GROUND + 4, B.CHAIN); return; }
        long h = c.hash(GROUND, 311);
        if (Rng.chance(h, 0.08)) c.set(GROUND + 1, Rng.chance(h >>> 8, 0.5) ? B.GRAY_CARPET : B.LIGHT_GRAY_CARPET);
        else if (Rng.chance(h >>> 16, 0.004)) Decor.container(c, GROUND + 1, B.BARREL, Decor.LOOT_HOSPITAL);
    }

    void seedVault(Column c, int ux, int uz) {
        int dx = ux - mx, z0 = 6, deep = GROUND - 13;
        if (Math.abs(dx) <= 3 && uz >= z0 && uz <= z0 + 10) {
            int h = GROUND + 12 - (uz - z0);
            for (int y = GROUND + 1; y <= h; y++) {
                boolean shell = Math.abs(dx) == 3 || y == h || uz == z0;
                boolean door = uz == z0 && Math.abs(dx) <= 1 && y <= GROUND + 3;
                boolean art = uz == z0 && Math.abs(dx) <= 1 && y > GROUND + 5 && y < h - 1 && Math.floorMod(y, 2) == 0;
                c.set(y, door ? B.AIR : art ? B.TINTED_GLASS : shell ? B.LIGHT_GRAY_CONCRETE : B.AIR);
            }
        }
        int[] side = {z0 + 20, z0 + 28};
        if (Math.abs(dx) <= 2 && uz >= z0 + 1 && uz <= z0 + 32) {
            int k = uz - (z0 + 1);
            int floor = Math.max(deep, GROUND - k);
            boolean wallC = Math.abs(dx) == 2;
            boolean doorway = false;
            for (int s : side) if (Math.abs(uz - s) <= 1 && floor == deep) doorway = true;
            c.set(floor, wallC ? B.SMOOTH_STONE : (floor > deep ? B.stair(Blocks.POLISHED_ANDESITE_STAIRS, Direction.NORTH) : B.SMOOTH_STONE));
            for (int y = floor + 1; y <= floor + 3; y++) c.set(y, wallC && !doorway ? B.SMOOTH_STONE : B.AIR);
            c.set(floor + 4, Math.floorMod(uz, 6) == 0 && !wallC ? B.REDSTONE_LAMP : B.GRAY_CONCRETE);
            return;
        }
        for (int s : side) {
            if (Math.abs(uz - s) <= 3 && Math.abs(dx) >= 3 && Math.abs(dx) <= 11) { vaultRoom(c, Math.abs(uz - s) == 3 || Math.abs(dx) == 11, deep, Math.abs(dx) == 10 || Math.abs(uz - s) == 2); return; }
        }
        if (uz >= z0 + 33 && uz <= z0 + 40 && Math.abs(dx) <= 6) {
            vaultRoom(c, uz == z0 + 40 || Math.abs(dx) == 6, deep, uz == z0 + 39 || Math.abs(dx) == 5);
        }
    }

    void vaultRoom(Column c, boolean shell, int deep, boolean byWall) {
        c.set(deep, B.SMOOTH_STONE);
        for (int y = deep + 1; y <= deep + 3; y++) c.set(y, shell ? B.SMOOTH_STONE : B.AIR);
        c.set(deep + 4, B.GRAY_CONCRETE);
        if (shell) return;
        long h = c.hash(deep, 320);
        if (byWall && Rng.chance(h, 0.3)) Decor.container(c, deep + 1, B.CHEST, Decor.LOOT_SEEDS);
        else if (Rng.chance(h >>> 9, 0.3)) c.set(deep + 1, B.snow(1));
    }
}
