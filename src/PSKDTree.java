import java.util.Iterator;

/**
 * PSKDTree is a Point collection that provides nearest neighbor searching using
 * 2d tree
 */
public class PSKDTree<Value> implements PointSearch<Value> {

    private class Node {
        Point p;
        Value v;
        Node left, right;
        Partition.Direction dir;
        public Node(Point p, Value v, Partition.Direction dir) {
            this.p = p;
            this.v = v;
            this.dir = dir;
        }
    }

    private Node root;
    private double minX;
    private double minY;
    private double maxX;
    private double maxY;
    private int size;
    private MaxPQ<PointDist> pq;
    private Point nearest;

    // constructor makes empty kD-tree
    public PSKDTree() { root = null; size = 0; }

    // Helper function for less comparator

    // add the given Point to kD-tree
    public void put(Point p, Value v) {
        if (p == null) throw new NullPointerException("Point is null");
        if (isEmpty()) {
            minX = p.x();
            minY = p.y();
            maxX = p.x();
            maxY = p.y();
        } else checkMinsMaxs(p);
        size++;
        root = put(root, p, v, Partition.Direction.LEFTRIGHT);
    }

    private Node put(Node x, Point p, Value v, Partition.Direction dir) {
        if (x == null) return new Node(p, v, dir);
        double rxy = x.p.xy(dir);
        double pxy = p.xy(dir);
        if (pxy < rxy)      x.left = put(x.left, p, v, Partition.nextDirection(dir));
        else if (pxy > rxy) x.right = put(x.right, p, v, Partition.nextDirection(dir));
        else {
            double ryx = x.p.xy(Partition.nextDirection(dir));
            double pyx = p.xy(Partition.nextDirection(dir));
            if (ryx == pyx) { x.v = v; size--; }
            else              x.left = put(x.left, p, v, Partition.nextDirection(dir));
        }
        return x;
    }

    private void checkMinsMaxs(Point p) {
        if (p.x() < minX) minX = p.x();
        if (p.y() < minY) minY = p.y();
        if (p.x() > maxX) maxX = p.x();
        if (p.y() > maxY) maxY = p.y();
    }

    public Value get(Point p) {
        if (isEmpty()) return null;
        if (p == null) throw new NullPointerException("Point is null");
        Node x = root;
        Partition.Direction dir = x.dir;
        while (x != null) {
            if (x.p.equals(p)) return x.v;
            double rxy = x.p.xy(dir);
            double pxy = p.xy(dir);
            if (pxy <= rxy)         x = x.left;
            else                    x = x.right;
            dir = x.dir;
        }
        return null;
    }

    public boolean contains(Point p) {
        return get(p) != null;
    }

    public Value getNearest(Point p) {
        return get(nearest(p));
    }

    // return an iterable of all points in collection
    public Iterable<Point> points() {
        Bag<Point> b = new Bag<>();
        return points(root, b);
    }

    // Recursive function to move through all points in the tree
    private Bag<Point> points(Node x, Bag<Point> b) {
        if (x == null) return b;
        b.add(x.p);
        b = points(x.left, b);
        b = points(x.right, b);
        return b;
    }

    // return an iterable of all partitions that make up the kD-tree
    // How does the Partition data type work? Why does it store 2 points?
    // Partition stores two endpoints of the partition
    // in that case, when new partition is made, it needs to know parent node info
    // Partition best solution: iterate through all points in partitions();
    public Iterable<Partition> partitions() { return null; }

    // return the Point that is closest to the given Point
    public Point nearest(Point p) {
        if (p == null) throw new NullPointerException("Point is null");
        if (root == null) return null;
        nearest = root.p;
        searchNearest(root, p, Partition.Direction.LEFTRIGHT);
        return nearest;
    }

    private void searchNearest(Node x, Point q, Partition.Direction dir) {
        if (x == null) return;
        double pxy = x.p.xy(dir);
        double qxy = q.xy(dir);
        double dist =  qxy - pxy;
        if (x.p.dist(q) < nearest.dist(q)) nearest = x.p;
        if (dist <= 0) {
            searchNearest(x.left, q, Partition.nextDirection(dir));
            if (Math.abs(dist) < nearest.dist(q)) searchNearest(x.right, q, Partition.nextDirection(dir));
        } else {
            searchNearest(x.right, q, Partition.nextDirection(dir));
            if (Math.abs(dist) < nearest.dist(q)) searchNearest(x.left, q, Partition.nextDirection(dir));
        }

    }

    // return the k nearest Points to the given Point
    public Iterable<Point> nearest(Point p, int k) {
        if (p == null) throw new NullPointerException("Point is null");
        if (k == 0 || root == null) return null;
        pq = new MaxPQ<>();
        searchNearestK(root, p, Partition.Direction.LEFTRIGHT, k);
        Bag<Point> s = new Bag<>();
        for (PointDist pd : pq) {
            s.add(pd.p());
        }
        return s;
    }
    private void searchNearestK(Node x, Point q, Partition.Direction dir, int k) {
        if (x == null) return;
        pq.insert(new PointDist(x.p, x.p.dist(q)));
        if (pq.size() > k) pq.delMax();
        double pxy = x.p.xy(dir);
        double qxy = q.xy(dir);
        double dist =  qxy - pxy;
        if (dist <= 0) {
            searchNearestK(x.left, q, Partition.nextDirection(dir), k);
            if (pq.size() < k || Math.abs(dist) < pq.max().d()) searchNearestK(x.right, q, Partition.nextDirection(dir), k);
        } else {
            searchNearestK(x.right, q, Partition.nextDirection(dir), k);
            if (pq.size() < k || Math.abs(dist) < pq.max().d()) searchNearestK(x.left, q, Partition.nextDirection(dir), k);
        }
    }

    // return the min and max for all Points in collection.
    // The min-max pair will form a bounding box for all Points.
    // if kD-tree is empty, return null.
    public Point min() {
        if (isEmpty()) return null;
        return new Point(minX, minY);
    }
    public Point max() {
        if (isEmpty()) return null;
        return new Point(maxX, maxY);
    }

    // return the number of Points in kD-tree
    public int size() { return size; }

    // return whether the kD-tree is empty
    public boolean isEmpty() { return size == 0; }

    // place your timing code or unit testing here
    public static void main(String[] args) {
        PSKDTree<String> tree0 = new PSKDTree<>();
        PSKDTree<String> tree1 = new PSKDTree<>();
        PSBruteForce<String> tree2 = new PSBruteForce<>();
        PSBruteForce<String> tree3 = new PSBruteForce<>();
        // Initialize all trees (file0 = 100k, file1 = 1mil)
        In file0 = new In(args[0]);
        In file1 = new In(args[1]);
        while (!file0.isEmpty()) {
            Point p = new Point(file0.readDouble(), file0.readDouble());
            tree0.put(p, "a");
            tree2.put(p, "c");
        }
        while (!file1.isEmpty()) {
            Point p = new Point(file1.readDouble(), file1.readDouble());
            tree1.put(p, "b");
            tree3.put(p, "d");
        }
        int c0 = 0;
        Stopwatch stopwatch = new Stopwatch();
        while (stopwatch.elapsedTime() < 1.0) {
            tree0.nearest(Point.uniform());
            c0++;
        }
        stopwatch = new Stopwatch();
        int c1 = 0;
        while (stopwatch.elapsedTime() < 1.0) {
            tree1.nearest(Point.uniform());
            c1++;
        }
        stopwatch = new Stopwatch();
        int c2 = 0;
        while (stopwatch.elapsedTime() < 1.0) {
            tree2.nearest(Point.uniform());
            c2++;
        }
        stopwatch = new Stopwatch();
        int c3 = 0;
        while (stopwatch.elapsedTime() < 1.0) {
            tree3.nearest(Point.uniform());
            c3++;
        }
        StdOut.println("Brute force 100k: " + c2 + " operations");
        StdOut.println("Brute force 1mil: " + c3 + " operations");
        StdOut.println("k-d 100k: " + c0 + " operations");
        StdOut.println("k-d 1mil: " + c1 + " operations");
    }

}
