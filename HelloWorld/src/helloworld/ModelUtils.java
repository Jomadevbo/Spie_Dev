package helloworld;

import java.io.*;
import java.util.*;

public class ModelUtils {

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
                    faceIndices[i - 1] = Integer.parseInt(parts[i].split("/")[0]) - 1;
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
