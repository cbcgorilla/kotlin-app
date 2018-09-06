#Kotlin快速开发Spring Boot接口应用

##1.数据保存部分：

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

##2. REST接口展示后台数据

                    bean {
                        val bookRepository = ref<BookRepository>()
                        val interval = Flux.interval(Duration.ofMillis(100))
                        router {
                            GET("/books") {
                                ServerResponse.ok()
                                        .contentType(MediaType.TEXT_EVENT_STREAM)
                                        .body(Flux.zip(interval, bookRepository.findAll()).map { it.t2 })
                            }
                        }
                    }
