package com.silence.vm;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public class VM {
  private static boolean debug = false;

  public static void Debug(boolean _de){
    debug = _de;
  }

  static void read_image_file(ReadableByteChannel byteChannel, Emulator emulator) {
    int record;
    ByteBuffer buffer = ByteBuffer.allocate(2);
    try {
      byteChannel.read(buffer);
      buffer.flip();
      int origin = record = merge(buffer.get() , buffer.get());
      if(buffer.hasRemaining())
        System.out.println("origin buffer remaining");
      int max_read_16bit = (ArrayMemory.MEMORY_MAX - origin);
      buffer = ByteBuffer.allocate(max_read_16bit * 2);
      int count = 0;
      while(byteChannel.read(buffer) > 0){
//        System.out.println("read 16bit size is %d %d".formatted(temp % 2, temp));
        buffer.flip();
        while (buffer.hasRemaining()){
          byte one = buffer.get();
          byte two = buffer.get();
          // 15         8 7         0
          // |<- 8 bit ->|<- 8 bit ->|
          //      one        two
          int merged = merge(one, two);
          if (debug){
            count++;
            System.out.printf("%d ", merged);
            if(count % 10 == 0){
              System.out.println();
            }
          }
          emulator.mem_write(origin++, merged);
        }
        buffer.clear();
      }
      if(debug){
        System.out.println("total read 16bit is %d".formatted(count));
        for(int i = 0; i + record< ArrayMemory.MEMORY_MAX && i<count; i++){
          if(i % 10 == 0)
            System.out.println();
          System.out.printf("%d ", emulator.mem_read(i + record));
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static void read_image(String file_name, Emulator emulator){
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
        read_image_file(channel, emulator);
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
  public static void run(String[] args, Emulator emulator){
    // load arguments
    if(args.length < 1){
      System.out.println("lc3 [image-file1] ...");
      System.exit(2);
    }

    for (int j=0; j < args.length; j++){
      read_image(args[j], emulator);
    }

    // setup
//    emulator.Debug(true);

    // set cond flag
    emulator.setReg( Registers.R_COND, ConditionFlags.FL_ZRO);

    // set pc to starting position
    // 0x3000 is the default
    emulator.setReg( Registers.R_PC, PC_START);

    boolean running = true;
    while (running){
      int instr = emulator.mem_read(emulator.getReg(Registers.R_PC));
      emulator.pcIncrease();
      emulator.execute(instr);
    }
    System.out.println("lc3 run exit");

  }
}
