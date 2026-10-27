package pe.edu.upeu.agrosierragonzales.presentation
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pe.edu.upeu.agrosierragonzales.domain.model.*
import pe.edu.upeu.agrosierragonzales.domain.repository.*
data class AppUiState(val cargando:Boolean=false,val sesion:Sesion?=null,val lotes:List<Lote> = emptyList(),val loteSeleccionado:Lote?=null,val labores:List<Labor> = emptyList(),val mensaje:String?=null,val sincronizando:Boolean=false)
class AppViewModel(private val auth:AuthRepository,private val agro:AgroRepository){
 private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Main);private val _state=MutableStateFlow(AppUiState());val state:StateFlow<AppUiState> = _state.asStateFlow()
 init{scope.launch{auth.currentSession()?.let{x->_state.update{it.copy(sesion=x)}};agro.observeLotes().collect{x->_state.update{it.copy(lotes=x)}}}}
 fun login(u:String,p:String)=scope.launch{_state.update{it.copy(cargando=true,mensaje=null)};auth.login(u,p).onSuccess{x->_state.update{it.copy(sesion=x)};sync()}.onFailure{x->_state.update{it.copy(mensaje=x.message)}};_state.update{it.copy(cargando=false)}}
 fun logout()=scope.launch{auth.logout();_state.value=AppUiState()}
 fun select(x:Lote)=scope.launch{_state.update{it.copy(loteSeleccionado=x)};agro.observeLabores(x.id).collect{items->_state.update{s->s.copy(labores=items)}}}
 fun refresh()=scope.launch{agro.refreshLotes().onFailure{e->_state.update{it.copy(mensaje=e.message)}}}
 fun createLabor(id:Long,t:TipoLabor,d:String,q:Double,o:String)=scope.launch{agro.createLabor(Labor(0,id,t,d,_state.value.sesion?.nombre.orEmpty(),q,o)).onFailure{e->_state.update{it.copy(mensaje=e.message)}}}
 fun sync()=scope.launch{_state.update{it.copy(sincronizando=true)};agro.sync().onSuccess{r->_state.update{it.copy(mensaje="Sincronización: ${r.enviados} pendientes enviados; ${r.descargados} lotes")}}.onFailure{e->_state.update{it.copy(mensaje=e.message)}};_state.update{it.copy(sincronizando=false)}}
}

