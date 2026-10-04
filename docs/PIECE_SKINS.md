# Pixel Chess — assets de peças originais recuperados

**Não publicar novamente as peças provisórias Guardiões ou Obsidiana.** Essas foram geradas diretamente no código e não são as imagens aprovadas.

As skins corretas são **Medieval** e **Floresta**. O arquivo de origem aprovado é `PixelChess_Animacoes_Personagens.zip`, arquivado na Biblioteca do projeto. Ele contém seis tipos de personagens, duas cores, frente e costas e oito quadros por combinação, totalizando 384 arquivos PNG RGBA.

O pacote binário preparado para inclusão exata no jogo é `PixelChess_Assets_Aprovados_Android.zip`. Este deve ser inserido SEM renomeação do conteúdo interno em:
`app/src/main/assets/approved_piece_atlases.zip`

**SHA256 esperado:** `f2cfe45b0d0073783f47305b3b4615bd679a0afbfd1f90cf596a251cd2feec69`.

O ZIP de Android contém oito atlas PNG com pixels idênticos aos PNGs de origem (verificação pixel a pixel na preparação do pacote), dimensões 1024×768: oito colunas de 128×128 por quadro, seis linhas para peão, torre, cavalo, bispo, rainha e rei. Quatro vistas por skin: claras_frente, claras_costas, escuras_frente e escuras_costas.

O `PixelPieceArt.java` extrai os PNGs do APK e usa 180 ms por quadro. O `ChessView` usa a vista de costas para o lado próximo do jogador e a frontal para o distante, respeitando a orientação nos modos Local, Bluetooth e Bot. Os movimentos do motor, o protocolo e o Stockfish não são alterados. As miniaturas do menu usam os mesmos desenhos que aparecem na partida.

**ESTADO:** a parte textual da integração foi corrigida na branch `feature/animated-piece-skins`, mas o asset ZIP ainda não está no GitHub. O conector de GitHub disponível nesta conversa não permite enviar arquivos binários locais. A CI agora exige a presença do pacote e verifica seu hash antes de gerar qualquer novo APK, evitando repetir a entrega com placeholders. A versão Android 0.28.0 já gerada continua inadequada para avaliação visual; não compartilhá-la como versão com assets aprovados.
