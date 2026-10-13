#version 430 compatibility
// Visual vanilla + luz colorida: troca o matiz da luz de bloco e estende o alcance.

layout(std430, binding = 1) readonly buffer LightA { uint lightA[]; };
layout(std430, binding = 2) readonly buffer LightB { uint lightB[]; };

uniform sampler2D gtexture;
uniform sampler2D lightmap;
uniform int frameCounter;
uniform vec3 cameraPosition;

in vec2 texcoord;
in vec2 lmcoord;
in vec4 glcolor;
in vec3 worldPos;
in vec3 worldNormal;

/* RENDERTARGETS: 0 */
layout(location = 0) out vec4 outColor;

int cellIndex(ivec3 p) {
    p &= 127;
    return p.x + 128 * (p.y + 128 * p.z);
}

vec3 fetchCell(ivec3 c, bool readA) {
    int i = cellIndex(c);
    uint u = readA ? lightA[i] : lightB[i];
    return vec3(float(u & 255u), float((u >> 8) & 255u), float((u >> 16) & 255u)) * (15.0 / 255.0);
}

// Interpolacao trilinear entre os centros das celulas
vec3 sampleLight(vec3 p) {
    bool readA = (frameCounter & 1) == 1; // quadro par: o compute escreveu em B
    vec3 q = p - 0.5;
    ivec3 b = ivec3(floor(q));
    vec3 f = fract(q);

    vec3 c000 = fetchCell(b + ivec3(0, 0, 0), readA);
    vec3 c100 = fetchCell(b + ivec3(1, 0, 0), readA);
    vec3 c010 = fetchCell(b + ivec3(0, 1, 0), readA);
    vec3 c110 = fetchCell(b + ivec3(1, 1, 0), readA);
    vec3 c001 = fetchCell(b + ivec3(0, 0, 1), readA);
    vec3 c101 = fetchCell(b + ivec3(1, 0, 1), readA);
    vec3 c011 = fetchCell(b + ivec3(0, 1, 1), readA);
    vec3 c111 = fetchCell(b + ivec3(1, 1, 1), readA);

    vec3 x00 = mix(c000, c100, f.x);
    vec3 x10 = mix(c010, c110, f.x);
    vec3 x01 = mix(c001, c101, f.x);
    vec3 x11 = mix(c011, c111, f.x);
    return mix(mix(x00, x10, f.y), mix(x01, x11, f.y), f.z);
}

void main() {
    vec4 albedo = texture(gtexture, texcoord) * glcolor;
    if (albedo.a < 0.1) discard;

    // Luz vanilla e a parte dela que NAO e luz de bloco
    vec3 vanilla = texture(lightmap, lmcoord).rgb;
    vec3 base    = texture(lightmap, vec2(1.0 / 32.0, lmcoord.y)).rgb;
    vec3 partV   = max(vanilla - base, 0.0);
    float vLevel = clamp(lmcoord.x * 16.0 - 0.5, 0.0, 15.0);

    // Luz colorida propagada (brilho 0..15 por canal) na celula a frente da superficie
    vec3 rel = worldPos - cameraPosition;
    vec3 cl = vec3(0.0);
    if (max(abs(rel.x), max(abs(rel.y), abs(rel.z))) < 60.0) {
        cl = sampleLight(worldPos + normalize(worldNormal) * 0.5);
    }
    float bOurs = max(cl.r, max(cl.g, cl.b));
    vec3 hue = cl / max(bOurs, 1e-4);

    // Nivel efetivo = o maior entre o nosso e o vanilla (permite ir alem do alcance de 15 vanilla)
    float L = clamp(max(bOurs, vLevel), 0.0, 15.0);
    vec3 partL = max(texture(lightmap, vec2(L / 16.0 + 1.0 / 32.0, lmcoord.y)).rgb - base, 0.0);
    vec3 tinted = hue * max(partL.r, max(partL.g, partL.b));

    // Quanto da luz de bloco aqui e "nossa"
    float w = (vLevel > 0.5) ? clamp(bOurs / vLevel, 0.0, 1.0) : ((bOurs > 0.3) ? 1.0 : 0.0);
    vec3 part = mix(partV, tinted, w);

    vec3 light = base + part;
    outColor = vec4(albedo.rgb * light, albedo.a);
}
