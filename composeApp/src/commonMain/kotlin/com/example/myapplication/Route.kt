package com.example.myapplication

sealed class Route {
    object Login : Route()
    object Home : Route()
    object Breathing : Route()

    object SignUp : Route()
}