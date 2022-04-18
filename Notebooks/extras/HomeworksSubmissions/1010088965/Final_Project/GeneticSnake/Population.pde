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
  void update() {
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
  void chooseBestSnakeAndGetAvgFitness() {
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
  void playBestSnake() {
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
  void changeGen() {
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
