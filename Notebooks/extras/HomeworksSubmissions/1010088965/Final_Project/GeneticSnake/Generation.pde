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
