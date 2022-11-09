package com.silence.vm;

public class Opcodes {
    public static final int OP_BR = 0;
    public static final int OP_ADD = 1; /* add */
    public static final int OP_LD = 2; /* load */
    public static final int OP_ST = 3; /* store */
    public static final int OP_JSR = 4; /* jump register */
    public static final int OP_AND = 5; /* bitwise and */
    public static final int OP_LDR = 6; /* load register */
    public static final int OP_STR = 7; /* store register */
    public static final int OP_RTI = 8; /* unused */
    public static final int OP_NOT = 9; /* bitwise not */
    public static final int OP_LDI = 10; /* load indirect */
    public static final int OP_STI = 11; /* store indirect */
    public static final int OP_JMP = 12; /* jump */
    public static final int OP_RES = 13; /* reserved (unused) */
    public static final int OP_LEA = 14; /* load effective address */
    public static final int OP_TRAP = 15; /* execute trap */
}
