package pe.edu.upeu.agrosierragonzales.data.local
import android.content.Context
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import pe.edu.upeu.agrosierragonzales.data.local.db.AgroDatabase
import pe.edu.upeu.agrosierragonzales.domain.model.*
class AndroidLocalStore(c:Context):LocalStore{
 private val db=AgroDatabase(AndroidSqliteDriver(AgroDatabase.Schema,c,"agrosierra.db"))
 override fun observeLotes():Flow<List<Lote>> = db.loteQueries.selectAll().asFlow().mapToList(Dispatchers.IO).map{it.map{r->Lote(r.id,r.codigo,r.nombre,r.hectareas,r.cultivo,r.valle,r.campaniaCerrada != 0L,r.dosisMaximaPorHectarea,r.ultimaActualizacion)}}
 override fun observeLabores(id:Long):Flow<List<Labor>> = db.laborQueries.selectByLote(id).asFlow().mapToList(Dispatchers.IO).map{it.map{r->Labor(r.id,r.loteId,TipoLabor.valueOf(r.tipo),r.fecha,r.responsable,r.cantidad,r.observacion,r.pendiente != 0L)}}
 override suspend fun saveLotes(x:List<Lote>){db.transaction{x.forEach{db.loteQueries.replaceAll(it.id,it.codigo,it.nombre,it.hectareas,it.cultivo,it.valle,if(it.campaniaCerrada) 1L else 0L,it.dosisMaximaPorHectarea,it.ultimaActualizacion)}}}
 override suspend fun saveLabores(x:List<Labor>){db.transaction{x.forEach{db.laborQueries.insertLabor(it.id,it.loteId,it.tipo.name,it.fecha,it.responsable,it.cantidad,it.observacion,if(it.pendiente) 1L else 0L)}}}
 override suspend fun addPending(x:Labor):Labor{var a=x;db.transaction{a=x.copy(id=db.laborQueries.nextTemporaryId().executeAsOne(),pendiente=true);db.laborQueries.insertLabor(a.id,a.loteId,a.tipo.name,a.fecha,a.responsable,a.cantidad,a.observacion,1L)};return a}
 override suspend fun pending()=db.laborQueries.selectPending().executeAsList().map{Labor(it.id,it.loteId,TipoLabor.valueOf(it.tipo),it.fecha,it.responsable,it.cantidad,it.observacion,it.pendiente != 0L)}
 override suspend fun markSynced(localId:Long,x:Labor){db.transaction{db.laborQueries.deleteLocal(localId);db.laborQueries.insertLabor(x.id,x.loteId,x.tipo.name,x.fecha,x.responsable,x.cantidad,x.observacion,0L)}}
 override suspend fun deleteLabor(id:Long){db.transaction{db.laborQueries.deleteById(id)}}
}




