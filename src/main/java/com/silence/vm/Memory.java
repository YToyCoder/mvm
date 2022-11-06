package com.silence.vm;

public class Memory {
    public static final int MEMORY_MAX = 1 << 16;
    private static final short[] memory = new short[MEMORY_MAX];
    public static short get(int ind){
        return memory[ind];
    }

    public static void set(int ind, short val){
        memory[ind] = val;
    }

}
