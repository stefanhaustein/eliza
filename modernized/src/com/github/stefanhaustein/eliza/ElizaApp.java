package com.github.stefanhaustein.eliza;

import java.awt.*;

/**
 *  Eliza Application.
 */
public class ElizaApp {

    static String scriptPathname = "script";
    static String scriptURL = "http://www.monmouth.com/~chayden/eliza/script";

    static final boolean local = true;

    public static void main(String args[]) {
        ElizaMain eliza = new ElizaMain();

        String script = scriptPathname;

        if (! local) script = scriptURL;
        if (args.length > 0) script = args[0];

        int res = eliza.readScript(script);
        if (res != 0) System.exit(res);

            res = eliza.runProgram();
            System.exit(res);

    }

}
