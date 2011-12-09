//------------------------------------------------------------------------------
///
/// author: thomas diewald
///
/// GLSL - fragment-shader: FluidSolver2D_project_STEP1.frag
///
///
/// date:        25.07.2011
/// last edited: 25.07.2011
///
//------------------------------------------------------------------------------





uniform sampler2D tex1_ro_x;
uniform sampler2D tex2_ro_y;
uniform sampler2D tex3_objects;

uniform vec2 inv_size;
uniform float h;

const vec2 off = vec2(.5, .5);
vec2 size = 1.0/inv_size;

////////////////////////////////////////////////////////////////////////////////
// forward declarations
//
bool isBorder(in vec2 frag_coord, in vec2 frame_size, in float border_size);

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
    
  gl_FragData[0].x = - 0.5 * h * (
                                    texture2D(tex1_ro_x, current_coord+vec2(+inv_size.x, 0) )[0] - 
                                    texture2D(tex1_ro_x, current_coord+vec2(-inv_size.x, 0) )[0] +  
                                    texture2D(tex2_ro_y, current_coord+vec2(0, +inv_size.y) )[0] -
                                    texture2D(tex2_ro_y, current_coord+vec2(0, -inv_size.y) )[0]
                                  )  ;

  gl_FragData[1].x = 0.0; 
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

  private void project_STEP1(float[] ro_x, float[] ro_y, float[] wo_p, float[] wo_div, float ro_h, int[] ro_objects){
    for (int i = 1; i <= NX_; i++){
      for (int j = 1; j <= NY_; j++){
        if( ro_objects[IDX(i, j)] != 0 ) continue;
        wo_div[IDX(i, j)] = - 0.5f * (ro_x[IDX(i+1, j)] - ro_x[IDX(i-1, j)] + ro_y[IDX(i, j+1)] - ro_y[IDX(i, j-1)])  /  ro_h;
        wo_p[IDX(i, j)] = 0;
      }
    }
  }
  
*/