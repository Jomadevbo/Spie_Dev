package helloworld;

public class FaceData {
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
