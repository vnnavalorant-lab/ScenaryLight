# Luz Ajustavel (Fabric 1.21.11) + luz colorida no Iris

## Mod
Uso no jogo: clique = +alcance | shift+clique = -alcance | corante = muda a cor.

### Compilar localmente
1. Instale Java 21 e abra a pasta no IntelliJ (ele importa o build.gradle).
2. Terminal: `gradle build`  ->  build/libs/luzajustavel-1.0.0.jar
   Para testar no jogo: `gradle runClient`
3. (Opcional) gerar o wrapper: `gradle wrapper --gradle-version 9.2.1`
   e commitar gradlew, gradlew.bat e gradle/wrapper/gradle-wrapper.jar.

### GitHub Actions
.github/workflows/build.yml compila a cada push e anexa o jar.
Tag `v*` (ex.: v1.0.0) cria uma Release com o jar.
Ele instala o Gradle por conta propria; nao precisa do gradlew.

Versoes (gradle.properties / build.gradle / build.yml): confira em
https://fabricmc.net/develop (1.21.11). Se o Loom exigir outro Gradle,
troque 9.2.1 no build.yml e no gradle-wrapper.properties.

## Luz colorida real (Iris)
Shader: https://github.com/eclipseisoffline/iris-coloured-lights (Download ZIP -> shaderpacks)
1. cd shader && python3 gerar_shader.py
2. Cole block_properties_snippet.txt no fim de shaders/block.properties
3. Cole lights_snippet.glsl dentro do array colouredLights em
   shaders/lib/cl/lights.glsl e some 240 ao tamanho do array.
4. Instale Sodium + Iris para 1.21.11 e ative o shader.
