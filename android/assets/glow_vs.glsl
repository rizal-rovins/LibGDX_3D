attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;

uniform mat4 u_worldTrans;
uniform mat4 u_projViewTrans;
uniform float u_time;

varying vec2 v_texCoord0;
varying float v_time;

void main() {
    v_texCoord0 = a_texCoord0;
    v_time=u_time;
    gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position, 1.0);
}