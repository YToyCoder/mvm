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
      byte[] array = buffer.array();
      short origin = merge(array[0] , array[1]);
      int max_read_16bit = (Memory.MEMORY_MAX - origin);
      buffer = ByteBuffer.allocate(max_read_16bit * 2);
      for(int i=0; i<max_read_16bit && buffer.hasRemaining(); i++){
        byte one = buffer.get();
        byte two = buffer.get();
        // 15         8 7         0
        // |<- 8 bit ->|<- 8 bit ->|
        //      one        two
        Memory.mem_write(origin + i, merge(one, two));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static void read_image(String file_name){
    if(Files.exists(Path.of(file_name)))
      System.err.println("can't find file %s".formatted(file_name));
    else {
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
  static  short merge(byte one, byte two){
    return (short) ((one << 8) | (two));
  }

  static short swap16(short x){
    return (short) ((x << 8) | (x >> 8));
  }
}
