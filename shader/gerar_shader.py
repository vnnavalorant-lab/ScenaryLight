#!/usr/bin/env python3
"""
Gera as linhas para o shader "Coloured Lights" (eclipseisoffline/iris-coloured-lights)
para cada combinacao (cor x alcance) do bloco luzajustavel:luz_ajustavel.

Saida:
  block_properties_snippet.txt  -> colar no FINAL de shaders/block.properties
  lights_snippet.glsl           -> colar DENTRO do array colouredLights em shaders/lib/cl/lights.glsl
"""
MOD = "luzajustavel:luz_ajustavel"
BASE_ID = 31000  # confira se nao colide com IDs ja usados no block.properties

CORES = {  # nome do estado : (r, g, b)  -- igual a LuzBlock.rgb()
    "white": 0xFFFFFF, "orange": 0xFF8C1A, "magenta": 0xFF33D9, "light_blue": 0x66BFFF,
    "yellow": 0xFFE64D, "lime": 0x80FF33, "pink": 0xFF8CB3, "gray": 0x808C99,
    "light_gray": 0xCCCCD9, "cyan": 0x1AE6E6, "purple": 0x9940FF, "blue": 0x334DFF,
    "brown": 0xCC8040, "green": 0x33E64D, "red": 0xFF261A, "black": 0x4D1A80,
}

props, lights = [], []
for ci, (nome, rgb) in enumerate(CORES.items()):
    r, g, b = ((rgb >> 16) & 255) / 255, ((rgb >> 8) & 255) / 255, (rgb & 255) / 255
    for nivel in range(1, 16):
        bid = BASE_ID + ci * 16 + nivel
        props.append(f"block.{bid} = {MOD}:cor={nome}:alcance={nivel}")
        lights.append(f"    ColouredLight({bid}, {nivel}.0, vec3({r:.3f}, {g:.3f}, {b:.3f}), true),")

open("block_properties_snippet.txt", "w").write(
    "# --- Luz Ajustavel (mod) ---\n" + "\n".join(props) + "\n")
open("lights_snippet.glsl", "w").write("\n".join(lights) + "\n")
print(f"{len(lights)} entradas geradas. Some {len(lights)} ao <array size> do lights.glsl.")
