package com.dekagames.atlasmaker;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: Deka
 * Date: 30.06.13
 * Time: 20:50
 * To change this template use File | Settings | File Templates.
 */
public class MainApplication {
    public static MainWindow mainWindow;

    public MainApplication(){
        mainWindow = new MainWindow();
        mainWindow.setVisible(true);

    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainApplication();
            }
        });

    }
}
