package com.silence.vm;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public class VM {
  static void read_image_file(ReadableByteChannel byteChannel) {
    ByteBuffer buffer = ByteBuffer.allocate(2);
    try {
      byteChannel.read(buffer);
      buffer.flip();
      int origin = merge(buffer.get() , buffer.get());
      if(buffer.hasRemaining())
        System.out.println("origin buffer remaining");
      int max_read_16bit = (Memory.MEMORY_MAX - origin);
      buffer = ByteBuffer.allocate(max_read_16bit * 2);
      byteChannel.read(buffer);
      buffer.flip();
      int count = 0;
      for(int i=0; i<max_read_16bit && buffer.hasRemaining(); i++){
        byte one = buffer.get();
        byte two = buffer.get();
        // 15         8 7         0
        // |<- 8 bit ->|<- 8 bit ->|
        //      one        two
        count++;
        int merged = merge(one, two);
        Memory.mem_write(origin + i, merged);
        if(count % 10 == 0) System.out.println();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static void read_image(String file_name){
    if(!Files.exists(Path.of(file_name))){
      System.err.println("can't find file %s".formatted(file_name));
      System.exit(1);
    }
    else {
      System.out.println("loading file %s in read_image".formatted(file_name));
      try(
          RandomAccessFile file = new RandomAccessFile(file_name, "rw");
          ReadableByteChannel channel = file.getChannel();
      ) {
        read_image_file(channel);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  // 15         8 7         0
  // |<- 8 bit ->|<- 8 bit ->|
  //      one        two
  static int merge(byte one, byte two){
    int convertedOne = (one & 0xff);
    int convertedTwo = two & 0xff;
    return (convertedOne << 8) | convertedTwo;
  }

  static final short PC_START = 0x3000;
  public static void run(String[] args){
    // load arguments
    if(args.length < 1){
      System.out.println("lc3 [image-file1] ...");
      System.exit(2);
    }

    for (int j=0; j < args.length; j++){
      read_image(args[j]);
    }

    // setup
    Emulator emulator = new Emulator();

    // set cond flag
    emulator.setReg( Registers.R_COND, ConditionFlags.FL_ZRO);

    // set pc to starting position
    // 0x3000 is the default
    emulator.setReg( Registers.R_PC, PC_START);

    boolean running = true;
    while (running){
      int instr = Memory.mem_read(emulator.getReg(Registers.R_PC));
      emulator.setReg(Registers.R_PC, emulator.getReg(Registers.R_PC) + 1);
      emulator.run_instruction(instr);
    }
    System.out.println("lc3 run exit");

  }
}
