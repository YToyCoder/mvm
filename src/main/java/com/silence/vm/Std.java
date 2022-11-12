package com.silence.vm;


import com.silence.app.OutStream;

import java.util.Objects;

public class Std {
  private Std(){}
  private static OutStream stdout;

  public static OutStream stdout(){
    if(Objects.isNull(stdout)){
      System.out.println("no stdout!");
      System.exit(1);
    }
    return stdout;
  }

  public static void setStdout(OutStream out){
    stdout = out;
  }

}
