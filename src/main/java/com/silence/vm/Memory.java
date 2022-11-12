package com.silence.vm;

public interface Memory {
  void mem_write(int address, int value);
  int mem_read(int address);
}
