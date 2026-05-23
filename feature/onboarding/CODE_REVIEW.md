# Code Review — Feature Onboarding

> **Status deste PR:** os itens 1.1, 1.2, 1.3, 2.1, 2.2, 3.1, 3.2, 3.3, 4.1, 4.2 e 4.3 foram aplicados.
> Os itens 5.1, 5.2, 5.3 e 5.4 ficaram fora — envolvem mudanças arquiteturais ou de produto que merecem
> discussão em separado.

Revisão dos arquivos do módulo `feature/onboarding`:

- `OnboardingScreen.kt`
- `OnboardingCard.kt`
- `res/values/strings.xml`
- `build.gradle.kts`

A análise considera correção, performance de recomposição, acessibilidade, design system, testabilidade e clareza de código.

---

## Resumo

O fluxo está funcional e bem segmentado em composables pequenos. As principais oportunidades de melhoria estão em:

1. Sobreposição entre o `HorizontalPager` e os controles inferiores em telas pequenas.
2. Pequenos bugs em `PagerIndicator` (modifier duplicado) e no botão "Skip" invisível mas clicável.
3. Recomposições evitáveis (lista de páginas recriada, leitura direta de `currentPage`).
4. Uso de cores hardcoded em vez do `MaterialTheme.colorScheme`.
5. Acessibilidade do indicador e da imagem do card.
6. Ausência de testes para o ViewModel e para o estado da UI.

---

## 1. Bugs e correção

### 1.1 Botão "Skip" invisível continua clicável e ocupa foco — `OnboardingScreen.kt:129-133`

```kotlin
LinkButton(
    text = stringResource(R.string.onboarding_button_skip),
    onClick = { if (!isLastPage) onFinish() },
    modifier = if (isLastPage) Modifier.invisible() else Modifier,
)
```

`Modifier.invisible()` aplica apenas `alpha(0f)` (ver `core/ui/ModifierExtensions.kt:6`). O botão segue:

- recebendo cliques (mitigado pelo `if (!isLastPage)`, mas isso é defensivo, não correto);
- recebendo foco de TalkBack/teclado físico;
- ocupando espaço no layout.

Sugestões (em ordem de preferência):

- Usar `AnimatedVisibility(visible = !isLastPage)` para preservar o espaço sem manter o nó interativo.
- Ou substituir `invisible()` por uma combinação `alpha(0f).clickable(enabled = false).clearAndSetSemantics { }` para evitar foco.
- Como mínimo, desabilitar o botão: `LinkButton(..., enabled = !isLastPage)` (exige adicionar `enabled` ao `LinkButton`).

### 1.2 `Modifier.height` redundante no indicador — `OnboardingScreen.kt:198-205`

```kotlin
Box(
    modifier =
        Modifier
            .height(8.dp)
            .size(width, 8.dp)
            .clip(CircleShape)
            .background(color),
)
```

`.size(width, 8.dp)` já define largura e altura. O `.height(8.dp)` anterior é sobrescrito e pode confundir. Remover para deixar apenas:

```kotlin
Modifier
    .size(width = width, height = 8.dp)
    .clip(CircleShape)
    .background(color)
```

### 1.3 Coluna inferior sobrepõe o conteúdo do pager — `OnboardingScreen.kt:80-135`

A `Column` com os botões está absolutamente posicionada com `Alignment.BottomCenter` dentro de um `Box`, e o `HorizontalPager` ocupa `fillMaxSize()`. Em dispositivos pequenos (ou com fonte aumentada), a descrição da página pode ser coberta pelos botões.

Sugestão: trocar o `Box` por uma `Column` raiz com:

- pager dentro de `Modifier.weight(1f)`
- bloco de controles abaixo, sem `align`.

Isso garante que o conteúdo respeita o espaço reservado para os botões.

---

## 2. Performance / recomposição

### 2.1 Lista `pages` recriada a cada recomposição — `OnboardingScreen.kt:57-74`

`stringResource` é composable, mas a `List<OnboardingPage>` em si poderia ser estabilizada para evitar realocações:

```kotlin
val page1Title = stringResource(R.string.onboarding_page1_title)
val page1Desc  = stringResource(R.string.onboarding_page1_description)
// ...
val pages = remember(page1Title, page1Desc, /* ... */) {
    listOf(
        OnboardingPage(page1Title, page1Desc, R.drawable.onboarding_discover),
        // ...
    )
}
```

Impacto pequeno (3 itens), mas o padrão é útil em listas maiores.

### 2.2 Ler `pagerState.currentPage` no escopo principal força recomposição em cada animação de página — `OnboardingScreen.kt:78`

```kotlin
val isLastPage = pagerState.currentPage == pages.lastIndex
```

Como `currentPage` é um `State<Int>`, qualquer mudança recompõe a função inteira. Encapsular em `derivedStateOf` reduz o escopo:

```kotlin
val isLastPage by remember {
    derivedStateOf { pagerState.currentPage == pages.lastIndex }
}
```

E preferir `pagerState.settledPage` para evitar alternar o texto do botão durante o swipe parcial — assim "Next" só vira "Get started" quando o snap acontece.

---

## 3. Design system / theming

### 3.1 Cores hardcoded fora da paleta — `OnboardingScreen.kt:169` e `:189`

```kotlin
color = Color.White.copy(alpha = 0.6f)
// ...
targetValue = if (isSelected) AccentPurple else Color.White.copy(alpha = 0.25f),
```

O resto do projeto define cores em `core/ui/theme/Color.kt`. Convém adicionar tokens como `OnSurfaceMuted` / `IndicatorInactive` e referenciá-los aqui, ou usar `MaterialTheme.colorScheme.onSurfaceVariant`. Isso facilita ajustes globais e suporte a tema claro no futuro.

### 3.2 Padding horizontal inconsistente

- `OnboardingPageContent`: `padding(horizontal = 32.dp)` (`OnboardingScreen.kt:147`).
- Bloco inferior: `padding(horizontal = 24.dp)` (`OnboardingScreen.kt:99`).

Sem motivo evidente para a diferença. Padronizar (24.dp é o múltiplo de 8 mais comum no app) reduz ruído visual.

### 3.3 `Spacer` somado a `spacedBy` — `OnboardingScreen.kt:101, 108`

`Arrangement.spacedBy(16.dp)` já espaça os filhos; o `Spacer(height = 8.dp)` extra entre o indicador e o botão soma para 24.dp. Funciona, mas mistura dois mecanismos de espaçamento — escolher um (ex.: só `Spacer`s explícitos, ou só `spacedBy`) deixa o layout mais previsível.

---

## 4. Acessibilidade

### 4.1 `contentDescription` da imagem repete o título — `OnboardingScreen.kt:151-154`

```kotlin
OnboardingCard(
    imageRes = page.imageRes,
    contentDescription = page.title,
)
```

O TalkBack vai ler o título da imagem e logo em seguida o `TitleText` com o mesmo conteúdo. Como o título visível já carrega a informação, a imagem pode ser tratada como decorativa:

```kotlin
OnboardingCard(imageRes = page.imageRes, contentDescription = null)
```

Hoje `OnboardingCard` exige `contentDescription: String` não-nulo (`OnboardingCard.kt:23`); torná-lo `String?` e propagar para `Image` resolve.

### 4.2 `PagerIndicator` sem semântica — `OnboardingScreen.kt:175-208`

Os `Box`es coloridos não comunicam progresso. Adicionar:

```kotlin
Row(
    modifier = modifier.semantics {
        contentDescription = "Page ${currentPage + 1} of $pageCount"
    },
    ...
```

(usando `stringResource` com placeholders no `strings.xml`, claro).

### 4.3 `ContentScale.Crop` em vetor decorativo — `OnboardingCard.kt:38`

Vetores 280×280 sendo cropados em um `Box` 280×280 funcionam hoje porque os tamanhos batem. Se o `size` parametrizado mudar, o crop pode esconder partes da arte. `ContentScale.Fit` é mais seguro para drawables vetoriais; ou `ContentScale.Inside` se quiser preservar o tamanho intrínseco.

---

## 5. Arquitetura / testabilidade

### 5.1 Estado do pager fora do ViewModel

`OnboardingScreen` mantém `pagerState` localmente, o que é OK para state UI puro. Mas isso impede testar regras como "ao chegar na última página, o botão muda para Get started" sem um teste de UI. Se quiser cobrir esse comportamento por unit test, mover `currentPage` e `isLastPage` para o `OnboardingViewModel` como `StateFlow<Int>` ajuda. Em escopo "navegação simples", o que está hoje basta.

### 5.2 `OnboardingViewModel` com sombreamento de nome — `OnboardingViewModel.kt:12-15`

```kotlin
private val markOnboardingCompleted: MarkOnboardingCompletedUseCase,
) : ViewModel() {
    fun markOnboardingCompleted() {
        markOnboardingCompleted.invoke()
    }
}
```

A propriedade e a função têm o mesmo nome. Não há erro de compilação porque a função é resolvida pelo escopo, mas leitores podem hesitar. Sugestões:

- Renomear a propriedade: `private val markOnboardingCompletedUseCase` (consistente com `UploadProfilePictureUseCase` em outros pontos do projeto).
- Ou renomear a função para `onGetStartedClicked()` / `onFinish()`, deixando a relação com use case implícita.

### 5.3 Sem testes do ViewModel

Existe `UploadProfilePictureUseCaseTest` no app, mas nenhum teste de `OnboardingViewModel`. Um teste simples verificando que `markOnboardingCompleted()` delega ao use case já cobre o caminho feliz. Os outros use cases de onboarding (`IsOnboardingCompletedUseCase`, `MarkOnboardingCompletedUseCase`) também merecem cobertura.

### 5.4 `onFinish` mistura "skip" e "concluir"

`OnboardingScreen` chama o mesmo `onFinish()` tanto no botão "Get started" quanto no "Skip" (`OnboardingScreen.kt:119, 131`). A navegação (`MovieNavigation.kt:60-67`) marca o onboarding como completo nos dois casos — incluindo skip. Isso pode ser intencional, mas merece confirmação de produto: usuários que pulam veem o onboarding de novo? Se sim, separar `onFinish` e `onSkip` no contrato do composable evita acoplar essa decisão à UI.

---

## 6. Detalhes menores

- `OnboardingScreen.kt:43`: `import com.example.movieapp.feature.onboarding.R` é redundante quando o pacote bate. Pode remover.
- `OnboardingScreenPreview` (`:212-214`) só cobre o estado inicial. Vale adicionar previews para o estado "última página" (botão "Get started" + skip invisível) e para cada página individual, ajudando design.
- `OnboardingPage` é um `data class` com strings, ou seja, instância nova a cada recomposição se as strings vêm de `stringResource`. Combinado com 2.1, vale o `remember`.
- `feature/onboarding/build.gradle.kts:33`: `androidx.lifecycle.runtime.ktx` é declarado mas não há uso aparente neste módulo (nenhum `LifecycleObserver`, `lifecycleScope`, etc.). Verificar se pode ser removido.
- O `build.gradle.kts` não declara `testImplementation` nem `androidTestImplementation`, o que reforça §5.3.

---

## Prioridade sugerida

| Prioridade | Item |
| --- | --- |
| Alta | 1.1 (skip invisível clicável), 1.3 (sobreposição de layout), 5.4 (semântica de skip vs. finish) |
| Média | 2.2 (`derivedStateOf` + `settledPage`), 3.1 (cores no tema), 4.1/4.2 (acessibilidade), 5.3 (testes) |
| Baixa | 1.2 (modifier duplicado), 2.1 (`remember`), 3.2/3.3, 4.3, 5.2, 6.x |

Nenhum dos pontos é bloqueante para merge; são melhorias incrementais.
