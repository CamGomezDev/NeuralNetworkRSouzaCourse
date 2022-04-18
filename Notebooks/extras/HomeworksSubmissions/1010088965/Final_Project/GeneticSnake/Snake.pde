public class Snake {
  NeuralNet brain = new NeuralNet(24, 24, 4); // Brain is NeuralNet
  int index;
  int lifetime = 0;
  int incBy = 0;
  float fitness = 0;
  int sinceFood = 0;
  float probs = 0;
  boolean alive = true;
  boolean justAte = false;
  boolean justDied = false;
  boolean isShowoff = false;

  PVector[] pos = new PVector[1];
  PVector prevHead = new PVector(0, 0);
  PVector vel;

  public Snake (int indexi) {
    if(indexi == -1) {
      isShowoff = true; // Simple identifier, in case this is the best snake to show
    }
    index = indexi;
    startVel();
    
    pos[0] = new PVector(scl*floor(horsqrs/2), scl*floor(versqrs/2));
    square(pos[0].x, pos[0].y);
  }

  // Initialize random speed in random direction
  void startVel() {
    float rel = random(4); //randomVel = rel
    vel = new PVector(0, 0);
    if(rel<1){vel.x=-1;vel.y=0;}else if(rel>=1&&rel<2){vel.x=1;vel.y=0;}else if(rel>=2&&rel<3){vel.x=0;vel.y=-1;}else{vel.x=0;vel.y=1;}
    vel.mult(scl);
  }

  void update() {
    lifetime = lifetime + 1;
    prevHead = pos[0].get();
    pos[0].add(vel);
    if(wasInFoodPos()) { // If it just ate...
      plusOne(); // increase size
    }
    if(!world.isInsideBoundaries(pos[0].x, pos[0].y)) {
      died();
    } else {
      move();
      if(collidedBody(pos[0].x, pos[0].y)) {
        died();
      }
    }
    sinceFood = sinceFood + 1;
    if(pos.length < 3 && sinceFood > 120) { // Die if it has gone 120 frames without eating and its size is lesser than 3
      died();
    } else if(pos.length >= 3 && sinceFood > 300) { // Die if it has gone 300 frames without eating and its size is larger or equal than 3
      died();
    }
  }

  void render() {
    for(int i = 0; i < pos.length; i++) {
      square(pos[i].x, pos[i].y); // Draw all squares
    }
  }

  // Move snake
  void move() {
    PVector previous = prevHead.get();
    PVector previousCopy = prevHead.get(); 
    for(int i = 1; i < pos.length; i++) {
      previous = pos[i];
      pos[i] = previousCopy;
      previousCopy = previous;
    }
  }

  // Check if snake was in food position
  boolean wasInFoodPos() {
    // Remember showoff is the best snake of the group being shown
    if(!isShowoff) {
      if(pos[0].x == foods.foods[index].pos.x && pos[0].y == foods.foods[index].pos.y) {
        return true;
      }
    } else {
      if(pos[0].x == population.showoffFood.pos.x && pos[0].y == population.showoffFood.pos.y) {
        return true;
      }
    }
    return false;
  }

  // Increase size of snake
  void plusOne() {
    sinceFood = 0;
    if(pos.length == 1) {
      pos = (PVector[])append(pos, new PVector(prevHead.x, prevHead.y));
    } else {
      pos = (PVector[])append(pos, new PVector(pos[pos.length - 1].x, pos[pos.length - 1].y));
    }
  }
  
  // Die and calculate fitness
  void died() {
    alive = false;
    justDied = true;
    if(pos.length < 10) { // pos.length is snake's size
      fitness = lifetime*lifetime*floor(pow(2, pos.length)); // fitness function 1
    } else {
      fitness = lifetime*lifetime*floor(pow(2, 10))*(pos.length - 9); // fitness function 2
    }

    pos[0].sub(vel);
  }
  
  boolean justDied() {
    if(justDied) {
      justDied = false;
      return true;
    }
    return false;
  }

  // Check if it hit the body
  boolean collidedBody(float x, float y) {
    for(int i = 2; i < pos.length; i++) {
      if(x == pos[i].x && y == pos[i].y) {
        return true;
      }
    }
    return false;
  }

  // Draw square in coords. x and y
  void square(float x, float y) {
    noStroke();
    fill(snakecol);
    rect(x + 1, y + 1, scl - 1, scl - 1);
  }
}
