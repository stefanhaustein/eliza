package com.github.stefanhaustein.eliza;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Eliza main class.
 * Stores the processed script.
 * Does the input transformations.
 */
public class Eliza {
    Map<String, Key> keys = new LinkedHashMap<>();
    List<Set<String>> synonymList = new ArrayList<>();
    Map<String, String> pre = new LinkedHashMap<>();
    Map<String, String> post = new LinkedHashMap<>();
    String initial = "Hello.";
    String finl = "Goodbye.";
    List<String> quit = new ArrayList<>();

    /**
     * Memory
     */
    List<String> memory = new ArrayList<>();

    boolean finished = false;

    /**
     * Process a line of script input.
     */
    public void load(Iterable<String> lines)  {
        List<DecompositionRule> currentDecomposition = null;
        List<String> currentReassembly = null;

        int lineNumber = 0;
        for (String s : lines) {
            lineNumber++;

            String parts[] = new String[4];

            if (Text.match(s, "*reasmb: *", parts)) {
                if (currentReassembly == null) {
                    throw new IllegalStateException("Error: no last reasemb; line: " + lineNumber);
                }
                currentReassembly.add(parts[1]);
            } else if (Text.match(s, "*decomp: *", parts)) {
                if (currentDecomposition == null) {
                    throw new IllegalStateException("Error: no last decomp; line: " + lineNumber);
                }
                currentReassembly = new ArrayList<>();
                String temp = parts[1];
                if (Text.match(temp, "$ *", parts)) {
                    currentDecomposition.add(new DecompositionRule(parts[0], true, currentReassembly));
                } else {
                    currentDecomposition.add(new DecompositionRule(temp, false, currentReassembly));
                }
            } else if (Text.match(s, "*key: * #*", parts)) {
                currentDecomposition = new ArrayList<>();
                currentReassembly = null;
                int n = 0;
                if (parts[2].length() != 0) {
                    try {
                        n = Integer.parseInt(parts[2]);
                    } catch (NumberFormatException e) {
                        throw new IllegalStateException("Number is wrong in key: " + parts[2] + " in line: " + lineNumber);
                    }
                }
                keys.put(parts[1], new Key(parts[1], n, currentDecomposition));
            } else if (Text.match(s, "*key: *", parts)) {
                currentDecomposition = new ArrayList<>();
                currentReassembly = null;
                keys.put(parts[1], new Key(parts[1], 0, currentDecomposition));
            } else if (Text.match(s, "*synon: * *", parts)) {
                Set<String> words = new LinkedHashSet<>();
                words.add(parts[1]);
                s = parts[2];
                while (Text.match(s, "* *", parts)) {
                    words.add(parts[0]);
                    s = parts[1];
                }
                words.add(s);
                synonymList.add(words);
            } else if (Text.match(s, "*pre: * *", parts)) {
                pre.put(parts[1], parts[2]);
            } else if (Text.match(s, "*post: * *", parts)) {
                post.put(parts[1], parts[2]);
            } else if (Text.match(s, "*initial: *", parts)) {
                initial = parts[1];
            } else if (Text.match(s, "*final: *", parts)) {
                finl = parts[1];
            } else if (Text.match(s, "*quit: *", parts)) {
                quit.add(" " + parts[1] + " ");
            } else {
                throw new IllegalStateException("Unrecognized input: " + s + " in line: " + lineNumber);
            }
        }
    }


    /**
     * Process a line of input.
     */
    public String processInput(String s) {
        String reply;
        //  Do some input transformations first.
        s = s.toLowerCase(Locale.ROOT);
        s = Text.replace(s, "@#$%^&*()_-+=~`{[}]|:;<>\\\"", ' ');
        s = Text.replace(s, ",?!", '.');
        //  Compress out multiple speace.
        s = Text.compress(s);
        String lines[] = new String[2];
        //  Break apart sentences, and do each separately.
        while (Text.match(s, "*.*", lines)) {
            reply = sentence(lines[0]);
            if (reply != null) return reply;
            s = lines[1].trim();
        }
        if (s.length() != 0) {
            reply = sentence(s);
            if (reply != null) return reply;
        }
        //  Nothing matched, so try memory.
        if (!memory.isEmpty()) return memory.remove(0);

        //  No memory, reply with xnone.
        Key key = keys.get("xnone");
        if (key != null) {
            Key dummy = null;
            reply = decompose(key, s, dummy);
            if (reply != null) return reply;
        }
        //  No xnone, just say anything.
        return "I am at a loss for words.";
    }

    /**
     * Process a sentence.
     * (1) Make pre transformations.
     * (2) Check for quit word.
     * (3) Scan sentence for keys, build key stack.
     * (4) Try decompositions for each key.
     */
    String sentence(String s) {
        s = translate(pre, s);
        s = Text.pad(s);
        if (quit.contains(s)) {
            finished = true;
            return finl;
        }
        for (Key key : buildKeyStack(keys, s.trim())) {
            Key gotoKey = new Key();
            String reply = decompose(key, s, gotoKey);
            if (reply != null) return reply;
            //  If decomposition returned gotoKey, try it
            while (gotoKey.getKey() != null) {
                reply = decompose(gotoKey, s, gotoKey);
                if (reply != null) return reply;
            }
        }
        return null;
    }

    /**
     * Decompose a string according to the given key.
     * Try each decomposition rule in order.
     * If it matches, assemble a reply and return it.
     * If assembly fails, try another decomposition rule.
     * If assembly is a goto rule, return null and give the key.
     * If assembly succeeds, return the reply;
     */
    String decompose(Key key, String s, Key gotoKey) {
        String reply[] = new String[10];
        for (DecompositionRule d : key.getDecompositionRuleList()) {
            String pat = d.getPattern();
            if (matchDecomposition(s, pat, reply)) {
                String rep = assemble(d, reply, gotoKey);
                if (rep != null) return rep;
                if (gotoKey.getKey() != null) return null;
            }
        }
        return null;
    }

    /**
     * Assembly a reply from a decomp rule and the input.
     * If the reassembly rule is goto, return null and give
     * the gotoKey to use.
     * Otherwise return the response.
     */
    String assemble(DecompositionRule d, String reply[], Key gotoKey) {
        String lines[] = new String[3];
        d.advanceToNextRule();
        String rule = d.getCurrentRule();
        if (Text.match(rule, "goto *", lines)) {
            //  goto rule -- set gotoKey and return false.
            gotoKey.copy(keys.get(lines[0]));
            if (gotoKey.getKey() != null) return null;
            System.out.println("Goto rule did not match key: " + lines[0]);
            return null;
        }
        String work = "";
        while (Text.match(rule, "* (#)*", lines)) {
            //  reassembly rule with number substitution
            rule = lines[2];        // there might be more
            int n = 0;
            try {
                n = Integer.parseInt(lines[1]) - 1;
            } catch (NumberFormatException e) {
                System.out.println("Number is wrong in reassembly rule " + lines[1]);
            }
            if (n < 0 || n >= reply.length) {
                System.out.println("Substitution number is bad " + lines[1]);
                return null;
            }
            reply[n] = translate(post, reply[n]);
            work += lines[0] + " " + reply[n];
        }
        work += rule;
        if (d.getMemoryFlag()) {
            memory.add(work);
            return null;
        }
        return work;
    }


    void readScript(InputStream stream) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")))) {
            load(in.lines().collect(Collectors.toUnmodifiableList()));
        }
    }

    int runProgram() {
        DataInputStream in;
        try {
            in = new DataInputStream(System.in);
            String s;
            s = "Hello.";
            while (true) {
                String reply = processInput(s);
                System.out.println(reply);
                if (finished) break;
                System.out.print(">> ");
                s = in.readLine();
                if (s == null) break;
            }
        } catch (IOException e) {
            System.out.println("Problem reading test file.");
            return 1;
        }
        return 0;
    }


    /**
     * Break the string s into words.
     * For each word, if isKey is true, then push the key
     * into the stack.
     */
    public static List<Key> buildKeyStack(Map<String, Key> keyMap, String s) {
        List<Key> stack = new ArrayList<>();
        String lines[] = new String[2];
        while (Text.match(s, "* *", lines)) {
            Key k = keyMap.get(lines[0]);
            if (k != null) stack.add(k);
            s = lines[1];
        }
        Key k = keyMap.get(s);
        if (k != null) stack.add(k);
        //stack.print();
        return stack;
    }

    /**
     * Find a synonym word list given the any word in it.
     */
    Set<String> findSynonyms(String s) {
        for (int i = 0; i < synonymList.size(); i++) {
            Set<String> w = synonymList.get(i);
            if (w.contains(s)) return w;
        }
        return null;
    }

    /**
     * Decomposition match,
     * If decomp has no synonyms, do a regular match.
     * Otherwise, try all synonyms.
     */
    boolean matchDecomposition(String str, String pat, String lines[]) {
        if (!Text.match(pat, "*@* *", lines)) {
            //  no synonyms in decomp pattern
            return Text.match(str, pat, lines);
        }
        //  Decomp pattern has synonym -- isolate the synonym
        String first = lines[0];
        String synWord = lines[1];
        String theRest = " " + lines[2];
        //  Look up the synonym
        Set<String> syn = findSynonyms(synWord);
        if (syn == null) {
            throw new IllegalStateException("Could not fnd syn list for " + synWord);
        }
        //  Try each synonym individually
        for (String synI : syn) {
            //  Make a modified pattern
            pat = first + synI + theRest;
            if (Text.match(str, pat, lines)) {
                int n = Text.count(first, '*');
                //  Make room for the synonym in the match list.
                for (int j = lines.length - 2; j >= n; j--)
                    lines[j + 1] = lines[j];
                //  The synonym goes in the match list.
                lines[n] = synI;
                return true;
            }
        }
        return false;
    }

    public static String translate(Map<String, String> map, String s) {
        String lines[] = new String[2];
        String work = s.trim();
        s = "";
        while (Text.match(work, "* *", lines)) {
            s += map.getOrDefault(lines[0], lines[0]) + " ";
            work = lines[1].trim();
        }
        s += map.getOrDefault(work, work);
        return s;
    }

    public static void main(String args[]) throws IOException {
        Eliza eliza = new Eliza();

        if (args.length > 0) {
            eliza.readScript(new FileInputStream(args[0]));
        } else {
            eliza.readScript(Eliza.class.getResourceAsStream("script"));
        }

        int res = eliza.runProgram();
        System.exit(res);
    }
}
