package pe.edu.upeu.agrosierragonzales
import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import pe.edu.upeu.agrosierragonzales.di.androidModules
class AgroSierraApplication:Application(){override fun onCreate(){super.onCreate();startKoin{androidContext(this@AgroSierraApplication);modules(androidModules)}}}
