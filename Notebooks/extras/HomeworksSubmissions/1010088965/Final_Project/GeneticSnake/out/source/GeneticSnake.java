import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class GeneticSnake extends PApplet {

int scl = 32;
// int scl = 16;
int horsqrs = 36;
int versqrs = 27;
int panelWidth = 240;
int fps = 300;
int currentScore = 0;
float mutRate = 0.05f;
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

public void settings() {
  size(scl*horsqrs + 1 + panelWidth, scl*versqrs + 1);
}

public void setup() {
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
public void draw() {
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
public void restart() {
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
public void keyPressed() {
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
    mutRate -= 0.01f;
  }
  if(key == 'd' && round(mutRate*100) < 100) {
    mutRate += 0.01f;
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
class Controller {
  boolean isHuman = false;
  boolean pressedKey = false;

  // This chooses the direction of the snake based on the value outputted by the brain
  public void play(Snake snake) {
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
  public float[] frame(Snake snake) {
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

  public float[] lookInDirection(PVector direction, Snake cSnake) {

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
class Food {
  PVector pos = new PVector(floor(random(horsqrs))*scl, floor(random(versqrs))*scl);
  
  public void render() {
    fill(foodcol);
    noStroke();
    rect(pos.x + 1, pos.y + 1, scl - 1, scl - 1);
  }
  
  // If snake catches food, appear elsewhere
  public void update(Snake snake) {
    if(snake.wasInFoodPos()) {
      boolean match = true;
      while(match) {
        match = false;
        pos.x = floor(random(horsqrs))*scl; 
        pos.y = floor(random(versqrs))*scl;
        // Make sure new position is not inside the body of the snake
        for(int i = 0; i < snake.pos.length; i++) {
          if(pos.x == snake.pos[i].x && pos.y == snake.pos[i].y) {
            match = true;
          }      
        }
      }
    }
  }
}
// All foods for all snakes training in a single gen.
class Foods {
  Food[] foods = new Food[population.gens.get(population.cg).ns];

  Foods() {
    for (int i = 0; i < foods.length; ++i) {
      foods[i] = new Food();
    }
  }


  public void update() {
    Generation gen = population.gens.get(population.cg);
    for(int i = 0; i < foods.length; ++i) {
      foods[i].update(gen.snakes[i]); // Update position of all foods
      if(i == population.cshowfI && !gen.showingBestSnake) {
        foods[i].render(); // but only show the food of the snake being shown
      }
    }
  }

  public void restart() {
    for (int i = 0; i < foods.length; ++i) {
      foods[i] = new Food();
    }
  }
}
class Generation {
  boolean showingBestSnake = false;
  int ns = 2000; // Number of snakes
  Snake[] snakes = new Snake[ns];
  int cs; // Serpiente actual (current snake)

  Generation() {
    for (int i = 0; i < snakes.length; ++i) {
      snakes[i] = new Snake(i);
    }
    cs = 0;
  }
}
class Matrix {
  
  // Local variables
  int rows;
  int cols;
  float[][] matrix;
  

  // Constructor
  Matrix(int r, int c) {
    rows = r;
    cols = c;
    matrix = new float[rows][cols];
  }
  

  // Constructor from 2D array
  Matrix(float[][] m) {
    matrix = m;
    cols = m.length;
    rows = m[0].length;
  }
  

  // Print matrix
  public void output() {
    for (int i =0; i<rows; i++) {
      for (int j = 0; j<cols; j++) {
        print(matrix[i][j] + "  ");
      }
      println(" ");
    }
    println();
  }

  
  // Multiply by scalar
  public void multiply(float n ) {

    for (int i =0; i<rows; i++) {
      for (int j = 0; j<cols; j++) {
        matrix[i][j] *= n;
      }
    }
  }

  // Return a matrix which is this matrix dot product parameter matrix 
  public Matrix dot(Matrix n) {
    Matrix result = new Matrix(rows, n.cols);
   
    if (cols == n.rows) {
      //for each spot in the new matrix
      for (int i =0; i<rows; i++) {
        for (int j = 0; j<n.cols; j++) {
          float sum = 0;
          for (int k = 0; k<cols; k++) {
            sum+= matrix[i][k]*n.matrix[k][j];
          }
          result.matrix[i][j] = sum;
        }
      }
    }

    return result;
  }

  // Set the matrix to random ints between -1 and 1
  public void randomize() {
    for (int i =0; i<rows; i++) {
      for (int j = 0; j<cols; j++) {
        matrix[i][j] = random(-1, 1);
      }
    }
  }

  // Add a scalar to the matrix
  public void Add(float n ) {
    for (int i =0; i<rows; i++) {
      for (int j = 0; j<cols; j++) {
        matrix[i][j] += n;
      }
    }
  }

  /// Return a matrix which is this matrix + parameter matrix
  public Matrix add(Matrix n ) {
    Matrix newMatrix = new Matrix(rows, cols);
    if (cols == n.cols && rows == n.rows) {
      for (int i =0; i<rows; i++) {
        for (int j = 0; j<cols; j++) {
          newMatrix.matrix[i][j] = matrix[i][j] + n.matrix[i][j];
        }
      }
    }
    return newMatrix;
  }

  // Return a matrix which is this matrix - parameter matrix
  public Matrix subtract(Matrix n ) {
    Matrix newMatrix = new Matrix(cols, rows);
    if (cols == n.cols && rows == n.rows) {
      for (int i =0; i<rows; i++) {
        for (int j = 0; j<cols; j++) {
          newMatrix.matrix[i][j] = matrix[i][j] - n.matrix[i][j];
        }
      }
    }
    return newMatrix;
  }

  // Return a matrix which is this matrix * parameter matrix (element wise multiplication)
  public Matrix multiply(Matrix n ) {
    Matrix newMatrix = new Matrix(rows, cols);
    if (cols == n.cols && rows == n.rows) {
      for (int i =0; i<rows; i++) {
        for (int j = 0; j<cols; j++) {
          newMatrix.matrix[i][j] = matrix[i][j] * n.matrix[i][j];
        }
      }
    }
    return newMatrix;
  }

  // Return a matrix which is the transpose of this matrix
  public Matrix transpose() {
    Matrix n = new Matrix(cols, rows);
    for (int i =0; i<rows; i++) {
      for (int j = 0; j<cols; j++) {
        n.matrix[j][i] = matrix[i][j];
      }
    }
    return n;
  }

  // Creates a single column array from the parameter array
  public Matrix singleColumnMatrixFromArray(float[] arr) {
    Matrix n = new Matrix(arr.length, 1);
    for (int i = 0; i< arr.length; i++) {
      n.matrix[i][0] = arr[i];
    }
    return n;
  }

  // Sets this matrix from an array
  public void fromArray(float[] arr) {
    for (int i = 0; i< rows; i++) {
      for (int j = 0; j< cols; j++) {
        matrix[i][j] =  arr[j+i*cols];
      }
    }
  }
  
  // Returns an array which represents this matrix
  public float[] toArray() {
    float[] arr = new float[rows*cols];
    for (int i = 0; i< rows; i++) {
      for (int j = 0; j< cols; j++) {
        arr[j+i*cols] = matrix[i][j];
      }
    }
    return arr;
  }

  // For ix1 matrixes adds one to the bottom
  public Matrix addBias() {
    Matrix n = new Matrix(rows+1, 1);
    for (int i = 0; i<rows; i++) {
      n.matrix[i][0] = matrix[i][0];
    }
    n.matrix[rows][0] = 1;
    return n;
  }

  // Applies the activation function(sigmoid) to each element of the matrix
  public Matrix activate() {
    Matrix n = new Matrix(rows, cols);
    for (int i =0; i<rows; i++) {
      for (int j = 0; j<cols; j++) {
        n.matrix[i][j] = sigmoid(matrix[i][j]);
      }
    }
    return n;
  }

  
  // Sigmoid activation function
  public float sigmoid(float x) {
    float y = 1 / (1 + pow((float)Math.E, -x));
    return y;
  }
  // Returns the matrix that is the derived sigmoid function of the current matrix
  public Matrix sigmoidDerived() {
    Matrix n = new Matrix(rows, cols);
    for (int i =0; i<rows; i++) {
      for (int j = 0; j<cols; j++) {
        n.matrix[i][j] = (matrix[i][j] * (1- matrix[i][j]));
      }
    }
    return n;
  }

  // Returns the matrix which is this matrix with the bottom layer removed
  public Matrix removeBottomLayer() {
    Matrix n = new Matrix(rows-1, cols);      
    for (int i =0; i<n.rows; i++) {
      for (int j = 0; j<cols; j++) {
        n.matrix[i][j] = matrix[i][j];
      }
    }
    return n;
  }

  //  Mutation function for genetic algorithm 
  
  public void mutate(float mutationRate) {
    
    // For each element in the matrix
    for (int i =0; i<rows; i++) {
      for (int j = 0; j<cols; j++) {
        float rand = random(1);
        if (rand<mutationRate) { // If chosen to be mutated
          matrix[i][j] += randomGaussian()/5; // Add a random value to it(can be negative)
          
          // Set the boundaries to 1 and -1
          if (matrix[i][j]>1) {
            matrix[i][j] = 1;
          }
          if (matrix[i][j] <-1) {
            matrix[i][j] = -1;
          }
        }
      }
    }
  }

  // Returns a matrix which has a random number of values from this matrix and the rest from the parameter matrix
  public Matrix crossover(Matrix partner) {
    Matrix child = new Matrix(rows, cols);
    
    // Pick a random point in the matrix
    int randC = floor(random(cols));
    int randR = floor(random(rows));
    for (int i =0; i<rows; i++) {
      for (int j = 0; j<cols; j++) {

        if ((i< randR)|| (i==randR && j<=randC)) { // If before the random point then copy from this matric
          child.matrix[i][j] = matrix[i][j];
        } else { // If after the random point then copy from the parameter array
          child.matrix[i][j] = partner.matrix[i][j];
        }
      }
    }
    return child;
  }

  // Return a copy of this matrix
  public Matrix clone() {
    Matrix clone = new  Matrix(rows, cols);
    for (int i =0; i<rows; i++) {
      for (int j = 0; j<cols; j++) {
        clone.matrix[i][j] = matrix[i][j];
      }
    }
    return clone;
  }
}
class NeuralNet {

  int iNodes; // No. of input nodes
  int hNodes; // No. of hidden nodes
  int oNodes; // No. of output nodes

  Matrix whi; // matrix containing weights between the input nodes and the hidden nodes
  Matrix whh; // matrix containing weights between the hidden nodes and the second layer hidden nodes
  Matrix woh; // matrix containing weights between the second hidden layer nodes and the output nodes  

  // constructor
  NeuralNet(int inputs, int hiddenNo, int outputNo) {

    // set dimensions from parameters
    iNodes = inputs;
    oNodes = outputNo;
    hNodes = hiddenNo;


    // create first layer weights 
    // included bias weight
    whi = new Matrix(hNodes, iNodes +1);

    // create second layer weights
    // include bias weight
    whh = new Matrix(hNodes, hNodes +1);

    // create second layer weights
    // include bias weight
    woh = new Matrix(oNodes, hNodes +1);  

    // set the matricies to random values
    whi.randomize();
    whh.randomize();
    woh.randomize();
  }  

  // mutation function for genetic algorithm
  public void mutate(float mr) {
    // mutates each weight matrix
    whi.mutate(mr);
    whh.mutate(mr);
    woh.mutate(mr);
  }
  
  // Calculate the output values by feeding forward through the neural network
  public float[] output(float[] inputsArr) {

    // First layer    
    Matrix inputs = woh.singleColumnMatrixFromArray(inputsArr); // convert array to matrix
    Matrix inputsBias = inputs.addBias(); // add bias 

    // Second layer
    Matrix hiddenInputs = whi.dot(inputsBias); // apply layer one weights to the inputs
    Matrix hiddenOutputs = hiddenInputs.activate(); // pass through activation function(sigmoid)
    Matrix hiddenOutputsBias = hiddenOutputs.addBias(); // add bias

    // Third layer
    Matrix hiddenInputs2 = whh.dot(hiddenOutputsBias); //apply layer two weights
    Matrix hiddenOutputs2 = hiddenInputs2.activate();
    Matrix hiddenOutputsBias2 = hiddenOutputs2.addBias();

    // Last layer
    Matrix outputInputs = woh.dot(hiddenOutputsBias2); //apply level three weights
    Matrix outputs = outputInputs.activate(); //pass through activation function(sigmoid)

    return outputs.toArray(); //convert to an array and return
  }  
  // Crossover function for genetic algorithm
  public NeuralNet crossover(NeuralNet partner) {

    // Creates a new child with layer matrices from both parents
    NeuralNet child = new NeuralNet(iNodes, hNodes, oNodes);
    child.whi = whi.crossover(partner.whi);
    child.whh = whh.crossover(partner.whh);
    child.woh = woh.crossover(partner.woh);
    return child;
  }  
  // Return a neural net which is a clone of this Neural net
  public NeuralNet clone() {
    NeuralNet clone  = new NeuralNet(iNodes, hNodes, oNodes); 
    clone.whi = whi.clone();
    clone.whh = whh.clone();
    clone.woh = woh.clone();

    return clone;
  }  
  // Converts the weights matrices to a single table 
  // Used for storing the snakes brain in a file
  public Table NetToTable() {

    // Create table
    Table t = new Table();


    // Convert the matrices to an array 
    float[] whiArr = whi.toArray();
    float[] whhArr = whh.toArray();
    float[] wohArr = woh.toArray();

    // Set the amount of columns in the table
    for (int i = 0; i< max(whiArr.length, whhArr.length, wohArr.length); i++) {
      t.addColumn();
    }

    // Set the first row as whi
    TableRow tr = t.addRow();

    for (int i = 0; i< whiArr.length; i++) {
      tr.setFloat(i, whiArr[i]);
    }


    // Set the second row as whh
    tr = t.addRow();

    for (int i = 0; i< whhArr.length; i++) {
      tr.setFloat(i, whhArr[i]);
    }

    // Set the third row as woh
    tr = t.addRow();

    for (int i = 0; i< wohArr.length; i++) {
      tr.setFloat(i, wohArr[i]);
    }

    //return table
    return t;
  }
  
  // Takes in table as parameter and overwrites the matrices data for this neural network
  // Used to load snakes from file
  public void TableToNet(Table t) {

    // Create arrays to tempurarily store the data for each matrix
    float[] whiArr = new float[whi.rows * whi.cols];
    float[] whhArr = new float[whh.rows * whh.cols];
    float[] wohArr = new float[woh.rows * woh.cols];

    // Set the whi array as the first row of the table
    TableRow tr = t.getRow(0);

    for (int i = 0; i< whiArr.length; i++) {
      whiArr[i] = tr.getFloat(i);
    }


    // Set the whh array as the second row of the table
    tr = t.getRow(1);

    for (int i = 0; i< whhArr.length; i++) {
      whhArr[i] = tr.getFloat(i);
    }

    // Set the woh array as the third row of the table

    tr = t.getRow(2);

    for (int i = 0; i< wohArr.length; i++) {
      wohArr[i] = tr.getFloat(i);
    }


    // Convert the arrays to matrices and set them as the layer matrices 
    whi.fromArray(whiArr);
    whh.fromArray(whhArr);
    woh.fromArray(wohArr);
  }
}
class Population {
  ArrayList<Generation> gens;
  int cg;
  int cshowfI;
  Snake showoff; // showoff es the best snake of the group being shown
  Food showoffFood;
  long lastAvgFitness = 0;
  int snakesRemaining = 0;

  public Population() {
    gens = new ArrayList<Generation>();
    gens.add(new Generation());
    cg = 0;
  }

  // From here all snakes are moved
  public void update() {
    boolean showingOneSelected = false;
    boolean oneAlive = false;
    Generation gen = gens.get(cg); // Obtain number of current gen.

    snakesRemaining = 0; // Snakes remaining
    
    // Loop of all alive snakes
    for (int i = 0; i < gen.snakes.length; ++i) {
      if(gen.snakes[i].alive) { // If the snake is still alive...
        
        snakesRemaining += 1;
        oneAlive = true;
        gen.snakes[i].update(); // ...update the position of the snake
        if(renderingAll) { // and if it's in mode Render all snakes...
          gen.snakes[i].render(); // ...render each one
          currentScore = 0;
        } else { // And only if one is being shown...
          if(!showingOneSelected) { // ...show the first one found alive
            showingOneSelected = true;
            cshowfI = i;
            gen.snakes[i].render();
            currentScore = gen.snakes[i].pos.length - 1;
          }
        }
        controller.play(gen.snakes[i]); // Here the AI is used to pick the movement of each snake
      }
    }

    // Logic used to show the best snake
    if(!oneAlive && !gens.get(cg).showingBestSnake) {
      chooseBestSnakeAndGetAvgFitness();
    }
    if(!oneAlive && gens.get(cg).showingBestSnake) {
      playBestSnake();
    }
  }

  // Here the best snake is picked and the avg. fitness of the generation is updated
  public void chooseBestSnakeAndGetAvgFitness() {
    showoff = new Snake(-1);
    showoffFood = new Food();
    double bestFitness = 0;
    lastAvgFitness = 0;
    for (Snake snake : gens.get(cg).snakes) {
      lastAvgFitness += snake.fitness;
      if(snake.fitness > bestFitness) {
        bestFitness = snake.fitness;
        showoff.brain = snake.brain.clone();
      }
    }
    lastAvgFitness = lastAvgFitness/gens.get(cg).ns;
    gens.get(cg).showingBestSnake = true;
  }

  // The best snake is played
  public void playBestSnake() {
    showoff.update();
    showoff.render();
    currentScore = showoff.pos.length - 1;
    controller.play(showoff);
    // When it dies, switch generation
    if(!showoff.alive) {
      changeGen();
    }
    showoffFood.update(showoff);
    showoffFood.render();
  }

  // This is the generation switch. Here the genetic algo. is executed
  public void changeGen() {
    foods.restart();
    Generation oldGen = gens.get(cg);
    float totFitness = 0;

    for(Snake snake : oldGen.snakes) {
      totFitness = totFitness + snake.fitness; // Sum of fitnesses
    }
    // println(totFitness);

    cg = cg + 1; // Increase gen number
    // Add new gen. to array. This creates the snakes with random brains
    gens.add(new Generation());

    Generation newGen = gens.get(cg);
    // Iterate over the snakes of new gen
    for(Snake childSnake : newGen.snakes) {

      // Pick first parent
      Snake firstParent = new Snake(0);
      float randomi = random(totFitness);
      float fitnessCount = 0;
      // Probability algorithm
      for(Snake snake : oldGen.snakes) {
        fitnessCount = fitnessCount + snake.fitness;
        if(randomi < fitnessCount) {
          firstParent = snake;
          break;
        }
      }

      // Pick second parent
      Snake secondParent = new Snake(0);
      randomi = random(totFitness);
      fitnessCount = 0;
      // Probability algorithm
      for(Snake snake : oldGen.snakes) {
        fitnessCount = fitnessCount + snake.fitness;
        if(randomi < fitnessCount) {
          secondParent = snake;
          break;
        }
      }
      
      childSnake.brain = firstParent.brain.crossover(secondParent.brain); // Crossover brains from parents
      childSnake.brain.mutate(mutRate); // Mutate a little, for variation
    }

    gens.set(cg, newGen); // set new generation, already being created
    gens.set(cg - 1, null); // remove previous generation to free memory
  }
}
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
  public void startVel() {
    float rel = random(4); //randomVel = rel
    vel = new PVector(0, 0);
    if(rel<1){vel.x=-1;vel.y=0;}else if(rel>=1&&rel<2){vel.x=1;vel.y=0;}else if(rel>=2&&rel<3){vel.x=0;vel.y=-1;}else{vel.x=0;vel.y=1;}
    vel.mult(scl);
  }

  public void update() {
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

  public void render() {
    for(int i = 0; i < pos.length; i++) {
      square(pos[i].x, pos[i].y); // Draw all squares
    }
  }

  // Move snake
  public void move() {
    PVector previous = prevHead.get();
    PVector previousCopy = prevHead.get(); 
    for(int i = 1; i < pos.length; i++) {
      previous = pos[i];
      pos[i] = previousCopy;
      previousCopy = previous;
    }
  }

  // Check if snake was in food position
  public boolean wasInFoodPos() {
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
  public void plusOne() {
    sinceFood = 0;
    if(pos.length == 1) {
      pos = (PVector[])append(pos, new PVector(prevHead.x, prevHead.y));
    } else {
      pos = (PVector[])append(pos, new PVector(pos[pos.length - 1].x, pos[pos.length - 1].y));
    }
  }
  
  // Die and calculate fitness
  public void died() {
    alive = false;
    justDied = true;
    if(pos.length < 10) { // pos.length is snake's size
      fitness = lifetime*lifetime*floor(pow(2, pos.length)); // fitness function 1
    } else {
      fitness = lifetime*lifetime*floor(pow(2, 10))*(pos.length - 9); // fitness function 2
    }

    pos[0].sub(vel);
  }
  
  public boolean justDied() {
    if(justDied) {
      justDied = false;
      return true;
    }
    return false;
  }

  // Check if it hit the body
  public boolean collidedBody(float x, float y) {
    for(int i = 2; i < pos.length; i++) {
      if(x == pos[i].x && y == pos[i].y) {
        return true;
      }
    }
    return false;
  }

  // Draw square in coords. x and y
  public void square(float x, float y) {
    noStroke();
    fill(snakecol);
    rect(x + 1, y + 1, scl - 1, scl - 1);
  }
}
class World {
  World() {
    render();
  }
  
  // Draw regular grid
  public void render() {
    fill(bgcol);
    noStroke();
    rect(0,0,width - panelWidth - 1, height);
    for(int i = 0; i < horsqrs + 1; i++) {
      stroke(gridcol);
      line(scl*i, 0, scl*i, versqrs*scl); 
    }
    for(int i = 0; i < versqrs + 1; i++) {
      stroke(gridcol);
      line(0, scl*i, horsqrs*scl, scl*i); 
    }
  }

  // Check is x and y are within limits
  public boolean isInsideBoundaries(float x, float y) {
    if(x >= scl*horsqrs || x < 0 || y >= scl*versqrs || y < 0) {
      return false;
    }
    return true;
  }
  
  // Show right panel
  public void renderPanel() {
    pushMatrix();
    translate(width - panelWidth, 0);
    stroke(175);
    fill(panelcol);
    rect(0, 0, panelWidth, height);
    textSize(20);
    textAlign(LEFT, CENTER);
    fill(30);
    text("Generation: " + (population.cg + 1), 20, 20);
    text("Avg. fit.: " + population.lastAvgFitness, 20, 60);
    text("Snakes in gen.: " + population.snakesRemaining, 20, 100);
    if(population.gens.get(population.cg).showingBestSnake) {
      text("Showing best snake", 20, 140);
    } else {
      text("Training", 20, 140);
    }
    text("Score: " + currentScore, 20, 180);
    text("Speed: " + speedText, 20, 220);
    text("Mut. rate: " + round(mutRate*100) + "%", 20, 260);
    if(!hideKeys) {
      text("J-K-L: vary speed", 20, 340);
      text("S-D: vary mut. rate", 20, 370);
      text("R: render all snakes", 20, 400);
    }

    popMatrix();
  } 
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "GeneticSnake" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
