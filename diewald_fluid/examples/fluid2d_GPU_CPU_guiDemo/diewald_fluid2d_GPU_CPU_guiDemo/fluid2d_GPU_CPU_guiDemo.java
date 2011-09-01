import processing.core.*; 
import processing.xml.*; 

import diewald.p5gui.*; 
import diewald.p5gui.constants.*; 
import diewald.p5gui.utilities.Color; 
import diewald_fluid.Fluid2D; 
import diewald_fluid.Fluid2D_CPU; 
import diewald_fluid.Fluid2D_GPU; 

import diewald.p5gui.designElements.*; 
import diewald.p5gui.utilities.*; 
import diewald.p5gui.constants.*; 
import diewald.p5gui.*; 
import diewald.p5gui.interfaces.*; 

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

public class fluid2d_GPU_CPU_guiDemo extends PApplet {

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
  fluid.setParam_Timestep          (       my_gui.sl_timestep   .getValue() *.005f );
  fluid.setParam_Iterations        ( (int) my_gui.sl_iterations1.getValue() );
  fluid.setParam_IterationsDiffuse ( (int) my_gui.sl_iterations2.getValue() );
  fluid.setParam_Viscosity         (       my_gui.sl_viscosity  .getValue() * .0000005f );
  fluid.setParam_Diffusion         (       my_gui.sl_diffusion  .getValue() * .00000005f );
  fluid.setParam_Vorticity         (       my_gui.sl_vorticity  .getValue() * .1f  );
  

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


//------------------------------------------------------------------------------
//
// author: thomas diewald
// date: 27.07.2011
//
// basic fluid examples with gui controlls
//
// TAB: GUI
// 
//------------------------------------------------------------------------------






public void initGUI(){
  //override gui-event
 
  my_gui = new GUI_CLASS(this, gui_pos_x, gui_pos_y, gui_size_x, gui_size_y, CPU_GPU);
  my_gui.makeGui();

  gui_event_ = new GUI_Event(){
      public void guiEvent(GUI_Element element){
      myGuiEvent(element);
    }
  };
  my_gui.gui.setGuiEvent(gui_event_);
  
  my_gui.cb_emitter1.trigger();
  my_gui.cb_emitter2.trigger();
  my_gui.cb_emitter3.trigger();
  my_gui.cb_emitter4.trigger();
  

  my_gui.cb_mouse_emit.trigger();
}
  
  
  
  
//------------------------------------------------------------------------------ 
//gets called automatically
public void myGuiEvent(GUI_Element element){
  //PApplet.println ("... active control = \"" +  element.Label().getLabel()+"\"");
  
  

  if( element == my_gui.cb_mouse_emit && my_gui.cb_mouse_emit.Status().isActive() ){
    my_gui.cb_addObstacles   .Status().setActive(false);
    my_gui.cb_removeObstacles.Status().setActive(false);
  }
  if( element == my_gui.cb_addObstacles && my_gui.cb_addObstacles.Status().isActive() ){
    my_gui.cb_mouse_emit     .Status().setActive(false);
    my_gui.cb_removeObstacles.Status().setActive(false);
  }
  if( element == my_gui.cb_removeObstacles && my_gui.cb_removeObstacles.Status().isActive() ){
    my_gui.cb_mouse_emit  .Status().setActive(false);
    my_gui.cb_addObstacles.Status().setActive(false);
  }
  
  //fluid_tmp.setDensityMap(output_densityMap);
  
  if( element == my_gui.b_reset_fluid){
    fluid.reset();
    fluid.setDensityMap(output_densityMap);
  }
  
}

















public class GUI_CLASS {
  // positioning
  public int gpx_, gpy_, gsx_, gsy_;
  public int gap_y_  = 15;
  public int offset_ = 20;
  public int h_def_  = 18;
  
  public PApplet papplet_;
  
  // gui instance
  public GUI             gui;
  
  // gui elements
  public GUI_Button      b_reset_fluid ;
  
  public GUI_Switch      sw1, sw2 ;
  
  public GUI_Tag         tag_caption;
 

  public GUI_CheckBox    cb_smooth,
                         cb_pause,
                         cb_emitter1,
                         cb_emitter2,
                         cb_emitter3,
                         cb_emitter4,
                         cb_addObstacles,
                         cb_removeObstacles,
                         cb_mouse_emit;

  public GUI_Slider      sl_timestep, 
                         sl_iterations1,
                         sl_iterations2, 
                         sl_viscosity,
                         sl_diffusion,
                         sl_vorticity,
                         sl_mouse_red, sl_mouse_green, sl_mouse_blue,
                         sl_addObstacle_size, sl_removeObstacle_size, sl_mouse_emit_size;
                         
  public GUI_TextDisplay td_frameRate;

  int fluid_mode_;
 
//------------------------------------------------------------------------------ 
  public GUI_CLASS(PApplet papplet, int gui_pos_x, int gui_pos_y, int gui_size_x, int gui_size_y, int fluid_mode){
    gpx_ = gui_pos_x;
    gpy_ = gui_pos_y;
    gsx_ = gui_size_x;
    gsy_ = gui_size_y;
    fluid_mode_ = fluid_mode;
    papplet_ = papplet;
  }

  


//------------------------------------------------------------------------------ 
  public void makeGui(){
     
    gui = new GUI(papplet_);
//    if( papplet_.g.is3D() )
//      gui.autoBuild(false);
//    else
      gui.autoBuild(true);
    
// ----------------------------------------------------------------

    PFont gui_label_font = papplet_.createFont("Calibri", 12);

    int x, y, w, h;
    

   
    
    w = gsx_-offset_*2;
    h = h_def_*2;
    x = gpx_+offset_;
    y = offset_;
 
    
//  tag 1
    String mode = fluid_mode_ == 0 ? "GPU" : "CPU";
    tag_caption = new GUI_Tag (gui, 1, "fluid simulation ["+mode+"]", x, y, w, h);
    tag_caption.Label().setLabelAlign(LABEL_ALIGN.CENTER);
    tag_caption.InfoBox().setVisible(false);
    
    
    h = h_def_;
    y += h+gap_y_;
    td_frameRate = new GUI_TextDisplay (gui, 1, "textdisplay", x, y, w, h);
    td_frameRate.Label().setLabelAlign(LABEL_ALIGN.CENTER);
    td_frameRate.Border().setVisible(false);
    td_frameRate.setMethodNameToInvoke("guiCallFrameRate");
    td_frameRate.InfoBox().setLabel("framerate on CPU-Simulation\nGPU simulation is about 10 to 20 times faster" );
    td_frameRate.InfoBox().setFont(papplet_.createFont("Calibri", 12));
    td_frameRate.InfoBox().setPopupDelay(600);
    
    x = gpx_+offset_;
    y += h+gap_y_;
    w = gsx_-offset_*2;
    h = h_def_;

    b_reset_fluid = new GUI_Button (gui, 1, "RESET FLUID", x, y, w, h);
    b_reset_fluid.Label().setFont(gui_label_font);
    b_reset_fluid.Label().setLabelAlign(LABEL_ALIGN.CENTER);
    b_reset_fluid.InfoBox().setLabel("reset fluid" );
    b_reset_fluid.InfoBox().setFont(papplet_.createFont("Calibri", 12));
    b_reset_fluid.InfoBox().setPopupDelay(600);
    
    
    x = gpx_+offset_;
    y += h+gap_y_;
    w = h;
    cb_pause = new GUI_CheckBox (gui, 1, "pause", x, y, w, h);
    cb_pause.Label().setLabelAlign(LABEL_ALIGN.LEFT);
    cb_pause.Label().setPosXrel(w);   
    cb_pause.InfoBox().setLabel("pause simulation" );
    cb_pause.InfoBox().setFont(papplet_.createFont("Calibri", 12));
    cb_pause.InfoBox().setPopupDelay(600);
    
    x = gpx_+gsx_/2;
    w = h;
    h = h_def_;
    cb_smooth = new GUI_CheckBox (gui, 1, "smooth output", x, y, w, h);
    cb_smooth.Label().setLabelAlign(LABEL_ALIGN.LEFT);
    cb_smooth.Label().setPosXrel(w);   
    cb_smooth.InfoBox().setLabel("smooth the output map" );
    cb_smooth.InfoBox().setFont(papplet_.createFont("Calibri", 12));
    cb_smooth.InfoBox().setPopupDelay(600);
    

    
    
    
    


    
    
    
    x = gpx_+offset_;
    w = gsx_-offset_*2;
    x += 80;
    w -= 80;
    y += h+gap_y_*2;
    h = (int)(2.0f*h_def_/3.0f);
    sl_timestep = new GUI_Slider (gui, 1, "timestep", x, y, w, h);
    sl_timestep.Label().setLabelAlign(LABEL_ALIGN.LEFT);
    sl_timestep.Label().setPosXrel(-80);
    sl_timestep.setValue(20f);
    sl_timestep.setValueMin(0);
    sl_timestep.setValueMax(100);
    sl_timestep.setLabelPrecision(4);
    sl_timestep.LabelValue().setLabelAlign(LABEL_ALIGN.CENTER);    
    sl_timestep.setOrientation(SLIDER_ORIENTATION.HORIZONTAL);
    sl_timestep.Border().setVisible(false);
    sl_timestep.InfoBox().setLabel("change timestep" );
    sl_timestep.InfoBox().setFont(papplet_.createFont("Calibri", 12));
    sl_timestep.InfoBox().setPopupDelay(600);
    
    
    y += h+gap_y_/2.0f;
    sl_viscosity = new GUI_Slider (gui, 1, "viscosity", x, y, w, h);
    sl_viscosity.Label().setLabelAlign(LABEL_ALIGN.LEFT);
    sl_viscosity.Label().setPosXrel(-80);
    sl_viscosity.setValue(0);
    sl_viscosity.setValueMin(0);
    sl_viscosity.setValueMax(100);
    sl_viscosity.setLabelPrecision(4);
    sl_viscosity.LabelValue().setLabelAlign(LABEL_ALIGN.CENTER);    
    sl_viscosity.setOrientation(SLIDER_ORIENTATION.HORIZONTAL);
    sl_viscosity.Border().setVisible(false);
    sl_viscosity.InfoBox().setLabel("change viscosity" );
    sl_viscosity.InfoBox().setFont(papplet_.createFont("Calibri", 12));
    sl_viscosity.InfoBox().setPopupDelay(600);
    
    y += h+gap_y_/2.0f;
    sl_diffusion = new GUI_Slider (gui, 1, "diffusion", x, y, w, h);
    sl_diffusion.Label().setLabelAlign(LABEL_ALIGN.LEFT);
    sl_diffusion.Label().setPosXrel(-80);
    sl_diffusion.setValue(6);
    sl_diffusion.setValueMin(0);
    sl_diffusion.setValueMax(100);
    sl_diffusion.setLabelPrecision(4);
    sl_diffusion.LabelValue().setLabelAlign(LABEL_ALIGN.CENTER);    
    sl_diffusion.setOrientation(SLIDER_ORIENTATION.HORIZONTAL);
    sl_diffusion.Border().setVisible(false);
    sl_diffusion.InfoBox().setLabel("change diffusion" );
    sl_diffusion.InfoBox().setFont(papplet_.createFont("Calibri", 12));
    sl_diffusion.InfoBox().setPopupDelay(600);
    
    y += h+gap_y_/2.0f;
    sl_vorticity = new GUI_Slider (gui, 1, "vorticity", x, y, w, h);
    sl_vorticity.Label().setLabelAlign(LABEL_ALIGN.LEFT);
    sl_vorticity.Label().setPosXrel(-80);
    sl_vorticity.setValue(20);
    sl_vorticity.setValueMin(0);
    sl_vorticity.setValueMax(100);
    sl_vorticity.setLabelPrecision(4);
    sl_vorticity.LabelValue().setLabelAlign(LABEL_ALIGN.CENTER);    
    sl_vorticity.setOrientation(SLIDER_ORIENTATION.HORIZONTAL);
    sl_vorticity.Border().setVisible(false);
    sl_vorticity.InfoBox().setLabel("change vorticity" );
    sl_vorticity.InfoBox().setFont(papplet_.createFont("Calibri", 12));
    sl_vorticity.InfoBox().setPopupDelay(600);
    
    
    y += h+gap_y_/2.0f;
    sl_iterations1 = new GUI_Slider (gui, 1, "iterations1", x, y, w, h);
    sl_iterations1.Label().setLabelAlign(LABEL_ALIGN.LEFT);
    sl_iterations1.Label().setPosXrel(-80);
    sl_iterations1.setValue(8);
    sl_iterations1.setValueMin(1);
    sl_iterations1.setValueMax(20);
    sl_iterations1.setLabelPrecision(0);
    sl_iterations1.LabelValue().setLabelAlign(LABEL_ALIGN.CENTER);    
    sl_iterations1.setOrientation(SLIDER_ORIENTATION.HORIZONTAL);
    sl_iterations1.Border().setVisible(false);
    sl_iterations1.InfoBox().setLabel("to speedup the simulation\n take a smaller value" );
    sl_iterations1.InfoBox().setFont(papplet_.createFont("Calibri", 12));
    sl_iterations1.InfoBox().setPopupDelay(600);
    
    y += h+gap_y_/2.0f;
    sl_iterations2 = new GUI_Slider (gui, 1, "iterations2", x, y, w, h);
    sl_iterations2.Label().setLabelAlign(LABEL_ALIGN.LEFT);
    sl_iterations2.Label().setPosXrel(-80);
    sl_iterations2.setValue(1);
    sl_iterations2.setValueMin(1);
    sl_iterations2.setValueMax(20);
    sl_iterations2.setLabelPrecision(0);
    sl_iterations2.LabelValue().setLabelAlign(LABEL_ALIGN.CENTER);    
    sl_iterations2.setOrientation(SLIDER_ORIENTATION.HORIZONTAL);
    sl_iterations2.Border().setVisible(false);
    sl_iterations2.InfoBox().setLabel("to speedup the simulation\n take a smaller value" );
    sl_iterations2.InfoBox().setFont(papplet_.createFont("Calibri", 12));
    sl_iterations2.InfoBox().setPopupDelay(600);



    
    x = gpx_+offset_;
    h = h_def_;
    w = h;

    y += h+gap_y_*2;
    cb_emitter1 = new GUI_CheckBox (gui, 1, "emitter[1] on/off", x, y, w, h);
    cb_emitter1.Label().setLabelAlign(LABEL_ALIGN.LEFT);
    cb_emitter1.Label().setPosXrel(w);   
    cb_emitter1.InfoBox().setVisible(false);
    
    y += h+gap_y_/2.0f;
    cb_emitter2 = new GUI_CheckBox (gui, 1, "emitter[2] on/off", x, y, w, h);
    cb_emitter2.Label().setLabelAlign(LABEL_ALIGN.LEFT);
    cb_emitter2.Label().setPosXrel(w);   
    cb_emitter2.InfoBox().setVisible(false);
    
    y += h+gap_y_/2.0f;
    cb_emitter3 = new GUI_CheckBox (gui, 1, "emitter[3] on/off", x, y, w, h);
    cb_emitter3.Label().setLabelAlign(LABEL_ALIGN.LEFT);
    cb_emitter3.Label().setPosXrel(w);   
    cb_emitter3.InfoBox().setVisible(false);
    
    y += h+gap_y_/2.0f;
    cb_emitter4 = new GUI_CheckBox (gui, 1, "emitter[4] on/off", x, y, w, h);
    cb_emitter4.Label().setLabelAlign(LABEL_ALIGN.LEFT);
    cb_emitter4.Label().setPosXrel(w);   
    cb_emitter4.InfoBox().setVisible(false);
    






    


    y += h+gap_y_*2;
    x = gpx_+offset_;
    h = h_def_;
    w = h;
    cb_addObstacles = new GUI_CheckBox (gui, 1, "add obstacles", x, y, w, h);
    cb_addObstacles.Label().setLabelAlign(LABEL_ALIGN.LEFT);
    cb_addObstacles.Label().setPosXrel(w);   
    cb_addObstacles.InfoBox().setLabel("use mouse LMB to add obstacles" );
    cb_addObstacles.InfoBox().setFont(papplet_.createFont("Calibri", 12));
    cb_addObstacles.InfoBox().setPopupDelay(600);
    
    x = gpx_+gsx_/2;
    w = gsx_/2-offset_;
    h = (int)(2.0f*h_def_/3.0f);
    sl_addObstacle_size = new GUI_Slider (gui, 1, "add obstacle", x, y, w, h);
    sl_addObstacle_size.Label().setLabelAlign(LABEL_ALIGN.LEFT);
    sl_addObstacle_size.Label().setPosXrel(-80);
    sl_addObstacle_size.Label().setVisible(false);
    sl_addObstacle_size.setValue(2);
    sl_addObstacle_size.setValueMin(1);
    sl_addObstacle_size.setValueMax(10);
    sl_addObstacle_size.setLabelPrecision(0);
    sl_addObstacle_size.LabelValue().setLabelAlign(LABEL_ALIGN.CENTER);    
    sl_addObstacle_size.setOrientation(SLIDER_ORIENTATION.HORIZONTAL);
    sl_addObstacle_size.Border().setVisible(false);
    sl_addObstacle_size.InfoBox().setLabel("set square size of obstacle" );
    sl_addObstacle_size.InfoBox().setFont(papplet_.createFont("Calibri", 12));
    sl_addObstacle_size.InfoBox().setPopupDelay(600);
    
    y += h_def_+gap_y_/2.0f;
    x = gpx_+offset_;
    h = h_def_;
    w = h;
    cb_removeObstacles = new GUI_CheckBox (gui, 1, "remove obstacles", x, y, w, h);
    cb_removeObstacles.Label().setLabelAlign(LABEL_ALIGN.LEFT);
    cb_removeObstacles.Label().setPosXrel(w);   
    cb_removeObstacles.InfoBox().setLabel("use mouse LMB to remove obstacles" );
    cb_removeObstacles.InfoBox().setFont(papplet_.createFont("Calibri", 12));
    cb_removeObstacles.InfoBox().setPopupDelay(600);
    
    
    x = gpx_+gsx_/2;
    w = gsx_/2-offset_;
    h = (int)(2.0f*h_def_/3.0f);
    sl_removeObstacle_size = new GUI_Slider (gui, 1, "remove obstacle", x, y, w, h);
    sl_removeObstacle_size.Label().setLabelAlign(LABEL_ALIGN.LEFT);
    sl_removeObstacle_size.Label().setPosXrel(-80);
    sl_removeObstacle_size.Label().setVisible(false);
    sl_removeObstacle_size.setValue(2);
    sl_removeObstacle_size.setValueMin(1);
    sl_removeObstacle_size.setValueMax(10);
    sl_removeObstacle_size.setLabelPrecision(0);
    sl_removeObstacle_size.LabelValue().setLabelAlign(LABEL_ALIGN.CENTER);    
    sl_removeObstacle_size.setOrientation(SLIDER_ORIENTATION.HORIZONTAL);
    sl_removeObstacle_size.Border().setVisible(false);
    sl_removeObstacle_size.InfoBox().setLabel("set square size of obstacle" );
    sl_removeObstacle_size.InfoBox().setFont(papplet_.createFont("Calibri", 12));
    sl_removeObstacle_size.InfoBox().setPopupDelay(600);
    
    
    y += h_def_+gap_y_/2.0f;
    x = gpx_+offset_;
    h = h_def_;
    w = h;
    cb_mouse_emit = new GUI_CheckBox (gui, 1, "mouse as emitter", x, y, w, h);
    cb_mouse_emit.Label().setLabelAlign(LABEL_ALIGN.LEFT);
    cb_mouse_emit.Label().setPosXrel(w);   
    cb_mouse_emit.InfoBox().setLabel("use mouse as emitter\nLMB + drag = density\nRBM + drag = velocity" );
    cb_mouse_emit.InfoBox().setFont(papplet_.createFont("Calibri", 12));
    cb_mouse_emit.InfoBox().setPopupDelay(600);
    
    
    

    x = gpx_+gsx_/2;
    w = gsx_/2-offset_;
    h = (int)(2.0f*h_def_/3.0f);
    sl_mouse_emit_size = new GUI_Slider (gui, 1, "mouse emitter size", x, y, w, h);
    sl_mouse_emit_size.Label().setLabelAlign(LABEL_ALIGN.LEFT);
    sl_mouse_emit_size.Label().setPosXrel(-80);
    sl_mouse_emit_size.Label().setVisible(false);
    sl_mouse_emit_size.setValue(5);
    sl_mouse_emit_size.setValueMin(1);
    sl_mouse_emit_size.setValueMax(10);
    sl_mouse_emit_size.setLabelPrecision(0);
    sl_mouse_emit_size.LabelValue().setLabelAlign(LABEL_ALIGN.CENTER);    
    sl_mouse_emit_size.setOrientation(SLIDER_ORIENTATION.HORIZONTAL);
    sl_mouse_emit_size.Border().setVisible(false);
    sl_mouse_emit_size.InfoBox().setLabel("set square size of obstacle" );
    sl_mouse_emit_size.InfoBox().setFont(papplet_.createFont("Calibri", 12));
    sl_mouse_emit_size.InfoBox().setPopupDelay(600);
    
    
    
    
    
    x = gpx_+offset_;
    w = gsx_-offset_*2;
    x += 80;
    w -= 80;
    y += h+gap_y_;
    h = (int)(2.0f*h_def_/3.0f);
    sl_mouse_red = new GUI_Slider (gui, 1, "mouse: red", x, y, w, h);
    sl_mouse_red.Label().setLabelAlign(LABEL_ALIGN.LEFT);
    sl_mouse_red.Label().setPosXrel(-80);
    sl_mouse_red.setValue(255);
    sl_mouse_red.setValueMin(0);
    sl_mouse_red.setValueMax(255);
    sl_mouse_red.setLabelPrecision(0);
    sl_mouse_red.LabelValue().setLabelAlign(LABEL_ALIGN.CENTER);    
    sl_mouse_red.setOrientation(SLIDER_ORIENTATION.HORIZONTAL);
    sl_mouse_red.Border().setVisible(false);
    sl_mouse_red.InfoBox().setLabel("red-value for mouse-emitter" );
    sl_mouse_red.InfoBox().setFont(papplet_.createFont("Calibri", 12));
    sl_mouse_red.InfoBox().setPopupDelay(600);
    
    
    y += h+gap_y_/2.0f;
    sl_mouse_green = new GUI_Slider (gui, 1, "mouse: green", x, y, w, h);
    sl_mouse_green.Label().setLabelAlign(LABEL_ALIGN.LEFT);
    sl_mouse_green.Label().setPosXrel(-80);
    sl_mouse_green.setValue(120);
    sl_mouse_green.setValueMin(0);
    sl_mouse_green.setValueMax(255);
    sl_mouse_green.setLabelPrecision(0);
    sl_mouse_green.LabelValue().setLabelAlign(LABEL_ALIGN.CENTER);    
    sl_mouse_green.setOrientation(SLIDER_ORIENTATION.HORIZONTAL);
    sl_mouse_green.Border().setVisible(false);
    sl_mouse_green.InfoBox().setLabel("green-value for mouse-emitter" );
    sl_mouse_green.InfoBox().setFont(papplet_.createFont("Calibri", 12));
    sl_mouse_green.InfoBox().setPopupDelay(600);
    
    y += h+gap_y_/2.0f;
    sl_mouse_blue = new GUI_Slider (gui, 1, "mouse: blue", x, y, w, h);
    sl_mouse_blue.Label().setLabelAlign(LABEL_ALIGN.LEFT);
    sl_mouse_blue.Label().setPosXrel(-80);
    sl_mouse_blue.setValue(0);
    sl_mouse_blue.setValueMin(0);
    sl_mouse_blue.setValueMax(255);
    sl_mouse_blue.setLabelPrecision(0);
    sl_mouse_blue.LabelValue().setLabelAlign(LABEL_ALIGN.CENTER);    
    sl_mouse_blue.setOrientation(SLIDER_ORIENTATION.HORIZONTAL);
    sl_mouse_blue.Border().setVisible(false);
    sl_mouse_blue.InfoBox().setLabel("blue-value for mouse-emitter" );
    sl_mouse_blue.InfoBox().setFont(papplet_.createFont("Calibri", 12));
    sl_mouse_blue.InfoBox().setPopupDelay(600);



//    edit all gui-elements
    ArrayList<GUI_Element> elements = gui.getElements(); 
    for(int i = 0; i < elements.size(); i++){
      GUI_Element e = elements.get(i);
      e.Border().setVisible(false);
      e.InfoBox().setBaseColor   (Color.col(0,150));
      e.InfoBox().setBorderColor (Color.col(50));
      e.InfoBox().setLabelColor  (Color.col(200));
      e.Base().setVisible(true);
      e.Base().setColor(ELEMENT_STATUS.DEFAULT, Color.col(30));
      e.Base().setColor(ELEMENT_STATUS.FOCUSED, Color.col(40));
      e.Base().setColor(ELEMENT_STATUS.PRESSED, Color.col(90));
      if( e instanceof GUI_Slider)
        ((GUI_Slider)e).setSliderColor(color(120));
    }
    
    
//    set a function to call automatically
    gui.MethodWrapper().addMethod("guiCallBackground");
   
  }
  
  
  public String guiCallFrameRate(){
    String str = String.format(Locale.ENGLISH, "fps: %3.2f\n", papplet_.frameRate);
    return str;
  }
  
  public void guiCallBackground(){
     int xp, yp;
     papplet_.fill(0);  papplet_.noStroke();
     papplet_.rect(gpx_, gpy_, gsx_, gsy_);
     papplet_.stroke(100); strokeWeight(3);
     papplet_.line(gpx_, gpy_, gsy_, gsy_);
     
     strokeWeight(1);
     
     xp = gpx_+offset_ ;
     
     yp = sl_timestep.Base().getPosY() - h_def_/2;
     papplet_.line(xp, yp, xp+gsx_-offset_*2, yp);
     
     yp = cb_emitter1.Base().getPosY() - h_def_/2;
     papplet_.line(xp, yp, xp+gsx_-offset_*2, yp);
     

     yp = cb_addObstacles.Base().getPosY() - h_def_/2;
     papplet_.line(xp, yp, xp+gsx_-offset_*2, yp);
    
  }
  
}


public String guiCallFrameRate(){
  return my_gui.guiCallFrameRate();
}

public void guiCallBackground(){
  my_gui.guiCallBackground();
}



/////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void keyPressed() {
  if( online && key == ESC) key = 0;
}






/////////////////////////////////////////////////////////////////////////////////////////////////////////////
// createFluidSolver();
//
public Fluid2D createFluidSolver(int type) {
  Fluid2D fluid_tmp = null;
  
  if ( type == 0) fluid_tmp = new Fluid2D_GPU(this, fluid_size_x, fluid_size_y);
  if ( type == 1) fluid_tmp = new Fluid2D_CPU(this, fluid_size_x, fluid_size_y);

  fluid_tmp.setParam_Timestep  ( 0.10f );
  fluid_tmp.setParam_Iterations( 8 );
  fluid_tmp.setParam_IterationsDiffuse(1);
  fluid_tmp.setParam_Viscosity ( 0.00000000f );
  fluid_tmp.setParam_Diffusion ( 0.000001f );
  fluid_tmp.setParam_Vorticity ( 2.0f );
  fluid_tmp.processDensityMap  ( true );
  fluid_tmp.processDiffusion   ( true );
  fluid_tmp.processViscosity   ( true );
  fluid_tmp.processVorticity   ( true );
  fluid_tmp.setObjectsColor    (1, 1, 1, 1); 
  
  output_densityMap    = createImage(window_size_x, window_size_y, RGB);
  fluid_tmp.setDensityMap(output_densityMap);
  return fluid_tmp;
}




/////////////////////////////////////////////////////////////////////////////////////////////////////////////
// fluidInfluence();
//
public void fluidInfluence( Fluid2D fluid2d ) {
  
  if (mouseButton == LEFT ){
    if( my_gui.cb_mouse_emit.Status().isActive() ){
      int size =  (int)my_gui.sl_mouse_emit_size.getValue();
      float r  =  my_gui.sl_mouse_red.getValue() /255f;
      float g  =  my_gui.sl_mouse_green.getValue() /255f;
      float b  =  my_gui.sl_mouse_blue.getValue() /255f;

      setDens(fluid2d, mouseX, mouseY, size, size, r, g, b);
    }
    if( my_gui.cb_addObstacles.Status().isActive() ){
      int size = (int)my_gui.sl_addObstacle_size.getValue();
      int xpos = (int)(mouseX/(float)cell_size) - size/2;
      int ypos = (int)(mouseY/(float)cell_size) - size/2;
      addObject(fluid2d, xpos, ypos, size, size, 0);
    }
    if( my_gui.cb_removeObstacles.Status().isActive() ){
      int size = (int)my_gui.sl_removeObstacle_size.getValue();
      int xpos = (int)(mouseX/(float)cell_size) - size/2;
      int ypos = (int)(mouseY/(float)cell_size) - size/2;
      addObject(fluid2d, xpos, ypos, size, size, 1);
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
    PApplet.main(new String[] { "--bgcolor=#D4D0C8", "fluid2d_GPU_CPU_guiDemo" });
  }
}
