/////////////////////////////////////////////////////////////////////////////////////////////////////////////
// createFluidSolver();
//
Fluid2D createFluid(int type) {

  Fluid2D fluid_tmp = null;

  if ( type == 0) fluid_tmp = new Fluid2D_GPU(this, fluid_size_x, fluid_size_y);
  if ( type == 1) fluid_tmp = new Fluid2D_CPU(this, fluid_size_x, fluid_size_y);

  fluid_tmp.setParam_Timestep  ( 0.10f );
  fluid_tmp.setParam_Iterations( 16 );
  fluid_tmp.setParam_IterationsDiffuse(4);
  fluid_tmp.setParam_Viscosity ( 0.0000010000f );
  fluid_tmp.setParam_Diffusion ( 0.0000001f );
  fluid_tmp.setParam_Vorticity ( 1.0f );
  fluid_tmp.processDensityMap  ( true );
  fluid_tmp.processDiffusion   ( true );
  fluid_tmp.processViscosity   ( true );
  fluid_tmp.processVorticity   ( true );
  fluid_tmp.processDensityMap  ( true );
  fluid_tmp.setObjectsColor    (1, 1, 1, 1); 
  return fluid_tmp;
}



/////////////////////////////////////////////////////////////////////////////////////////////////////////////
// reset();
//
public void reset(Fluid2D fluid2d) {
  fluid2d.reset();
  fluid2d.setDensityMap(output_densityMap);
  initDensity(fluid2d, input_density_values);
  
//  int sx = fluid2d.getSizeXTotal();
//  int sy = fluid2d.getSizeYTotal();
//  int s = 3;
//  for(int y = 0; y < 10; y++){
//    for(int x= 0; x < 5; x++){
//      addObject(fluid2d, (int)random(60, 100), (int)random(20, sy-20), 2, 2, 0);
//    }
//  }
//  addObject(fluid2d, 0, 0, sx, 2, 0);
//  addObject(fluid2d, 0, sy-2, sx, 2, 0);
//  addObject(fluid2d, 15, 45, 4, 16, 0);
}



/////////////////////////////////////////////////////////////////////////////////////////////////////////////
// initDensity();
//
private void initDensity(Fluid2D fluid2d, PImage img) {
  if ( img == null) 
    return;
  int sx = fluid2d.getSizeXTotal();
  int sy = fluid2d.getSizeYTotal();

//  fluid2d.setTextureBackground(img);
//  fluid2d.setTextureBackground(null);

  img.resize(sx, sy);
  img.loadPixels();

  for (int y = 0; y < sy; y++) {
    for (int x = 0; x < sx; x++) { 
      int col = img.pixels[x+sx*y];
      fluid2d.addDensity( 0, x, y, ((col >> 16) & 0xFF) / 255.0f);
      fluid2d.addDensity( 1, x, y, ((col >>  8) & 0xFF) / 255.0f);
      fluid2d.addDensity( 2, x, y, ((col >>  0) & 0xFF) / 255.0f);
    }
  }
}

