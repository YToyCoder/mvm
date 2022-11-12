package com.silence;

import com.silence.app.KeyBoard;
import com.silence.vm.IntEmulator;
import com.silence.vm.Std;
import com.silence.vm.VM;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        char c = 65535;
        System.out.println();
        ars = args;
        launch(args);
    }
    private static String[] ars;

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            BorderPane root = new BorderPane();
            Text text = new Text();
            Std.setStdout(System.out::print);
            root.setCenter(text);
            Scene scene = new Scene(root, 400, 400);
            IntEmulator emulator = new IntEmulator();
//            emulator.Debug(true);
            scene.setOnKeyPressed(event -> {
                text.setText("press " + event.getCharacter() + " : " + event.getText());
//                KeyBoard.c =  event.getText().toLowerCase().charAt(0);
                KeyBoard.c = event.getCode().getChar().toLowerCase().charAt(0);
                emulator.awareKeyPressing();
                emulator.awareKeyReleasing();
            });
            scene.setOnKeyReleased(event -> {
                emulator.awareKeyReleasing();
                KeyBoard.c = null;
            });
            primaryStage.setScene(scene);
            primaryStage.setTitle("Virtual Machine");
            primaryStage.show();
            new Thread(() -> {
                VM.run(ars, emulator);
            }).start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}