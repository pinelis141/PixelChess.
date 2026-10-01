# PixelChess

Jogo de xadrez Android em Java: partida Local, multiplayer Bluetooth e bot
Stockfish 19 offline, com cinco dificuldades. Inclui temas, relógios, histórico,
sons, menu e animações em pixel art.

## Licença

**O código original do PixelChess é GPL-3.0-or-later**, conforme autorização do
responsável pelo projeto em 2026-10-01. Consulte [LICENSE](LICENSE) (texto completo
GPL v3) e [NOTICE](NOTICE) (concessão da versão 3 ou posterior e créditos).
É permitido usar, estudar, modificar e redistribuir o código, inclusive comercialmente,
sujeito às condições da GPL. Não há garantia.

Stockfish 19 é GPL-3.0-or-later e mantém seus autores, copyright e avisos próprios;
o motor upstream está inalterado. Consulte [docs/STOCKFISH.md](docs/STOCKFISH.md).
O áudio de peças de mh2o é CC0, conforme [docs/PIECE_AUDIO.md](docs/PIECE_AUDIO.md).
A licença do código **não atribui automaticamente uma licença a músicas/imagens**;
consulte o inventário e os limites em [docs/LICENSING.md](docs/LICENSING.md).
Créditos e GPL estão acessíveis em **Licenças e créditos**, no menu do aplicativo.

## Compilar

Linux, JDK 17, Gradle 8.7 e Android SDK:

```sh
sdkmanager 'platforms;android-35' 'build-tools;35.0.0' 'ndk;28.2.13676358'
tools/build_stockfish.sh
gradle :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleRelease :app:bundleRelease
```

O motor usa fonte oficial fixada e NNUE incorporado; depois de instalado, o bot não
precisa de internet. Detalhes de ABI/API/rebuild offline: docs/STOCKFISH.md.
Release APK/AAB requerem as configurações de assinatura descritas em app/build.gradle;
sem elas, o CI gera pacotes release sem assinatura.

## Distribuir

Os artifacts do mesmo workflow incluem APK/AAB, **PixelChess-corresponding-source**
e **Stockfish-19-corresponding-source** (com NNUE e receita). Use os pacotes da mesma
revisão do binário. O aplicativo identifica o commit quando construído pelo CI.
Não basta disponibilizar uma branch que poderá mudar ou apenas o código do motor.

Para download público, hospede APK e ambos os pacotes de fonte com acesso equivalente,
sem custo adicional para obter a fonte, e preserve os avisos e direitos da GPL.
Artifacts do Actions expiram: antes de publicar uma release, disponibilize as fontes
em armazenamento durável e confirme os direitos dos assets. Este repositório não
publica automaticamente uma release a partir de um merge.
