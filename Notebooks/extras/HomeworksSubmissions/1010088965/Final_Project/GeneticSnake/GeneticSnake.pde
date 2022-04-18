int scl = 32;
// int scl = 16;
int horsqrs = 36;
int versqrs = 27;
int panelWidth = 240;
int fps = 300;
int currentScore = 0;
float mutRate = 0.05;
String speedText = "20x";
World world;
Population population;
Controller controller;
Generation cGen;
Foods foods;
boolean gamePaused;
boolean renderingAll;
boolean hideKeys = false;

// Colors
int bgcol = color(29,30,58);
int gridcol = color(113,112,110);
int snakecol = color(0,204,102);
int foodcol = color(255,78,96);
int panelcol = 175;

void settings() {
  size(scl*horsqrs + 1 + panelWidth, scl*versqrs + 1);
}

void setup() {
  background(bgcol);
  // fullScreen();
  pushMatrix();
  // translate(50,6);
  gamePaused = false;
  frameRate(fps);
  population = new Population();
  world = new World();
  controller = new Controller();
  foods = new Foods();
  popMatrix();
  world.renderPanel();
}

// Game loop
void draw() {
  pushMatrix();
  // translate(50,6);
  if(!gamePaused) {
    frameRate(fps);
    world.render(); // Draw grid
    population.update(); // Move all snakes and update generation
    foods.update(); // Update positions of foods
  }
  popMatrix();
  world.renderPanel(); // Draw panel
}

// Restart everything
void restart() {
  pushMatrix();
  // translate(50,6);
  population = new Population();
  world = new World();
  controller = new Controller();
  foods = new Foods();
  popMatrix();
}

/* Buttons to interact, k: pause, j: desaccelerate, l: accelerate, 
   r: render all snakes or not, s: decrease mutRate,
   d: increase mutRate, f: hide or not panel with keys, q: restart */
void keyPressed() {
  if(key == 'k') {
    gamePaused = !gamePaused;
  }
  if(key == 'l') {
    switch (fps) {
      case 15 :
        fps = 30;
        speedText = "2x";
      break;
      case 30 :
        fps = 60;
        speedText = "4x";
      break;
      case 60 :
        fps = 150;
        speedText = "10x";
      break;	
      case 150 :
        fps = 300;
        speedText = "20x";
      break;	
      default :
      break;	
    }
  }
  if(key == 'j') {
    switch (fps) {
      case 300 :
        fps = 150;
        speedText = "10x";
      break;
      case 150 :
        fps = 60;
        speedText = "4x";
      break;
      case 60 :
        fps = 30;
        speedText = "2x";
      break;	
      case 30 :
        fps = 15;
        speedText = "1x";
      break;	
      default :
      break;	
    }
  }
  if(key == 's' && round(mutRate*100) > 0) {
    mutRate -= 0.01;
  }
  if(key == 'd' && round(mutRate*100) < 100) {
    mutRate += 0.01;
  }
  if(key == 'r') {
    renderingAll = !renderingAll;
  }
  if(key == 'f') {
    hideKeys = !hideKeys;
  }
  if(key == 'q') {
    restart();
  }
}