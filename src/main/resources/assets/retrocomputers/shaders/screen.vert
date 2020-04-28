#version 330 core

in vec3 xyz;
in vec2 uv;

out vec2 f_uv;

uniform mat4 mvp;

void main() {
    f_uv = uv;
    gl_Position = mvp * vec4(xyz, 1);
}