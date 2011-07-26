float fac = 4.0;
float f_rot = 0.05/fac;
float f_rad = 1.5/(float)cell_size/fac;
float spiral_size = 1200;
boolean spiral_obstacles = true;

public void generateSpiralObstacles(Fluid2D fluid2d, boolean mode){
  float coords[] = new float[2];
  float center[] = {fluid2d.getSizeXTotal()/2, fluid2d.getSizeXTotal()/2};
  for(int i = 0; i < spiral_size; i++){

    float rotation = i*f_rot;
    float radius   = i*f_rad;
    //getCoords(coords, center, radius, rotation );
    //addObject(fluid, (int)(coords[0]+.5), (int)(coords[1]+.5), 1, 1, 0);

    getCoords(coords, center, radius, rotation-PI );
    addObject(fluid2d, (int)(coords[0]+.5), (int)(coords[1]+.5), 1, 5, (mode)?0:1);
  }
}


public void generateSpiralInfluence(Fluid2D fluid2d){
  float coords_s[] = new float[2];
  float coords_e[] = new float[2];
  float center[] = {fluid.getSizeXTotal()/2, fluid.getSizeYTotal()/2};
  int step = 50;
  float rotation, radius;
  for(int i = 0; i < spiral_size/step; i++){

    rotation = (i*step)*f_rot;
    radius   = (i*step)*f_rad;
    getCoords(coords_s, center,radius, rotation-PI/2.0 );
    
    rotation = ((i+1)*step)*f_rot;
    radius   = ((i+1)*step)*f_rad;
    getCoords(coords_e, center,radius, rotation-PI/2.0 );
    
    float velx = coords_e[0] - coords_s[0];
    float vely = coords_e[1] - coords_s[1];
    velx *= .05;
    vely *= .05;
    if( !( keyPressed && key == 'd') && i > 2 ){
      fluid.addDensity(0, (int)coords_s[0], (int)coords_s[1], .8);
      fluid.addDensity(1, (int)coords_s[0], (int)coords_s[1], 0);
      fluid.addDensity(2, (int)coords_s[0], (int)coords_s[1], rotation/5.0);
    }
    if( !( keyPressed && key == 'v') ){
      fluid.addVelocity((int)coords_s[0], (int)coords_s[1], velx, vely);
    }
  }
  setVel( fluid, 1*cell_size, 1*cell_size, 3, height, .02, 0);
}


void getCoords(float coords[], float center[], float radius, float rotation){
  coords[0] = center[0]+radius*cos(rotation) ;
  coords[1] = center[1]+radius*sin(rotation) ;
}
