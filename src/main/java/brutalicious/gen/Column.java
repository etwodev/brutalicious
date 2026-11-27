package brutalicious.gen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Column {
    public static final int MIN_Y = -64;
    public static final int HEIGHT = 512;
    public static final int MAX_Y = MIN_Y + HEIGHT - 1;

    public interface Sink {
        void blockEntity(CompoundTag tag);
        void postProcess(BlockPos pos);
    }

    record Pending(int y, BlockState state, CompoundTag tag) {}

    public final BlockState[] states = new BlockState[HEIGHT];
    public int x, z;
    public long seed;
    public Sink sink;
    public boolean stairs;
    private final List<Pending> pending = new ArrayList<>();

    public void reset(long seed, int x, int z, Sink sink) {
        this.seed = seed;
        this.x = x;
        this.z = z;
        this.sink = sink;
        this.stairs = false;
        Arrays.fill(states, B.AIR);
        pending.clear();
    }

    public void set(int y, BlockState s) {
        if (y >= MIN_Y && y <= MAX_Y) states[y - MIN_Y] = s;
    }

    public void fill(int y0, int y1, BlockState s) {
        int a = Math.max(y0, MIN_Y), b = Math.min(y1, MAX_Y);
        for (int y = a; y <= b; y++) states[y - MIN_Y] = s;
    }

    public BlockState get(int y) {
        return (y >= MIN_Y && y <= MAX_Y) ? states[y - MIN_Y] : B.AIR;
    }

    public boolean isAir(int y) {
        return get(y).isAir();
    }

    public void setConnected(int y, BlockState s) {
        set(y, s);
        if (sink != null) pending.add(new Pending(y, s, null));
    }

    public void blockEntity(int y, BlockState s, CompoundTag tag) {
        set(y, s);
        if (sink != null) pending.add(new Pending(y, s, tag));
    }

    public void shift(int dh) {
        if (dh == 0) return;
        int s = Terrain.SPLIT - MIN_Y;
        if (dh > 0) {
            for (int i = HEIGHT - 1; i >= s; i--) states[i] = i - dh >= s ? states[i - dh] : states[s - 1];
        } else {
            for (int i = s; i < HEIGHT; i++) states[i] = i - dh < HEIGHT ? states[i - dh] : B.AIR;
        }
        List<Pending> moved = new ArrayList<>(pending.size());
        for (Pending p : pending) {
            if (p.y < Terrain.SPLIT) moved.add(p);
            else if (p.y + dh >= Terrain.SPLIT) moved.add(new Pending(p.y + dh, p.state, p.tag));
        }
        pending.clear();
        pending.addAll(moved);
    }

    public void flush() {
        if (sink == null) return;
        for (Pending p : pending) {
            if (p.y < MIN_Y || p.y > MAX_Y || states[p.y - MIN_Y] != p.state) continue;
            if (p.tag == null) {
                sink.postProcess(new BlockPos(x, p.y, z));
            } else {
                p.tag.putInt("x", x);
                p.tag.putInt("y", p.y);
                p.tag.putInt("z", z);
                sink.blockEntity(p.tag);
            }
        }
        pending.clear();
    }

    public long hash(int y, int salt) {
        return Rng.hash(seed ^ (salt * 0x9E3779B97F4A7C15L), x, y, z);
    }
}
