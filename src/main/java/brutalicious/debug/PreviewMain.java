package brutalicious.debug;

import brutalicious.gen.CityGen;
import brutalicious.gen.Column;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

import java.io.File;

public final class PreviewMain {
    public static void main(String[] args) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        String s = args.length > 1 ? args[1] : "brutalist";
        long seed;
        try { seed = Long.parseLong(s); } catch (NumberFormatException e) { seed = s.hashCode(); }
        final long fs = seed;
        String probe = System.getProperty("brutalicious.probe");
        if (probe != null) {
            String[] p = probe.split(",");
            if (p[0].equals("camp")) {
                int[] at = brutalicious.gen.Streets.findRoadCamp(fs);
                int y = Integer.parseInt(p[1]);
                System.out.println("camp at " + at[0] + "," + at[1]);
                for (int dz = -4; dz <= 4; dz++) {
                    StringBuilder sb = new StringBuilder();
                    for (int dx = -4; dx <= 4; dx++) {
                        Column c = new Column();
                        c.reset(fs, at[0] + dx, at[1] + dz, null);
                        CityGen.fill(c);
                        String n = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(c.get(y).getBlock()).getPath();
                        sb.append(String.format("%-14.14s", n));
                    }
                    System.out.println(sb);
                }
                return;
            }
            Column c = new Column();
            c.reset(fs, Integer.parseInt(p[0]), Integer.parseInt(p[1]), null);
            CityGen.fill(c);
            for (int y = Integer.parseInt(p[3]); y >= Integer.parseInt(p[2]); y--) System.out.println(y + " " + c.get(y));
            System.out.println(CityGen.describe(fs, c.x, c.z));
            return;
        }
        ThreadLocal<Column> tl = ThreadLocal.withInitial(Column::new);
        PreviewRenderer.renderAll(new PreviewRenderer.Src() {
            public Column column(int x, int z, Column.Sink sink) {
                Column c = tl.get();
                c.reset(fs, x, z, sink);
                CityGen.fill(c);
                c.flush();
                return c;
            }
            public long seed() { return fs; }
        }, new File(args[0]));
    }
}
