# Floresta Ancestral: folhas e vento

Branch `feature/forest-leaves`, baseada em `feature/stockfish-bot` no commit
`e90488939abe4ab4f1f00de7cdc09e3c54691996`. PR separado, sem alteração no main.
Os quatro assets aprovados da Floresta mantêm seus checksums originais.

## Visual e animação

- `drawable-nodpi/forest_leaf_strip.png`: atlas RGBA, 4 colunas × 2 linhas, oito
  imagens distintas da mesma folha virando. Dimensão efetiva gerada: 1774 × 887.
- Quadros lidos por divisões proporcionais, sem presumir células de tamanho inteiro.
  A sequência mostra frente, perfil, verso e retorno; não é somente girar uma imagem.
- `ForestLeafMotion`: quatro vagas no máximo, períodos individuais de 17,3 a 25,43 s,
  quedas de 9,2 a 11,69 s, início escalonado e intervalos sem emissão em cada vaga.
- Quadros avançam a cada 140–179 ms, com fases diferentes. Balanço e leve rotação
  complementam a mudança de imagem. Opacidade máxima 160–190/255, com fade de entrada
  de 650 ms e saída de 900 ms. A folha ocupa cerca de 9–20 dp visíveis no atlas de 28–34 dp.
- Rajada compartilhada dura 4,5 s a cada 29 s, com entrada/saída suave e direção alternada.
  O deslocamento usa a integral do vento, evitando voltar bruscamente quando a rajada termina.
- Folhas ficam atrás da moldura/tabuleiro. Clipping exclui todas as casas, os relógios,
  status, coordenadas inferiores e histórico; elas não recebem toque nem afetam regras.
- Desativar a preferência de efeitos também desativa folhas e evita carregar o atlas.
  Outras skins não carregam o asset. Vaga-lumes existentes permanecem.

## Recursos e ciclo de vida

Um bitmap é decodificado uma vez com `inSampleSize=8` (menos de 150 KB de pixels),
com filtro/antialias/dither desligados. Não há bitmap novo, lista aleatória, thread ou
Timer por quadro. Paint, Rect e RectF são reutilizados. O redraw usa os 50 ms existentes.
O movimento depende de tempo monotônico, não da quantidade de frames recebidos.
Activity pause interrompe agendamento decorativo e congela o tempo das folhas; resume
retoma sem contabilizar o período fora do app. A regra/relógio de partida não mudou.

## Arte e origem

Novo atlas produzido com a ferramenta integrada de geração de imagens; não foi
baixado de banco de imagens nem copiado de outro jogo. O original foi copiado para
`app/src/main/res/drawable-nodpi/forest_leaf_strip.png` sem substituir assets aprovados.
A geração retornou resolução diferente da solicitada; o renderer aceita a dimensão real.
Não houve alteração da licença do projeto nesta implementação.

Prompt usado:

> Use case: stylized-concept. Asset type: production sprite sheet for Pixel Chess Android, falling forest leaf, a seamless eight-frame animation. Produce a transparent PNG sprite sheet exactly 1024x512, four columns and two rows of equal square 256x256 cells. Exactly one small pixel-art leaf centered on the same pivot in every cell, reading order left to right, top then bottom = frames 0 through 7. The SAME understated olive/ochre leaf progressively tumbles about its long axis: broad front, three-quarter front, thin edge, three-quarter back, broad back, three-quarter back, thin edge, three-quarter front, so frame 7 returns smoothly to frame 0. Shape resembles an elongated oval woodland leaf with a small stem; no maple/fancy lobes. Actual visible sprite around 90x120 pixels per cell, sufficient transparent padding. Render as crisp authentic low-resolution pixel art using large square pixel blocks on a consistent underlying 32x32 grid per cell, 5 muted shades of olive green, earthy ochre and dark brown, thin dark stepped outline, a subtle central vein. No antialiasing, no blur, no glow, no scenery, no text, no numbers, no grids, no shadows outside the leaf, no decorative extra objects. Genuine transparent empty space between frames and in all surrounding areas. Pixel art intended to display at only 12-18dp; keep readable and restrained.

## Verificação

`ForestLeafMotionTest` verifica limites por uma hora simulada, sequência de oito
quadros, fade/intervalos e continuidade/direção do vento. `ForestLeafRenderingTest`
usa gráficos Android nativos para verificar o atlas real, transparência, memória,
movimento visível, ausência de folhas no tabuleiro/relógio, preferência, outras skins
e pausa/retomada. Gera uma prévia da partida em escala de celular para auditoria visual.

CI preserva testes completos, perft 197.281, Android Lint, APK debug/release, AAB e
smoke do Stockfish no Android 35. Validação estética e consumo no celular continuam
necessários. A quantidade de folhas visíveis depende do recorte: parte da trajetória
passa atrás do tabuleiro, portanto nem sempre as quatro vagas aparecem na tela.
