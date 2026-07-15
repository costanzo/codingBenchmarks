import java.io.*;
import java.util.*;

public final class TemporalDependencyRouter {

    static final class Edge {
        final int to;
        final long[] opens;
        final long[] closes;
        final long[] lats;
        final int wCount;
        Edge(int to, long[] opens, long[] closes, long[] lats) {
            this.to = to;
            this.opens = opens;
            this.closes = closes;
            this.lats = lats;
            this.wCount = opens.length;
        }
        // earliest arrival achievable if departure time = t, or INF
        long earliestArrival(long t) {
            long best = Long.MAX_VALUE;
            for (int i = 0; i < wCount; i++) {
                long close = closes[i];
                if (t > close) continue;
                long open = opens[i];
                long lat = lats[i];
                long arr = (t <= open ? open + lat : t + lat);
                if (arr < best) best = arr;
            }
            return best;
        }
    }

    static final class FrontierEntry {
        int mask;
        long time;
        FrontierEntry(int mask, long time) {
            this.mask = mask;
            this.time = time;
        }
    }

    static final class State {
        long time;
        int node;
        int mask;
        State(long time, int node, int mask) {
            this.time = time;
            this.node = node;
            this.mask = mask;
        }
    }

    static final class NodeTime {
        long time;
        int node;
        NodeTime(long time, int node) { this.time = time; this.node = node; }
    }

    // ---------- fast scanner ----------
    static final class FastScanner {
        private final InputStream in;
        private final byte[] buffer = new byte[1 << 16];
        private int ptr = 0, len = 0;
        FastScanner(InputStream in) { this.in = in; }
        private int readByte() throws IOException {
            if (ptr >= len) {
                len = in.read(buffer);
                ptr = 0;
                if (len <= 0) return -1;
            }
            return buffer[ptr++];
        }
        long nextLong() throws IOException {
            int c;
            do {
                c = readByte();
                if (c == -1) return Long.MIN_VALUE;
            } while (c <= ' ');
            boolean neg = false;
            if (c == '-') {
                neg = true;
                c = readByte();
            }
            long val = 0;
            while (c > ' ') {
                val = val * 10 + (c - '0');
                c = readByte();
            }
            return neg ? -val : val;
        }
        int nextInt() throws IOException {
            long v = nextLong();
            return (int) v;
        }
    }

    // ---------- dominance helpers ----------
    static boolean isDominated(List<FrontierEntry> list, int newMask, long newTime) {
        if (list == null || list.isEmpty()) return false;
        for (FrontierEntry e : list) {
            // e.mask superset of newMask ?
            if ( (e.mask & newMask) == newMask && e.time <= newTime) {
                return true;
            }
        }
        return false;
    }

    // removes entries dominated by new entry, assumes not dominated itself
    static void removeDominated(List<FrontierEntry> list, int newMask, long newTime) {
        if (list == null) return;
        Iterator<FrontierEntry> it = list.iterator();
        while (it.hasNext()) {
            FrontierEntry e = it.next();
            if ( (newMask & e.mask) == e.mask && newTime <= e.time) {
                // new dominates e
                // avoid removing if exactly same (but this case would have been caught as dominated earlier)
                it.remove();
            }
        }
    }

    // check if cur state is still alive (not removed and not dominated by another entry in its frontier)
    static boolean isStateDominated(List<FrontierEntry> list, State cur) {
        if (list == null) return true; // no frontier => stale
        boolean foundExact = false;
        for (FrontierEntry e : list) {
            if (e.mask == cur.mask && e.time == cur.time) foundExact = true;
            // check dominating other than exact equality
            if ( (e.mask & cur.mask) == cur.mask && e.time <= cur.time ) {
                if (e.mask != cur.mask || e.time != cur.time) {
                    return true;
                }
            }
        }
        // if exact not found, it was removed -> dominated
        return !foundExact;
    }

    // --------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        FastScanner fs = new FastScanner(System.in);
        long first = fs.nextLong();
        if (first == Long.MIN_VALUE) return; // empty input
        int N = (int) first;
        int M = fs.nextInt();
        int K = fs.nextInt();
        int Q = fs.nextInt();

        int allowedMask;
        if (K <= 0) allowedMask = 0;
        else if (K >= 31) allowedMask = -1;
        else allowedMask = (1 << K) - 1;

        int[] cap = new int[N + 1];
        for (int i = 1; i <= N; i++) {
            int cm = fs.nextInt();
            // sanitize to allowed bits, but keep original for masking? Actually extra bits beyond K should not help.
            if (K >= 0 && K < 31) cm &= allowedMask;
            cap[i] = cm;
        }

        List<Edge>[] graph = new ArrayList[N + 1];
        for (int i = 1; i <= N; i++) graph[i] = new ArrayList<>();

        for (int i = 0; i < M; i++) {
            int u = fs.nextInt();
            int v = fs.nextInt();
            int W = fs.nextInt();
            long[] opens = new long[W];
            long[] closes = new long[W];
            long[] lats = new long[W];
            for (int j = 0; j < W; j++) {
                long o = fs.nextLong();
                long c = fs.nextLong();
                long l = fs.nextLong();
                opens[j] = o;
                closes[j] = c;
                lats[j] = l;
            }
            if (u < 1 || u > N) continue; // defensive
            Edge e = new Edge(v, opens, closes, lats);
            graph[u].add(e);
        }

        // reusable structures for simple (capability-free) searches
        long[] dist = new long[N + 1];
        int[] distVer = new int[N + 1];
        int distCur = 1;

        // reusable structures for capability searches
        List<FrontierEntry>[] frontiers = new ArrayList[N + 1];
        int[] frontVer = new int[N + 1];
        int frontCur = 1;

        StringBuilder out = new StringBuilder();

        for (int qi = 0; qi < Q; qi++) {
            int source = fs.nextInt();
            int target = fs.nextInt();
            long startTime = fs.nextLong();
            long deadline = fs.nextLong();
            int reqMaskRaw = fs.nextInt();
            int reqMask = reqMaskRaw;
            // mask sanitization for allowed range check
            if (K >= 0 && K < 31) {
                // if req contains bits outside allowed, impossible
                if ((reqMask & ~allowedMask) != 0) {
                    out.append(-1).append('\n');
                    continue;
                }
            } else if (K == 0 && reqMask != 0) {
                out.append(-1).append('\n');
                continue;
            }

            if (startTime > deadline) {
                // even zero travel not allowed if source==target still start>deadline => -1
                // However if source==target and we consider waiting? Arrival is start itself, so still >deadline => -1
                out.append(-1).append('\n');
                continue;
            }

            // Mask for relevant bits only
            int relevantReq = reqMask; // since already limited to K bits, it's subset
            // trivial case: source==target
            if (source == target) {
                int have = cap[source] & relevantReq;
                if (have == relevantReq) {
                    // earliest is startTime
                    out.append(startTime).append('\n');
                    continue;
                }
                // otherwise need to potentially go out and back, fall through to search
            }

            if (relevantReq == 0) {
                // simple earliest path
                long ans = earliestPathSimple(source, target, startTime, deadline, graph, dist, distVer, distCur);
                distCur++;
                if (distCur == Integer.MAX_VALUE) {
                    // reset version arrays if overflow (unlikely)
                    Arrays.fill(distVer, 0);
                    distCur = 1;
                }
                out.append(ans).append('\n');
            } else {
                long ans = earliestPathWithCaps(source, target, startTime, deadline, relevantReq, cap, graph, frontiers, frontVer, frontCur);
                frontCur++;
                if (frontCur == Integer.MAX_VALUE) {
                    Arrays.fill(frontVer, 0);
                    frontCur = 1;
                }
                out.append(ans).append('\n');
            }
        }

        System.out.print(out.toString());
    }

    // simple Dijkstra without capabilities
    static long earliestPathSimple(int s, int t, long start, long deadline,
                                   List<Edge>[] graph, long[] dist, int[] ver, int curVer) {
        if (s == t) return start; // already handled, but here start<=deadline

        PriorityQueue<NodeTime> pq = new PriorityQueue<>(Comparator.comparingLong(a -> a.time));
        dist[s] = start;
        ver[s] = curVer;
        pq.offer(new NodeTime(start, s));

        while (!pq.isEmpty()) {
            NodeTime cur = pq.poll();
            long curTime = cur.time;
            int u = cur.node;
            if (ver[u] != curVer || dist[u] != curTime) continue; // stale
            if (curTime > deadline) break; // minimal exceeds deadline
            if (u == t) return curTime;
            for (Edge e : graph[u]) {
                long arr = e.earliestArrival(curTime);
                if (arr == Long.MAX_VALUE) continue;
                if (arr > deadline) continue;
                int v = e.to;
                if (ver[v] != curVer || arr < dist[v]) {
                    dist[v] = arr;
                    ver[v] = curVer;
                    pq.offer(new NodeTime(arr, v));
                }
            }
        }
        return -1;
    }

    // Dijkstra over (node, mask) with dominance pruning
    static long earliestPathWithCaps(int s, int t, long start, long deadline, int reqMask,
                                     int[] cap, List<Edge>[] graph,
                                     List<FrontierEntry>[] frontiers, int[] frontVer, int frontCur) {

        // fast path: check if impossible due to total available capabilities in graph?
        // we could precompute total OR of all caps, but not necessarily reachable; skip.

        int startMask = cap[s] & reqMask;

        // get frontier for s
        List<FrontierEntry> fS;
        if (frontVer[s] != frontCur || frontiers[s] == null) {
            fS = new ArrayList<>(4);
            frontiers[s] = fS;
            frontVer[s] = frontCur;
        } else {
            fS = frontiers[s];
            fS.clear();
        }
        fS.add(new FrontierEntry(startMask, start));

        PriorityQueue<State> pq = new PriorityQueue<>(Comparator.comparingLong(a -> a.time));
        pq.offer(new State(start, s, startMask));

        // helper to get frontier list for node (create if needed)
        // We'll inline for performance

        while (!pq.isEmpty()) {
            State cur = pq.poll();
            long curTime = cur.time;
            int u = cur.node;
            int curMask = cur.mask;

            if (curTime > deadline) break; // smallest time exceeds deadline

            // Check if state still valid
            List<FrontierEntry> listU = (frontVer[u] == frontCur) ? frontiers[u] : null;
            if (listU == null) continue;
            // dominance check (is cur dominated by another entry)
            boolean dominated = false;
            boolean exactFound = false;
            // small optimization: if list size is 1, skip loops
            for (FrontierEntry e : listU) {
                if (e.mask == curMask && e.time == curTime) exactFound = true;
                if ((e.mask & curMask) == curMask && e.time <= curTime) {
                    if (e.mask != curMask || e.time != curTime) {
                        dominated = true;
                        break;
                    }
                }
            }
            if (!exactFound) dominated = true; // removed earlier
            if (dominated) continue;

            if (u == t && curMask == reqMask) {
                return curTime;
            }

            for (Edge e : graph[u]) {
                long arr = e.earliestArrival(curTime);
                if (arr == Long.MAX_VALUE) continue;
                if (arr > deadline) continue;
                int v = e.to;
                int capV = cap[v] & reqMask;
                int nextMask = curMask | capV;

                // get frontier list for v
                List<FrontierEntry> listV;
                if (frontVer[v] != frontCur || frontiers[v] == null) {
                    listV = new ArrayList<>(4);
                    frontiers[v] = listV;
                    frontVer[v] = frontCur;
                } else {
                    listV = frontiers[v];
                }

                // check dominance
                if (isDominated(listV, nextMask, arr)) continue;

                // remove dominated
                // manual loop to avoid ConcurrentModification issues with iterator removal but we have method
                // we want to remove in iterator
                Iterator<FrontierEntry> it = listV.iterator();
                while (it.hasNext()) {
                    FrontierEntry fe = it.next();
                    if ((nextMask & fe.mask) == fe.mask && arr <= fe.time) {
                        it.remove();
                    }
                }
                listV.add(new FrontierEntry(nextMask, arr));
                pq.offer(new State(arr, v, nextMask));
            }
        }
        return -1;
    }
}
