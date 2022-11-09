package com.silence.vm;

/**
 * You may be wondering why the trap codes are not included in the instructions.
 * This is because they do not actually introduce any new functionality to the LC-3,
 * they just provide a convenient way to perform a task (similar to OS system calls).
 * In the official LC-3 simulator, trap routines are written in assembly.
 * When a trap code is called, the PC is moved to that code’s address.
 * The CPU executes the procedure’s instructions, and when it is complete,
 * the PC is reset to the location following the initial call.
 *
 * Note: This is why programs start at address 0x3000 instead of 0x0. The lower addresses are left empty to leave space for the trap routine code.
 */
public class TrapCodes {
  static final int TRAP_GETC = 0x20;  /* get character from keyboard, not echoed onto the terminal */
  static final int TRAP_OUT = 0x21;   /* output a character */
  static final int TRAP_PUTS = 0x22;  /* output a word string */
  static final int TRAP_IN = 0x23;    /* get character from keyboard, echoed onto the terminal */
  static final int TRAP_PUTSP = 0x24; /* output a byte string */
  static final int TRAP_HALT = 0x25;   /* halt the program */
}
