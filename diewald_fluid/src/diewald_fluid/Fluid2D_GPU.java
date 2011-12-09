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

import codeanticode.glgraphics.GLTexture;
import codeanticode.glgraphics.GLTextureFilter;
import codeanticode.glgraphics.GLTextureParameters;
import processing.core.PApplet;
import processing.core.PImage;


/**
*
* this class creates a 2D-fluid-solver that runs completely on the GPU.
* GLSL-fragment-shaders are computing the fluid.<br>
* The performance is about 10-20 times (depending on your graphics-card) as good 
* as with the CPU-version.<br>
* <br>
* to use this class, you have to import the processing library:
* "GLGraphics.jar" by Andres Colubri.<br>
* <br>
* if its not already in the library folder, or comes with this library, 
* here is the download location:<br>
* <br>
*  <a href="http://glgraphics.sourceforge.net/">http://glgraphics.sourceforge.net</a>
*  <br>
*  <br>
* @author Thomas Diewald
*  
*  
*/
public final class Fluid2D_GPU extends Fluid2D{
  private GLTexture[] TEX_density_, TEX_densityOld_;
  private GLTexture TEX_uVel_, TEX_uVelOld_;
  private GLTexture TEX_vVel_, TEX_vVelOld_;
  private GLTexture TEX_curl_;
  private GLTexture TEX_objects_;
  private GLTexture TEX_background_;
  private GLTexture TEX_densityMap_;

  private GLTextureFilter TEX_FILTER_addSource_;
  private GLTextureFilter TEX_FILTER_applySource_;
  private GLTextureFilter TEX_FILTER_constrainValues_;
  private GLTextureFilter TEX_FILTER_slow_;
  private GLTextureFilter TEX_FILTER_project_STEP1_;
  private GLTextureFilter TEX_FILTER_project_STEP2_;
  private GLTextureFilter TEX_FILTER_linearSolver_Jacobi_;
  private GLTextureFilter TEX_FILTER_advect_;
  private GLTextureFilter TEX_FILTER_setBounds_;
  private GLTextureFilter TEX_FILTER_vorticityConfinement_STEP1_;
  private GLTextureFilter TEX_FILTER_vorticityConfinement_STEP2_;
  
  private GLTextureFilter TEX_FILTER_generateDensityMap_;



  
  //----------------------------------------------------------------------------
  // FluidSolver(); - CONSTRUCTOR
  //----------------------------------------------------------------------------
  public Fluid2D_GPU(PApplet p5parent, int nx, int ny){
    super(p5parent, nx, ny);
  }
  



  //----------------------------------------------------------------------------
  // reset();
  //----------------------------------------------------------------------------
  @Override
  protected final void privateReset(){
    // allocate array-buffers
    density_    = new float[number_of_density_layers_][BUFFER_SIZE_];
    densityOld_ = new float[number_of_density_layers_][BUFFER_SIZE_];
    uVel_       = new float[BUFFER_SIZE_];
    uVelOld_    = new float[BUFFER_SIZE_];
    vVel_       = new float[BUFFER_SIZE_];
    vVelOld_    = new float[BUFFER_SIZE_];
    curl_       = new float[BUFFER_SIZE_];
    objects_    = new float[BUFFER_SIZE_];

    // reset array-buffers to 0
    for (int i = 0; i < BUFFER_SIZE_; i++){
      uVel_[i] = uVelOld_[i] = vVel_[i] = vVelOld_[i] = curl_[i] = 0.0f;
      objects_[i] = 0;
    }
    
    for(int i = 0; i < density_.length; i++)
      for (int j = 0; j < BUFFER_SIZE_; j++)
        density_[i][j] = densityOld_[i][j] = 0.0f; 
    
    
    // allocate GLSL textures
    GLTextureParameters floatTexParams = new GLTextureParameters();
    floatTexParams.minFilter = GLTexture.NEAREST_SAMPLING;
    floatTexParams.magFilter = GLTexture.NEAREST_SAMPLING;        
    floatTexParams.format    = GLTexture.FLOAT;  
    
    GLTextureParameters colorTexParams = new GLTextureParameters();
    colorTexParams.minFilter = GLTexture.NEAREST_SAMPLING;
    colorTexParams.magFilter = GLTexture.NEAREST_SAMPLING;        
    colorTexParams.format    = GLTexture.COLOR;

    
    TEX_uVel_       = new GLTexture(p5parent_, (NX_ + 2), (NY_ + 2), floatTexParams);
    TEX_uVelOld_    = new GLTexture(p5parent_, (NX_ + 2), (NY_ + 2), floatTexParams);
    TEX_vVel_       = new GLTexture(p5parent_, (NX_ + 2), (NY_ + 2), floatTexParams);
    TEX_vVelOld_    = new GLTexture(p5parent_, (NX_ + 2), (NY_ + 2), floatTexParams);
    TEX_curl_       = new GLTexture(p5parent_, (NX_ + 2), (NY_ + 2), floatTexParams);
    TEX_objects_    = new GLTexture(p5parent_, (NX_ + 2), (NY_ + 2), floatTexParams);
    TEX_background_ = new GLTexture(p5parent_, (NX_ + 2), (NY_ + 2), colorTexParams);
    TEX_densityMap_ = new GLTexture(p5parent_, (NX_ + 2), (NY_ + 2), floatTexParams);
    
    TEX_density_    = new GLTexture[number_of_density_layers_];
    TEX_densityOld_ = new GLTexture[number_of_density_layers_];
    for(int i = 0; i < TEX_density_.length; i++){
      TEX_density_[i]     = new GLTexture(p5parent_, (NX_ + 2) , (NY_ + 2), floatTexParams);
      TEX_densityOld_[i]  = new GLTexture(p5parent_, (NX_ + 2) , (NY_ + 2), floatTexParams);
    }

    
    // load GLSL filters
    TEX_FILTER_addSource_                  = new GLTextureFilter(p5parent_, "FluidSolver2D_addSource.xml");
    TEX_FILTER_applySource_                = new GLTextureFilter(p5parent_, "FluidSolver2D_applySource.xml");
    TEX_FILTER_constrainValues_            = new GLTextureFilter(p5parent_, "FluidSolver2D_constrainValues.xml");
    TEX_FILTER_slow_                       = new GLTextureFilter(p5parent_, "FluidSolver2D_slow.xml");
    TEX_FILTER_project_STEP1_              = new GLTextureFilter(p5parent_, "FluidSolver2D_project_STEP1.xml");
    TEX_FILTER_project_STEP2_              = new GLTextureFilter(p5parent_, "FluidSolver2D_project_STEP2.xml");
    TEX_FILTER_linearSolver_Jacobi_        = new GLTextureFilter(p5parent_, "FluidSolver2D_linearSolver_Jacobi.xml");
    TEX_FILTER_advect_                     = new GLTextureFilter(p5parent_, "FluidSolver2D_advect.xml");
    TEX_FILTER_setBounds_                  = new GLTextureFilter(p5parent_, "FluidSolver2D_setBounds.xml");
    TEX_FILTER_vorticityConfinement_STEP1_ = new GLTextureFilter(p5parent_, "FluidSolver2D_vorticityConfinement_STEP1.xml");
    TEX_FILTER_vorticityConfinement_STEP2_ = new GLTextureFilter(p5parent_, "FluidSolver2D_vorticityConfinement_STEP2.xml");
    TEX_FILTER_generateDensityMap_         = new GLTextureFilter(p5parent_, "FluidSolver2D_generateDensityMap.xml"); 
    
  
    // clear textures to black
    TEX_uVel_      .clear(0);
    TEX_uVelOld_   .clear(0);
    TEX_vVel_      .clear(0);
    TEX_vVelOld_   .clear(0);
    TEX_curl_      .clear(0);
    TEX_objects_   .clear(0);
    TEX_densityMap_.clear(0);
    TEX_background_.clear(0);
    
    for(int i = 0; i < density_.length; i++){
      TEX_density_[i]     .clear(0);
      TEX_densityOld_[i]  .clear(0);
    }
  }
  
  
  
  
  
  
  
  
  
  @Override
  protected final PImage privateGetTextureBackground_byRef(){
    return TEX_background_;
  }
  @Override
  protected final void privateSetTextureBackground(PImage img){
    if( img == null ){
      TEX_background_.clear(0);
    }else{
      TEX_background_.putImage(img);
    }
  }
  @Override
  protected final void privateSetTextureBackgroundColor(int r, int g, int b){
    TEX_background_.clear(r, g, b);
  }
  
  public final void setTextureBackground_byRef(GLTexture tex_bg){
    TEX_background_ = tex_bg;
  }
  
  
  @Override
  protected final PImage privateGetDensityMap(){
    return TEX_densityMap_;
  }
  
  public final GLTexture getDensityMapTexture(){
    return TEX_densityMap_;
  }
  
  @Override
  protected final void privateSetDensityMap(PImage img){
    TEX_densityMap_.putImage(img);
  }
  
  
  @Override
  protected final float[] privateGetBufferDensity_byRef(int density_layer){
    TEX_density_[density_layer].getBuffer(density_[density_layer], 1);
    return density_[density_layer];
  }
  
  @Override
  protected final float[] privateGetBufferVelocityU_byRef(){
    TEX_uVel_.getBuffer(uVel_, 1);
    return uVel_;
  }
  
  @Override
  protected final float[] privateGetBufferVelocityV_byRef(){
    TEX_vVel_.getBuffer(vVel_, 1);
    return vVel_;
  }
  
  
  
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  //// UPDATE();
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  
  @Override
  protected final void privateUpdate(){
    setTextureBuffers();
    addNewValues();
    velocitySolver();
    densitySolver();
    if( b_generate_density_map_ ){
      visualize();
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  

  
  
  
  
  //----------------------------------------------------------------------------
  // setTextureBuffers();
  //----------------------------------------------------------------------------
  private final void setTextureBuffers(){
    TEX_uVelOld_.putBuffer(uVelOld_, 1);
    TEX_vVelOld_.putBuffer(vVelOld_, 1);
    TEX_objects_.putBuffer(objects_, 1);
    
    for(int i = 0; i < TEX_densityOld_.length; i++)
      TEX_densityOld_[i].putBuffer(densityOld_[i], 1);
    
    clear(uVelOld_, 0);
    clear(vVelOld_, 0);
    for(int i = 0; i < densityOld_.length; i++)
      clear(densityOld_[i], 0);
  }
  
  
  
  //----------------------------------------------------------------------------
  // addNewValues();
  //----------------------------------------------------------------------------
  private final void addNewValues(){
    addSource(TEX_uVel_, TEX_uVelOld_, 1.0f);
    addSource(TEX_vVel_, TEX_vVelOld_, 1.0f);

    for(int i = 0; i < TEX_density_.length; i++)
      addSource(TEX_density_[i],  TEX_densityOld_[i], 1.0f);
   
      
//    TEX_uVelOld_.clear(0);
//    TEX_vVelOld_.clear(0);
//    for(int i = 0; i < density_.length; i++)
//      TEX_densityOld_[i].clear(0);
  }
  
  
  
  
  //----------------------------------------------------------------------------
  //  velocitySolver();
  //----------------------------------------------------------------------------
  private final void velocitySolver(){
    
    if( b_vorticity_ ){
      vorticityConfinement(TEX_uVel_, TEX_vVel_, TEX_uVelOld_, TEX_vVelOld_, TEX_curl_, vorticity_, TEX_objects_);
      addSource(TEX_uVel_, TEX_uVelOld_, timestep_);
      addSource(TEX_vVel_, TEX_vVelOld_, timestep_);
  //    TEX_uVelOld_.clear(0);
  //    TEX_vVelOld_.clear(0);
    }

    if( b_buoyancy_ ){
      System.err.println(this.getClass().getCanonicalName() +": 'buoyancy' is not implemented right now!!");
      //buoyancy(density_, vVelOld_);
      //addSource(TEX_vVel_, TEX_vVelOld_, timestep_);
    }

    if ( b_viscosity_ ){
      swapU();
      swapV();
      diffuse(1, TEX_uVel_, TEX_uVelOld_, viscosity_, TEX_objects_);
      diffuse(2, TEX_vVel_, TEX_vVelOld_, viscosity_, TEX_objects_);
    }
    
    project(TEX_uVel_, TEX_vVel_, TEX_uVelOld_, TEX_vVelOld_, TEX_objects_);

    swapU(); 
    swapV();
 
    advect(1, TEX_uVel_, TEX_uVelOld_, TEX_uVelOld_, TEX_vVelOld_, TEX_objects_);
    advect(2, TEX_vVel_, TEX_vVelOld_, TEX_uVelOld_, TEX_vVelOld_, TEX_objects_);

    project(TEX_uVel_, TEX_vVel_, TEX_uVelOld_, TEX_vVelOld_, TEX_objects_);
    
    TEX_uVelOld_.clear(0);
    TEX_vVelOld_.clear(0);                        
  }
  


  
  //----------------------------------------------------------------------------
  // densitySolver();
  //----------------------------------------------------------------------------
  private final void densitySolver(){
    for(int i = 0; i < TEX_density_.length; i++){
      if( b_diffusion_ ){
        swapD(i);
        diffuse(0, TEX_density_[i], TEX_densityOld_[i], diffusion_, TEX_objects_);
      }
      
      swapD(i);
      advect(0, TEX_density_[i], TEX_densityOld_[i],  TEX_uVel_, TEX_vVel_, TEX_objects_);   
      constrainValues(TEX_density_[i], 0f, 1f);

      TEX_densityOld_[i].clear(0);    
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  
  
  
  
  
  
  

  
  //----------------------------------------------------------------------------
  // vorticityConfinement();
  //----------------------------------------------------------------------------
  private final void vorticityConfinement(
      GLTexture uVel,    GLTexture vVel, 
      GLTexture uVelOld, GLTexture vVelOld,
      GLTexture curl, float vorticity, GLTexture objects)
  {
    
    TEX_FILTER_vorticityConfinement_STEP1_.apply(new GLTexture[]{ uVel, vVel, objects}, 
                                                 new GLTexture[]{ curl } );
    
    TEX_FILTER_vorticityConfinement_STEP2_.setParameterValue("vorticity", vorticity);
    TEX_FILTER_vorticityConfinement_STEP2_.apply(new GLTexture[]{ uVel, vVel, curl}, 
                                                 new GLTexture[]{ uVelOld, vVelOld } );
  }
  
  
  private final void buoyancy(float[][] dens, float[] force_buoy){
    float Tamb = 0;
    float a = 0.00625f;
    float b = 0.025f;
    float sum_factor = 10.0f;
    float sum = 0;
    
    // sum all temperatures
    for (int i = 1; i <= NX_; i++){
      for (int j = 1; j <= NY_; j++){
        sum = (dens[0][IDX(i, j)] + dens[1][IDX(i, j)] + dens[2][IDX(i, j)])/.3f;
        sum/= sum_factor;
        Tamb += sum;
      }
    }
    
    // get average temperature
    Tamb /= (NX_ * NY_);
    
    // for each cell compute buoyancy force
    for (int i = 1; i <= NX_; i++){
      for (int j = 1; j <= NY_; j++){
        sum = dens[0][IDX(i, j)] + dens[1][IDX(i, j)] + dens[2][IDX(i, j)];
        sum /= sum_factor;
        force_buoy[IDX(i, j)] = a * sum  -b * (sum - Tamb);
      }
    }
  }

  
  
  //----------------------------------------------------------------------------
  // constrainValues();
  //----------------------------------------------------------------------------
  private final void constrainValues(GLTexture tex, float min, float max){
    TEX_FILTER_constrainValues_.setParameterValue("min", min);
    TEX_FILTER_constrainValues_.setParameterValue("max", max);
    TEX_FILTER_constrainValues_.apply(tex, tex);
  }
  
  
  
  
  
  //----------------------------------------------------------------------------
  // addSource();
  //----------------------------------------------------------------------------
  private final void addSource(GLTexture dst, GLTexture src, float factor){
    TEX_FILTER_addSource_.setParameterValue("timestep", factor);
    TEX_FILTER_addSource_.apply( new GLTexture[]{dst, src}, dst);
  }
  
  //----------------------------------------------------------------------------
  // applySource();
  //----------------------------------------------------------------------------
  private final void applySource(GLTexture dst, GLTexture src, float factor){
    TEX_FILTER_applySource_.apply(src, dst);
  }
  
  //----------------------------------------------------------------------------
  // applySource();
  //----------------------------------------------------------------------------
  private final void slow(GLTexture dst, float factor){
    TEX_FILTER_slow_.setParameterValue("slow_factor", factor);
    TEX_FILTER_slow_.apply(dst, dst);
  }
  
  
  
  //----------------------------------------------------------------------------
  // project();
  //----------------------------------------------------------------------------
  private final void project(GLTexture x, GLTexture y, 
                             GLTexture p, GLTexture div, 
                             GLTexture objects){
    project_factor_ = (NX_+NY_)*.5f;
    TEX_FILTER_project_STEP1_.setParameterValue("h", 1f/project_factor_);
    TEX_FILTER_project_STEP1_.apply(new GLTexture[]{ x, y, objects}, 
                                    new GLTexture[]{ div, p } );
        
    setBounds(0, div, objects);
    setBounds(0,   p, objects);
   
                                
    linearSolver(iterations_, 0, p, div, 1, 4, objects);
    
    TEX_FILTER_project_STEP2_.setParameterValue("h", project_factor_);
    TEX_FILTER_project_STEP2_.apply(new GLTexture[]{ x, y, p, objects}, 
                                    new GLTexture[]{ x, y } );
                     
    setBounds(1, x, objects);
    setBounds(2, y, objects);
  }
  
  //----------------------------------------------------------------------------
  // setBounds();
  //----------------------------------------------------------------------------
  private final void setBounds(int b, GLTexture d, GLTexture objects){
    TEX_FILTER_setBounds_.setParameterValue("b", b);  
    TEX_FILTER_setBounds_.setParameterValue("closed", 0);  
    TEX_FILTER_setBounds_.apply(new GLTexture[]{ d, objects}, 
                                new GLTexture[]{ d } );
  }
  
  private final void diffuse(int b, 
                             GLTexture c, GLTexture c0, 
                             float diff, GLTexture objects){
    float a = timestep_ * diff * NX_ * NY_ ;
    linearSolver(iterations_diffuse_, b, c, c0, a, 1 + 4 * a, objects);
  }
  

  
  //----------------------------------------------------------------------------
  // linearSolver(); - JACOBI
  //----------------------------------------------------------------------------
  private final void linearSolver(int iterations, int b, 
                                  GLTexture x, GLTexture x0, 
                                  float a, float c, GLTexture objects){
    TEX_FILTER_linearSolver_Jacobi_.setParameterValue("a", a);  
    TEX_FILTER_linearSolver_Jacobi_.setParameterValue("c", 1f/c);  
    
    TEX_FILTER_setBounds_.setParameterValue("b", b);    
    
    

    TEX_FILTER_linearSolver_Jacobi_.beginIterativeMode();
    for (int it = 0; it < iterations; it++){
      TEX_FILTER_linearSolver_Jacobi_.apply( new GLTexture[]{ x, x0, objects},  
                                             new GLTexture[]{ x } );

      TEX_FILTER_setBounds_.apply(new GLTexture[]{ x, objects}, 
                                  new GLTexture[]{ x } );
    }
    TEX_FILTER_linearSolver_Jacobi_.endIterativeMode();
  }
  
  
  
  
  //----------------------------------------------------------------------------
  // advect();
  //----------------------------------------------------------------------------
  private final void advect(int b, 
                            GLTexture d,  GLTexture d0, 
                            GLTexture du, GLTexture dv, 
                            GLTexture objects){
    
    float dt0 = timestep_ * (NX_+NY_)*.5f;  
    
    TEX_FILTER_advect_.setParameterValue("dt0", dt0);       
    TEX_FILTER_advect_.apply(new GLTexture[]{ d0, du, dv, objects}, 
                             new GLTexture[]{ d } );
                             
    TEX_FILTER_setBounds_.setParameterValue("b", b);  
    TEX_FILTER_setBounds_.apply(new GLTexture[]{ d, objects}, 
                                new GLTexture[]{ d } );
  }
  



  
  //----------------------------------------------------------------------------
  // visualize();
  //----------------------------------------------------------------------------
  private final void visualize(){
    float param_dst_size[]           = {TEX_densityMap_.width, TEX_densityMap_.height};
    int   param_smooth               = b_smooth_density_map_ ? 1 : 0;
    int   param_with_border          = 1;
    
    TEX_FILTER_generateDensityMap_.setParameterValue("dst_size", param_dst_size);
    
    TEX_FILTER_generateDensityMap_.setParameterValue("smooth_texture", param_smooth);
    TEX_FILTER_generateDensityMap_.setParameterValue("with_border", param_with_border); // 1 = true, 0 = false;
    TEX_FILTER_generateDensityMap_.setParameterValue("col_objects_border", param_col_objects_border );
//    TEX_FILTER_generateDensityMap_.setParameterValue("col_objects_body",   col_objects_body );
    
    TEX_FILTER_generateDensityMap_.apply( 
        new GLTexture[]{ TEX_density_[0], TEX_density_[1], TEX_density_[2] , TEX_objects_, TEX_background_}, 
        new GLTexture[]{ TEX_densityMap_ } );
  }
  
  
  
  
  
  
  //----------------------------------------------------------------------------
  // swap();
  //----------------------------------------------------------------------------
  GLTexture TEX_tmp_;
  private final void swapV() { 
    TEX_tmp_     = TEX_vVel_; 
    TEX_vVel_    = TEX_vVelOld_; 
    TEX_vVelOld_ = TEX_tmp_;
  }
  private final void swapU() { 
    TEX_tmp_     = TEX_uVel_; 
    TEX_uVel_    = TEX_uVelOld_; 
    TEX_uVelOld_ = TEX_tmp_;
  }
  
  private final void swapD(int id) { 
    TEX_tmp_            = TEX_density_[id]; 
    TEX_density_[id]    = TEX_densityOld_[id]; 
    TEX_densityOld_[id] = TEX_tmp_;
  }
  
  
  
  
  
  /*
  
  
  //----------------------------------------------------------------------------
  // addQuader();
  //----------------------------------------------------------------------------
  public final void addQuader(int posx, int posy, int sizex, int sizey){
    int offset = 0;
    int xlow = posx;
    int xhig = posx + sizex;
    int ylow = posy;
    int yhig = posy + sizey;
  
    for (int x = xlow-offset ; x < xhig+offset ; x++) {
      for (int y = ylow-offset ; y < yhig+offset ; y++) {
        if( x < 0 || x >= NX_+2 || y < 0 || y >= NY_+2 )
          continue;
          objects_[IDX(x, y)] = 2; // border of object = 2
      }
    } 
//    for (int x = xlow ; x < xhig ; x++) {
//      for (int y = ylow ; y < yhig ; y++) {
//        if( x < 0 || x >= NX_+2 || y < 0 || y >= NY_+2 )
//          continue;
//        objects_[IDX(x, y)] = 1;      // inner part of object = 1
//      }
//    }
//    cleanObjectsArea(xlow-1 , ylow-1, sizex+2, sizey+2);
  }
*/


  
/*
  //----------------------------------------------------------------------------
  // removeQuader();
  //----------------------------------------------------------------------------
  public final void removeQuader(int posx, int posy, int sizex, int sizey){
    int xlow = posx;
    int xhig = posx + sizex;
    int ylow = posy;
    int yhig = posy + sizey;
  
    for (int x = xlow ; x < xhig ; x++) {
      for (int y = ylow ; y < yhig ; y++) {
        if( x < 0 || x >= NX_+2 || y < 0 || y >= NY_+2 )
          continue;
        objects_[IDX(x, y)] = 0;     
      }
    } 
//    cleanObjectsArea(xlow-2 , ylow-2, sizex+4, sizey+4);
//    for (int x = xlow-1 ; x < xhig+1 ; x++){
//      if( x < 0 || x >= NX_+2 || ylow-1 < 0 || yhig >= NY_+2 )
//        continue;
//      if( objects_[IDX(x, ylow-1)] == 1 ) objects_[IDX(x, ylow-1)] = 2;
//      if( objects_[IDX(x, yhig-0)] == 1 ) objects_[IDX(x, yhig-0)] = 2;
//    }
//    
//    for (int y = ylow-1 ; y < yhig+1 ; y++){
//      if( xlow-1 < 0 || xhig >= NX_+2 || y < 0 || y >= NY_+2 )
//        continue;
//      if( objects_[IDX(xlow-1, y)] == 1 ) objects_[IDX(xlow-1, y)] = 2;
//      if( objects_[IDX(xhig+0, y)] == 1 ) objects_[IDX(xhig+0, y)] = 2;
//    }
  }
  */
  /*
  private final void cleanObjectsArea(int posx, int posy, int sizex, int sizey){
    int xlow = posx;
    int xhig = posx + sizex;
    int ylow = posy;
    int yhig = posy + sizey;
    for (int x = xlow ; x < xhig ; x++) {
      for (int y = ylow ; y < yhig ; y++) {
        if( x < 0 || x >= NX_+2 || y < 0 || y >= NY_+2 )
          continue;
        int id = getObjectId(x, y);      // inner part of object = 1
        if( id != 0){
          int count = 0;
          if( getObjectId(x+1, y+0) == 0) count++;
          if( getObjectId(x-1, y+0) == 0) count++;    
          if( getObjectId(x+0, y+1) == 0) count++;    
          if( getObjectId(x+0, y-1) == 0) count++;  
          if( count == 0 ) objects_[IDX(x, y)] = 1;
//          if( count == 0 ){ 
//            if( id == 1) objects_[IDX(x, y)] = 2;
//            else objects_[IDX(x, y)] = 1;
          
//          }
        }
      }
    }
  }
  
*/  
  
  

  
}