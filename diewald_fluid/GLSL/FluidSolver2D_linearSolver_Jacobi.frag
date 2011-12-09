//------------------------------------------------------------------------------
///
/// author: thomas diewald
///
/// GLSL - fragment-shader: FluidSolver2D_linearSolver_Jacobi.frag
///
///
/// date:        25.07.2011
/// last edited: 25.07.2011
///
//------------------------------------------------------------------------------







uniform sampler2D tex1_ro_x;
uniform sampler2D tex2_ro_x0;
uniform sampler2D tex3_objects;

uniform vec2  inv_size;
uniform float a;
uniform float c;


const vec2 off = vec2(.5, .5);
vec2 size = 1.0/inv_size;
////////////////////////////////////////////////////////////////////////////////
// forward declaratiosn
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
    
  gl_FragData[0].x = c * (  texture2D(tex2_ro_x0, current_coord)[0] + 
                            a * ( texture2D(tex1_ro_x, current_coord+vec2(-inv_size.x, 0) )[0] + 
                                  texture2D(tex1_ro_x, current_coord+vec2(+inv_size.x, 0) )[0] + 
                                  texture2D(tex1_ro_x, current_coord+vec2(0, -inv_size.y) )[0] + 
                                  texture2D(tex1_ro_x, current_coord+vec2(0, +inv_size.y) )[0] ) 
                                 ) ;

                               
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
  private void linearSolveJacobi(float[] wo_buffer, float[] ro_x, float[] ro_x0, float ro_a, float ro_c, int[] ro_objects){
    for (int i = 1; i <= NX_; i++){
      for (int j = 1; j <= NY_; j++){
        int k = IDX(i, j);
        if( ro_objects[k] != 0 ) continue;
        wo_buffer[k] = (ro_x0[k] + ro_a * ( ro_x[k-1] + ro_x[k+1] + ro_x[k-(NX_+2)] + ro_x[k+(NX_+2)]) ) / ro_c;
      }
    }
  }
  
*/