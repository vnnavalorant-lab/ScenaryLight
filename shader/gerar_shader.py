#!/usr/bin/env python3
"""
Regenera, a partir de UMA tabela de cores:
  LuzColorida/shaders/block.properties   (IDs 31000 + cor*64 + alcance(1..48))
  paleta dentro de gbuffers_terrain.vsh  (entre BEGIN_PALETTE / END_PALETTE)
Mantenha a tabela igual a LuzBlock.rgb() no Java.
"""
import re, pathlib
MOD = "luzajustavel:luz_ajustavel"
BASE_ID = 31000
CORES = {
    "white": 0xFFFFFF, "orange": 0xFF8C1A, "magenta": 0xFF33D9, "light_blue": 0x66BFFF,
    "yellow": 0xFFE64D, "lime": 0x80FF33, "pink": 0xFF8CB3, "gray": 0x808C99,
    "light_gray": 0xCCCCD9, "cyan": 0x1AE6E6, "purple": 0x9940FF, "blue": 0x334DFF,
    "brown": 0xCC8040, "green": 0x33E64D, "red": 0xFF261A, "black": 0x4D1A80,
}
root = pathlib.Path(__file__).parent / "LuzColorida" / "shaders"

lines = ["# Gerado por gerar_shader.py", "# Luz Ajustavel: id = 31000 + corIndex*64 + alcance (1..48)"]
for ci, nome in enumerate(CORES):
    for nivel in range(1, 49):
        lines.append(f"block.{BASE_ID + ci*64 + nivel} = {MOD}:cor={nome}:alcance={nivel}")
(root / "block.properties").write_text("\n".join(lines) + "\n")

vecs = ", ".join(
    f"vec3({((v>>16)&255)/255:.3f}, {((v>>8)&255)/255:.3f}, {(v&255)/255:.3f})"
    for v in CORES.values())
vsh = root / "gbuffers_terrain.vsh"
txt = vsh.read_text()
txt = re.sub(r"// BEGIN_PALETTE.*?// END_PALETTE",
             f"// BEGIN_PALETTE\nconst vec3 PALETTE[16] = vec3[16]({vecs});\n// END_PALETTE",
             txt, flags=re.S)
vsh.write_text(txt)
print("ok:", len(lines) - 2, "estados")
