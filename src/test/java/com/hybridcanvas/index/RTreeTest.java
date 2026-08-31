package com.hybridcanvas.index;

import com.hybridcanvas.geom.Bounds2D;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RTreeTest {

    @Test
    void insertHitReturnsId() {
        RTree tree = new RTree();
        UUID id = UUID.randomUUID();
        tree.insert(id, new Bounds2D(10, 10, 20, 20));

        List<UUID> out = new ArrayList<>();
        tree.search(new Bounds2D(0, 0, 30, 30), out);
        assertEquals(1, out.size());
        assertEquals(id, out.get(0));
    }

    @Test
    void queryMissReturnsEmpty() {
        RTree tree = new RTree();
        tree.insert(UUID.randomUUID(), new Bounds2D(10, 10, 20, 20));

        List<UUID> out = new ArrayList<>();
        tree.search(new Bounds2D(100, 100, 200, 200), out);
        assertTrue(out.isEmpty());
    }

    @Test
    void removeThenQueryEmpty() {
        RTree tree = new RTree();
        UUID id = UUID.randomUUID();
        tree.insert(id, new Bounds2D(10, 10, 20, 20));
        tree.remove(id);

        List<UUID> out = new ArrayList<>();
        tree.search(new Bounds2D(0, 0, 30, 30), out);
        assertTrue(out.isEmpty());
        assertEquals(0, tree.size());
    }

    @Test
    void reInsertSameIdUpdatesBounds() {
        RTree tree = new RTree();
        UUID id = UUID.randomUUID();
        tree.insert(id, new Bounds2D(10, 10, 20, 20));

        // query should find it in old bounds
        List<UUID> out = new ArrayList<>();
        tree.search(new Bounds2D(15, 15, 25, 25), out);
        assertEquals(1, out.size());

        // re-insert same id with new bounds — old bounds gone
        tree.insert(id, new Bounds2D(100, 100, 200, 200));
        out.clear();
        tree.search(new Bounds2D(15, 15, 25, 25), out);
        assertTrue(out.isEmpty());

        // new bounds found
        out.clear();
        tree.search(new Bounds2D(150, 150, 250, 250), out);
        assertEquals(1, out.size());
        assertEquals(id, out.get(0));
        assertEquals(1, tree.size());
    }

    @Test
    void mixedTinyAndHuge() {
        RTree tree = new RTree();

        // tiny shapes
        UUID tiny1 = UUID.randomUUID();
        UUID tiny2 = UUID.randomUUID();
        tree.insert(tiny1, new Bounds2D(5, 5, 6, 6));     // tiny, at (5-6)
        tree.insert(tiny2, new Bounds2D(95, 95, 96, 96)); // tiny, at (95-96)

        // huge shape covering everything
        UUID huge = UUID.randomUUID();
        tree.insert(huge, new Bounds2D(0, 0, 1000, 1000));

        // huge box query → all three
        List<UUID> out = new ArrayList<>();
        tree.search(new Bounds2D(0, 0, 1000, 1000), out);
        assertEquals(3, out.size());
        assertTrue(out.contains(tiny1));
        assertTrue(out.contains(tiny2));
        assertTrue(out.contains(huge));

        // small box at (4,4)-(7,7) → tiny1 + huge
        out.clear();
        tree.search(new Bounds2D(4, 4, 7, 7), out);
        assertEquals(2, out.size());
        assertTrue(out.contains(tiny1));
        assertTrue(out.contains(huge));

        // point query at (5.5,5.5) → tiny1 + huge
        out.clear();
        tree.search(new Bounds2D(5.5, 5.5, 5.5, 5.5), out);
        assertEquals(2, out.size());
        assertTrue(out.contains(tiny1));
        assertTrue(out.contains(huge));
    }

    @Test
    void randomStress20kMatchesBruteForce() {
        Random rng = new Random(42);
        int count = 20_000;

        UUID[] ids = new UUID[count];
        double[] minXs = new double[count], minYs = new double[count],
                 maxXs = new double[count], maxYs = new double[count];

        RTree tree = new RTree();
        for (int i = 0; i < count; i++) {
            ids[i] = UUID.randomUUID();
            double x = rng.nextDouble() * 100_000;
            double y = rng.nextDouble() * 100_000;
            double w = rng.nextDouble() * 500 + 1;
            double h = rng.nextDouble() * 500 + 1;
            minXs[i] = x; minYs[i] = y;
            maxXs[i] = x + w; maxYs[i] = y + h;
            tree.insert(ids[i], new Bounds2D(x, y, x + w, y + h));
        }
        assertEquals(count, tree.size());

        // 100 random queries
        List<UUID> out = new ArrayList<>();
        for (int q = 0; q < 100; q++) {
            double qx = rng.nextDouble() * 100_000;
            double qy = rng.nextDouble() * 100_000;
            double qw = rng.nextDouble() * 1000 + 1;
            double qh = rng.nextDouble() * 1000 + 1;
            Bounds2D query = new Bounds2D(qx, qy, qx + qw, qy + qh);

            // brute-force
            Set<UUID> expected = new HashSet<>();
            for (int i = 0; i < count; i++) {
                if (minXs[i] <= query.maxX && maxXs[i] >= query.minX
                        && minYs[i] <= query.maxY && maxYs[i] >= query.minY) {
                    expected.add(ids[i]);
                }
            }

            out.clear();
            tree.search(query, out);
            assertEquals(expected.size(), out.size(),
                    "query " + q + ": expected " + expected.size() + " got " + out.size());
            assertTrue(expected.containsAll(out), "query " + q + ": extra ids in result");
        }

        // remove all — no error, size 0
        for (UUID id : ids) {
            tree.remove(id);
        }
        assertEquals(0, tree.size());

        out.clear();
        tree.search(new Bounds2D(0, 0, 100_000, 100_000), out);
        assertTrue(out.isEmpty());
    }

    @Test
    void randomStress20kOverlapMatchesBruteForce() {
        Random rng = new Random(99);
        int count = 20_000;

        UUID[] ids = new UUID[count];
        double[] minXs = new double[count], minYs = new double[count],
                 maxXs = new double[count], maxYs = new double[count];

        RTree tree = new RTree();
        for (int i = 0; i < count; i++) {
            ids[i] = UUID.randomUUID();
            double x = rng.nextDouble() * 5000;
            double y = rng.nextDouble() * 5000;
            double w = rng.nextDouble() * 2000 + 100;
            double h = rng.nextDouble() * 2000 + 100;
            minXs[i] = x; minYs[i] = y;
            maxXs[i] = x + w; maxYs[i] = y + h;
            tree.insert(ids[i], new Bounds2D(x, y, x + w, y + h));
        }
        assertEquals(count, tree.size());

        List<UUID> out = new ArrayList<>();
        for (int q = 0; q < 100; q++) {
            double qx = rng.nextDouble() * 5000;
            double qy = rng.nextDouble() * 5000;
            double qw = rng.nextDouble() * 500 + 10;
            double qh = rng.nextDouble() * 500 + 10;
            Bounds2D query = new Bounds2D(qx, qy, qx + qw, qy + qh);

            Set<UUID> expected = new HashSet<>();
            for (int i = 0; i < count; i++) {
                if (minXs[i] <= query.maxX && maxXs[i] >= query.minX
                        && minYs[i] <= query.maxY && maxYs[i] >= query.minY) {
                    expected.add(ids[i]);
                }
            }

            out.clear();
            tree.search(query, out);
            assertEquals(expected.size(), out.size(),
                    "query " + q + ": expected " + expected.size() + " got " + out.size());
            assertTrue(expected.containsAll(out), "query " + q + ": extra ids in result");
        }

        for (UUID id : ids) {
            tree.remove(id);
        }
        assertEquals(0, tree.size());
    }

    @Test
    void degenerateZeroAreaBounds() {
        RTree tree = new RTree();
        UUID id = UUID.randomUUID();
        tree.insert(id, new Bounds2D(10, 10, 10, 10)); // zero-area point

        List<UUID> out = new ArrayList<>();
        tree.search(new Bounds2D(10, 10, 10, 10), out); // exact point → found
        assertEquals(1, out.size());
        assertEquals(id, out.get(0));

        out.clear();
        tree.search(new Bounds2D(9, 9, 11, 11), out); // enclosing box → found
        assertEquals(1, out.size());

        out.clear();
        tree.search(new Bounds2D(11, 11, 12, 12), out); // no overlap
        assertTrue(out.isEmpty());
    }

    @Test
    void degenerateNegativeSizeNormalized() {
        RTree tree = new RTree();
        UUID id = UUID.randomUUID();
        tree.insert(id, new Bounds2D(20, 20, 5, 5)); // negative size → should normalize to (5,5)-(20,20)

        List<UUID> out = new ArrayList<>();
        tree.search(new Bounds2D(10, 10, 15, 15), out);
        assertEquals(1, out.size());

        out.clear();
        tree.search(new Bounds2D(0, 0, 30, 30), out);
        assertEquals(1, out.size());

        out.clear();
        tree.search(new Bounds2D(21, 21, 30, 30), out);
        assertTrue(out.isEmpty());
    }

    @Test
    void duplicateIdReplaces() {
        RTree tree = new RTree();
        UUID id = UUID.randomUUID();
        tree.insert(id, new Bounds2D(0, 0, 10, 10));
        tree.insert(id, new Bounds2D(50, 50, 60, 60)); // same id, new bounds

        assertEquals(1, tree.size());

        List<UUID> out = new ArrayList<>();
        tree.search(new Bounds2D(0, 0, 15, 15), out);
        assertTrue(out.isEmpty());

        out.clear();
        tree.search(new Bounds2D(55, 55, 65, 65), out);
        assertEquals(1, out.size());
        assertEquals(id, out.get(0));
    }

    @Test
    void clearEmptiesTree() {
        RTree tree = new RTree();
        tree.insert(UUID.randomUUID(), new Bounds2D(0, 0, 10, 10));
        tree.insert(UUID.randomUUID(), new Bounds2D(50, 50, 60, 60));
        assertEquals(2, tree.size());

        tree.clear();
        assertEquals(0, tree.size());

        List<UUID> out = new ArrayList<>();
        tree.search(new Bounds2D(0, 0, 100, 100), out);
        assertTrue(out.isEmpty());
    }
}
