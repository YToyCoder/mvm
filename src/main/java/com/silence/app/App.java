package com.silence.app;

import com.silence.vm.IntEmulator;

public abstract class App {
  IntEmulator emulator;

  public App(IntEmulator emulator) {
    this.emulator = emulator;
  }


}
