//------------------------------------------------------------------------------
///
/// author: thomas diewald
///
/// GLSL - fragment-shader: FluidSolver2D_advect.frag
///
///
/// date:        25.07.2011
/// last edited: 25.07.2011
///
//------------------------------------------------------------------------------





 
uniform sampler2D tex1_ro_d0;
uniform sampler2D tex2_ro_du;
uniform sampler2D tex3_ro_dv;
uniform sampler2D tex4_objects;

uniform vec2 inv_size;
uniform float dt0;

vec2 size = 1.0/inv_size;
const vec2 off = vec2(.5, .5);

////////////////////////////////////////////////////////////////////////////////
// forward declaratiosn
//
bool isBorder(in vec2 frag_coord, in vec2 frame_size, in float border_size);


////////////////////////////////////////////////////////////////////////////////
// MAIN
//
void main(void) {   
  vec2 current_coord = gl_TexCoord[0].st;
 
  float objects_value = texture2D(tex4_objects, current_coord).x;
  
  if( objects_value > .1 ) 
    discard;
  
  if( isBorder(gl_FragCoord.xy, size, 1.0) )
    discard;
    
  vec2 N = size- vec2(2.0, 2.0);
  
  vec2 xy_min = vec2(.5, .5);
  vec2 xy_max = N + xy_min;
  
  vec2 real_coord = gl_FragCoord.xy - off;

  vec2 xy = vec2(real_coord.x - dt0 * texture2D(tex2_ro_du, current_coord)[0],
                 real_coord.y - dt0 * texture2D(tex3_ro_dv, current_coord)[0]);
          
          
  xy = clamp(xy, xy_min, xy_max);
  
  float i0 = floor(xy.x);
  float i1 = i0 + 1.0;
  float j0 = floor(xy.y);
  float j1 = j0 + 1.0;

  float s1 = xy.x - i0; 
  float s0 = 1.0 - s1; 
  float t1 = xy.y - j0; 
  float t0 = 1.0 - t1;

  gl_FragData[0].x = s0 * ( t0 * texture2D(tex1_ro_d0,  inv_size * ((vec2(i0, j0)+off))  )[0] + 
                            t1 * texture2D(tex1_ro_d0,  inv_size * ((vec2(i0, j1)+off))  )[0]
                           ) + 
                     s1 * ( t0 * texture2D(tex1_ro_d0,  inv_size * ((vec2(i1, j0)+off))  )[0] + 
                            t1 * texture2D(tex1_ro_d0,  inv_size * ((vec2(i1, j1)+off))  )[0] );
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

  private void advect(float[] wo_d, float[] ro_d0, float[] ro_du, float[] ro_dv, float ro_timestep, int[] ro_objects){
    int i0, j0, i1, j1;
    float x, y, s0, t0, s1, t1;
    
    float x_max = NX_ + 0.5f;
    float y_max = NY_ + 0.5f;

    float dt0 = ro_timestep * (NX_+NY_)/2.0;//
    //dt0 = timestep;//

    for (int i = 1; i <= NX_; i++){
      for (int j = 1; j <= NY_; j++){
        // go backwards through velocity field
        if( ro_objects[IDX(i, j)] != 0 ) continue;
        x = i - dt0 * ro_du[IDX(i, j)];
        y = j - dt0 * ro_dv[IDX(i, j)];

        // interpolate results
        if (x > x_max) x = x_max;
        if (x < 0.5)   x = 0.5f;
        if (y > y_max) y = y_max;
        if (y < 0.5)   y = 0.5f;

        i0 = (int) x;
        i1 = i0 + 1;
        j0 = (int) y;
        j1 = j0 + 1;

        s1 = x - i0; 
        s0 = 1 - s1;
        t1 = y - j0; 
        t0 = 1 - t1;
        
        wo_d[IDX(i, j)] = s0 * (t0 * ro_d0[IDX(i0, j0)] + t1 * ro_d0[IDX(i0, j1)]) + s1 * (t0 * ro_d0[IDX(i1, j0)] + t1 * ro_d0[IDX(i1, j1)]);
      }
    }
  }
  
*/