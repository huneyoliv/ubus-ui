package com.ubusmobilidade.ubus.ui.theme

import androidx.compose.ui.graphics.Color

// ── Light palette — mirrors ubus-front index.css @theme tokens ──

// Backgrounds & surfaces
val UbusBackground     = Color(0xFFF0F4FF) // --color-bg
val UbusSurface        = Color(0xFFFFFFFF) // --color-surface
val UbusSurface2       = Color(0xFFF8FAFF) // --color-surface-2
val UbusBorder         = Color(0xFFE2E8F0) // --color-border

// Primary (blue)
val UbusPrimary        = Color(0xFF2563EB) // --color-primary
val UbusPrimaryLight   = Color(0xFF3B82F6) // --color-primary-light
val UbusPrimaryDark    = Color(0xFF1D4ED8) // --color-primary-dark

// Secondary / accent
val UbusSecondary      = Color(0xFF7C3AED) // --color-secondary
val UbusAccent         = Color(0xFF0EA5E9) // --color-accent

// Semantic
val UbusSuccess        = Color(0xFF10B981) // --color-success
val UbusWarning        = Color(0xFFF59E0B) // --color-warning
val UbusDestructive    = Color(0xFFEF4444) // --color-danger

// Text
val UbusText           = Color(0xFF0F172A) // --color-text
val UbusText2          = Color(0xFF64748B) // --color-text-2
val UbusText3          = Color(0xFF94A3B8) // --color-text-3

// Sidebar / dark surfaces (gestor & motorista layouts)
val UbusSidebarBg      = Color(0xFF0F172A) // --color-sidebar-bg
val UbusSidebarSurface = Color(0xFF1E293B) // --color-sidebar-surface

// Derived
val UbusPrimaryContainer = Color(0x1A2563EB) // primary ~10%
val UbusOnPrimary      = Color(0xFFFFFFFF)

// ── Backward-compat aliases (will be removed after full migration) ──
val UbusAccentLight    = UbusPrimaryLight
val UbusCard           = UbusSurface
val UbusMutedForeground = UbusText3
val UbusForeground     = UbusText
val UbusMuted          = UbusSurface2
val UbusAccentContainer = UbusPrimaryContainer
val UbusOnAccent       = UbusOnPrimary
