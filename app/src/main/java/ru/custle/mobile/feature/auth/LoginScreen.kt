package ru.custle.mobile.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ru.custle.mobile.core.ui.components.AppHeroCard
import ru.custle.mobile.core.ui.components.AppSectionCard
import ru.custle.mobile.core.ui.components.ErrorBanner

@Composable
fun LoginScreen(
    isBusy: Boolean,
    errorMessage: String?,
    onLogin: (String, String) -> Unit,
    onLoginWithYandex: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AppHeroCard(
            title = "Custle Mobile",
            subtitle = "Входной экран должен сразу объяснять, что это рабочее приложение, а не черновой каркас. Сначала вход, потом workspace, затем основной контур.",
            chips = listOf(
                "Email / пароль" to Icons.Outlined.LockOpen,
                "Яндекс OAuth" to Icons.AutoMirrored.Outlined.Send,
            ),
        )
        AppSectionCard(
            title = "Вход в приложение",
            hint = "Обычный логин или вход через Яндекс, если backend уже настроен.",
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { androidx.compose.material3.Icon(Icons.Outlined.AccountCircle, contentDescription = null) },
                label = { Text("Email") },
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { androidx.compose.material3.Icon(Icons.Outlined.Key, contentDescription = null) },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
            )
            if (!errorMessage.isNullOrBlank()) {
                ErrorBanner(errorMessage)
            }
            Button(
                onClick = { onLogin(email.trim(), password) },
                enabled = !isBusy && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isBusy) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.padding(vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Войти")
                }
            }
            OutlinedButton(
                onClick = onLoginWithYandex,
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Войти через Яндекс")
            }
        }
    }
}
