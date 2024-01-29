package org.example;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class DownPanel extends Panels implements ActionListener {
    final int x = 0;
    final int y = 615;
    final int width = 1024;
    final int height = 153;

    final int play_x = 485;
    final int play_Y = 40;
    final int play_width = 55;
    final int play_height = 55;

    final int skip_x = 555;
    final int skip_Y = 40;
    final int skip_width = 55;
    final int skip_height = 55;
    final int rewind_x = 415;
    final int rewind_Y = 40;
    final int rewind_width = 55;
    final int rewind_height = 55;

    Client cl;
    Socket cs;
    JButton play;
    JButton skip;

    JButton rewind;
    boolean state = false;
    private ExecutorService executorService;
    ImageIcon playicon = processimage("play-button.png", 50, 50);
    ImageIcon skipicon = processimage("skip-button.png", 50, 50);
    ImageIcon rewindicon = processimage("fast-rewind-button.png", 35, 35);
    ImageIcon pauseicon = processimage("video-pause-button.png", 50, 50);

    DownPanel(Client client) {
        cl = client;
        cs = cl.getClientSocket();
        this.setLayout(null);

        executorService = Executors.newFixedThreadPool(1);
        play = new JButton();
        skip = new JButton();
        rewind = new JButton();

        play.setBounds(play_x, play_Y, play_width, play_height);
        play.setIcon(playicon);
        play.setBackground(Color.GRAY);
        play.setBorder(BorderFactory.createEmptyBorder());
        play.addActionListener(this);
        play.setFocusPainted(false);
        play.setFocusable(false);


        skip.setBackground(Color.GRAY);
        skip.setBounds(skip_x, skip_Y, skip_width, skip_height);
        skip.setIcon(skipicon);
        skip.setBorder(BorderFactory.createEmptyBorder());
        skip.setFocusPainted(false);
        skip.setFocusable(false);

        rewind.setBackground(Color.GRAY);
        rewind.setBounds(rewind_x, rewind_Y, rewind_width, rewind_height);
        rewind.setIcon(rewindicon);
        rewind.setBorder(BorderFactory.createEmptyBorder());
        rewind.setFocusPainted(false);
        rewind.setFocusable(false);


        SwingUtilities.invokeLater(() -> {
            this.setBackground(Color.GRAY);
            this.setBounds(x, y, width, height);
            this.add(play);
            this.add(skip);
            this.add(rewind);
        });


    }

//    protected ImageIcon processimage(String FILE, int width, int height) {
//               ImageIcon icon = new ImageIcon(FILE);
//        Image image = icon.getImage();
//        Image newing = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
//        icon = new ImageIcon(newing);
//        return icon;
//    }
    protected ImageIcon processimage(String filename, int width, int height) {
        URL imageURL = getClass().getClassLoader().getResource("Images/" + filename);
        if (imageURL != null) {
            ImageIcon icon = new ImageIcon(imageURL);
            Image image = icon.getImage();
            Image newImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(newImage);
        } else {
            System.err.println("Nie można znaleźć pliku obrazu: " + filename);
            return null;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == play) {
            if (state == false) {
                play.setIcon(pauseicon);
                executorService.execute(() -> {
                    try {
                        cl.handleDownloadAndDelete();
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                });

            } else {
                play.setIcon(playicon);

                state = false;
            }

        } else if (e.getSource() == skip) {
            // Handle skip button click
            executorService.execute(() -> {
                try {
                    System.out.println("s");
                    cl.handleSkip(); // Add a method in your Client class to handle skipping
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });

        }

    }
}


