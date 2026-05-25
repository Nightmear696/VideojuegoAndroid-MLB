package com.example.videojuegoandroid_mlb

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast

/**
 * MenuActivity - Pantalla inicial del juego
 * Permite al usuario introducir su nombre y seleccionar el nivel de dificultad.
 * Requisito: "La primera pantalla que aparezca ha de ser aquella en donde se
 * introduzca el nombre y dos niveles de dificultad del juego." - 1 punto
 */
class MenuActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val editNombre_MLB = findViewById<EditText>(R.id.editNombre_MLB)
        val radioGroupDificultad_MLB = findViewById<RadioGroup>(R.id.radioGroupDificultad_MLB)
        val btnIniciar_MLB = findViewById<Button>(R.id.btnIniciar_MLB)

        btnIniciar_MLB.setOnClickListener {
            val nombre_MLB = editNombre_MLB.text.toString().trim()

            //n Validar que se ha introducido un ombre
            if (nombre_MLB.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Obtener nivel de dificultad seleccionado
            // Nivel 1 = Fácil, Nivel 2 = Difícil
            val dificultad_MLB = if (radioGroupDificultad_MLB.checkedRadioButtonId == R.id.radioFacil_MLB) 1 else 2

            // Iniciar el juego pasando nombre y dificultad por Intent
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("NOMBRE_MLB", nombre_MLB)
            intent.putExtra("DIFICULTAD_MLB", dificultad_MLB)
            startActivity(intent)
        }
    }
}
