import javax.swing.*;
import java.awt.*;


public class frame extends JFrame {
    frame(int width,int height ,String title,Client client){

        this.setTitle(title);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setExtendedState(0);
        this.setSize(width,height);
        this.setResizable(false);
        this.setVisible(true);
        this.setLayout(null);
        this.getContentPane().setBackground(new Color(128,128,128));
        System.out.println(client);
        this.getContentPane().add(new MiddlePanel(client));
       // this.getContentPane().add(new UpperPanel());
        this.getContentPane().add(new DownPanel(client));


    }



}
