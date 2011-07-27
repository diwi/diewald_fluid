import processing.core.*; 
import processing.xml.*; 

import diewald_fluid.Fluid2D; 
import diewald_fluid.Fluid2D_CPU; 
import diewald_fluid.Fluid2D_GPU; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class fluid2d_basic_CPU_v2 extends PApplet {

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
  size(600, 600, JAVA2D);


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
public Fluid2D createFluidSolver(int type) {
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


/////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void keyPressed() {
  if ( key == 'y') edit_quader = true;
  if( online && key == ESC) key = 0;
}


public void keyReleased() {
  edit_quader = false;
}



/////////////////////////////////////////////////////////////////////////////////////////////////////////////
// fluidInfluence();
//
public void fluidInfluence( Fluid2D fluid2d ) {
  if (mouseButton == LEFT ) {
    if ( edit_quader) {
      int quader_size = 2;
      int xpos = (int)(mouseX/(float)cell_size) - quader_size/2;
      int ypos = (int)(mouseY/(float)cell_size) - quader_size/2;
      addObject(fluid2d, xpos, ypos, quader_size, quader_size, 0);
    } 
    else {
      setDens(fluid2d, mouseX, mouseY, 4, 4, 1, 1, 1);
    }
  }
  if (mouseButton == CENTER ) {
    if ( edit_quader ) {
      int quader_size = 2;
      int xpos = (int)(mouseX/(float)cell_size) - quader_size/2;
      int ypos = (int)(mouseY/(float)cell_size) - quader_size/2;
      addObject(fluid2d, xpos, ypos, quader_size, quader_size, 1);
    } 
    else {
      setDens(fluid2d, mouseX, mouseY, 4, 4, 2, 0, 0);
    }
  }
  if (mouseButton == RIGHT ) {
    float vel_fac = 0.13f;
    int size = (int)(((fluid_size_x+fluid_size_y)/2.0f) / 50.0f);
    size = max(size, 1);
    setVel(fluid2d, mouseX, mouseY, size, size, (mouseX - pmouseX)*vel_fac, (mouseY - pmouseY)*vel_fac);
  }
}    


/////////////////////////////////////////////////////////////////////////////////////////////////////////////
// setDens();
//
public void setDens(Fluid2D fluid2d, int x, int y, int sizex, int sizey, float r, float g, float b) {
  for (int y1 = 0; y1 < sizey; y1++) {
    for (int x1 = 0; x1 < sizex; x1++) {
      int xpos = (int)(x/(float)cell_size) + x1 - sizex/2;
      int ypos = (int)(y/(float)cell_size) + y1 - sizey/2;
      fluid2d.addDensity(0, xpos, ypos, r);
      fluid2d.addDensity(1, xpos, ypos, g);
      fluid2d.addDensity(2, xpos, ypos, b);
    }
  }
}


/////////////////////////////////////////////////////////////////////////////////////////////////////////////
// setVel();
//
public void setVel(Fluid2D fluid2d, int x, int y, int sizex, int sizey, float velx, float vely) {
  for (int y1 = 0; y1 < sizey; y1++) {
    for (int x1 = 0; x1 < sizex; x1++) {
      int xpos = (int)((x/(float)cell_size)) + x1 - sizex/2;
      int ypos = (int)((y/(float)cell_size)) + y1 - sizey/2;
      fluid2d.addVelocity(xpos, ypos, velx, vely);
    }
  }
}


/////////////////////////////////////////////////////////////////////////////////////////////////////////////
// addObject();
//
public void addObject(Fluid2D fluid2d, int posx, int posy, int sizex, int sizey, int mode) {
  int offset = 0;
  int xlow = posx;
  int xhig = posx + sizex;
  int ylow = posy;
  int yhig = posy + sizey;

  for (int x = xlow-offset ; x < xhig+offset ; x++) {
    for (int y = ylow-offset ; y < yhig+offset ; y++) {
      if ( x < 0 || x >= fluid2d.getSizeXTotal() || y < 0 || y >= fluid2d.getSizeYTotal() )
        continue;
      if ( mode == 0) fluid2d.addObject(x, y);
      if ( mode == 1) fluid2d.removeObject(x, y); 
    }
  }
}

  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#D4D0C8", "fluid2d_basic_CPU_v2" });
  }
}
