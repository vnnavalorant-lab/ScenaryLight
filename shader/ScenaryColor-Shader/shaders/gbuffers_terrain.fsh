#version 430 compatibility
// Visual vanilla + luz colorida: troca o matiz da luz de bloco e estende o alcance.
// Perto: grade fina (cor + alcance exatos). Longe: grade grossa (so a COR, sem vazar para vanilla).

// ===== OPCOES (aparecem em Video > Shaders > Configuracoes) =====
// Vibrancia das cores da luz do mod: 0 = cor pura da paleta, valores maiores = mais saturada e intensa
#define VIBRANCIA 0.5 // [0.0 0.25 0.5 0.75 1.0 1.5 2.0 3.0]

layout(std430, binding = 1) readonly buffer LightA { uint lightA[]; };
layout(std430, binding = 2) readonly buffer LightB { uint lightB[]; };
layout(std430, binding = 3) readonly buffer CoarseBuf { uvec4 coarse[]; };

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

const int TTL = 240;

int cellIndex(ivec3 p) {
    p &= 127;
    return p.x + 128 * (p.y + 128 * p.z);
}

int coarseIndex(ivec3 c) {
    c &= 63;
    return c.x + 64 * (c.y + 64 * c.z);
}

uint hash3(ivec3 c) {
    return uint(c.x) * 73856093u ^ uint(c.y) * 19349663u ^ uint(c.z) * 83492791u;
}

vec3 unpackRGB(uint u) {
    return vec3(float(u & 255u), float((u >> 8) & 255u), float((u >> 16) & 255u)) * (15.0 / 255.0);
}

float peak(vec3 v) { return max(v.r, max(v.g, v.b)); }

// Aumenta a saturacao do matiz (mantem o canal mais forte em 1)
vec3 vibrante(vec3 hue) {
    float l = dot(hue, vec3(0.2126, 0.7152, 0.0722));
    vec3 h = max(mix(vec3(l), hue, 1.0 + VIBRANCIA), vec3(0.0));
    return h / max(peak(h), 1e-4);
}

// Intensidade extra da luz colorida
const float VIB_GANHO = 1.0 + 0.25 * VIBRANCIA;

// ---------- grade fina ----------
vec3 fetchCell(ivec3 c, bool readA) {
    int i = cellIndex(c);
    return unpackRGB(readA ? lightA[i] : lightB[i]);
}

vec3 sampleFine(vec3 p) {
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

// ---------- grade grossa (longe) ----------
// retorna vec4(cor*peso, peso)
vec4 coarseCell(ivec3 c, float w) {
    uvec4 e = coarse[coarseIndex(c)];
    int age = frameCounter - int(e.y);
    if (e.y == 0u || age < 0 || age > TTL || e.z != hash3(c)) return vec4(0.0);
    return vec4(unpackRGB(e.x) * w, w);
}

vec4 sampleCoarse(vec3 p) {
    vec3 q = p / 16.0 - 0.5;
    ivec3 b = ivec3(floor(q));
    vec3 f = fract(q);
    vec4 acc = vec4(0.0);
    for (int i = 0; i < 8; i++) {
        ivec3 o = ivec3(i & 1, (i >> 1) & 1, (i >> 2) & 1);
        vec3 wv = mix(1.0 - f, f, vec3(o));
        acc += coarseCell(b + o, wv.x * wv.y * wv.z);
    }
    return acc;
}

void main() {
    vec4 albedo = texture(gtexture, texcoord) * glcolor;
    if (albedo.a < 0.1) discard;

    // Luz vanilla e a parte dela que NAO e luz de bloco
    vec3 vanilla = texture(lightmap, lmcoord).rgb;
    vec3 base    = texture(lightmap, vec2(1.0 / 32.0, lmcoord.y)).rgb;
    vec3 partV   = max(vanilla - base, 0.0);
    float vLevel = clamp(lmcoord.x * 16.0 - 0.5, 0.0, 15.0);

    vec3 rel = worldPos - cameraPosition;
    float maxRel = max(abs(rel.x), max(abs(rel.y), abs(rel.z)));

    // ----- PERTO: cor + alcance exatos -----
    vec3 partNear = partV;
    if (maxRel < 60.0) {
        vec3 cl = sampleFine(worldPos + normalize(worldNormal) * 0.5);
        float bOurs = peak(cl);
        vec3 hue = cl / max(bOurs, 1e-4);

        // Nivel efetivo = o maior entre o nosso e o vanilla (vai alem do alcance 15 vanilla)
        float L = clamp(max(bOurs, vLevel), 0.0, 15.0);
        vec3 partL = max(texture(lightmap, vec2(L / 16.0 + 1.0 / 32.0, lmcoord.y)).rgb - base, 0.0);
        vec3 tinted = vibrante(hue) * peak(partL) * VIB_GANHO;

        float w = (vLevel > 0.5) ? clamp(bOurs / vLevel, 0.0, 1.0) : ((bOurs > 0.3) ? 1.0 : 0.0);
        partNear = mix(partV, tinted, w);
    }

    // ----- LONGE: so troca o matiz da luz vanilla pela cor da fonte mais proxima -----
    vec3 partFar = partV;
    float farBlend = smoothstep(50.0, 58.0, maxRel);
    if (farBlend > 0.0 && vLevel > 0.5) {
        vec4 cs = sampleCoarse(worldPos);
        if (cs.w > 0.001) {
            vec3 hueF = cs.rgb / max(peak(cs.rgb), 1e-4);
            float fw = clamp(cs.w * 4.0, 0.0, 1.0);
            partFar = mix(partV, vibrante(hueF) * peak(partV) * VIB_GANHO, fw);
        }
    }

    vec3 part = mix(partNear, partFar, farBlend);
    vec3 light = base + part;
    outColor = vec4(albedo.rgb * light, albedo.a);
}
