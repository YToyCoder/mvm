package com.silence.app;

import com.silence.vm.Emulator;

public abstract class App {
  Emulator emulator;

  public App(Emulator emulator) {
    this.emulator = emulator;
  }


}
