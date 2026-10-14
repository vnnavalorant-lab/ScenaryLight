# ScenaryColor (Fabric 1.21.11)

Luz INVISIVEL com alcance de 1 a 48 blocos e cor ajustavel. A cor e o alcance acima
de 15 aparecem com o shader (Iris); sem ele o bloco ilumina como o vanilla (ate 15).

## Compilar
1. Java 21 + IntelliJ (abre o build.gradle).
2. `gradle build` -> build/libs/scenarycolor-1.0.0.jar  (teste: `gradle runClient`)
3. GitHub Actions: .github/workflows/build.yml compila a cada push; tag `v*` cria Release.
   Confira as versoes em https://fabricmc.net/develop (1.21.11).

## Como usar no jogo
- Bloco **Luz Ajustavel** (receita: tocha + po de glowstone). E invisivel: so aparece o
  contorno ao mirar. Segurando o bloco, faiscas coloridas marcam os que estao por perto.
- **Ajustar o alcance (editor visual):** com a MAO VAZIA (ou segurando o bloco de luz):
  1. Mire no bloco: aparece uma esfera de particulas (3 aneis) com o raio atual.
  2. Segure o botao direito mirando no bloco e AFASTE a mira: o raio cresce.
     Aproxime: diminui. Uma haste com ponta brilhante mostra a "alca" sendo puxada.
- **Cor:** clique no bloco com um corante (16 cores).

## Shader (Iris) - ScenaryColor-Shader.zip
1. Copie ScenaryColor-Shader.zip para .minecraft/shaderpacks (nao descompacte).
2. Instale Sodium + Iris (1.21.11) e escolha o pack em Video > Shaders.
3. **Video > Shaders > Configuracoes > Vibrancia das Cores**: 0 = cor pura da paleta;
   valores maiores deixam a luz do mod mais saturada e intensa (padrao 0.5, ate 3.0).
4. Mudar a paleta: edite gerar_shader.py e LuzBlock.rgb(), rode
   `python3 shader/gerar_shader.py` e recompacte a pasta shaders/.

Limitacoes: a luz atravessa paredes; fonte fora da tela some apos ~4 s; a luz leva
~1 s para se espalhar; ate ~58 blocos a luz e exata, alem disso o shader so troca o
matiz da luz vanilla pela cor da fonte mais proxima; sem nevoa de distancia; vidro e
agua sem cor; a esfera de particulas so aparece ate ~32 blocos de voce.
