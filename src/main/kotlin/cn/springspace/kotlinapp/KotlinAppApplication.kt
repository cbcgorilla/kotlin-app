package cn.springspace.kotlinapp

import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux
import java.time.Duration
import java.util.*
import java.util.stream.Stream

@SpringBootApplication
class KotlinAppApplication

interface BookRepository : ReactiveMongoRepository<Book, String>

@Document
data class Book(val name: String, val author: String, val publish: Date)

fun main(args: Array<String>) {
    runApplication<KotlinAppApplication>(*args) {
        addInitializers(
                beans {

                    bean {
                        val bookRepository = ref<BookRepository>()
                        val interval = Flux.interval(Duration.ofMillis(100))
                        router {
                            GET("/books") {
                                ServerResponse.ok().contentType(MediaType.TEXT_EVENT_STREAM)
                                        .body(Flux.zip(interval, bookRepository.findAll()).map { entry -> entry.t2 })
                            }
                        }
                    }

                    bean {
                        ApplicationRunner {
                            val bookRepository = ref<BookRepository>()
                            bookRepository.deleteAll()
                                    .thenMany(
                                            Flux.fromStream(
                                                    Stream.generate { "Spring 空间【${UUID.randomUUID()}】" }
                                                            .limit(30)))
                                    .map { Book(it, "Michael Chen", Date()) }
                                    .flatMap { bookRepository.save(it) }
                                    .thenMany(bookRepository.findAll())
                                    .subscribe { println(it) }
                        }
                    }
                }
        )
    }
}
