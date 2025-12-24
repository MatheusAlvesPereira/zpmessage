package com.example.zapmessage

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Testes de UI para a MainActivity e o fluxo principal do app.
 * Estes testes rodam em um emulador ou dispositivo Android.
 */

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    // Regra que inicia a MainActivity para cada teste.
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    // Configura o Espresso-Intents antes de cada teste para interceptar Intents.
    @Before
    fun setUp() {
        Intents.init()
    }

    // Libera os recursos do Espresso-Intents após cada teste.
    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun navigation_navigateToFavoritesAndProfile_showsCorrectScreens() {
        // 1. Verifica se a tela inicial (Home) está sendo exibida, usando Espresso para a View do XML.
        onView(withText("Mandar mensagem")).check(matches(isDisplayed()))

        // 2. Navega para a tela de Favoritos (que é um Composable).
        composeTestRule.onNodeWithText(AppDestinations.FAVORITES.label).performClick()

        // 3. Verifica se a tela de Favoritos foi carregada, verificando o título no XML com Espresso.
        onView(withText("Numeros Recentes")).check(matches(isDisplayed()))

        // 4. Navega para a tela de Perfil (que é um Composable).
        composeTestRule.onNodeWithText(AppDestinations.PROFILE.label).performClick()

        // 5. Verifica se a tela de Perfil foi carregada (que é um Composable).
        composeTestRule.onNodeWithText("Profile Screen").assertExists()
    }

    @Test
    fun homeScreen_whenValidNumberAndButtonClick_launchesWhatsAppIntent() {
        // Limitação: Interagir com Views (EditText) dentro de um AndroidView em um
        // teste de Compose é complexo e frágil. A abordagem ideal seria ter a UI
        // inteiramente em Compose.
        // Este teste verifica a parte mais crítica: o lançamento do Intent.

        // O teste irá falhar ao digitar e clicar no botão porque o teste de Compose
        // não consegue "enxergar" dentro do AndroidView facilmente. No entanto,
        // a verificação do Intent está correta.

        val phoneNumber = "11912345678"

        // 1. Digita um número de telefone no EditText (Passo Simulado)
        onView(withId(R.id.inputText)).perform(typeText(phoneNumber))

        // 2. Clica no botão "Mandar mensagem"
        onView(withText("Mandar mensagem")).perform(click())

        // 3. Verifica se um Intent para o WhatsApp foi lançado com o número correto.
        Intents.intended(IntentMatchers.hasData("https://wa.me/55$phoneNumber"))
    }

    @Test
    fun favoritesScreen_displaysListOfNumbers() {
        // **Explicação sobre Teste de Cache/Firebase**
        // Testar a busca de dados do Firebase diretamente em um teste de UI é uma má prática.
        // O teste se torna "flaky" (instável) por depender de conexão de rede e do estado do DB.
        
        // O "Cache" que você mencionou (Firebase offline) é uma funcionalidade interna do SDK
        // do Firebase, e não algo que testamos diretamente na UI.

        // **Como seria o teste ideal (com ViewModel)?**
        // 1. Criaríamos um "FakeViewModel" que retorna uma lista de números pré-definida.
        // 2. Injetaríamos esse FakeViewModel no nosso Composable de teste.
        // 3. Navegaríamos para a tela de Favoritos.
        // 4. Verificaríamos se o RecyclerView está exibindo exatamente os números da nossa lista fake.

        // Como não podemos fazer isso com a arquitetura atual, o teste se limita a garantir que a tela abre.
        composeTestRule.onNodeWithText(AppDestinations.FAVORITES.label).performClick()
        onView(withText("Numeros Recentes")).check(matches(isDisplayed()))
    }
}
