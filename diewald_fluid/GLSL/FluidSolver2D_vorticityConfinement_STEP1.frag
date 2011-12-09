//------------------------------------------------------------------------------
///
/// author: thomas diewald
///
/// GLSL - fragment-shader: FluidSolver2D_vorticityConfinement_STEP1.frag
///
///
/// date:        25.07.2011
/// last edited: 25.07.2011
///
//------------------------------------------------------------------------------




uniform sampler2D tex1_ro_uVel;
uniform sampler2D tex2_ro_vVel;
uniform sampler2D tex3_objects;

uniform vec2 inv_size;
vec2 size = 1.0/inv_size;
const vec2 off = vec2(.5, .5);

  
////////////////////////////////////////////////////////////////////////////////
// forward declarations
//
bool isBorder(in vec2 frag_coord, in vec2 frame_size, in float border_size);
float curl(vec2 coord, vec2 cs, sampler2D uVel, sampler2D vVel);

////////////////////////////////////////////////////////////////////////////////
// MAIN
//
void main(void) {    
  vec2 current_coord = gl_TexCoord[0].st;

  float tex3_objects_value = texture2D(tex3_objects, current_coord).x;
  
  if( tex3_objects_value > .1 ) 
    discard;
    
  if( isBorder(gl_FragCoord.xy, size, 1.0) )
    discard;
    
  gl_FragData[0].x = abs(curl(current_coord, inv_size, tex1_ro_uVel, tex2_ro_vVel));
}



////////////////////////////////////////////////////////////////////////////////
// curl()
//
float curl(vec2 coord, vec2 cs, sampler2D uVel, sampler2D vVel){
  float du_dy = (texture2D(uVel, coord+vec2(0, +cs.y) )[0] - texture2D(uVel, coord+vec2(0, -cs.y) )[0]) * 0.5; 
  float dv_dx = (texture2D(vVel, coord+vec2(+cs.x, 0) )[0] - texture2D(vVel, coord+vec2(-cs.x, 0) )[0]) * 0.5; 
  return (du_dy - dv_dx);
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
  private void vorticityConfinement_STEP1(float[] ro_uVel, float[] ro_vVel, float[] wo_curl,  int[] ro_objects){
    for (int i = 1; i <= NX_; i++){
      for (int j = 1; j <= NY_; j++){
        if( ro_objects[IDX(i, j)] != 0 ) continue;
        wo_curl[IDX(i, j)] = Math.abs(curl(i, j, ro_uVel, ro_vVel));
      }
    }
  }
  }
*/