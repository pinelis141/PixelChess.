# Temas — marco 1

Base: `feature/forest-skin-test`, commit `c9d410a877fc059568e5f7cd62d914f21e852d3e`.

## Estrutura

- `BoardTheme`: definição imutável de ID, nome, recursos, margem, tintas e efeito.
- `BoardThemes`: registro dos temas disponíveis e migração do índice antigo.
- `BoardThemeRenderer`: carregamento único de bitmaps e desenho de moldura, efeitos e tabuleiro.
- `FireflyMotion`: trajetórias determinísticas com fases, alturas e velocidades diferentes.
- `MainActivity.ChessView`: fornece os limites do tabuleiro e desenha peças/HUD por cima.

A preferência `skin` antiga continua sendo lida quando `theme_id` ainda não existe.
Novos temas devem usar IDs permanentes, nunca a posição na lista.

## Floresta Ancestral

Os arquivos `forest_board.webp` e `forest_frame.webp` permanecem byte a byte iguais.
A margem externa passou de 14 dp para 14,98 dp (+7%). Isso aumenta a margem,
não o tamanho do tabuleiro nem a espessura uniforme de todos os detalhes do asset.
A margem continua limitada ao espaço disponível na tela.

As duas barras luminosas foram substituídas por 11 vaga-lumes: seis de um lado,
cinco do outro. Há deriva lenta, halos sobrepostos e pulsos independentes.
O Canvas exclui explicitamente o retângulo jogável ao desenhar os efeitos.
Intensidade, velocidade e cor ficam na configuração do tema.
Intensidade zero desliga o efeito; velocidade zero mantém uma imagem estática.
A atualização decorativa usa 20 fps apenas enquanto a View está visível.

## Contrato para próximos assets

- Recursos em `app/src/main/res/drawable-nodpi`, com nomes `<id>_board.webp` e `<id>_frame.webp`.
- Board quadrado, opaco, exatamente 8×8, sem margem externa ou coordenadas gravadas.
- A primeira casa (a8) é clara. Casas têm dimensões idênticas, alinhadas à borda do arquivo.
- Frame quadrado separado, canal alpha verdadeiro, abertura central alinhada ao board.
- Usar o par atual da Floresta como referência de proporção; conservar a resolução dos originais aprovados.
- WebP lossless para novos assets; não recomprimir os atuais para esta etapa.
- Não colocar sombra/glow nem partículas dentro do board. Efeitos são desenhados em tempo de execução.
- Carregar sem escala de densidade; renderizar bitmaps sem filtragem para preservar os pixels.
- Conferir alpha, centro, recorte e alinhamento nas quatro bordas antes de registrar um tema.
- Se uma nova moldura exigir abertura/proporção diferente, explicitar esse parâmetro no modelo antes de integrá-la.

## Validação

CI: `gradle :app:testDebugUnitTest :app:assembleDebug`.
Testes cobrem migração/fallback de preferências, controle de animação e limites das trajetórias.
Não substituem os futuros testes completos de regras/Bluetooth.

Checklist no aparelho antes de congelar a Floresta:

- Floresta e Tradicional, seleção persistida após reiniciar.
- Moldura legível, sem corte, em S23 Ultra e tela menor (ex.: 360 dp de largura).
- Vaga-lumes fora das casas, sem aparência espelhada e sem disputar atenção com as peças.
- Tocar na margem não seleciona peças da primeira linha/coluna.
- Partida local e Bluetooth nas duas orientações: movimentos, captura e promoção.
- Retorno ao menu sem callback de relógio da View descartada.

A aprovação visual no aparelho ainda é necessária: esta versão não congela a skin automaticamente.
