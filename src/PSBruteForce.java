import java.util.Iterator;

/**
 * PSBruteForce is a Point collection that provides brute force
 * nearest neighbor searching using red-black tree.
 */
public class PSBruteForce<Value> implements PointSearch<Value> {
    RedBlackBST<Point, Value> tree;
    public double minX;
    public double minY;
    public double maxX;
    public double maxY;

    // constructor makes empty collection
    public PSBruteForce() {
        tree = new RedBlackBST<>();
    }

    // add the given Point to KDTree
    public void put(Point p, Value v) {
        if (isEmpty()) {
            minX = p.x();
            minY = p.y();
            maxX = p.x();
            maxY = p.y();
        } else compare(p);
        tree.put(p, v);
    }

    // Helper function to check cases for min and max
    private void compare(Point p) {
        if (p.x() < minX) minX = p.x();
        if (p.y() < minY) minY = p.y();
        if (p.x() > maxX) maxX = p.x();
        if (p.y() > maxY) maxY = p.y();
    }

    public Value get(Point p) {
        if (tree.isEmpty()) return null;
        return tree.get(p);
    }

    public boolean contains(Point p) {
        return tree.contains(p);
    }

    // return an iterable of all points in collection
    public Iterable<Point> points() {
        return tree.keys();
    }

    // return the Point that is closest to the given Point
    public Point nearest(Point p) {
        if (isEmpty()) return null;
        PointDist pd = null;
        for (Point pt : points()) {
            PointDist newPd = new PointDist(pt, p.dist(pt));
            if (pd == null) pd = newPd;
            else if (newPd.compareTo(pd) < 0) pd = newPd;
        }
        return pd.p();
    }

    // return the Value associated to the Point that is closest to the given Point
    public Value getNearest(Point p) {
        return tree.get(nearest(p));
    }

    // return the min and max for all Points in collection.
    // The min-max pair will form a bounding box for all Points.
    // if KDTree is empty, return null.
    public Point min() {
        if (tree.isEmpty()) return null;
        return new Point(minX, minY);
    }
    public Point max() {
        if (tree.isEmpty()) return null;
        return new Point(maxX, maxY);
    }

    // return the k nearest Points to the given Point
    public Iterable<Point> nearest(Point p, int k) {
        MaxPQ<PointDist> pq = new MaxPQ<>();
        for (Point pt : points()) {
            PointDist pd = new PointDist(pt, p.dist(pt));
            pq.insert(pd);
            if (pq.size() > k) pq.delMax();
        }
        Stack<Point> s = new Stack<>();
        for (PointDist pd : pq) {
            s.push(pd.p());
        }
        return s;
    }

    // leave return type as null for brute force implementation
    public Iterable<Partition> partitions() { return null; }

    // return the number of Points in KDTree
    public int size() { return tree.size(); }

    // return whether the KDTree is empty
    public boolean isEmpty() { return tree.isEmpty(); }

    // place your timing code or unit testing here
    public static void main(String[] args) {
        /*
         * Timing tests within the PSKDTree.main()
         */
        PSBruteForce<Integer> test = new PSBruteForce<>();
        test.put(new Point(0.0, 1.0), 0);
        test.put(new Point(1.0, 0.0), 2);
        StdOut.println(test.max());
    }
}
