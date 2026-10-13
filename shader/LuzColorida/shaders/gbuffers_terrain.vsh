#version 430 compatibility

layout(std430, binding = 0) buffer SrcBuf { uvec2 srcLights[]; };

attribute vec4 mc_Entity;

uniform mat4 gbufferModelViewInverse;
uniform vec3 cameraPosition;
uniform int frameCounter;

out vec2 texcoord;
out vec2 lmcoord;
out vec4 glcolor;
out vec3 worldPos;
out vec3 worldNormal;

// BEGIN_PALETTE
const vec3 PALETTE[16] = vec3[16](vec3(1.000, 1.000, 1.000), vec3(1.000, 0.549, 0.102), vec3(1.000, 0.200, 0.851), vec3(0.400, 0.749, 1.000), vec3(1.000, 0.902, 0.302), vec3(0.502, 1.000, 0.200), vec3(1.000, 0.549, 0.702), vec3(0.502, 0.549, 0.600), vec3(0.800, 0.800, 0.851), vec3(0.102, 0.902, 0.902), vec3(0.600, 0.251, 1.000), vec3(0.200, 0.302, 1.000), vec3(0.800, 0.502, 0.251), vec3(0.200, 0.902, 0.302), vec3(1.000, 0.149, 0.102), vec3(0.302, 0.102, 0.502));
// END_PALETTE

int cellIndex(ivec3 p) {
    p &= 127;
    return p.x + 128 * (p.y + 128 * p.z);
}

uint packCell(vec3 c, float d) {
    uvec3 q = uvec3(clamp(c * (255.0 / 15.0) + 0.5, 0.0, 255.0));
    uint qd = uint(clamp(d * 16.0 + 0.5, 0.0, 255.0));
    return q.r | (q.g << 8) | (q.b << 16) | (qd << 24);
}

void main() {
    vec3 viewPos = (gl_ModelViewMatrix * gl_Vertex).xyz;
    vec3 feet = (gbufferModelViewInverse * vec4(viewPos, 1.0)).xyz;
    worldPos = feet + cameraPosition;
    worldNormal = normalize(mat3(gbufferModelViewInverse) * (gl_NormalMatrix * gl_Normal));

    texcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
    lmcoord  = (gl_TextureMatrix[1] * gl_MultiTexCoord1).xy;
    glcolor  = gl_Color;

    // Bloco do mod? id = 31000 + corIndex*64 + alcance(1..48)
    int k = int(mc_Entity.x + 0.5) - 31000;
    if (k >= 1 && k < 1024) {
        int ci = k >> 6;
        int level = k & 63;
        if (level >= 1 && level <= 48) {
            ivec3 cell = ivec3(floor(worldPos));
            ivec3 camCell = ivec3(floor(cameraPosition));
            ivec3 d = abs(cell - camCell);
            if (d.x < 40 && d.y < 40 && d.z < 40) {
                // Vanilla: nivel 1..15 igual ao Minecraft. Acima de 15: mesmo brilho (15),
                // so que cai mais devagar (15/alcance por bloco).
                float b0 = min(15.0, float(level));
                float decay = b0 / float(level);
                srcLights[cellIndex(cell)] =
                    uvec2(packCell(PALETTE[ci] * b0, decay), uint(frameCounter));
            }
        }
    }

    gl_Position = gl_ProjectionMatrix * vec4(viewPos, 1.0);
}
