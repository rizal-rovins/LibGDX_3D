#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoord0;
varying float v_time;
void main() {

    gl_FragColor =vec4(cos(v_time),sin(v_time),1,1);
}