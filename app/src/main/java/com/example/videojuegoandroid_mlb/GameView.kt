package com.example.videojuegoandroid_mlb

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import java.util.*

/**
 * GameView - Vista personalizada que hereda de View
 * Núcleo del videojuego: renderizado, lógica e input.
 *
 * Patrón del curso:
 * - Hereda de View (ver teoría sección 3 "Dibujar elementos básicos")
 * - onDraw para dibujar la escena (Canvas + Paint)
 * - onTouchEvent para captar acciones del usuario (ratón/touch)
 * - onKeyDown/onKeyUp para captar acciones del teclado
 * - invalidate() para redibujar (llamado desde el Timer del GameActivity)
 * - RectF.intersects() para detección de colisiones
 *
 * Variables personalizadas con sufijo _MLB según requisito de la práctica.
 */
class GameView : View {

    // ==================== JUGADOR (nave del jugador a la derecha) ====================
    var jugadorX_MLB = 0f
    var jugadorY_MLB = 0f
    val jugadorAncho_MLB = 120f
    val jugadorAlto_MLB = 70f

    // ==================== DIMENSIONES DEL JUEGO ====================
    var anchoJuego_MLB = 0
    var altoJuego_MLB = 0

    // ==================== ESTADO DEL JUEGO ====================
    var nombre_MLB = ""
    var dificultad_MLB = 1  // 1 = Fácil, 2 = Difícil
    var puntuacion_MLB = 0
    var gameOver_MLB = false
    var juegoIniciado_MLB = false
    var tiempoJuego_MLB = 0L

    // ==================== VELOCIDAD ====================
    // Requisito: "A medida que pase el tiempo, la velocidad de las naves
    //             de ataque aumentará." - 1 punto
    var velocidadBase_MLB = 4f
    var multiplicadorVelocidad_MLB = 1.0f

    // ==================== NAVES ENEMIGAS ====================
    // Requisito: "aparezcan naves de forma aleatoria desde la izquierda
    //             de la pantalla hacia la derecha"
    data class NaveEnemiga_MLB(
        var x: Float, var y: Float,
        var velocidad: Float,
        var ancho: Float = 90f,
        var alto: Float = 55f
    )
    val navesEnemigas_MLB = mutableListOf<NaveEnemiga_MLB>()

    // ==================== DISPAROS ====================
    // Requisito: "Mi nave ha de poder disparar." - 1 punto
    // Requisito: "Las naves atacantes sólo pueden disparar de forma
    //             aleatoria en el segundo nivel" - 1 punto
    data class Disparo_MLB(
        var x: Float, var y: Float,
        var esJugador: Boolean,
        var velocidad: Float
    )
    val disparos_MLB = mutableListOf<Disparo_MLB>()

    // ==================== ESTRELLAS (fondo parallax - mejora) ====================
    data class Estrella_MLB(
        var x: Float, var y: Float,
        var vel: Float, var tam: Float,
        var brillo: Int
    )
    val estrellas_MLB = mutableListOf<Estrella_MLB>()

    // ==================== EXPLOSIONES (mejora visual) ====================
    data class Explosion_MLB(
        var x: Float, var y: Float,
        var frame: Int = 0,
        var maxFrame: Int = 20
    )
    val explosiones_MLB = mutableListOf<Explosion_MLB>()

    // ==================== SONIDOS ====================
    // Requisito: "El juego debe tener sonidos diferentes. El sonido mientras
    //             se juega y el sonido al recibir una colisión." - 1 punto
    var musicaFondo_MLB: MediaPlayer? = null
    var soundPool_MLB: SoundPool? = null
    var sonidoDisparoId_MLB = 0
    var sonidoExplosionId_MLB = 0
    var sonidosCargados_MLB = false

    // ==================== INPUT ====================
    // Requisito: "El control de mi nave se ha de poder realizar
    //             desde teclado y desde ratón."
    var moverArriba_MLB = false
    var moverAbajo_MLB = false
    var cooldownDisparo_MLB = 0
    val velocidadJugador_MLB = 12f

    // ==================== HERRAMIENTAS ====================
    val random_MLB = Random()

    // ==================== PAINTS (pinceles para dibujar) ====================
    val paintFondo_MLB = Paint().apply {
        color = Color.rgb(5, 5, 15); style = Paint.Style.FILL
    }
    val paintJugador_MLB = Paint().apply {
        color = Color.rgb(0, 200, 255); style = Paint.Style.FILL; isAntiAlias = true
    }
    val paintJugadorDetalle_MLB = Paint().apply {
        color = Color.rgb(0, 120, 180); style = Paint.Style.FILL; isAntiAlias = true
    }
    val paintEnemigo_MLB = Paint().apply {
        color = Color.rgb(220, 50, 50); style = Paint.Style.FILL; isAntiAlias = true
    }
    val paintEnemigoDetalle_MLB = Paint().apply {
        color = Color.rgb(180, 30, 30); style = Paint.Style.FILL; isAntiAlias = true
    }
    val paintDisparoJugador_MLB = Paint().apply {
        color = Color.rgb(0, 255, 200); style = Paint.Style.FILL; isAntiAlias = true
    }
    val paintDisparoEnemigo_MLB = Paint().apply {
        color = Color.rgb(255, 120, 0); style = Paint.Style.FILL; isAntiAlias = true
    }
    val paintTexto_MLB = Paint().apply {
        color = Color.WHITE; textSize = 36f; isAntiAlias = true
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }
    val paintTextoGrande_MLB = Paint().apply {
        color = Color.RED; textSize = 80f; isAntiAlias = true
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    val paintEstrella_MLB = Paint().apply {
        color = Color.WHITE; style = Paint.Style.FILL; isAntiAlias = true
    }
    val paintExplosion_MLB = Paint().apply {
        style = Paint.Style.FILL; isAntiAlias = true
    }
    val paintMotor_MLB = Paint().apply {
        color = Color.rgb(255, 180, 0); style = Paint.Style.FILL; isAntiAlias = true
    }
    val paintCabinaJugador_MLB = Paint().apply {
        color = Color.rgb(100, 230, 255); style = Paint.Style.FILL; isAntiAlias = true
    }
    val paintCabinaEnemigo_MLB = Paint().apply {
        color = Color.rgb(255, 150, 150); style = Paint.Style.FILL; isAntiAlias = true
    }

    // ==================== CALLBACK GAME OVER ====================
    var onGameOver_MLB: ((Int) -> Unit)? = null

    // ==================== CONSTRUCTORES ====================
    // Patrón del curso: dos constructores necesarios
    // (ver teoría: "Error inflating class... el motivo es que no hemos
    //  definido el constructor necesario para pintarlo")
    constructor(context: Context) : super(context) { initView_MLB() }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) { initView_MLB() }
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) { initView_MLB() }

    private fun initView_MLB() {
        isFocusable = true
        isFocusableInTouchMode = true
    }

    // ==================== INICIALIZACIÓN DEL JUEGO ====================
    fun iniciarJuego_MLB(nombre: String, dificultad: Int) {
        this.nombre_MLB = nombre
        this.dificultad_MLB = dificultad
        this.puntuacion_MLB = 0
        this.gameOver_MLB = false
        this.tiempoJuego_MLB = 0
        this.multiplicadorVelocidad_MLB = 1.0f
        this.cooldownDisparo_MLB = 0
        navesEnemigas_MLB.clear()
        disparos_MLB.clear()
        explosiones_MLB.clear()

        // Iniciar música de fondo (sonido durante el juego) - 1 punto
        try {
            musicaFondo_MLB = MediaPlayer.create(context, R.raw.gameloop)
            musicaFondo_MLB?.isLooping = true
            musicaFondo_MLB?.setVolume(0.4f, 0.4f)
            musicaFondo_MLB?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // SoundPool para efectos de sonido cortos (disparo, explosión)
        try {
            val audioAttr_MLB = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            soundPool_MLB = SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttr_MLB)
                .build()
            sonidoDisparoId_MLB = soundPool_MLB!!.load(context, R.raw.shoot, 1)
            sonidoExplosionId_MLB = soundPool_MLB!!.load(context, R.raw.explosion, 1)
            soundPool_MLB?.setOnLoadCompleteListener { _, _, _ ->
                sonidosCargados_MLB = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Configurar dimensiones del juego una vez el layout está pintado.
     * Se llama desde el OnGlobalLayoutListener del GameActivity.
     */
    fun configurarDimensiones_MLB(ancho: Int, alto: Int) {
        anchoJuego_MLB = ancho
        altoJuego_MLB = alto
        // La nave del jugador se sitúa a la derecha
        jugadorX_MLB = ancho - jugadorAncho_MLB - 40f
        jugadorY_MLB = alto / 2f - jugadorAlto_MLB / 2f

        // Crear estrellas de fondo (mejora: efecto parallax)
        estrellas_MLB.clear()
        for (i in 0..80) {
            estrellas_MLB.add(
                Estrella_MLB(
                    random_MLB.nextFloat() * ancho,
                    random_MLB.nextFloat() * alto,
                    random_MLB.nextFloat() * 5 + 1,
                    random_MLB.nextFloat() * 2.5f + 0.5f,
                    100 + random_MLB.nextInt(155)
                )
            )
        }

        juegoIniciado_MLB = true
    }

    // ==================================================================
    //                    LÓGICA DEL JUEGO (Game Loop)
    // ==================================================================
    // Patrón del curso (ver teoría "Lógica del juego"):
    // 1. Comprobar eventos del usuario
    // 2. Actualizar posiciones de los elementos
    // 3. Detectar colisiones
    // 4. Dibujar de nuevo la pantalla (invalidate -> onDraw)
    // ==================================================================

    fun actualizarJuego_MLB() {
        if (gameOver_MLB || !juegoIniciado_MLB) return

        tiempoJuego_MLB += 20

        // === Velocidad aumenta con el tiempo - 1 punto ===
        multiplicadorVelocidad_MLB = 1.0f + (tiempoJuego_MLB / 10000f) * 0.3f

        // === Movimiento por teclado ===
        if (moverArriba_MLB && jugadorY_MLB > 10) {
            jugadorY_MLB -= velocidadJugador_MLB
        }
        if (moverAbajo_MLB && jugadorY_MLB < altoJuego_MLB - jugadorAlto_MLB - 10) {
            jugadorY_MLB += velocidadJugador_MLB
        }

        // === Generar naves enemigas ===
        // Requisito: "El nivel de dificultad tendrá una relación directa
        //             con el número de naves atacantes." - 1 punto
        val maxEnemigos_MLB = if (dificultad_MLB == 1) 3 else 6
        val tasaAparicion_MLB = if (dificultad_MLB == 1) 40 else 20
        if (navesEnemigas_MLB.size < maxEnemigos_MLB && random_MLB.nextInt(tasaAparicion_MLB) == 0) {
            val vel_MLB = (random_MLB.nextFloat() * 3 + velocidadBase_MLB) * multiplicadorVelocidad_MLB
            navesEnemigas_MLB.add(
                NaveEnemiga_MLB(
                    -100f,
                    random_MLB.nextFloat() * (altoJuego_MLB - 120) + 30,
                    vel_MLB
                )
            )
        }

        // === Mover naves enemigas ===
        val itEnemigos_MLB = navesEnemigas_MLB.iterator()
        while (itEnemigos_MLB.hasNext()) {
            val enemigo_MLB = itEnemigos_MLB.next()
            enemigo_MLB.x += enemigo_MLB.velocidad

            // === Disparos enemigos solo en nivel 2 (difícil) - 1 punto ===
            if (dificultad_MLB == 2 &&
                random_MLB.nextInt(100) == 0 &&
                enemigo_MLB.x > 0 &&
                enemigo_MLB.x < anchoJuego_MLB - 100
            ) {
                disparos_MLB.add(
                    Disparo_MLB(
                        enemigo_MLB.x + enemigo_MLB.ancho,
                        enemigo_MLB.y + enemigo_MLB.alto / 2,
                        false,
                        7f * multiplicadorVelocidad_MLB
                    )
                )
            }

            // Eliminar si sale de pantalla
            if (enemigo_MLB.x > anchoJuego_MLB + 150) {
                itEnemigos_MLB.remove()
            }
        }

        // === Cooldown de disparo del jugador ===
        if (cooldownDisparo_MLB > 0) cooldownDisparo_MLB--

        // === Mover disparos ===
        val itDisparos_MLB = disparos_MLB.iterator()
        while (itDisparos_MLB.hasNext()) {
            val disparo_MLB = itDisparos_MLB.next()
            if (disparo_MLB.esJugador) {
                disparo_MLB.x -= disparo_MLB.velocidad  // Hacia la izquierda
            } else {
                disparo_MLB.x += disparo_MLB.velocidad  // Hacia la derecha
            }
            if (disparo_MLB.x < -30 || disparo_MLB.x > anchoJuego_MLB + 30) {
                itDisparos_MLB.remove()
            }
        }

        // ==============================================================
        //    DETECCIÓN DE COLISIONES (RectF.intersects)
        //    Patrón del curso (ver teoría sección 3.2 y 3.5)
        // ==============================================================
        val disparosAEliminar_MLB = mutableSetOf<Disparo_MLB>()
        val enemigosAEliminar_MLB = mutableSetOf<NaveEnemiga_MLB>()

        // --- Disparos del jugador vs naves enemigas ---
        for (disparo_MLB in disparos_MLB) {
            if (disparo_MLB.esJugador && disparo_MLB !in disparosAEliminar_MLB) {
                val rectD_MLB = RectF(
                    disparo_MLB.x - 15, disparo_MLB.y - 4,
                    disparo_MLB.x + 15, disparo_MLB.y + 4
                )
                for (enemigo_MLB in navesEnemigas_MLB) {
                    if (enemigo_MLB !in enemigosAEliminar_MLB) {
                        val rectE_MLB = RectF(
                            enemigo_MLB.x, enemigo_MLB.y,
                            enemigo_MLB.x + enemigo_MLB.ancho,
                            enemigo_MLB.y + enemigo_MLB.alto
                        )
                        // Detección de colisión con intersects
                        if (RectF.intersects(rectD_MLB, rectE_MLB)) {
                            disparosAEliminar_MLB.add(disparo_MLB)
                            enemigosAEliminar_MLB.add(enemigo_MLB)
                            puntuacion_MLB += 10
                            // Explosión visual (mejora)
                            explosiones_MLB.add(
                                Explosion_MLB(
                                    enemigo_MLB.x + enemigo_MLB.ancho / 2,
                                    enemigo_MLB.y + enemigo_MLB.alto / 2
                                )
                            )
                            // Sonido de explosión al destruir enemigo
                            if (sonidosCargados_MLB) {
                                soundPool_MLB?.play(sonidoExplosionId_MLB, 0.6f, 0.6f, 1, 0, 1f)
                            }
                            break
                        }
                    }
                }
            }
        }

        // --- Naves enemigas vs jugador = Game Over ---
        // Requisito: "A la que se detecte una única colisión ha de salir
        //             la pantalla de Game Over." - 1 punto
        val rectJugador_MLB = RectF(
            jugadorX_MLB + 10, jugadorY_MLB + 5,
            jugadorX_MLB + jugadorAncho_MLB - 5,
            jugadorY_MLB + jugadorAlto_MLB - 5
        )
        for (enemigo_MLB in navesEnemigas_MLB) {
            if (enemigo_MLB !in enemigosAEliminar_MLB) {
                val rectE_MLB = RectF(
                    enemigo_MLB.x + 5, enemigo_MLB.y + 5,
                    enemigo_MLB.x + enemigo_MLB.ancho - 5,
                    enemigo_MLB.y + enemigo_MLB.alto - 5
                )
                if (RectF.intersects(rectJugador_MLB, rectE_MLB)) {
                    activarGameOver_MLB()
                    return
                }
            }
        }

        // --- Disparos enemigos vs jugador = Game Over ---
        for (disparo_MLB in disparos_MLB) {
            if (!disparo_MLB.esJugador && disparo_MLB !in disparosAEliminar_MLB) {
                val rectD_MLB = RectF(
                    disparo_MLB.x - 8, disparo_MLB.y - 8,
                    disparo_MLB.x + 8, disparo_MLB.y + 8
                )
                if (RectF.intersects(rectJugador_MLB, rectD_MLB)) {
                    activarGameOver_MLB()
                    return
                }
            }
        }

        // Eliminar elementos destruidos
        disparos_MLB.removeAll(disparosAEliminar_MLB)
        navesEnemigas_MLB.removeAll(enemigosAEliminar_MLB)

        // === Actualizar explosiones ===
        val itExplosion_MLB = explosiones_MLB.iterator()
        while (itExplosion_MLB.hasNext()) {
            val exp_MLB = itExplosion_MLB.next()
            exp_MLB.frame++
            if (exp_MLB.frame >= exp_MLB.maxFrame) {
                itExplosion_MLB.remove()
            }
        }

        // === Actualizar estrellas (parallax) ===
        for (estrella_MLB in estrellas_MLB) {
            estrella_MLB.x -= estrella_MLB.vel
            if (estrella_MLB.x < 0) {
                estrella_MLB.x = anchoJuego_MLB.toFloat()
                estrella_MLB.y = random_MLB.nextFloat() * altoJuego_MLB
            }
        }

        // Redibujar - llama a onDraw()
        // Patrón del curso: invalidate() para actualizar la pantalla
        invalidate()
    }

    /**
     * Activar Game Over: detener música, reproducir sonido de colisión,
     * crear explosión del jugador y notificar al Activity.
     */
    private fun activarGameOver_MLB() {
        gameOver_MLB = true
        // Detener música de fondo
        try {
            musicaFondo_MLB?.stop()
        } catch (_: Exception) {}

        // Sonido de colisión / explosión del jugador (sonido diferente) - 1 punto
        if (sonidosCargados_MLB) {
            soundPool_MLB?.play(sonidoExplosionId_MLB, 1f, 1f, 1, 0, 0.7f)
        }

        // Explosión visual del jugador (mejora)
        explosiones_MLB.add(
            Explosion_MLB(
                jugadorX_MLB + jugadorAncho_MLB / 2,
                jugadorY_MLB + jugadorAlto_MLB / 2,
                maxFrame = 30
            )
        )
        invalidate()

        // Esperar 2 segundos antes de mostrar la pantalla de Game Over
        postDelayed({
            onGameOver_MLB?.invoke(puntuacion_MLB)
        }, 2000)
    }

    // ==================== DISPARO DEL JUGADOR ====================
    // Requisito: "Mi nave ha de poder disparar." - 1 punto
    fun disparar_MLB() {
        if (gameOver_MLB || cooldownDisparo_MLB > 0) return
        disparos_MLB.add(
            Disparo_MLB(
                jugadorX_MLB,
                jugadorY_MLB + jugadorAlto_MLB / 2,
                true,
                18f
            )
        )
        cooldownDisparo_MLB = 8  // Cooldown en frames (8 * 20ms = 160ms)

        // Sonido de disparo
        if (sonidosCargados_MLB) {
            soundPool_MLB?.play(sonidoDisparoId_MLB, 0.3f, 0.3f, 0, 0, 1.2f)
        }
    }

    // ==================================================================
    //                    RENDERIZADO (onDraw)
    // ==================================================================
    // Patrón del curso (ver teoría sección 3):
    // "En el onDraw es donde dibujamos la escena"
    // Usamos Canvas y Paint para dibujar todos los elementos.
    // ==================================================================

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // === Fondo negro (espacio) ===
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paintFondo_MLB)

        // === Estrellas de fondo (parallax) - mejora visual ===
        for (estrella_MLB in estrellas_MLB) {
            paintEstrella_MLB.alpha = estrella_MLB.brillo
            canvas.drawCircle(estrella_MLB.x, estrella_MLB.y, estrella_MLB.tam, paintEstrella_MLB)
        }

        // === Nave del jugador (a la derecha, apuntando a la izquierda) ===
        if (!gameOver_MLB) {
            dibujarNaveJugador_MLB(canvas)
        }

        // === Naves enemigas (vienen desde la izquierda) ===
        for (enemigo_MLB in navesEnemigas_MLB.toList()) {
            dibujarNaveEnemiga_MLB(canvas, enemigo_MLB)
        }

        // === Disparos ===
        for (disparo_MLB in disparos_MLB.toList()) {
            if (disparo_MLB.esJugador) {
                // Disparo del jugador: línea cyan/verde
                canvas.drawRoundRect(
                    disparo_MLB.x - 18, disparo_MLB.y - 3,
                    disparo_MLB.x + 18, disparo_MLB.y + 3,
                    3f, 3f, paintDisparoJugador_MLB
                )
            } else {
                // Disparo enemigo: bola naranja con glow
                canvas.drawCircle(disparo_MLB.x, disparo_MLB.y, 6f, paintDisparoEnemigo_MLB)
                paintDisparoEnemigo_MLB.alpha = 60
                canvas.drawCircle(disparo_MLB.x, disparo_MLB.y, 12f, paintDisparoEnemigo_MLB)
                paintDisparoEnemigo_MLB.alpha = 255
            }
        }

        // === Explosiones (mejora visual) ===
        for (explosion_MLB in explosiones_MLB.toList()) {
            val progress_MLB = explosion_MLB.frame.toFloat() / explosion_MLB.maxFrame
            val radius_MLB = 20f + progress_MLB * 60f
            val alpha_MLB = ((1f - progress_MLB) * 255).toInt().coerceIn(0, 255)

            // Círculo exterior (naranja/rojo)
            paintExplosion_MLB.color = Color.argb(
                alpha_MLB, 255,
                (180 * (1 - progress_MLB)).toInt().coerceIn(0, 255), 0
            )
            canvas.drawCircle(explosion_MLB.x, explosion_MLB.y, radius_MLB, paintExplosion_MLB)

            // Círculo medio (amarillo)
            paintExplosion_MLB.color = Color.argb(
                alpha_MLB, 255, 255,
                (150 * (1 - progress_MLB)).toInt().coerceIn(0, 255)
            )
            canvas.drawCircle(explosion_MLB.x, explosion_MLB.y, radius_MLB * 0.6f, paintExplosion_MLB)

            // Círculo interior (blanco)
            paintExplosion_MLB.color = Color.argb(
                (alpha_MLB * 0.8f).toInt().coerceIn(0, 255), 255, 255, 255
            )
            canvas.drawCircle(explosion_MLB.x, explosion_MLB.y, radius_MLB * 0.3f, paintExplosion_MLB)
        }

        // === HUD (información en pantalla) ===
        // Puntuación (mejora: sistema de puntuación)
        paintTexto_MLB.textAlign = Paint.Align.LEFT
        paintTexto_MLB.color = Color.rgb(0, 255, 200)
        canvas.drawText("Score: $puntuacion_MLB", 20f, 45f, paintTexto_MLB)

        // Requisito: "El nombre tiene que aparecer en la segunda pantalla" - 1 punto
        paintTexto_MLB.color = Color.rgb(200, 200, 200)
        canvas.drawText(nombre_MLB, 20f, 85f, paintTexto_MLB)

        // Multiplicador de velocidad
        paintTexto_MLB.textAlign = Paint.Align.RIGHT
        paintTexto_MLB.color = Color.rgb(255, 200, 0)
        canvas.drawText(
            "Speed: x%.1f".format(multiplicadorVelocidad_MLB),
            width - 20f, 45f, paintTexto_MLB
        )

        // Indicador de dificultad
        val diffText_MLB = if (dificultad_MLB == 1) "EASY" else "HARD"
        val diffColor_MLB = if (dificultad_MLB == 1) Color.rgb(76, 175, 80) else Color.rgb(244, 67, 54)
        paintTexto_MLB.color = diffColor_MLB
        canvas.drawText(diffText_MLB, width - 20f, 85f, paintTexto_MLB)

        // === Texto de GAME OVER superpuesto ===
        if (gameOver_MLB) {
            // Fondo semitransparente
            val paintOverlay_MLB = Paint().apply {
                color = Color.argb(120, 0, 0, 0); style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paintOverlay_MLB)

            paintTextoGrande_MLB.color = Color.RED
            paintTextoGrande_MLB.textSize = 80f
            canvas.drawText("GAME OVER", width / 2f, height / 2f - 20f, paintTextoGrande_MLB)

            paintTextoGrande_MLB.color = Color.WHITE
            paintTextoGrande_MLB.textSize = 40f
            canvas.drawText("Score: $puntuacion_MLB", width / 2f, height / 2f + 40f, paintTextoGrande_MLB)
            paintTextoGrande_MLB.textSize = 80f  // Reset
        }
    }

    // ==================== DIBUJAR NAVE DEL JUGADOR ====================
    // Nave apuntando a la IZQUIERDA (el jugador está a la derecha)
    // Dibujada con Canvas Path (polígono)
    private fun dibujarNaveJugador_MLB(canvas: Canvas) {
        val x = jugadorX_MLB
        val y = jugadorY_MLB
        val w = jugadorAncho_MLB
        val h = jugadorAlto_MLB

        // Motor (llamas animadas en el lado derecho)
        val tamLlama_MLB = 15f + random_MLB.nextInt(20).toFloat()
        paintMotor_MLB.color = Color.rgb(255, 150 + random_MLB.nextInt(105), 0)
        val pathLlama_MLB = Path()
        pathLlama_MLB.moveTo(x + w, y + h * 0.3f)
        pathLlama_MLB.lineTo(x + w + tamLlama_MLB, y + h * 0.5f)
        pathLlama_MLB.lineTo(x + w, y + h * 0.7f)
        pathLlama_MLB.close()
        canvas.drawPath(pathLlama_MLB, paintMotor_MLB)

        // Cuerpo principal de la nave
        val pathNave_MLB = Path()
        pathNave_MLB.moveTo(x, y + h / 2)                    // Punta (izquierda)
        pathNave_MLB.lineTo(x + w * 0.35f, y + h * 0.05f)    // Ala superior
        pathNave_MLB.lineTo(x + w * 0.6f, y + h * 0.2f)      // Indentación superior
        pathNave_MLB.lineTo(x + w, y + h * 0.25f)             // Cuerpo superior derecho
        pathNave_MLB.lineTo(x + w, y + h * 0.75f)             // Cuerpo inferior derecho
        pathNave_MLB.lineTo(x + w * 0.6f, y + h * 0.8f)      // Indentación inferior
        pathNave_MLB.lineTo(x + w * 0.35f, y + h * 0.95f)    // Ala inferior
        pathNave_MLB.close()
        canvas.drawPath(pathNave_MLB, paintJugador_MLB)

        // Detalle: línea central
        val pathDetalle_MLB = Path()
        pathDetalle_MLB.moveTo(x + w * 0.15f, y + h * 0.45f)
        pathDetalle_MLB.lineTo(x + w * 0.9f, y + h * 0.45f)
        pathDetalle_MLB.lineTo(x + w * 0.9f, y + h * 0.55f)
        pathDetalle_MLB.lineTo(x + w * 0.15f, y + h * 0.55f)
        pathDetalle_MLB.close()
        canvas.drawPath(pathDetalle_MLB, paintJugadorDetalle_MLB)

        // Cabina (detalle visual)
        canvas.drawCircle(x + w * 0.45f, y + h * 0.5f, h * 0.12f, paintCabinaJugador_MLB)
    }

    // ==================== DIBUJAR NAVE ENEMIGA ====================
    // Nave apuntando a la DERECHA (viene desde la izquierda)
    private fun dibujarNaveEnemiga_MLB(canvas: Canvas, enemigo_MLB: NaveEnemiga_MLB) {
        val x = enemigo_MLB.x
        val y = enemigo_MLB.y
        val w = enemigo_MLB.ancho
        val h = enemigo_MLB.alto

        // Motor (llamas en el lado izquierdo)
        val tamLlama_MLB = 10f + random_MLB.nextInt(12).toFloat()
        paintMotor_MLB.color = Color.rgb(255, 80 + random_MLB.nextInt(80), 0)
        val pathLlama_MLB = Path()
        pathLlama_MLB.moveTo(x, y + h * 0.3f)
        pathLlama_MLB.lineTo(x - tamLlama_MLB, y + h * 0.5f)
        pathLlama_MLB.lineTo(x, y + h * 0.7f)
        pathLlama_MLB.close()
        canvas.drawPath(pathLlama_MLB, paintMotor_MLB)

        // Cuerpo de la nave enemiga
        val pathNave_MLB = Path()
        pathNave_MLB.moveTo(x + w, y + h / 2)                 // Punta (derecha)
        pathNave_MLB.lineTo(x + w * 0.65f, y + h * 0.05f)     // Ala superior
        pathNave_MLB.lineTo(x + w * 0.4f, y + h * 0.2f)       // Indentación superior
        pathNave_MLB.lineTo(x, y + h * 0.25f)                  // Cuerpo superior izquierdo
        pathNave_MLB.lineTo(x, y + h * 0.75f)                  // Cuerpo inferior izquierdo
        pathNave_MLB.lineTo(x + w * 0.4f, y + h * 0.8f)       // Indentación inferior
        pathNave_MLB.lineTo(x + w * 0.65f, y + h * 0.95f)     // Ala inferior
        pathNave_MLB.close()
        canvas.drawPath(pathNave_MLB, paintEnemigo_MLB)

        // Detalle central
        val pathDetalle_MLB = Path()
        pathDetalle_MLB.moveTo(x + w * 0.1f, y + h * 0.45f)
        pathDetalle_MLB.lineTo(x + w * 0.85f, y + h * 0.45f)
        pathDetalle_MLB.lineTo(x + w * 0.85f, y + h * 0.55f)
        pathDetalle_MLB.lineTo(x + w * 0.1f, y + h * 0.55f)
        pathDetalle_MLB.close()
        canvas.drawPath(pathDetalle_MLB, paintEnemigoDetalle_MLB)

        // Cabina
        canvas.drawCircle(x + w * 0.55f, y + h * 0.5f, h * 0.1f, paintCabinaEnemigo_MLB)
    }

    // ==================================================================
    //       ENTRADA TÁCTIL (ratón/touch) - onTouchEvent
    // ==================================================================
    // Requisito: "El control de mi nave se ha de poder realizar
    //             desde teclado y desde ratón."
    // Patrón del curso (ver teoría "Entradas del usuario"):
    //   event.getAction() == MotionEvent.ACTION_DOWN / ACTION_MOVE
    // ==================================================================

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gameOver_MLB) return true

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Mover nave a posición Y del touch y disparar
                jugadorY_MLB = (event.y - jugadorAlto_MLB / 2)
                    .coerceIn(10f, (altoJuego_MLB - jugadorAlto_MLB - 10).toFloat())
                disparar_MLB()
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                // Seguir el dedo verticalmente (solo Y, no X - no se mueve horizontalmente)
                jugadorY_MLB = (event.y - jugadorAlto_MLB / 2)
                    .coerceIn(10f, (altoJuego_MLB - jugadorAlto_MLB - 10).toFloat())
                // Auto-disparo mientras se mueve
                if (cooldownDisparo_MLB <= 0) {
                    disparar_MLB()
                }
                invalidate()
            }
        }
        return true
    }

    // ==================================================================
    //       ENTRADA POR TECLADO - onKeyDown / onKeyUp
    // ==================================================================
    // Requisito: "El control de mi nave se ha de poder realizar
    //             desde teclado y desde ratón."
    // Teclas: W/↑ = arriba, S/↓ = abajo, Espacio = disparar
    // ==================================================================

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_W -> {
                moverArriba_MLB = true; return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_S -> {
                moverAbajo_MLB = true; return true
            }
            KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_DPAD_CENTER -> {
                disparar_MLB(); return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_W -> {
                moverArriba_MLB = false; return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_S -> {
                moverAbajo_MLB = false; return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    // ==================== GESTIÓN DE SONIDOS ====================
    fun pausarSonidos_MLB() {
        try { musicaFondo_MLB?.pause() } catch (_: Exception) {}
    }

    fun reanudarSonidos_MLB() {
        try { if (!gameOver_MLB) musicaFondo_MLB?.start() } catch (_: Exception) {}
    }

    fun liberarRecursos_MLB() {
        try {
            musicaFondo_MLB?.stop()
            musicaFondo_MLB?.release()
            musicaFondo_MLB = null
        } catch (_: Exception) {}
        try {
            soundPool_MLB?.release()
            soundPool_MLB = null
        } catch (_: Exception) {}
    }
}
