package de.gutlog.app.ui

import androidx.compose.runtime.Composable

@Composable
expect fun SharedBackHandler(enabled: Boolean, onBack: () -> Unit)
