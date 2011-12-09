//------------------------------------------------------------------------------
///
/// author: thomas diewald
///
/// GLSL - fragment-shader: FluidSolver2D_generateDensityMap.frag
///
///
/// date:        25.07.2011
/// last edited: 25.07.2011
///
//------------------------------------------------------------------------------

uniform sampler2D tex_d0;
uniform sampler2D tex_d1;
uniform sampler2D tex_d2;
uniform sampler2D tex_objects;
uniform sampler2D tex_background;

uniform vec2 src_size_inv;
uniform vec2 dst_size;
uniform int smooth_texture;
uniform int with_border;

uniform vec4 col_objects_border;
uniform vec4 col_objects_body;

vec2 src_size = 1.0/src_size_inv;
const vec2 off = vec2(.5, .5);
vec3 rgb;

////////////////////////////////////////////////////////////////////////////////
// forward declaratiosn
//
bool isBorder(in vec2 frag_coord, in vec2 frame_size, in float border_size);
float maxVal(in vec3 v);
vec3 getColor(in vec3 bg, in vec3 col);
vec2 map(in vec2 v1, in vec2 min1, in vec2 max1, in vec2 min2, in vec2 max2);

////////////////////////////////////////////////////////////////////////////////
// MAIN
//
void main(void) {    
  vec2 coords = gl_TexCoord[0].st;
  
  // dont use the border coordinates
  if( with_border == 0)
    coords = map(coords, vec2(0), vec2(1), vec2(0)+src_size_inv*1.0, vec2(1)-src_size_inv*1.0); 
  
  if ( smooth_texture == 1){
    vec2 src_coords_real    = coords * src_size-off;
    vec2 src_coords_real_00 = floor(src_coords_real);

    vec2 src00 = ( src_coords_real_00+off + vec2(0.0, 0.0) ) * src_size_inv;
    vec2 src10 = ( src_coords_real_00+off + vec2(1.0, 0.0) ) * src_size_inv;
    vec2 src01 = ( src_coords_real_00+off + vec2(0.0, 1.0) ) * src_size_inv;
    vec2 src11 = ( src_coords_real_00+off + vec2(1.0, 1.0) ) * src_size_inv;

    vec3 rgb00 = vec3(texture2D(tex_d0, src00)[0], texture2D(tex_d1, src00)[0], texture2D(tex_d2, src00)[0]);
    vec3 rgb10 = vec3(texture2D(tex_d0, src10)[0], texture2D(tex_d1, src10)[0], texture2D(tex_d2, src10)[0]);
    vec3 rgb01 = vec3(texture2D(tex_d0, src01)[0], texture2D(tex_d1, src01)[0], texture2D(tex_d2, src01)[0]);
    vec3 rgb11 = vec3(texture2D(tex_d0, src11)[0], texture2D(tex_d1, src11)[0], texture2D(tex_d2, src11)[0]);
    
    vec2 p     = src_coords_real - src_coords_real_00;
    

    // interpolating v1: slower
    //vec2 p_inv =  vec2(1, 1) - p;
    //rgb.r = p_inv.y*( rgb00.r*p_inv.x + rgb10.r*p.x ) +  p.y*(rgb01.r*p_inv.x + rgb11.r*p.x);
    //rgb.g = p_inv.y*( rgb00.g*p_inv.x + rgb10.g*p.x ) +  p.y*(rgb01.g*p_inv.x + rgb11.g*p.x);
    //rgb.b = p_inv.y*( rgb00.b*p_inv.x + rgb10.b*p.x ) +  p.y*(rgb01.b*p_inv.x + rgb11.b*p.x);
    
    // interpolating v2: faster
    rgb = mix( mix(rgb00, rgb10, p.x),  mix(rgb01, rgb11, p.x) , p.y);

  } else {
    rgb.r = texture2D(tex_d0, coords)[0];
    rgb.g = texture2D(tex_d1, coords)[0];
    rgb.b = texture2D(tex_d2, coords)[0];
  }
  
  
  float object = texture2D(tex_objects, coords)[0]; 

  if( object >= 1.0 ){
    if( object == 1.0 ) gl_FragColor = vec4( col_objects_body) ;
    if( object == 2.0 ) gl_FragColor = vec4( col_objects_border) ;
    if( col_objects_border.a == 0.0 ){
      gl_FragColor = texture2D(tex_background, coords);
    }
  }else {
    vec3 bg_col = texture2D(tex_background, coords).rgb;
    gl_FragColor = vec4(getColor(bg_col, rgb), 1.0);
  }
  if( with_border == 1 ){
    if( isBorder(gl_FragCoord.xy, dst_size, 0.0) )
      gl_FragColor = vec4(0, 0, 0, 1) ;
      
    if( isBorder(coords * src_size, src_size, 1.0) )
      gl_FragColor = vec4(0, 0, 0, 1) ;
  }

}


////////////////////////////////////////////////////////////////////////////////
// getColor()
//
vec3 getColor(in vec3 bg, in vec3 col){
  bg  = clamp(bg,  vec3(0), vec3(1));
  col = clamp(col, vec3(0), vec3(1));
  float alpha_value_percent_in = 1.0 - maxVal(col);
  return (col) + (bg)*(alpha_value_percent_in); 
}

////////////////////////////////////////////////////////////////////////////////
// maxVal()
//
float maxVal(in vec3 v){
  return max(max(v.x, v.y), max(v.y, v.z));
}



////////////////////////////////////////////////////////////////////////////////
// isBorder()
//
bool isBorder(in vec2 frag_coord, in vec2 frame_size, in float border_size){
  vec2 bs_new = vec2(border_size, border_size)  - vec2(.5,.5);
  return( frag_coord.x <=              bs_new.x || frag_coord.y <=              bs_new.y ||
          frag_coord.x >= frame_size.x-bs_new.x || frag_coord.y >= frame_size.y-bs_new.y );
}

////////////////////////////////////////////////////////////////////////////////
// map()
//
vec2 map(in vec2 v1, in vec2 min1, in vec2 max1, in vec2 min2, in vec2 max2){
  vec2 range1 = max1 - min1;
  vec2 range2 = max2 - min2;
  vec2 v2 = min2 + range2* (v1 - min1) / range1;
  return v2; 
}


