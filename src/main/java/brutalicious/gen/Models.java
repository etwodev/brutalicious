package brutalicious.gen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class Models {
    private Models() {}

    static final BlockState PLATE = B.LIGHT_GRAY_CONCRETE, DARK = B.POLISHED_DEEPSLATE, MID = B.GRAY_CONCRETE,
            JOINT = B.POLISHED_ANDESITE, GUN = B.POLISHED_BLACKSTONE, GLASS = B.TINTED_GLASS;

    static Voxels rocket(int h, boolean gantry) {
        Voxels v = new Voxels();
        v.cylinder(0, 0, 1, h, 3, PLATE, true);
        for (int y = 10; y < h; y += 10) v.cylinder(0, 0, y, y, 3, B.BLACK_CONCRETE, true);
        for (int y = h + 1; y <= h + 8; y++) v.cylinder(0, 0, y, y, Math.max(0.4, 3 * (1 - (y - h) / 9.0)), PLATE, false);
        v.cylinder(0, 0, -3, 0, 1.2, GUN, false);
        v.cylinder(-2, 1, -2, 0, 0.7, GUN, false);
        v.cylinder(2, 1, -2, 0, 0.7, GUN, false);
        for (int[] f : new int[][]{{4, 0}, {-4, 0}, {0, 4}, {0, -4}})
            v.box(f[0] == 0 ? 0 : f[0], 1, f[1] == 0 ? 0 : f[1], f[0] == 0 ? 0 : f[0] + Integer.signum(f[0]) * 2, 7, f[1] == 0 ? 0 : f[1] + Integer.signum(f[1]) * 2, PLATE);
        int cabin = h - 6;
        for (int y = 2; y < cabin; y++) v.set(0, y, -2, B.ladder(Direction.SOUTH));
        v.cylinder(0, 0, cabin, cabin, 2, B.SMOOTH_STONE_SLAB_TOP, false);
        v.set(0, cabin, -2, B.ladder(Direction.SOUTH));
        v.set(0, cabin + 1, 1, Blocks.OBSERVER.defaultBlockState().setValue(DirectionalBlock.FACING, Direction.NORTH));
        v.set(-1, cabin + 1, 0, B.stair(Blocks.POLISHED_BLACKSTONE_STAIRS, Direction.WEST));
        v.box(3, cabin + 1, 0, 3, cabin + 2, 0, B.AIR);
        if (gantry) {
            int top = h + 4;
            for (int x = 6; x <= 11; x++) for (int z = -3; z <= 2; z++) {
                boolean corner = (x == 6 || x == 11) && (z == -3 || z == 2);
                boolean edge = x == 6 || x == 11 || z == -3 || z == 2;
                for (int y = 0; y <= top; y++) {
                    if (corner) v.set(x, y, z, JOINT);
                    else if (y % 8 == 0 && y > 0) v.set(x, y, z, B.SMOOTH_STONE_SLAB_TOP);
                    else if (edge && y % 4 == 2) v.set(x, y, z, B.IRON_BARS);
                }
            }
            for (int y = 0; y <= top; y++) { v.set(8, y, -3, JOINT); v.set(8, y, -2, B.ladder(Direction.SOUTH)); }
            for (int y = top + 1; y <= top + 10; y++) v.set(8, y, -1, B.IRON_BARS);
            for (int x = 4; x <= 5; x++) {
                for (int z = -1; z <= 1; z++) v.set(x, cabin, z, B.SMOOTH_STONE_SLAB_TOP);
                v.set(x, cabin + 1, -2, B.IRON_BARS);
                v.set(x, cabin + 1, 2, B.IRON_BARS);
            }
            v.box(6, cabin + 1, -1, 6, cabin + 2, 1, B.AIR);
            v.box(6, cabin, -1, 6, cabin, 1, B.SMOOTH_STONE_SLAB_TOP);
        }
        return v;
    }

    static final Voxels[] ROCKET = new Voxels[8];
    static final Voxels[] ROCKET_DOWN = new Voxels[4];
    static {
        for (int r = 0; r < 4; r++) {
            ROCKET[r] = rocket(44, true).rotated(r).freeze();
            ROCKET[4 + r] = rocket(60, true).rotated(r).freeze();
            ROCKET_DOWN[r] = rocket(30, false).damaged(0xD0E5L + r, 0.1).toppled().rotated(r).freeze();
        }
    }

    public static Voxels rocket(int variant, int rot) { return ROCKET[(variant & 1) * 4 + Math.floorMod(rot, 4)]; }

    public static Voxels fallenRocket(int rot) { return ROCKET_DOWN[Math.floorMod(rot, 4)]; }

    public static final Voxels SPHERE_TANK;
    static {
        Voxels v = new Voxels();
        v.sphere(0, 12, 0, 6, PLATE, true);
        for (int[] l : new int[][]{{4, 4}, {-4, 4}, {4, -4}, {-4, -4}}) v.box(l[0], 0, l[1], l[0], 9, l[1], JOINT);
        v.box(-6, 12, 0, 6, 12, 0, MID);
        for (int y = 0; y <= 18; y++) { v.set(8, y, 0, JOINT); v.set(9, y, 0, B.ladder(Direction.EAST)); }
        for (int x = 1; x <= 7; x++) v.set(x, 18, 0, B.SMOOTH_STONE_SLAB);
        v.set(0, 18, 0, B.AIR);
        SPHERE_TANK = v.freeze();
    }

    static Voxels dish() {
        Voxels v = new Voxels();
        v.box(-2, 0, -2, 2, 9, 2, MID);
        v.cylinder(0, 0, 10, 10, 3, JOINT, false);
        double cy = 17, s = Math.sqrt(0.5);
        v.box(0, 11, 0, 0, 16, 0, JOINT);
        for (double u = -10; u <= 10; u += 0.5) for (double w = -10; w <= 10; w += 0.5) {
            if (u * u + w * w > 100) continue;
            double depth = (u * u + w * w) / 32.0;
            double x = u, y = cy + w * s + depth * s, z = -w * s + depth * s;
            v.set((int) Math.round(x), (int) Math.round(y), (int) Math.round(z), PLATE);
        }
        for (double t = 0; t <= 8; t += 0.5) v.set(0, (int) Math.round(cy + t * s), (int) Math.round(t * s), B.IRON_BARS);
        return v;
    }

    static final Voxels[] DISH = new Voxels[4];
    static { for (int r = 0; r < 4; r++) DISH[r] = dish().rotated(r).freeze(); }

    public static Voxels dish(int rot) { return DISH[Math.floorMod(rot, 4)]; }

    public static final Voxels TRANSFORMER;
    static {
        Voxels v = new Voxels();
        v.box(-2, 0, -2, 2, 0, 2, B.SMOOTH_STONE);
        v.box(-1, 1, -1, 1, 4, 1, MID);
        for (int y = 1; y <= 3; y++) { v.box(-2, y, -1, -2, y, 1, B.IRON_BARS); v.box(2, y, -1, 2, y, 1, B.IRON_BARS); }
        for (int x = -1; x <= 1; x++) v.set(x, 5, 0, B.END_ROD);
        TRANSFORMER = v.freeze();
    }

    static Voxels lattice(int h) {
        Voxels v = new Voxels();
        for (int y = 0; y <= h; y++) {
            int s = (int) Math.round(2.0 * (1 - y / (double) h));
            for (int[] c : new int[][]{{s, s}, {-s, s}, {s, -s}, {-s, -s}}) v.set(c[0], y, c[1], y % 6 == 0 ? JOINT : B.IRON_BARS);
            if (y % 6 == 3 && s > 0) for (int i = -s; i <= s; i++) { v.set(i, y, s, B.IRON_BARS); v.set(i, y, -s, B.IRON_BARS); }
        }
        for (int i = -4; i <= 4; i++) { v.set(0, h - 4, i, JOINT); v.set(i, h - 1, 0, JOINT); }
        for (int i : new int[]{-4, 4}) { v.set(0, h - 5, i, B.END_ROD); v.set(i, h - 2, 0, B.END_ROD); }
        return v;
    }

    public static final int PYLON_H = 30;
    public static final Voxels PYLON = lattice(PYLON_H).freeze();

    public static final Voxels MISSILE = rocket(22, false).freeze();

    static Voxels tank(boolean wreck) {
        Voxels v = new Voxels();
        for (int s : new int[]{-1, 1}) {
            int x = 3 * s;
            v.box(x, 0, -5, x, 1, 5, GUN);
            for (int z = -4; z <= 4; z += 2) v.set(x, 0, z, DARK);
            v.box(x, 2, -5, x, 2, 5, B.SMOOTH_STONE_SLAB);
        }
        v.box(-2, 1, -5, 2, 2, 4, MID);
        v.box(-2, 2, 5, 2, 2, 5, B.SMOOTH_STONE_SLAB);
        v.box(-2, 3, -4, 2, 3, -4, B.IRON_BARS);
        int tz = wreck ? -1 : 0, tx = wreck ? 1 : 0;
        v.box(-2 + tx, 3, -2 + tz, 2 + tx, 4, 1 + tz, MID);
        v.box(-1 + tx, 5, -1 + tz, 1 + tx, 5, 0 + tz, B.SMOOTH_STONE_SLAB);
        v.set(-1 + tx, 5, -1 + tz, B.IRON_TRAPDOOR);
        if (wreck) v.line(1 + tx, 4, 2 + tz, 6, 2, 7, GUN);
        else v.box(0, 4, 2, 0, 4, 9, GUN);
        return v;
    }

    static final Voxels[] TANK = new Voxels[4], TANK_WRECK = new Voxels[4];
    static {
        for (int r = 0; r < 4; r++) {
            TANK[r] = tank(false).rotated(r).freeze();
            TANK_WRECK[r] = tank(true).damaged(0x7A4CL + r, 0.12).rotated(r).freeze();
        }
    }

    public static Voxels tank(int rot, boolean wreck) { return (wreck ? TANK_WRECK : TANK)[Math.floorMod(rot, 4)]; }

    static Voxels submarine() {
        Voxels v = new Voxels();
        for (int z = -15; z <= 15; z++) {
            double r = 2.6 * Math.min(1, 0.35 + (15 - Math.abs(z)) / 5.0);
            v.cylinderX(z, z, 0, 0, r, MID, false);
        }
        Voxels hull = v.rotated(1);
        hull.box(-1, 3, -3, 1, 6, 2, MID);
        hull.box(-1, 7, -3, 1, 7, 2, B.SMOOTH_STONE_SLAB);
        hull.box(0, 8, 0, 0, 10, 0, B.IRON_BARS);
        hull.box(0, 8, -1, 0, 9, -1, B.IRON_BARS);
        hull.box(0, 3, 6, 0, 3, 6, DARK);
        hull.box(0, 4, 6, 0, 4, 9, GUN);
        hull.box(0, 3, -12, 0, 3, 11, B.SMOOTH_STONE_SLAB);
        return hull;
    }

    public static final Voxels SUBMARINE = submarine().freeze();
    public static final Voxels SUBMARINE_SUNK = submarine().damaged(0x5B5BL, 0.15).freeze();

    static Voxels wheel() {
        Voxels v = new Voxels();
        int hub = 22;
        double R = 18;
        for (double a = 0; a < 360; a += 1.2) {
            double r = Math.toRadians(a);
            int y = (int) Math.round(hub + R * Math.sin(r)), z = (int) Math.round(R * Math.cos(r));
            v.set(-1, y, z, JOINT);
            v.set(1, y, z, JOINT);
        }
        for (int i = 0; i < 12; i++) {
            double r = Math.toRadians(i * 30);
            double y = hub + R * Math.sin(r), z = R * Math.cos(r);
            v.line(-1, hub, 0, -1, y, z, B.ANDESITE_WALL);
            v.line(1, hub, 0, 1, y, z, B.ANDESITE_WALL);
            v.box(-1, (int) Math.round(y), (int) Math.round(z), 1, (int) Math.round(y), (int) Math.round(z), JOINT);
            int cy = (int) Math.round(y) - 1, cz = (int) Math.round(z);
            v.hollow(-1, cy - 3, cz - 1, 1, cy, cz + 1, B.LIGHT_GRAY_CONCRETE);
            v.set(0, cy - 2, cz - 1, GLASS);
            v.set(0, cy - 2, cz + 1, GLASS);
        }
        v.box(-3, hub, 0, 3, hub, 0, DARK);
        for (int s : new int[]{-3, 3}) {
            v.line(s, hub, 0, s * 2, 0, -10, MID);
            v.line(s, hub, 0, s * 2, 0, 10, MID);
            v.line(s * 2, 6, -7, s * 2, 6, 7, MID);
        }
        return v;
    }

    public static final Voxels WHEEL = wheel().freeze();
    public static final Voxels WHEEL_FALLEN = wheel().damaged(0xFE77L, 0.08).toppled().freeze();

    static Voxels head() {
        Voxels v = new Voxels();
        v.ellipsoid(0, 8, 0, 6, 8, 6, B.LIGHT_GRAY_CONCRETE);
        v.ellipsoid(0, 8, 0, 5, 7, 5, B.AIR);
        v.cylinder(0, 0, 0, 2, 3.5, B.LIGHT_GRAY_CONCRETE, false);
        v.box(-5, 11, 5, 5, 12, 6, B.LIGHT_GRAY_CONCRETE);
        v.box(-1, 6, 6, 1, 10, 7, B.LIGHT_GRAY_CONCRETE);
        v.box(-3, 4, 5, 3, 4, 6, B.GRAY_CONCRETE);
        for (int s : new int[]{-1, 1}) v.box(s * 3 - 1, 9, 4, s * 3 + 1, 10, 6, B.AIR);
        v.cylinder(0, 0, 14, 16, 6.5, B.GRAY_CONCRETE, false);
        v.box(-6, 14, 6, 6, 14, 10, B.GRAY_CONCRETE);
        return v;
    }

    static Voxels fist() {
        Voxels v = new Voxels();
        v.cylinderX(0, 20, 3, 0, 3, B.LIGHT_GRAY_CONCRETE, false);
        v.box(21, 0, -4, 28, 8, 4, B.LIGHT_GRAY_CONCRETE);
        for (int y = 2; y <= 6; y += 2) v.box(28, y, -4, 28, y, 4, B.GRAY_CONCRETE);
        v.box(22, 7, -5, 26, 8, -5, B.LIGHT_GRAY_CONCRETE);
        for (int x = 0; x <= 2; x++) v.set(x, 3, 0, B.IRON_BARS);
        return v;
    }

    public static final Voxels HEAD_FALLEN = head().damaged(0x4EADL, 0.03).toppled().freeze();
    public static final Voxels FIST = fist().freeze();
}
