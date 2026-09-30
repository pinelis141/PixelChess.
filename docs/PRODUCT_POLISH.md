# Produto — 0.26.2

## Arquitetura e UX

- O losango de movimentos legais ganhou contorno escuro externo e realce interno para permanecer visível em casas claras e escuras.
- O cavalo agora percorre visualmente uma trajetória em L, com dois segmentos, em vez de interpolar em linha direta até a casa de destino.
- A casa da peça selecionada não recebe mais o mesmo destaque de um destino legal; apenas movimentos e capturas possíveis são marcados.
- Capturas usam moldura própria e o histórico mostra `x` em vez de `-`, com reticências quando exibe apenas os lances mais recentes.
- O histórico ganhou contraste ligeiramente maior e o contorno das peças brancas foi suavizado.
- O destaque vermelho do rei agora é exclusivo de xeque-mate; término por tempo não é confundido visualmente com mate.
- Sair de uma partida ativa pelo botão Voltar exige confirmação, evitando abandono acidental.
- Sprites brancos recebem contorno fino de contraste no renderer, independentemente do PNG de origem.
- `ChessView` agora é uma classe independente. Recebe tema, tempo, lado e a interface `Actions`; não referencia `MainActivity` nem abre sockets.
- `BluetoothMatchController` controla tentativas, cancelamento, handshake, timeout, envio e descarte de mensagens de sessões antigas. `BluetoothManager` mantém apenas o transporte.
- `ChessGame` continua puro; valida a cor do turno e a promoção antes de modificar o estado. A chave de repetição só considera en passant quando há captura legal e os direitos efetivos de roque.
- Configurações, ícone inicial, música, sons opcionais e os temas aprovados continuam disponíveis. A orientação do jogador é mantida após perda da conexão.

- Novas skins foram deliberadamente adiadas; o catálogo desta versão permanece Tradicional, Floresta Ancestral, Castelo Medieval e Forja Vulcânica.

## Retomada Bluetooth

Os dois aparelhos devem usar 0.26.0 ou posterior compatível (protocolo 3).
Ao detectar a interrupção, cada aparelho mantém a partida e pausa o relógio local.
O usuário toca em **Reconectar** nos dois aparelhos. O anfitrião volta a aguardar; o convidado tenta o mesmo aparelho pareado.

O handshake confere a versão e o identificador da partida. O anfitrião envia o histórico completo de jogadas e seus dois relógios. O convidado reproduz e valida cada jogada em uma nova instância do motor antes de aceitar o estado. Isso restaura direitos de roque, en passant, promoções, repetição, contador de 50 lances, histórico e resultados por regras/tempo. O estado anterior só é substituído depois da confirmação; relógios recomeçam a partir do checkpoint, sem cobrar o período desconectado. Tentativas têm limite de 60 segundos e podem ser canceladas.

Limites: retomada em memória enquanto a partida continua aberta nos dois aplicativos; voltar ao menu, fechar o aplicativo ou encerramento do processo descarta essa sessão. Pareamento ainda usa a configuração do Android, agora acessível pelo menu do jogo. Não há descoberta automática de novos aparelhos. A latência entre a queda física e sua detecção ainda depende do Bluetooth do Android.

## Regressão

Testes cobrem afogamento versus mate, limite de 100 meios-lances e reinício do contador por captura/peão, material insuficiente, roque, en passant, promoção, turno incorreto, repetição com en passant irrelevante, retomada por replay e rejeição de partidas/protocolos diferentes. Testes da View conferem pausa, restauração dos relógios e divergência de turnos.

A CI verifica os assets-base da Floresta, executa a suíte Android e gera APK debug, APK release e AAB. Os dois pacotes release são artefatos separados. Na ausência dos quatro segredos de assinatura, são **sem assinatura**. Ícone e áudio ainda exigem aprovação no aparelho; retomada deve ser ensaiada com dois celulares físicos antes de distribuir.

## Assinatura

Segredos opcionais de GitHub Actions: `PIXELCHESS_KEYSTORE_BASE64`, `PIXELCHESS_STORE_PASSWORD`, `PIXELCHESS_KEY_ALIAS` e `PIXELCHESS_KEY_PASSWORD`. Nenhuma chave privada é guardada no repositório.
