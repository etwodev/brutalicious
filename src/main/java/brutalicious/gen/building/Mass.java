package brutalicious.gen.building;

import brutalicious.gen.B;
import brutalicious.gen.Camp;
import brutalicious.gen.Column;
import brutalicious.gen.Decor;
import brutalicious.gen.Palette;
import brutalicious.gen.Rng;
import net.minecraft.world.level.block.state.BlockState;

import static brutalicious.gen.building.Building.*;

public final class Mass {
    public enum Facade { RIBBON, SLIT, GRID, BLANK, LOGGIA, FINS, CARPARK }
    public enum Layout { CELLS, OPEN, ARCHIVE, GUTTED, PARKING, WARD, STORAGE, URNS, CONTROL }

    final long seed;
    final int px, pz;
    final int floors, basement;
    final int[] x0, z0, x1, z1;

    Palette pal = Palette.BETON;
    Facade facadeLow = Facade.RIBBON, facadeHigh = Facade.RIBBON;
    int highFrom = Integer.MAX_VALUE;
    boolean blankEnds;
    boolean pilotis;
    int balcony;
    int coreX = -1, coreZ = -1;
    int rampX = -1, rampZ = -1;
    Layout layout = Layout.CELLS;
    Decor.RoomKind kind = Decor.RoomKind.HOME;
    double decay = 0.2;
    int roomW = 6;
    int doorSides = 0b1111;
    int extraDoorFloor = Integer.MIN_VALUE;
    int deckDoorFloor = Integer.MIN_VALUE;
    int roofDoorFloor = Integer.MIN_VALUE;
    boolean allDoors;
    int[][] blasts = new int[0][];
    int ruinY = Integer.MAX_VALUE, ruinAmp = 0;
    boolean ribs;
    int[][] roofBoxes = new int[0][];
    int antennaX = -1, antennaZ = -1, antennaH = 0;
    boolean beacon;
    int roofUse;
    double livedShare = 0.07;
    boolean lit;
    boolean groundDoors = true;
    Layout deepLayout;
    int muralSide = -1, muralA0, muralKind;

    public Mass(long seed, int px, int pz, int floors, int basement) {
        this.seed = seed;
        this.px = px;
        this.pz = pz;
        this.floors = Math.max(1, floors);
        this.basement = Math.max(0, basement);
        int n = this.floors + this.basement;
        x0 = new int[n]; z0 = new int[n]; x1 = new int[n]; z1 = new int[n];
    }

    public Mass rect(int f, int ax0, int az0, int ax1, int az1) {
        int i = f + basement;
        x0[i] = ax0; z0[i] = az0; x1[i] = ax1; z1[i] = az1;
        return this;
    }

    public Mass rectAll(int ax0, int az0, int ax1, int az1) {
        for (int f = -basement; f < floors; f++) rect(f, ax0, az0, ax1, az1);
        return this;
    }

    public Mass centreCore() {
        int a = Integer.MIN_VALUE, b = Integer.MIN_VALUE, c = Integer.MAX_VALUE, d = Integer.MAX_VALUE;
        for (int i = 0; i < floors + basement; i++) {
            a = Math.max(a, x0[i]); b = Math.max(b, z0[i]); c = Math.min(c, x1[i]); d = Math.min(d, z1[i]);
        }
        if (c - a >= 8 && d - b >= 8) {
            coreX = (a + c) / 2 - 2;
            coreZ = (b + d) / 2 - 2;
        }
        return this;
    }

    public int floors() { return floors; }

    boolean in(int f, int ux, int uz) {
        if (f < -basement || f >= floors) return false;
        int i = f + basement;
        return ux >= x0[i] && ux <= x1[i] && uz >= z0[i] && uz <= z1[i];
    }

    int idx(int f) { return f + basement; }

    Facade facade(int f) { return f >= highFrom ? facadeHigh : facadeLow; }

    static int depthOf(Facade f) {
        return switch (f) { case LOGGIA -> 2; case GRID -> 1; default -> 0; };
    }

    int depth(int f) { return f < 0 ? 0 : depthOf(facade(f)); }

    int topY() {
        int top = floorY(floors) + 1;
        if (coreX >= 0) top = Math.max(top, floorY(floors + 1));
        for (int[] r : roofBoxes) top = Math.max(top, floorY(floors) + r[4]);
        if (antennaH > 0) top = Math.max(top, floorY(floors) + antennaH + 2);
        return top;
    }

    public int reach(int side, int f) {
        if (f < -basement || f >= floors) return -1;
        if (ruinY != Integer.MAX_VALUE && floorY(f) + 4 >= ruinY) return -1;
        int i = idx(f), cx = px / 2, cz = pz / 2;
        return switch (side) {
            case W -> (cz >= z0[i] && cz <= z1[i]) ? x0[i] : -1;
            case E -> (cz >= z0[i] && cz <= z1[i]) ? px - 1 - x1[i] : -1;
            case N -> (cx >= x0[i] && cx <= x1[i]) ? z0[i] : -1;
            default -> (cx >= x0[i] && cx <= x1[i]) ? pz - 1 - z1[i] : -1;
        };
    }

    public void fill(Column c, int ux, int uz) {
        boolean inCore = coreX >= 0 && ux >= coreX && ux < coreX + 5 && uz >= coreZ && uz < coreZ + 5;
        int coreDist = coreX < 0 ? 99 : Math.max(Math.max(coreX - ux, ux - (coreX + 4)), Math.max(coreZ - uz, uz - (coreZ + 4)));
        boolean inRamp = rampX >= 0 && ux >= rampX && ux < rampX + 8 && uz >= rampZ && uz < rampZ + 3;

        for (int f = -basement; f <= floors; f++) {
            int F = floorY(f);
            boolean inR = in(f, ux, uz);
            boolean inB = in(f - 1, ux, uz);
            if (inCore && (inR || inB)) {
                coreColumn(c, f, ux - coreX, uz - coreZ, F);
                continue;
            }
            if (inRamp && (inR || inB)) {
                rampColumn(c, f, ux - rampX, F, inR);
                continue;
            }
            if (inR || inB) {
                c.set(F, slab(c, f, ux, uz, inR, inB, coreDist));
            } else if (f >= 1 && f < floors && balcony > 0) {
                balconyCell(c, f, ux, uz, F);
            }
            if (inR) {
                for (int r = 1; r <= 3; r++) c.set(F + r, room(c, f, r, ux, uz, F + r, coreDist));
                if (edge(f, ux, uz) > depth(f)) furnish(c, f, ux, uz, F, coreDist);
            } else if (inB && f >= 1) {
                terrace(c, f, ux, uz, F);
            } else if (f >= 0 && f < floors) {
                fin(c, f, ux, uz, F);
            }
        }
        if (inCore) {
            c.fill(floorY(floors + 1), floorY(floors + 1), pal.trim);
        }
        roof(c, ux, uz);
        for (int[] b : blasts) {
            int dx = ux - b[0], dz = uz - b[2];
            int rr = b[3] * b[3] - dx * dx - dz * dz;
            if (rr < 0) continue;
            int dy = (int) Math.sqrt(rr);
            c.fill(b[1] - dy, b[1] + dy, B.AIR);
        }
        if (ruinY != Integer.MAX_VALUE) ruin(c, ux, uz);
    }

    public Mass blast(int n) {
        if (floors < 3) return this;
        blasts = new int[n][];
        for (int k = 0; k < n; k++) {
            long h = Rng.hash(seed, 500 + k);
            int f = Rng.range(h, 1, floors - 1), i = idx(f);
            int side = Rng.range(h >>> 8, 0, 3);
            int ax = Rng.range(h >>> 16, x0[i] + 2, x1[i] - 2), az = Rng.range(h >>> 24, z0[i] + 2, z1[i] - 2);
            int bx = side == W ? x0[i] : side == E ? x1[i] : ax;
            int bz = side == N ? z0[i] : side == S ? z1[i] : az;
            blasts[k] = new int[]{bx, floorY(f) + 2, bz, Rng.range(h >>> 32, 2, 4)};
        }
        return this;
    }

    public boolean occupied(int ux, int uz, int y) {
        int f = Math.floorDiv(y - GROUND, FLOOR_H), r = y - floorY(f);
        if (in(f, ux, uz)) return true;
        if (r == 0 && in(f - 1, ux, uz)) return true;
        if (coreX >= 0 && f == floors && ux >= coreX && ux < coreX + 5 && uz >= coreZ && uz < coreZ + 5) return true;
        return false;
    }

    int edge(int f, int ux, int uz) {
        int i = idx(f);
        return Math.min(Math.min(ux - x0[i], x1[i] - ux), Math.min(uz - z0[i], z1[i] - uz));
    }

    BlockState slab(Column c, int f, int ux, int uz, boolean inR, boolean inB, int coreDist) {
        boolean edgeR = inR && edge(f, ux, uz) == 0;
        boolean edgeB = !inR && inB && edge(f - 1, ux, uz) == 0;
        if (edgeR || edgeB) return pal.trim;
        if (f >= 1 && f < floors && coreDist > 2 && inR && edge(f, ux, uz) > depth(f) && hole(c, f)) return B.AIR;
        return pal.floor;
    }

    boolean hole(Column c, int f) {
        if (decay <= 0.05) return false;
        double n = Rng.fbm(seed + f * 7919L, c.x / 5.5, c.z / 5.5);
        return n > 1.0 - decay * 0.42;
    }

    static final int[][] RING = {{0, 7, 6}, {1, -1, 5}, {2, 3, 4}};

    void coreColumn(Column c, int f, int lx, int lz, int F) {
        boolean wall = lx == 0 || lx == 4 || lz == 0 || lz == 4;
        if (wall) {
            c.set(F, pal.trim);
            if (f == floors + 1) return;
            for (int r = 1; r <= 3; r++) {
                boolean door = lx == 0 && (lz == 1 || lz == 2) && r <= 2;
                c.set(F + r, door ? B.AIR : pal.wallAt(c.seed, c.x, F + r, c.z, decay * 0.5));
            }
            return;
        }
        int i = lx - 1, j = lz - 1;
        int k = RING[i][j];
        if (k < 0) {
            for (int r = 0; r <= 3; r++) c.set(F + r, pal.trim);
            return;
        }
        if (f == -basement) c.set(F, pal.floor);
        else c.set(F, stepAt(k, 0));
        for (int r = 1; r <= 3; r++) c.set(F + r, f == floors ? B.AIR : stepAt(k, r));
    }

    BlockState stepAt(int k, int r) {
        if (Math.floorMod(k / 2 + 1, 4) != r) return B.AIR;
        return (k & 1) == 0 ? pal.slabBottom : pal.slabTop;
    }

    void rampColumn(Column c, int f, int k, int F, boolean inR) {
        if (f <= 0) c.set(F, pal.floor);
        else c.set(F, k < 6 ? B.AIR : stepAt(k, 0));
        if (inR) for (int r = 1; r <= 3; r++) c.set(F + r, f >= 0 ? stepAt(k, r) : B.AIR);
    }

    BlockState room(Column c, int f, int r, int ux, int uz, int y, int coreDist) {
        int i = idx(f);
        int dW = ux - x0[i], dE = x1[i] - ux, dN = uz - z0[i], dS = z1[i] - uz;
        int e = Math.min(Math.min(dW, dE), Math.min(dN, dS));

        if (f < 0) return e == 0 ? pal.wallAt(c.seed, c.x, y, c.z, decay) : (f < -3 && deepLayout != null ? interior(c, f, r, ux, uz, y, coreDist, 0) : B.AIR);

        boolean longX = (x1[i] - x0[i]) >= (z1[i] - z0[i]);
        int side; int along; int sideStart;
        if (e == dW || e == dE) { side = e == dW ? W : E; along = uz; sideStart = z0[i]; }
        else { side = e == dN ? N : S; along = ux; sideStart = x0[i]; }
        int a = along - sideStart;

        if (f == 0 && pilotis) {
            if (e == 0) {
                boolean corner = Math.min(dW, dE) == 0 && Math.min(dN, dS) == 0;
                return (corner || Math.floorMod(a, 6) == 0) ? pal.trim : B.AIR;
            }
            return B.AIR;
        }

        Facade fac = facade(f);
        if (blankEnds && ((longX && (side == W || side == E)) || (!longX && (side == N || side == S)))) fac = Facade.BLANK;
        int d = depthOf(fac);

        if (e <= d) {
            boolean doorFloor = (f == 0 && groundDoors) || f == extraDoorFloor || f == deckDoorFloor || f == roofDoorFloor || allDoors;
            if (doorFloor && r <= 2 && (doorSides & (1 << side)) != 0) {
                int mid = (side == W || side == E) ? (z0[i] + z1[i]) / 2 : (x0[i] + x1[i]) / 2;
                if (Math.abs(along - mid) <= 1) return B.AIR;
            }
            int perp = (side == W || side == E) ? Math.min(dN, dS) : Math.min(dW, dE);
            if (perp <= d) return pal.wallAt(c.seed, c.x, y, c.z, decay);
            if (side == muralSide && e == 0 && f >= 1 && f <= 4 && a >= muralA0 && a < muralA0 + 16)
                return mosaic(a - muralA0, (f - 1) * 4 + r);
            return facadeCell(c, fac, e, d, a, r, f, y);
        }
        return interior(c, f, r, ux, uz, y, coreDist, d);
    }

    BlockState facadeCell(Column c, Facade fac, int e, int d, int a, int r, int f, int y) {
        BlockState wall = pal.wallAt(c.seed, c.x, y, c.z, decay);
        if (e == 0 && d > 0) {
            return switch (fac) {
                case LOGGIA -> Math.floorMod(a, 4) == 0 ? wall : (r == 1 ? pal.parapet : B.AIR);
                case GRID -> (Math.floorMod(a, 3) == 0 || r == 3) ? wall : B.AIR;
                default -> wall;
            };
        }
        if (e < d) return r == 1 && Rng.chance(c.hash(y, 5), 0.03) ? B.POTTED_DEAD_BUSH : B.AIR;
        return switch (fac) {
            case RIBBON -> (r == 2 && Math.floorMod(a, 6) != 0) ? window(c, y) : wall;
            case SLIT -> (Math.floorMod(a, 4) == 2 && r <= 2) ? window(c, y) : wall;
            case GRID -> r <= 2 ? window(c, y) : wall;
            case BLANK -> (f > 0 && r == 2 && Math.floorMod(a, 12) == 6 && ribs) ? window(c, y) : wall;
            case LOGGIA -> r == 3 ? wall : (Math.floorMod(a, 8) == 6 ? B.AIR : window(c, y));
            case FINS -> (r <= 2 && Math.floorMod(a, 3) != 0) ? window(c, y) : wall;
            case CARPARK -> r == 1 ? pal.parapet : B.AIR;
        };
    }

    BlockState window(Column c, int y) {
        double u = Rng.unit(c.hash(y, 3));
        if (u < 0.3 + decay * 0.45) return B.AIR;
        return pal.window;
    }

    BlockState interior(Column c, int f, int r, int ux, int uz, int y, int coreDist, int d) {
        if (coreDist <= 2) return B.AIR;
        int i = idx(f);
        int ix0 = x0[i] + d + 1, ix1 = x1[i] - d - 1, iz0 = z0[i] + d + 1, iz1 = z1[i] - d - 1;
        boolean longX = (ix1 - ix0) >= (iz1 - iz0);
        int u = longX ? ux - ix0 : uz - iz0;
        int v = longX ? uz - iz0 : ux - ix0;
        int vSpan = longX ? iz1 - iz0 : ix1 - ix0;
        Layout lay = layoutOf(f);
        switch (lay) {
            case CELLS -> {
                if (vSpan < 4) break;
                if (vSpan < 8) {
                    if (v <= 1) break;
                    if (v == 2) {
                        boolean door = Math.floorMod(u, roomW) == roomW / 2 && r <= 2;
                        return door ? B.AIR : partition(c, y);
                    }
                    if (Math.floorMod(u, roomW) == 0) return partition(c, y);
                    break;
                }
                int vc = vSpan / 2, dv = Math.abs(v - vc);
                if (dv <= 1) break;
                if (dv == 2) {
                    boolean door = Math.floorMod(u, roomW) == roomW / 2 && r <= 2;
                    return door ? B.AIR : partition(c, y);
                }
                if (Math.floorMod(u, roomW) == 0) return partition(c, y);
            }
            case OPEN, GUTTED -> {
                int mu = Math.floorMod(u, 6), mv = Math.floorMod(v, 6);
                if (mu == 3 && mv == 3) return pal.trim;
                if (r == 3) {
                    if ((Math.abs(mu - 3) <= 1 && mv == 3) || (mu == 3 && Math.abs(mv - 3) <= 1)) return pal.trim;
                    if (lay == Layout.OPEN && (Math.floorMod(u, 3) == 0 || Math.floorMod(v, 3) == 0)) return pal.slabTop;
                }
            }
            case ARCHIVE -> {
                if (Math.floorMod(u, 12) == 0 || Math.floorMod(u, 12) == 11) break;
                if (Math.floorMod(v, 3) == 1 && r <= 2) {
                    long h = c.hash(y, 9);
                    double q = Rng.unit(h);
                    if (q < 0.02) { Decor.container(c, y, B.BARREL, Decor.LOOT_ARCHIVE); return c.get(y); }
                    if (q < 0.25) return B.AIR;
                    return q < 0.55 ? B.CHISELED_BOOKSHELF : B.BOOKSHELF;
                }
            }
            case PARKING -> {
                if (Math.floorMod(u, 8) == 4 && Math.floorMod(v, 8) == 4) return pal.trim;
            }
            case WARD -> {
                if (r != 1 || vSpan < 6 || Math.floorMod(u, 3) != 1) break;
                int vc = vSpan / 2;
                if (Math.abs(v - vc) <= 1) break;
                boolean low = v < vc;
                int dv = low ? v : vSpan - v;
                if (dv > 1) break;
                if (!Rng.chance(Rng.hash(seed, f * 1000 + u, low ? 1 : 2), 0.8)) break;
                net.minecraft.core.Direction head = longX ? (low ? net.minecraft.core.Direction.NORTH : net.minecraft.core.Direction.SOUTH)
                        : (low ? net.minecraft.core.Direction.WEST : net.minecraft.core.Direction.EAST);
                Decor.bed(c, y, kind == Decor.RoomKind.HOSPITAL, head, dv == 0);
                return c.get(y);
            }
            case URNS -> {
                if (Math.floorMod(u, 10) == 0) break;
                if (Math.floorMod(v, 3) == 1) {
                    if (r == 2 && Math.floorMod(u, 2) == 0) {
                        long h = c.hash(y, 10);
                        return Rng.chance(h, 0.55) ? B.POTTED_DEAD_BUSH : Rng.chance(h >>> 8, 0.4) ? B.CANDLE : B.AIR;
                    }
                    return pal.wall;
                }
            }
            case STORAGE -> {
                if (Math.floorMod(u, 10) == 0 || Math.floorMod(u, 10) == 9) break;
                if (Math.floorMod(v, 3) == 1 && r <= 2) {
                    long h = c.hash(y, 9);
                    double q = Rng.unit(h);
                    if (q < 0.03) { Decor.container(c, y, B.BARREL, Decor.LOOT_INDUSTRIAL); return c.get(y); }
                    if (q < 0.30) return B.AIR;
                    if (q < 0.45) return B.SMOOTH_STONE;
                    if (q < 0.52) return B.POLISHED_ANDESITE;
                    return B.BARREL;
                }
            }
        }
        return B.AIR;
    }

    void mural() {
        long h = Rng.hash(seed, 0x3A2);
        int side = Rng.range(h, 0, 3), i = idx(1);
        int len = (side == W || side == E) ? z1[i] - z0[i] + 1 : x1[i] - x0[i] + 1;
        if (len < 20) return;
        muralSide = side;
        muralA0 = (len - 16) / 2;
        muralKind = Rng.range(h >>> 8, 0, 2);
    }

    static final BlockState[] MOSAIC = {B.GRAY_CONCRETE, B.WHITE_CONCRETE_BLOCK, B.SMOOTH_STONE, B.POLISHED_ANDESITE, B.ANDESITE};

    BlockState mosaic(int i, int j) {
        double x = i - 7.5, y = j - 7.5;
        double rr = Math.sqrt(x * x + y * y);
        return switch (muralKind) {
            case 0 -> {
                if (rr < 3) yield MOSAIC[1];
                if (rr < 4.2) yield MOSAIC[0];
                int sector = (int) Math.floor((Math.atan2(y, x) + Math.PI) / (Math.PI / 8));
                yield (sector & 1) == 0 ? MOSAIC[2] : MOSAIC[1];
            }
            case 1 -> {
                int band = (int) ((Math.abs(x) + Math.abs(y)) / 2);
                yield MOSAIC[band % 4 == 0 ? 0 : band % 4 == 1 ? 1 : band % 4 == 2 ? 3 : 4];
            }
            default -> {
                if (rr < 4) yield MOSAIC[0];
                int stripe = Math.floorDiv(i + j, 3) % 3;
                yield MOSAIC[stripe == 0 ? 2 : stripe == 1 ? 1 : 4];
            }
        };
    }

    Layout layoutOf(int f) {
        if (f < -3 && deepLayout != null) return deepLayout;
        if (layout == Layout.PARKING || f < 0) return layout == Layout.PARKING ? Layout.PARKING : Layout.OPEN;
        if (layout == Layout.WARD || layout == Layout.STORAGE || layout == Layout.URNS || layout == Layout.CONTROL) return layout;
        long h = Rng.hash(seed, f, 31);
        double u = Rng.unit(h);
        if (u < decay * 0.3) return Layout.GUTTED;
        if (layout == Layout.CELLS && u > 0.85) return Layout.OPEN;
        if (layout == Layout.ARCHIVE && u > 0.6) return Layout.CELLS;
        if (layout == Layout.ARCHIVE && u > 0.45) return Layout.CONTROL;
        if (layout == Layout.OPEN && u > 0.82) return Layout.CONTROL;
        return layout;
    }

    BlockState partition(Column c, int y) {
        return Rng.chance(c.hash(y, 4), decay * 0.15) ? B.AIR : pal.wall;
    }

    long livedRoom(int f, int ux, int uz, Layout lay, int coreDist) {
        if (f < 0 || lay != Layout.CELLS || kind != Decor.RoomKind.HOME || coreDist <= 2) return 0;
        int i = idx(f), d = depth(f);
        int ix0 = x0[i] + d + 1, ix1 = x1[i] - d - 1, iz0 = z0[i] + d + 1, iz1 = z1[i] - d - 1;
        boolean longX = (ix1 - ix0) >= (iz1 - iz0);
        int u = longX ? ux - ix0 : uz - iz0, v = longX ? uz - iz0 : ux - ix0;
        int vSpan = longX ? iz1 - iz0 : ix1 - ix0;
        int side;
        if (vSpan < 4) return 0;
        if (vSpan < 8) { if (v <= 2) return 0; side = 1; }
        else { int vc = vSpan / 2; if (Math.abs(v - vc) <= 2) return 0; side = v > vc ? 1 : 0; }
        long key = Rng.hash(seed ^ 0x11FEDL, f, Math.floorDiv(u, roomW) * 2 + side);
        return Rng.chance(key, livedShare) ? key | 1 : 0;
    }

    void furnish(Column c, int f, int ux, int uz, int F, int coreDist) {
        int y1 = F + 1, y3 = F + 3;
        if (f + 1 < floors && hole(c, f + 1) && coreDist > 2) {
            if (c.isAir(y1)) c.set(y1, Rng.chance(c.hash(y1, 6), 0.6) ? B.COBBLE : B.GRAVEL);
            return;
        }
        Layout lay = layoutOf(f);
        if (c.isAir(y1) && !c.get(F).isAir()) {
            Decor.RoomKind k = f < 0 ? Decor.RoomKind.BUNKER : (coreDist <= 2 ? Decor.RoomKind.LOBBY : kind);
            if (lay == Layout.ARCHIVE) k = Decor.RoomKind.ARCHIVE;
            long room = livedRoom(f, ux, uz, lay, coreDist);
            if (lay == Layout.PARKING) {
                car(c, f, ux, uz, F);
            } else if (lay == Layout.CONTROL && coreDist > 2) {
                int i = idx(f);
                boolean longX = (x1[i] - x0[i]) >= (z1[i] - z0[i]);
                Decor.console(c, y1, longX ? ux - x0[i] : uz - z0[i], longX ? uz - z0[i] : ux - x0[i], Rng.hash(seed, f, 0xC0));
            } else if (room != 0) {
                Decor.livedItem(c, y1, room);
            } else {
                Decor.floorItem(c, y1, k, lay == Layout.GUTTED ? decay + 0.4 : decay);
            }
        }
        if (c.isAir(y3) && lay != Layout.PARKING) Decor.ceiling(c, y3, ux - (coreX >= 0 ? coreX : 0), uz - (coreZ >= 0 ? coreZ : 0), lit ? -1 : decay);
    }

    void car(Column c, int f, int ux, int uz, int F) {
        if (Rng.chance(c.hash(F, 19), 0.004)) c.set(F + 1, B.ANDESITE_SLAB);
    }

    void terrace(Column c, int f, int ux, int uz, int F) {
        int e = edge(f - 1, ux, uz);
        if (e == 0) {
            c.set(F + 1, pal.parapet);
        } else if (e == 1 && f < floors && Rng.chance(c.hash(F, 8), 0.08)) {
            c.set(F + 1, B.GRAVEL);
        }
    }

    void fin(Column c, int f, int ux, int uz, int F) {
        Facade fac = facade(f);
        boolean isFin = fac == Facade.FINS || (fac == Facade.BLANK && ribs);
        if (!isFin) return;
        int i = idx(f);
        int dx = Math.max(Math.max(x0[i] - ux, ux - x1[i]), 0);
        int dz = Math.max(Math.max(z0[i] - uz, uz - z1[i]), 0);
        if (!((dx == 1 && dz == 0) || (dx == 0 && dz == 1))) return;
        int a = dx == 1 ? uz - z0[i] : ux - x0[i];
        int spacing = fac == Facade.FINS ? 3 : 4;
        if (Math.floorMod(a, spacing) != 0) return;
        for (int r = 0; r <= 3; r++) c.set(F + r, pal.wallAt(c.seed, c.x, F + r, c.z, decay));
    }

    void balconyCell(Column c, int f, int ux, int uz, int F) {
        int i = idx(f);
        int dx = Math.max(Math.max(x0[i] - ux, ux - x1[i]), 0);
        int dz = Math.max(Math.max(z0[i] - uz, uz - z1[i]), 0);
        if (dx > 0 && dz > 0) return;
        int out = Math.max(dx, dz);
        int a = dx > 0 ? uz - z0[i] : ux - x0[i];
        int depth = (Math.floorDiv(a, 3) & 1) == 0 ? balcony : balcony - 1;
        if (depth <= 0 || out > depth) return;
        c.set(F, pal.trim);
        if (out == depth) c.set(F + 1, pal.parapet);
    }

    void roof(Column c, int ux, int uz) {
        int R = floorY(floors);
        for (int[] b : roofBoxes) {
            if (ux >= b[0] && ux <= b[2] && uz >= b[1] && uz <= b[3]) {
                boolean shell = ux == b[0] || ux == b[2] || uz == b[1] || uz == b[3];
                for (int y = R + 1; y <= R + b[4]; y++) {
                    if (y == R + b[4]) c.set(y, pal.trim);
                    else if (shell) c.set(y, pal.wallAt(c.seed, c.x, y, c.z, decay));
                }
            }
        }
        if (ux == antennaX && uz == antennaZ) {
            for (int y = R + 1; y <= R + antennaH; y++) c.set(y, y <= R + 2 ? pal.trim : B.IRON_BARS);
            if (beacon) {
                c.set(R + antennaH + 1, B.SHROOMLIGHT);
                c.set(R + antennaH + 2, B.RED_GLASS);
            }
        }
        if (roofUse == 0) return;
        boolean inCore = coreX >= 0 && ux >= coreX - 1 && ux <= coreX + 5 && uz >= coreZ - 1 && uz <= coreZ + 5;
        int i = idx(floors - 1);
        if (inCore || !in(floors - 1, ux, uz) || edge(floors - 1, ux, uz) < 2) return;
        if (roofUse == 1 || roofUse == 3) {
            int gx0 = x0[i] + 2, gz0 = z0[i] + 2, gx1 = Math.min(x1[i] - 2, gx0 + 10), gz1 = Math.min(z1[i] - 2, gz0 + 8);
            if (ux < gx0 || ux > gx1 || uz < gz0 || uz > gz1 || !c.isAir(R + 1)) return;
            if (roofUse == 3) {
                c.set(R + 4, B.GLASS);
                boolean rim = ux == gx0 || ux == gx1 || uz == gz0 || uz == gz1;
                if (rim) {
                    boolean door = ux == gx0 && uz == (gz0 + gz1) / 2;
                    for (int y = R + 1; y <= R + 3; y++) c.set(y, door && y <= R + 2 ? B.AIR : B.GLASS);
                    return;
                }
                if (ux == gx0 + 1 && uz == gz0 + 1) { c.set(R + 3, B.LANTERN_HANGING); }
            }
            if (ux == gx1 && uz == gz1) { c.set(R + 1, B.COMPOSTER); return; }
            if (ux == gx1 && uz == gz0) { c.set(R + 1, B.WATER_CAULDRON); return; }
            if (Math.floorMod(ux - gx0, 3) == 0) return;
            c.set(R, B.FARMLAND);
            long h = c.hash(R, 95);
            if (Rng.chance(h, 0.8)) c.set(R + 1, B.crop(h >>> 4));
        } else {
            Camp.fill(c, ux - (x1[i] - 5), uz - (z1[i] - 5), R, Rng.hash(seed, 0xCA4F));
        }
    }

    boolean inFootprint(int ux, int uz) {
        int m = balcony + 1;
        for (int i = 0; i < floors + basement; i++)
            if (ux >= x0[i] - m && ux <= x1[i] + m && uz >= z0[i] - m && uz <= z1[i] + m) return true;
        return coreX >= 0 && ux >= coreX && ux < coreX + 5 && uz >= coreZ && uz < coreZ + 5;
    }

    void ruin(Column c, int ux, int uz) {
        if (!inFootprint(ux, uz)) return;
        int cut = ruinY + (int) (Rng.fbm(seed ^ 0xC011A75EL, c.x / 7.0, c.z / 7.0) * ruinAmp);
        int top = topY() + 3;
        for (int y = Math.max(cut, GROUND + 1); y <= top; y++) c.set(y, B.AIR);
        if (cut - 1 > GROUND && !c.isAir(cut - 1) && Rng.chance(c.hash(cut, 15), 0.5)) c.set(cut - 1, B.COBBLE);
    }
}
