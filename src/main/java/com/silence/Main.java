package com.silence;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        int a = 0b10111;
        System.out.println("%s - %s".formatted(Integer.toBinaryString(a), Integer.toBinaryString(a | (0xFFFF << 5))));
        System.out.println("0x7 is %s".formatted(Integer.toBinaryString(0x7)));
    }
}