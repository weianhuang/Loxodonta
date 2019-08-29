#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif
varying LOWP vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float completion;

const float PI = 3.14159265358979;

void main() {
    vec2 dif = v_texCoords - vec2(0.5, 0.5);
    float ang = atan(dif.x, dif.y);
    float f = (ang + PI) / (2.0 * PI);
    
    if (f <= completion) {
        gl_FragColor = v_color * texture2D(u_texture, v_texCoords);
    } else {
        gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
    }
}
