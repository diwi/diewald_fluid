//------------------------------------------------------------------------------
///
/// author: thomas diewald
///
/// GLSL - fragment-shader: FluidSolver2D_project_STEP2.frag
///
///
/// date:        25.07.2011
/// last edited: 25.07.2011
///
//------------------------------------------------------------------------------





uniform sampler2D tex1_rw_x;
uniform sampler2D tex2_rw_y;
uniform sampler2D tex3_ro_p;
uniform sampler2D tex4_objects;

uniform vec2 inv_size;
uniform float     h;

vec2 size = 1.0/inv_size;
const vec2 off = vec2(.5, .5);


////////////////////////////////////////////////////////////////////////////////
// forward declarations
//
bool isBorder(in vec2 frag_coord, in vec2 frame_size, in float border_size);


////////////////////////////////////////////////////////////////////////////////
// MAIN
//
void main(void) {    
  vec2 current_coord = gl_TexCoord[0].st;

  float tex3_objects_value = texture2D(tex4_objects, current_coord).x;
  
  if( tex3_objects_value > .1 ) 
    discard;
  if( isBorder(gl_FragCoord.xy, size, 1.0) )
    discard;
    
  float rw_x = texture2D(tex1_rw_x, current_coord)[0];
  float rw_y = texture2D(tex2_rw_y, current_coord)[0];
    
  gl_FragData[0].x = rw_x - 0.5 * h *( texture2D(tex3_ro_p, current_coord+vec2(+inv_size.x, 0) )[0] - 
                                       texture2D(tex3_ro_p, current_coord+vec2(-inv_size.x, 0) )[0]);
                                       
  gl_FragData[1].x = rw_y - 0.5 * h *( texture2D(tex3_ro_p, current_coord+vec2(0, +inv_size.y) )[0] - 
                                       texture2D(tex3_ro_p, current_coord+vec2(0, -inv_size.y) )[0]);
                               
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



  private void project_STEP2(float[] wo_x, float[] wo_y, float[] ro_p, float ro_h, int[] ro_objects){
    for (int i = 1; i <= NX_; i++){
      for (int j = 1; j <= NY_; j++){
        if( ro_objects[IDX(i, j)] != 0 ) continue;
        wo_x[IDX(i, j)] -= 0.5f * ro_h * (ro_p[IDX(i+1, j)] - ro_p[IDX(i-1, j)]);
        wo_y[IDX(i, j)] -= 0.5f * ro_h * (ro_p[IDX(i, j+1)] - ro_p[IDX(i, j-1)]);
      }
    }
  }
  
*/