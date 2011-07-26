/////////////////////////////////////////////////////////////////////////////////////////////////////////////
// createFluidSolver();
//
Fluid2D createFluid(int type) {

  Fluid2D fluid_tmp = null;

  if ( type == 0) fluid_tmp = new Fluid2D_GPU(this, fluid_size_x, fluid_size_y);
  if ( type == 1) fluid_tmp = new Fluid2D_CPU(this, fluid_size_x, fluid_size_y);

  fluid_tmp.setParam_Timestep  ( 0.08f );
  fluid_tmp.setParam_Iterations( 16 );
  fluid_tmp.setParam_IterationsDiffuse(8);
  fluid_tmp.setParam_Viscosity ( 0.0f );
  fluid_tmp.setParam_Diffusion ( 0.000052f );
  fluid_tmp.setParam_Vorticity ( 2.0f );
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
  fluid2d.setTextureBackgroundColor(0, 0, 0);
  generateSpiralObstacles(fluid2d, spiral_obstacles);
}




