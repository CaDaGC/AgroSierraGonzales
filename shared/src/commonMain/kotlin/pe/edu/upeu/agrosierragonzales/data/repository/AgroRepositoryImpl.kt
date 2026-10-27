package pe.edu.upeu.agrosierragonzales.data.repository
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.agrosierragonzales.data.local.LocalStore
import pe.edu.upeu.agrosierragonzales.data.remote.AgroApi
import pe.edu.upeu.agrosierragonzales.data.remote.dto.*
import pe.edu.upeu.agrosierragonzales.domain.model.*
import pe.edu.upeu.agrosierragonzales.domain.repository.*
class AgroRepositoryImpl(private val api:AgroApi,private val db:LocalStore):AgroRepository{
 override fun observeLotes():Flow<List<Lote>> = db.observeLotes()
 override fun observeLabores(id:Long):Flow<List<Labor>> = db.observeLabores(id)
 override suspend fun refreshLotes(valle:String?)=runCatching{db.saveLotes(api.lotes(valle).map{it.toDomain()})}.mapFailure()
 override suspend fun createLabor(labor:Labor):Result<Labor> = runCatching{val x=db.addPending(labor.copy(pendiente=true));try{val r=api.createLabor(LaborRequestDto(x.loteId,x.tipo.name,x.fecha,x.cantidad,x.observacion)).toDomain();db.markSynced(x.id,r);r}catch(_:Throwable){x}}
 override suspend fun sync()=runCatching{var n=0;db.pending().forEach{x->val r=api.createLabor(LaborRequestDto(x.loteId,x.tipo.name,x.fecha,x.cantidad,x.observacion)).toDomain();db.markSynced(x.id,r);n++};val lots=api.lotes();db.saveLotes(lots.map{it.toDomain()});lots.forEach{db.saveLabores(api.labores(it.id).map{d->d.toDomain()})};SyncReport(n,lots.size)}.mapFailure()
}
private fun <T> Result<T>.mapFailure():Result<T> = fold({Result.success(it)},{e->Result.failure(IllegalStateException(when(e){is HttpRequestTimeoutException->"Tiempo de espera agotado";is ResponseException->"HTTP ${e.response.status.value}";else->e.message?:"Sin conexión"},e))})
