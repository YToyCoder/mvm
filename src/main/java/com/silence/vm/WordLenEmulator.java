package com.silence.vm;

public class WordLenEmulator implements RegisterOp, Memory {
  private char[] reg = new char[Registers.R_COUNT];
  private final WordLenMem memory = new WordLenMem();

  @Override
  public void setReg(char _id, char value) {
    reg[_id] = value;
  }

  @Override
  public char getReg(char _id) {
    return reg[_id];
  }

  @Override
  public void mem_write(char address, char value) {
    memory.mem_write(address, value);
  }

  @Override
  public char mem_read(char address) {
    return memory.mem_read(address);
  }

  private char sign_extend(char x, char bit_count){
    if(((x >>> (bit_count - 1)) & 1) != 0)
      // negative number
      // 543210
      // 111111
      // 100000
      // int has 4 byte, short has 2 byte
      x |= (0xFFFF << bit_count);
    return x;
  }

  void update_flag(char r){
    if(reg[r] == 0){ // equal
      setReg(Registers.R_COND, (char) ConditionFlags.FL_ZRO);
    }else if((reg[r] >>> 15) != 0){ // negative
      setReg(Registers.R_COND, ConditionFlags.FL_NEG);
    }else // positive
      setReg(Registers.R_COND, ConditionFlags.FL_POS);
  }

  char extractRegisterIdFromInstr(char instr, int location){
    return backMvAnd(instr, (char) location, (char) 0x7);
  }

  char backMvAnd(char instr, char mv, char and){
    return (char) ((instr >>> mv) & and);
  }

  private void ADD(char instruction){
    char r0 = extractRegisterIdFromInstr(instruction, 9);
    char r1 = extractRegisterIdFromInstr(instruction, 6);
    char imm_flag = (char) ((instruction >>> 5) & 0x1);
    if(imm_flag != 0){
      int imm5 = sign_extend((char) (instruction & 0x1F), (char) 5);
      setReg(r0, (char) (reg[r1] + imm5));
    }else {
      char r2 = (char) (instruction & 0x7);
      setReg(r0, (char) (getReg(r1) + getReg(r2)));
    }
    update_flag(r0);
  }

}
