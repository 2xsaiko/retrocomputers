#version 130

in vec3 xyz;
in vec2 uv;

out vec2 uv1;

void main() {
    uv1 = uv;
    gl_Position = gl_ModelViewProjectionMatrix * vec4(xyz, 1);
}