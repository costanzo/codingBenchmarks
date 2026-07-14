import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Temporal Dependency Router.
 *
 * <p>Solves earliest-arrival routing over a directed temporal graph whose edges are only usable
 * during specific time windows, subject to a required set of capabilities that must be accumulated
 * along the traversed route (including the source and target services).
 *
 * <p>Search strategy: per-query time-ordered (Dijkstra-like) label-setting search over states
 * {@code (node, capabilityMask)}. Because only the bits present in a query's required mask can ever
 * matter, capability masks are projected onto the required mask, which collapses the state space to
 * {@code node x 2^popcount(requiredMask)}. Exact-state deduplication plus superset-dominance pruning
 * eliminate redundant labels: a label is discarded if the same node already holds a label whose mask
 * is a superset and whose arrival time is no later.
 *
 * <p>All time arithmetic uses {@code long}. Input parsing is done with a hand-rolled byte reader,
 * adjacency is stored in CSR form, and the frontier uses a manual binary heap over primitive arrays
 * to avoid per-state object allocation in the hot loop.
 */
public final class TemporalDependencyRouter {

    private TemporalDependencyRouter() {
    }

    public static void main(String[] args) throws Exception {
        FastReader in = new FastReader(System.in);
        StringBuilder sb = new StringBuilder();
        Solver solver = Solver.read(in);
        int q = solver.q;
        for (int i = 0; i < q; i++) {
            int source = (int) in.nextLong();
            int target = (int) in.nextLong();
            long startTime = in.nextLong();
            long deadline = in.nextLong();
            int requiredMask = (int) in.nextLong();
            long ans = solver.query(source, target, startTime, deadline, requiredMask);
            sb.append(ans).append('\n');
        }
        OutputStream out = System.out;
        out.write(sb.toString().getBytes());
        out.flush();
    }

    /**
     * Immutable graph container plus per-query search. Reusable scratch buffers are held on the
     * instance and reset lazily per query using a generation stamp so repeated queries avoid
     * re-zeroing large arrays.
     */
    static final class Solver {
        final int n;
        final int m;
        final int k;
        final int q;
        final int[] cap; // cap[node], 1-indexed

        // CSR adjacency over edges
        final int[] head;      // head[node] .. head[node+1] index range into edge arrays
        final int[] edgeTo;    // edgeTo[e]
        final int[] winStart;  // window range per edge into window arrays
        final int[] winEnd;
        final long[] wOpen;
        final long[] wClose;
        final long[] wLat;

        // Per-query dominance table. For a given node we store labels as (mask,time) pairs.
        // Indexed by node in CSR-like fashion but sized dynamically per query using hashing-free
        // small arrays because the number of distinct masks per node is <= 2^popcount(R).
        Solver(int n, int m, int k, int q, int[] cap, int[] head, int[] edgeTo,
               int[] winStart, int[] winEnd, long[] wOpen, long[] wClose, long[] wLat) {
            this.n = n;
            this.m = m;
            this.k = k;
            this.q = q;
            this.cap = cap;
            this.head = head;
            this.edgeTo = edgeTo;
            this.winStart = winStart;
            this.winEnd = winEnd;
            this.wOpen = wOpen;
            this.wClose = wClose;
            this.wLat = wLat;
        }

        static Solver read(FastReader in) throws IOException {
            int n = (int) in.nextLong();
            int m = (int) in.nextLong();
            int k = (int) in.nextLong();
            int q = (int) in.nextLong();

            int[] cap = new int[n + 1];
            for (int i = 1; i <= n; i++) {
                cap[i] = (int) in.nextLong();
            }

            int[] edgeToTmp = new int[m];
            int[] edgeFrom = new int[m];
            int[] winStart = new int[m];
            int[] winEnd = new int[m];

            // first pass: read edges and windows into growable arrays
            long[] wOpen = new long[Math.max(1, m)];
            long[] wClose = new long[Math.max(1, m)];
            long[] wLat = new long[Math.max(1, m)];
            int wCount = 0;

            int[] degree = new int[n + 2];

            for (int e = 0; e < m; e++) {
                int u = (int) in.nextLong();
                int v = (int) in.nextLong();
                int w = (int) in.nextLong();
                edgeFrom[e] = u;
                edgeToTmp[e] = v;
                degree[u]++;
                winStart[e] = wCount;
                if (wCount + w > wOpen.length) {
                    int newCap = Math.max(wOpen.length * 2, wCount + w);
                    wOpen = grow(wOpen, newCap);
                    wClose = grow(wClose, newCap);
                    wLat = grow(wLat, newCap);
                }
                for (int j = 0; j < w; j++) {
                    wOpen[wCount] = in.nextLong();
                    wClose[wCount] = in.nextLong();
                    wLat[wCount] = in.nextLong();
                    wCount++;
                }
                winEnd[e] = wCount;
            }

            // Build CSR ordering of edges by source node.
            int[] head = new int[n + 2];
            for (int i = 1; i <= n; i++) {
                head[i + 1] = head[i] + degree[i];
            }
            // head[node] gives start; use a cursor copy
            int[] cursor = new int[n + 2];
            System.arraycopy(head, 0, cursor, 0, n + 2);
            // shift: head[node] should be prefix sum starting index for node
            // Recompute properly: head[1]=0, head[node]=head[node-1]+degree[node-1]
            head[1] = 0;
            for (int i = 2; i <= n + 1; i++) {
                head[i] = head[i - 1] + degree[i - 1];
            }
            System.arraycopy(head, 0, cursor, 0, n + 2);

            int[] edgeTo = new int[m];
            int[] winStartCsr = new int[m];
            int[] winEndCsr = new int[m];
            for (int e = 0; e < m; e++) {
                int u = edgeFrom[e];
                int pos = cursor[u]++;
                edgeTo[pos] = edgeToTmp[e];
                winStartCsr[pos] = winStart[e];
                winEndCsr[pos] = winEnd[e];
            }

            return new Solver(n, m, k, q, cap, head, edgeTo, winStartCsr, winEndCsr,
                    wOpen, wClose, wLat);
        }

        private static long[] grow(long[] a, int newLen) {
            long[] b = new long[newLen];
            System.arraycopy(a, 0, b, 0, a.length);
            return b;
        }

        // ---- Per-query state ----
        // Manual binary heap over (time, packedState) where packedState = node*maskCount + mask.
        private long[] heapTime = new long[16];
        private long[] heapState = new long[16];
        private int heapSize = 0;

        // Dominance labels per node: for each node a small list of (mask,time).
        // Stored via generation-stamped arrays to allow O(1) reset between queries.
        private long[][] nodeMasks = null; // lazily grown per node
        private long[][] nodeTimes = null;
        private int[] nodeCount = null;
        private int[] nodeGen = null;
        private int gen = 0;

        private void ensureQueryBuffers() {
            if (nodeMasks == null) {
                nodeMasks = new long[n + 1][];
                nodeTimes = new long[n + 1][];
                nodeCount = new int[n + 1];
                nodeGen = new int[n + 1];
            }
        }

        long query(int source, int target, long startTime, long deadline, int requiredMask) {
            ensureQueryBuffers();
            gen++;
            heapSize = 0;

            // Project everything onto requiredMask bits only.
            int startMask = cap[source] & requiredMask;

            if (startTime > deadline) {
                // Even the source cannot be "arrived at" within the deadline in a meaningful sense;
                // but source is reached at startTime. If startTime>deadline no route can be valid.
                return -1L;
            }

            pushLabel(source, startMask, startTime, requiredMask);

            while (heapSize > 0) {
                long t = heapTime[0];
                long packed = heapState[0];
                popTop();

                if (t > deadline) {
                    // All remaining labels have time >= t > deadline.
                    return -1L;
                }

                int maskCount = requiredMask + 1; // masks range 0..requiredMask, but only subsets valid
                int node = (int) (packed / maskCount);
                int mask = (int) (packed % maskCount);

                // Stale check: if a dominating label now exists, skip.
                if (isStale(node, mask, t)) {
                    continue;
                }

                if (node == target && mask == requiredMask) {
                    return t;
                }

                // Relax outgoing edges.
                int hs = head[node];
                int he = head[node + 1];
                for (int e = hs; e < he; e++) {
                    int v = edgeTo[e];
                    long arrival = bestArrival(e, t);
                    if (arrival < 0 || arrival > deadline) {
                        continue;
                    }
                    int newMask = mask | (cap[v] & requiredMask);
                    pushLabel(v, newMask, arrival, requiredMask);
                }
            }
            return -1L;
        }

        /**
         * Earliest arrival over all windows of edge index {@code e} given current time {@code t},
         * or -1 if the edge cannot be used.
         */
        private long bestArrival(int e, long t) {
            int s = winStart[e];
            int en = winEnd[e];
            long best = -1L;
            for (int w = s; w < en; w++) {
                long close = wClose[w];
                if (t > close) {
                    continue; // window expired for this current time
                }
                long open = wOpen[w];
                long depart = t > open ? t : open; // max(t, open)
                long arr = depart + wLat[w];
                if (best < 0 || arr < best) {
                    best = arr;
                }
            }
            return best;
        }

        /**
         * Attempt to register a label (node, mask, time). Returns after inserting into the heap if it
         * is not dominated by an existing label. A label is dominated when the node already holds a
         * label with a superset mask and a time no greater than this one.
         */
        private void pushLabel(int node, int mask, long time, int requiredMask) {
            if (nodeGen[node] != gen) {
                nodeGen[node] = gen;
                nodeCount[node] = 0;
                if (nodeMasks[node] == null) {
                    nodeMasks[node] = new long[4];
                    nodeTimes[node] = new long[4];
                }
            }
            long[] masks = nodeMasks[node];
            long[] times = nodeTimes[node];
            int cnt = nodeCount[node];

            // Dominance check + prune labels this new one dominates.
            for (int i = 0; i < cnt; i++) {
                int em = (int) masks[i];
                long et = times[i];
                // existing dominates new: existing mask superset of new AND existing time <= new time
                if ((em & mask) == mask && et <= time) {
                    return;
                }
            }
            // Insert; also drop any existing labels dominated by the new one to keep list small.
            int write = 0;
            for (int i = 0; i < cnt; i++) {
                int em = (int) masks[i];
                long et = times[i];
                boolean dominatedByNew = (mask & em) == em && time <= et;
                if (!dominatedByNew) {
                    masks[write] = em;
                    times[write] = et;
                    write++;
                }
            }
            cnt = write;
            if (cnt == masks.length) {
                int nl = masks.length * 2;
                long[] nm = new long[nl];
                long[] nt = new long[nl];
                System.arraycopy(masks, 0, nm, 0, cnt);
                System.arraycopy(times, 0, nt, 0, cnt);
                nodeMasks[node] = nm;
                nodeTimes[node] = nt;
                masks = nm;
                times = nt;
            }
            masks[cnt] = mask;
            times[cnt] = time;
            nodeCount[node] = cnt + 1;

            int maskCount = requiredMask + 1;
            long packed = (long) node * maskCount + mask;
            heapPush(time, packed);
        }

        /** A popped label is stale if no stored label for that node matches (mask,time) exactly
         *  as the best, i.e. a dominating label with strictly better standing now exists. */
        private boolean isStale(int node, int mask, long time) {
            if (nodeGen[node] != gen) {
                return true;
            }
            long[] masks = nodeMasks[node];
            long[] times = nodeTimes[node];
            int cnt = nodeCount[node];
            for (int i = 0; i < cnt; i++) {
                int em = (int) masks[i];
                long et = times[i];
                if ((em & mask) == mask && et <= time) {
                    // dominating (or equal) label exists; this popped label is only fresh if it IS
                    // that label (same mask and same time).
                    if (em == mask && et == time) {
                        return false;
                    }
                    return true;
                }
            }
            return true;
        }

        // ---- manual binary heap keyed by time ----
        private void heapPush(long time, long packed) {
            if (heapSize == heapTime.length) {
                int nl = heapTime.length * 2;
                long[] nt = new long[nl];
                long[] ns = new long[nl];
                System.arraycopy(heapTime, 0, nt, 0, heapSize);
                System.arraycopy(heapState, 0, ns, 0, heapSize);
                heapTime = nt;
                heapState = ns;
            }
            int i = heapSize++;
            heapTime[i] = time;
            heapState[i] = packed;
            while (i > 0) {
                int parent = (i - 1) >>> 1;
                if (heapTime[parent] <= heapTime[i]) {
                    break;
                }
                swap(parent, i);
                i = parent;
            }
        }

        private void popTop() {
            heapSize--;
            if (heapSize > 0) {
                heapTime[0] = heapTime[heapSize];
                heapState[0] = heapState[heapSize];
                int i = 0;
                while (true) {
                    int l = 2 * i + 1;
                    int r = 2 * i + 2;
                    int smallest = i;
                    if (l < heapSize && heapTime[l] < heapTime[smallest]) {
                        smallest = l;
                    }
                    if (r < heapSize && heapTime[r] < heapTime[smallest]) {
                        smallest = r;
                    }
                    if (smallest == i) {
                        break;
                    }
                    swap(smallest, i);
                    i = smallest;
                }
            }
        }

        private void swap(int a, int b) {
            long t = heapTime[a];
            heapTime[a] = heapTime[b];
            heapTime[b] = t;
            long s = heapState[a];
            heapState[a] = heapState[b];
            heapState[b] = s;
        }
    }

    /** Minimal fast reader for signed decimal longs from a byte stream. */
    static final class FastReader {
        private final DataInputStream din;
        private final byte[] buffer = new byte[1 << 16];
        private int bufferPointer = 0;
        private int bytesRead = 0;

        FastReader(InputStream in) {
            din = new DataInputStream(in);
        }

        long nextLong() throws IOException {
            int b = read();
            while (b != -1 && b != '-' && (b < '0' || b > '9')) {
                b = read();
            }
            if (b == -1) {
                throw new IOException("Unexpected EOF while reading number");
            }
            boolean neg = false;
            if (b == '-') {
                neg = true;
                b = read();
            }
            long ret = 0;
            while (b >= '0' && b <= '9') {
                ret = ret * 10 + (b - '0');
                b = read();
            }
            return neg ? -ret : ret;
        }

        private int read() throws IOException {
            if (bufferPointer == bytesRead) {
                fillBuffer();
                if (bytesRead == -1) {
                    return -1;
                }
            }
            return buffer[bufferPointer++];
        }

        private void fillBuffer() throws IOException {
            bytesRead = din.read(buffer, 0, buffer.length);
            bufferPointer = 0;
        }
    }
}
