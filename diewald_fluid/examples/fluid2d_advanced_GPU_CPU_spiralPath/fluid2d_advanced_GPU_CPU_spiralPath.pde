//------------------------------------------------------------------------------
//
// author: thomas diewald
// date: 25.07.2011
//
// basic example to show how to make custom obstacles and add several density/velocity sources
// see the performance-difference of GPU / CPU by changing the variable "CPU_GPU"
//
// interaction:
//    LMB: set density white
//    MMB: set density red
//    RMB: set velocity
//
//    key 'y' + LMB: add obstacle
//    key 'y' + MMB: remove userdefined obstacle
//
//    key 's': to visualize the difference in smoothing
//    key 'r': reset
//    key ' ': (SPACE) to pause computation
//    key 'd': pressed: stop density emitters
//    key 'v': pressed: stop velocity emitters
//    key 'o': add/remove obstacle (defining the spiral)
//
//------------------------------------------------------------------------------


import processing.opengl.*;
import codeanticode.glgraphics.*;
import diewald_fluid.Fluid2D;
import diewald_fluid.Fluid2D_CPU;
import diewald_fluid.Fluid2D_GPU;

int  CPU_GPU        = 0; // 0 is GPU, 1 is CPU;
int  cell_size      = 6;
int  fluid_size_x   = 150;
int  fluid_size_y   = 150;
int  window_size_x  = fluid_size_x  * cell_size + (cell_size * 2);
int  window_size_y  = fluid_size_y  * cell_size + (cell_size * 2);


boolean edit_quader = false;

Fluid2D fluid;
PImage output_densityMap, input_density_values;




/////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void setup() {
  if ( CPU_GPU == 0 ){
    size(window_size_x, window_size_y, GLConstants.GLGRAPHICS);
  }
  if ( CPU_GPU == 1 ){
      size(window_size_x, window_size_y, JAVA2D);
  }

  //input_density_values = loadImage("mondrian_640x480.jpg");
  output_densityMap    = createImage(window_size_x, window_size_y, RGB);

  fluid = createFluid(CPU_GPU);
  reset(fluid);

  frameRate(60);
 
}






/////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void draw() {

  background(255);
  if ( mousePressed ) fluidInfluence(fluid);
  
  generateSpiralInfluence(fluid);
  

  fluid.smoothDensityMap(!( keyPressed && key == 's'));

  if ( !(keyPressed && key == ' ') ) 
    fluid.update();

  image(fluid.getDensityMap(), 0, 0);
  println(frameRate);
  
}



/////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void keyPressed() {
  if ( key == 'y') edit_quader = true;
}


public void keyReleased() {
  edit_quader = false;
  if ( key == 'r') reset(fluid);
  if ( key == 'o') {
    spiral_obstacles = !spiral_obstacles;
    generateSpiralObstacles(fluid, spiral_obstacles);
  }
}


