//------------------------------------------------------------------------------
///
/// author: thomas diewald
///
/// GLSL - fragment-shader: FluidSolver2D_vorticityConfinement_STEP2.frag
///
///
/// date:        25.07.2011
/// last edited: 25.07.2011
///
//------------------------------------------------------------------------------



uniform sampler2D tex1_ro_uVel;
uniform sampler2D tex2_ro_vVel;
uniform sampler2D tex3_ro_curl;


uniform vec2  inv_size;
uniform float vorticity;
vec2 size = 1.0/inv_size;
const vec2 off = vec2(.5, .5);
  
////////////////////////////////////////////////////////////////////////////////
// forward declarations
//
bool isBorder(in vec2 frag_coord, in vec2 frame_size, in float border_size);
float curl(in vec2 coord, in vec2 cs, in sampler2D uVel, in sampler2D vVel);

////////////////////////////////////////////////////////////////////////////////
// MAIN
//
void main(void) {    
  vec2 current_coord = gl_TexCoord[0].st;

  
  if( isBorder(gl_FragCoord.xy, size, 2.0) )
    discard;
    
  float dw_dx = 0.5 * (texture2D(tex3_ro_curl, current_coord+vec2(+inv_size.x, 0) )[0] - 
                       texture2D(tex3_ro_curl, current_coord+vec2(-inv_size.x, 0) )[0]); 
  float dw_dy = 0.5*  (texture2D(tex3_ro_curl, current_coord+vec2(0, +inv_size.y) )[0] - 
                       texture2D(tex3_ro_curl, current_coord+vec2(0, -inv_size.y) )[0]); 
  
  float length = 1.0/(sqrt(dw_dx * dw_dx + dw_dy * dw_dy) + 0.000001);
 
  dw_dx *= length;
  dw_dy *= length;
  
  float v = curl(current_coord, inv_size, tex1_ro_uVel, tex2_ro_vVel);
  
  gl_FragData[0].x = dw_dy * -v * vorticity;
  gl_FragData[1].x = dw_dx *  v * vorticity;
}




////////////////////////////////////////////////////////////////////////////////
// curl()
//
float curl(in vec2 coord, in vec2 cs, in sampler2D uVel, in sampler2D vVel){
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
  private void vorticityConfinement_STEP2(float[] wo_Fvc_x, float[] wo_Fvc_y, float[] ro_uVel, float[] ro_vVel, float[] ro_curl,  int[] ro_object, float ro_vorticity){
    for (int i = 1; i <= NX_; i++){
      for (int j = 1; j <= NY_; j++){
        float dw_dx = (ro_curl[IDX(i + 1, j)] - ro_curl[IDX(i - 1, j)]) * 0.5f;
        float dw_dy = (ro_curl[IDX(i, j + 1)] - ro_curl[IDX(i, j - 1)]) * 0.5f;

        float length = (float) Math.sqrt(dw_dx * dw_dx + dw_dy * dw_dy) + 0.000001f;
 
        dw_dx /= length;
        dw_dy /= length;

        float v = curl(i, j, ro_uVel, ro_vVel);
        wo_Fvc_x[IDX(i, j)] = dw_dy * -v * ro_vorticity;
        wo_Fvc_y[IDX(i, j)] = dw_dx *  v * ro_vorticity;
      }
    }
  }
*/