package com.silence.vm;

public class Registers {
    public static final char R_R0 = 0;
    public static final char R_R1 = 1;
    public static final char R_R2 = 2;
    public static final char R_R3 = 3;
    public static final char R_R4 = 4;
    public static final char R_R5 = 5;
    public static final char R_R6 = 6;
    public static final char R_R7 = 7;
    public static final char R_PC = 8;
    public static final char R_COND = 9;
    public static final char R_COUNT = 10;

    /** Memory Mapped Registers */
    public static final char MR_KBSR = 0xFE00; /** keyboard status */
    public static final char MR_KBDR = 0xFE02; /** keyboard data */

}
