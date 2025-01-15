package helloworld;

import java.awt.Color;
import java.util.ArrayList;

public class DrawUtils {

    public static void drawFaces(ModelData model, LineComponent lineComponent, Camera camera, int focalLength, int[] screenSz) {
        ArrayList<FaceData> faceDataList = new ArrayList<>();
        
        // Get the camera's view matrix
        double[][] viewMatrix = camera.getViewMatrix();

        for (int[] face : model.FaceTable) {
            int[] xPoints = new int[face.length];
            int[] yPoints = new int[face.length];
            double meanZ = 0;

            double[] normal = calculateNormal(model.VertTable, face);
            double brightness = calculateBrightness(normal);

            for (int j = 0; j < face.length; j++) {
                double[] vertex = model.VertTable[face[j]];

                // Transform the vertex by the camera's view matrix
                double[] transformedVertex = transformVertex(vertex, viewMatrix);
                double[] proj = ProjectionMapper(transformedVertex[0], transformedVertex[1], transformedVertex[2], focalLength);
                xPoints[j] = (int) (proj[0] + screenSz[0] / 2);
                yPoints[j] = (int) (screenSz[1] / 2 - proj[1]);
                
                // Calculate mean Z for depth sorting
                meanZ += transformedVertex[2]; // Use the transformed Z value
            }

            meanZ /= face.length; // Compute mean Z
            faceDataList.add(new FaceData(meanZ, xPoints, yPoints, brightness));
        }

        faceDataList.sort((a, b) -> Double.compare(b.meanZ, a.meanZ));

        for (FaceData faceData : faceDataList) {
            int brightnessValue = (int) (faceData.brightness);
            brightnessValue = Math.max(0, Math.min(255, brightnessValue));
            Color faceColor = new Color(brightnessValue, brightnessValue, brightnessValue);

            lineComponent.addFace(faceData.xPoints, faceData.yPoints, faceColor);
        }
    }

    private static double[] transformVertex(double[] vertex, double[][] viewMatrix) {
        double[] transformedVertex = new double[4]; // Homogeneous coordinates
        transformedVertex[0] = vertex[0] * viewMatrix[0][0] + vertex[1] * viewMatrix[0][1] + vertex[2] * viewMatrix[0][2] + viewMatrix[0][3];
        transformedVertex[1] = vertex[0] * viewMatrix[1][0] + vertex[1] * viewMatrix[1][1] + vertex[2] * viewMatrix[1][2] + viewMatrix[1][3];
        transformedVertex[2] = vertex[0] * viewMatrix[2][0] + vertex[1] * viewMatrix[2][1] + vertex[2] * viewMatrix[2][2] + viewMatrix[2][3];
        transformedVertex[3] = 1.0; // Homogeneous coordinate
        return transformedVertex;
    }

    public static double[] ProjectionMapper(double x, double y, double z, double focalLength) {
        double x2D = (x * focalLength) / (z + focalLength);
        double y2D = (y * focalLength) / (z + focalLength);
        return new double[]{x2D, y2D};
    }

    public static double[] calculateNormal(double[][] VertTable, int[] face) {
        double[] v0 = VertTable[face[0]];
        double[] v1 = VertTable[face[1]];
        double[] v2 = VertTable[face[2]];

        double[] edge1 = {v1[0] - v0[0], v1[1] - v0[1], v1[2] - v0[2]};
        double[] edge2 = {v2[0] - v0[0], v2[1] - v0[1], v2[2] - v0[2]};

        double[] normal = {
            edge1[1] * edge2[2] - edge1[2] * edge2[1],
            edge1[2] * edge2[0] - edge1[0] * edge2[2],
            edge1[0] * edge2[1] - edge1[1] * edge2[0]
        };

        double length = Math.sqrt(normal[0] * normal[0] + normal[1] * normal[1] + normal[2] * normal[2]);
        if (length != 0) {
            normal[0] /= length;
            normal[1] /= length;
            normal[2] /= length;
        }

        return normal;
    }

    public static double calculateBrightness(double[] normal) {
        double[] lightDir = {0.5, 0.5, 0}; // Direction of the light
        double dotProduct = normal[0] * lightDir[0] + normal[1] * lightDir[1] + normal[2] * lightDir[2];
        return Math.max(0, Math.min(255, (dotProduct + 1) * 120));
    }
}