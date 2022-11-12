package com.silence.vm;

import com.silence.app.KeyBoard;

public class ArrayMemory implements Memory {
    private final int[] memory = new int[MEMORY_MAX];
    private volatile boolean keyPressing = false;

    /**
     * Memory mapped registers make memory access a bit more complicated.
     * We can’t read and write to the memory array directly, but must instead call setter and getter functions.
     * When memory is read from KBSR, the getter will check the keyboard and update both memory locations.
     */
    @Override
    public char mem_read(char address){
        if(address == Registers.MR_KBSR){
            if(check_key()){
                memory[Registers.MR_KBSR] = 1 << 15;
                memory[Registers.MR_KBDR] = KeyBoard.getc();
            }else {
                memory[Registers.MR_KBSR] = 0;
            }
        }
        return (char) memory[address];
    }

    public char mem_read(int address){
        return mem_read((char)address);
    }

    public boolean check_key(){
        return keyPressing;
    }

    public void setKeyPressing(boolean _v){
        keyPressing = _v;
    }

    @Override
    public void mem_write(char address, char val){
        memory[address] = val;
    }

    public void mem_write(int address, char val){
        mem_write((char)address, val);
    }

    public void mem_write(int address, int val){
        mem_write(address, (char)val);
    }

}
