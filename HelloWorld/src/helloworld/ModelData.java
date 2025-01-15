package helloworld;

public class ModelData {
    public double[][] VertTable;
    public int[][] LineTable;
    public int[][] FaceTable;

    public ModelData(double[][] VertTable, int[][] LineTable, int[][] FaceTable) {
        this.VertTable = VertTable;
        this.LineTable = LineTable;
        this.FaceTable = FaceTable;
    }
}