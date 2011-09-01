//------------------------------------------------------------------------------
//
// author: thomas diewald
// date: 27.07.2011
//
// basic fluid examples with gui controlls
//
// interaction: see GUI on right side of application
// 
//------------------------------------------------------------------------------

//import processing.opengl.*;
//import codeanticode.glgraphics.*;

import diewald.p5gui.*;
import diewald.p5gui.constants.*;
import diewald.p5gui.utilities.Color;

import diewald_fluid.Fluid2D;
import diewald_fluid.Fluid2D_CPU;
import diewald_fluid.Fluid2D_GPU;

//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
int  CPU_GPU        = 1; // 0 is GPU, 1 is CPU;
//------------------------------------------------------------------------------
//------------------------------------------------------------------------------


int  fluid_size_x = 150; 
int  fluid_size_y = 150;

int  cell_size    = 4;
int  window_size_x = fluid_size_x  * cell_size + (cell_size * 2);
int  window_size_y = fluid_size_y  * cell_size + (cell_size * 2);


int gui_size_x = 300;
int gui_size_y = window_size_y;
int gui_pos_x  = window_size_x;
int gui_pos_y  = 0;

Fluid2D fluid;
PImage output_densityMap;



GUI_CLASS my_gui;
GUI_Event gui_event_;
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void setup() {
  size(900, 600, JAVA2D); // for applet export
//  if ( CPU_GPU == 0 ) size(window_size_x + gui_size_x, window_size_y, GLConstants.GLGRAPHICS);
//  if ( CPU_GPU == 1 ) size(window_size_x + gui_size_x, window_size_y, JAVA2D);
  

  fluid = createFluidSolver(CPU_GPU);

  initGUI();
  frameRate(60);
}



/////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void draw() {
  background(0);
  
  
  
  // mouse influence
  if ( mousePressed && !my_gui.cb_pause.Status().isActive()) 
    fluidInfluence(fluid);
    
    
    
  // emitters on corners
  float speed = .25f;
  int off = 15;
  if( my_gui.cb_emitter1.Status().isActive() ){
    setVel (fluid,               off,               10, 2, 2, speed, speed);
    setDens(fluid,               off,               10, 3, 3, 1, 0, 0);
  }
  if( my_gui.cb_emitter2.Status().isActive() ){
    setVel (fluid, window_size_x-off,               10, 2, 2, -speed, speed);
    setDens(fluid, window_size_x-off,               10, 3, 3, 1, 1, 1);
  }
  if( my_gui.cb_emitter3.Status().isActive() ){
    setVel (fluid, window_size_x-off, window_size_y-10, 2, 2, -speed, -speed);
    setDens(fluid, window_size_x-off, window_size_y-10, 3, 3, 0, 0, 1);
  }
  if( my_gui.cb_emitter4.Status().isActive() ){
    setVel (fluid,               off, window_size_y-10, 2, 2, speed, -speed);
    setDens(fluid,               off, window_size_y-10, 3, 3, 0, 1, 0);
  }
  


  // fluid parameters
  fluid.setParam_Timestep          (       my_gui.sl_timestep   .getValue() *.005 );
  fluid.setParam_Iterations        ( (int) my_gui.sl_iterations1.getValue() );
  fluid.setParam_IterationsDiffuse ( (int) my_gui.sl_iterations2.getValue() );
  fluid.setParam_Viscosity         (       my_gui.sl_viscosity  .getValue() * .0000005 );
  fluid.setParam_Diffusion         (       my_gui.sl_diffusion  .getValue() * .00000005 );
  fluid.setParam_Vorticity         (       my_gui.sl_vorticity  .getValue() * .1  );
  

  fluid.processViscosity   ( (my_gui.sl_viscosity.getValue() > 1) );
  fluid.processDiffusion   ( (my_gui.sl_diffusion.getValue() > 1) );
  fluid.processVorticity   ( (my_gui.sl_vorticity.getValue() > 1) );
  
  
  // smooth output
  fluid.smoothDensityMap(my_gui.cb_smooth.Status().isActive());
  
  // simulate next step
  if( !my_gui.cb_pause.Status().isActive() ){
    fluid.update();
  }
  
  // visualize
  image(fluid.getDensityMap(), 0, 0);
//println(frameRate);

}


