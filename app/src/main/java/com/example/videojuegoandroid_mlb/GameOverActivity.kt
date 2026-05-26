package com.example.videojuegoandroid_mlb

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class GameOverActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        // Obtener datos del Intent
        val puntuacion_MLB = intent.getIntExtra("PUNTUACION_MLB", 0)
        val nombre_MLB = intent.getStringExtra("NOMBRE_MLB") ?: "Player"
        val dificultad_MLB = intent.getIntExtra("DIFICULTAD_MLB", 1)

        // Mostrar nombre y puntuación
        val txtNombre_MLB = findViewById<TextView>(R.id.txtNombre_MLB)
        val txtPuntuacion_MLB = findViewById<TextView>(R.id.txtPuntuacion_MLB)
        val btnReiniciar_MLB = findViewById<Button>(R.id.btnReiniciar_MLB)
        val btnMenu_MLB = findViewById<Button>(R.id.btnMenu_MLB)

        txtNombre_MLB.text = nombre_MLB
        txtPuntuacion_MLB.text = "Score: $puntuacion_MLB"

        // Botón para reiniciar el juego con los mismos parámetros
        btnReiniciar_MLB.setOnClickListener {
            val intentJuego_MLB = Intent(this, GameActivity::class.java)
            intentJuego_MLB.putExtra("NOMBRE_MLB", nombre_MLB)
            intentJuego_MLB.putExtra("DIFICULTAD_MLB", dificultad_MLB)
            startActivity(intentJuego_MLB)
            finish()
        }

        // Botón para volver al menú principal
        btnMenu_MLB.setOnClickListener {
            val intentMenu_MLB = Intent(this, MenuActivity::class.java)
            startActivity(intentMenu_MLB)
            finish()
        }
    }
}
