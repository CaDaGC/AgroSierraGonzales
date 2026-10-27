package pe.edu.upeu.agrosierragonzales.data.repository
import pe.edu.upeu.agrosierragonzales.data.remote.AgroApi
import pe.edu.upeu.agrosierragonzales.domain.model.Sesion
import pe.edu.upeu.agrosierragonzales.domain.repository.AuthRepository
interface SecureSessionStore{fun read():Sesion?;fun write(x:Sesion);fun clear()}
class AuthRepositoryImpl(private val api:AgroApi,private val store:SecureSessionStore):AuthRepository{
 override suspend fun login(usuario:String,contrasena:String)=runCatching{api.login(usuario,contrasena).let{Sesion(it.token,it.usuario,it.nombre,it.rol,it.privilegios).also(store::write)}}
 override suspend fun currentSession()=store.read()
 override suspend fun logout()=store.clear()
}
