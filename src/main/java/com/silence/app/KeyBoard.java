package com.silence.app;

import java.util.Objects;

public class KeyBoard {
  public static volatile Character c ;
  public static char getc(){
    waitForChar();
    return c;
  }

  public static void waitForChar(){
    while(Objects.isNull(c));
  }

  private static StringBuilder builder = new StringBuilder();

  public static void put(char c){
    builder.append(c);
  }

  public static void flush(OutStream out){
    out.write(builder.toString());
    builder.setLength(0);
  }

}
