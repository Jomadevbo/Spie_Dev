package helloworld;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
                    String objFilePath = new File(".").getCanonicalPath() + "\\src\\Helloworld\\model.obj";
                    ModelData modelData = loadModel(objFilePath);

                    int[] screenSz = {1300, 700};
                    int focalLength = 110;

                    LineComponent lineComponent = new LineComponent(screenSz[0], screenSz[1]);

                    JPanel panel = new JPanel();
                    panel.add(lineComponent);

                    JButton rotateButton = new JButton("Rotate");
                    panel.add(rotateButton);

                    Timer timer = new Timer(25, new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            rotateCube(modelData.VertTable, 2);
                            lineComponent.clear();
                            drawFaces(modelData, lineComponent, focalLength, screenSz);
                            //drawLines(modelData, lineComponent, focalLength, screenSz);
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
                    //drawLines(modelData, lineComponent, focalLength, screenSz);

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

    public static void drawFaces(ModelData model, LineComponent lineComponent, int focalLength, int[] screenSz) {
        List<FaceData> faceDataList = new ArrayList<>();

        for (int[] face : model.FaceTable) {
            int[] xPoints = new int[face.length];
            int[] yPoints = new int[face.length];
            double meanZ = 0;

            // Calculate the face normal and determine brightness
            double[] normal = calculateNormal(model.VertTable, face);
            double brightness = calculateBrightness(normal);

            for (int j = 0; j < face.length; j++) {
                double[] vertex = model.VertTable[face[j]];
                meanZ += vertex[2];
                double[] proj = ProjectionMapper(vertex[0], vertex[1], vertex[2], focalLength);
                xPoints[j] = (int) (proj[0] + screenSz[0] / 2);
                yPoints[j] = (int) (screenSz[1] / 2 - proj[1]);
            }

            meanZ /= face.length;
            faceDataList.add(new FaceData(meanZ, xPoints, yPoints, brightness));
        }

        // Sort the faces by meanZ to draw the closest ones last (in front)
        faceDataList.sort((a, b) -> Double.compare(b.meanZ, a.meanZ));

        // Draw faces with proper brightness
        for (FaceData faceData : faceDataList) {
        
            int brightnessValue = (int) (faceData.brightness);
            brightnessValue = Math.max(0, Math.min(255, brightnessValue));
            Color faceColor = new Color(brightnessValue, brightnessValue, brightnessValue);

            // This is where you set the values for ColoredPolygon
            lineComponent.addFace(faceData.xPoints, faceData.yPoints, faceColor);
        }
    }


    // Calculate the normal vector of a face using its vertices
    public static double[] calculateNormal(double[][] VertTable, int[] face) {
        double[] v0 = VertTable[face[0]];
        double[] v1 = VertTable[face[1]];
        double[] v2 = VertTable[face[2]];

        // Calculate two edge vectors
        double[] edge1 = {v1[0] - v0[0], v1[1] - v0[1], v1[2] - v0[2]};
        double[] edge2 = {v2[0] - v0[0], v2[1] - v0[1], v2[2] - v0[2]};

        // Calculate the cross product to find the normal
        double[] normal = {
            edge1[1] * edge2[2] - edge1[2] * edge2[1],
            edge1[2] * edge2[0] - edge1[0] * edge2[2],
            edge1[0] * edge2[1] - edge1[1] * edge2[0]
        };

        // Normalize the normal vector
        double length = Math.sqrt(normal[0] * normal[0] + normal[1] * normal[1] + normal[2] * normal[2]);
        if (length != 0) {
            normal[0] /= length;
            normal[1] /= length;
            normal[2] /= length;
        }

        return normal;
    }

    // Calculate the brightness based on the dot product of the normal and light direction
    public static double calculateBrightness(double[] normal) {
        // Example light direction (you can make this dynamic)
        double[] lightDir = {0.5, 0.5, 0}; // Light coming from the positive Z direction

        // Calculate the dot product between the normal and the light direction
        double dotProduct = normal[0] * lightDir[0] + normal[1] * lightDir[1] + normal[2] * lightDir[2];

        // Clamp the dot product to be between 0 and 240, seperation from Background was nescessary
        double brightnessFactor = Math.min(255, Math.max(0, (dotProduct+1)*120));

        return brightnessFactor;

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
    private ArrayList<ColoredPolygon> faces; // Change to store ColoredPolygon

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
        faces.add(new ColoredPolygon(polygon, color)); // Store both polygon and color
        repaint();
    }

    public void clear() {
        lines.clear();
        faces.clear();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // Set rendering hints for antialiasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paintComponent(g);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw faces with the calculated color
        for (ColoredPolygon coloredPolygon : faces) {
            g.setColor(coloredPolygon.color); // Use the stored color
            g.fillPolygon(coloredPolygon.polygon);
        }

        // Draw lines on top of the faces
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

class FaceData {
    public double meanZ;
    public int[] xPoints;
    public int[] yPoints;
    public double brightness;

    public FaceData(double meanZ, int[] xPoints, int[] yPoints, double brightness) {
        this.meanZ = meanZ;
        this.xPoints = xPoints;
        this.yPoints = yPoints;
        this.brightness = brightness;
    }
}
class ColoredPolygon {
    Polygon polygon;
    Color color;

    public ColoredPolygon(Polygon polygon, Color color) {
        this.polygon = polygon;
        this.color = color;
    }
}
