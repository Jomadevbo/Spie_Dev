package helloworld;

public class Camera {
    public double[] position;
    public double[] direction;  // Direction vector (camera's view)
    public double[] up;         // Up vector
    public double nearPlane;    // Near clipping plane
    public double farPlane;     // Far clipping plane
    public double fov;          // Field of view (in radians)
    public double aspectRatio;  // Aspect ratio of the screen

    public Camera(double[] position, double[] direction, double[] up, double fov, double aspectRatio, double nearPlane, double farPlane) {
        this.position = position;
        this.direction = direction;
        this.up = up;
        this.fov = fov;
        this.aspectRatio = aspectRatio;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;
    }

    // This method creates a view matrix, for simplicity, we assume the camera looks at the origin
    public double[][] getViewMatrix() {
        // Compute the right, up, and forward vectors
        double[] right = crossProduct(direction, up);
        double[] trueUp = crossProduct(right, direction);

        // Translate the camera position to the origin
        double[][] viewMatrix = new double[4][4];

        viewMatrix[0][0] = right[0];
        viewMatrix[0][1] = right[1];
        viewMatrix[0][2] = right[2];
        viewMatrix[0][3] = -dotProduct(right, position);

        viewMatrix[1][0] = trueUp[0];
        viewMatrix[1][1] = trueUp[1];
        viewMatrix[1][2] = trueUp[2];
        viewMatrix[1][3] = -dotProduct(trueUp, position);

        viewMatrix[2][0] = -direction[0];
        viewMatrix[2][1] = -direction[1];
        viewMatrix[2][2] = -direction[2];
        viewMatrix[2][3] = dotProduct(direction, position);

        viewMatrix[3][0] = 0;
        viewMatrix[3][1] = 0;
        viewMatrix[3][2] = 0;
        viewMatrix[3][3] = 1;

        return viewMatrix;
    }

    // Movement methods
    public void moveForward(double distance) {
        position[0] += direction[0] * distance;
        position[1] += direction[1] * distance;
        position[2] += direction[2] * distance;
    }

    public void moveBackward(double distance) {
        moveForward(-distance);
    }

    public void moveLeft(double distance) {
        double[] left = crossProduct(up, direction);
        position[0] += left[0] * distance;
        position[1] += left[1] * distance;
        position[2] += left[2] * distance;
    }

    public void moveRight(double distance) {
        moveLeft(-distance);
    }

    // Utility methods for vector math
    private double[] crossProduct(double[] a, double[] b) {
        return new double[]{
            a[1] * b[2] - a[2] * b[1],
            a[2] * b[0] - a[0] * b[2],
            a[0] * b[1] - a[1] * b[0]
        };
    }

    private double dotProduct(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }
}