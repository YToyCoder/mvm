package com.silence.vm;

public class Memory {
    public static final int MEMORY_MAX = 1 << 16;
    private static final short[] memory = new short[MEMORY_MAX];
    public static short mem_read(int ind){
        return memory[ind];
    }

    public static void mem_write(int ind, short val){
        memory[ind] = val;
    }

}
