package com.silence.vm;

public class Memory {
    public static final int MEMORY_MAX = 1 << 16;
    private static final int[] memory = new int[MEMORY_MAX];
    private static boolean keyPressing = false;

    /**
     * Memory mapped registers make memory access a bit more complicated.
     * We can’t read and write to the memory array directly, but must instead call setter and getter functions.
     * When memory is read from KBSR, the getter will check the keyboard and update both memory locations.
     */
    public static int mem_read(int address){
        if(address == Registers.MR_KBSR){
            if(check_key()){
                memory[Registers.MR_KBSR] = (short) (1 << 15);
                memory[Registers.MR_KBDR] = 0;
            }else {
                memory[Registers.MR_KBSR] = 0;
            }
        }
        return memory[address];
    }

    public static boolean check_key(){
        return keyPressing;
    }

    public static void mem_write(int address, int val){
        memory[address] = val;
    }

}
