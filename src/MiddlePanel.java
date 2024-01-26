import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Flow;

public class MiddlePanel extends JPanel {
    final int x = 0;
    final int y = 0;
    final int width = 1024;
    final int height = 615;
    JLabel label = new JLabel();
    DefaultListModel<JButton> listModel = new DefaultListModel<>();
    JList<JButton> buttonList = new JList<>(listModel);
    public MiddlePanel() {
        SwingUtilities.invokeLater(() -> {
            initGUI();
        });
    }

    private void initGUI() {

        for (int i = 1; i <= 40; i++) {
            JButton button = new JButton("Button " + i);
            button.setFocusable(false);
            button.addActionListener(new ButtonClickListener());
            listModel.addElement(button);
        }

        // Set a custom cell renderer for the JList
        buttonList.setCellRenderer(new ButtonListRenderer());

        // Create a JScrollPane with the JList
        JScrollPane scrollPane = new JScrollPane(buttonList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);


        // Create buttons
        JButton addButton = new JButton("Add Button");
        addButton.setBackground(Color.GRAY);
        addButton.setBorder(BorderFactory.createEmptyBorder());
        addButton.setFocusable(false);
        JButton removeButton = new JButton("Remove Selected");
        JButton upButton = new JButton("up");
        JButton downButton = new JButton("down");

        // Add ActionListener to the buttons
        addButton.addActionListener(e -> {
            JButton newButton = new JButton("New Button");
            newButton.addActionListener(new ButtonClickListener());
            listModel.addElement(newButton);
        });

        removeButton.addActionListener(e -> {
            int selectedIndex = buttonList.getSelectedIndex();
            if (selectedIndex != -1) {
                listModel.remove(selectedIndex);
            }
        });

        // Create a panel to hold buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(4,1,0,10));
        buttonPanel.setBackground(Color.GRAY);
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(upButton);
        buttonPanel.add(downButton);

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

            value.setForeground(isSelected ? Color.BLACK : Color.BLACK);
            value.setBorderPainted(isSelected);
            return value;
        }
    }

    private class ButtonClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton clickedButton = (JButton) e.getSource();
            label.setText("Clicked Button: " + clickedButton.getText());
        }
    }
}



