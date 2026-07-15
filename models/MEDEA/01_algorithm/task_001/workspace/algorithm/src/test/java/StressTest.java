import java.util.*;

public class StressTest {
    static final class Edge {
        int to;
        long[] opens, closes, lats;
        Edge(int to, long[] o, long[] c, long[] l) {this.to=to; opens=o; closes=c; lats=l;}
        long earliestArrival(long t){
            long best=Long.MAX_VALUE;
            for(int i=0;i<opens.length;i++){
                if(t>closes[i]) continue;
                long arr = t<=opens[i]? opens[i]+lats[i] : t+lats[i];
                if(arr<best) best=arr;
            }
            return best;
        }
    }

    // brute force: run Dijkstra over (node,mask) without dominance pruning, full state space N * 2^k
    static long brute(int N, List<Edge>[] graph, int[] cap, int s, int t, long start, long deadline, int reqMask){
        if(start>deadline) return -1;
        int maxMask = 1<<12; // not used
        // dist[node][mask] where mask is subset of reqMask (compressed as original bits)
        // We'll map masks to up to 2^popcount but use HashMap for brute
        Map<Integer, Long>[] best = new HashMap[N+1];
        for(int i=1;i<=N;i++) best[i]=new HashMap<>();
        PriorityQueue<long[]> pq = new PriorityQueue<>(Comparator.comparingLong(a->a[0]));
        int startMask = cap[s] & reqMask;
        best[s].put(startMask, start);
        pq.offer(new long[]{start, s, startMask});
        long answer = -1;
        while(!pq.isEmpty()){
            long[] cur = pq.poll();
            long curTime = cur[0];
            int u = (int)cur[1];
            int curMask = (int)cur[2];
            Long b = best[u].get(curMask);
            if(b==null || b!=curTime) continue;
            if(curTime>deadline) continue;
            if(u==t && curMask==reqMask){
                return curTime;
            }
            for(Edge e: graph[u]){
                long arr = e.earliestArrival(curTime);
                if(arr==Long.MAX_VALUE || arr>deadline) continue;
                int nextMask = curMask | (cap[e.to] & reqMask);
                Long old = best[e.to].get(nextMask);
                if(old==null || arr < old){
                    best[e.to].put(nextMask, arr);
                    pq.offer(new long[]{arr, e.to, nextMask});
                }
            }
        }
        return -1;
    }

    // our dominance version
    static class FrontierEntry{int mask; long time; FrontierEntry(int m,long t){mask=m; time=t;}}
    static class State{long time; int node; int mask; State(long t,int n,int m){time=t;node=n;mask=m;}}

    static boolean isDominated(List<FrontierEntry> list, int newMask, long newTime){
        if(list==null) return false;
        for(FrontierEntry e: list){
            if((e.mask & newMask)==newMask && e.time <= newTime) return true;
        }
        return false;
    }

    static long optimized(int N, List<Edge>[] graph, int[] cap, int s, int t, long start, long deadline, int reqMask){
        if(start>deadline) return -1;
        @SuppressWarnings("unchecked")
        List<FrontierEntry>[] front = new ArrayList[N+1];
        for(int i=1;i<=N;i++) front[i]=new ArrayList<>();
        int startMask = cap[s] & reqMask;
        front[s].add(new FrontierEntry(startMask, start));
        PriorityQueue<State> pq = new PriorityQueue<>(Comparator.comparingLong(a->a.time));
        pq.offer(new State(start,s,startMask));
        while(!pq.isEmpty()){
            State cur = pq.poll();
            long curTime = cur.time;
            int u = cur.node;
            int curMask = cur.mask;
            if(curTime>deadline) break;
            List<FrontierEntry> listU = front[u];
            boolean exact=false, dom=false;
            for(FrontierEntry e: listU){
                if(e.mask==curMask && e.time==curTime) exact=true;
                if((e.mask & curMask)==curMask && e.time<=curTime){
                    if(e.mask!=curMask || e.time!=curTime) {dom=true; break;}
                }
            }
            if(!exact) dom=true;
            if(dom) continue;
            if(u==t && curMask==reqMask) return curTime;
            for(Edge e: graph[u]){
                long arr = e.earliestArrival(curTime);
                if(arr==Long.MAX_VALUE || arr>deadline) continue;
                int nextMask = curMask | (cap[e.to] & reqMask);
                List<FrontierEntry> listV = front[e.to];
                if(isDominated(listV, nextMask, arr)) continue;
                Iterator<FrontierEntry> it = listV.iterator();
                while(it.hasNext()){
                    FrontierEntry fe = it.next();
                    if((nextMask & fe.mask)==fe.mask && arr <= fe.time) it.remove();
                }
                listV.add(new FrontierEntry(nextMask, arr));
                pq.offer(new State(arr, e.to, nextMask));
            }
        }
        return -1;
    }

    public static void main(String[] args){
        Random rnd = new Random(42);
        for(int test=0; test<1000; test++){
            int N = rnd.nextInt(5)+1;
            int M = rnd.nextInt(8)+1;
            int K = rnd.nextInt(4)+1;
            int allowed = (1<<K)-1;
            int[] cap = new int[N+1];
            for(int i=1;i<=N;i++) cap[i]=rnd.nextInt(allowed+1);
            @SuppressWarnings("unchecked")
            List<Edge>[] graph = new ArrayList[N+1];
            for(int i=1;i<=N;i++) graph[i]=new ArrayList<>();
            for(int i=0;i<M;i++){
                int u = rnd.nextInt(N)+1;
                int v = rnd.nextInt(N)+1;
                int W = rnd.nextInt(3)+1;
                long[] o = new long[W];
                long[] c = new long[W];
                long[] l = new long[W];
                for(int j=0;j<W;j++){
                    long open = rnd.nextInt(20);
                    long close = open + rnd.nextInt(20);
                    long lat = rnd.nextInt(10)+1;
                    o[j]=open; c[j]=close; l[j]=lat;
                }
                graph[u].add(new Edge(v,o,c,l));
            }
            int s = rnd.nextInt(N)+1;
            int t = rnd.nextInt(N)+1;
            long start = rnd.nextInt(10);
            long deadline = start + rnd.nextInt(50);
            int req = rnd.nextInt(allowed+1);
            long b = brute(N, graph, cap, s, t, start, deadline, req);
            long o2 = optimized(N, graph, cap, s, t, start, deadline, req);
            if(b!=o2){
                System.out.println("Mismatch test "+test);
                System.out.println("N="+N+" M="+M+" K="+K);
                System.out.println("caps "+Arrays.toString(cap));
                System.out.println("s="+s+" t="+t+" start="+start+" deadline="+deadline+" req="+req);
                System.out.println("brute="+b+" opt="+o2);
                System.exit(1);
            }
        }
        System.out.println("All 1000 random tests passed");
    }
}
