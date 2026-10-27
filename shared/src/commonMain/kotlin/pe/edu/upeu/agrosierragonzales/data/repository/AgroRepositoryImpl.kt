package pe.edu.upeu.agrosierragonzales.data.repository
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.agrosierragonzales.data.local.LocalStore
import pe.edu.upeu.agrosierragonzales.data.remote.*
import pe.edu.upeu.agrosierragonzales.data.remote.dto.*
import pe.edu.upeu.agrosierragonzales.domain.model.*
import pe.edu.upeu.agrosierragonzales.domain.repository.*
class AgroRepositoryImpl(private val api:AgroApi,private val db:LocalStore):AgroRepository{
 override fun observeLotes():Flow<List<Lote>> = db.observeLotes()
 override fun observeLabores(id:Long):Flow<List<Labor>> = db.observeLabores(id)
 override suspend fun refreshLotes(valle:String?)=runCatching{db.saveLotes(api.lotes(valle).map{it.toDomain()})}.mapFailure()
 override suspend fun createLabor(labor:Labor):Result<Labor>{
  val local=try{db.addPending(labor.copy(pendiente=true))}catch(e:Throwable){return Result.failure(e)}
  return try{val remote=api.createLabor(local.request()).toDomain();db.markSynced(local.id,remote);Result.success(remote)}
  catch(e:ApiFailure){db.deleteLabor(local.id);Result.failure(e)}
  catch(e:Throwable){if(e is IOException||e is HttpRequestTimeoutException) Result.success(local) else {db.deleteLabor(local.id);Result.failure(e)}}
 }
 override suspend fun editLabor(labor:Labor):Result<Labor> = try{
  if(labor.id<0){val pending=labor.copy(pendiente=true);db.saveLabores(listOf(pending));Result.success(pending)}
  else{val updated=api.editLabor(labor.id,labor.request()).toDomain();db.saveLabores(listOf(updated));Result.success(updated)}
 }catch(e:Throwable){Result.failure(e)}
 override suspend fun deleteLabor(labor:Labor):Result<Unit> = try{
  if(labor.id>0)api.deleteLabor(labor.id)
  db.deleteLabor(labor.id);Result.success(Unit)
 }catch(e:Throwable){Result.failure(e)}
 override suspend fun closeCampaign(loteId:Long):Result<Unit> = try{
  val updated=api.closeCampaign(loteId).toDomain();db.saveLotes(listOf(updated));Result.success(Unit)
 }catch(e:Throwable){Result.failure(e)}
 override suspend fun sync(canReadLabor:Boolean)=runCatching{
  var sent=0
  db.pending().forEach{item->val remote=api.createLabor(item.request()).toDomain();db.markSynced(item.id,remote);sent++}
  val lots=api.lotes();db.saveLotes(lots.map{it.toDomain()})
  if(canReadLabor) lots.forEach{db.saveLabores(api.labores(it.id).map{d->d.toDomain()})}
  SyncReport(sent,lots.size)
 }.mapFailure()
}
private fun Labor.request()=LaborRequestDto(loteId,tipo.name,fecha,cantidad,observacion)
private fun <T> Result<T>.mapFailure():Result<T> = fold({Result.success(it)},{e->Result.failure(when(e){is ApiFailure->e;is HttpRequestTimeoutException->IllegalStateException("Tiempo de espera agotado",e);is IOException->IllegalStateException("Sin conexión",e);else->e})})

