package ec.edu.ug.proyectodesarrollomovil.ui.common

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import ec.edu.ug.proyectodesarrollomovil.R
import ec.edu.ug.proyectodesarrollomovil.ui.courses.CoursesActivity

enum class BottomNavTab {
    COURSES,
    DISCUSSIONS
}

/**
 * Cablea el BottomNavigationView compartido. La tab "Discusiones" no tiene una pantalla
 * propia sin un forumId concreto (los foros de Moodle cuelgan de un módulo dentro de un
 * curso), así que solo se muestra como destino navegable cuando ya estamos parados ahí;
 * desde las otras pantallas queda deshabilitada en vez de simular una navegación rota.
 */
fun AppCompatActivity.setupBottomNav(bottomNav: BottomNavigationView, currentTab: BottomNavTab) {
    bottomNav.selectedItemId = when (currentTab) {
        BottomNavTab.COURSES -> R.id.nav_courses
        BottomNavTab.DISCUSSIONS -> R.id.nav_discussions
    }

    bottomNav.menu.findItem(R.id.nav_discussions).isEnabled = currentTab == BottomNavTab.DISCUSSIONS

    bottomNav.setOnItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_courses -> {
                if (currentTab != BottomNavTab.COURSES) {
                    startActivity(CoursesActivity.newIntent(this))
                    finish()
                }
                true
            }
            R.id.nav_discussions -> true
            else -> false
        }
    }
}
