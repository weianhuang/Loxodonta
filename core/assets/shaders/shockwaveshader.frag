#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

#define MAX_WAVES 10

varying LOWP vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform int wave_count;
uniform vec2 wave_position[MAX_WAVES];
uniform float wave_time[MAX_WAVES];
uniform vec2 resolution;

uniform float wave_radius;
uniform float wave_speed;

const float PI = 3.14159265358979323846264;

void main() {
    float ratio = resolution.y/resolution.x;
    vec2 screenP = gl_FragCoord.xy;
    vec2 sample_coords = v_texCoords;
    for (int i=0; i<wave_count; i++) {
        float wave_middle = wave_time[i] * wave_speed;
        vec2 waveP = wave_position[i];
        vec2 dif = (screenP - waveP);
        float len = length(dif);
        float prog = (len - wave_middle) / wave_radius;
        if (prog <= -1.0 || prog >= 1.0) continue;
        float center_weight = (cos(PI * prog) + 1.0) / 2.0;
        vec2 middle = waveP + normalize(dif) * wave_middle;
        sample_coords = middle / resolution * center_weight + sample_coords * (1.0 - center_weight);
    }
    
    if (sample_coords.x < 0.0) sample_coords.x = 0.0;
    if (sample_coords.x > 1.0) sample_coords.x = 1.0;
    if (sample_coords.y < 0.0) sample_coords.y = 0.0;
    if (sample_coords.y > 1.0) sample_coords.y = 1.0;
    
    gl_FragColor = v_color * texture2D(u_texture, sample_coords);
}
