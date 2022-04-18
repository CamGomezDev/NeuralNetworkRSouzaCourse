class Controller {
  boolean isHuman = false;
  boolean pressedKey = false;

  // This chooses the direction of the snake based on the value outputted by the brain
  void play(Snake snake) {
    float[] moves = snake.brain.output(frame(snake));

    // The max value and its corresponding index is chosen
    float max = moves[0];
    int indexOfMax = 0;
    for(int i = 1; i < moves.length; i++) {
      if(moves[i] > max) {
        max = moves[i];
        indexOfMax = i;
      }
    }
    
    // The index is used to choose direction of snake

    // Upwards
    if (indexOfMax == 1) {
      /* The double if is for the snake to be able to go backwards and die hitting 
         its own body if it has size lesser than 2, so it learns to avoid it. */
      if(snake.pos.length > 2) {
        snake.vel.x = 0;
        snake.vel.y = -scl;
      } else {
        if(snake.vel.y != scl) { // If it's not moving downwards...
          snake.vel.x = 0;
          snake.vel.y = -scl; // go upwards
        }
      }
    // Downwards
    } else if (indexOfMax == 3) {
      if(snake.pos.length > 2) {
        snake.vel.x = 0;
        snake.vel.y = scl;
      } else {
        if(snake.vel.y != -scl) {
          snake.vel.x = 0;
          snake.vel.y = scl;
        }
      }
    // Left
    } else if (indexOfMax == 2) {
      if(snake.pos.length > 2) {
        snake.vel.x = -scl;
        snake.vel.y = 0;
      } else {
        if(snake.vel.x != scl) {
          snake.vel.x = -scl;
          snake.vel.y = 0;
        }
      }
    // Right
    } else if (indexOfMax == 0) {
      if(snake.pos.length > 2) {
        snake.vel.x = scl;
        snake.vel.y = 0;
      } else {
        if(snake.vel.x != -scl) {
          snake.vel.x = scl;
          snake.vel.y = 0;
        }
      }
    }
  }

  // Calculate distance in the eight directions of the snake
  float[] frame(Snake snake) {
    float[] vision = new float[24];
    // Left
    float[] tempValues = lookInDirection(new PVector(-scl, 0), snake);
    vision[0] = tempValues[0];
    vision[1] = tempValues[1];
    vision[2] = tempValues[2];
    // Left/Up
    tempValues = lookInDirection(new PVector(-scl, -scl), snake);
    vision[3] = tempValues[0];
    vision[4] = tempValues[1];
    vision[5] = tempValues[2];
    // Up
    tempValues = lookInDirection(new PVector(0, -scl), snake);
    vision[6] = tempValues[0];
    vision[7] = tempValues[1];
    vision[8] = tempValues[2];
    // Up/Left
    tempValues = lookInDirection(new PVector(scl, -scl), snake);
    vision[9] = tempValues[0];
    vision[10] = tempValues[1];
    vision[11] = tempValues[2];
    // Right
    tempValues = lookInDirection(new PVector(scl, 0), snake);
    vision[12] = tempValues[0];
    vision[13] = tempValues[1];
    vision[14] = tempValues[2];
    // Right/Down
    tempValues = lookInDirection(new PVector(scl, scl), snake);
    vision[15] = tempValues[0];
    vision[16] = tempValues[1];
    vision[17] = tempValues[2];
    // Down
    tempValues = lookInDirection(new PVector(0, scl), snake);
    vision[18] = tempValues[0];
    vision[19] = tempValues[1];
    vision[20] = tempValues[2];
    // Down/Left
    tempValues = lookInDirection(new PVector(-scl, scl), snake);
    vision[21] = tempValues[0];
    vision[22] = tempValues[1];
    vision[23] = tempValues[2];

    return vision;
  }

  float[] lookInDirection(PVector direction, Snake cSnake) {

    float[] visionInDirection = new float[3];

    PVector position = new PVector(cSnake.pos[0].x, cSnake.pos[0].y); // Initial pos
    boolean foodIsFound = false;
    boolean tailIsFound = false;
    float distance = 0;
    
    // Move in desired direction before starting
    position.add(direction);
    distance +=1;

    // Look in that direction until it hits a wall
    while (!(position.x < 0 || position.y < 0 || position.x >= horsqrs*scl || position.y >= versqrs*scl)) {

      // Look for food
      if(cSnake.index == -1) {
        if (!foodIsFound && position.x == population.showoffFood.pos.x && position.y == population.showoffFood.pos.y) {
          visionInDirection[0] = 1;
          foodIsFound = true;
        }
      } else {
        if (!foodIsFound && position.x == foods.foods[cSnake.index].pos.x && position.y == foods.foods[cSnake.index].pos.y) {
          visionInDirection[0] = 1;
          foodIsFound = true;
        }
      }

      // Look for tail
      if (!tailIsFound && cSnake.collidedBody(position.x, position.y)) {
        visionInDirection[1] = 1/distance;
        tailIsFound = true;
      }

      // Keep moving in direction
      position.add(direction);
      distance +=1;
    }

    // Distance to wall
    visionInDirection[2] = 1/distance;

    return visionInDirection;
  }
}
