#version 130

#define SCREEN_WIDTH 80
#define SCREEN_HEIGHT 50

#define BGCOLOR vec3(0.09, 0.07, 0)
#define FGCOLOR vec3(0.78, 0.57, 0.01)

uniform usampler2D charset;
uniform usampler2D screen;

in vec2 uv1;

out vec4 fragColor;

float get_pixel(in ivec2 px) {
    // where is this character on the screen? (0,0) - (SCREEN_WIDTH,SCREEN_HEIGHT)
    ivec2 char = px / 8;

    // which pixel of this character is this? (0,0)-(8,8)
    ivec2 chPixel = px % 8;

    // which character is this? 0-255
    int chIndex = int(texelFetch(screen, char, 0).x);

    // the bitmap of the currently drawing line of the character
    int lineData = int(texelFetch(charset, ivec2(chPixel.y, chIndex), 0).x);

    return float((lineData >> (7 - chPixel.x)) & 1);
}

float get_pixel_with_fx(in ivec2 px) {
    float strength = get_pixel(px);

    float f = 0;
    for (int dist = 6; dist > 0; dist--) {
        f *= 0.5;
        f = max(f, get_pixel(px - ivec2(dist, 0)) * 0.3);
    }
    strength = min(1, strength + f);

    if (px.y % 2 == 0) strength *= 0.9;

    strength *= dot(vec3(0, 0, 1), normalize(vec3(px - 0.5 * vec2(SCREEN_WIDTH * 8, SCREEN_HEIGHT * 8), 300)));

    return strength;
}

void main() {
    ivec2 px = ivec2(uv1.x * SCREEN_WIDTH * 8, uv1.y * SCREEN_HEIGHT * 8);

    float strength = get_pixel_with_fx(px);

    vec3 color = mix(BGCOLOR, FGCOLOR, strength);

    fragColor = vec4(color, 1);
}