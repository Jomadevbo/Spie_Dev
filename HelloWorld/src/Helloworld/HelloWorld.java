package Helloworld;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.geom.Line2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.SwingUtilities;
import java.util.ArrayList;

public class HelloWorld {
    public static void main(String[] args) {
        Runnable r = new Runnable() {
            public void run() {
                int[] ScreenSz = {800, 800};
                int focalLength = 800;
                double[][] VertTable = {
                    {-100,  100,  100},//0
                    {-100,  100, -100},//1
                    { 100,  100, -100},//2
                    { 100,  100,  100},//3
                    {-100, -100,  100},//4
                    {-100, -100, -100},//5
                    { 100, -100, -100},//6
                    { 100, -100,  100},//7
                    { 0,    200,    0} //8
                };

                int[][] LineTable = {
                    {0, 1},
                    {1, 2},
                    {2, 3},
                    {3, 0},
                    {0, 4},
                    {1, 5},
                    {2, 6},
                    {3, 7},
                    {4, 5},
                    {5, 6},
                    {6, 7},
                    {7, 4},
                    {1, 8},
                    {3, 8},
                    {2, 8},
                    {0, 8}
                };

                // Create the LineComponent instance
                LineComponent lineComponent = new LineComponent(ScreenSz[0], ScreenSz[1]);

                // Create a panel to hold the LineComponent and the rotation button
                JPanel panel = new JPanel();
                panel.add(lineComponent);

                // Add a button to rotate the cube
                JButton rotateButton = new JButton("Rotate");
                panel.add(rotateButton);

                // Timer for continuous rotation
                Timer timer = new Timer(50, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Rotate cube by a fixed angle (e.g., 1 degree)
                        rotateCube(VertTable, 1);
                        lineComponent.clearLines(); // Clear previous lines
                        drawLines(VertTable, LineTable, lineComponent, focalLength, ScreenSz); // Draw updated lines
                    }
                });

                // Rotate button to start/stop the rotation
                rotateButton.addActionListener(e -> {
                    if (timer.isRunning()) {
                        timer.stop();
                    } else {
                        timer.start();
                    }
                });

                // Show the component in a dialog box
                JOptionPane.showMessageDialog(null, panel);
                drawLines(VertTable, LineTable, lineComponent, focalLength, ScreenSz);
            }
        };
        SwingUtilities.invokeLater(r);
    }

    public static double[] ProjectionMapper(double x, double y, double z, double focalLength) {
        double x2D = (x * focalLength) / (z + focalLength);
        double y2D = (y * focalLength) / (z + focalLength);
        return new double[]{x2D, y2D};
    }

    public static void drawLines(double[][] VertTable, int[][] LineTable, LineComponent lineComponent, int focalLength, int[] ScreenSz) {
        for (int i = 0; i < LineTable.length; i++) {
        	//Linien-Lookup
            int[] calc = new int[2];
            calc[0] = LineTable[i][0];
            calc[1] = LineTable[i][1];

            double[] calc1 = new double[3];
            double[] calc2 = new double[3];

            // Koordinaten-Lookup
            for (int k = 0; k < 3; k++) {
                calc1[k] = VertTable[calc[0]][k];
                calc2[k] = VertTable[calc[1]][k];
            }

            // Projektion
            double[] proj1 = ProjectionMapper(calc1[0], calc1[1], calc1[2], focalLength);
            double[] proj2 = ProjectionMapper(calc2[0], calc2[1], calc2[2], focalLength);

            // Zentrieren des Würfels
            lineComponent.addLine(
                proj1[0] + ScreenSz[0] / 2, 
                ScreenSz[1] / 2 - proj1[1], 
                proj2[0] + ScreenSz[0] / 2, 
                ScreenSz[1] / 2 - proj2[1]
            );
        }
    }

    // Function to rotate the cube by a specified angle
    public static void rotateCube(double[][] VertTable, double angle) {
        double rad = Math.toRadians(angle); // Convert angle to radians

        // Rotation matrix for rotating around the Y-axis
        for (int i = 0; i < VertTable.length; i++) {
            double[] vertex = VertTable[i];
            double x = vertex[0];
            double y = vertex[1];
            double z = vertex[2];

            // Rotate around the Y-axis
            double newX = x * Math.cos(rad) - z * Math.sin(rad);
            double newZ = x * Math.sin(rad) + z * Math.cos(rad);

            // Update the vertex position after rotation
            vertex[0] = newX;
            vertex[2] = newZ;
        }
    }
}

class LineComponent extends JComponent {

    ArrayList<Line2D.Double> lines;

    LineComponent(int width, int height) {
        super();
        setPreferredSize(new Dimension(width, height));
        lines = new ArrayList<Line2D.Double>();
    }

    // Draw a new line
    public void addLine(double x1, double y1, double x2, double y2) {
        Line2D.Double line = new Line2D.Double(x1, y1, x2, y2);
        lines.add(line);
        repaint();
    }

    // Clear previous lines
    public void clearLines() {
        lines.clear();
    }

    // Paint the component
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.black);
        for (Line2D.Double line : lines) {
            g.drawLine(
                (int) line.getX1(),
                (int) line.getY1(),
                (int) line.getX2(),
                (int) line.getY2()
            );
        }
    }
}
