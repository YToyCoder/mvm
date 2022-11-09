package com.silence;

import com.silence.vm.VM;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
//        System.out.println((byte) 234);
        VM.run(args);
//        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            BorderPane root = new BorderPane();
            Scene scene = new Scene(root, 400, 400);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Virtual Machine");
            primaryStage.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}