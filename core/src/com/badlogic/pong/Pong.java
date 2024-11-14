package com.badlogic.pong;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.InputAdapter;

public class Pong extends ApplicationAdapter implements PuckObserver{
	SpriteBatch batch;
	Texture paddleTexture, puckTexture;
	float paddleWidth = 8, paddleHeight = 100;
	float leftPaddleX, leftPaddleY;
	float rightPaddleX, rightPaddleY;
	int puckStartSpeed = 15;		// CHANGE THIS VALUE IF YOU WANT A FASTER SIMULATION
	int aiSpeedScale = 350;		// CHANGE THIS VALUE FOR MORE DIFFICULT OPPONENT
	int collisionCounter = 0;
	String winText;
	BitmapFont font;
	float speedIncrement = 1f;
	float maxSpeed = 20f;
	float aiTolerance = 10f;
	Puck puck;
	Game game;


	@Override
	public void create () {
		batch = new SpriteBatch();
		paddleTexture = new Texture("paddle.png");
		puckTexture = new Texture("puck.png");
		font = new BitmapFont();
		font.setColor(1,1,1,1);

		// Initialize paddle positions
		leftPaddleX = 20;
		leftPaddleY = (float) Gdx.graphics.getHeight() / 2 - paddleHeight / 2;
		rightPaddleX = Gdx.graphics.getWidth() - 20 - paddleWidth;
		rightPaddleY = (float) Gdx.graphics.getHeight() / 2 - paddleHeight / 2;

		float puckX = 0;
		float puckY = 0;

		// Initialize instances
		game = Game.getInstance();
		puck = new Puck(puckX, puckY, puckStartSpeed);
		puck.addObserver(this); // Register this class as an observer

		// Initialize puck position
		puck.setX((float) Gdx.graphics.getWidth() / 2 - puck.getSize() / 2);
		puck.setY((float) Gdx.graphics.getHeight() / 2 - puck.getSize() / 2);

		// Set up input processing for dragging (mouse/touch input)
		Gdx.input.setInputProcessor(new InputAdapter() {
			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) {
				if (game.isGameOver()) {
					game.resetGame();
					puck.resetPuck(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
					return true;
				}

				// Convert the touch/mouse coordinates to game coordinates
				float touchY = Gdx.graphics.getHeight() - screenY;

				// Update the left paddle's Y position based on the touch
				leftPaddleY = touchY - paddleHeight / 2;
				leftPaddleY = Math.max(leftPaddleY, 0); // Prevent it from going below floor
				leftPaddleY = Math.min(leftPaddleY, Gdx.graphics.getHeight() - paddleHeight); // Prevent it from going above roof
				return true;
			}
		});
	}

	@Override
	public void render () {
		// Update puck position
		if (!game.isGameOver()) {
			puck.update(); // This will also check for scoring
		}

		// Simple AI for right paddle
		float aiSpeed = aiSpeedScale * Gdx.graphics.getDeltaTime();
		float puckCenterY = puck.getY() + puck.getSize() / 2;
		float paddleCenterY = rightPaddleY + paddleHeight / 2;

		// Move the AI paddle towards the puck with a tolerance 'aiTolerance' to combat jittering
		if (!game.isGameOver()) {
			if (puckCenterY > paddleCenterY + aiTolerance) {
				rightPaddleY += aiSpeed; // Move down if puck is sufficiently lower
			} else if (puckCenterY < paddleCenterY - aiTolerance) {
				rightPaddleY -= aiSpeed; // Move up if puck is sufficiently higher
			}
		}

		// Prevent AI paddle from moving out of bounds
		rightPaddleY = Math.max(rightPaddleY, 0);
		rightPaddleY = Math.min(rightPaddleY, Gdx.graphics.getHeight() - paddleHeight);

		// Create rectangles for collision detection
		Rectangle leftPaddleRect = new Rectangle(leftPaddleX, leftPaddleY, paddleWidth, paddleHeight);
		Rectangle rightPaddleRect = new Rectangle(rightPaddleX, rightPaddleY, paddleWidth, paddleHeight);

		// Handle collisions with either paddle
		handlePaddleCollision(leftPaddleRect);
		handlePaddleCollision(rightPaddleRect);

		// Set win text if either 'player' has won
		if (game.getLeftPlayerScore() >= game.getWinningScore() || game.getRightPlayerScore() >= game.getWinningScore()) {
			winText = game.getLeftPlayerScore() >= game.getWinningScore() ? "Left-hand player has won!" : "Right-hand player has won!";
		}

		// Rendering
		ScreenUtils.clear(0, 0, 0, 1);
		batch.begin();
		// Draw left paddle
		batch.draw(paddleTexture, leftPaddleX, leftPaddleY, paddleWidth, paddleHeight);
		// Draw right paddle
		batch.draw(paddleTexture, rightPaddleX, rightPaddleY, paddleWidth, paddleHeight);
		// Draw puck
		batch.draw(puckTexture, puck.getX(), puck.getY(), puck.getSize(), puck.getSize());

		if (game.isGameOver()) {
			font.draw(batch, winText, (float) Gdx.graphics.getWidth() /2, (float) Gdx.graphics.getHeight() /2);
			font.draw(batch, "Move paddle to start a new game", (float) Gdx.graphics.getWidth() /2, (float) Gdx.graphics.getHeight() /2 - 40);
		}
		// Draw scores
		font.draw(batch, Integer.toString(game.getRightPlayerScore()), Gdx.graphics.getWidth()-20, Gdx.graphics.getHeight()-10);
		font.draw(batch, Integer.toString(game.getLeftPlayerScore()), 20, Gdx.graphics.getHeight()-10);

		// Draw collision counter
		font.draw(batch, Integer.toString(collisionCounter),
				(float) Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() - 35);

		batch.end();
	}

	// Function for resetting game state after a point has been scored
	private void resetBoard() {
		if (!game.isGameOver()) {
			puck.resetPuck(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			collisionCounter = 0;
		}
	}

	// Helper method for handling paddle-puck collision
	private void handlePaddleCollision(Rectangle paddleRect) {
		Rectangle puckRect = new Rectangle(puck.getX(), puck.getY(), puck.getSize(), puck.getSize());
		if (!puckRect.overlaps(paddleRect)) {
			return; // No collision
		}

		// Collision detected, increment collision counter
		collisionCounter++;

		// Calculate collision point and adjust puck's direction based on where it hits the paddle
		float collisionPoint = (puck.getY() + puck.getSize() / 2) - (paddleRect.y + paddleRect.height / 2);
		float maxBounceAngle = 75; // Max angle in degrees for puck to bounce off
		float bounceAngle = (collisionPoint / (paddleRect.height / 2)) * maxBounceAngle;
		bounceAngle = MathUtils.degreesToRadians * bounceAngle; // Convert to radians

		// Prepare to adjust speed
		float currentSpeed = puck.getVector().len();
		if (collisionCounter % 3 == 0 && currentSpeed < maxSpeed) {
			currentSpeed += speedIncrement; // Increase the speed after every three hits, up to max speed
		}

		// Adjust direction based on the bounce angle
		Vector2 newDirection = new Vector2(MathUtils.cos(bounceAngle), MathUtils.sin(bounceAngle)).nor(); // Normalize to get direction

		// Make sure the x-component is correctly inverted and apply the updated speed
		if (paddleRect.x == leftPaddleX) {
			newDirection.x = Math.abs(newDirection.x);
		} else {
			newDirection.x = -Math.abs(newDirection.x);
		}
		puck.setVector(newDirection.scl(currentSpeed)); // Set the new direction and speed

		// Adjust the puck's position slightly to avoid sticking to the paddle
		if (paddleRect.x == leftPaddleX) {
			puck.setX(paddleRect.x + paddleRect.width + 1); // Adjust position to just outside the paddle
		} else {
			puck.setX(paddleRect.x - puck.getSize() - 1); // Adjust position
		}
	}

	@Override
	public void dispose () {
		batch.dispose();
		paddleTexture.dispose();
		puckTexture.dispose();
		font.dispose();
	}

	@Override
	public void onPuckScored(int scoringSide) {
		if (scoringSide == -1) {
			game.scoreRightPlayer();
			resetBoard();
		} else if (scoringSide == 1) {
			game.scoreLeftPlayer();
			resetBoard();
		}
	}
}