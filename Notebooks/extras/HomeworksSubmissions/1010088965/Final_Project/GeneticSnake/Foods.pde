// All foods for all snakes training in a single gen.
class Foods {
  Food[] foods = new Food[population.gens.get(population.cg).ns];

  Foods() {
    for (int i = 0; i < foods.length; ++i) {
      foods[i] = new Food();
    }
  }


  void update() {
    Generation gen = population.gens.get(population.cg);
    for(int i = 0; i < foods.length; ++i) {
      foods[i].update(gen.snakes[i]); // Update position of all foods
      if(i == population.cshowfI && !gen.showingBestSnake) {
        foods[i].render(); // but only show the food of the snake being shown
      }
    }
  }

  void restart() {
    for (int i = 0; i < foods.length; ++i) {
      foods[i] = new Food();
    }
  }
}