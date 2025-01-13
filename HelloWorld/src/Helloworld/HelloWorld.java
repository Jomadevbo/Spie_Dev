package Helloworld;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.geom.Line2D;
import java.io.*;
import java.util.*;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

public class HelloWorld {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    // Load model from .obj file
                    String objFilePath = new File(".").getCanonicalPath() + "\\test\\HelloWorld\\src\\Helloworld\\auto.obj";
                    ModelData modelData = loadModel(objFilePath);

                    int[] screenSz = {800, 800};
                    int focalLength = 200;

                    LineComponent lineComponent = new LineComponent(screenSz[0], screenSz[1]);

                    JPanel panel = new JPanel();
                    panel.add(lineComponent);

                    JButton rotateButton = new JButton("Rotate");
                    panel.add(rotateButton);

                    Timer timer = new Timer(50, new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            rotateCube(modelData.VertTable, 1);
                            lineComponent.clear();
                            drawFaces(modelData, lineComponent, focalLength, screenSz);
                            drawLines(modelData, lineComponent, focalLength, screenSz);
                        }
                    });

                    rotateButton.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            if (timer.isRunning()) {
                                timer.stop();
                            } else {
                                timer.start();
                            }
                        }
                    });

                    JOptionPane.showMessageDialog(null, panel);
                    drawFaces(modelData, lineComponent, focalLength, screenSz);
                    drawLines(modelData, lineComponent, focalLength, screenSz);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public static ModelData loadModel(String objFilePath) throws IOException {
        List<double[]> vertices = new ArrayList<>();
        List<int[]> lines = new ArrayList<>();
        List<int[]> faces = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(objFilePath));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length == 0) continue;
            int start;
            if (parts[0].equals("v")) { // Vertex line
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                double z = Double.parseDouble(parts[3]);
                vertices.add(new double[]{x, y, z});
            } else if (parts[0].equals("f")) { // Face line
                int[] faceIndices = new int[parts.length - 1];
                for (int i = 1; i < parts.length; i++) {
                    faceIndices[i - 1] = Integer.parseInt(parts[i].split("/")[0]) - 1; // Convert 1-based to 0-based index
                }
                faces.add(faceIndices);

                // Add edges for the face
                for (int i = 0; i < faceIndices.length; i++) {
                    start = faceIndices[i];
                    int end = faceIndices[(i + 1) % faceIndices.length];
                    int[] edge = {Math.min(start, end), Math.max(start, end)};
                    if (!lines.contains(edge)) {
                        lines.add(edge);
                    }
                }
            }
        }
        reader.close();

        double[][] vertArray = vertices.toArray(new double[0][]);
        int[][] lineArray = lines.toArray(new int[0][]);
        int[][] faceArray = faces.toArray(new int[0][]);

        return new ModelData(vertArray, lineArray, faceArray);
    }

    public static void drawLines(ModelData model, LineComponent lineComponent, int focalLength, int[] screenSz) {
        for (int[] line : model.LineTable) {
            double[] start = model.VertTable[line[0]];
            double[] end = model.VertTable[line[1]];
            double[] proj1 = ProjectionMapper(start[0], start[1], start[2], focalLength);
            double[] proj2 = ProjectionMapper(end[0], end[1], end[2], focalLength);

            lineComponent.addLine(
                proj1[0] + screenSz[0] / 2, 
                screenSz[1] / 2 - proj1[1], 
                proj2[0] + screenSz[0] / 2, 
                screenSz[1] / 2 - proj2[1]
            );
        }
    }

    public static void drawFaces(ModelData model, LineComponent lineComponent, int focalLength, int[] screenSz) {
        for (int[] face : model.FaceTable) {
            int[] xPoints = new int[face.length];
            int[] yPoints = new int[face.length];

            for (int j = 0; j < face.length; j++) {
                double[] vertex = model.VertTable[face[j]];
                double[] proj = ProjectionMapper(vertex[0], vertex[1], vertex[2], focalLength);
                xPoints[j] = (int) (proj[0] + screenSz[0] / 2);
                yPoints[j] = (int) (screenSz[1] / 2 - proj[1]);
            }

            lineComponent.addFace(xPoints, yPoints, Color.LIGHT_GRAY); // Default face color
        }
    }

    public static double[] ProjectionMapper(double x, double y, double z, double focalLength) {
        double x2D = (x * focalLength) / (z + focalLength);
        double y2D = (y * focalLength) / (z + focalLength);
        return new double[]{x2D, y2D};
    }

    public static void rotateCube(double[][] VertTable, double angle) {
        double rad = Math.toRadians(angle);

        for (int i = 0; i < VertTable.length; i++) {
            double[] vertex = VertTable[i];
            double x = vertex[0];
            double z = vertex[2];

            double newX = x * Math.cos(rad) - z * Math.sin(rad);
            double newZ = x * Math.sin(rad) + z * Math.cos(rad);

            vertex[0] = newX;
            vertex[2] = newZ;
        }
    }
}

class ModelData {
    public double[][] VertTable;
    public int[][] LineTable;
    public int[][] FaceTable;

    public ModelData(double[][] VertTable, int[][] LineTable, int[][] FaceTable) {
        this.VertTable = VertTable;
        this.LineTable = LineTable;
        this.FaceTable = FaceTable;
    }
}

class LineComponent extends JComponent {
    private ArrayList<Line2D.Double> lines;
    private ArrayList<Polygon> faces;

    public LineComponent(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        lines = new ArrayList<>();
        faces = new ArrayList<>();
    }

    public void addLine(double x1, double y1, double x2, double y2) {
        lines.add(new Line2D.Double(x1, y1, x2, y2));
        repaint();
    }

    public void addFace(int[] xPoints, int[] yPoints, Color color) {
        Polygon polygon = new Polygon(xPoints, yPoints, xPoints.length);
        faces.add(polygon);
        repaint();
    }

    public void clear() {
        lines.clear();
        faces.clear();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.LIGHT_GRAY);
        for (Polygon face : faces) {
            g.fillPolygon(face);
        }

        g.setColor(Color.BLACK);
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
