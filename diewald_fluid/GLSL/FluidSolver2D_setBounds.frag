//------------------------------------------------------------------------------
///
/// author: thomas diewald
///
/// GLSL - fragment-shader: FluidSolver2D_setBounds.frag
///
///
/// date:        25.07.2011
/// last edited: 25.07.2011
///
//------------------------------------------------------------------------------





uniform sampler2D tex1_rw_d;
uniform sampler2D tex2_objects;

uniform vec2 inv_size;
uniform int b;
uniform int closed;


const vec2 off = vec2(.5, .5);

////////////////////////////////////////////////////////////////////////////////
// forward declarations
//
bool isBorder(in vec2 frag_coord, in vec2 frame_size, in float border_size);


////////////////////////////////////////////////////////////////////////////////
// MAIN
//
void main(void) {    

  vec2 co   = gl_TexCoord[0].st;
  vec2 c_le = co + vec2(-inv_size.x, 0);
  vec2 c_ri = co + vec2(+inv_size.x, 0);
  vec2 c_to = co + vec2(0, -inv_size.y);
  vec2 c_bo = co + vec2(0, +inv_size.y);
  
  int objects_value    = int( texture2D(tex2_objects, co  )[0] );
  int objects_value_le = int( texture2D(tex2_objects, c_le)[0] );
  int objects_value_ri = int( texture2D(tex2_objects, c_ri)[0] );
  int objects_value_to = int( texture2D(tex2_objects, c_to)[0] );
  int objects_value_bo = int( texture2D(tex2_objects, c_bo)[0] );
  

  
  // corners
  /*
  if((co.x <=     inv_size.x) && 
     (co.y <=     inv_size.y))  gl_FragData[0].x = (texture2D(tex1_rw_d, c_bo)[0] + texture2D(tex1_rw_d, c_ri)[0]) * .5;
     
  if((co.x >= 1.0-inv_size.x) && 
     (co.y <=     inv_size.y))  gl_FragData[0].x = (texture2D(tex1_rw_d, c_bo)[0] + texture2D(tex1_rw_d, c_le)[0]) * .5;
     
  if((co.x <=     inv_size.x) && 
     (co.y >= 1.0-inv_size.y))  gl_FragData[0].x = (texture2D(tex1_rw_d, c_to)[0] + texture2D(tex1_rw_d, c_ri)[0]) * .5;
     
  if((co.x >= 1.0-inv_size.x) && 
     (co.y >= 1.0-inv_size.y))  gl_FragData[0].x = (texture2D(tex1_rw_d, c_to)[0] + texture2D(tex1_rw_d, c_le)[0]) * .5;
  */



  /// manage objects

  if( objects_value == 2) {
    if (b==1) {
       // inverse horizontal velocity at vertical object border
       if ( objects_value_le == 0 ) gl_FragData[0].x =  -texture2D(tex1_rw_d, c_le)[0];
       if ( objects_value_ri == 0 ) gl_FragData[0].x =  -texture2D(tex1_rw_d, c_ri)[0];
    } 
    else if (b == 2 ) {
      // inverse vertical velocity at horizontal object border
      if ( objects_value_to == 0 ) gl_FragData[0].x =  -texture2D(tex1_rw_d, c_to)[0];
      if ( objects_value_bo == 0 ) gl_FragData[0].x =  -texture2D(tex1_rw_d, c_bo)[0];
    } 
    else if (b == 0) {
    
      // same density as active neighbour for egde-border, average of 2 active neighbours for corner-border
      float tmp = 0.0;
      float count = 0.0;
      if ( objects_value_le == 0 ) { tmp += texture2D(tex1_rw_d, c_le)[0]; count++; }
      if ( objects_value_ri == 0 ) { tmp += texture2D(tex1_rw_d, c_ri)[0]; count++; }
      if ( objects_value_to == 0 ) { tmp += texture2D(tex1_rw_d, c_to)[0]; count++; }
      if ( objects_value_bo == 0 ) { tmp += texture2D(tex1_rw_d, c_bo)[0]; count++; }
      if( count == 0.0){
        gl_FragData[0].x = 0.0;
      } else 
        gl_FragData[0].x = tmp / count;

    }
    
  } 
  else if (objects_value == 1){
    gl_FragData[0].x = 0.0;
  } else {
   
   if( closed == 1){
         if(co.x <=     inv_size.x) gl_FragData[0].x = (b == 1) ? -texture2D(tex1_rw_d, c_ri)[0] : texture2D(tex1_rw_d, c_ri)[0];
    else if(co.x >= 1.0-inv_size.x) gl_FragData[0].x = (b == 1) ? -texture2D(tex1_rw_d, c_le)[0] : texture2D(tex1_rw_d, c_le)[0];
    else if(co.y <=     inv_size.y) gl_FragData[0].x = (b == 2) ? -texture2D(tex1_rw_d, c_bo)[0] : texture2D(tex1_rw_d, c_bo)[0];
    else if(co.y >= 1.0-inv_size.y) gl_FragData[0].x = (b == 2) ? -texture2D(tex1_rw_d, c_to)[0] : texture2D(tex1_rw_d, c_to)[0];
    else  
    discard;  
    
    if((co.x <=     inv_size.x) && 
       (co.y <=     inv_size.y))  gl_FragData[0].x = (texture2D(tex1_rw_d, c_bo)[0] + texture2D(tex1_rw_d, c_ri)[0]) * .5;
       
    if((co.x >= 1.0-inv_size.x) && 
       (co.y <=     inv_size.y))  gl_FragData[0].x = (texture2D(tex1_rw_d, c_bo)[0] + texture2D(tex1_rw_d, c_le)[0]) * .5;
       
    if((co.x <=     inv_size.x) && 
       (co.y >= 1.0-inv_size.y))  gl_FragData[0].x = (texture2D(tex1_rw_d, c_to)[0] + texture2D(tex1_rw_d, c_ri)[0]) * .5;
       
    if((co.x >= 1.0-inv_size.x) && 
       (co.y >= 1.0-inv_size.y))  gl_FragData[0].x = (texture2D(tex1_rw_d, c_to)[0] + texture2D(tex1_rw_d, c_le)[0]) * .5;
     
    
    } else {
           if(co.x <=     inv_size.x) gl_FragData[0].x = 0.0;
      else if(co.x >= 1.0-inv_size.x) gl_FragData[0].x = 0.0;
      else if(co.y <=     inv_size.y) gl_FragData[0].x = 0.0;
      else if(co.y >= 1.0-inv_size.y) gl_FragData[0].x = 0.0;
      else  
      discard;  
    }
    
  }

}




////////////////////////////////////////////////////////////////////////////////
// isBorder()
//
bool isBorder(in vec2 frag_coord, in vec2 frame_size, in float border_size){
  vec2 bs_new = vec2(border_size, border_size) - off;
  return( frag_coord.x <=              bs_new.x || frag_coord.y <=              bs_new.y ||
          frag_coord.x >= frame_size.x-bs_new.x || frag_coord.y >= frame_size.y-bs_new.y );
}







/*

  private void setBounds(int b, float[] rw_d, int[] object){


    // left-right border
//    for (int y = 1; y <= NY_; y++) {
//      d[IDX(    0, y)] =   (b == 1) ? -d[IDX(  1, y)]  :  d[IDX(  1, y)];
//      d[IDX(NX_+1, y)] =   (b == 1) ? -d[IDX(NX_, y)]  :  d[IDX(NX_, y)];
//    }
    
    // top-bottom, border
    for (int x = 1; x <= NX_; x++) {
      rw_d[IDX(x,     0)] =   (b == 2) ? -rw_d[IDX(x,   1)]  :  rw_d[IDX(x,   1)];
      rw_d[IDX(x, NY_+1)] =   (b == 2) ? -rw_d[IDX(x, NY_)]  :  rw_d[IDX(x, NY_)];
    }
    
    //corners
    rw_d[IDX(    0,     0)] = 0.5 * (rw_d[IDX(    0,     1)] + rw_d[IDX(  1,   0)]);
    rw_d[IDX(    0, NY_+1)] = 0.5 * (rw_d[IDX(    1, NY_+1)] + rw_d[IDX(  0, NY_)]);
    rw_d[IDX(NX_+1,     0)] = 0.5 * (rw_d[IDX(NX_+1,     1)] + rw_d[IDX(NX_,   0)]);
    rw_d[IDX(NX_+1, NY_+1)] = 0.5 * (rw_d[IDX(NX_+1, NY_+1)] + rw_d[IDX(NX_, NY_)]);
    
    
    for (int i=1 ; i <= NX_ ; i++ ) {
      for (int j=1 ; j <= NY_ ; j++ ) {
        if ( object[IDX(i, j)] == 2 ) {
          if (b==1) {
            // inverse horizontal velocity at vertical object border
            if (object[IDX(i-1, j)]==0) rw_d[IDX(i, j)] =  -rw_d[IDX(i-1, j)];
            if (object[IDX(i+1, j)]==0) rw_d[IDX(i, j)] =  -rw_d[IDX(i+1, j)];
          } 
          else if (b==2) {
            // inverse vertical velocity at horizontal object border
            if (object[IDX(i, j-1)]==0) rw_d[IDX(i, j)] =  -rw_d[IDX(i, j-1)];
            if (object[IDX(i, j+1)]==0) rw_d[IDX(i, j)] =  -rw_d[IDX(i, j+1)];
          } 
          else if (b==0) {
            // same density as active neighbour for egde-border, 
            // average of two active neighbours for corner-border
            int cnt=0; 
            rw_d[IDX(i, j)] = 0;
            if (object[IDX(i-1, j)]==0) { rw_d[IDX(i, j)] += rw_d[IDX(i-1, j)]; cnt++;  }
            if (object[IDX(i+1, j)]==0) { rw_d[IDX(i, j)] += rw_d[IDX(i+1, j)]; cnt++; }
            if (object[IDX(i, j-1)]==0) { rw_d[IDX(i, j)] += rw_d[IDX(i, j-1)]; cnt++; }
            if (object[IDX(i, j+1)]==0) { rw_d[IDX(i, j)] += rw_d[IDX(i, j+1)]; cnt++; }
            rw_d[IDX(i, j)] /= cnt;
          }
        }
      }
    }
  }
  
*/