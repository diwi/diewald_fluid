//------------------------------------------------------------------------------
//
// author: thomas diewald
// date: 25.07.2011
//
// basic example to show how to initialize a GPU-based fluidsolver
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
//
//------------------------------------------------------------------------------

import processing.opengl.*;
import codeanticode.glgraphics.*;
import diewald_fluid.Fluid2D;
import diewald_fluid.Fluid2D_CPU;
import diewald_fluid.Fluid2D_GPU;

int  CPU_GPU        = 1; // 0 is GPU, 1 is CPU;
int  fluid_size_x = 100; 
int  fluid_size_y = 100;

int  cell_size    = 6;
int  window_size_x = fluid_size_x  * cell_size + (cell_size * 2);
int  window_size_y = fluid_size_y  * cell_size + (cell_size * 2);

Fluid2D fluid;
PImage output_densityMap;
boolean edit_quader = false;
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void setup() {
  if ( CPU_GPU == 0 ){
    size(window_size_x, window_size_y, GLConstants.GLGRAPHICS);
  }
  if ( CPU_GPU == 1 ){
    size(window_size_x, window_size_y, JAVA2D);
  }

  fluid = createFluidSolver(CPU_GPU);
  frameRate(60);
}



/////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void draw() {
  background(255);
  if ( mousePressed ) 
    fluidInfluence(fluid);
  float speed = .15f;
  setVel (fluid, 10, 10, 1, 1, speed, speed);
  setDens(fluid, 10, 10, 3, 3, 1, 0, 0);
  
  setVel (fluid, width-10, cell_size*4, 2, 2, -speed, speed);
  setDens(fluid, width-10, cell_size*4, 3, 3, 1, 1, 1);
  
  setVel (fluid, width-10, height-10, 2, 2, -speed, -speed);
  setDens(fluid, width-10, height-10, 3, 3, 0, 0, 1);
  
  setVel (fluid, 10, height-10, 2, 2, speed, -speed);
  setDens(fluid, 10, height-10, 3, 3, 0, 1, 0);
  
  fluid.smoothDensityMap(( keyPressed && key == 's'));
  
  fluid.update();
  image(fluid.getDensityMap(), 0, 0, width, height);
  println(frameRate);
}


/////////////////////////////////////////////////////////////////////////////////////////////////////////////
// createFluidSolver();
//
Fluid2D createFluidSolver(int type) {
  Fluid2D fluid_tmp = null;
  
  if ( type == 0) fluid_tmp = new Fluid2D_GPU(this, fluid_size_x, fluid_size_y);
  if ( type == 1) fluid_tmp = new Fluid2D_CPU(this, fluid_size_x, fluid_size_y);

  fluid_tmp.setParam_Timestep  ( 0.10f );
  fluid_tmp.setParam_Iterations( 16 );
  fluid_tmp.setParam_IterationsDiffuse(1);
  fluid_tmp.setParam_Viscosity ( 0.00000001f );
  fluid_tmp.setParam_Diffusion ( 0.0000000001f );
  fluid_tmp.setParam_Vorticity ( 2.0f );
  fluid_tmp.processDensityMap  ( true );
  fluid_tmp.processDiffusion   ( true );
  fluid_tmp.processViscosity   ( true );
  fluid_tmp.processVorticity   ( true );
  fluid_tmp.processDensityMap  ( true );
  fluid_tmp.setObjectsColor    (1, 1, 1, 1); 
  
  output_densityMap    = createImage(window_size_x, window_size_y, RGB);
  fluid_tmp.setDensityMap(output_densityMap);
  return fluid_tmp;
}

