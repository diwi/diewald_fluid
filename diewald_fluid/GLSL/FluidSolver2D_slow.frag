//------------------------------------------------------------------------------
///
/// author: thomas diewald
///
/// GLSL - fragment-shader: FluidSolver2D_slow.frag
///
///
/// date:        25.07.2011
/// last edited: 25.07.2011
///
//------------------------------------------------------------------------------



uniform sampler2D tex_x0;
uniform vec2 src_size_inv;
uniform float     slow_factor;


////////////////////////////////////////////////////////////////////////////////
// MAIN
//
void main(void) {    

  gl_FragData[0].x = slow_factor * texture2D(tex_x0, gl_TexCoord[0].st)[0];
}


