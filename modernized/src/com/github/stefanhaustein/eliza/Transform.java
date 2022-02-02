package com.github.stefanhaustein.eliza;

/**
 *  This is used to store pre transforms or post transforms.
 */
public class Transform {
    /** The words */
    private final String src;
    private final String dest;

    /**
     *  Initialize the entry.
     */
    Transform(String src, String dest) {
        this.src = src;
        this.dest = dest;
    }

    /**
     *  Get src.
     */
    public String src() {
        return src;
    }

    /**
     *  Get dest.
     */
    public String dest() {
        return dest;
    }
}

