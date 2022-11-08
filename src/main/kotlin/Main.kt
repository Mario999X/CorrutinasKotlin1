import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.system.measureTimeMillis

/*
* Otra vez el ejercicio de las capsulas, pero usando corrutinas.
*
*     // Corrutinas
*     implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
*
*/

private const val NUM_CAPSULAS = 75
private const val NUM_CORRUTINAS = 15

private var contadorID: Int = 0
private var totalPasajeros = 0


/*
* El uso de runBlocking es solo recomendable en la funcion principal,
* de esa forma esperamos a que termine lo que haya dentro
*/
fun main() = runBlocking {

    // No es recomendable usar GlobalScope, usaremos coroutineScope
    val capsulas = generaCapsulas()
    //println(capsulas)

    println("Preparando zonas de lanzamiento -- Numero de trabajadores: $NUM_CORRUTINAS | Numero de Capsulas: $NUM_CAPSULAS")
    val tiempo = measureTimeMillis {
        // Generamos la lista de trabajadores -> CONSTRUCTOR DE CORRUTINAS
        val trabajadores = List(NUM_CORRUTINAS) {
            launch(Dispatchers.Default) {
                while (!capsulas.isEmpty()) {
                    val capsula = capsulas.poll() // Retiramos el primer item de la cola concurrente
                    println(
                        """Trabajador: ${Thread.currentThread().name} | Capsula: ${capsula.id}
                   | Pasajeros: ${capsula.pasajeros} 
                   | Tiempo de Lanzamiento: ${capsula.tiempoLanzamiento}
                   | ----------------------------------------------------------------
               """.trimMargin()
                    )
                    totalPasajeros += capsula.pasajeros // Vamos sumando los pasajeros

                    delay(capsula.tiempoLanzamiento)

                    println("\t --Trabajador: ${Thread.currentThread().name} lanzo la capsula -> ${capsula.id} ")
                }
            }
        }
        trabajadores.forEach { it.join() }
    }

    // El resultado de usar runBlocking, ejemplo basico.
    println("\nFin de mision en: ${tiempo / 1000} s \nTotal Pasajeros Salvados: $totalPasajeros")

}

// Generamos una funcion suspendida que devuelve una cola concurrente (segura frente al acceso de multiples hilos)
suspend fun generaCapsulas(): ConcurrentLinkedQueue<Capsula> = coroutineScope {

    val listadoCapsulas = ConcurrentLinkedQueue<Capsula>()

    println("Cargando capsulas: $NUM_CAPSULAS")

    // El aplicar async, hacemos que devuela un resultado.
    val result = async {
        for (i in 0 until NUM_CAPSULAS) {
            contadorID++
            listadoCapsulas.add(Capsula(contadorID))
            print(".")
            delay(5)
        }
        println("\n¡Capsulas generadas!\n")
        delay(1000)
        listadoCapsulas
    }

    return@coroutineScope result.await()
}