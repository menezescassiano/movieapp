package com.example.movieapp.feature.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.movieapp.core.ui.components.CustomButton
import com.example.movieapp.core.ui.components.LinkButton
import com.example.movieapp.core.ui.components.text.BodyText
import com.example.movieapp.core.ui.components.text.TitleText
import com.example.movieapp.core.ui.theme.AccentPurple
import com.example.movieapp.core.ui.theme.AppBackground
import com.example.movieapp.core.ui.theme.IndicatorInactive
import com.example.movieapp.core.ui.theme.OnSurfaceMuted
import kotlinx.coroutines.launch

private val HorizontalGutter = 24.dp

data class OnboardingPage(
    val title: String,
    val description: String,
    @DrawableRes val imageRes: Int,
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val page1Title = stringResource(R.string.onboarding_page1_title)
    val page1Desc = stringResource(R.string.onboarding_page1_description)
    val page2Title = stringResource(R.string.onboarding_page2_title)
    val page2Desc = stringResource(R.string.onboarding_page2_description)
    val page3Title = stringResource(R.string.onboarding_page3_title)
    val page3Desc = stringResource(R.string.onboarding_page3_description)

    val pages =
        remember(page1Title, page1Desc, page2Title, page2Desc, page3Title, page3Desc) {
            listOf(
                OnboardingPage(page1Title, page1Desc, R.drawable.onboarding_discover),
                OnboardingPage(page2Title, page2Desc, R.drawable.onboarding_details),
                OnboardingPage(page3Title, page3Desc, R.drawable.onboarding_favorites),
            )
        }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage by remember {
        derivedStateOf { pagerState.settledPage == pages.lastIndex }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppBackground),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
        ) { pageIndex ->
            OnboardingPageContent(page = pages[pageIndex])
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = HorizontalGutter, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            PagerIndicator(
                pageCount = pages.size,
                currentPage = pagerState.currentPage,
            )

            CustomButton(
                text =
                    if (isLastPage) {
                        stringResource(R.string.onboarding_button_get_started)
                    } else {
                        stringResource(R.string.onboarding_button_next)
                    },
                onClick = {
                    if (isLastPage) {
                        onFinish()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            AnimatedVisibility(
                visible = !isLastPage,
                enter = fadeIn(tween(durationMillis = 200)),
                exit = fadeOut(tween(durationMillis = 200)),
            ) {
                LinkButton(
                    text = stringResource(R.string.onboarding_button_skip),
                    onClick = onFinish,
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = HorizontalGutter),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        OnboardingCard(
            imageRes = page.imageRes,
            contentDescription = null,
        )

        Spacer(modifier = Modifier.height(48.dp))

        TitleText(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))

        BodyText(
            text = page.description,
            color = OnSurfaceMuted,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    val a11y = stringResource(R.string.onboarding_indicator_a11y, currentPage + 1, pageCount)
    Row(
        modifier =
            modifier.semantics {
                contentDescription = a11y
            },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            val color by animateColorAsState(
                targetValue = if (isSelected) AccentPurple else IndicatorInactive,
                animationSpec = tween(durationMillis = 300),
                label = "IndicatorColor",
            )
            val width by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 8.dp,
                animationSpec = tween(durationMillis = 300),
                label = "IndicatorWidth",
            )
            Box(
                modifier =
                    Modifier
                        .size(width = width, height = 8.dp)
                        .clip(CircleShape)
                        .background(color),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF12121A)
@Composable
private fun OnboardingScreenPreview() {
    OnboardingScreen(onFinish = {})
}
