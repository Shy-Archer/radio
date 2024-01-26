import javax.swing.*;
import java.awt.*;

public abstract class Panels extends JPanel {
    private int x;
    private int y;
    private int weight;
    private int height;

    protected abstract ImageIcon processimage(String FILE,int width,
                                              int height);



}
