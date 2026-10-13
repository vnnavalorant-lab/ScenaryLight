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

## Como usar no jogo
- Bloco **Luz Ajustavel**: INVISIVEL (so aparece o contorno ao mirar). Receita: tocha + po de glowstone.
- **Varinha de Luz** (graveto + po de glowstone): editor visual.
  1. Segure a varinha: faiscas coloridas marcam os blocos de luz num raio de 12 blocos.
  2. Mire num bloco: aparece uma esfera de particulas (3 aneis) com o raio atual.
  3. Segure o botao direito mirando no bloco e AFASTE a mira: o raio cresce.
     Aproxime: diminui. Uma haste com ponta brilhante mostra a "alca" sendo puxada.
- Corante no bloco: muda a cor. Mao vazia: +1 de alcance (Shift: -1).
- Alcance 1..48. O vanilla so ilumina ate 15; o resto vem do shader.

## Luz colorida e alcance estendido (Iris) - shader LuzColorida
O shader fica em shader/LuzColorida/ (e pronto em LuzColorida.zip).
Sem ele o bloco ilumina como no vanilla (ate 15), sem cor.

1. Copie LuzColorida.zip para .minecraft/shaderpacks (nao descompacte)
2. Instale Sodium + Iris (1.21.11) e escolha "LuzColorida" em Video > Shaders
3. Se mudar a paleta: edite gerar_shader.py e LuzBlock.rgb(), rode
   `python3 shader/gerar_shader.py` e recompacte a pasta shaders/.

Limitacoes: a luz atravessa paredes; fonte fora da tela some apos ~3 s; a luz leva
~1 s para se espalhar; sem nevoa de distancia no terreno; vidro/agua sem cor;
a esfera de particulas so aparece ate ~32 blocos de voce.
