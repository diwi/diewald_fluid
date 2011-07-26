//------------------------------------------------------------------------------
//
// author: thomas diewald
// date: 25.07.2011
//
// basic example to show how to setup a fluid direction 
// play around by making your own obstacles
// see the performance-difference of GPU / CPU by changing the variable "CPU_GPU"
//
// interaction:
//    LMB: set density white
//    MMB: set density red
//    RMB: set velocity
//
//    key 'y' + LMB: add obstacle
//    key 'y' + MMB: remove obstacle
//
//    key 's': to visualize the difference in smoothing
//    key 'r': reset
//    key ' ': (SPACE) to pause computation
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
int  fluid_size_y   = 100;
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

  for(int y = 5; y < height-5; y+=30){
    setDens(fluid, 1*cell_size, y, 1, 1,   .2f,.5f, 1);
  }
  setVel(fluid, 6*cell_size, 1*cell_size, 2, 400,   0.11f, 0);

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
}


