import java.util.*;

public class Tutorial {
    static String[] inputs = {"0000", "0004", "000c", "2200", "00d0",
            "00e0", "1130", "0028", "113c", "2204", "0010", "0020",
            "0004", "0040", "2208", "0008", "00a0", "0004", "1104",
            "0028", "000c", "0084", "000c", "3390", "00b0", "1100",
            "0028", "0064", "0070", "00d0", "0008", "3394"};
    public static void main(String[] args) {
        List<Cache> caches = new ArrayList<Cache>();
        caches.add(new Cache(16, 8, 1));
        caches.add(new Cache(16, 4, 2));
        caches.add(new Cache(16, 2, 4));
        caches.add(new Cache(16, 1, 8));
        for (Cache c : caches) {
            int count = 0;
            for (String i : inputs) {
                if (c.check(i))
                    count++;
            }
            int com = 0, cap = 0;
            for (Set s : c.sets) {
                com += s.tagUnique.size();
                cap += s.capacity;
            }
            System.out.println("L=" + c.L + ", N=" + c.N + ", K=" + c.K + ": " + count + " hits");
            System.out.println("\t"+com+" compulsory, "+cap+" capacity");
        }
    }
}
class Cache {
    int L, N, K;
    List<Set> sets = new ArrayList<Set>();
    Cache(int L, int N, int K) {
        this.L = L;
        this.N = N;
        this.K = K;
        for (int i = 0; i < N; i++)
            sets.add(i, new Set(K));
    }
    boolean check(String s) {
        int a = (int) Long.parseLong(s, 16);
        int setno = (a >> 4) & ((1 << (int)(Math.log(N)/Math.log(2))) - 1);
        int tag = a >> ((int)(Math.log(N)/Math.log(2)) + 4);
        return sets.get(setno).check(tag, s);
    }
}
class Set {
    List<Tag> tags = new ArrayList<Tag>();
    List<Integer> tagUnique = new ArrayList<Integer>();
    int K, timestamp, capacity;
    Set(int K) {
        this.K = K;
        this.timestamp = 0;
        for (int i = 0; i < K; i++)
            tags.add(i, new Tag());
    }
    // returns true for a hit. it also caches the misses using lru
    public boolean check(int tag, String s) {
        timestamp++; // count timestamp for lru
        boolean isUnique = false;
        if (!tagUnique.contains(tag)) {
            isUnique = true;
            tagUnique.add(tag);
        }
        int index = -1;
        for (int i = 0; i < tags.size(); i++) {
            if (tags.get(i).val == tag) { // check if tag is cached
                index = i;
                break;
            }
        }
        System.out.print(s+": ");
        if (index >= 0) { // cache hit
            Tag t = tags.get(index);
            t.timestamp = timestamp; // update timestamp for lru
            System.out.println("hit");
            return true;
        }
        else { // cache miss
            int oldest = 0, t = Integer.MAX_VALUE;
            for (int i = 0; i < tags.size(); i++) { // find lru
                if (tags.get(i).timestamp < t) {
                    oldest = i;
                    t = tags.get(i).timestamp;
                }
            }
            tags.get(oldest).val = tag;             // replace lru
            tags.get(oldest).timestamp = timestamp; // set write timestamp
            if (!isUnique) {
                System.out.println("capacity miss");
                capacity++;
            }
            else {
                System.out.println("compulsory miss");
            }
        }
        return false;
    }
}
class Tag {
    int val, timestamp;
    Tag() {
        val = -1; // uninitialised state
        timestamp = 0;
    }
}