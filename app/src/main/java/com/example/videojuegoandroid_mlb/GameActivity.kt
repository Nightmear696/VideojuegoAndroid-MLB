package com.example.videojuegoandroid_mlb

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.ViewTreeObserver
import android.view.WindowInsets
import android.view.WindowInsetsController
import java.util.Timer
import java.util.TimerTask

/**
 * GameActivity - Controlador de la pantalla de juego
 * Configura el GameView, el game loop (Timer/Handler cada 20ms),
 * y gestiona el ciclo de vida de la actividad.
 *
 * Patrón del curso: Timer + Handler para renderización periódica
 * (ver teoría sección 3.4 "Creación de bolas en caída")
 */
class GameActivity : Activity() {

    lateinit var gameView_MLB: GameView
    var timer_MLB: Timer? = null
    val handler_MLB = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_game)

        // Pantalla completa (ocultar barras del sistema)
        // IMPORTANTE: debe ir DESPUÉS de setContentView para que exista la DecorView
        window.insetsController?.let {
            it.hide(WindowInsets.Type.systemBars())
            it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Obtener datos del Intent (nombre y dificultad del menú)
        val nombre_MLB = intent.getStringExtra("NOMBRE_MLB") ?: "Player"
        val dificultad_MLB = intent.getIntExtra("DIFICULTAD_MLB", 1)

        // Referencia al GameView definido en el layout
        gameView_MLB = findViewById(R.id.gameView_MLB)

        // Iniciar el juego con nombre y dificultad
        gameView_MLB.iniciarJuego_MLB(nombre_MLB, dificultad_MLB)

        // Callback cuando se produce Game Over - 1 punto
        gameView_MLB.onGameOver_MLB = { puntuacion ->
            timer_MLB?.cancel()
            val intentGameOver = Intent(this, GameOverActivity::class.java)
            intentGameOver.putExtra("PUNTUACION_MLB", puntuacion)
            intentGameOver.putExtra("NOMBRE_MLB", nombre_MLB)
            intentGameOver.putExtra("DIFICULTAD_MLB", dificultad_MLB)
            startActivity(intentGameOver)
            finish()
        }

        // Esperar a que el layout esté completamente pintado para obtener dimensiones
        // Patrón del curso: ViewTreeObserver.OnGlobalLayoutListener
        // (ver teoría: "getWidth y getHeight devuelven 0 en el constructor")
        val obs_MLB = gameView_MLB.viewTreeObserver
        obs_MLB.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                gameView_MLB.configurarDimensiones_MLB(gameView_MLB.width, gameView_MLB.height)
                gameView_MLB.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        // Iniciar el game loop
        iniciarTimer_MLB()
    }

    /**
     * Timer para el game loop - se ejecuta cada 20ms (≈50 FPS)
     * Patrón del curso: Timer + Handler para renderización
     * (ver teoría sección "Bolas en caída")
     */
    private fun iniciarTimer_MLB() {
        timer_MLB = Timer()
        timer_MLB?.schedule(object : TimerTask() {
            override fun run() {
                handler_MLB.post {
                    gameView_MLB.actualizarJuego_MLB()
                }
            }
        }, 0, 20)
    }

    // ==================== Entrada por teclado ====================
    // Requisito: "El control de mi nave se ha de poder realizar desde teclado" - incluido en 1 punto
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (::gameView_MLB.isInitialized) {
            if (gameView_MLB.onKeyDown(keyCode, event)) return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (::gameView_MLB.isInitialized) {
            if (gameView_MLB.onKeyUp(keyCode, event)) return true
        }
        return super.onKeyUp(keyCode, event)
    }

    // ==================== Ciclo de vida ====================
    override fun onPause() {
        super.onPause()
        timer_MLB?.cancel()
        if (::gameView_MLB.isInitialized) gameView_MLB.pausarSonidos_MLB()
    }

    override fun onResume() {
        super.onResume()
        if (::gameView_MLB.isInitialized && !gameView_MLB.gameOver_MLB) {
            iniciarTimer_MLB()
            gameView_MLB.reanudarSonidos_MLB()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer_MLB?.cancel()
        if (::gameView_MLB.isInitialized) gameView_MLB.liberarRecursos_MLB()
    }
}
