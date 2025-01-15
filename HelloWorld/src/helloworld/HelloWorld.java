package helloworld;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class HelloWorld {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Load model from .obj file - path should be adjusted based on your project structure
                String objFilePath = new File(".").getCanonicalPath() + "\\src\\Helloworld\\model.obj";
                ModelData modelData = ModelUtils.loadModel(objFilePath);

                int[] screenSz = {1300, 700};
                int focalLength = 100; // Set a reasonable focal length

                LineComponent lineComponent = new LineComponent(screenSz[0], screenSz[1]);
                JPanel panel = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        lineComponent.paintComponent(g); // Paint the line component
                    }
                };
                panel.setFocusable(true);
                panel.requestFocusInWindow();

                // Initialize the camera
                double[] cameraPosition = {0, 0, 5};
                Camera camera = new Camera(cameraPosition);

                // Use javax.swing.Timer to periodically update the rendering
                javax.swing.Timer timer = new javax.swing.Timer(25, e -> {
                    lineComponent.clear();
                    DrawUtils.drawFaces(modelData, lineComponent, camera, focalLength, screenSz);
                    panel.repaint(); // Repaint the panel for the updated rendering
                });
                timer.start();

                // Key Listener for camera movement
                panel.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        switch (e.getKeyCode()) {
                            case KeyEvent.VK_W: // Move forward
                                camera.moveForward();
                                break;
                            case KeyEvent.VK_S: // Move backward
                                camera.moveBackward();
                                break;
                            case KeyEvent.VK_A: // Move left
                                camera.moveLeft();
                                break;
                            case KeyEvent.VK_D: // Move right
                                camera.moveRight();
                                break;
                            case KeyEvent.VK_SPACE: // Move up
                                camera.moveUp();
                                break;
                            case KeyEvent.VK_SHIFT: // Move down
                                camera.moveDown();
                                break;
                        }
                    }
                });

                // Mouse Motion Listener for camera rotation
                MouseAdapter mouseAdapter = new MouseAdapter() {
                    private int lastX = -1;
                    private int lastY = -1;

                    @Override
                    public void mouseMoved(MouseEvent e) {
                        if (lastX == -1 || lastY == -1) {
                            lastX = e.getX();
                            lastY = e.getY();
                            return; // Skip the first movement
                        }

                        int deltaX = e.getX() - lastX;
                        int deltaY = e.getY() - lastY;

                        // Adjust yaw and pitch based on mouse movement
                        camera.rotate(deltaX * 0.1, deltaY * 0.1); // Sensitivity

                        lastX = e.getX();
                        lastY = e.getY();
                    }
                };

                panel.addMouseMotionListener(mouseAdapter);

                // Create a JFrame to hold the panel
                JFrame frame = new JFrame("3D Scene");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(screenSz[0], screenSz[1]);
                frame.add(panel);
                frame.setVisible(true);
                panel.requestFocusInWindow(); // Request focus to capture key events

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }
}