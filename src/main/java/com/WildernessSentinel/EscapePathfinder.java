package com.WildernessSentinel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.CollisionData;
import net.runelite.api.CollisionDataFlag;
import net.runelite.api.coords.WorldPoint;

/**
 * A* pathfinder using RuneLite's live collision data.
 * Finds the shortest walkable path from the player to a destination
 * within the currently loaded scene.
 */
public class EscapePathfinder {

  private static final int MAX_SEARCH = 5000;

  private static final int[][] DIRECTIONS = {
      {0, 1},   // North
      {0, -1},  // South
      {1, 0},   // East
      {-1, 0},  // West
      {1, 1},   // NE
      {-1, 1},  // NW
      {1, -1},  // SE
      {-1, -1}  // SW
  };

  private static final int[] DIRECTION_FLAGS = {
      CollisionDataFlag.BLOCK_MOVEMENT_NORTH,
      CollisionDataFlag.BLOCK_MOVEMENT_SOUTH,
      CollisionDataFlag.BLOCK_MOVEMENT_EAST,
      CollisionDataFlag.BLOCK_MOVEMENT_WEST,
      CollisionDataFlag.BLOCK_MOVEMENT_NORTH_EAST,
      CollisionDataFlag.BLOCK_MOVEMENT_NORTH_WEST,
      CollisionDataFlag.BLOCK_MOVEMENT_SOUTH_EAST,
      CollisionDataFlag.BLOCK_MOVEMENT_SOUTH_WEST
  };

  public static List<WorldPoint> findPath(Client client, WorldPoint start, WorldPoint end) {
    CollisionData[] collisionMaps = client.getTopLevelWorldView().getCollisionMaps();
    if (collisionMaps == null) {
      return List.of();
    }

    int plane = start.getPlane();
    if (plane < 0 || plane >= collisionMaps.length) {
      return List.of();
    }

    int[][] flags = collisionMaps[plane].getFlags();
    int baseX = client.getTopLevelWorldView().getBaseX();
    int baseY = client.getTopLevelWorldView().getBaseY();
    int sceneWidth = flags.length;
    int sceneHeight = flags[0].length;

    int startSceneX = start.getX() - baseX;
    int startSceneY = start.getY() - baseY;

    // Clamp destination to scene bounds
    int endSceneX = Math.max(0, Math.min(end.getX() - baseX, sceneWidth - 1));
    int endSceneY = Math.max(0, Math.min(end.getY() - baseY, sceneHeight - 1));

    if (startSceneX < 0 || startSceneX >= sceneWidth || startSceneY < 0 || startSceneY >= sceneHeight) {
      return List.of();
    }

    // A* search
    Set<Long> visited = new HashSet<>();
    PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));

    Node startNode = new Node(startSceneX, startSceneY, 0, heuristic(startSceneX, startSceneY, endSceneX, endSceneY), null);
    open.add(startNode);
    visited.add(key(startSceneX, startSceneY));

    Node bestNode = startNode;
    int iterations = 0;

    while (!open.isEmpty() && iterations < MAX_SEARCH) {
      iterations++;
      Node current = open.poll();

      // Track closest node to destination
      int currentDist = heuristic(current.x, current.y, endSceneX, endSceneY);
      int bestDist = heuristic(bestNode.x, bestNode.y, endSceneX, endSceneY);
      if (currentDist < bestDist) {
        bestNode = current;
      }

      // Reached destination (or close enough)
      if (current.x == endSceneX && current.y == endSceneY) {
        return reconstructPath(current, baseX, baseY, plane);
      }

      // Expand neighbors
      for (int i = 0; i < 8; i++) {
        int nx = current.x + DIRECTIONS[i][0];
        int ny = current.y + DIRECTIONS[i][1];

        if (nx < 0 || nx >= sceneWidth || ny < 0 || ny >= sceneHeight) {
          continue;
        }

        if (visited.contains(key(nx, ny))) {
          continue;
        }

        if (!canMove(flags, current.x, current.y, i, sceneWidth, sceneHeight)) {
          continue;
        }

        visited.add(key(nx, ny));
        // Cardinal = cost 10, diagonal = cost 14 (approximates sqrt(2))
        int moveCost = (i < 4) ? 10 : 14;
        int g = current.g + moveCost;
        int h = heuristic(nx, ny, endSceneX, endSceneY);
        open.add(new Node(nx, ny, g, g + h, current));
      }
    }

    // Return best partial path if destination not reached
    return reconstructPath(bestNode, baseX, baseY, plane);
  }

  private static boolean canMove(int[][] flags, int x, int y, int direction, int w, int h) {
    int nx = x + DIRECTIONS[direction][0];
    int ny = y + DIRECTIONS[direction][1];

    // Bounds check on destination
    if (nx < 0 || nx >= w || ny < 0 || ny >= h) {
      return false;
    }

    // Destination tile must not be fully blocked (object or floor)
    if ((flags[nx][ny] & (CollisionDataFlag.BLOCK_MOVEMENT_OBJECT | CollisionDataFlag.BLOCK_MOVEMENT_FLOOR)) != 0) {
      return false;
    }

    // Cardinal directions - just check the directional flag on source tile
    if (direction < 4) {
      return (flags[x][y] & DIRECTION_FLAGS[direction]) == 0;
    }

    // Diagonal directions - check the diagonal flag on source tile
    // AND both cardinal components must also be passable (no corner cutting)
    switch (direction) {
      case 4: // NE
        return (flags[x][y] & CollisionDataFlag.BLOCK_MOVEMENT_NORTH_EAST) == 0
            && (flags[x][y] & CollisionDataFlag.BLOCK_MOVEMENT_NORTH) == 0
            && (flags[x][y] & CollisionDataFlag.BLOCK_MOVEMENT_EAST) == 0;
      case 5: // NW
        return (flags[x][y] & CollisionDataFlag.BLOCK_MOVEMENT_NORTH_WEST) == 0
            && (flags[x][y] & CollisionDataFlag.BLOCK_MOVEMENT_NORTH) == 0
            && (flags[x][y] & CollisionDataFlag.BLOCK_MOVEMENT_WEST) == 0;
      case 6: // SE
        return (flags[x][y] & CollisionDataFlag.BLOCK_MOVEMENT_SOUTH_EAST) == 0
            && (flags[x][y] & CollisionDataFlag.BLOCK_MOVEMENT_SOUTH) == 0
            && (flags[x][y] & CollisionDataFlag.BLOCK_MOVEMENT_EAST) == 0;
      case 7: // SW
        return (flags[x][y] & CollisionDataFlag.BLOCK_MOVEMENT_SOUTH_WEST) == 0
            && (flags[x][y] & CollisionDataFlag.BLOCK_MOVEMENT_SOUTH) == 0
            && (flags[x][y] & CollisionDataFlag.BLOCK_MOVEMENT_WEST) == 0;
      default:
        return false;
    }
  }

  private static int heuristic(int x1, int y1, int x2, int y2) {
    // Octile distance: matches cost 10 cardinal, 14 diagonal
    int dx = Math.abs(x1 - x2);
    int dy = Math.abs(y1 - y2);
    return 10 * (dx + dy) + (14 - 2 * 10) * Math.min(dx, dy);
  }

  private static long key(int x, int y) {
    return ((long) x << 32) | (y & 0xFFFFFFFFL);
  }

  private static List<WorldPoint> reconstructPath(Node node, int baseX, int baseY, int plane) {
    List<WorldPoint> path = new ArrayList<>();
    Node current = node;
    while (current.parent != null) {
      path.add(0, new WorldPoint(current.x + baseX, current.y + baseY, plane));
      current = current.parent;
    }
    return path;
  }

  private static class Node {
    final int x;
    final int y;
    final int g;
    final int f;
    final Node parent;

    Node(int x, int y, int g, int f, Node parent) {
      this.x = x;
      this.y = y;
      this.g = g;
      this.f = f;
      this.parent = parent;
    }
  }
}
