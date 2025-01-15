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

            // Calculate the vector from the camera to the face's centroid
            double[] centroid = new double[3];
            for (int vertexIndex : face) {
                centroid[0] += model.VertTable[vertexIndex][0];
                centroid[1] += model.VertTable[vertexIndex][1];
                centroid[2] += model.VertTable[vertexIndex][2];
            }
            centroid[0] /= face.length;
            centroid[1] /= face.length;
            centroid[2] /= face.length;

            double[] cameraToCentroid = {
                centroid[0] - camera.position[0],
                centroid[1] - camera.position[1],
                centroid[2] - camera.position[2]
            };

            // Check if the face is facing the camera using the dot product
            double dotProduct = dot(normal, cameraToCentroid);
            if (dotProduct < 0) {
                // Face is facing away from the camera, skip rendering this face
                continue;
            }

            for (int j = 0; j < face.length; j++) {
                double[] vertex = model.VertTable[face[j]];

                // Transform the vertex by the camera's view matrix
                double[] transformedVertex = transformVertex(vertex, viewMatrix);
                
                // Check if the vertex is in front of the near plane
                if (transformedVertex[2] <= 0) {
                    continue; // Skip vertices behind the near plane
                }
                
                double[] proj = ProjectionMapper(transformedVertex[0], transformedVertex[1], transformedVertex[2], focalLength);
                xPoints[j] = (int) (proj[0] + screenSz[0] / 2);
                yPoints[j] = (int) (screenSz[1] / 2 - proj[1]);

                meanZ += transformedVertex[2]; // Use the transformed Z value
            }

            meanZ /= face.length; // Compute mean Z
            faceDataList.add(new FaceData(meanZ, xPoints, yPoints, calculateBrightness(normal, camera.position, centroid)));
        }

        // Sort faces based on mean Z value for proper depth rendering
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
        transformedVertex[3] = vertex[0] * viewMatrix[3][0] + vertex[1] * viewMatrix[3][1] + vertex[2] * viewMatrix[3][2] + viewMatrix[3][3]; // Homogeneous coordinate
        return transformedVertex;
    }

    private static double[] calculateNormal(double[][] vertices, int[] face) {
        double[] v0 = vertices[face[0]];
        double[] v1 = vertices[face[1]];
        double[] v2 = vertices[face[2]];

        double[] edge1 = { v1[0] - v0[0], v1[1] - v0[1], v1[2] - v0[2] };
        double[] edge2 = { v2[0] - v0[0], v2[1] - v0[1], v2[2] - v0[2] };

        double[] normal = {
            edge1[1] * edge2[2] - edge1[2] * edge2[1],
            edge1[2] * edge2[0] - edge1[0] * edge2[2],
            edge1[0] * edge2[1] - edge1[1] * edge2[0]
        };
        
        //Flip normals because have to else program no no worky
        normal[0] = -normal[0];
        normal[1] = -normal[1];
        normal[2] = -normal[2];

        double length = Math.sqrt(normal[0] * normal[0] + normal[1] * normal[1] + normal[2] * normal[2]);
        if (length > 0) {
            normal[0] /= length;
            normal[1] /= length;
            normal[2] /= length;
        }

        return normal;
    }

    private static double calculateBrightness(double[] normal, double[] cameraPosition, double[] faceCentroid) {
        // Calculate the light direction from the face centroid to the light source
        double[] lightDirection = { 0, 0, -1 }; // Light coming from the "back" of the camera

        // Calculate the direction from the face centroid to the camera
        double[] toCamera = {
            cameraPosition[0] - faceCentroid[0],
            cameraPosition[1] - faceCentroid[1],
            cameraPosition[2] - faceCentroid[2]
        };

        // Normalize the toCamera vector
        double length = Math.sqrt(toCamera[0] * toCamera[0] + toCamera[1] * toCamera[1] + toCamera[2] * toCamera[2]);
        if (length > 0) {
            toCamera[0] /= length;
            toCamera[1] /= length;
            toCamera[2] /= length;
        }

        // Calculate brightness based on the angle between the normal and light direction
        double dotProduct = dot(normal, lightDirection);
        double brightness = Math.max(0, dotProduct * 255); // Scale brightness

        return brightness;
    }

    private static double[] ProjectionMapper(double x, double y, double z, int focalLength) {
        double[] projected = new double[2];
        projected[0] = (focalLength * x) / z; // Perspective projection
        projected[1] = (focalLength * y) / z; // Perspective projection
        return projected;
    }

    private static double dot(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }
}