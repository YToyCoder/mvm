package com.silence.vm;

public class Emulator {

    /**
     * add instruction
     *  15       12 11     9  8     6  5  4  3  2     0
     * |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
     * |   0001    |    DR  |   SR1  | 0|  00 |  SR2   |
     * |   0001    |    DR  |   SR1  | 1|     imm5     |
     *
     * **************************************************
     * Add Register Assembly
     * ADD R2 R0 R1 ; add the content of R0 to R1 and store in R2
     */
    private void ADD(short instruction){
    }
}
