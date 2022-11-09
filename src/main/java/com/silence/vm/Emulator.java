package com.silence.vm;

import java.util.Scanner;

public class Emulator {
    private short[] reg = new short[Registers.R_COUNT];

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
        if(imm_flag != 0){
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
//            reg[Registers.Register.R_COND.val()] = ConditionFlags.FL_ZRO;
            reg[Registers.R_COND] = ConditionFlags.FL_ZRO;
        }else if(reg[r] >> 15 == 1){ // negative
//            reg[Registers.Register.R_COND.val()] = ConditionFlags.FL_NEG;
            reg[Registers.R_COND] = ConditionFlags.FL_NEG;
        }else // positive
//            reg[Registers.Register.R_COND.val()] = ConditionFlags.FL_POS;
            reg[Registers.R_COND] = ConditionFlags.FL_POS;
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
//        reg[r0] = Memory.get(Memory.get(reg[Registers.Register.R_PC.val()] + pc_offset));
        reg[r0] = Memory.mem_read(Memory.mem_read(reg[Registers.R_PC] + pc_offset));
        update_flag(r0);
    }

    void AND(short instr){
        short r0 = (short) ((instr >> 9) & 0x7);
        short r1 = (short) ((instr >> 6) & 0x7);
        short imm_flag = (short) ((instr >> 5) & 0x1);
        if(imm_flag != 0){
            short imm5 = sign_extend((short) (instr & 0x1F), 5);
            reg[r0] = (short) (reg[r1] & imm5);
        }else {
            short r2 = (short) (instr & 0x7);
            reg[r0] = (short) (reg[r1] & reg[r2]);
        }
        update_flag(r0);
    }

    void NOT(short instr){
        short r0 = (short) ((instr >> 9) & 0x7);
        short r1 = (short) ((instr >> 6) & 0x7);
        reg[r0] = (short) ~reg[r1];
        update_flag(r0);
    }

    void BR(short instr){
        short pc_offset = sign_extend((short) (instr & 0x1FF), 9);
        short cond_flag = (short) ((instr >> 9) & 0x7);
//        if(((cond_flag & reg[Registers.Register.R_COND.val()])) != 0)
//            reg[Registers.Register.R_PC.val()] += pc_offset;
        if(((cond_flag & reg[Registers.R_COND])) != 0)
            reg[Registers.R_PC] += pc_offset;
    }

    void JMP(short instr){
        short r1 = (short) ((instr >> 6) & 0x7);
//        reg[Registers.Register.R_PC.val()] = reg[r1];
        reg[Registers.R_PC] = reg[r1];
    }

    /** jump register */
    void JSR(short instr){
        short long_flag = (short) ((instr >> 11) & 1);
        reg[Registers.R_R7] = reg[Registers.R_PC];
        if(long_flag != 0){
            short long_pc_offset = sign_extend((short) (instr & 0x7FF), 11);
            reg[Registers.R_PC] = long_pc_offset;
        }else {
            short r1 = (short) ((instr >> 6) & 0x7);
            reg[Registers.R_PC] = reg[r1];
        }
    }

    /** load */
    void LD(short instr){
        short r0 = (short) ((instr >> 9) & 0x7);
        short pc_offset = sign_extend((short) (instr & 0x1FF), 9);
        reg[r0] = Memory.mem_read(reg[Registers.R_PC] + pc_offset);
        update_flag(r0);
    }

    /** load register */
    void LDR(short instr){
        short r0 = (short) ((instr >> 9) & 0x7);
        short r1 = (short) ((instr >> 6) & 0x7);
        short offset = sign_extend((short) (instr & 0x3F), 6);
        reg[r0] = Memory.mem_read(reg[r1] + offset);
    }

    /** load effective address */
    void LEA(short instr){
        short r0 = (short) ((instr >> 9) & 0x7);
        short pc_offset = sign_extend((short) (instr & 0x1FF), 9);
        reg[r0] = (short) (reg[Registers.R_PC] + pc_offset);
        update_flag(r0);
    }

    /** store */
    void ST(short instr){
        short r0 = (short) ((instr >> 9) & 0x7);
        short pc_offset = sign_extend((short) (instr & 0x1FF), 9);
        Memory.mem_write(reg[Registers.R_PC] + pc_offset, reg[r0]);
    }

    /** store register */
    void STR(short instr){
        short r0 = (short) ((instr >> 9) & 0x7);
        short r1 = (short) ((instr >> 6) & 0x7);
        short offset = sign_extend((short) (instr & 0x3F), 6);
        Memory.mem_write(reg[r1] + offset, reg[r0]);
    }

    void trap(short instr){
        reg[Registers.R_R7] = reg[Registers.R_PC];
        switch (instr & 0xFF){
            case TrapCodes.TRAP_GETC -> trap_getc();
            case TrapCodes.TRAP_OUT -> trap_out();
            case TrapCodes.TRAP_PUTS -> trap_puts();
            case TrapCodes.TRAP_IN -> trap_in();
            case TrapCodes.TRAP_PUTSP -> trap_putsp();
            case TrapCodes.TRAP_HALT -> trap_halt();
        }
    }

    void trap_puts(){
        short c = (short) (0 + reg[Registers.R_R0]);
        StringBuilder builder = new StringBuilder();
        while(Memory.mem_read(c) != 0){
            builder.append((char) Memory.mem_read(c));
        }
        System.out.println(builder);
    }

    void trap_getc(){
        Scanner scanner = new Scanner(System.in);
        reg[Registers.R_R0] = scanner.nextByte();
        update_flag((short) Registers.R_R0);
    }

    void trap_out(){
        System.out.println((char) reg[Registers.R_R0]);
    }

    void trap_in(){
        System.out.println("Enter a character : ");
        Scanner scanner = new Scanner(System.in);
        char c = (char) scanner.nextByte();
        System.out.println(c);
        reg[Registers.R_R0] = (short) c;
        update_flag((short) Registers.R_R0);
    }

    void trap_putsp(){
        short c = (short) (0 + reg[Registers.R_R0]);
        StringBuilder builder = new StringBuilder();
        while(Memory.mem_read(c) != 0){
            char char1 = (char) (Memory.mem_read(c) & 0xFF);
            builder.append(char1);
            char char2 = (char) (Memory.mem_read(c) >> 8);
            if(char2 != 0)
                builder.append(char2);
            ++c;
        }
        System.out.print(builder);
    }

    void trap_halt(){
        System.out.println("HALT");
    }
}
