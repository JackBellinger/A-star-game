package mygame;

public class Point {
    public int x;
    public int y;
    public float height;
    public Point parent;
    public double toHereScore = 0;
    public double toEndScore = 0;
    public double score = 0;
    
    public Point(int x, int y, float height){
        this.x = x;
        this.y = y;
        this.height = height;
    }
    public Point(int x, int y, float height, Point prev, Point end){
        this.x = x;
        this.y = y;
        this.height = height;
        parent = prev;
        updateToHereScore(prev);
        toEndScore = getDistanceTo(end);
        score = toHereScore + toEndScore;
    }
    
    private double getDistanceTo(Point b){
        float dX = Math.abs(x - b.x);
        float dY = Math.abs(y - b.y);
        double distance = Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2));
        return distance;
    }
    private void updateToHereScore(Point prev){
        double dist = getDistanceTo(prev);
        float dHeight = Math.abs(prev.height - height);
        toHereScore = prev.toHereScore + (dist * dHeight);
    }
    public double getToHereScore(){
        return toHereScore;
    }
    public boolean equals(Point p){
        return(this.x == p.x && this.y == p.y);
    }
}
