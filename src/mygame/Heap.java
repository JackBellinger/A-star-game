package mygame;

import java.awt.Color;

public class Heap {

    private Point[] data;
    private int last = 0;

    public Heap(int depth) {//this constructor will make a heap with size dependant on how deep you want it to be
        int size = (int) (Math.pow(2, depth) - 1);
        data = new Point[size];
        //for(int i = 0; i < data.length; i++)
            //data[i]= Double.NaN;
    }

    public Heap() {
        data = new Point[20];
    }

    public void add(Point value) {
        if (last != data.length) {
            int current = last;
            data[current] = value;
            int pIndex = getParent(current);
            while (data[pIndex].score > data[current].score) {
                Point temp = data[current];
                data[current] = data[pIndex];
                data[pIndex] = temp;
                current = pIndex;
                pIndex = getParent(current);
            }
            last++;
        }
    }

    public void delete() {
        if (last > 0) {
            data[0] = data[last - 1];
            data[last - 1] = null;
            last--;
            bubbleDown(0);
        }
    }

    private void bubbleDown(int current) {
        int min;
        int left = getLeft(current);
        int right = getRight(current);
        if (right >= last) {
            if (left >= last) {
                return;
            } else {
                min = left;
            }
        } else {
            if (data[left].score <= data[right].score) {
                min = left;
            } else {
                min = right;
            }
        }
        if (data[current].score > data[min].score) {
            Point temp = data[min];
            data[min] = data[current];
            data[current] = temp;
            bubbleDown(min);
        }
    }

    private int getParent(int index) {
        return (index - 1) / 2;
    }

    private int getLeft(int index) {
        return (index * 2) + 1;
    }

    private int getRight(int index) {
        return (index * 2) + 2;
    }
}
