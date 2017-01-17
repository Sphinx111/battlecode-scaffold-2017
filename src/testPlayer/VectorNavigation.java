package testPlayer;
import battlecode.common.*;

/**
 * Created by Celeron on 1/14/2017.
 */

// TODO: add sub-class for GRID CELLS to make pathfinding/heatmapping easier, class only needs to return a vector for each gridCell.

/*  This class generates the low resolution vector field for whole-map navigation.
    Only used by the unit designated as leader, and can be allowed to run for multiple turns.
    Ideally only called if area is safe.

    The unit calling this function must transmit the vector field results to the messaging array for other units to query it.
    Must also transmit resolution and mapOffets to messageArray so other units can find out which gridSquare to query.
 */

public class VectorNavigation extends Globals {

    public static boolean mapEdgesFound = false;
    public static float mapXOffset;
    public static float mapYOffset;
    public static float mapWidth;
    public static float mapHeight;
    public static float resolutionX;
    public static float resolutionY;

    //subdivides map into 10x10 overview.
    private static int gridX = 10;
    private static int gridY = 10;
    public static boolean vectorMapReady = false;
    public static MapLocation[][] vectorMap;
    public static float[][] heatMap;
    public static float[][] treeCoverMap;
    private static float passableDensity = 0.3f;

    private static int[] goalLoc = new int[2];
    private static MapLocation[] treesToAdd;

    public static boolean updateVectorMap() throws GameActionException {
        //Checks if map edges exist, sets mapEdgesFound flag.
        checkMapEdges();
        //if map edges haven't been found, break early, don't attempt to build the vector map.
        if (!mapEdgesFound) {return false;}
        else {
            //If there isn't already a vector map, find a resolution and initialize array at fixed gridsize.
            if (!vectorMapReady) {
                calculateGridSize();
                vectorMap = new MapLocation[gridY][gridX];
                treeCoverMap = new float[gridY][gridX];
                heatMap = new float[gridY][gridX];
            }
            //Begin calculating heatMap from targetLocation
            if (goalLoc == null) {
                goalLoc = mapToGridConvert(theirArchonStartLocs[0]);
            }
            //buildTreeDensityMap(); //TreeDensity needs to be built first to find impassable areas for heatmap.
            buildHeatMap();
            //buildvectorMap(); //finally build vector map to follow heatMap pathing.
            //encodeVectorMap(); //encode vector map for transmission to messaging array.
            //transmitVectorMap(); //transmit encoded vector map to messaging array, including key-data.

            return true;
        }
    }

    //set up to 2 goals for bots to path towards
    public static void setGoals(MapLocation goal) {
        for (int i = 0; i < 2; i++) {
            goalLoc = mapToGridConvert(goal);
        }
    }

    private static void buildHeatMap() {

    }

    private static int[] mapToGridConvert(MapLocation mapLoc) {
        int x = (int)Math.floor(mapLoc.x -mapXOffset / resolutionX);
        int y = (int)Math.floor(mapLoc.y -mapYOffset / resolutionY);
        int[] gridLoc = {x,y};
        return gridLoc;
    }

    //calculates size of each grid square to create a Y by X sized grid.
    private static void calculateGridSize() {
        resolutionX = mapWidth / gridY;
        resolutionY = mapHeight / gridX;
    }

    //set a new treeDensity value at a specific gridSquare
    public static void setTreeDensity(int[] gridIndex, int treeDensity) {

    }

    public static void checkMapEdges() throws GameActionException {
        //Reads mapEdges data from the messaging array. Updates mapEdges if existing.
        Messaging.processMapEdges(rc.readBroadcast(Messaging.indexMapEdges));

        if(Radar.minX > Radar.UNKNOWN && Radar.maxX > Radar.UNKNOWN && Radar.minY > Radar.UNKNOWN && Radar.maxX > Radar.UNKNOWN) {
            mapEdgesFound = true;
            mapHeight = Radar.maxY-Radar.minY;
            mapWidth = Radar.maxX-Radar.minX;
            mapXOffset = Radar.minX;
            mapYOffset = Radar.minY;
        }
    }

}
