package helloworld;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class HelloWorld {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    // Load model from .obj file
                    String objFilePath = new File(".").getCanonicalPath() + "\\src\\Helloworld\\model.obj";
                    ModelData modelData = ModelUtils.loadModel(objFilePath);

                    int[] screenSz = {1300, 700};
                    int focalLength = 160;

                    LineComponent lineComponent = new LineComponent(screenSz[0], screenSz[1]);
                    JPanel panel = new JPanel();
                    panel.add(lineComponent);
                    panel.setFocusable(true); // Make sure the panel is focusable
                    panel.requestFocusInWindow(); // Request focus to capture key events

                    // Initialize the camera
                    double[] cameraPosition = {0, 0, 5};
                    double[] cameraDirection = {0, 0, -1};
                    double[] cameraUp = {0, 1, 0};
                    Camera camera = new Camera(cameraPosition, cameraDirection, cameraUp, Math.toRadians(60), (double) screenSz[0] / screenSz[1], 0.1, 10);

                    // Use javax.swing.Timer to avoid ambiguity
                    javax.swing.Timer timer = new javax.swing.Timer(25, new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            lineComponent.clear();
                            DrawUtils.drawFaces(modelData, lineComponent, camera, focalLength, screenSz);
                        }
                    });
                    timer.start();

                    // Key Listener for camera movement
                    panel.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyPressed(KeyEvent e) {
                            switch (e.getKeyCode()) {
                                case KeyEvent.VK_W: // Move forward
                                    camera.moveForward(0.1);
                                    break;
                                case KeyEvent.VK_S: // Move backward
                                    camera.moveBackward(0.1);
                                    break;
                                case KeyEvent.VK_A: // Move left
                                    camera.moveLeft(0.1);
                                    break;
                                case KeyEvent.VK_D: // Move right
                                    camera.moveRight(10);
                                    break;
                            }
                        }
                    });

                    // Mouse Motion Listener for camera rotation
                    MouseAdapter mouseAdapter = new MouseAdapter() {
                        private int lastX = -1;
                        private int lastY = -1;
                        private double yaw = 0.0;
                        private double pitch = 0.0;

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
                            yaw += deltaX * 0.01; // Sensitivity
                            pitch -= deltaY * 0.01; // Sensitivity
                            pitch = Math.max(-Math.PI / 2, Math.min(Math.PI / 2, pitch)); // Clamp pitch

                            // Update camera direction based on yaw and pitch
                            camera.direction[0] = Math.cos(pitch) * Math.sin(yaw);
                            camera.direction[1] = Math.sin(pitch); // Keep Y as the pitch directly
                            camera.direction[2] = Math.cos(pitch) * Math.cos(yaw);

                            // Normalize direction vector
                            double length = Math.sqrt(camera.direction[0] * camera.direction[0] + camera.direction[1] * camera.direction[1] + camera.direction[2] * camera.direction[2]);
                            if (length != 0) {
                                camera.direction[0] /= length;
                                camera.direction[1] /= length;
                                camera.direction[2] /= length;
                            }

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

                    // Initial drawing
                    DrawUtils.drawFaces(modelData, lineComponent, camera, focalLength, screenSz);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}