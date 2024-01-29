import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.Socket;
import java.util.LinkedList;
import java.util.TimerTask;
import java.util.concurrent.Flow;
import java.util.Timer;
public class MiddlePanel extends Panels {
    private final Object lock = new Object();
    private boolean isTaskRunning = false; // Nowa zmienna kontrolna

    final int x = 0;
    final int y = 0;
    final int width = 1024;
    final int height = 615;
    JLabel label = new JLabel();
    DefaultListModel<JButton> listModel = new DefaultListModel<>();
    JList<JButton> buttonList = new JList<>(listModel);
    LinkedList<String> queuelist;
    Client cl;
    Socket cs;
    ImageIcon plus = processimage("Images/add-symbol.png",50,50);
    ImageIcon minus = processimage("Images/minus.png",45,45);
    ImageIcon up = processimage("Images/up-arrow.png",45,45);
    ImageIcon down = processimage("Images/down-arrow.png",45,45);
    public MiddlePanel(Client client) {
        cl = client;
        cs = cl.getClientSocket();
        SwingUtilities.invokeLater(() -> {
            initGUI();
            startAutoRefresh();
        });
    }
    private void startAutoRefresh() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                synchronized (lock) { // Synchronizacja w celu uniknięcia równoczesnego dostępu
                    if (!isTaskRunning) {
                        isTaskRunning = true;
                        SwingUtilities.invokeLater(() -> {
                            listModel.removeAllElements();
                            queuelist = cl.handleViewQueue();
                            for (String i : queuelist) {
                                JButton button = new JButton(i);
                                button.setFocusable(false);
                                button.addActionListener(new ButtonClickListener());
                                listModel.addElement(button);
                            }
                            isTaskRunning = false;
                        });
                    }
                }
            }
        }, 0, 3000); // Odświeżaj co 3000 milisekund (czyli co 3 sekundy)
    }
    private void initGUI() {
       // queuelist = cl.handleViewQueue();
      /* for (String i : queuelist) {
            JButton button = new JButton(i);
            button.setFocusable(false);
            button.addActionListener(new ButtonClickListener());


            listModel.addElement(button);
        }*/

        // Set a custom cell renderer for the JList
        buttonList.setCellRenderer(new ButtonListRenderer());

        // Create a JScrollPane with the JList
        JScrollPane scrollPane = new JScrollPane(buttonList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);


        // Create buttons
        JButton addButton = new JButton();
        addButton.setIcon(plus);
        addButton.setBackground(new Color(186,188,189));
        addButton.setBorder(BorderFactory.createLineBorder(Color.black));
        addButton.setFocusable(false);
        addButton.setFocusPainted(false);

        JButton removeButton = new JButton();
        removeButton.setIcon(minus);
        removeButton.setBackground(new Color(186,188,189));
        removeButton.setBorder(BorderFactory.createLineBorder(Color.black));
        removeButton.setFocusable(false);
        removeButton.setFocusPainted(false);


        JButton upButton = new JButton();
        upButton.setIcon(up);
        upButton.setBackground(new Color(186,188,189));
        upButton.setBorder(BorderFactory.createLineBorder(Color.black));
        upButton.setFocusable(false);
        upButton.setFocusPainted(false);


        JButton downButton = new JButton();
        downButton.setIcon(down);
        downButton.setBackground(new Color(186,188,189));
        downButton.setBorder(BorderFactory.createLineBorder(Color.black));
        downButton.setFocusable(false);
        downButton.setFocusPainted(false);

        // Add ActionListener to the buttons
        addButton.addActionListener(e -> {
            isTaskRunning = true;
            JFileChooser fileChooser = new JFileChooser();
            int response = fileChooser.showOpenDialog(null);
            if (response == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                JButton newButton = new JButton(file.getName().split("\\.")[0]);
                newButton.addActionListener(new ButtonClickListener());
                FileUploadWorker uploadWorker = new FileUploadWorker(file.getAbsolutePath(), file.getName(), cs);
                uploadWorker.execute();
                isTaskRunning = false;
            }

        });

        removeButton.addActionListener(e -> {
            int selectedIndex = buttonList.getSelectedIndex();
            if (selectedIndex != -1) {
                listModel.remove(selectedIndex);
            }
        });
        upButton.addActionListener(e-> {
            int selectedIndex = buttonList.getSelectedIndex();
            if (selectedIndex > 0) {JButton selectedButton = listModel.getElementAt(selectedIndex);
                listModel.setElementAt(listModel.getElementAt(selectedIndex - 1), selectedIndex);
                listModel.setElementAt(selectedButton, selectedIndex - 1);
                buttonList.setSelectedIndex(selectedIndex - 1);
            }
        });
        downButton.addActionListener(e-> {
            int selectedIndex = buttonList.getSelectedIndex();
            int lastIndex = listModel.getSize() - 1;

            if (selectedIndex >= 0 && selectedIndex < lastIndex) { // Sprawdzamy, czy wybrany przycisk nie jest już na dole listy
                JButton selectedButton = listModel.getElementAt(selectedIndex);
                listModel.setElementAt(listModel.getElementAt(selectedIndex + 1), selectedIndex);
                listModel.setElementAt(selectedButton, selectedIndex + 1);
                buttonList.setSelectedIndex(selectedIndex + 1);
            }
        });
        JButton viewQueueButton = new JButton("View Queue");
        viewQueueButton.addActionListener(e -> {
            queuelist = cl.handleViewQueue();
            for (String i : queuelist) {
                JButton button = new JButton(i);
                button.setFocusable(false);
                button.addActionListener(new ButtonClickListener());


                listModel.addElement(button);
            }
        });

        // Create a panel to hold buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(5,1,0,10));
        buttonPanel.setBackground(Color.GRAY);
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(upButton);
        buttonPanel.add(downButton);
        buttonPanel.add(viewQueueButton);

        // Set the layout manager to BorderLayout
       // setLayout(new BorderLayout());
        scrollPane.setPreferredSize(new Dimension(800, 540));
        setLayout(new BorderLayout());
        // Add the JScrollPane, buttons, and label to the panel
        add(scrollPane, BorderLayout.WEST);
        add(buttonPanel);
        add(label, BorderLayout.SOUTH);

        // Set the background color and bounds
        setBackground(Color.white);
        setBounds(x, y, width, height);


        // Validate and repaint the panel
        validate();
        repaint();
    }
    private class ButtonListRenderer implements ListCellRenderer<JButton> {
        @Override
        public Component getListCellRendererComponent(JList<? extends JButton> list, JButton value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            value.setBackground(isSelected ? new Color(255,253,208) : new Color(186,188,189));

            value.setForeground(isSelected ? Color.BLACK : Color.BLACK);
            Dimension buttonSize = new Dimension(value.getPreferredSize().width, 35);
            value.setPreferredSize(buttonSize);
            value.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            return value;
        }
    }
   protected ImageIcon processimage(String FILE,int width,int height){
        ImageIcon icon = new ImageIcon(FILE);
        Image image = icon.getImage();
        Image newing = image.getScaledInstance(width, height,  Image.SCALE_SMOOTH);
        icon = new ImageIcon(newing);
        return icon;
    }
    private class ButtonClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton clickedButton = (JButton) e.getSource();

        }
    }
}



