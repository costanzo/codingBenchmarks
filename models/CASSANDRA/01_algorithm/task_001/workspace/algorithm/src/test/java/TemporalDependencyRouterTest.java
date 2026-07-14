import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * Standalone test harness (no external test framework) for {@link TemporalDependencyRouter}.
 *
 * <p>Each case feeds an input document to the router's {@code main} via a redirected
 * {@code System.in}/{@code System.out} and compares the produced stdout to an expected value.
 * Exit code is non-zero if any case fails.
 */
public final class TemporalDependencyRouterTest {

    private static int failures = 0;
    private static int total = 0;

    public static void main(String[] args) throws Exception {
        // Sample from the problem statement.
        run("sample",
                "4 4 3 3\n" +
                "1\n2\n4\n0\n" +
                "1 2 2\n0 10 5\n20 30 1\n" +
                "2 4 1\n6 25 4\n" +
                "1 3 1\n0 100 3\n" +
                "3 4 1\n4 8 10\n" +
                "1 4 0 20 3\n" +
                "1 4 0 20 5\n" +
                "1 4 11 40 3\n",
                "10\n14\n25\n");

        // Source equals target, capabilities already satisfied at source.
        run("source_equals_target_satisfied",
                "2 1 2 1\n" +
                "3\n0\n" +
                "1 2 1\n0 100 5\n" +
                "1 1 7 50 3\n",
                "7\n");

        // Source equals target but capabilities NOT satisfied -> must leave and return.
        // Node1 cap=1(bit0), node2 cap=2(bit1). Need mask 3 at node1.
        // 1->2 window [0,100] lat 5, 2->1 window [0,100] lat 5. Start t=0.
        // Route: 1(t0,mask1) ->2(t5,mask3) ->1(t10,mask3). Answer 10.
        run("source_equals_target_needs_roundtrip",
                "2 2 2 1\n" +
                "1\n2\n" +
                "1 2 1\n0 100 5\n" +
                "2 1 1\n0 100 5\n" +
                "1 1 0 50 3\n",
                "10\n");

        // Capability mask 0: only earliest arrival matters.
        run("mask_zero",
                "3 2 2 1\n" +
                "0\n0\n0\n" +
                "1 2 1\n0 100 2\n" +
                "2 3 1\n0 100 3\n" +
                "1 3 0 100 0\n",
                "5\n");

        // Unreachable target.
        run("unreachable",
                "3 1 1 1\n" +
                "1\n0\n0\n" +
                "1 2 1\n0 100 2\n" +
                "1 3 0 100 0\n",
                "-1\n");

        // Deadline exactly equal to arrival time (should succeed).
        run("deadline_exact",
                "2 1 1 1\n" +
                "1\n0\n" +
                "1 2 1\n0 100 5\n" +
                "1 2 0 5 0\n",
                "5\n");

        // Deadline one less than arrival (should fail).
        run("deadline_miss",
                "2 1 1 1\n" +
                "1\n0\n" +
                "1 2 1\n0 100 5\n" +
                "1 2 0 4 0\n",
                "-1\n");

        // Expired window forces waiting on a later window (query 3 style).
        run("expired_then_later_window",
                "2 1 0 1\n" +
                "0\n0\n" +
                "1 2 2\n0 5 1\n20 30 2\n" +
                "1 2 11 40 0\n",
                "22\n");

        // Weaker-early vs stronger-late tradeoff.
        // Need mask 2 (bit1) at target node3.
        // Path A: 1->3 direct arrives early but never collects bit1 (node3 cap=0) => invalid.
        // Path B: 1->2 (collect bit1) ->3 arrives later but valid.
        run("weak_early_vs_strong_late",
                "3 3 2 1\n" +
                "0\n2\n0\n" +
                "1 3 1\n0 100 1\n" +
                "1 2 1\n0 100 3\n" +
                "2 3 1\n0 100 3\n" +
                "1 3 0 100 2\n",
                "6\n");

        // Multiple windows: choose the one giving earliest valid arrival.
        // Window 3 opens at t=10 (must wait) -> arr=15; window 2 open now -> arr=40. Best is 15.
        run("multi_window_choose_best",
                "2 1 0 1\n" +
                "0\n0\n" +
                "1 2 3\n0 100 50\n0 100 40\n10 100 5\n" +
                "1 2 0 100 0\n",
                "15\n");

        // startTime after deadline -> -1.
        run("start_after_deadline",
                "2 1 0 1\n" +
                "0\n0\n" +
                "1 2 1\n0 100 1\n" +
                "1 2 50 40 0\n",
                "-1\n");

        // Parallel edges between same pair with different windows/latency.
        run("parallel_edges",
                "2 2 0 1\n" +
                "0\n0\n" +
                "1 2 1\n0 100 20\n" +
                "1 2 1\n0 100 7\n" +
                "1 2 0 100 0\n",
                "7\n");

        System.out.println("\n" + (total - failures) + "/" + total + " tests passed.");
        if (failures > 0) {
            System.exit(1);
        }
    }

    private static void run(String name, String input, String expected) throws Exception {
        total++;
        InputStream oldIn = System.in;
        PrintStream oldOut = System.out;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            System.setOut(new PrintStream(bos));
            TemporalDependencyRouter.main(new String[0]);
        } finally {
            System.setIn(oldIn);
            System.setOut(oldOut);
        }
        String actual = bos.toString();
        if (actual.equals(expected)) {
            System.out.println("PASS  " + name);
        } else {
            failures++;
            System.out.println("FAIL  " + name
                    + "\n  expected: " + expected.replace("\n", "\\n")
                    + "\n  actual:   " + actual.replace("\n", "\\n"));
        }
    }
}
