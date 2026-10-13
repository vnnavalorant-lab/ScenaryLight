# Luz Ajustavel (Fabric 1.21.11) + luz colorida no Iris

## Mod
./gradlew build  -> build/libs/luzajustavel-1.0.0.jar
(Gere o gradle wrapper pelo template oficial em https://fabricmc.net/develop se faltar.)

Uso: clique = +alcance | shift+clique = -alcance | corante = muda a cor.

## Luz colorida real (Iris)
O mod expoe cada estado (cor + alcance) como bloco identificavel. Quem pinta a luz
e o shader. Aqui usamos o pacote "Coloured Lights":
https://github.com/eclipseisoffline/iris-coloured-lights  (Download ZIP -> pasta shaderpacks)

1. Rode:  cd shader && python3 gerar_shader.py
2. No shader: cole block_properties_snippet.txt no fim de shaders/block.properties
3. Em shaders/lib/cl/lights.glsl: cole lights_snippet.glsl dentro do array
   colouredLights e some 240 ao tamanho do array.
4. Instale Sodium + Iris para 1.21.11 (Iris 1.10.x exige Sodium 0.8.x compativel).
5. Ative o shader no Iris. Requisitos do shader: SSBO e GLSL 430+.
