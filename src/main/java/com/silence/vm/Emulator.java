package com.silence.vm;

import java.util.Scanner;

public class Emulator {
    private int[] reg = new int[Registers.R_COUNT];

    public void setReg(int regAddr, int value){
        if(debug)
        System.out.println("set register %d , value %d".formatted(regAddr, value));
        reg[regAddr] = value;
    }

    private boolean debug = false;
    public void Debug(boolean _de){
        debug = _de;
    }

    public int getReg(int regAddr){
        return reg[regAddr];
    }

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
    private void ADD(int instruction){
        if(debug)
            System.out.println("run instr ADD");
        int r0 = (instruction >> 9) & 0x7;
        int r1 = (instruction >> 6) & 0x7;
        int imm_flag = (instruction >> 5) & 0x1;
        if(imm_flag != 0){
            int imm5 = sign_extend(instruction & 0x1F, 5);
            reg[r0] =  reg[r1] + imm5;
        }else {
            int r2 = instruction & 0x7;
            reg[r0] = reg[r1] + reg[r2];
        }
        update_flag(r0);
    }
    int sign_extend(int x, int bit_count){
        if(((x >> (bit_count - 1)) & 1) == 1)
            // negative number
            // 543210
            // 111111
            // 100000
            x |= (0xFFFF << bit_count);
        return x;
    }

    void update_flag(int r){
        if(reg[r] == 0){ // equal
            reg[Registers.R_COND] = ConditionFlags.FL_ZRO;
        }else if(reg[r] >> 15 != 0){ // negative
            reg[Registers.R_COND] = ConditionFlags.FL_NEG;
        }else // positive
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
    void LDI(int instr){
        if(debug)
        System.out.println("run instr LDI");
        int r0 =  (instr >> 9) & 0x7;
        int pc_offset = sign_extend(instr & 0x1FF, 9);
//        reg[r0] = Memory.get(Memory.get(reg[Registers.Register.R_PC.val()] + pc_offset));
        reg[r0] = Memory.mem_read(Memory.mem_read(reg[Registers.R_PC] + pc_offset));
        update_flag(r0);
    }

    void AND(int instr){
        if(debug)
        System.out.println("run instr AND");
        int r0 = (instr >> 9) & 0x7;
        int r1 = (instr >> 6) & 0x7;
        int imm_flag = (instr >> 5) & 0x1;
        if(imm_flag != 0){
            int imm5 = sign_extend(instr & 0x1F, 5);
            reg[r0] = reg[r1] & imm5;
        }else {
            int r2 = instr & 0x7;
            reg[r0] = reg[r1] & reg[r2];
        }
        update_flag(r0);
    }

    void NOT(int instr){
        if(debug)
        System.out.println("run instr NOT");
        int r0 = (instr >> 9) & 0x7;
        int r1 = (instr >> 6) & 0x7;
        reg[r0] = ~reg[r1];
        update_flag(r0);
    }

    void BR(int instr){
        if(debug)
        System.out.println("run instr BR");
        int pc_offset = sign_extend(instr & 0x1FF, 9);
        int cond_flag = (instr >> 9) & 0x7;
        if((cond_flag & reg[Registers.R_COND]) != 0)
            reg[Registers.R_PC] += pc_offset;
    }

    void JMP(int instr){
        if(debug)
        System.out.println("run instr JMP");
        int r1 = (instr >> 6) & 0x7;
//        reg[Registers.Register.R_PC.val()] = reg[r1];
        reg[Registers.R_PC] = reg[r1];
    }

    /** jump register */
    void JSR(int instr){
        if(debug)
        System.out.println("run instr JSR");
        int long_flag = (instr >> 11) & 1;
        reg[Registers.R_R7] = reg[Registers.R_PC];
        if(long_flag != 0){
            int long_pc_offset = sign_extend(instr & 0x7FF, 11);
            reg[Registers.R_PC] = long_pc_offset;
        }else {
            int r1 = (instr >> 6) & 0x7;
            reg[Registers.R_PC] = reg[r1];
        }
    }

    /** load */
    void LD(int instr){
        if(debug)
        System.out.println("run instr LD");
        int r0 = (instr >> 9) & 0x7;
        int pc_offset = sign_extend( instr & 0x1FF, 9);
        reg[r0] = Memory.mem_read(reg[Registers.R_PC] + pc_offset);
        update_flag(r0);
    }

    /** load register */
    void LDR(int instr){
        if(debug)
        System.out.println("run instr LDR");
        int r0 = (instr >> 9) & 0x7;
        int r1 = (instr >> 6) & 0x7;
        int offset = sign_extend( instr & 0x3F, 6);
        reg[r0] = Memory.mem_read(reg[r1] + offset);
    }

    /** load effective address */
    void LEA(int instr){
        if(debug)
        System.out.println("run instr LEA");
        int r0 = (instr >> 9) & 0x7;
        int pc_offset = sign_extend( instr & 0x1FF, 9);
        reg[r0] = reg[Registers.R_PC] + pc_offset;
        update_flag(r0);
    }

    /** store */
    void ST(int instr){
        if(debug)
        System.out.println("run instr ST");
        int r0 = (instr >> 9) & 0x7;
        int pc_offset = sign_extend( instr & 0x1FF, 9);
        Memory.mem_write(reg[Registers.R_PC] + pc_offset, reg[r0]);
    }

    void STI(int instr){
        System.out.println("run instr STI");
        int r0 = (instr >> 9) & 0x7;
        int pc_offset = sign_extend(instr & 0x1FF, 9);
        Memory.mem_write(Memory.mem_read(reg[Registers.R_PC] + pc_offset) , reg[r0]);
    }

    /** store register */
    void STR(int instr){
        if(debug)
        System.out.println("run instr STR");
        int r0 = (instr >> 9) & 0x7;
        int r1 = (instr >> 6) & 0x7;
        int offset = sign_extend(instr & 0x3F, 6);
        Memory.mem_write(reg[r1] + offset, reg[r0]);
    }

    void trap(int instr){
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
        int c = reg[Registers.R_R0];
        StringBuilder builder = new StringBuilder();
        char ch ;
        while((ch = (char) Memory.mem_read(c)) != 0){
            builder.append(ch);
            c++;
        }
        System.out.println(builder);
    }

    void trap_getc(){
        Scanner scanner = new Scanner(System.in);
        reg[Registers.R_R0] = scanner.nextByte();
        update_flag(Registers.R_R0);
    }

    void trap_out(){
        System.out.println((char) reg[Registers.R_R0]);
    }

    void trap_in(){
        System.out.println("Enter a character : ");
        Scanner scanner = new Scanner(System.in);
        char c = (char) scanner.nextByte();
        System.out.print(c);
        reg[Registers.R_R0] = c;
        update_flag( Registers.R_R0);
    }

    void trap_putsp(){
        int c = 0 + reg[Registers.R_R0];
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
        System.exit(0);
    }

    void run_instruction(int instr){
        if(debug)
        System.out.println("instr is %d".formatted(instr));
        int op = instr >> 12;
        switch (op){
            case Opcodes.OP_ADD -> ADD(instr);
            case Opcodes.OP_AND -> AND(instr);
            case Opcodes.OP_NOT -> NOT(instr);
            case Opcodes.OP_BR -> BR(instr);
            case Opcodes.OP_JMP -> JMP(instr);
            case Opcodes.OP_JSR -> JSR(instr);
            case Opcodes.OP_LD -> LD(instr);
            case Opcodes.OP_LDI -> LDI(instr);
            case Opcodes.OP_LDR -> LDR(instr);
            case Opcodes.OP_LEA -> LEA(instr);
            case Opcodes.OP_ST -> ST(instr);
            case Opcodes.OP_STI -> STI(instr);
            case Opcodes.OP_STR -> STR(instr);
            case Opcodes.OP_TRAP -> trap(instr);
            default -> {
                // BAD Opcode
                System.out.println("bad opcode");
                System.exit(1);
            }
        }
    }
}
