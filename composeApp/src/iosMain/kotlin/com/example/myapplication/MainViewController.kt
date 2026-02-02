package com.example.myapplication

import AppRoot
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import Wearable_Hub.AppGraph

fun MainViewController() = ComposeUIViewController {
    val graph = remember { AppGraph(platformContext = null) }
    AppRoot(graph)
}
