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

/**
 *
 * this class creates a 2D-fluid-solver that runs completely on the CPU.<br>
 * 
 * @author      Thomas Diewald
 *  
 *  
 */
public final class Fluid2D_CPU extends Fluid2D{
  private boolean use_tex_background = false;
  private PImage TEX_background_;
  private PImage TEX_densityMap_;
  
  float[][] density_constrained;
  int[][] TEX_background_rgb_;
  
  
  public Fluid2D_CPU(PApplet p5parent, int nx, int ny) {
    super(p5parent, nx, ny);
  }
  
  
  
  
  @Override
  protected final PImage privateGetTextureBackground_byRef(){
    return TEX_background_;
  }
  
  @Override
  protected final void privateSetTextureBackground(PImage img){

    TEX_background_ = img;
    if( TEX_background_ == null ){
      TEX_background_rgb_ = null;
      reallocBackgroundRGB(TEX_densityMap_);
      use_tex_background = false;
      return;
    }
    use_tex_background = true;
    TEX_background_.loadPixels();
    TEX_densityMap_.loadPixels();
    
    int bgsx = TEX_densityMap_.width; //buffer size x
    int bgsy = TEX_densityMap_.height; //buffer size y
    
    float idx_scale_x = img.width /(float) bgsx;
    float idx_scale_y = img.height/(float) bgsy;
    
    for(int y = 0; y < bgsy; y++){
      for(int x = 0; x < bgsx; x++){
        int idxb = x + y*bgsx;
        int idx_img = (int)(x*idx_scale_x) + (int)(y*idx_scale_y)*img.width;
        int bg_col = TEX_background_.pixels[idx_img];
        TEX_background_rgb_[0][idxb] = ( bg_col>>16)& 0xFF;
        TEX_background_rgb_[1][idxb] = ( bg_col>> 8)& 0xFF;
        TEX_background_rgb_[2][idxb] = ( bg_col>> 0)& 0xFF;
      }
    }
  }
  @Override
  protected final void privateSetTextureBackgroundColor(int r, int g, int b){
    if( r < 0 ) r = 0; if(r > 255) r = 255;
    if( g < 0 ) g = 0; if(g > 255) g = 255;
    if( b < 0 ) b = 0; if(b > 255) b = 255;
    
    if( r == 0 && g == 0 && b == 0){
      use_tex_background = false;
      return;
    }
    clear(TEX_background_rgb_[0], r);
    clear(TEX_background_rgb_[1], g);
    clear(TEX_background_rgb_[2], b);
    use_tex_background = true;
    
//    if( TEX_background_ == null){
//      TEX_background_ = p5parent_.createImage(1, 1, PApplet.RGB); //just a fake
//    }
//    
//    TEX_background_.loadPixels();
//    for(int i = 0; i < TEX_background_.pixels.length; i++){
//      TEX_background_.pixels[i] = (0xFF)<<24 | (r&0xFF)<<16 | (g&0xFF)<<8 | (b&0xFF);
//    }
//    TEX_background_.updatePixels();
//    privateSetTextureBackground(TEX_background_);
  }
  
  
  @Override
  protected final PImage privateGetDensityMap(){
    return TEX_densityMap_;
  }
  
  @Override
  protected final void privateSetDensityMap(PImage TEX_densityMap){
    TEX_densityMap_ = TEX_densityMap;
    setTextureBackground(TEX_background_);
  }
  
  

  
  
  

  @Override
  protected float[] privateGetBufferDensity_byRef(int density_layer) {
    return density_[density_layer];
  }

  @Override
  protected float[] privateGetBufferVelocityU_byRef() {
    return uVel_;
  }

  @Override
  protected float[] privateGetBufferVelocityV_byRef() {
    return vVel_;
  }

  @Override
  protected void privateReset() {
    // allocate array-buffers
    density_    = new float[number_of_density_layers_][BUFFER_SIZE_];
    density_constrained = new float[number_of_density_layers_][BUFFER_SIZE_];
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
    
    TEX_densityMap_  = p5parent_.createImage((NX_ + 2), (NY_ + 2), PApplet.RGB);
//    TEX_background_  = p5parent_.createImage((NX_ + 2), (NY_ + 2), PApplet.RGB);
    
    reallocBackgroundRGB(TEX_densityMap_);
  }
  
  private final void reallocBackgroundRGB(PImage dens_map){
    TEX_background_rgb_ = new int[number_of_density_layers_][dens_map.pixels.length];
    clear(TEX_background_rgb_[0], 0);
    clear(TEX_background_rgb_[1], 0);
    clear(TEX_background_rgb_[2], 0);;
  }
  
  
  

  @Override
  protected void privateUpdate() {
    addNewValues();
    velocitySolver();
    densitySolver();
    
    if( b_generate_density_map_ ){
      visualize();
    }
  }
  
  
  
  //----------------------------------------------------------------------------
  // addNewValues();
  //----------------------------------------------------------------------------
  private final void addNewValues(){
    addSource(uVel_, uVelOld_, 1.0f);
    addSource(vVel_, vVelOld_, 1.0f);

    for(int i = 0; i < density_.length; i++)
      addSource(density_[i],  densityOld_[i], 1.0f);
   
//    clear(uVelOld_);
//    clear(vVelOld_);
//    for(int i = 0; i < density_.length; i++)
//      clear(density_[i]);
  }
  
  
  
  
  
  
  //----------------------------------------------------------------------------
  //  velocitySolver();
  //----------------------------------------------------------------------------
  private final void velocitySolver(){
    
    if( b_vorticity_ ){
      vorticityConfinement(uVel_, vVel_, uVelOld_, vVelOld_, curl_, vorticity_, objects_);
      addSource(uVel_, uVelOld_, timestep_);
      addSource(vVel_, vVelOld_, timestep_);
//      clear(uVelOld_);
//      clear(vVelOld_); 
    }

    if( b_buoyancy_ ){
      System.err.println(this.getClass().getCanonicalName() +": 'buoyancy' is not implemented right now!!");
      //buoyancy(density_, vVelOld_);
      //addSource(vVel_, vVelOld_, timestep_);
    }
    
    if( b_viscosity_ ){
      swapU();
      swapV();
      diffuse(1, uVel_, uVelOld_, viscosity_, objects_);
      diffuse(2, vVel_, vVelOld_, viscosity_, objects_);
    }


    project(uVel_, vVel_, uVelOld_, vVelOld_, objects_);
    

    swapU(); 
    swapV();
 
    advect(1, uVel_, uVelOld_, uVelOld_, vVelOld_, objects_);
    advect(2, vVel_, vVelOld_, uVelOld_, vVelOld_, objects_);

    project(uVel_, vVel_, uVelOld_, vVelOld_, objects_);
    
    clear(uVelOld_, 0);
    clear(vVelOld_, 0);                     
  }
  
  
  //----------------------------------------------------------------------------
  // densitySolver();
  //----------------------------------------------------------------------------
  private final void densitySolver(){
    for(int i = 0; i < density_.length; i++){
      if( b_diffusion_ ){
        swapD(i);
        diffuse(0, density_[i], densityOld_[i], diffusion_, objects_);
      }
      
      swapD(i);
      advect(0, density_[i], densityOld_[i],  uVel_, vVel_, objects_);   
      constrainValues(density_[i], 0f, 2f);
      clear(densityOld_[i], 0);
    }
  }
  
  
  //----------------------------------------------------------------------------
  // constrainValues();
  //----------------------------------------------------------------------------
  private final void constrainValues(float[] values, float min, float max){
    for(int i = 0; i < BUFFER_SIZE_; i++){
      if( values[i] < min ) values[i] = min;
      if( values[i] > max ) values[i] = max;
    }
  }
  
  
  
  
  
  
  
  
  

  
  private final void vorticityConfinement(float[] uVel, float[] vVel, 
                                          float[] uVelOld, float[] vVelOld, 
                                          float[] curl, 
                                          float vorticity, 
                                          float[] objects){
    
    // Calculate magnitude of curl(u,v) for each cell. (|w|)
    for (int i = 1; i <= NX_; i++){
      for (int j = 1; j <= NY_; j++){
        if( objects[IDX(i, j)] > 0 ) continue;
        curl[IDX(i, j)] = Math.abs(curl(i, j, uVel, vVel));
      }
    }

    for (int i = 2; i < NX_; i++){
      for (int j = 2; j < NY_; j++){
        float dw_dx = (curl[IDX(i + 1, j)] - curl[IDX(i - 1, j)]) * 0.5f;
        float dw_dy = (curl[IDX(i, j + 1)] - curl[IDX(i, j - 1)]) * 0.5f;
        
        float length = (float) Math.sqrt(dw_dx * dw_dx + dw_dy * dw_dy) + 0.000001f;

        dw_dx /= length;
        dw_dy /= length;

        float v = curl(i, j, uVel, vVel);

        uVelOld[IDX(i, j)] = dw_dy * -v * vorticity;
        vVelOld[IDX(i, j)] = dw_dx *  v * vorticity;
      }
    }
  }
  
  private final float curl(int i, int j, float[] uVel, float[] vVel){
    float du_dy = (uVel[IDX(i, j + 1)] - uVel[IDX(i, j - 1)]) * 0.5f;
    float dv_dx = (vVel[IDX(i + 1, j)] - vVel[IDX(i - 1, j)]) * 0.5f;
    return du_dy - dv_dx;
  }
  
  private final void slow(float[] d, float visc) {
    float factor = 1.0f/(visc*timestep_/20.0f+1.0f);
    for (int i = 0; i<d.length; i++){
      d[i] *= factor;
    }
  }

  
  
  
  
  
  private final void addSource(float[] x, float[] x0, float factor){
    for (int i = 0; i < BUFFER_SIZE_; i++){
      x[i] += factor * x0[i];
    }
  }
  
  
  private final void project(float[] x, float[] y, float[] p, float[] div, float[] objects){
    float h = project_factor_; 
    for (int i = 1; i <= NX_; i++){
      for (int j = 1; j <= NY_; j++){
        if( objects[IDX(i, j)] > 0 ) continue;
        div[IDX(i, j)] = - 0.5f * (x[IDX(i+1, j)] - x[IDX(i-1, j)] + y[IDX(i, j+1)] - y[IDX(i, j-1)])  /  h;
        p  [IDX(i, j)] = 0;
      }
    }

    setBounds(0, div, objects);
    setBounds(0, p, objects);

    linearSolver(iterations_, 0, p, div, 1, 4, objects);

    for (int i = 1; i <= NX_; i++){
      for (int j = 1; j <= NY_; j++){
        if( objects[IDX(i, j)] > 0 ) continue;
        x[IDX(i, j)] -= 0.5f * h * (p[IDX(i+1, j)] - p[IDX(i-1, j)]);
        y[IDX(i, j)] -= 0.5f * h * (p[IDX(i, j+1)] - p[IDX(i, j-1)]);
      }
    }

    setBounds(1, x, objects);
    setBounds(2, y, objects);
  }
  
  
  
  
  private final void advect(int b, float[] d, float[] d0, float[] du, float[] dv, float[] objects){

    int i0, j0, i1, j1;
    float x, y, s0, t0, s1, t1;
    
    float x_max = NX_ + 0.5f;
    float y_max = NY_ + 0.5f;

    float dt0 = timestep_ * (NX_+NY_)*.5f;  

    for (int i = 1; i <= NX_; i++){
      for (int j = 1; j <= NY_; j++){
        if( objects[IDX(i, j)] > 0 ) continue;
        x = i - dt0 * du[IDX(i, j)];
        y = j - dt0 * dv[IDX(i, j)];

        if (x > x_max) x = x_max;
        if (x < 0.5)   x = 0.5f;

        i0 = (int) x;
        i1 = i0 + 1;

        if (y > y_max) y = y_max;
        if (y < 0.5)   y = 0.5f;

        j0 = (int) y;
        j1 = j0 + 1;

        s1 = x - i0;
        s0 = 1 - s1;
        t1 = y - j0;
        t0 = 1 - t1;

        d[IDX(i, j)] = s0 * (t0 * d0[IDX(i0, j0)] +
                             t1 * d0[IDX(i0, j1)])+ 
                       s1 * (t0 * d0[IDX(i1, j0)] + 
                             t1 * d0[IDX(i1, j1)]);
      }
    }
    setBounds(b, d, objects);
  }
  
  
  

  private final void diffuse(int b, float[] c, float[] c0, float diff, float[] objects){
    float a = timestep_ * diff * NX_ * NY_ ;
    linearSolver(iterations_diffuse_, b, c, c0, a, 1 + 4 * a, objects);
  }
   
   
   
   
  
  private final void linearSolver(int iterations, int b, float[] x, float[] x0, float a, float c, float[] objects){
   //linearSolver_GAUSS_SEIDL_improved( iterations, b, x, x0, a, c, objects);  
   linearSolver_JACOBI            ( iterations, b, x, x0, a, c, objects);  
   //linearSolver_JACOBI4shader      (iterations, b, x, x0, a, c, objects); 
  }
  

  private final void linearSolver_GAUSS_SEIDL_improved(int iterations, int b, float[] x, float[] x0, float a, float c, float[] objects){
    float inv_c = 1.0f/c;
    double wMax = 1.8;
    double wMin = 0.5;
    for (int it = 0; it < iterations; it++){
      float w = (float)Math.max((wMin-wMax)*it /60.0+wMax, wMin);
      for (int i = 1; i <= NX_; i++){
        for (int j = 1; j <= NY_; j++){
          int k = IDX(i, j);
          if( objects[k] > 0 ) continue;
          float jacobi_val = inv_c * (x0[k] + a * ( x[k-1] + x[k+1] + x[k-(NX_+2)] + x[k+(NX_+2)]));
          x[k]             = x[k] + w*( jacobi_val - x[k]);
        }
      }
      setBounds(b, x, objects);
    }
  }
  


//  private final linearSolver_JACOBI4Shader(int iterations, int b, float[] x, float[] x0, float a, float c, int[] objects){
//   float inv_c = 1.0f/c;
//    for (int it = 0; it < iterations; it++){
//      for (int i = 1; i <= NX_; i++){
//        for (int j = 1; j <= NY_; j++){
//          int k = IDX(i, j);
//          if( objects[k] != 0 ) continue;
//          tmpx_[k] = inv_c *(x0[k] + a * ( x[k-1]       + x[k+1] +
//                                           x[k-(NX_+2)] + x[k+(NX_+2)]) ) 
//        }
//      }
//      System.arraycopy(tmpx_, 0, x, 0, tmpx_.length);
//      setBounds(b, x, objects);
//    }
//  }
  
  private final void linearSolver_JACOBI(int iterations, int b, float[] x, float[] x0, float a, float c, float[] objects){
    float inv_c = 1.0f/c;
    for (int it = 0; it < iterations; it++){
      for (int i = 1; i <= NX_; i++){
        for (int j = 1; j <= NY_; j++){
          int k = IDX(i, j);
          if( objects[k] > 0 ) continue;
          x[k] = inv_c * (x0[k] + a * ( x[k-1]    + x[k+1] +
                                        x[k-(NX_+2)] + x[k+(NX_+2)]));
        }
      }
      setBounds(b, x, objects);
    }
  }
  
  

  
  
  

  
  
  
  
  
  
  
  
  
  
  
  
  
  private final void setBounds(int b, float[] d, float[] objects ){
    int xSize = NX_+2;
    int ySize = NY_+2;
    
    // left-right border
//    for (int y = 1; y <= NY_; y++) {
//      d[IDX(    0, y)] =   (b == 1) ? -d[IDX(  1, y)]  :  d[IDX(  1, y)];
//      d[IDX(NX_+1, y)] =   (b == 1) ? -d[IDX(NX_, y)]  :  d[IDX(NX_, y)];
//    }
    
    // top-bottom, border
//    for (int x = 1; x <= NX_; x++) {
//      d[IDX(x,     0)] =   (b == 2) ? -d[IDX(x,   1)]  :  d[IDX(x,   1)];
//      d[IDX(x, NY_+1)] =   (b == 2) ? -d[IDX(x, NY_)]  :  d[IDX(x, NY_)];
//    }
//    
//    //corners
//    d[IDX(    0,     0)] = 0.5f * (d[IDX(    0,     1)] + d[IDX(  1,   0)]);
//    d[IDX(    0, NY_+1)] = 0.5f * (d[IDX(    1, NY_+1)] + d[IDX(  0, NY_)]);
//    d[IDX(NX_+1,     0)] = 0.5f * (d[IDX(NX_+1,     1)] + d[IDX(NX_,   0)]);
//    d[IDX(NX_+1, NY_+1)] = 0.5f * (d[IDX(NX_+1, NY_+1)] + d[IDX(NX_, NY_)]);
    set_bnd_objekt(b, d, objects);
  }
  
  
  private final void set_bnd_objekt (int b, float[] x, float[] objects ){ // b == 1 bei u ; b == 2 bei v
    for (int i=1 ; i <= NX_ ; i++ ) {
      for (int j=1 ; j <= NY_ ; j++ ) {
        if ( objects[IDX(i, j)] == 2.0 ) {
          if ( b == 1 ) {
            // inverse horizontal velocity at vertical object border
            if (objects[IDX(i-1, j)]==0) x[IDX(i, j)] =  -x[IDX(i-1, j)];
            if (objects[IDX(i+1, j)]==0) x[IDX(i, j)] =  -x[IDX(i+1, j)];
          } 
          else if ( b == 2 ) {
            // inverse vertical velocity at horizontal object border
            if (objects[IDX(i, j-1)]==0) x[IDX(i, j)] =  -x[IDX(i, j-1)];
            if (objects[IDX(i, j+1)]==0) x[IDX(i, j)] =  -x[IDX(i, j+1)];
          } 
          else if (b == 0 ) {
            // same density as active neighbour for egde-border, 
            // average of two active neighbours for corner-border
            int count = 0; 
            float tmp = 0.0f;
            x[IDX(i, j)] = 0;
            if (objects[IDX(i-1, j)]==0) { tmp += x[IDX(i-1, j)]; count++; }
            if (objects[IDX(i+1, j)]==0) { tmp += x[IDX(i+1, j)]; count++; }
            if (objects[IDX(i, j-1)]==0) { tmp += x[IDX(i, j-1)]; count++; }
            if (objects[IDX(i, j+1)]==0) { tmp += x[IDX(i, j+1)]; count++; }
            if( count == 0){
              x[IDX(i, j)] = 0; 
            } else {
              x[IDX(i, j)] = tmp/count; 
            }
          }
        }
      }
    }
  }
  
  
  
  
 

  //----------------------------------------------------------------------------
  // visualize();
  //----------------------------------------------------------------------------
  private final void visualize(){
//    int   param_with_border          = 1;
    int   col_objects[]         = { (int)(param_col_objects_border[0]*255.0),
                                    (int)(param_col_objects_border[1]*255.0),
                                    (int)(param_col_objects_border[2]*255.0),
                                    (int)(param_col_objects_border[3]*255.0)
                                  };

    constrainAndCopy(density_[0], density_constrained[0], 0, 255);
    constrainAndCopy(density_[1], density_constrained[1], 0, 255);
    constrainAndCopy(density_[2], density_constrained[2], 0, 255);
    
    float[][] dns = density_constrained;
    
    int src_width  = (NX_ + 2);
    int src_height = (NY_ + 2);
    
    int offset = b_smooth_density_map_ ? 1 : 0;
    
    int src_width_min1  = src_width  - offset;
    int src_height_min1 = src_height - offset;
    float dens_width_inv  = 1f/(float)TEX_densityMap_.width;
    float dens_height_inv = 1f/(float)TEX_densityMap_.height;
    float size_fac_x = dens_width_inv  * src_width_min1;
    float size_fac_y = dens_height_inv * src_height_min1;
    
    int r, g, b, a;
    int idx00, idx10, idx01, idx11;
    float px, py, pxi, pyi;
    float x_density, y_density;
    int x_pos, y_pos, y_pos_0, y_pos_1;
    float col_p = 1f/255f;
    
    TEX_densityMap_.loadPixels();

    for(int y = 0; y <  TEX_densityMap_.height; y++){
      
      y_density = y*size_fac_y;
      y_pos = (int)y_density;
      y_pos_0 = src_width*(y_pos);
      y_pos_1 = src_width*(y_pos+1);
      py  = y_density - y_pos;
      pyi = 1f-py;
      
      for(int x = 0; x <  TEX_densityMap_.width; x++){
        int pix_index = x + TEX_densityMap_.width*y;
        x_density = x*size_fac_x;
        x_pos = (int)x_density;

        px  = x_density - x_pos;
        pxi = 1f-px;
        
        idx00 =  x_pos    + y_pos_0;
        idx10 = (x_pos+1) + y_pos_0;
        idx01 =  x_pos    + y_pos_1;
        idx11 = (x_pos+1) + y_pos_1;
        
        // smooth the pixels
        if( b_smooth_density_map_ ) {
          r = (int) (pyi*( dns[0][idx00]*pxi + dns[0][idx10]*px) +  py*(dns[0][idx01]*pxi + dns[0][idx11]*px));
          g = (int) (pyi*( dns[1][idx00]*pxi + dns[1][idx10]*px) +  py*(dns[1][idx01]*pxi + dns[1][idx11]*px));
          b = (int) (pyi*( dns[2][idx00]*pxi + dns[2][idx10]*px) +  py*(dns[2][idx01]*pxi + dns[2][idx11]*px));
          a = 0xFF;
        } else {
          r = (int)dns[0][idx00];
          g = (int)dns[1][idx00];
          b = (int)dns[2][idx00];
          a = 0xFF;
        }
        
        // add background, if existing
        if( use_tex_background){
          float alpha_value_percent_in = (255f-PApplet.max(r, g, b)) *col_p;
          r += TEX_background_rgb_[0][pix_index] * alpha_value_percent_in;
          g += TEX_background_rgb_[1][pix_index] * alpha_value_percent_in;
          b += TEX_background_rgb_[2][pix_index] * alpha_value_percent_in;
        }
        
        // add objects, if existing
        if( objects_[idx00] > 0){
          // not that nice, but as soon as the the object has a little alpha, than
          // it is set completely transparent
          if( col_objects[3] > 0 ){
            r = col_objects[0];
            g = col_objects[1];
            b = col_objects[2];
            a = col_objects[3];
          } else {
            r = TEX_background_rgb_[0][pix_index];
            g = TEX_background_rgb_[1][pix_index];
            b = TEX_background_rgb_[2][pix_index];
          }
        } 
        
        

        int pixel_col =   (a & 0xFF) << 24 | 
                          (r & 0xFF) << 16 |
                          (g & 0xFF) <<  8 |
                          (b & 0xFF);
        TEX_densityMap_.pixels[pix_index] = pixel_col;
        
      }
    }
    TEX_densityMap_.updatePixels();
  }
  
  
  
  
  
  private final void constrainAndCopy(float[] val_src, float[] val_dst, float min, float max){
    float val;
    for(int i = 0; i < val_dst.length; i++){
      val = val_src[i]*255;
      val_dst[i] = (val < min) ? min : ((val > max) ? max : val);
    }
  }
  
  
  //----------------------------------------------------------------------------
  // swap();
  //----------------------------------------------------------------------------
  float tmp_[];
  private final void swapV() { 
    tmp_     = vVel_; 
    vVel_    = vVelOld_; 
    vVelOld_ = tmp_;
  }
  private final void swapU() { 
    tmp_     = uVel_; 
    uVel_    = uVelOld_; 
    uVelOld_ = tmp_;
  }
  
  private final void swapD(int id) { 
    tmp_            = density_[id]; 
    density_[id]    = densityOld_[id]; 
    densityOld_[id] = tmp_;
  }
  

}
