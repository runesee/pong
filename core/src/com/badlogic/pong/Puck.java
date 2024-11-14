package com.badlogic.pong;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Random;

public class Puck {
    float x, y;
    float size = 15;
    Vector2 vector;
    int minSpeed;
    private ArrayList<PuckObserver> observers = new ArrayList<>();

    public Puck(float x, float y, float size, int minSpeed) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.minSpeed = minSpeed;
        this.vector = getRandomVector();
    }

    public Puck(float x, float y, int minSpeed) {
        this.x = x;
        this.y = y;
        this.minSpeed = minSpeed;
        this.vector = getRandomVector();
    }

    public Vector2 getVector() {
        return vector;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getSize() {
        return size;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setVector(Vector2 vector) {
        this.vector = vector;
    }

    // Helper function for generating a random (limited) starting vector
    private Vector2 getRandomVector() {
        Random rand = new Random();
        // Define a minimum x-component speed for horizontal movement
        float minXSpeed = this.minSpeed * 0.5f;

        // Generate a random (limited) angle
        float angle;
        do {
            angle = rand.nextFloat() * MathUtils.PI2; // Random angle from 0 to 2π
        } while (Math.abs(MathUtils.cos(angle)) * this.minSpeed < minXSpeed);

        // Convert polar coordinates (angle & magnitude) to Cartesian coordinates (x, y)
        float x = MathUtils.cos(angle) * this.minSpeed;
        float y = MathUtils.sin(angle) * this.minSpeed;
        return new Vector2(x, y);
    }

    // Function for resetting puck position after a point has been scored
    public void resetPuck(int width, int height) {
            // Center position
            this.setX((float) width / 2 - this.size / 2);
            this.setY((float) height / 2 - this.size / 2);

            // Generate new, random initial vector
            this.setVector(getRandomVector());
    }

    public void addObserver(PuckObserver observer) {
        observers.add(observer);
    }

    public void notifyObservers(int scoringSide) {
        for (PuckObserver observer : observers) {
            observer.onPuckScored(scoringSide);
        }
    }

    // Update puck position
    public void update() {
        this.x += this.vector.x;
        this.y += this.vector.y;

        // If the puck crosses the left boundary
        if (this.x < 0) {
            notifyObservers(-1);
        }
        // If the puck crosses the right boundary
        else if (this.x > Gdx.graphics.getWidth()) {
            notifyObservers(1);
        }


        // Check if puck hits roof or floor
        if (vector.y > 0) { // Moving upwards
            if ((y + size + vector.y) >= Gdx.graphics.getHeight()) {
                setVector(new Vector2(vector.x, vector.y*(-1)));
                setY(Gdx.graphics.getHeight() - size); // Adjust position to prevent multiple collisions
            }
        } else if (vector.y < 0) { // Moving downwards
            if ((y + vector.y) <= 0) {
                setVector(new Vector2(vector.x, vector.y*(-1)));
                setY(0); // Adjust position to prevent multiple collisions
            }
        }
    }
}


