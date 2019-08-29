#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif
varying LOWP vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float time;
uniform vec2 slash;
uniform vec2 resolution;

vec3 sepia(vec3 col) {
    return vec3((col.r * 0.393) + (col.g * 0.769) + (col.b * 0.189), (col.r * 0.349) + (col.g * 0.686) + (col.b * 0.168), (col.r * 0.272) + (col.g * 0.534) + (col.b * 0.131));
}

vec3 greyscale(vec3 col) {
    float m = (col.r + col.g + col.b) / 3.0;
    return vec3(m, m, m);
}

void main()
{
    //if (length(v_texCoords - slash) < 0.2) {
    //    gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
    //} else {
    gl_FragColor = v_color * texture2D(u_texture, v_texCoords);
    //}
    float ratio = resolution.y/resolution.x;
    
    vec2 screenFrac = gl_FragCoord.xy / resolution.xy;
    screenFrac.y *= ratio;
    vec2 waveFrac = slash / resolution;
    waveFrac.y *= ratio;
    
    float t = time;
    
    vec2 dir = normalize(screenFrac - waveFrac);
    
    /*vec2 uv = v_texCoords;
    for (int x=-3; x<3; x++) {
        for (int y=-3; y<3; y++) {
            gl_FragColor += texture2D(u_texture, v_texCoords + vec2(x, y)*0.01) / 50.0;
        }
    }*/
    //gl_FragColor = vec4(greyscale(gl_FragColor.xyz), 1.0);
    //v_texCoords - dir * t;
    //uv.y += sin(v_texCoords.x * 10.0 + time) / 10.0;
    //uv.x += cos(v_texCoords.y * 20.0 + time) / 10.0;
    //gl_FragColor = v_color * texture2D(u_texture, uv);

    //gl_FragColor = vec4(1.0 - length(position), 0.0, 0.0, 1.0);
    //gl_FragColor = vec4(gl_FragCoord.xy, 0.0, 1.0);
}
