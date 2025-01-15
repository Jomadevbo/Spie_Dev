package helloworld;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;

public class LineComponent extends JComponent {
    private ArrayList<Line2D.Double> lines;
    private ArrayList<ColoredPolygon> faces;

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
        faces.add(new ColoredPolygon(polygon, color));
        repaint();
    }

    public void clear() {
        lines.clear();
        faces.clear();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Enable antialiasing for smooth rendering
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw faces with the calculated color
        for (ColoredPolygon coloredPolygon : faces) {
            g.setColor(coloredPolygon.color);
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
