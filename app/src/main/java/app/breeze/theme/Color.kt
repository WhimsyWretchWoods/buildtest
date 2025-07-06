package app.breeze.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

val ArcticPrimaryLight = Color(0xFF007B8C)
val OnArcticPrimaryLight = Color(0xFFFFFFFF)
val ArcticPrimaryContainerLight = Color(0xFFB3E0E6)
val OnArcticPrimaryContainerLight = Color(0xFF001F24)

val ArcticPrimaryDark = Color(0xFF5ED8E6)
val OnArcticPrimaryDark = Color(0xFF00363D)
val ArcticPrimaryContainerDark = Color(0xFF004F58)
val OnArcticPrimaryContainerDark = Color(0xFFB3E0E6)

val WhiteBackground = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFF2F2F2)
val OnLightSurface = Color(0xFF1C1B1F)

val BlackBackground = Color(0xFF1C1B1F)
val DarkSurface = Color(0xFF2B292F)
val OnDarkSurface = Color(0xFFE6E0E9)

val LightColorScheme = lightColorScheme(
    primary = ArcticPrimaryLight,
    onPrimary = OnArcticPrimaryLight,
    primaryContainer = ArcticPrimaryContainerLight,
    onPrimaryContainer = OnArcticPrimaryContainerLight,
    secondary = Color(0xFF6A5A72),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF2DAFF),
    onSecondaryContainer = Color(0xFF25182C),
    tertiary = Color(0xFF81525C),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD9E1),
    onTertiaryContainer = Color(0xFF33111A),
    background = WhiteBackground,
    onBackground = OnLightSurface,
    surface = LightSurface,
    onSurface = OnLightSurface,
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF7A757F),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

val DarkColorScheme = darkColorScheme(
    primary = ArcticPrimaryDark,
    onPrimary = OnArcticPrimaryDark,
    primaryContainer = ArcticPrimaryContainerDark,
    onPrimaryContainer = OnArcticPrimaryContainerDark,
    secondary = Color(0xFFD6BEE4),
    onSecondary = Color(0xFF3B2C42),
    secondaryContainer = Color(0xFF52435A),
    onSecondaryContainer = Color(0xFFF2DAFF),
    tertiary = Color(0xFFF2B7C4),
    onTertiary = Color(0xFF4B252E),
    tertiaryContainer = Color(0xFF653B45),
    onTertiaryContainer = Color(0xFFFFD9E1),
    background = BlackBackground,
    onBackground = OnDarkSurface,
    surface = DarkSurface,
    onSurface = OnDarkSurface,
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF948F99),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)
