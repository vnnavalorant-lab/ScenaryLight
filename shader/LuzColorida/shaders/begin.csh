#version 430 compatibility
// Propaga COR + brilho da luz pela grade (1 passo por quadro).
// Cada celula guarda 1 uint: R,G,B (8 bits cada, brilho 0..15) + decaimento por bloco (8 bits).

layout(local_size_x = 8, local_size_y = 8, local_size_z = 8) in;
const ivec3 workGroups = ivec3(16, 16, 16);

layout(std430, binding = 0) buffer SrcBuf { uvec2 srcLights[]; };
layout(std430, binding = 1) buffer LightA { uint lightA[]; };
layout(std430, binding = 2) buffer LightB { uint lightB[]; };

uniform int frameCounter;

const int TTL = 180; // quadros que uma fonte continua valendo sem ser vista

int cellIndex(ivec3 p) {
    p &= 127;
    return p.x + 128 * (p.y + 128 * p.z);
}

uint packCell(vec3 c, float d) {
    uvec3 q = uvec3(clamp(c * (255.0 / 15.0) + 0.5, 0.0, 255.0));
    uint qd = uint(clamp(d * 16.0 + 0.5, 0.0, 255.0));
    return q.r | (q.g << 8) | (q.b << 16) | (qd << 24);
}

void unpackCell(uint u, out vec3 c, out float d) {
    c = vec3(float(u & 255u), float((u >> 8) & 255u), float((u >> 16) & 255u)) * (15.0 / 255.0);
    d = float(u >> 24) / 16.0;
}

float peak(vec3 v) { return max(v.r, max(v.g, v.b)); }

void main() {
    ivec3 p = ivec3(gl_GlobalInvocationID.xyz);
    int i = cellIndex(p);
    bool prevIsA = (frameCounter & 1) == 0; // quadro par: le A, escreve B

    vec3 bestC = vec3(0.0);
    float bestD = 1.0;

    uvec2 s = srcLights[i];
    int age = frameCounter - int(s.y);
    if (s.y != 0u && age >= 0 && age <= TTL) {
        unpackCell(s.x, bestC, bestD);
    }

    const ivec3 D[6] = ivec3[6](
        ivec3(1, 0, 0), ivec3(-1, 0, 0),
        ivec3(0, 1, 0), ivec3(0, -1, 0),
        ivec3(0, 0, 1), ivec3(0, 0, -1));
    for (int k = 0; k < 6; k++) {
        int j = cellIndex(p + D[k]);
        uint u = prevIsA ? lightA[j] : lightB[j];
        vec3 c; float d;
        unpackCell(u, c, d);
        vec3 cand = max(c - d, vec3(0.0));
        if (peak(cand) > peak(bestC)) bestD = d;
        bestC = max(bestC, cand);
    }

    uint outv = packCell(bestC, bestD);
    if (frameCounter < 3) outv = 0u; // limpa os buffers no inicio

    if (prevIsA) lightB[i] = outv;
    else         lightA[i] = outv;
}
