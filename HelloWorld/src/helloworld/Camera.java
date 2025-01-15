package helloworld;

public class Camera {
    public double[] position;  // Camera position (x, y, z)
    public double yaw;         // Rotation angle around the vertical axis (Y-axis)
    public double pitch;       // Rotation angle around the horizontal axis (X-axis)
    private final double speed; // Speed of camera movement

    public Camera(double[] position) {
        this.position = position;
        this.yaw = 0;
        this.pitch = 0;
        this.speed = 100; // Set a reasonable movement speed
    }

    public double[][] getViewMatrix() {
        double[][] matrix = new double[4][4];

        // Calculate the rotation matrix based on yaw and pitch
        double cosYaw = Math.cos(Math.toRadians(yaw));
        double sinYaw = Math.sin(Math.toRadians(yaw));
        double cosPitch = Math.cos(Math.toRadians(pitch));
        double sinPitch = Math.sin(Math.toRadians(pitch));

        // Set up rotation matrix
        matrix[0][0] = cosYaw;          // X-axis
        matrix[0][1] = 0;               // Y-axis
        matrix[0][2] = -sinYaw;         // Z-axis
        matrix[0][3] = 0;

        matrix[1][0] = sinYaw * sinPitch; // X-axis
        matrix[1][1] = cosPitch;          // Y-axis
        matrix[1][2] = cosYaw * sinPitch; // Z-axis
        matrix[1][3] = 0;

        matrix[2][0] = sinYaw * cosPitch; // X-axis
        matrix[2][1] = -sinPitch;         // Y-axis
        matrix[2][2] = cosYaw * cosPitch; // Z-axis
        matrix[2][3] = 0;

        // Translate to camera position
        matrix[3][0] = -dotProduct(matrix[0], position);
        matrix[3][1] = -dotProduct(matrix[1], position);
        matrix[3][2] = -dotProduct(matrix[2], position);
        matrix[3][3] = 1;

        return matrix;
    }

    private double dotProduct(double[] vector, double[] point) {
        return vector[0] * point[0] + vector[1] * point[1] + vector[2] * point[2];
    }

    public void rotate(double deltaYaw, double deltaPitch) {
        this.yaw += deltaYaw;
        this.pitch += deltaPitch;

        // Clamp pitch to avoid flipping
        if (this.pitch > 89) this.pitch = 89;
        if (this.pitch < -89) this.pitch = -89;
    }

    public void moveForward() {
        position[0] += speed * Math.sin(Math.toRadians(yaw)); // Move in the direction of yaw
        position[2] -= speed * Math.cos(Math.toRadians(yaw)); // Adjust for the Z-axis
    }

    public void moveBackward() {
        position[0] -= speed * Math.sin(Math.toRadians(yaw)); // Move in the opposite direction of yaw
        position[2] += speed * Math.cos(Math.toRadians(yaw)); // Adjust for the Z-axis
    }

    public void moveLeft() {
        position[0] -= speed * Math.cos(Math.toRadians(yaw)); // Move left relative to yaw
        position[2] -= speed * Math.sin(Math.toRadians(yaw)); // Adjust for the Z-axis
    }

    public void moveRight() {
        position[0] += speed * Math.cos(Math.toRadians(yaw)); // Move right relative to yaw
        position[2] += speed * Math.sin(Math.toRadians(yaw)); // Adjust for the Z-axis
    }

    public void moveUp() {
        position[1] += speed; // Move up
    }

    public void moveDown() {
        position[1] -= speed; // Move down
    }
}