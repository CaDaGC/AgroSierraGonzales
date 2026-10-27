package pe.edu.upeu.agrosierragonzales.data.remote
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import pe.edu.upeu.agrosierragonzales.data.remote.dto.*
class AgroApi(private val c:HttpClient){
 suspend fun login(u:String,p:String)=c.post("api/v1/auth/login"){contentType(ContentType.Application.Json);setBody(LoginRequestDto(u,p))}.bodyOrFailure<LoginResponseDto>()
 suspend fun lotes(v:String?=null)=c.get("api/v1/lotes"){parameter("pagina",0);parameter("tamanio",100);v?.let{parameter("valle",it)}}.bodyOrFailure<PageDto<LoteDto>>().contenido
 suspend fun labores(id:Long)=c.get("api/v1/lotes/$id/labores"){parameter("pagina",0);parameter("tamanio",100)}.bodyOrFailure<PageDto<LaborDto>>().contenido
 suspend fun createLabor(x:LaborRequestDto)=c.post("api/v1/labores"){contentType(ContentType.Application.Json);setBody(x)}.bodyOrFailure<LaborDto>()
}
