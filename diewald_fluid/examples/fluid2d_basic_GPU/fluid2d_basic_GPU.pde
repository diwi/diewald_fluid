//------------------------------------------------------------------------------
//
// author: thomas diewald
// date: 25.07.2011
//
// basic example to show how to initialize a GPU-based fluidsolver
//
// interaction:
//    LMB: set density
//    RMB: set velocity
//
//------------------------------------------------------------------------------

import processing.opengl.*;
import codeanticode.glgraphics.*;
import diewald_fluid.Fluid2D;
import diewald_fluid.Fluid2D_CPU;
import diewald_fluid.Fluid2D_GPU;


int  fluid_size_x = 200; 
int  fluid_size_y = 200;

int  cell_size    = 3;
int  window_size_x = fluid_size_x  * cell_size + (cell_size * 2);
int  window_size_y = fluid_size_y  * cell_size + (cell_size * 2);

Fluid2D fluid;


/////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void setup() {
  size(window_size_x, window_size_y, GLConstants.GLGRAPHICS); // P2D is not working, don't know why

  fluid = createFluidSolver();
  frameRate(60);
}



/////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void draw() {
  background(255);
  if ( mousePressed ) 
    fluidInfluence(fluid);
    
  fluid.update();
  image(fluid.getDensityMap(), 0, 0, width, height);
  println(frameRate);
}


/////////////////////////////////////////////////////////////////////////////////////////////////////////////
// createFluidSolver();
//
Fluid2D createFluidSolver() {
  Fluid2D fluid_tmp = new Fluid2D_GPU(this, fluid_size_x, fluid_size_y); // initialize de solver

  fluid_tmp.setParam_Timestep  ( 0.10f );
  fluid_tmp.setParam_Iterations( 16 );
  fluid_tmp.setParam_IterationsDiffuse(1);
  fluid_tmp.setParam_Viscosity ( 0.000001f );
  fluid_tmp.setParam_Diffusion ( 0.00000001f );
  fluid_tmp.setParam_Vorticity ( 1.0f );
  fluid_tmp.processDensityMap  ( true );
  fluid_tmp.processDiffusion   ( true );
  fluid_tmp.processViscosity   ( true );
  fluid_tmp.processVorticity   ( true );
  fluid_tmp.processDensityMap  ( true );
  fluid_tmp.setObjectsColor    (1, 1, 1, 1); 
  return fluid_tmp;
}

