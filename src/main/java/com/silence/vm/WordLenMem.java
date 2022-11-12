package com.silence.vm;

import com.silence.app.KeyBoard;

public class WordLenMem implements Memory{
  private char[] memory = new char[MEMORY_MAX];
  private volatile boolean key_pressing = false;

  @Override
  public void mem_write(char address,char value) {
    memory[address] = value;
  }

  public void setKeyPressing(boolean _v){
    key_pressing = _v;
  }
  @Override
  public char mem_read(char address) {
    if(address == Registers.MR_KBSR){
      if(key_pressing){
        memory[Registers.MR_KBSR] = 1 << 15;
        memory[Registers.MR_KBDR] = KeyBoard.getc();
      }else {
        memory[Registers.MR_KBSR] = 0;
      }
    }
    return memory[address];
  }
}
