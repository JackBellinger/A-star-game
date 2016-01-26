/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author Jackson Bellinger
 */
public class MrRoboto {
    public float[][] heightMap;
    private LinkedList<Point> openList;
    private LinkedList<Point> closedList;
    private Point start;
    private Point end;
    public LinkedList<Point> path;
    private int size = 0;
    
    public MrRoboto(float[] data, int x, int y){
        size = (int)Math.ceil(Math.sqrt((double)data.length));
        heightMap = new float[size][size];
        for(int i = 0; i < size; i++)//I convert jme's heightmap float array into a 2d array so it's easier to work with
            for(int j = 0; j < size; j++)
                heightMap[i][j] = data[(i * size) + j];
        openList = new LinkedList<Point>();
        closedList = new LinkedList<Point>();
        int sX = (size / 2) + x;
        int sY = (size / 2) + y;
        start = new Point(sX, sY, heightMap[sX][sY]);
    }
    
    public LinkedList<Point> findPathTo(int x, int y){
        int eX = (size / 2) + x;
        int eY = (size / 2) + y;end = new Point(eX, eY, heightMap[eX][eY]);
        path = new LinkedList<Point>();
        Point current = start;
        openList.addFirst(start);
        while(!current.equals(end)){
            current = getBestFromOpenList();
            addAdjacents(current);
        }
        while(!current.equals(start)){
            path.addFirst(current);
            current = current.parent;
        }
        return path;
    }
    public void visualize(Node rootNode, AssetManager assetManager){
        for(Point p : path){
            Sphere sphere = new Sphere(32, 32, 10f, false, true);
            Geometry ball = new Geometry("ball", sphere);
            Material boxMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md"); 
            boxMat.setBoolean("UseMaterialColors", true); 
            boxMat.setColor("Ambient", ColorRGBA.Black); 
            boxMat.setColor("Diffuse", ColorRGBA.Black); 
            ball.setMaterial(boxMat);
            ball.setLocalTranslation((size / 2) + p.x, heightMap[p.x][p.y] + 5, (size / 2) + p.y);
            rootNode.attachChild(ball);
        }
    }
    private Point getBestFromOpenList(){
        Point best = openList.getFirst();
        for(Point p : openList)
            if(p.score < best.score)
                best = p;
        openList.remove(best);
        closedList.addFirst(best);
        return best;
    }
    private void addAdjacents(Point p){
        if(canGoRight(p))
           addToOpenList(new Point(p.x + 1, p.y, heightMap[p.x + 1][p.y], p, end));
        if(canGoLeft(p))
           addToOpenList(new Point(p.x - 1, p.y, heightMap[p.x - 1][p.y], p, end));
        if(canGoUp(p))
           addToOpenList(new Point(p.x, p.y - 1, heightMap[p.x][p.y - 1], p, end));
        if(canGoDown(p))
           addToOpenList(new Point(p.x, p.y + 1, heightMap[p.x][p.y + 1], p, end));
    }

    private boolean canGoRight(Point p){
        return (p.x + 1 < heightMap.length);
    }
    private boolean canGoLeft(Point p){
        return (p.x - 1 > 0);
    }
    private boolean canGoUp(Point p){
        return (p.y - 1 > 0);
    }
    private boolean canGoDown(Point p){
        return (p.y + 1 < heightMap.length);
    }
    
    private void addToOpenList(Point p){
        if(isWalkable(p)){
            for(Iterator it = openList.iterator(); it.hasNext();){
                Point pn = (Point)it.next();
                if(p.equals(pn))
                    it.remove();
                
            }
                
            openList.add(p);
        }  
    }
    private boolean isWalkable(Point p){//placeholder method, if I implement obstacles this will check for them
        return true;
    }
    public void print(){
        for(Point p: path)
            System.out.println(p.x + ", " + p.y);
    }
}
