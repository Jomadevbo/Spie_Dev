package helloworld;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class LineComponent extends JComponent {
    private ArrayList<Line2D.Double> lines;
    private ArrayList<ColoredPolygon> faces;
    private BufferedImage buffer;

    public LineComponent(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        lines = new ArrayList<>();
        faces = new ArrayList<>();
        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    public void addLine(double x1, double y1, double x2, double y2) {
        lines.add(new Line2D.Double(x1, y1, x2, y2));
        repaint();
    }

    public void addFace(int[] xPoints, int[] yPoints, Color color) {
        Polygon polygon = new Polygon(xPoints, yPoints, xPoints.length);
        faces.add(new ColoredPolygon(polygon, color));
        repaint();
    }

    public void clear() {
        lines.clear();
        faces.clear();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = buffer.createGraphics();

        // Enable antialiasing for smooth rendering
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Clear the buffer
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Draw faces with the calculated color
        for (ColoredPolygon coloredPolygon : faces) {
            g2.setColor(coloredPolygon.color);
            g2.fillPolygon(coloredPolygon.polygon);
        }

        // Draw lines on top of the faces
        g2.setColor(Color.BLACK);
        for (Line2D.Double line : lines) {
            g2.drawLine(
                (int) line.getX1(),
                (int) line.getY1(),
                (int) line.getX2(),
                (int) line.getY2()
            );
        }

        g2.dispose(); // Dispose of the graphics context

        // Draw the buffer to the component
        g.drawImage(buffer, 0, 0, null);
    }
}