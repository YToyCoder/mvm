package com.silence.vm;

public interface Memory {
  int MEMORY_MAX = 1 << 16;
  void mem_write(char address, char value);
  char mem_read(char address);
}
