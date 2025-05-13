package com.example.flappybirdklon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Random;

// GameView class handles the game logic and rendering for the Flappy Bird clone
public class GameView extends View {
    private float birdY; // Bird's Y position
    private float birdVelocity; // Bird's vertical velocity
    private boolean isGameOver; // Game over state
    private boolean gameStarted; // Start state
    private Paint paint; // Paint for drawing
    private Bitmap birdBitmap; // Bird sprite
    private Bitmap backgroundBitmap; // Background image
    private Bitmap pipeTopBitmap; // Top pipe sprite
    private Bitmap pipeBottomBitmap; // Bottom pipe sprite
    private ArrayList<Pipe> pipes; // List of pipes
    private Random random; // Random number generator
    private int score; // Player score

    // Pipe class to store position and state of each pipe
    private class Pipe {
        float x; // Pipe's X position
        float gapY; // Center of the gap
        boolean scored; // Whether pipe has been scored
        static final float GAP_HEIGHT = 350; // Height of gap between pipes

        Pipe(float x) {
            this.x = x;
            // Randomly set gap position, ensuring it stays within screen bounds
            this.gapY = 200 + random.nextInt((int) (getHeight() - 400));
            this.scored = false;
        }
    }

    // Constructor initializes the game view
    public GameView(Context context) {
        super(context);
        init();
    }

    // Initialize game variables and load resources
    private void init() {
        birdY = 0; // Will be set dynamically in onDraw
        birdVelocity = 0;
        isGameOver = false;
        gameStarted = false;
        paint = new Paint();
        paint.setAntiAlias(true); // Smooth rendering
        pipes = new ArrayList<>();
        random = new Random();
        score = 0;

        // Load and scale bitmaps for bird, background, and pipes
        try {
            birdBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bird);
            if (birdBitmap == null) throw new Exception("Failed to load bird.png");
            // Scale bird to 100x100 pixels
            birdBitmap = Bitmap.createScaledBitmap(birdBitmap, 100, 100, true);

            backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background);
            if (backgroundBitmap == null) throw new Exception("Failed to load background.png");

            pipeTopBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pipe_top);
            if (pipeTopBitmap == null) throw new Exception("Failed to load pipe_top.png");
            // Scale pipe to 250 width, 2400 height
            pipeTopBitmap = Bitmap.createScaledBitmap(pipeTopBitmap, 250, 2400, true);

            pipeBottomBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pipe_bottom);
            if (pipeBottomBitmap == null) throw new Exception("Failed to load pipe_bottom.png");
            pipeBottomBitmap = Bitmap.createScaledBitmap(pipeBottomBitmap, 250, 2400, true);
        } catch (Exception e) {
            Log.e("GameView", "Error loading bitmap: " + e.getMessage());
            // Fallback to colored rectangles if bitmaps fail to load
            birdBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
            birdBitmap.eraseColor(Color.YELLOW);
            pipeTopBitmap = Bitmap.createBitmap(250, 2400, Bitmap.Config.ARGB_8888);
            pipeTopBitmap.eraseColor(Color.GREEN);
            pipeBottomBitmap = Bitmap.createBitmap(250, 2400, Bitmap.Config.ARGB_8888);
            pipeBottomBitmap.eraseColor(Color.GREEN);
        }
    }

    // Main drawing method, updates and renders the game
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Log screen dimensions for debugging
        Log.d("GameView", "Screen width: " + getWidth() + ", height: " + getHeight());

        // Set bird's initial Y position to center of screen
        if (birdY == 0 && getHeight() > 0) {
            birdY = getHeight() / 2f;
            Log.d("GameView", "Initial birdY: " + birdY);
        }

        // Draw background image, stretched to fit screen
        if (backgroundBitmap != null) {
            canvas.drawBitmap(backgroundBitmap, null, new Rect(0, 0, getWidth(), getHeight()), paint);
        } else {
            canvas.drawColor(Color.CYAN);
            Log.w("GameView", "Background bitmap is null, using fallback color");
        }

        // Show start screen if game hasn't started
        if (!gameStarted) {
            paint.setColor(Color.BLACK);
            paint.setTextSize(50);
            canvas.drawText("Tap to Start", getWidth() / 4f, getHeight() / 2f, paint);
            invalidate();
            return;
        }

        // Initialize pipes when game starts
        if (pipes.isEmpty() && getWidth() > 0) {
            pipes.add(new Pipe(getWidth() + 500));
            Log.d("GameView", "Added first pipe at x: " + (getWidth() + 500));
        }

        // Draw bird at fixed X (100) and current Y position
        if (birdBitmap != null) {
            canvas.drawBitmap(birdBitmap, 100, birdY, paint);
        } else {
            Log.w("GameView", "Bird bitmap is null");
        }

        // Define bird's bounding box for collision detection
        Rect birdRect = new Rect(100, (int) birdY, 100 + birdBitmap.getWidth(), (int) birdY + birdBitmap.getHeight());

        // Update and draw all pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x -= 5; // Move pipe left by 5 pixels per frame

            // Calculate positions for top and bottom pipes
            float topPipeBottomY = pipe.gapY - Pipe.GAP_HEIGHT / 2;
            float bottomPipeTopY = pipe.gapY + Pipe.GAP_HEIGHT / 2;

            // Draw top and bottom pipes
            if (pipeTopBitmap != null && pipeBottomBitmap != null) {
                canvas.drawBitmap(pipeTopBitmap, pipe.x, topPipeBottomY - pipeTopBitmap.getHeight(), paint);
                canvas.drawBitmap(pipeBottomBitmap, pipe.x, bottomPipeTopY, paint);
            } else {
                Log.w("GameView", "Pipe bitmaps are null");
            }

            // Define bounding boxes for collision detection
            Rect topPipeRect = new Rect((int) pipe.x, (int) (topPipeBottomY - pipeTopBitmap.getHeight()),
                    (int) pipe.x + pipeTopBitmap.getWidth(), (int) topPipeBottomY);
            Rect bottomPipeRect = new Rect((int) pipe.x, (int) bottomPipeTopY,
                    (int) pipe.x + pipeBottomBitmap.getWidth(), (int) (bottomPipeTopY + pipeBottomBitmap.getHeight()));

            // Check for collisions between bird and pipes
            if (gameStarted && (Rect.intersects(birdRect, topPipeRect) || Rect.intersects(birdRect, bottomPipeRect))) {
                isGameOver = true;
                Log.d("GameView", "Collision detected");
            }

            // Increment score when bird passes a pipe
            if (!pipe.scored && pipe.x + pipeTopBitmap.getWidth() < 100) {
                score++;
                pipe.scored = true;
            }

            // Add new pipe when current pipe reaches halfway across screen
            if (pipe.x < getWidth() / 2 && i == pipes.size() - 1) {
                pipes.add(new Pipe(pipe.x + 600));
            }
        }

        // Remove pipes that have moved off-screen
        if (!pipes.isEmpty() && pipes.get(0).x < -pipeTopBitmap.getWidth()) {
            pipes.remove(0);
        }

        // Update bird's position if game is active
        if (!isGameOver && gameStarted) {
            birdVelocity += 0.8f; // Apply gravity
            birdY += birdVelocity;

            // End game if bird goes off-screen
            if (birdY < 0 || birdY > getHeight()) {
                isGameOver = true;
                Log.d("GameView", "Bird off-screen: birdY=" + birdY + ", height=" + getHeight());
            }
        }

        // Draw current score
        paint.setColor(Color.BLACK);
        paint.setTextSize(50);
        canvas.drawText("Score: " + score, 50, 100, paint);

        // Display game over screen if game ended
        if (isGameOver) {
            paint.setColor(Color.RED);
            paint.setTextSize(100);
            canvas.drawText("Game Over", getWidth() / 4f, getHeight() / 2f, paint);
            paint.setColor(Color.BLACK);
            paint.setTextSize(40);
            canvas.drawText("Tap to Restart", getWidth() / 3f, getHeight() / 2f + 100, paint);
        }

        // Request redraw for animation
        invalidate();
    }

    // Handle touch input for bird jumps and game state changes
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!gameStarted) {
                gameStarted = true; // Start game on first tap
            } else if (isGameOver) {
                // Restart game: reset variables and clear pipes
                birdY = getHeight() / 2f;
                birdVelocity = 0;
                pipes.clear();
                score = 0;
                isGameOver = false;
                gameStarted = true;
            } else {
                birdVelocity = -12; // Make bird jump upward
            }
        }
        return true;
    }
}