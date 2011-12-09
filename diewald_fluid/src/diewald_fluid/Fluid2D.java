/**
 * 
 * diewald_fluid v00.30
 * 
 * fluid dynamics library for processing - CPU / GPU mode.
 * 
 * 
 * 
 *   (C) 2011    Thomas Diewald
 *               http://www.thomasdiewald.com
 *   
 *   last built: 12/09/2011
 *   
 *   download:   http://thomasdiewald.com/processing/libraries/diewald_fluid/
 *   source:     https://github.com/diwi/diewald_fluid 
 *   
 *   tested OS:  osx,windows
 *   processing: 1.5.1, 2.04
 *
 *
 *
 *
 * This source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * A copy of the GNU General Public License is available on the World
 * Wide Web at <http://www.gnu.org/copyleft/gpl.html>. You can also
 * obtain it by writing to the Free Software Foundation,
 * Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */



package diewald_fluid;

import processing.core.PApplet;
import processing.core.PImage;

import processing.opengl.PGraphicsOpenGL;


/**
*
* this class handles all the parameters of the fluid (CPU / GPU version).<br>
* 
* @author      Thomas Diewald
*  
*  
*/
public abstract class Fluid2D {
  protected static final String NAME_    = "diewald_fluid";
  protected static final String VERSION_ = "v0.30";
  
  protected PApplet p5parent_;
  protected int     NX_, NY_, BUFFER_SIZE_;
  
  protected float   project_factor_     = 10; 
  protected int     iterations_         = 16;
  protected int     iterations_diffuse_ = 1;
  protected float   timestep_           = 0.25245f;
  protected float   viscosity_          = 0.0000100f;
  protected float   diffusion_          = 0.0000000000f;
  protected float   vorticity_          = 1.0f;
  
  protected boolean b_buoyancy_       = false;
  protected boolean b_viscosity_      = true;
  protected boolean b_diffusion_      = true;
  protected boolean b_vorticity_      = true;
  protected boolean b_generate_density_map_ = true;
  protected boolean b_smooth_density_map_   = true;
                              
  protected int number_of_density_layers_ = 3;

  protected float[][] density_, densityOld_;
  protected float[] uVel_, uVelOld_;
  protected float[] vVel_, vVelOld_;
  protected float[] curl_;
  protected float[] objects_;
  
  
  // for visualizing the density map
  protected float param_col_objects_border[] = { 1, 1, 1, 1};
  //protected float col_objects_body[]         = { 0, 1, 1, 1};
  
  static{
    System.out.println("\nprocessing library: "+NAME_ +" - "+ VERSION_+"\n");
  }
  
  
  
  
  protected Fluid2D(PApplet p5parent, int nx, int ny) {
    p5parent_ =  p5parent;
    
    if( this instanceof Fluid2D_GPU){
      if( !(p5parent_.g instanceof PGraphicsOpenGL) ){
        System.err.println("Fluid2D_GPU is USING WRONG RENDERER: ");
        System.err.println("  --> change renderer to \"GLConstants.GLGRAPHICS\"");
      }
      System.out.println("created Fluid2D_GPU");
    }

    reset(nx, ny);
  }
  /**
   *  color components have to be in the range from 0 - 1
   * 
   */
  public final void setObjectsColor(float r, float g, float b, float a){
    if( r < 0 ) r = 0; if( r > 1 ) r = 1;
    if( g < 0 ) g = 0; if( g > 1 ) g = 1;
    if( b < 0 ) b = 0; if( b > 1 ) b = 1;
    if( a < 0 ) a = 0; if( a > 1 ) a = 1;
    param_col_objects_border = new float[]{r, g, b, a};
  }

  
  public final void setTextureBackground(PImage img){
    privateSetTextureBackground(img);
  }
  public final void setTextureBackgroundColor(int r, int g, int b){
    privateSetTextureBackgroundColor(r, g, b);
  }
  
  public final PImage getTextureBackground_byRef(){
    return privateGetTextureBackground_byRef();
  }
  
  public final Fluid2D setDensityMap(PImage img){
    privateSetDensityMap(img);
    return this;
  }
  public final PImage getDensityMap(){
    return privateGetDensityMap();
  }
  
  
 
  public final void reset(int nx, int ny){
    NX_ = nx;
    NY_ = ny;
//    number_of_density_layers_ = density_layers;
    BUFFER_SIZE_ = (NX_ + 2) *(NY_ + 2);
    privateReset();
  }
  
  public final void reset(){
    privateReset();
  }
  

  public final void update(){
    privateUpdate();
  }
  
  public final float[] getBufferDensity_byRef(int density_layer){
    return privateGetBufferDensity_byRef(density_layer);
  }

  
  public final float[] getBufferVelocityU_byRef(){
    return privateGetBufferVelocityU_byRef();
  }

  public final float[] getBufferVelocityV_byRef(){
    return privateGetBufferVelocityV_byRef();
  }
  
  
  
  // abstract methods !!!!
  protected abstract float[] privateGetBufferDensity_byRef(int layer);
  protected abstract float[] privateGetBufferVelocityU_byRef();
  protected abstract float[] privateGetBufferVelocityV_byRef();
  protected abstract void    privateReset();
  protected abstract void    privateUpdate();
  protected abstract void    privateSetDensityMap       (PImage img);
  protected abstract void    privateSetTextureBackground(PImage img);
  protected abstract void    privateSetTextureBackgroundColor(int r, int g, int b);
  protected abstract PImage  privateGetTextureBackground_byRef();
  protected abstract PImage  privateGetDensityMap();

  
  

  
  
  
  /**
   * processBuoyany()
   * not implemented at the current stage!
   * 
   * @param b_buoyancy
   * @return current fluid instance
   */
  public final Fluid2D processBuoyany(boolean b_buoyancy){
    b_buoyancy_ = b_buoyancy;
    return this;
  }
  public final Fluid2D processViscosity(boolean b_viscosity){
    b_viscosity_ = b_viscosity;
    return this;
  }
  public final Fluid2D processDiffusion(boolean b_diffusion){
    b_diffusion_ = b_diffusion;
    return this;
  }
  public final Fluid2D processVorticity(boolean b_vorticity){
    b_vorticity_ = b_vorticity;
    return this;
  }
  public final Fluid2D processDensityMap(boolean b_generate_density_map){
    b_generate_density_map_ = b_generate_density_map;
    return this;
  }
  public final Fluid2D smoothDensityMap(boolean b_smooth_density_map){
    b_smooth_density_map_ = b_smooth_density_map;
    return this;
  }
  
  
  // getter
  public final boolean processBuoyany(){
    return b_buoyancy_;
  }
  public final boolean processViscosity(){
    return b_viscosity_;
  }
  public final boolean processDiffusion(){
    return b_diffusion_;
  }
  public final boolean processVorticity(){
    return b_vorticity_;
  }
  public final boolean processDensityMap(){
    return b_generate_density_map_;
  }
  public final boolean smoothDensityMap(){
    return b_smooth_density_map_;
  }
  
  
  
  
  public final int getParam_Iterations(){
    return iterations_;
  }
  public final int getParam_IterationsDiffuse(){
    return iterations_diffuse_;
  }
  public final float getParam_Timestep(){
    return timestep_;
  }
  public final float getParam_Viscosity(){
    return viscosity_;
  }
  public final float getParam_Diffusion(){
    return diffusion_;
  }
  public final float getParam_Vorticity(){
    return vorticity_;
  }
  
  
  public final int getSizeXTotal(){
    return NX_+2; 
  }
  public final int getSizeYTotal(){
    return NY_+2; 
  }
  public final int getBufferSize(){
    return BUFFER_SIZE_; 
  }
  
  
  // return buffers by refference
  // for editing the values directly
  public final float[] getSourceBufferObjects_byRef(){
    return objects_;
  }
  public final float[] getSourceBufferDensity_byRef(int layer){
    return densityOld_[layer];
  }
  public final float[] getSourceBufferVelocityU_byRef(){
    return uVelOld_;
  }
  public final float[] getSourceBufferVelocityV_byRef(){
    return vVelOld_;
  }
  
  public final int getNumberOfdensityLayer(){
    return number_of_density_layers_;
  }
  
  
  public final Fluid2D setParam_Iterations(int iterations){
    iterations_ = iterations;
    return this;
  }
  public final Fluid2D setParam_IterationsDiffuse(int iterations_diffuse){
    iterations_diffuse_ = iterations_diffuse;
    return this;
  }
  public final Fluid2D setParam_Timestep(float timestep){
    timestep_ = timestep;
    return this;
  }
  public final Fluid2D setParam_Viscosity(float viscosity){
    viscosity_ = viscosity;
    return this;
  }
  public final Fluid2D setParam_Diffusion(float diffusion){
    diffusion_ = diffusion;
    return this;
  }
  public final Fluid2D setParam_Vorticity(float vorticity){
    vorticity_ = vorticity;
    return this;
  }
  
  
  
  
  
  
  
  
  
  /**
   * addDensity()
   * add density to the fluid solver.
   * 
   * @param density_layer
   * @param x
   * @param y
   * @param value
   */
  public final void addDensity(int density_layer, int x, int y, float value){
    if( !isInsideGrid(x, y) ) return;
    densityOld_[density_layer][IDX(x, y)] = value;
  }
  
  
  /**
   * addVelocity()
   * add velocity to the fluid solver.
   * 
   * @param x
   * @param y
   * @param x_value  x-direction-vector
   * @param y_value  y-direction-vector
   */
  public final void addVelocity(int x, int y, float x_value, float y_value){
    if( !isInsideGrid(x, y) ) return;
    uVelOld_[IDX(x, y)] = x_value;
    vVelOld_[IDX(x, y)] = y_value;
  }
  
  /**
   * addObject()
   * add obstacles to the fluid solver.
   * 
   * @param x
   * @param y
   */
  public final void addObject(int x, int y){
    if( !isInsideGrid(x, y) ) return;
    objects_[IDX(x, y)] = 2.0f;
  }
  
  /**
   * removeObject()
   * remove obstacles to the fluid solver.
   * 
   * @param density_layer
   * @param x
   * @param y
   */
  public final void removeObject(int x, int y){
    objects_[IDX(x, y)] = 0.0f;
  }
  
  /**
   * isInsideGrid()
   * this can be used, to check if the given position (x and y) is actually
   * on the fluid grid, which has usually a smaller resolution than the actual frame.
   * 
   * @param x
   * @param y
   */
  public final boolean isInsideGrid(int x, int y){
    return !( x < 1 || x > NX_ || y < 1 || y > NY_);
  }
  
 
  //----------------------------------------------------------------------------
  // IDX();
  //----------------------------------------------------------------------------
  protected final int IDX(int x, int y) { 
    return x + (NX_+2) * y;
  }
  
  
  //----------------------------------------------------------------------------
  // clear();
  //----------------------------------------------------------------------------
  protected final void clear(float[] array, float val) { 
    for(int i = 0; i < array.length; i++)
      array[i] = val;
  }
  protected final void clear(int[] array, int val) { 
    for(int i = 0; i < array.length; i++)
      array[i] =  val;
  }
  

  
}
