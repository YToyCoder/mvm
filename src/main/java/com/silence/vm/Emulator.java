package com.silence.vm;

public class Emulator {
    private short[] reg = new short[Registers.Register.values().length];

    /*** add instruction */
    /**  15       12 11     9  8     6  5  4  3  2     0 */
    /** |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  | */
    /** |   0001    |    DR  |   SR1  | 0|  00 |  SR2   | */
    /** |   0001    |    DR  |   SR1  | 1|     imm5     | */
    /**
     * **************************************************
     * 0001 is opcode value for OP_ADD, DR is short for destination register ,
     * SR1 is the register containing the first number to add.
     * Add Register Assembly
     * ADD R2 R0 R1 ; add the content of R0 to R1 and store in R2
     * 如果bit[5]是0,第二个数据源来自于SR2.
     * 如果是1，第二个数据源来自于imm5
     */
    private void ADD(short instruction){
        short r0 = (short) ((instruction >> 9) & 0x7);
        short r1 = (short) ((instruction >> 6) & 0x7);
        short imm_flag = (short) ((instruction >> 5) & 0x1);
        if(imm_flag == 1){
            short imm5 = sign_extend((short) (instruction & 0x1F), 5);
            reg[r0] = (short) (reg[r1] + imm5);
        }else {
            short r2 = (short) (instruction & 0x7);
            reg[r0] = (short) (reg[r1] + reg[r2]);
        }
        update_flag(r0);
    }
    short sign_extend(short x, int bit_count){
        if(((x >> (bit_count - 1)) & 1) == 1)
            // negative number
            // 543210
            // 111111
            // 100000
            x |= (0xFFFF << bit_count);
        return x;
    }

    void update_flag(short r){
        if(reg[r] == 0){ // equal
            reg[Registers.Register.R_COND.val()] = ConditionFlags.FL_ZRO;
        }else if(reg[r] >> 15 == 1){ // negative
            reg[Registers.Register.R_COND.val()] = ConditionFlags.FL_NEG;
        }else // positive
            reg[Registers.Register.R_COND.val()] = ConditionFlags.FL_POS;
    }

    /** ldi instruction */
    /**  15       12 11     9  8     6  5  4  3  2     0 */
    /** |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  | */
    /** |   1010    |    DR  |          PCoffset9       | */
    /**
     * ldi 指令是用于将内存的值装载入寄存器。
     * PCoffset从8位符号扩展到16位，然后将该值与增加的PC相加后得到的地址是内存需要加到内存的数据的地址
     */
    void LDI(short instr){
        short r0 = (short) ((instr >> 9) & 0x7);
        short pc_offset = sign_extend((short) (instr & 0x1FF), 9);
        reg[r0] = Memory.get(Memory.get(reg[Registers.Register.R_PC.val()] + pc_offset));
        update_flag(r0);
    }
}
