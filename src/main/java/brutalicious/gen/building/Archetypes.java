package brutalicious.gen.building;

import brutalicious.gen.B;
import brutalicious.gen.Decor;
import brutalicious.gen.Palette;
import brutalicious.gen.Rng;
import brutalicious.gen.Zones;
import net.minecraft.world.level.block.state.BlockState;

import static brutalicious.gen.building.Building.*;

public final class Archetypes {
    private Archetypes() {}

    public enum Type {
        SLAB, TOWER, ZIGGURAT, INVERTED, BUNKER, CARPARK, TRELLICK, PODIUM, WOTRUBA, PLAZA, FLAK, HOCHBUNKER, MONUMENT,
        BARRACKS, HOSPITAL, DEPOT, PANOPTICON, COOLING, FACTORY, SILOS, STACKS, COLUMBARIUM, CENTREPIECE, VACANT, LAUNCH_PAD, TANK_FARM, TRACKING, ASSEMBLY, POWER_STATION, SUBSTATION, SOLAR, ARCOLOGY, KEEP, MINISTRY,
        SUB_PEN, MISSILE_SILO, RADAR_ARRAY, ROUNDHOUSE, OBSERVATORY, CONCERT_HALL, LIBRARY, FUNFAIR, COLOSSUS, SARCOPHAGUS, DECON,
        SEED_VAULT, WAR_ROOM, TANK_YARD, COMPUTER_HALL, WATERWORKS, CREMATORIUM, BELL_TOWER, MAUSOLEUM, AIR_PLANT, LABYRINTH, REACTOR, GASHOLDER
    }

    record W(Type t, int w) {}

    static W w(Type t, double w) { return new W(t, Math.max(0, (int) Math.round(w))); }

    public static Type pick(long h, double dens, boolean mono, Zones.Zone zone) {
        W[] table = switch (zone) {
            case COMPOUND -> new W[]{w(Type.BARRACKS, 26), w(Type.BUNKER, 22), w(Type.FLAK, 12), w(Type.HOCHBUNKER, 10),
                    w(Type.DEPOT, 12), w(Type.PANOPTICON, 9), w(Type.HOSPITAL, 4), w(Type.PLAZA, 7),
                    w(Type.MISSILE_SILO, 8), w(Type.WAR_ROOM, 7), w(Type.TANK_YARD, 18), w(Type.RADAR_ARRAY, 5)};
            case SPACEPORT -> new W[]{w(Type.LAUNCH_PAD, 22), w(Type.TANK_FARM, 16), w(Type.TRACKING, 14), w(Type.ASSEMBLY, 8),
                    w(Type.BUNKER, 10), w(Type.DEPOT, 10), w(Type.VACANT, 18),
                    w(Type.MISSILE_SILO, 8), w(Type.RADAR_ARRAY, 8), w(Type.OBSERVATORY, 4)};
            case POWER -> new W[]{w(Type.POWER_STATION, 12), w(Type.SUBSTATION, 22), w(Type.SOLAR, 22), w(Type.COOLING, 10),
                    w(Type.STACKS, 6), w(Type.DEPOT, 8), w(Type.SLAB, 8), w(Type.VACANT, 12),
                    w(Type.REACTOR, 10), w(Type.WATERWORKS, 6)};
            case SURVIVOR -> new W[]{w(Type.SLAB, 16), w(Type.ZIGGURAT, 14), w(Type.TOWER, 8), w(Type.PODIUM, 6),
                    w(Type.HOSPITAL, 8), w(Type.WOTRUBA, 4), w(Type.PLAZA, 22), w(Type.SEED_VAULT, 3)};
            case NECROPOLIS -> new W[]{w(Type.COLUMBARIUM, 34), w(Type.WOTRUBA, 22), w(Type.PLAZA, 30), w(Type.MONUMENT, 8), w(Type.HOCHBUNKER, 6),
                    w(Type.CREMATORIUM, 12), w(Type.BELL_TOWER, 10), w(Type.MAUSOLEUM, 6), w(Type.SEED_VAULT, 2)};
            case INDUSTRIAL -> new W[]{w(Type.COOLING, 15), w(Type.FACTORY, 26), w(Type.SILOS, 14), w(Type.STACKS, 6),
                    w(Type.DEPOT, 12), w(Type.SLAB, 10), w(Type.CARPARK, 4), w(Type.BUNKER, 5), w(Type.PLAZA, 5),
                    w(Type.ROUNDHOUSE, 10), w(Type.WATERWORKS, 6), w(Type.GASHOLDER, 10)};
            case DARK -> new W[]{w(Type.SLAB, 14), w(Type.BUNKER, 14), w(Type.DEPOT, 12), w(Type.CARPARK, 6), w(Type.PANOPTICON, 5),
                    w(Type.FACTORY, 10), w(Type.HOSPITAL, 4), w(Type.COMPUTER_HALL, 14), w(Type.AIR_PLANT, 8), w(Type.LIBRARY, 4), w(Type.VACANT, 8)};
            case ABANDONED -> new W[]{w(Type.SLAB, 18), w(Type.TOWER, 10), w(Type.ZIGGURAT, 10), w(Type.PODIUM, 4), w(Type.HOSPITAL, 6),
                    w(Type.CARPARK, 4), w(Type.FUNFAIR, 6), w(Type.COLOSSUS, 5), w(Type.DECON, 8), w(Type.VACANT, 15), w(Type.PLAZA, 4), w(Type.SEED_VAULT, 1)};
            case FLOODED -> new W[]{w(Type.SLAB, 18), w(Type.TOWER, 8 + 20 * dens), w(Type.ZIGGURAT, 13), w(Type.INVERTED, 8),
                    w(Type.BUNKER, 7), w(Type.TRELLICK, 7), w(Type.PODIUM, 3 + 8 * dens), w(Type.PLAZA, 12), w(Type.MONUMENT, 7), w(Type.SUB_PEN, 18)};
            default -> mono
                    ? new W[]{w(Type.SLAB, 8), w(Type.TOWER, 10), w(Type.ZIGGURAT, 5), w(Type.INVERTED, 12), w(Type.BUNKER, 26),
                    w(Type.CARPARK, 2), w(Type.TRELLICK, 3), w(Type.PODIUM, 5), w(Type.WOTRUBA, 5), w(Type.PLAZA, 8),
                    w(Type.FLAK, 10), w(Type.HOCHBUNKER, 8), w(Type.MONUMENT, 7), w(Type.HOSPITAL, 4), w(Type.PANOPTICON, 4),
                    w(Type.COMPUTER_HALL, 6), w(Type.LIBRARY, 4), w(Type.AIR_PLANT, 3), w(Type.MAUSOLEUM, 2)}
                    : new W[]{w(Type.SLAB, 18), w(Type.TOWER, 8 + 20 * dens), w(Type.ZIGGURAT, 13), w(Type.INVERTED, 8),
                    w(Type.BUNKER, 7), w(Type.CARPARK, 4), w(Type.TRELLICK, 7), w(Type.PODIUM, 3 + 8 * dens), w(Type.WOTRUBA, 3),
                    w(Type.PLAZA, 15 - 7 * dens), w(Type.FLAK, 4), w(Type.HOCHBUNKER, 6), w(Type.MONUMENT, 7),
                    w(Type.HOSPITAL, 5), w(Type.PANOPTICON, 2), w(Type.DEPOT, 3), w(Type.ARCOLOGY, 1 + 4 * dens * dens),
                    w(Type.OBSERVATORY, 2), w(Type.CONCERT_HALL, 2), w(Type.LIBRARY, 2), w(Type.FUNFAIR, 2), w(Type.COLOSSUS, 2),
                    w(Type.BELL_TOWER, 2), w(Type.MAUSOLEUM, 1), w(Type.SEED_VAULT, 1), w(Type.AIR_PLANT, 1)};
        };
        int total = 0;
        for (W e : table) total += e.w;
        int r = (int) Math.floorMod(h, (long) total);
        for (W e : table) {
            if (r < e.w) return e.t;
            r -= e.w;
        }
        return Type.SLAB;
    }

    static Palette palette(long h, boolean mono) {
        Palette[] set = mono ? Palette.GRIM : Palette.ALL;
        return set[(int) Math.floorMod(h, (long) set.length)];
    }

    static int scaled(long h, int lo, int hi, double dens) {
        int top = lo + (int) Math.round((hi - lo) * (0.35 + 0.65 * dens));
        return Rng.range(h, lo, Math.max(lo, top));
    }

    public static Building build(Type type, long seed, int px, int pz, double dens, boolean mono, Zones.Zone zone, int ground) {
        long h = Rng.mix(seed);
        Palette pal = palette(h >>> 3, mono);
        double decay = 0.12 + Rng.unit(Rng.hash(seed, 7)) * (mono ? 0.45 : 0.35);
        if (zone == Zones.Zone.BOMBED) decay = Math.min(0.85, decay + 0.25);
        if (zone == Zones.Zone.ABANDONED) decay = Math.min(0.85, decay + 0.3);
        boolean compound = zone == Zones.Zone.COMPOUND;
        Building b = switch (type) {
            case SLAB -> slab(seed, px, pz, dens, pal, decay);
            case TOWER -> tower(seed, px, pz, dens, pal, decay);
            case ZIGGURAT -> ziggurat(seed, px, pz, dens, pal, decay);
            case INVERTED -> inverted(seed, px, pz, dens, pal, decay);
            case BUNKER -> bunker(seed, px, pz, dens, mono || compound ? Palette.MONOLITH : Palette.GRIM[(int) Math.floorMod(h >>> 9, 3L)], decay + 0.1, compound ? 10 : 3);
            case CARPARK -> carpark(seed, px, pz, pal, decay);
            case TRELLICK -> trellick(seed, px, pz, dens, pal, decay);
            case PODIUM -> podium(seed, px, pz, dens, pal, decay);
            case WOTRUBA -> new Wotruba(seed, px, pz, pal);
            case PLAZA -> plaza(seed, px, pz, pal, zone, ground);
            case VACANT -> {
                Plaza v = new Plaza(Plaza.Kind.VACANT, seed, px, pz, Palette.BETON);
                double wu = Rng.unit(Rng.hash(seed, 0x3A2C));
                if (zone == Zones.Zone.SPACEPORT && wu < 0.3) v.wreck = 2;
                else if ((zone == Zones.Zone.BOMBED && wu < 0.35) || (zone == Zones.Zone.COMPOUND && wu < 0.2) || wu < 0.03) v.wreck = 1;
                yield v;
            }
            case MINISTRY -> ministry(seed, px, pz);
            case FLAK -> flak(seed, px, pz);
            case MONUMENT -> new Monument(seed, px, pz, mono ? Palette.MONOLITH : Palette.BETON);
            case HOCHBUNKER -> new Hochbunker(seed, px, pz, mono ? Palette.MONOLITH : Palette.BETON);
            case BARRACKS -> barracks(seed, px, pz, decay);
            case HOSPITAL -> hospital(seed, px, pz, dens, decay);
            case DEPOT -> depot(seed, px, pz, decay);
            case PANOPTICON -> new Panopticon(seed, px, pz);
            case COOLING -> new Industrial(Industrial.Kind.COOLING_TOWER, seed, px, pz);
            case FACTORY -> new Industrial(Industrial.Kind.FACTORY, seed, px, pz);
            case SILOS -> new Industrial(Industrial.Kind.SILOS, seed, px, pz);
            case STACKS -> new Industrial(Industrial.Kind.STACKS, seed, px, pz);
            case LAUNCH_PAD -> new Spaceport(Spaceport.Kind.LAUNCH_PAD, seed, px, pz);
            case TANK_FARM -> new Spaceport(Spaceport.Kind.TANK_FARM, seed, px, pz);
            case TRACKING -> new Spaceport(Spaceport.Kind.TRACKING, seed, px, pz);
            case ASSEMBLY -> new Spaceport(Spaceport.Kind.ASSEMBLY, seed, px, pz);
            case POWER_STATION -> new Power(Power.Kind.STATION, seed, px, pz);
            case SUBSTATION -> new Power(Power.Kind.SUBSTATION, seed, px, pz);
            case SOLAR -> new Power(Power.Kind.SOLAR, seed, px, pz);
            case ARCOLOGY -> arcology(seed, px, pz, dens, pal, decay);
            case KEEP -> keep(seed, px, pz);
            case COLUMBARIUM -> columbarium(seed, px, pz, decay);
            case SUB_PEN -> new Places(Places.Kind.SUB_PEN, seed, px, pz, zone);
            case MISSILE_SILO -> new Places(Places.Kind.MISSILE_SILO, seed, px, pz, zone);
            case RADAR_ARRAY -> new Places(Places.Kind.RADAR_ARRAY, seed, px, pz, zone);
            case ROUNDHOUSE -> new Places(Places.Kind.ROUNDHOUSE, seed, px, pz, zone);
            case OBSERVATORY -> new Places(Places.Kind.OBSERVATORY, seed, px, pz, zone);
            case CONCERT_HALL -> new Places(Places.Kind.CONCERT_HALL, seed, px, pz, zone);
            case LIBRARY -> new Places(Places.Kind.LIBRARY, seed, px, pz, zone);
            case FUNFAIR -> new Places(Places.Kind.FUNFAIR, seed, px, pz, zone);
            case COLOSSUS -> new Places(Places.Kind.COLOSSUS, seed, px, pz, zone);
            case SARCOPHAGUS -> new Places(Places.Kind.SARCOPHAGUS, seed, px, pz, zone);
            case DECON -> new Places(Places.Kind.DECON, seed, px, pz, zone);
            case SEED_VAULT -> new Places(Places.Kind.SEED_VAULT, seed, px, pz, zone);
            case WAR_ROOM -> new Places(Places.Kind.WAR_ROOM, seed, px, pz, zone);
            case TANK_YARD -> new Places(Places.Kind.TANK_YARD, seed, px, pz, zone);
            case COMPUTER_HALL -> new Places(Places.Kind.COMPUTER_HALL, seed, px, pz, zone);
            case WATERWORKS -> new Places(Places.Kind.WATERWORKS, seed, px, pz, zone);
            case CREMATORIUM -> new Places(Places.Kind.CREMATORIUM, seed, px, pz, zone);
            case BELL_TOWER -> new Places(Places.Kind.BELL_TOWER, seed, px, pz, zone);
            case MAUSOLEUM -> new Places(Places.Kind.MAUSOLEUM, seed, px, pz, zone);
            case AIR_PLANT -> new Places(Places.Kind.AIR_PLANT, seed, px, pz, zone);
            case LABYRINTH -> new Places(Places.Kind.LABYRINTH, seed, px, pz, zone);
            case REACTOR -> new Places(Places.Kind.REACTOR, seed, px, pz, zone);
            case GASHOLDER -> new Places(Places.Kind.GASHOLDER, seed, px, pz, zone);
            case CENTREPIECE -> switch (zone) {
                case COMPOUND -> flak(seed, px, pz);
                case INDUSTRIAL -> new Industrial(Industrial.Kind.COOLING_TOWER, seed, px, pz);
                case FLOODED -> new Monument(seed, px, pz, Palette.BETON, Monument.Kind.WATER_TOWER);
                case NECROPOLIS -> new Monument(seed, px, pz, Palette.MONOLITH, Monument.Kind.SPOMENIK);
                case SPACEPORT -> new Spaceport(Spaceport.Kind.LAUNCH_PAD, seed ^ 0x5A7L, px, pz);
                case POWER -> new Power(Power.Kind.STATION, seed, px, pz);
                case SURVIVOR -> keep(seed, px, pz);
                case DARK -> new Places(Places.Kind.LABYRINTH, seed, px, pz, zone);
                case ABANDONED -> new Places(Places.Kind.SARCOPHAGUS, seed, px, pz, zone);
                default -> new Plaza(Plaza.Kind.GROUND_ZERO, seed, px, pz, Palette.BETON);
            };
        };
        if (b instanceof Composite comp) {
            if (zone == Zones.Zone.BOMBED) {
                for (Mass m : comp.parts) {
                    long rh = Rng.hash(m.seed, 0xB0B);
                    if (m.ruinY == Integer.MAX_VALUE && m.floors >= 4 && Rng.chance(rh, 0.55)) {
                        m.ruinY = floorY(Rng.range(rh >>> 10, 1, Math.max(1, m.floors / 2)));
                        m.ruinAmp = Rng.range(rh >>> 20, 6, 20);
                    }
                }
            }
            int n = (int) Math.round(decay * (zone == Zones.Zone.BOMBED ? 16 : 8));
            for (Mass m : comp.parts) if (m.ruinY == Integer.MAX_VALUE) m.blast(n);
            for (Mass m : comp.parts) {
                double r = Rng.unit(Rng.hash(m.seed, 0x400F));
                if (zone == Zones.Zone.SURVIVOR) {
                    m.livedShare = Math.max(m.livedShare, 0.55);
                    m.lit = true;
                    m.roofUse = r < 0.15 ? 3 : r < 0.3 ? 1 : r > 0.84 ? 2 : 0;
                } else if (zone == Zones.Zone.ABANDONED) {
                    m.livedShare = 0;
                    m.roofUse = 0;
                    if (m.ruinY == Integer.MAX_VALUE && m.floors >= 4 && Rng.chance(Rng.hash(m.seed, 0xAB4D), 0.3)) {
                        long rh = Rng.hash(m.seed, 0xAB4E);
                        m.ruinY = floorY(Rng.range(rh, 2, Math.max(2, m.floors - 1)));
                        m.ruinAmp = Rng.range(rh >>> 20, 6, 16);
                    }
                    if (Rng.chance(Rng.hash(m.seed, 0xAB4F), 0.5)) m.layout = Mass.Layout.GUTTED;
                } else if (zone == Zones.Zone.DARK) {
                    m.livedShare = 0.02;
                    m.roofUse = 0;
                } else if (r > 0.84) m.roofUse = 2;
                if (m.facadeLow == Mass.Facade.BLANK && m.floors >= 4 && Rng.chance(Rng.hash(m.seed, 0x3A1), 0.45)) m.mural();
            }
        }
        return b;
    }

    static Building wrap(String name, Mass m) {
        return new Composite(name, m.pal).add(m);
    }

    static void maybeRuin(Mass m, long seed) {
        long h = Rng.hash(seed, 0xD00D);
        if (m.floors >= 4 && Rng.chance(h, 0.13)) {
            m.ruinY = floorY(Rng.range(h >>> 10, 2, Math.max(2, m.floors - 1)));
            m.ruinAmp = Rng.range(h >>> 20, 6, 18);
            m.decay = Math.min(0.9, m.decay + 0.25);
        }
    }

    static Building slab(long seed, int px, int pz, double dens, Palette pal, double decay) {
        long h = Rng.hash(seed, 1);
        boolean longX = px >= pz ? !Rng.chance(h, 0.2) : Rng.chance(h, 0.2);
        int s = Rng.range(h >>> 4, 2, 4);
        int depth = Rng.range(h >>> 8, 12, 14);
        int floors = scaled(h >>> 12, 7, 20, dens);
        int across = longX ? pz : px;
        if (Rng.chance(h >>> 16, 0.55) && across >= 2 * depth + 12 + 2 * s) {
            int gap = across - 2 * s - 2 * depth;
            Mass a = slabMass(seed, px, pz, longX, s, s, depth, floors, pal, decay);
            Mass b = slabMass(seed ^ 0xB10CL, px, pz, longX, s, s + depth + gap, depth, Math.max(5, floors - Rng.range(h >>> 20, 0, 4)), pal, decay);
            Composite c = new Composite("park_hill", pal).add(a).add(b);
            int along = longX ? px : pz;
            int lo = s + depth - 3, hi = s + depth + gap + 2;
            for (int f = 2; f < Math.min(a.floors, b.floors) - 1; f += 3) {
                for (int m : new int[]{along / 3, 2 * along / 3}) {
                    if (longX) c.link(m - 2, lo, m + 2, hi, f, false);
                    else c.link(lo, m - 2, hi, m + 2, f, true);
                }
            }
            return c;
        }
        Mass m = slabMass(seed, px, pz, longX, s, (across - depth) / 2, depth, floors, pal, decay);
        maybeRuin(m, seed);
        return wrap("slab_block", m);
    }

    static Mass slabMass(long seed, int px, int pz, boolean longX, int s, int a0, int depth, int floors, Palette pal, double decay) {
        Mass m = new Mass(seed, px, pz, floors, 3);
        if (longX) m.rectAll(s, a0, px - 1 - s, a0 + depth - 1);
        else m.rectAll(a0, s, a0 + depth - 1, pz - 1 - s);
        m.pal = pal; m.decay = decay;
        m.facadeLow = m.facadeHigh = Mass.Facade.LOGGIA;
        m.blankEnds = true;
        m.pilotis = true;
        m.roomW = 5;
        m.kind = Decor.RoomKind.HOME;
        m.centreCore();
        int[] r = {m.x0[m.idx(0)], m.z0[m.idx(0)], m.x1[m.idx(0)], m.z1[m.idx(0)]};
        if (longX) m.roofBoxes = new int[][]{{r[0] + 3, r[1] + 3, r[0] + 6, r[1] + 6, 7}, {r[2] - 11, r[1] + 2, r[2] - 3, r[3] - 2, 4}};
        else m.roofBoxes = new int[][]{{r[0] + 3, r[1] + 3, r[0] + 6, r[1] + 6, 7}, {r[0] + 2, r[3] - 11, r[2] - 2, r[3] - 3, 4}};
        return m;
    }

    static Building tower(long seed, int px, int pz, double dens, Palette pal, double decay) {
        long h = Rng.hash(seed, 2);
        if (Rng.chance(h >>> 40, 0.35)) return cluster(seed, px, pz, dens, decay);
        boolean barbican = Rng.chance(h, 0.4);
        int inset = barbican ? 4 : 2;
        int w = Math.min(px - 2 * inset - 2, Rng.range(h >>> 4, 14, 22));
        int d = Math.min(pz - 2 * inset - 2, Rng.range(h >>> 9, 14, 22));
        int floors = scaled(h >>> 14, 14, 52, dens);
        Mass m = new Mass(seed, px, pz, floors, 3);
        int x0 = (px - w) / 2, z0 = (pz - d) / 2;
        m.rectAll(x0, z0, x0 + w - 1, z0 + d - 1);
        m.pal = barbican ? Palette.BARBICAN : pal;
        m.decay = decay;
        Mass.Facade[] f = {Mass.Facade.RIBBON, Mass.Facade.GRID, Mass.Facade.FINS};
        m.facadeLow = m.facadeHigh = f[(int) Math.floorMod(h >>> 20, 3L)];
        if (barbican) { m.balcony = 2; m.facadeLow = m.facadeHigh = Mass.Facade.RIBBON; }
        m.kind = Rng.chance(h >>> 24, 0.7) ? Decor.RoomKind.HOME : Decor.RoomKind.OFFICE;
        m.layout = m.kind == Decor.RoomKind.HOME ? Mass.Layout.CELLS : Mass.Layout.OPEN;
        m.centreCore();
        if (Rng.chance(h >>> 28, 0.5)) { m.antennaX = x0 + 2; m.antennaZ = z0 + 2; m.antennaH = Rng.range(h >>> 32, 5, 12); }
        maybeRuin(m, seed);
        return wrap(barbican ? "barbican_tower" : "point_block", m);
    }

    static Building cluster(long seed, int px, int pz, double dens, double decay) {
        long h = Rng.hash(seed, 12);
        int s = 3;
        int t = Math.min(16, Math.min(px, pz) / 2 - s - 4);
        int[][] at = {{s, s}, {px - 1 - s - (t - 1), s}, {s, pz - 1 - s - (t - 1)}};
        Composite c = new Composite("barbican_cluster", Palette.BARBICAN);
        Mass[] m = new Mass[3];
        for (int i = 0; i < 3; i++) {
            long hi = Rng.hash(h, i);
            m[i] = new Mass(seed ^ (0x7011L * (i + 1)), px, pz, scaled(hi, 12, 44, dens), i == 0 ? 3 : 0);
            m[i].rectAll(at[i][0], at[i][1], at[i][0] + t - 1, at[i][1] + t - 1);
            m[i].pal = Palette.BARBICAN;
            m[i].decay = decay;
            m[i].balcony = 2;
            m[i].facadeLow = m[i].facadeHigh = Mass.Facade.RIBBON;
            m[i].kind = Decor.RoomKind.HOME;
            m[i].centreCore();
            maybeRuin(m[i], seed ^ i);
            c.add(m[i]);
        }
        int mid = s + t / 2;
        int lowest = Math.min(m[0].floors, Math.min(m[1].floors, m[2].floors));
        for (int f = 2; f < lowest; f += 6) {
            c.link(at[0][0] + t - 2, mid - 2, at[1][0] + 1, mid + 2, f, true);
            c.link(mid - 2, at[0][1] + t - 2, mid + 2, at[2][1] + 1, f, false);
        }
        return c;
    }

    static Building ziggurat(long seed, int px, int pz, double dens, Palette pal, double decay) {
        long h = Rng.hash(seed, 3);
        boolean longX = px >= pz;
        boolean oneSided = Rng.chance(h, 0.4);
        int s = 2, step = Rng.range(h >>> 3, 1, 2);
        int across = (longX ? pz : px) - 2 * s;
        int maxF = (across - 12) / (oneSided ? step : 2 * step);
        int floors = Math.max(3, Math.min(maxF, scaled(h >>> 7, 5, 14, dens)));
        Mass m = new Mass(seed, px, pz, floors, 3);
        for (int f = -3; f < floors; f++) {
            int k = Math.max(0, f) * step;
            int a0 = s + k, a1 = s + across - 1 - (oneSided ? 0 : k);
            if (longX) m.rect(f, s, a0, px - 1 - s, a1);
            else m.rect(f, a0, s, a1, pz - 1 - s);
        }
        m.pal = oneSided ? Palette.ALEXANDRA : pal;
        m.decay = decay;
        m.facadeLow = m.facadeHigh = Rng.chance(h >>> 11, 0.5) ? Mass.Facade.GRID : Mass.Facade.LOGGIA;
        m.kind = Decor.RoomKind.HOME;
        m.centreCore();
        maybeRuin(m, seed);
        return wrap(oneSided ? "terrace_housing" : "ziggurat", m);
    }

    static Building inverted(long seed, int px, int pz, double dens, Palette pal, double decay) {
        long h = Rng.hash(seed, 4);
        int floors = scaled(h, 7, 16, dens);
        Mass m = new Mass(seed, px, pz, floors, 3);
        int base = Math.min(px, pz) / 2 - 9;
        for (int f = -3; f < floors; f++) {
            int inset = f < 2 ? base : Math.max(2, base - (f - 1) * 2);
            m.rect(f, inset, inset, px - 1 - inset, pz - 1 - inset);
        }
        m.pal = pal;
        m.decay = decay;
        m.facadeLow = Mass.Facade.GRID;
        m.facadeHigh = Mass.Facade.FINS;
        m.highFrom = Math.max(2, floors - 3);
        m.kind = Decor.RoomKind.OFFICE;
        m.layout = Mass.Layout.OPEN;
        m.centreCore();
        maybeRuin(m, seed);
        return wrap("inverted_hall", m);
    }

    static Building bunker(long seed, int px, int pz, double dens, Palette pal, double decay, int basement) {
        long h = Rng.hash(seed, 5);
        int s = Rng.range(h, 2, 4);
        int floors = scaled(h >>> 4, 4, 15, dens);
        Mass m = new Mass(seed, px, pz, floors, basement);
        m.deepLayout = Mass.Layout.CELLS;
        m.rectAll(s, s, px - 1 - s, pz - 1 - s);
        m.pal = pal;
        m.decay = Math.min(0.8, decay);
        m.facadeLow = Mass.Facade.BLANK;
        m.ribs = true;
        m.facadeHigh = Mass.Facade.SLIT;
        m.highFrom = floors - Rng.range(h >>> 8, 1, 2);
        m.doorSides = 1 << Rng.range(h >>> 12, 0, 3);
        m.layout = Mass.Layout.ARCHIVE;
        m.kind = Decor.RoomKind.BUNKER;
        m.centreCore();
        int x1 = px - 1 - s, z1 = pz - 1 - s;
        m.roofBoxes = new int[][]{{s + 3, s + 3, s + 11, s + 9, 5}, {x1 - 9, z1 - 7, x1 - 3, z1 - 3, 3}};
        if (Rng.chance(h >>> 16, 0.6)) { m.antennaX = x1 - 6; m.antennaZ = s + 5; m.antennaH = Rng.range(h >>> 20, 8, 18); }
        maybeRuin(m, seed);
        return wrap("monolith", m);
    }

    static Building carpark(long seed, int px, int pz, Palette pal, double decay) {
        long h = Rng.hash(seed, 6);
        int floors = Rng.range(h, 3, 7);
        Mass m = new Mass(seed, px, pz, floors, 1);
        int s = 3;
        m.rectAll(s, s, px - 1 - s, pz - 1 - s);
        m.pal = pal;
        m.decay = decay;
        m.facadeLow = m.facadeHigh = Mass.Facade.CARPARK;
        m.layout = Mass.Layout.PARKING;
        m.kind = Decor.RoomKind.OFFICE;
        m.balcony = 1;
        m.rampX = s + 4;
        m.rampZ = s + 3;
        m.coreX = px - 1 - s - 8;
        m.coreZ = pz - 1 - s - 8;
        return wrap("car_park", m);
    }

    static Building trellick(long seed, int px, int pz, double dens, Palette pal, double decay) {
        long h = Rng.hash(seed, 8);
        int s = 3, depth = 12;
        int floors = scaled(h, 18, 40, dens);
        boolean longX = px >= pz;
        int len = (longX ? px : pz) - 2 * s - 13;
        int aStart = s, aEnd = s + len - 1;
        int c0 = ((longX ? pz : px) - depth) / 2;
        Mass main = new Mass(seed, px, pz, floors, 3);
        Mass svc = new Mass(seed ^ 0x5E7L, px, pz, floors + 3, 3);
        int sa0 = aEnd + 4, sa1 = sa0 + 8;
        int sc0 = c0 + depth / 2 - 4, sc1 = sc0 + 8;
        if (longX) {
            main.rectAll(aStart, c0, aEnd, c0 + depth - 1);
            svc.rectAll(sa0, sc0, sa1, sc1);
            for (int f = floors + 1; f < floors + 3; f++) svc.rect(f, sa0 - 4, sc0 - 1, sa1, sc1 + 1);
        } else {
            main.rectAll(c0, aStart, c0 + depth - 1, aEnd);
            svc.rectAll(sc0, sa0, sc1, sa1);
            for (int f = floors + 1; f < floors + 3; f++) svc.rect(f, sc0 - 1, sa0 - 4, sc1 + 1, sa1);
        }
        main.pal = svc.pal = pal;
        main.decay = svc.decay = decay;
        main.facadeLow = main.facadeHigh = Mass.Facade.GRID;
        svc.facadeLow = svc.facadeHigh = Mass.Facade.SLIT;
        main.kind = Decor.RoomKind.HOME;
        svc.layout = Mass.Layout.OPEN;
        main.centreCore();
        svc.centreCore();
        Composite c = new Composite("trellick", pal).add(main).add(svc);
        int mid = (sc0 + sc1) / 2;
        for (int f = 1; f < floors; f += 3) {
            if (longX) c.link(aEnd - 2, mid - 2, sa0, mid + 2, f, true);
            else c.link(mid - 2, aEnd - 2, mid + 2, sa0, f, false);
        }
        return c;
    }

    static Building podium(long seed, int px, int pz, double dens, Palette pal, double decay) {
        long h = Rng.hash(seed, 9);
        int pf = Rng.range(h, 2, 4);
        Mass pod = new Mass(seed, px, pz, pf, 3);
        int s = 2;
        pod.rectAll(s, s, px - 1 - s, pz - 1 - s);
        pod.pal = pal;
        pod.decay = decay;
        pod.facadeLow = pod.facadeHigh = Mass.Facade.RIBBON;
        pod.layout = Mass.Layout.OPEN;
        pod.kind = Decor.RoomKind.OFFICE;
        int tw = Rng.range(h >>> 4, 15, 19);
        int floors = scaled(h >>> 8, 14, 44, dens);
        Mass tower = new Mass(seed ^ 0x707L, px, pz, floors, 3);
        int x0 = (px - tw) / 2, z0 = (pz - tw) / 2;
        tower.rectAll(x0, z0, x0 + tw - 1, z0 + tw - 1);
        tower.pal = pal;
        tower.decay = decay;
        tower.facadeLow = Mass.Facade.SLIT;
        tower.facadeHigh = Rng.chance(h >>> 16, 0.5) ? Mass.Facade.FINS : Mass.Facade.GRID;
        tower.highFrom = pf;
        tower.extraDoorFloor = pf;
        tower.kind = Decor.RoomKind.OFFICE;
        tower.layout = Mass.Layout.OPEN;
        tower.centreCore();
        return new Composite("podium_tower", pal).add(pod).add(tower);
    }

    static Building plaza(long seed, int px, int pz, Palette pal, Zones.Zone zone, int ground) {
        long h = Rng.hash(seed, 10);
        double u = Rng.unit(h);
        Plaza.Kind k;
        if (zone == Zones.Zone.COMPOUND) k = Plaza.Kind.PARADE;
        else if (zone == Zones.Zone.NECROPOLIS) k = Plaza.Kind.CEMETERY;
        else if (zone == Zones.Zone.FLOODED) k = u < 0.3 ? Plaza.Kind.SUNKEN : u < 0.5 ? Plaza.Kind.LIDO : u < 0.7 ? Plaza.Kind.STADIUM : Plaza.Kind.CIVIC;
        else if (zone == Zones.Zone.SURVIVOR)
            k = u < 0.12 ? Plaza.Kind.FARM : u < 0.22 ? Plaza.Kind.GREENHOUSE : u < 0.55 ? Plaza.Kind.MARKET : u < 0.62 ? Plaza.Kind.PARK
                    : u < 0.70 ? Plaza.Kind.ALLOTMENT : Plaza.Kind.CIVIC;
        else if (zone == Zones.Zone.INDUSTRIAL || zone == Zones.Zone.POWER || zone == Zones.Zone.SPACEPORT || zone == Zones.Zone.ABANDONED) k = Plaza.Kind.CIVIC;
        else k = u < 0.18 ? Plaza.Kind.PIT : u < 0.32 ? Plaza.Kind.SUNKEN : u < 0.50 ? Plaza.Kind.STATION : u < 0.64 ? Plaza.Kind.LIDO
                : u < 0.78 ? Plaza.Kind.STADIUM : Plaza.Kind.CIVIC;
        if (k == Plaza.Kind.PIT && ground != 0) k = Plaza.Kind.SUNKEN;
        return new Plaza(k, seed, px, pz, pal);
    }

    static Building flak(long seed, int px, int pz) {
        long h = Rng.hash(seed, 13);
        Palette pal = Palette.BARBICAN;
        int size = Math.min(40, Math.min(px, pz) - 10);
        int sx = (px - size) / 2, sz = (pz - size) / 2;
        int ex = sx + size - 1, ez = sz + size - 1;
        int floors = Rng.range(h, 9, 12);
        Mass main = new Mass(seed, px, pz, floors, 3);
        main.rectAll(sx, sz, ex, ez);
        main.pal = pal;
        main.decay = 0.06;
        main.facadeLow = main.facadeHigh = Mass.Facade.SLIT;
        main.layout = Mass.Layout.OPEN;
        main.kind = Decor.RoomKind.BUNKER;
        main.doorSides = 1 << Rng.range(h >>> 6, 0, 3);
        main.centreCore();
        Composite c = new Composite("flak_tower", pal).add(main);
        int t = 11;
        int[][] corners = {{sx, sz, -1, -1}, {ex - t + 1, sz, 1, -1}, {sx, ez - t + 1, -1, 1}, {ex - t + 1, ez - t + 1, 1, 1}};
        int roof = Building.floorY(floors + 2);
        for (int[] k : corners) {
            Mass tur = new Mass(seed ^ (k[0] * 31L + k[1]), px, pz, floors + 2, 0);
            for (int f = 0; f < floors + 2; f++) {
                int o = f >= floors - 2 ? 2 : 0;
                int ax = k[0] + k[2] * o, az = k[1] + k[3] * o;
                tur.rect(f, ax, az, ax + t - 1, az + t - 1);
            }
            tur.pal = pal;
            tur.decay = 0.06;
            tur.facadeLow = tur.facadeHigh = Mass.Facade.SLIT;
            tur.layout = Mass.Layout.OPEN;
            tur.kind = Decor.RoomKind.BUNKER;
            tur.allDoors = true;
            c.add(tur);
            int gx = k[0] + k[2] * 2 + t / 2, gz = k[1] + k[3] * 2 + t / 2;
            c.box(gx - 1, gz - 1, gx + 1, gz + 1, roof + 1, roof + 2, B.POLISHED_DEEPSLATE);
            c.box(gx + k[2] * 2, gz, gx + k[2] * 7, gz, roof + 3, roof + 3, B.BASALT_X);
            c.box(gx, gz + k[3] * 2, gx, gz + k[3] * 7, roof + 3, roof + 3, B.BASALT_Z);
        }
        return c;
    }

    static Building barracks(long seed, int px, int pz, double decay) {
        long h = Rng.hash(seed, 14);
        boolean longX = px >= pz;
        int across = longX ? pz : px, along = longX ? px : pz;
        int n = across >= 46 ? 3 : 2, depth = 10, s = 3;
        int gap = (across - 2 * s - n * depth) / Math.max(1, n - 1);
        Composite c = new Composite("barracks", Palette.MONOLITH);
        for (int i = 0; i < n; i++) {
            Mass m = new Mass(seed ^ (0xBA5L * (i + 1)), px, pz, Rng.range(Rng.hash(h, i), 2, 4), 1);
            int a0 = s + i * (depth + gap);
            if (longX) m.rectAll(s, a0, along - 1 - s, a0 + depth - 1);
            else m.rectAll(a0, s, a0 + depth - 1, along - 1 - s);
            m.pal = Palette.MONOLITH;
            m.decay = decay;
            m.facadeLow = m.facadeHigh = Mass.Facade.SLIT;
            m.layout = Mass.Layout.WARD;
            m.kind = Decor.RoomKind.MILITARY;
            m.blankEnds = true;
            int mid = (longX ? px : pz) / 2;
            if (longX) { m.coreX = mid - 2; m.coreZ = a0 + 2; } else { m.coreX = a0 + 2; m.coreZ = mid - 2; }
            c.add(m);
        }
        c.box(px / 2, pz / 2, px / 2, pz / 2, GROUND + 1, GROUND + 14, B.IRON_BARS);
        c.box(px / 2 - 1, pz / 2 - 1, px / 2 + 1, pz / 2 + 1, GROUND + 1, GROUND + 1, B.SMOOTH_STONE);
        return c;
    }

    static Building hospital(long seed, int px, int pz, double dens, double decay) {
        long h = Rng.hash(seed, 15);
        Palette pal = Rng.chance(h, 0.5) ? Palette.ALEXANDRA : Palette.BETON;
        int s = 3;
        Mass ward = new Mass(seed, px, pz, Rng.range(h >>> 4, 4, 7), 3);
        ward.rectAll(s, s, px - 1 - s, s + 13);
        ward.pal = pal;
        ward.decay = decay;
        ward.facadeLow = ward.facadeHigh = Mass.Facade.RIBBON;
        ward.layout = Mass.Layout.WARD;
        ward.kind = Decor.RoomKind.HOSPITAL;
        ward.centreCore();
        Mass tower = new Mass(seed ^ 0x4057L, px, pz, scaled(h >>> 8, 8, 20, dens), 3);
        int tw = 18;
        tower.rectAll(px / 2 - tw / 2, s + 13, px / 2 + tw / 2 - 1, Math.min(pz - 1 - s, s + 13 + 22));
        tower.pal = pal;
        tower.decay = decay;
        tower.facadeLow = tower.facadeHigh = Mass.Facade.GRID;
        tower.layout = Mass.Layout.CELLS;
        tower.roomW = 5;
        tower.kind = Decor.RoomKind.HOSPITAL;
        tower.extraDoorFloor = 1;
        tower.centreCore();
        return new Composite("hospital", pal).add(ward).add(tower);
    }

    static Building depot(long seed, int px, int pz, double decay) {
        long h = Rng.hash(seed, 16);
        int s = 3;
        Mass m = new Mass(seed, px, pz, Rng.range(h, 2, 5), 1);
        m.rectAll(s, s, px - 1 - s, pz - 1 - s);
        m.pal = Palette.STONE;
        m.decay = decay;
        m.facadeLow = m.facadeHigh = Mass.Facade.BLANK;
        m.ribs = true;
        m.layout = Mass.Layout.STORAGE;
        m.kind = Decor.RoomKind.DEPOT;
        m.centreCore();
        return wrap("depot", m);
    }

    static Building arcology(long seed, int px, int pz, double dens, Palette pal, double decay) {
        long h = Rng.hash(seed, 18);
        int floors = Rng.range(h, 60, 60 + (int) Math.round(24 * dens));
        Mass m = new Mass(seed, px, pz, floors, 3);
        for (int f = -3; f < floors; f++) {
            int inset = 3 + 3 * (Math.max(0, f) / 20);
            m.rect(f, inset, inset, px - 1 - inset, pz - 1 - inset);
        }
        m.pal = pal;
        m.decay = Math.min(0.35, decay);
        m.facadeLow = Mass.Facade.GRID;
        m.facadeHigh = Mass.Facade.FINS;
        m.highFrom = 40;
        m.layout = Mass.Layout.CELLS;
        m.kind = Decor.RoomKind.HOME;
        m.centreCore();
        m.antennaX = px / 2 + 6;
        m.antennaZ = pz / 2 + 6;
        m.antennaH = 14;
        return wrap("arcology", m);
    }

    static Building keep(long seed, int px, int pz) {
        long h = Rng.hash(seed, 19);
        Mass m = new Mass(seed, px, pz, Rng.range(h, 14, 18), 3);
        int x0 = px / 2 - 12, z0 = pz / 2 - 12;
        m.rectAll(x0, z0, x0 + 23, z0 + 23);
        m.pal = Palette.BARBICAN;
        m.decay = 0.05;
        m.facadeLow = Mass.Facade.SLIT;
        m.facadeHigh = Mass.Facade.RIBBON;
        m.highFrom = 4;
        m.layout = Mass.Layout.CELLS;
        m.kind = Decor.RoomKind.HOME;
        m.livedShare = 1.0;
        m.lit = true;
        m.roofUse = 3;
        m.centreCore();
        Composite c = new Composite("the_keep", Palette.BARBICAN).add(m);
        int top = GROUND + 7, s = 2, e = px - 3, f = pz - 3, gm = px / 2;
        BlockState wall = Palette.BARBICAN.wall;
        c.box(s, s, e, s, GROUND + 1, top, wall);
        c.box(s, s, s, f, GROUND + 1, top, wall);
        c.box(e, s, e, f, GROUND + 1, top, wall);
        c.box(s, f, gm - 3, f, GROUND + 1, top, wall);
        c.box(gm + 3, f, e, f, GROUND + 1, top, wall);
        return c;
    }

    static Building columbarium(long seed, int px, int pz, double decay) {
        long h = Rng.hash(seed, 17);
        int s = Rng.range(h, 3, 6);
        Mass m = new Mass(seed, px, pz, Rng.range(h >>> 4, 3, 7), 1);
        m.rectAll(s, s, px - 1 - s, pz - 1 - s);
        m.pal = Rng.chance(h >>> 8, 0.5) ? Palette.STONE : Palette.BETON;
        m.decay = Math.min(0.4, decay);
        m.facadeLow = m.facadeHigh = Mass.Facade.GRID;
        m.layout = Mass.Layout.URNS;
        m.kind = Decor.RoomKind.LOBBY;
        m.centreCore();
        return wrap("columbarium", m);
    }

    public static Mass watchtower(long seed, int wx, int wz, int[] doorFloors) {
        Mass m = new Mass(seed, wx, wz, 10, 0);
        int ox = (wx - 10) / 2, oz = (wz - 10) / 2;
        m.rectAll(ox, oz, ox + 9, oz + 9);
        m.pal = Palette.MONOLITH;
        m.decay = 0.05;
        m.facadeLow = Mass.Facade.BLANK;
        m.facadeHigh = Mass.Facade.SLIT;
        m.highFrom = 8;
        m.layout = Mass.Layout.OPEN;
        m.kind = Decor.RoomKind.MILITARY;
        m.groundDoors = false;
        m.deckDoorFloor = doorFloors[0];
        if (doorFloors.length > 1) m.extraDoorFloor = doorFloors[1];
        m.antennaX = ox + 1;
        m.antennaZ = oz + 1;
        m.antennaH = 4;
        m.centreCore();
        return m;
    }

    public static Mass megaColumn(long seed, int w, int floors, int tierFloor, int skyFloor) {
        Mass m = tierPillar(seed, w, floors);
        m.extraDoorFloor = skyFloor;
        m.pal = Palette.MONOLITH;
        m.facadeLow = m.facadeHigh = Mass.Facade.SLIT;
        m.deckDoorFloor = tierFloor;
        return m;
    }

    public static Mass tierPillar(long seed, int w, int floors) {
        Mass m = new Mass(seed, w, w, floors, 0);
        int o = (w - 10) / 2;
        m.rectAll(o, o, o + 9, o + 9);
        m.pal = Palette.BETON;
        m.decay = 0.05;
        m.pilotis = true;
        m.facadeLow = m.facadeHigh = Mass.Facade.BLANK;
        m.ribs = true;
        m.layout = Mass.Layout.OPEN;
        m.kind = Decor.RoomKind.LOBBY;
        m.centreCore();
        return m;
    }

    static Building ministry(long seed, int px, int pz) {
        long h = Rng.hash(seed, 11);
        int floors = Rng.range(h, 50, 56);
        Palette pal = Palette.MONOLITH;
        Mass pod = new Mass(seed, px, pz, 6, 3);
        for (int f = -3; f < 6; f++) {
            int k = 1 + Math.max(0, f) * 2;
            pod.rect(f, k, k, px - 1 - k, pz - 1 - k);
        }
        pod.pal = pal;
        pod.decay = 0.15;
        pod.facadeLow = pod.facadeHigh = Mass.Facade.SLIT;
        pod.layout = Mass.Layout.ARCHIVE;
        pod.kind = Decor.RoomKind.BUNKER;
        Mass t = new Mass(seed ^ 0x313L, px, pz, floors, 3);
        int base = Math.min(px, pz) / 2 - 12;
        for (int f = -3; f < floors; f++) {
            int inset = f < floors - 6 ? base : Math.max(base - 6, base - (f - (floors - 7)));
            t.rect(f, inset, inset, px - 1 - inset, pz - 1 - inset);
        }
        t.pal = pal;
        t.decay = 0.12;
        t.facadeLow = Mass.Facade.FINS;
        t.facadeHigh = Mass.Facade.BLANK;
        t.ribs = true;
        t.highFrom = floors - 6;
        t.extraDoorFloor = 6;
        t.layout = Mass.Layout.ARCHIVE;
        t.kind = Decor.RoomKind.BUNKER;
        t.centreCore();
        t.antennaX = px / 2 + 4;
        t.antennaZ = pz / 2 + 4;
        t.antennaH = 18;
        t.beacon = true;
        return new Composite("the_ministry", pal).add(pod).add(t);
    }
}
