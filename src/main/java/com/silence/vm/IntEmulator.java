package com.silence.vm;

import com.silence.app.KeyBoard;
import com.silence.app.KeyBoardStatusAware;


public class IntEmulator implements Memory, CPU, KeyBoardStatusAware {
    private int[] reg = new int[Registers.R_COUNT];
    private final ArrayMemory memory = new ArrayMemory();

    public void setReg(int regAddr, int value){
        if(debug)
            System.out.println("set register %d , value %d".formatted(regAddr, value));
        reg[regAddr] = value;
    }

    public void pcIncrease(){
        reg[Registers.R_PC]++;
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
        int r0 = (instruction >>> 9) & 0x7;
        int r1 = (instruction >>> 6) & 0x7;
        int imm_flag = (instruction >>> 5) & 0x1;
        if(debug){
            System.out.print("run instr ADD : ");
            System.out.print("destination register %d, first register is %d , value is %d,".formatted(r0, r1, reg[r1]));
        }
        if(imm_flag != 0){
            int imm5 = sign_extend(instruction & 0x1F, 5);
            if(debug) System.out.println("second value is " + imm5);
            reg[r0] =  reg[r1] + imm5;
        }else {
            int r2 = instruction & 0x7;
            if(debug) System.out.println("second value is " + reg[r2]);
            reg[r0] = reg[r1] + reg[r2];
        }
        update_flag(r0);
    }
    int sign_extend(int x, int bit_count){
        if(((x >>> (bit_count - 1)) & 1) != 0)
            // negative number
            // 543210
            // 111111
            // 100000
            // int has 4 byte, short has 2 byte
            x |= (0xFFFFFFFF << bit_count);
        return x;
    }

    void update_flag(int r){
        if(reg[r] == 0){ // equal
            reg[Registers.R_COND] = ConditionFlags.FL_ZRO;
        }else if((reg[r] >>> 15) != 0){ // negative
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
        int r0 =  (instr >>> 9) & 0x7;
        int pc_offset = sign_extend(instr & 0x1FF, 9);
        int load_data_loc = memory.mem_read(reg[Registers.R_PC] + pc_offset);
        if(debug){
            System.out.println(
                "register is %d, pc value is  %d, pc_offset is %d, load_data_loc in memory is %d"
                    .formatted(r0, reg[Registers.R_PC], pc_offset, load_data_loc)
            );
        }
        reg[r0] = memory.mem_read(load_data_loc);
        update_flag(r0);
    }

    void AND(int instr){
        if(debug)
            System.out.println("run instr AND");
        int r0 = (instr >>> 9) & 0x7;
        int r1 = (instr >>> 6) & 0x7;
        int imm_flag = (instr >>> 5) & 0x1;
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
        int r0 = (instr >>> 9) & 0x7;
        int r1 = (instr >>> 6) & 0x7;
        reg[r0] = ~reg[r1];
        update_flag(r0);
    }

    void BR(int instr){
        int pc_offset = sign_extend(instr & 0x1FF, 9);
        int cond_flag = (instr >>> 9) & 0x7;
        if(debug){
            System.out.print("run instr BR : ");
            System.out.println(
                "cond_flag is %d, R_COND %d, cond_flag & R_COND %d , pc_offset is %d, instr in pc + 1 value is %s, plus offset instr %s"
                .formatted(
                    cond_flag,
                    reg[Registers.R_COND],
                    cond_flag & reg[Registers.R_COND],
                    pc_offset,
                    Integer.toBinaryString( memory.mem_read(reg[Registers.R_PC] + 1) ),
                    Integer.toBinaryString( memory.mem_read(reg[Registers.R_PC] + pc_offset) )
                )
            );
        }
        if((cond_flag & reg[Registers.R_COND]) != 0)
            reg[Registers.R_PC] += pc_offset;
    }

    void JMP(int instr){
        if(debug)
            System.out.println("run instr JMP");
        int r1 = (instr >>> 6) & 0x7;
        reg[Registers.R_PC] = reg[r1];
    }

    /** jump register */
    void JSR(int instr){
        if(debug)
            System.out.println("run instr JSR");
        int long_flag = (instr >>> 11) & 1;
        reg[Registers.R_R7] = reg[Registers.R_PC];
        if(long_flag != 0){
            int long_pc_offset = sign_extend(instr & 0x7FF, 11);
            reg[Registers.R_PC] += long_pc_offset; /** JSR */
        }else {
            int r1 = (instr >> 6) & 0x7;
            reg[Registers.R_PC] = reg[r1]; /** JSRR */
        }
    }

    /** load */
    void LD(int instr){
        if(debug)
            System.out.println("run instr LD");
        int r0 = (instr >>> 9) & 0x7;
        int pc_offset = sign_extend( instr & 0x1FF, 9);
        reg[r0] = memory.mem_read(reg[Registers.R_PC] + pc_offset);
        update_flag(r0);
    }

    /** load register */
    void LDR(int instr){
        if(debug)
            System.out.println("run instr LDR");
        int r0 = (instr >>> 9) & 0x7;
        int r1 = (instr >>> 6) & 0x7;
        int offset = sign_extend( instr & 0x3F, 6);
        reg[r0] = memory.mem_read(reg[r1] + offset);
        update_flag(r0);
    }

    /** load effective address */
    void LEA(int instr){
        if(debug)
            System.out.println("run instr LEA");
        int r0 = (instr >>> 9) & 0x7;
        int pc_offset = sign_extend( instr & 0x1FF, 9);
        reg[r0] = reg[Registers.R_PC] + pc_offset;
        update_flag(r0);
    }

    /** store */
    void ST(int instr){
        if(debug)
            System.out.println("run instr ST");
        int r0 = (instr >>> 9) & 0x7;
        int pc_offset = sign_extend( instr & 0x1FF, 9);
        memory.mem_write(reg[Registers.R_PC] + pc_offset, reg[r0]);
    }

    void STI(int instr){
        int r0 = (instr >>> 9) & 0x7;
        int pc_offset = sign_extend(instr & 0x1FF, 9);
        int write_location = memory.mem_read(reg[Registers.R_PC] + pc_offset);
        if(debug){
            System.out.print("run instr STI : ");
            System.out.println("write from register %d, write to memory %d, pc is %d, offset is %d".formatted(r0, write_location, reg[Registers.R_PC], pc_offset));
        }
        memory.mem_write( write_location, reg[r0] );
    }

    /** store register */
    void STR(int instr){
        int r0 = (instr >>> 9) & 0x7;
        int r1 = (instr >>> 6) & 0x7;
        int offset = sign_extend(instr & 0x3F, 6);
        if(debug) {
            System.out.print("run instr STR : ");
            System.out.println(
                "in STR ,r1 is register %d, r1's value is %d, offset is %d, instr is %s"
                .formatted(r1, reg[r1], offset,Integer.toBinaryString(instr)));
        }
        memory.mem_write(reg[r1] + offset, reg[r0]);
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
//        StringBuilder builder = new StringBuilder();
        char ch ;
        while((ch = (char) memory.mem_read(c)) != 0){
//            builder.append(ch);
            KeyBoard.put(ch);
            c++;
        }
//        System.out.print(builder);
        KeyBoard.flush(Std.stdout());
    }

    void trap_getc(){
        reg[Registers.R_R0] = KeyBoard.getc();
        update_flag(Registers.R_R0);
    }

    void trap_out(){
        KeyBoard.put((char) reg[Registers.R_R0]);
        KeyBoard.flush(Std.stdout());
    }

    void trap_in(){
        if(debug)
            System.out.println("execute trap_in");
        System.out.println("Enter a character : ");
//        Scanner scanner = new Scanner(System.in);
//        char c = (char) scanner.nextByte();
//        System.out.print(c);
        char c = KeyBoard.getc();
        KeyBoard.put(c);
        KeyBoard.flush(Std.stdout());
        reg[Registers.R_R0] = c;
        update_flag( Registers.R_R0);
    }

    void trap_putsp(){
        int put_chars = reg[Registers.R_R0];
//        StringBuilder builder = new StringBuilder();
        while(memory.mem_read(put_chars) != 0){
            char char1 = (char) (memory.mem_read(put_chars) & 0xFF);
//            builder.append(char1);
            KeyBoard.put(char1);
            char char2 = (char) (memory.mem_read(put_chars) >> 8);
            if(char2 != 0)
//                builder.append(char2);
                KeyBoard.put(char2);
            ++put_chars;
        }
        KeyBoard.flush(Std.stdout());
//        System.out.print(builder);
    }

    void trap_halt(){
        System.out.println("HALT");
        System.exit(0);
    }

    void run_instruction(int instr){
        if(debug){
            System.out.println("*".repeat(10) + " start run instr %s".formatted( Integer.toBinaryString( instr )) + "*".repeat(10));
        }
        int op = instr >>> 12;
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
        if(debug)
            System.out.printf("%s end run instr %s %s%n", "*".repeat(10), Integer.toBinaryString(instr), "*".repeat(10));
    }

    @Override
    public void mem_write(char address,char value) {
        memory.mem_write(address, value);
    }

    @Override
    public char mem_read(char address) {
        return memory.mem_read(address);
    }

    @Override
    public void execute(char instr) {
        run_instruction(instr);
    }

    @Override
    public void awareKeyPressing() {
        memory.setKeyPressing(true);
    }

    @Override
    public void awareKeyReleasing() {
        memory.setKeyPressing(false);
    }
}
