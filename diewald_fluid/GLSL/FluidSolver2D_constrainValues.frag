//------------------------------------------------------------------------------
///
/// author: thomas diewald
///
/// GLSL - fragment-shader: FluidSolver2D_constrainValues.frag
///
///
/// date:        25.07.2011
/// last edited: 25.07.2011
///
//------------------------------------------------------------------------------






uniform sampler2D tex_x0;

uniform float min;
uniform float max;

////////////////////////////////////////////////////////////////////////////////
// MAIN
//
void main(void) {    
  vec2 coord = gl_TexCoord[0].st;
  gl_FragData[0].x = clamp(texture2D(tex_x0, coord)[0],  min, max );
}



