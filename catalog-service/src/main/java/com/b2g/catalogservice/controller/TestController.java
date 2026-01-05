package com.b2g.catalogservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@Slf4j
@RequiredArgsConstructor
public class TestController {
//    private final CategoryRepository categoryRepository;
//    private final CatalogBookApplicationService bookService;
//    @GetMapping("/create")
//    public ResponseEntity<?> test() {
//        if(categoryRepository.findAll().size()==50) {
//            log.info("Catalog already initialized");
//            return ResponseEntity.ok("Already Initialized Catalog");
//        }
//
//        // 1️⃣ Creazione 50 categorie hardcoded
//        Map<String, UUID> categoriesMap = new HashMap<>();
//        List<Category> categories = List.of(
//                Category.builder().name("Protagonista femminile").description("Protagonista principale di sesso femminile").build(),
//                Category.builder().name("Protagonista maschile").description("Protagonista principale di sesso maschile").build(),
//                Category.builder().name("Sotto i 10 anni").description("Storie con protagonisti giovani").build(),
//                Category.builder().name("Avventura epica").description("Avventure su larga scala").build(),
//                Category.builder().name("Romantico").description("Storie d'amore").build(),
//                Category.builder().name("Fantasy classico").description("Fantasy tradizionale").build(),
//                Category.builder().name("Fantasy moderno").description("Fantasy contemporaneo").build(),
//                Category.builder().name("Giallo investigativo").description("Storie di investigazione").build(),
//                Category.builder().name("Thriller psicologico").description("Thriller basati sulla mente").build(),
//                Category.builder().name("Distopico").description("Storie distopiche").build(),
//                Category.builder().name("Storico").description("Romanzi ambientati in contesti storici").build(),
//                Category.builder().name("Romanzo contemporaneo").description("Romanzi ambientati oggi").build(),
//                Category.builder().name("Romanzo psicologico").description("Focus sulle emozioni e mente dei personaggi").build(),
//                Category.builder().name("Saggistica educativa").description("Opere non fiction").build(),
//                Category.builder().name("Biografia").description("Storie di vita reale").build(),
//                Category.builder().name("Autobiografia").description("Autobiografie").build(),
//                Category.builder().name("Satira").description("Critica umoristica").build(),
//                Category.builder().name("Umoristico").description("Commedia e humor").build(),
//                Category.builder().name("Magia nera").description("Elementi di magia oscura").build(),
//                Category.builder().name("Magia bianca").description("Elementi di magia benevola").build(),
//                Category.builder().name("Steampunk").description("Storie steampunk").build(),
//                Category.builder().name("Cyberpunk").description("Storie cyberpunk").build(),
//                Category.builder().name("Noir").description("Storie noir").build(),
//                Category.builder().name("Horror gotico").description("Horror classico gotico").build(),
//                Category.builder().name("Horror moderno").description("Horror contemporaneo").build(),
//                Category.builder().name("Narrativa femminile").description("Letteratura per donne").build(),
//                Category.builder().name("Narrativa maschile").description("Letteratura per uomini").build(),
//                Category.builder().name("Classico").description("Opere classiche").build(),
//                Category.builder().name("Letteratura per ragazzi").description("Libri per bambini e ragazzi").build(),
//                Category.builder().name("Letteratura YA").description("Young Adult").build(),
//                Category.builder().name("Letteratura per bambini").description("Libri per bambini").build(),
//                Category.builder().name("Racconto breve").description("Storie brevi").build(),
//                Category.builder().name("Romanzo lungo").description("Romanzi estesi").build(),
//                Category.builder().name("Serie di libri").description("Parte di una serie").build(),
//                Category.builder().name("Novella").description("Novelle").build(),
//                Category.builder().name("Protagonista animale").description("Animali protagonisti").build(),
//                Category.builder().name("Protagonista umano").description("Umano protagonista").build(),
//                Category.builder().name("Conflitto familiare").description("Storie di famiglie").build(),
//                Category.builder().name("Viaggio nel tempo").description("Viaggi temporali").build(),
//                Category.builder().name("Mistero storico").description("Misteri nel passato").build(),
//                Category.builder().name("Thriller legale").description("Thriller giudiziari").build(),
//                Category.builder().name("Thriller politico").description("Intrighi politici").build(),
//                Category.builder().name("Avventura spaziale").description("Avventure nello spazio").build(),
//                Category.builder().name("Romanzo epistolare").description("Romanzi fatti di lettere").build(),
//                Category.builder().name("Ambientazione urbana").description("Storie in città").build(),
//                Category.builder().name("Ambientazione rurale").description("Storie in campagna").build(),
//                Category.builder().name("Società segreta").description("Trame con società segrete").build(),
//                Category.builder().name("Intelligenza artificiale").description("Tematiche AI").build(),
//                Category.builder().name("Supereroi").description("Supereroi e poteri").build(),
//                Category.builder().name("Mondi paralleli").description("Storie in universi alternativi").build()
//        );
//
//        List<BookFormatCreateDTO> defaultFormats = List.of(
//                new BookFormatCreateDTO(
//                        "PHYSICAL",
//                        BigDecimal.valueOf(19.99),
//                        40,
//                        true,
//                        true,
//                        false
//
//                ),
//                new BookFormatCreateDTO(
//                        "EBOOK",
//                        BigDecimal.valueOf(9.99),
//                        null,
//                        true,
//                        true,
//                        true
//                ),
//                new BookFormatCreateDTO(
//                        "AUDIOBOOK",
//                        BigDecimal.valueOf(14.99),
//                        null,
//                        true,
//                        false,
//                        true
//                )
//        );
//
//        categories.forEach(cat -> {
//            Category saved = categoryRepository.save(cat);
//            log.info("Saved category: {}", saved.getId());
//            categoriesMap.put(saved.getName(), saved.getId());
//        });
//
//        // 2️⃣ Creazione 20 libri hardcoded
//        List<CatalogBookCreateRequestDTO> booksToCreate = List.of(
//                // 1️⃣
//                CatalogBookCreateRequestDTO.builder()
//                        .title("Viaggio nel tempo di Anna")
//                        .author("Clara Rossi")
//                        .isbn("9781234567001")
//                        .description("Anna scopre di poter viaggiare nel tempo e cambiare eventi storici.")
//                        .publisher("Mondadori")
//                        .publicationDate(LocalDate.of(2015, 3, 10))
//                        .categoryIds(Set.of(
//                                categoriesMap.get("Protagonista femminile"),
//                                categoriesMap.get("Viaggio nel tempo"),
//                                categoriesMap.get("Letteratura YA")
//                        ))
//                        .formats(defaultFormats)
//                        .build(),
//                // 2️⃣
//                CatalogBookCreateRequestDTO.builder()
//                        .title("Mistero a Venezia")
//                        .author("Luca Bianchi")
//                        .isbn("9781234567002")
//                        .description("Un detective deve risolvere un intricato mistero nella città dei canali.")
//                        .publisher("Einaudi")
//                        .publicationDate(LocalDate.of(2018, 9, 21))
//                        .categoryIds(Set.of(
//                                categoriesMap.get("Giallo investigativo"),
//                                categoriesMap.get("Ambientazione urbana"),
//                                categoriesMap.get("Thriller psicologico")
//                        ))
//                        .formats(defaultFormats)
//                        .build(),
//                // 3️⃣
//                CatalogBookCreateRequestDTO.builder()
//                        .title("La principessa e il drago")
//                        .author("Elena Ferri")
//                        .isbn("9781234567003")
//                        .description("Una principessa giovane deve affrontare un drago per salvare il suo regno.")
//                        .publisher("Salani")
//                        .publicationDate(LocalDate.of(2020, 1, 15))
//                        .categoryIds(Set.of(
//                                categoriesMap.get("Protagonista femminile"),
//                                categoriesMap.get("Fantasy classico"),
//                                categoriesMap.get("Avventura epica")
//                        ))
//                        .formats(defaultFormats)
//                        .build(),
//                // 4️⃣
//                CatalogBookCreateRequestDTO.builder()
//                        .title("L'ombra del detective")
//                        .author("Marco Neri")
//                        .isbn("9781234567004")
//                        .description("Un thriller legale con colpi di scena in tribunale.")
//                        .publisher("Rizzoli")
//                        .publicationDate(LocalDate.of(2016, 5, 12))
//                        .categoryIds(Set.of(
//                                categoriesMap.get("Thriller legale"),
//                                categoriesMap.get("Giallo investigativo"),
//                                categoriesMap.get("Romanzo contemporaneo")
//                        ))
//                        .formats(defaultFormats)
//                        .build(),
//                // 5️⃣
//                CatalogBookCreateRequestDTO.builder()
//                        .title("Il futuro oscuro")
//                        .author("Sara Galli")
//                        .isbn("9781234567005")
//                        .description("Romanzo distopico con protagonista femminile in un mondo controllato dalle IA.")
//                        .publisher("Mondadori")
//                        .publicationDate(LocalDate.of(2021, 2, 28))
//                        .categoryIds(Set.of(
//                                categoriesMap.get("Distopico"),
//                                categoriesMap.get("Protagonista femminile"),
//                                categoriesMap.get("Intelligenza artificiale")
//                        ))
//                        .formats(defaultFormats)
//                        .build(),
//                // 6️⃣
//                CatalogBookCreateRequestDTO.builder()
//                        .title("Avventura nello spazio profondo")
//                        .author("Giovanni Rizzo")
//                        .isbn("9781234567006")
//                        .description("Missione esplorativa tra pianeti inesplorati e alieni ostili.")
//                        .publisher("Einaudi")
//                        .publicationDate(LocalDate.of(2019, 7, 19))
//                        .categoryIds(Set.of(
//                                categoriesMap.get("Avventura spaziale"),
//                                categoriesMap.get("Protagonista maschile")
//                        ))
//                        .formats(defaultFormats)
//                        .build(),
//                // 7️⃣
//                CatalogBookCreateRequestDTO.builder()
//                        .title("Il segreto della società segreta")
//                        .author("Laura Conti")
//                        .isbn("9781234567007")
//                        .description("Un gruppo di giovani indaga su antichi misteri.")
//                        .publisher("Salani")
//                        .publicationDate(LocalDate.of(2017, 11, 3))
//                        .categoryIds(Set.of(
//                                categoriesMap.get("Società segreta"),
//                                categoriesMap.get("Narrativa femminile"),
//                                categoriesMap.get("Mistero storico")
//                        ))
//                        .formats(defaultFormats)
//                        .build(),
//                // 8️⃣
//                CatalogBookCreateRequestDTO.builder()
//                        .title("Romantico destino")
//                        .author("Francesca De Luca")
//                        .isbn("9781234567008")
//                        .description("Storia d'amore intensa tra due giovani in una città italiana.")
//                        .publisher("Rizzoli")
//                        .publicationDate(LocalDate.of(2014, 6, 20))
//                        .categoryIds(Set.of(
//                                categoriesMap.get("Romantico"),
//                                categoriesMap.get("Protagonista femminile"),
//                                categoriesMap.get("Romanzo contemporaneo")
//                        ))
//                        .formats(defaultFormats)
//                        .build(),
//                // 9️⃣
//                CatalogBookCreateRequestDTO.builder()
//                        .title("Il mistero del castello")
//                        .author("Andrea Fontana")
//                        .isbn("9781234567009")
//                        .description("Giallo investigativo ambientato in un antico castello medievale.")
//                        .publisher("Mondadori")
//                        .publicationDate(LocalDate.of(2013, 10, 11))
//                        .categoryIds(Set.of(
//                                categoriesMap.get("Giallo investigativo"),
//                                categoriesMap.get("Storico"),
//                                categoriesMap.get("Thriller psicologico")
//                        ))
//                        .formats(defaultFormats)
//                        .build(),
//                // 10️⃣
//                CatalogBookCreateRequestDTO.builder()
//                        .title("La leggenda del mago oscuro")
//                        .author("Elisa Marchetti")
//                        .isbn("9781234567010")
//                        .description("Magia nera e avventure epiche in un mondo fantasy.")
//                        .publisher("Salani")
//                        .publicationDate(LocalDate.of(2018, 8, 5))
//                        .categoryIds(Set.of(
//                                categoriesMap.get("Magia nera"),
//                                categoriesMap.get("Fantasy classico"),
//                                categoriesMap.get("Avventura epica")
//                        ))
//                        .formats(defaultFormats)
//                        .build(),
//                // 11️⃣
//                CatalogBookCreateRequestDTO.builder()
//                        .title("Il piccolo esploratore")
//                        .author("Matteo Rossi")
//                        .isbn("9781234567011")
//                        .description("Un bambino sotto i 10 anni parte per un'avventura indimenticabile.")
//                        .publisher("Einaudi")
//                        .publicationDate(LocalDate.of(2016, 12, 1))
//                        .categoryIds(Set.of(
//                                categoriesMap.get("Sotto i 10 anni"),
//                                categoriesMap.get("Avventura epica"),
//                                categoriesMap.get("Letteratura per ragazzi")
//                        ))
//                        .formats(defaultFormats)
//                        .build(),
//                // 12️⃣
//                CatalogBookCreateRequestDTO.builder()
//                        .title("Cyberpunk City")
//                        .author("Marco Lupo")
//                        .isbn("9781234567012")
//                        .description("Thriller futuristico in una città governata da corporazioni.")
//                        .publisher("Mondadori")
//                        .publicationDate(LocalDate.of(2020, 4, 14))
//                        .categoryIds(Set.of(
//                                categoriesMap.get("Cyberpunk"),
//                                categoriesMap.get("Thriller politico"),
//                                categoriesMap.get("Ambientazione urbana")
//                        ))
//                        .formats(defaultFormats)
//                        .build(),
//                // 13️⃣
//                CatalogBookCreateRequestDTO.builder()
//                        .title("Il viaggio della principessa")
//                        .author("Elena Ferri")
//                        .isbn("9781234567013")
//                        .description("Una giovane principessa intraprende un viaggio epico in terre sconosciute.")
//                        .publisher("Salani")
//                        .publicationDate(LocalDate.of(2019, 9, 18))
//                        .categoryIds(Set.of(
//                                categoriesMap.get("Protagonista femminile"),
//                                categoriesMap.get("Avventura epica"),
//                                categoriesMap.get("Fantasy moderno")
//                        ))
//                        .formats(defaultFormats)
//                        .build(),
//                // 14️⃣
//                CatalogBookCreateRequestDTO.builder()
//                        .title("Romanzo psicologico intenso")
//                        .author("Giorgia Neri")
//                        .isbn("9781234567014")
//                        .description("Studio delle emozioni dei protagonisti in contesti contemporanei.")
//                        .publisher("Einaudi")
//                        .publicationDate(LocalDate.of(2017, 3, 22))
//                        .categoryIds(Set.of(
//                                categoriesMap.get("Romanzo psicologico"),
//                                categoriesMap.get("Narrativa femminile"),
//                                categoriesMap.get("Conflitto familiare")
//                        ))
//                        .formats(defaultFormats)
//                        .build(),
//                // 15️⃣
//                CatalogBookCreateRequestDTO.builder()
//                        .title("Steampunk Adventures")
//                        .author("Leonardo Bianchi")
//                        .isbn("9781234567015")
//                        .description("Avventure in un mondo steampunk con macchine incredibili.")
//                        .publisher("Mondadori")
//                        .publicationDate(LocalDate.of(2015, 5, 9))
//                        .categoryIds(Set.of(
//                                categoriesMap.get("Steampunk"),
//                                categoriesMap.get("Cyberpunk"),
//                                categoriesMap.get("Avventura epica"),
//                                categoriesMap.get("Fantasy moderno")
//                        ))
//                        .formats(defaultFormats)
//                        .build(),
//                // 16️⃣
//                CatalogBookCreateRequestDTO.builder()
//                        .title("Horror nella villa abbandonata")
//                        .author("Sara Conti")
//                        .isbn("9781234567016")
//                        .description("Horror gotico con misteri e segreti nella villa antica.")
//                        .publisher("Rizzoli")
//                        .publicationDate(LocalDate.of(2016, 10, 31))
//                        .categoryIds(Set.of(
//                                categoriesMap.get("Horror gotico"),
//                                categoriesMap.get("Mistero storico"),
//                                categoriesMap.get("Thriller psicologico")
//                        ))
//                        .formats(defaultFormats)
//                        .build(),
//                // 17️⃣
//                CatalogBookCreateRequestDTO.builder()
//                        .title("Supereroi e poteri nascosti")
//                        .author("Filippo Rossi")
//                        .isbn("9781234567017")
//                        .description("Giovani con superpoteri affrontano sfide epiche.")
//                        .publisher("Mondadori")
//                        .publicationDate(LocalDate.of(2018, 1, 5))
//                        .categoryIds(Set.of(
//                                categoriesMap.get("Supereroi"),
//                                categoriesMap.get("Avventura epica"),
//                                categoriesMap.get("Protagonista maschile")
//                        ))
//                        .formats(defaultFormats)
//                        .build(),
//                // 18️⃣
//                CatalogBookCreateRequestDTO.builder()
//                        .title("Mondi paralleli")
//                        .author("Clara Bianchi")
//                        .isbn("9781234567018")
//                        .description("Viaggi tra universi alternativi con conseguenze inaspettate.")
//                        .publisher("Salani")
//                        .publicationDate(LocalDate.of(2019, 6, 12))
//                        .categoryIds(Set.of(
//                                categoriesMap.get("Mondi paralleli"),
//                                categoriesMap.get("Fantasy moderno"),
//                                categoriesMap.get("Protagonista femminile")
//                        ))
//                        .formats(defaultFormats)
//                        .build(),
//                // 19️⃣
//                CatalogBookCreateRequestDTO.builder()
//                        .title("La novella del villaggio")
//                        .author("Giorgio Verdi")
//                        .isbn("9781234567019")
//                        .description("Storia breve ambientata in un piccolo villaggio rurale.")
//                        .publisher("Einaudi")
//                        .publicationDate(LocalDate.of(2014, 8, 15))
//                        .categoryIds(Set.of(
//                                categoriesMap.get("Novella"),
//                                categoriesMap.get("Ambientazione rurale"),
//                                categoriesMap.get("Racconto breve")
//                        ))
//                        .formats(defaultFormats)
//                        .build(),
//                // 20️⃣
//                CatalogBookCreateRequestDTO.builder()
//                        .title("Intelligenza artificiale e futuro")
//                        .author("Luca Moretti")
//                        .isbn("9781234567020")
//                        .description("Saggistica educativa sulla tecnologia e AI.")
//                        .publisher("Mondadori")
//                        .publicationDate(LocalDate.of(2020, 11, 20))
//                        .categoryIds(Set.of(
//                                categoriesMap.get("Intelligenza artificiale"),
//                                categoriesMap.get("Saggistica educativa"),
//                                categoriesMap.get("Romanzo contemporaneo")
//                        ))
//                        .formats(defaultFormats)
//                        .build()
//        );
//
//        // 3️⃣ Persistenza dei libri tramite BookService
//        for (CatalogBookCreateRequestDTO request : booksToCreate) {
//            try {
//                bookService.createBook(request); // invia anche evento RabbitMQ
//            } catch (Exception e) {
//                log.error("Errore durante la creazione libro: {}", request.title());
//                log.error(e.getMessage());
//            }
//        }
//
//        System.out.println("Inizializzazione completata: categorie e libri creati!");
//
//
//        return ResponseEntity.ok("Initialized Catalog");
//    }
}
