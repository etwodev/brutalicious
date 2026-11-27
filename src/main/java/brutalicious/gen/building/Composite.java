package brutalicious.gen.building;

import brutalicious.gen.B;
import brutalicious.gen.Column;
import brutalicious.gen.Palette;

import java.util.ArrayList;
import java.util.List;

public final class Composite implements Building {
    final String name;
    final List<Mass> parts = new ArrayList<>();
    final List<int[]> links = new ArrayList<>();
    final Palette pal;

    public Composite(String name, Palette pal) {
        this.name = name;
        this.pal = pal;
    }

    public Composite add(Mass m) { parts.add(m); return this; }

    public Composite link(int x0, int z0, int x1, int z1, int f, boolean alongX) {
        links.add(new int[]{x0, z0, x1, z1, f, alongX ? 1 : 0});
        return this;
    }

    @Override public String name() { return name; }

    @Override
    public void fill(Column c, int ux, int uz) {
        for (Mass m : parts) m.fill(c, ux, uz);
        int fallen = Integer.MAX_VALUE;
        for (Mass m : parts) fallen = Math.min(fallen, m.ruinY);
        for (int[] l : links) {
            if (ux < l[0] || ux > l[2] || uz < l[1] || uz > l[3]) continue;
            if (Building.floorY(l[4]) + 4 >= fallen) continue;
            boolean alongX = l[5] == 1;
            int across = alongX ? uz : ux, a0 = alongX ? l[1] : l[0], a1 = alongX ? l[3] : l[2];
            int along = alongX ? ux : uz;
            boolean side = across == a0 || across == a1;
            int F = Building.floorY(l[4]);
            c.set(F, side ? pal.trim : pal.floor);
            for (int r = 1; r <= 3; r++) {
                if (side) c.set(F + r, (r == 2 && (along & 1) == 0) ? pal.window : pal.wallAt(c.seed, c.x, F + r, c.z, 0.1));
                else c.set(F + r, B.AIR);
            }
            c.set(F + 4, pal.trim);
        }
        for (int i = 0; i < boxes.size(); i++) {
            int[] b = boxes.get(i);
            if (b[4] >= fallen) continue;
            if (ux >= b[0] && ux <= b[2] && uz >= b[1] && uz <= b[3]) c.fill(b[4], b[5], boxStates.get(i));
        }
    }

    final java.util.List<int[]> boxes = new java.util.ArrayList<>();
    final java.util.List<net.minecraft.world.level.block.state.BlockState> boxStates = new java.util.ArrayList<>();

    public Composite box(int x0, int z0, int x1, int z1, int y0, int y1, net.minecraft.world.level.block.state.BlockState s) {
        boxes.add(new int[]{Math.min(x0, x1), Math.min(z0, z1), Math.max(x0, x1), Math.max(z0, z1), y0, y1});
        boxStates.add(s);
        return this;
    }

    @Override
    public boolean occupied(int ux, int uz, int y) {
        for (Mass m : parts) if (m.occupied(ux, uz, y)) return true;
        return false;
    }

    @Override
    public void openDeckDoors(int f) {
        for (Mass m : parts) if (m.floors > f) m.deckDoorFloor = f;
    }

    @Override
    public void openRoofDoors(int f) {
        for (Mass m : parts) if (m.floors > f) m.roofDoorFloor = f;
    }

    @Override
    public boolean ruined() {
        for (Mass m : parts) if (m.ruinY != Integer.MAX_VALUE) return true;
        return false;
    }

    @Override
    public int floors() {
        int n = 0;
        for (Mass m : parts) n = Math.max(n, m.floors);
        return n;
    }

    Mass reachPart(int side, int f) {
        Mass best = null;
        int br = Integer.MAX_VALUE;
        for (Mass m : parts) {
            int r = m.reach(side, f);
            if (r >= 0 && r < br) { br = r; best = m; }
        }
        return best;
    }

    @Override
    public int reach(int side, int f) {
        Mass m = reachPart(side, f);
        return m == null ? -1 : m.reach(side, f);
    }

    @Override
    public int facadeDepth(int side, int f) {
        Mass m = reachPart(side, f);
        return m == null ? 0 : m.depth(f);
    }

    @Override
    public int basementReach(int side) {
        Mass m = reachPart(side, -3);
        return m == null || m.basement < 3 ? -1 : m.reach(side, -3);
    }
}
