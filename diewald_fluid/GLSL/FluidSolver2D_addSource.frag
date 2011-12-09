//------------------------------------------------------------------------------
///
/// author: thomas diewald
///
/// GLSL - fragment-shader: FluidSolver2D_addSource.frag
///
///
/// date:        25.07.2011
/// last edited: 25.07.2011
///
//------------------------------------------------------------------------------




uniform sampler2D tex_x0;
uniform sampler2D tex_x1;
uniform float     timestep;


////////////////////////////////////////////////////////////////////////////////
// MAIN
//
void main(void) {    
  vec2 coord = gl_TexCoord[0].st;
  gl_FragData[0].x = texture2D(tex_x0, coord)[0]  + (timestep * texture2D(tex_x1, coord)[0]);
}





/*
  private void addSource(float[] wo_x, float[] ro_x0, float ro_timestep){
    for (int i = 0; i < SIZE_; i++){
      wo_x[i] += ro_timestep * ro_x0[i];
    }
  }
*/