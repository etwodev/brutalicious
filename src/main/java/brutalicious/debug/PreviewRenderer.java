package brutalicious.debug;

import brutalicious.Brutalicious;
import brutalicious.gen.Column;
import brutalicious.gen.CityChunkGenerator;
import brutalicious.gen.CityPlan;
import brutalicious.gen.building.Archetypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.material.MapColor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.EnumMap;
import java.util.Map;

public final class PreviewRenderer {
    private PreviewRenderer() {}

    public interface Src {
        Column column(int x, int z, Column.Sink sink);
        long seed();
    }

    static Src of(CityChunkGenerator g) {
        return new Src() {
            public Column column(int x, int z, Column.Sink sink) { return g.column(x, z, sink); }
            public long seed() { return g.seed(); }
        };
    }

    public static void run(MinecraftServer server, String dir) {
        try {
            ServerLevel level = server.overworld();
            ChunkGenerator g = level.getChunkSource().getGenerator();
            if (!(g instanceof CityChunkGenerator cg)) {
                Brutalicious.LOG.error("Preview: overworld is not a Brutalist City world");
                return;
            }
            cg.setSeed(level.getSeed());
            renderAll(of(cg), new File(dir));
        } catch (Throwable t) {
            Brutalicious.LOG.error("Preview failed", t);
        } finally {
            server.halt(false);
        }
    }

    public static void renderAll(Src gen, File out) throws Exception {
        {
            out.mkdirs();
            long t0 = System.currentTimeMillis();
            census(gen.seed());
            snowCensus(gen);
            leakCheck(gen);
            topDown(gen, new File(out, "map.png"), -384, -384, 768);
            iso(gen, new File(out, "iso_spawn.png"), -48, -48, 176, 40);
            int[] m = findMinistry(gen.seed());
            if (m != null) iso(gen, new File(out, "iso_ministry.png"), m[0] - 24, m[1] - 24, 128, 40);
            section(gen, new File(out, "section.png"), -256, 40, 512);
            isoUnder(gen, new File(out, "iso_underground.png"), 70, 70, 120);
            for (String b : new String[]{"flak_tower", "winkel_towers", "hochbunker"}) {
                CityPlan.Plot fp = findPlot(gen.seed(), b);
                if (fp != null) iso(gen, new File(out, "iso_" + b + ".png"), fp.ox - 12, fp.oz - 12, 80, 40);
            }
            for (String b : new String[]{"water_tower", "congress", "spomenik"}) {
                CityPlan.Plot mp = findPlot(gen.seed(), b);
                if (mp != null) iso(gen, new File(out, "iso_" + b + ".png"), mp.ox - 4, mp.oz - 4, mp.px + 8, 50, 2);
            }
            for (String b : new String[]{"plaza_allotment", "plaza_civic"}) {
                CityPlan.Plot ap = findPlot(gen.seed(), b);
                if (ap != null) iso(gen, new File(out, "iso_" + b + ".png"), ap.ox, ap.oz, 40, 58, 4);
            }
            int[] camp = brutalicious.gen.Streets.findRoadCamp(gen.seed());
            if (camp != null) iso(gen, new File(out, "iso_camp.png"), camp[0] - 8, camp[1] - 8, 16, 58, 8);
            for (String b : new String[]{"barracks", "hospital", "depot", "panopticon", "cooling_tower", "factory", "silos", "stacks",
                    "tv_tower", "plaza_station", "plaza_lido", "plaza_stadium", "plaza_parade", "plaza_ground_zero", "columbarium", "plaza_cemetery",
                    "launch_pad", "tank_farm", "tracking", "assembly", "power_station", "substation", "solar",
                    "arcology", "the_keep", "plaza_farm", "plaza_greenhouse", "plaza_market",
                    "sub_pen", "missile_silo", "radar_array", "roundhouse", "observatory", "concert_hall", "library", "funfair",
                    "colossus", "sarcophagus", "decon", "seed_vault", "war_room", "tank_yard", "computer_hall", "waterworks",
                    "crematorium", "bell_tower", "mausoleum", "air_plant", "labyrinth", "reactor", "gasholder"}) {
                CityPlan.Plot np = findPlot(gen.seed(), b);
                if (np != null) iso(gen, new File(out, "new_" + b + ".png"), np.ox - 8, np.oz - 8, np.px + 16, 40, 2);
                else Brutalicious.LOG.info("Preview: no {} found nearby", b);
            }
            CityPlan.Plot sp = findPlot(gen.seed(), "plaza_station");
            if (sp != null) {
                zoom(gen, new File(out, "new_station_section.png"), sp.ox - 6, sp.oz + sp.pz / 2, sp.px + 12, 44, 70, 6);
                plan(gen, new File(out, "new_station_plan.png"), sp.ox - 6, sp.oz - 6, sp.px + 12, 53);
            }
            int[] ts = findTerraceStair(gen.seed());
            if (ts != null) {
                iso(gen, new File(out, "new_terrain_stairs.png"), ts[0] - 48, ts[1] - 48, 96, 40, 3);
                iso(gen, new File(out, "new_terrain_wide.png"), ts[0] - 160, ts[1] - 160, 320, 40, 1);
                zoomZ(gen, new File(out, "new_terrain_section.png"), ts[0], ts[1] - 40, 56, 30, 90, 7);
            }
            int[] st = findStairGate(gen.seed());
            if (st != null) iso(gen, new File(out, "new_stair_gate.png"), st[0] - 40, st[1] - 40, 80, 40, 3);
            if (st != null) for (int dx : new int[]{-5, 3, 4}) zoomZ(gen, new File(out, "new_stair_section_" + (dx + 5) + ".png"), st[0] + dx, st[1] - 30, 40, 55, 110, 8);
            modelSheet(new File(out, "new_models.png"));
            int[] mg = findMega(gen.seed());
            if (mg != null) {
                iso(gen, new File(out, "new_roof.png"), mg[0] - 96, mg[1] - 96, 192, 40, 1);
                zoom(gen, new File(out, "new_roof_section.png"), mg[0] - 60, mg[1] + 5, 120, 40, 330, 2);
            }
            int[] dw = findDeepJunction(gen.seed());
            if (dw != null) {
                plan(gen, new File(out, "new_deep_works_plan.png"), dw[0] - 64, dw[1] - 64, 160, -4);
                zoom(gen, new File(out, "new_deep_works_section.png"), dw[0] - 30, dw[1] + 40, 60, -40, 70, 5);
            }
            renderLayers(gen, out);
            zoneSchematic(gen.seed(), new File(out, "districts.png"), 6144);
            int[] wt = findWallTower(gen.seed());
            if (wt != null) iso(gen, new File(out, "new_compound_wall.png"), wt[0] - 40, wt[1] - 40, 96, 40, 2);
            zoneMap(gen, new File(out, "zones.png"), -1024, -1024, 2048);
            int[] open = findOpen(gen.seed());
            if (open != null) iso(gen, new File(out, "iso_street.png"), open[0], open[1], 128, 40);
            int[] tier = findTier(gen.seed());
            if (tier != null) iso(gen, new File(out, "iso_tier.png"), tier[0] - 88, tier[1] - 88, 176, 40);
            CityPlan.Plot tp = findPlot(gen.seed(), "point_block");
            if (tp != null) for (int dz = -1; dz <= 1; dz++)
                zoom(gen, new File(out, "core_" + (dz + 1) + ".png"), tp.ox, tp.oz + tp.pz / 2 + dz, tp.px, 40, 120, 5);
            CityPlan.Plot pp = findPlot(gen.seed(), "plaza_pit");
            if (pp != null) zoom(gen, new File(out, "pit.png"), pp.ox - 4, pp.oz + pp.pz / 2, pp.px + 8, -40, 70, 4);
            plan(gen, new File(out, "plan_floor2.png"), -128, -128, 256, 73);
            plan(gen, new File(out, "plan_ground.png"), -128, -128, 256, 65);
            plan(gen, new File(out, "plan_tunnels.png"), -128, -128, 256, 53);
            plan(gen, new File(out, "plan_cistern.png"), -128, -128, 256, -31);
            Brutalicious.LOG.info("Preview written to {} in {} ms", out.getAbsolutePath(), System.currentTimeMillis() - t0);
        }
    }

    static void snowCensus(Src gen) {
        int snow = 0, n = 0;
        for (int x = -2000; x < 2000; x += 41) for (int z = -2000; z < 2000; z += 41) {
            Column c = gen.column(x, z, null);
            int y = Column.MAX_Y; while (y > Column.MIN_Y && c.get(y).isAir()) y--;
            n++; if (c.get(y).is(Blocks.SNOW)) snow++;
        }
        Brutalicious.LOG.info("Preview census: {}% of columns are snow-topped", Math.round(100.0 * snow / n));
    }

    static void leakCheck(Src gen) {
        long seed = gen.seed();
        int rx = 0, rz = 0; boolean found = false;
        for (int r = 1; r < 12 && !found; r++) for (int a = -r; a <= r && !found; a++) for (int b = -r; b <= r && !found; b++) {
            if (brutalicious.gen.Zones.zone(seed, a * brutalicious.gen.Zones.REGION, b * brutalicious.gen.Zones.REGION) == brutalicious.gen.Zones.Zone.FLOODED) { rx = a; rz = b; found = true; }
        }
        if (!found) { Brutalicious.LOG.info("Leak check: no reservoir nearby"); return; }
        int span = brutalicious.gen.Zones.REGION * 64, x0 = rx * span - 12, z0 = rz * span - 12, n = span + 24, y0 = 24, y1 = 100;
        int pens = 0;
        for (int a = 0; a < brutalicious.gen.Zones.REGION; a++) for (int b = 0; b < brutalicious.gen.Zones.REGION; b++)
            if (CityPlan.plot(seed, rx * brutalicious.gen.Zones.REGION + a, rz * brutalicious.gen.Zones.REGION + b).type == Archetypes.Type.SUB_PEN) pens++;
        Brutalicious.LOG.info("Leak check: the reservoir holds {} submarine pens", pens);
        BlockState[][][] cols = sample(gen, x0, z0, n, y0, y1);
        int leaks = 0, water = 0; String first = null;
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) for (int k = 1; k < y1 - y0; k++) {
            if (!cols[i][j][k].is(Blocks.WATER)) continue;
            water++;
            boolean leak = cols[i][j][k - 1].isAir();
            int[][] nb = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int[] d : nb) {
                int a = i + d[0], b = j + d[1];
                if (a >= 0 && b >= 0 && a < n && b < n && cols[a][b][k].isAir()) leak = true;
            }
            if (leak) { leaks++; first = (first == null ? "" : first + " ") + (x0 + i) + "," + (y0 + k) + "," + (z0 + j); }
        }
        Brutalicious.LOG.info("Leak check: reservoir at district {},{}: {} water blocks, {} could flow (first at {})", rx, rz, water, leaks, first);
        try { iso(gen, new File("run/preview/new_reservoir.png"), rx * 256 - 20, rz * 256 - 20, 120, 40, 1); } catch (Exception e) { throw new RuntimeException(e); }
    }

    static void census(long seed) {
        Map<Archetypes.Type, Integer> counts = new EnumMap<>(Archetypes.Type.class);
        int maxFloors = 0;
        for (int cx = -12; cx < 12; cx++) for (int cz = -12; cz < 12; cz++) {
            CityPlan.Plot p = CityPlan.plot(seed, cx, cz);
            counts.merge(p.type, 1, Integer::sum);
            maxFloors = Math.max(maxFloors, p.building.floors());
        }
        Brutalicious.LOG.info("Preview census (24x24 cells): {} tallest={} floors", counts, maxFloors);
        int tier = 0, n = 0;
        for (int x = -4000; x < 4000; x += 37) for (int z = -4000; z < 4000; z += 37) { n++; if (brutalicious.gen.Tier.present(seed, x, z)) tier++; }
        Brutalicious.LOG.info("Preview census: upper tier covers {}% of the city", Math.round(100.0 * tier / n));
        int vac = 0, pl = 0, cells = 0;
        java.util.Map<brutalicious.gen.Zones.Zone, Integer> zones = new java.util.EnumMap<>(brutalicious.gen.Zones.Zone.class);
        for (int cx = -150; cx < 150; cx += 3) for (int cz = -150; cz < 150; cz += 3) {
            cells++;
            CityPlan.Plot p = CityPlan.plot(seed, cx, cz);
            if (p.type == Archetypes.Type.VACANT) vac++;
            if (CityPlan.plain(seed, p.ox + p.px / 2, p.oz + p.pz / 2)) pl++;
            zones.merge(p.zone, 1, Integer::sum);
        }
        Brutalicious.LOG.info("Preview census (wide): {}% vacant lots, {}% open plain, districts {}", Math.round(100.0 * vac / cells), Math.round(100.0 * pl / cells), zones);
    }

    static int[] findMinistry(long seed) {
        for (int r = 0; r < 16; r++) for (int cx = -r; cx <= r; cx++) for (int cz = -r; cz <= r; cz++) {
            if (Math.max(Math.abs(cx), Math.abs(cz)) != r) continue;
            CityPlan.Plot p = CityPlan.plot(seed, cx, cz);
            if (p.type == Archetypes.Type.MINISTRY) return new int[]{p.ox, p.oz};
        }
        return null;
    }

    static int[] findTerraceStair(long seed) {
        for (int r = 0; r < 40; r++) for (int cx = -r; cx <= r; cx++) for (int cz = -r; cz <= r; cz++) {
            if (Math.max(Math.abs(cx), Math.abs(cz)) != r) continue;
            if (Math.abs(brutalicious.gen.Terrain.segmentDelta(seed, cx, cz, true)) < 8) continue;
            int x = cx * 64 + 5, z = cz * 64 + 60;
            if (brutalicious.gen.Tier.present(seed, x, z) || brutalicious.gen.Tier.present(seed, x - 100, z - 100) || brutalicious.gen.Tier.present(seed, x + 100, z + 100)) continue;
            Brutalicious.LOG.info("Preview: street staircase at cell {},{} (drop {})", cx, cz, brutalicious.gen.Terrain.segmentDelta(seed, cx, cz, true));
            return new int[]{x, z};
        }
        return null;
    }

    static int[] findStairGate(long seed) {
        for (int r = 0; r < 40; r++) for (int cx = -r; cx <= r; cx++) for (int cz = -r; cz <= r; cz++) {
            if (Math.max(Math.abs(cx), Math.abs(cz)) != r) continue;
            if (!brutalicious.gen.Zones.wallX(seed, cx, cz)) continue;
            int ha = brutalicious.gen.Zones.height(seed, cx - 1, cz), hb = brutalicious.gen.Zones.height(seed, cx, cz);
            if (ha == hb || brutalicious.gen.Zones.zone(seed, cx - 1, cz) == brutalicious.gen.Zones.Zone.FLOODED || brutalicious.gen.Zones.zone(seed, cx, cz) == brutalicious.gen.Zones.Zone.FLOODED) continue;
            if (!brutalicious.gen.Zones.gateAt(seed, cx, cz, true)) continue;
            Brutalicious.LOG.info("Preview: stair gate at cell {},{} heights {} / {}", cx, cz, ha, hb);
            return new int[]{cx * 64 + 5, cz * 64 + 10 + 27};
        }
        return null;
    }

    static void modelSheet(File f) throws Exception {
        Object[][] items = {
                {brutalicious.gen.Models.rocket(0, 0), 4, 28},
                {brutalicious.gen.Models.SPHERE_TANK, 34, 30}, {brutalicious.gen.Models.dish(0), 58, 30},
                {brutalicious.gen.Models.PYLON, 70, 4}, {brutalicious.gen.Models.TRANSFORMER, 70, 18},
                {brutalicious.gen.Models.tank(0, false), 4, 62}, {brutalicious.gen.Models.tank(1, true), 18, 62},
                {brutalicious.gen.Models.SUBMARINE, 34, 62}, {brutalicious.gen.Models.MISSILE, 46, 62},
                {brutalicious.gen.Models.WHEEL, 62, 64}, {brutalicious.gen.Models.HEAD_FALLEN, 2, 80},
                {brutalicious.gen.Models.FIST, 24, 80}};
        Src s = new Src() {
            public Column column(int x, int z, Column.Sink sink) {
                Column c = new Column();
                c.reset(0, x, z, null);
                c.set(63, Blocks.SMOOTH_STONE.defaultBlockState());
                for (Object[] it : items) {
                    brutalicious.gen.Voxels v = (brutalicious.gen.Voxels) it[0];
                    int dx = x - (Integer) it[1], dz = z - (Integer) it[2];
                    if (v.covers(dx, dz)) v.apply(c, dx, dz, 64);
                }
                return c;
            }
            public long seed() { return 0; }
        };
        iso(s, f, -14, -14, 110, 60, 2);
    }

    static void renderLayers(Src gen, File out) throws Exception {
        long seed = gen.seed();
        CityPlan.Plot lab = findPlot(seed, "labyrinth");
        if (lab != null) {
            int x = lab.ox + lab.px / 2, z = lab.oz + lab.pz / 2;
            zoom(gen, new File(out, "new_blackout_section.png"), x - 120, z, 240, 50, 140, 2);
            plan(gen, new File(out, "new_blackout_plan.png"), x - 96, z - 96, 192, 66);
            plan(gen, new File(out, "new_labyrinth_plan.png"), lab.ox - 4, lab.oz - 4, lab.px + 8, 66);
            iso(gen, new File(out, "new_blackout_lid.png"), x - 128, z - 128, 256, 40, 1);
            int wallX = Math.floorDiv(lab.cx, brutalicious.gen.Zones.REGION) * brutalicious.gen.Zones.REGION * 64;
            zoom(gen, new File(out, "new_blackout_wall.png"), wallX - 40, z, 80, 50, 135, 4);
        } else Brutalicious.LOG.info("Preview: no Blackout district found nearby");
        int[] sh = null;
        for (int r = 0; r < 30 && sh == null; r++) for (int cx = -r; cx <= r && sh == null; cx++) for (int cz = -r; cz <= r; cz++) {
            if (Math.max(Math.abs(cx), Math.abs(cz)) != r) continue;
            if (brutalicious.gen.Shelters.present(seed, cx, cz) && brutalicious.gen.Shelters.present(seed, cx + 1, cz) && brutalicious.gen.Shelters.present(seed, cx, cz + 1)) { sh = new int[]{cx * 64, cz * 64}; break; }
        }
        if (sh != null) {
            plan(gen, new File(out, "new_shelters_plan.png"), sh[0] - 8, sh[1] - 8, 144, 21);
            plan(gen, new File(out, "new_shelters_room.png"), sh[0] - 4, sh[1] - 4, 52, 21);
        }
        int[] dw = findDeepJunction(seed);
        if (dw != null) {
            plan(gen, new File(out, "new_footings_plan.png"), dw[0] - 80, dw[1] - 80, 176, -52);
            zoomZ(gen, new File(out, "new_layers_section.png"), dw[0] + 4, dw[1] - 30, 64, -64, 70, 3);
        }
        int[] mg = findMega(seed);
        if (mg != null) {
            plan(gen, new File(out, "new_skyways_plan.png"), mg[0] - 100, mg[1] - 100, 200, 217);
            zoom(gen, new File(out, "new_skyways_section.png"), mg[0] - 100, mg[1], 200, 130, 310, 2);
            iso(gen, new File(out, "new_skyways_iso.png"), mg[0] - 40, mg[1] - 40, 80, 205, 3);
        }
        CityPlan.Plot ex = findPlot(seed, "sarcophagus");
        if (ex != null) iso(gen, new File(out, "new_exclusion_zone.png"), ex.ox - 100, ex.oz - 100, 240, 40, 1);
    }

    static void zoneSchematic(long seed, File f, int half) throws Exception {
        int n = half * 2 / 16;
        BufferedImage img = new BufferedImage(n, n, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) {
            int x = -half + i * 16, z = -half + j * 16;
            int rgb = switch (brutalicious.gen.Zones.zoneAt(seed, x, z)) {
                case CITY -> 0x9A9A9A; case COMPOUND -> 0xB04848; case INDUSTRIAL -> 0xC0A040; case FLOODED -> 0x4070C0;
                case BOMBED -> 0x7A5A50; case NECROPOLIS -> 0x6A5A90; case SPACEPORT -> 0x50A0A0; case POWER -> 0xA060C0;
                case SURVIVOR -> 0x40C040; case DARK -> 0x101014; case ABANDONED -> 0xD8D8C8;
            };
            int lx = Math.floorMod(x, 64), lz = Math.floorMod(z, 64);
            if (lx < 16 && lz < 16 && brutalicious.gen.Roof.megaAt(seed, Math.floorDiv(x, 64), Math.floorDiv(z, 64))) rgb = 0xFFFFFF;
            img.setRGB(i, j, rgb);
        }
        ImageIO.write(img, "png", f);
    }

    static int[] findMega(long seed) {
        for (int r = 0; r < 60; r++) for (int cx = -r; cx <= r; cx++) for (int cz = -r; cz <= r; cz++) {
            if (Math.max(Math.abs(cx), Math.abs(cz)) != r) continue;
            if (brutalicious.gen.Roof.megaAt(seed, cx, cz)) return new int[]{cx * 64 + 5, cz * 64 + 5};
        }
        return null;
    }

    static int[] findDeepJunction(long seed) {
        for (int r = 0; r < 20; r++) for (int cx = -r; cx <= r; cx++) for (int cz = -r; cz <= r; cz++) {
            if (Math.max(Math.abs(cx), Math.abs(cz)) != r) continue;
            if (CityPlan.major(cx) && CityPlan.major(cz)) return new int[]{cx * 64 + 8, cz * 64 + 8};
        }
        return null;
    }

    static int[] findWallTower(long seed) {
        for (int r = 0; r < 30; r++) for (int cx = -r; cx <= r; cx++) for (int cz = -r; cz <= r; cz++) {
            if (Math.max(Math.abs(cx), Math.abs(cz)) != r) continue;
            if (brutalicious.gen.Zones.towerAt(seed, cx, cz)) return new int[]{cx * 64 + 5, cz * 64 + 5};
        }
        return null;
    }

    static void zoneMap(Src gen, File f, int x0, int z0, int n) throws Exception {
        int s = n / 2;
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < s; i++) for (int j = 0; j < s; j++) {
            int x = x0 + i * 2, z = z0 + j * 2;
            Column c = gen.column(x, z, null);
            int y = Column.MAX_Y;
            while (y > Column.MIN_Y && c.get(y).isAir()) y--;
            int base = shade(colour(c.get(y)), 0.55 + Math.min(0.6, (y - 63) / 220.0));
            int tint = switch (brutalicious.gen.Zones.zoneAt(gen.seed(), x, z)) {
                case COMPOUND -> 0xC04040; case INDUSTRIAL -> 0xC0A040; case FLOODED -> 0x4070C0; case BOMBED -> 0x806060;
                case DARK -> 0x101018; case ABANDONED -> 0xA0A090; case SPACEPORT -> 0x60A0A0; case POWER -> 0xA060C0; case SURVIVOR -> 0x40C040;
                case NECROPOLIS -> 0x605080; default -> -1;
            };
            if (tint != -1) base = blend(base, tint, 0.35);
            img.setRGB(i, j, base);
        }
        ImageIO.write(img, "png", f);
    }

    static int blend(int a, int b, double t) {
        int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t), g = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t), bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
        return (r << 16) | (g << 8) | bl;
    }

    static int[] findOpen(long seed) {
        for (int r = 0; r < 24; r++) for (int cx = -r; cx <= r; cx++) for (int cz = -r; cz <= r; cz++) {
            if (Math.max(Math.abs(cx), Math.abs(cz)) != r) continue;
            boolean clear = true;
            for (int x = 0; x < 128 && clear; x += 8) for (int z = 0; z < 128 && clear; z += 8)
                if (brutalicious.gen.Tier.present(seed, cx * 64 + x, cz * 64 + z)) clear = false;
            if (clear) return new int[]{cx * 64, cz * 64};
        }
        return null;
    }

    static int[] findTier(long seed) {
        for (int r = 0; r < 24; r++) for (int cx = -r; cx <= r; cx++) for (int cz = -r; cz <= r; cz++) {
            if (Math.max(Math.abs(cx), Math.abs(cz)) != r) continue;
            if (brutalicious.gen.Tier.pillarAt(seed, cx, cz)) return new int[]{cx * CityPlan.CELL, cz * CityPlan.CELL};
        }
        return null;
    }

    static CityPlan.Plot findPlot(long seed, String name) {
        for (int r = 0; r < 60; r++) for (int cx = -r; cx <= r; cx++) for (int cz = -r; cz <= r; cz++) {
            if (Math.max(Math.abs(cx), Math.abs(cz)) != r) continue;
            CityPlan.Plot p = CityPlan.plot(seed, cx, cz);
            if (p.building.name().equals(name)) return p;
        }
        return null;
    }

    static void zoomZ(Src gen, File f, int x, int z0, int n, int y0, int y1, int k) throws Exception {
        int h = y1 - y0 + 1;
        BufferedImage img = new BufferedImage(n * k, h * k, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < n; i++) {
            Column c = gen.column(x, z0 + i, null);
            for (int y = y0; y <= y1; y++) {
                BlockState s = c.get(y);
                int rgb = s.isAir() ? 0x141414 : s.getBlock() instanceof net.minecraft.world.level.block.StairBlock ? 0xE09040 : colour(s);
                put(img, i * k, (y1 - y) * k, k, k, 0xFF000000 | rgb);
            }
        }
        ImageIO.write(img, "png", f);
    }

    static void zoom(Src gen, File f, int x0, int z, int n, int y0, int y1, int k) throws Exception {
        int h = y1 - y0 + 1;
        BufferedImage img = new BufferedImage(n * k, h * k, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < n; i++) {
            Column c = gen.column(x0 + i, z, null);
            for (int y = y0; y <= y1; y++) {
                BlockState s = c.get(y);
                int py = (y1 - y) * k;
                if (s.isAir()) { put(img, i * k, py, k, k, 0xFF141414); continue; }
                if (s.getBlock() instanceof net.minecraft.world.level.block.SlabBlock) {
                    boolean top = s.getValue(net.minecraft.world.level.block.SlabBlock.TYPE) == net.minecraft.world.level.block.state.properties.SlabType.TOP;
                    put(img, i * k, py, k, k, 0xFF141414);
                    put(img, i * k, py + (top ? 0 : k / 2), k, k / 2, 0xFFE09040);
                    continue;
                }
                put(img, i * k, py, k, k, 0xFF000000 | colour(s));
            }
        }
        ImageIO.write(img, "png", f);
    }

    static int colour(BlockState s) {
        if (s.isAir()) return 0;
        if (s.is(Blocks.WATER)) return 0x2A4A6A;
        if (s.is(Blocks.TINTED_GLASS) || s.is(Blocks.BLACK_STAINED_GLASS)) return 0x1E1E26;
        MapColor mc = s.getMapColor(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        if (mc == MapColor.NONE) return 0x8090A0;
        return mc.col;
    }

    static int shade(int rgb, double f) {
        int r = (int) Math.min(255, ((rgb >> 16) & 255) * f), g = (int) Math.min(255, ((rgb >> 8) & 255) * f), b = (int) Math.min(255, (rgb & 255) * f);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    static BlockState[][][] sample(Src gen, int x0, int z0, int n, int yMin, int yMax) {
        BlockState[][][] cols = new BlockState[n][n][];
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) {
            Column c = gen.column(x0 + i, z0 + j, null);
            BlockState[] a = new BlockState[yMax - yMin + 1];
            System.arraycopy(c.states, yMin - Column.MIN_Y, a, 0, a.length);
            cols[i][j] = a;
        }
        return cols;
    }

    static void topDown(Src gen, File f, int x0, int z0, int n) throws Exception {
        BufferedImage img = new BufferedImage(n, n, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) {
            Column c = gen.column(x0 + i, z0 + j, null);
            int y = Column.MAX_Y;
            while (y > Column.MIN_Y && c.get(y).isAir()) y--;
            double light = 0.55 + Math.min(0.6, (y - 63) / 220.0);
            img.setRGB(i, j, shade(colour(c.get(y)), light));
        }
        ImageIO.write(img, "png", f);
    }

    static void iso(Src gen, File f, int x0, int z0, int n, int yMin) throws Exception {
        iso(gen, f, x0, z0, n, yMin, 1);
    }

    static void iso(Src gen, File f, int x0, int z0, int n, int yMin, int s) throws Exception {
        int yMax = Column.MAX_Y;
        if (f.getName().contains("compound") || f.getName().contains("terrain")) yMax = 130;
        String nm = f.getName();
        if (nm.startsWith("new_") && !nm.contains("roof") && !nm.contains("skyways") && !nm.contains("arcology")) yMax = 136;
        BlockState[][][] cols = sample(gen, x0, z0, n, yMin, yMax);
        if (f.getName().contains("camp") || f.getName().contains("plaza")) {
            for (BlockState[][] row : cols) for (BlockState[] col : row) for (int k = 0; k < col.length; k++) if (col[k].is(Blocks.SNOW)) col[k] = Blocks.AIR.defaultBlockState();
        }
        int top = yMin;
        for (BlockState[][] row : cols) for (BlockState[] col : row) for (int k = col.length - 1; k >= 0; k--) if (!col[k].isAir()) { top = Math.max(top, yMin + k); break; }
        int H = top - yMin + 1;
        int w = (4 * n + 4) * s, h = (2 * n + 2 * H + 8) * s;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int px = 0; px < w; px++) for (int py = 0; py < h; py++) img.setRGB(px, py, 0x15171A);
        int ox = 2 * n * s, oy = 2 * H * s;
        for (int dg = 0; dg <= 2 * (n - 1); dg++) {
            for (int i = Math.max(0, dg - n + 1); i <= Math.min(n - 1, dg); i++) {
                int j = dg - i;
                BlockState[] col = cols[i][j];
                for (int k = 0; k < H; k++) {
                    BlockState b = col[k];
                    if (b.isAir()) continue;
                    boolean upOpen = k + 1 >= H || !opaque(col[k + 1]);
                    boolean xOpen = i + 1 >= n || !opaque(cols[i + 1][j][k]);
                    boolean zOpen = j + 1 >= n || !opaque(cols[i][j + 1][k]);
                    if (!upOpen && !xOpen && !zOpen) continue;
                    int rgb = colour(b);
                    int sx = ox + (i - j) * 2 * s, sy = oy + ((i + j) - 2 * k) * s;
                    put(img, sx - 2 * s, sy, 4 * s, 2 * s, shade(rgb, 1.0));
                    put(img, sx - 2 * s, sy + 2 * s, 2 * s, 2 * s, shade(rgb, 0.62));
                    put(img, sx, sy + 2 * s, 2 * s, 2 * s, shade(rgb, 0.8));
                }
            }
        }
        ImageIO.write(img, "png", f);
    }

    static void isoUnder(Src gen, File f, int x0, int z0, int n) throws Exception {
        int yMin = -40, yMax = 58;
        BlockState[][][] cols = sample(gen, x0, z0, n, yMin, yMax);
        int H = yMax - yMin + 1;
        int w = 4 * n + 4, h = 2 * n + 2 * H + 8;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        int ox = 2 * n, oy = 2 * H;
        for (int s = 0; s <= 2 * (n - 1); s++) {
            for (int i = Math.max(0, s - n + 1); i <= Math.min(n - 1, s); i++) {
                int j = s - i;
                for (int k = 0; k < H; k++) {
                    BlockState b = cols[i][j][k];
                    if (b.isAir()) continue;
                    boolean nearAir = (k + 1 < H && cols[i][j][k + 1].isAir()) || (k > 0 && cols[i][j][k - 1].isAir())
                            || (i + 1 < n && cols[i + 1][j][k].isAir()) || (i > 0 && cols[i - 1][j][k].isAir())
                            || (j + 1 < n && cols[i][j + 1][k].isAir()) || (j > 0 && cols[i][j - 1][k].isAir());
                    if (!nearAir) continue;
                    if (!(k + 1 < H && cols[i][j][k + 1].isAir())) continue;
                    int rgb = colour(b);
                    int sx = ox + (i - j) * 2, sy = oy + (i + j) - 2 * k;
                    put(img, sx - 2, sy, 4, 2, shade(rgb, 1.0));
                    put(img, sx - 2, sy + 2, 2, 2, shade(rgb, 0.55));
                    put(img, sx, sy + 2, 2, 2, shade(rgb, 0.75));
                }
            }
        }
        ImageIO.write(img, "png", f);
    }

    static boolean opaque(BlockState s) {
        return !s.isAir() && s.canOcclude();
    }

    static void put(BufferedImage img, int x, int y, int w, int h, int argb) {
        for (int a = 0; a < w; a++) for (int b = 0; b < h; b++) {
            int px = x + a, py = y + b;
            if (px >= 0 && py >= 0 && px < img.getWidth() && py < img.getHeight()) img.setRGB(px, py, argb);
        }
    }

    static void plan(Src gen, File f, int x0, int z0, int n, int y) throws Exception {
        BufferedImage img = new BufferedImage(n * 3, n * 3, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) {
            Column c = gen.column(x0 + i, z0 + j, null);
            BlockState s = c.get(y);
            int rgb;
            if (s.isAir()) rgb = c.get(y - 1).isAir() ? 0x101010 : 0x303438;
            else if (s.getBlock() instanceof net.minecraft.world.level.block.SlabBlock) rgb = 0xC08040;
            else if (s.is(Blocks.LADDER)) rgb = 0xFFD000;
            else if (s.is(Blocks.DARK_OAK_SIGN)) rgb = 0xFF2020;
            else if (s.is(Blocks.CHEST) || s.is(Blocks.BARREL)) rgb = 0x20FF20;
            else if (s.is(Blocks.SPAWNER)) rgb = 0xFF00FF;
            else rgb = colour(s);
            put(img, i * 3, j * 3, 3, 3, 0xFF000000 | rgb);
        }
        ImageIO.write(img, "png", f);
    }

    static void section(Src gen, File f, int x0, int z, int n) throws Exception {
        int H = Column.HEIGHT;
        BufferedImage img = new BufferedImage(n * 2, H * 2, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < n; i++) {
            Column c = gen.column(x0 + i, z, null);
            for (int y = Column.MIN_Y; y <= Column.MAX_Y; y++) {
                BlockState s = c.get(y);
                int rgb = s.isAir() ? (y > 63 ? 0x2B2F36 : 0x050505) : colour(s);
                put(img, i * 2, (Column.MAX_Y - y) * 2, 2, 2, 0xFF000000 | rgb);
            }
        }
        ImageIO.write(img, "png", f);
    }
}
