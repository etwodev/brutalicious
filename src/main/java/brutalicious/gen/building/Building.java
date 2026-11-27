package brutalicious.gen.building;

import brutalicious.gen.Column;

public interface Building {
    int W = 0, E = 1, N = 2, S = 3;

    int GROUND = 63;
    int FLOOR_H = 4;

    static int floorY(int f) { return GROUND + FLOOR_H * f; }

    void fill(Column c, int ux, int uz);

    String name();

    default int floors() { return 0; }

    default int reach(int side, int f) { return -1; }

    default int facadeDepth(int side, int f) { return 0; }

    default int basementReach(int side) { return -1; }

    default boolean ruined() { return false; }

    default boolean occupied(int ux, int uz, int y) { return false; }

    default void openDeckDoors(int f) {}

    default void openRoofDoors(int f) {}

    default boolean needsCistern() { return false; }
}
