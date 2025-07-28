/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {
    private JButton button1;
    private JButton button2;
    private JButton button3;
    private JFrame currentFrame;

    public MainFrame() {
        button1 = new JButton("Open Add User");
        button2 = new JButton("Open Camera");
        button3 = new JButton("Open Image Upload");

        button1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openClient(new ClientForm());
            }
        });

        button2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openClient(new CameraCapture());
            }
        });

        button3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openClient(new ImageClient());
            }
        });

        setLayout(new java.awt.GridLayout(3, 1));
        add(button1);
        add(button2);
        add(button3);

        setTitle("Main Frame");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void openClient(JFrame clientFrame) {
        if (currentFrame != null) {
            currentFrame.dispose();
        }
        currentFrame = clientFrame;
        currentFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }
}
