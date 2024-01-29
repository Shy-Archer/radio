package org.example;
import javax.swing.*;
import java.awt.Color;

public class UpperPanel extends Panels{
    final int x = 0;
    final int y = 0;
    final int weight = 1024;
    final int height = 75;

    UpperPanel(){

        this.setBackground(Color.red);
        this.setBounds(x,y,weight,height);

    }

    @Override
    protected ImageIcon processimage(String FILE, int width, int height) {
        return null;
    }
}
