package com.louaraya.macrame.data

import com.louaraya.macrame.domain.Category
import com.louaraya.macrame.domain.FairType
import com.louaraya.macrame.util.Dates
import com.louaraya.macrame.util.Formatters

/** Datos ficticios para la demostracion */
object SeedData {
    const val ADMIN_EMAIL = "admin@louaraya.cl"
    const val ADMIN_PASSWORD = "Admin123!"
    const val DEMO_EMAIL = "valentina@demo.cl"
    const val DEMO_PASSWORD = "Demo1234"

    fun products(): List<ProductEntity> = listOf(
        product(1, "Arco Ceremonial Aliento", Category.MATRIMONIOS, 220, 180, "Algodón natural", 120_000, 0, true,
            "Arco de cuerda trenzada con follaje seco, pensado para ceremonias al aire libre. Se confecciona a pedido según el espacio."),
        product(2, "Tapiz Mural Sol Andino", Category.INTERIORES, 90, 70, "Algodón natural", 65_000, 5, false,
            "Tapiz tejido a mano e inspirado en la naturaleza andina. Aporta calidez y textura a livings y dormitorios."),
        product(3, "Colgante de Plantas Quillay", Category.INTERIORES, 40, 110, "Algodón reciclado", 28_000, 12, false,
            "Colgante para maceteros con nudos en espiral. Soporta macetas de hasta 3 kilos."),
        product(4, "Cortina Decorativa Maitén", Category.INTERIORES, 140, 220, "Algodón y lino", 85_000, 3, false,
            "Cortina de hilos largos que filtra la luz y separa ambientes sin cerrarlos."),
        product(5, "Atrapasueños Lluvia de Luna", Category.FESTIVALES, 50, 80, "Yute", 32_000, 8, false,
            "Atrapasueños de gran formato con plumas teñidas, ideal para stands y zonas de descanso en festivales."),
        product(6, "Pasillo de Ceremonia Peumo", Category.MATRIMONIOS, 300, 200, "Algodón natural", 185_000, 0, true,
            "Estructura de cortinas tejidas para enmarcar un pasillo nupcial. Confección a pedido con visita técnica."),
        product(7, "Bandera Festival Brisa", Category.FESTIVALES, 70, 120, "Cuerda teñida", 45_000, 6, false,
            "Banderola de macramé con flecos degradados, liviana y resistente a la intemperie."),
        product(8, "Corredor de Mesa Copihue", Category.MATRIMONIOS, 40, 200, "Algodón natural", 58_000, 4, false,
            "Corredor de mesa calado con flecos en los extremos. Funciona para mesas de hasta 8 personas."),
        product(9, "Cabecera Tejida Raulí", Category.INTERIORES, 160, 90, "Lino", 140_000, 0, true,
            "Cabecera de cama en macramé de gran formato. Se ajusta al ancho de tu cama."),
        product(10, "Carpa Nómade", Category.FESTIVALES, 200, 250, "Cuerda teñida", 210_000, 0, true,
            "Cubierta decorativa para carpas y zonas lounge de eventos. Confección a pedido.")
    )

    fun fairs(): List<FairEntity> = listOf(
        FairEntity(
            name = "Feria Pueblo Artesanal", type = FairType.FERIA, place = "Parque Bicentenario, Vitacura",
            dateText = "12 – 14 Nov 2026", description = "Stand con colección de interiores y toma de pedidos personalizados.",
            lat = -33.4013, lng = -70.5989
        ),
        FairEntity(
            name = "Showroom Lou Araya", type = FairType.SHOWROOM, place = "Av. Providencia, Providencia",
            dateText = "Lun a Sáb · 10:00 – 19:00", description = "Exhibición permanente de piezas para matrimonios y hogar. Atención con cita.",
            lat = -33.4248, lng = -70.6104
        ),
        FairEntity(
            name = "Festival Raíces", type = FairType.EVENTO, place = "Parque Araucano, Las Condes",
            dateText = "5 – 6 Dic 2026", description = "Instalación de atrapasueños y banderas de macramé en la zona de descanso.",
            lat = -33.4046, lng = -70.5744
        ),
        FairEntity(
            name = "Encuentro Cultural Ñuñoa", type = FairType.FERIA, place = "Plaza Ñuñoa, Ñuñoa",
            dateText = "20 Nov 2026", description = "Feria de diseño local con talleres cortos de nudos básicos.",
            lat = -33.4561, lng = -70.5974
        ),
        FairEntity(
            name = "Expo Matrimonios", type = FairType.EVENTO, place = "Espacio Riesco, Huechuraba",
            dateText = "17 – 18 Ene 2027", description = "Muestra de arcos y pasillos de ceremonia con asesoría de decoración.",
            lat = -33.3783, lng = -70.6481
        )
    )

    fun campaigns(): List<CampaignEntity> = listOf(
        CampaignEntity(
            name = "Bodas de Ensueño",
            description = "Arcos, pasillos y corredores para una ceremonia que se recuerde.",
            theme = Category.MATRIMONIOS,
            startDate = Dates.utcMillisFromNow(-20), endDate = Dates.utcMillisFromNow(90), published = true
        ),
        CampaignEntity(
            name = "Festivales Libres",
            description = "Atrapasueños, banderas y carpas para encuentros al aire libre.",
            theme = Category.FESTIVALES,
            startDate = Dates.utcMillisFromNow(-5), endDate = Dates.utcMillisFromNow(45), published = true
        ),
        CampaignEntity(
            name = "Hogares con Alma",
            description = "Tapices, cortinas y colgantes para decorar tus espacios.",
            theme = Category.INTERIORES,
            startDate = Dates.utcMillisFromNow(30), endDate = Dates.utcMillisFromNow(120), published = true
        ),
        CampaignEntity(
            name = "Invierno Cálido",
            description = "Borrador de campaña de invierno con cabeceras y cortinas gruesas.",
            theme = Category.INTERIORES,
            startDate = Dates.utcMillisFromNow(150), endDate = Dates.utcMillisFromNow(220), published = false
        )
    )

    /** Pares (indice de campaña, codigos de producto) para poblar la tabla de asociaciones. */
    val campaignProductIndexes: List<List<Int>> = listOf(
        listOf(1, 6, 8),
        listOf(5, 7, 10),
        listOf(2, 3, 4),
        listOf(9, 4)
    )

    private fun product(
        n: Long, name: String, category: String, w: Int, h: Int, material: String,
        price: Int, stock: Int, madeToOrder: Boolean, description: String
    ) = ProductEntity(
        code = Formatters.productCode(n), name = name, description = description, category = category,
        widthCm = w, heightCm = h, material = material, priceClp = price, stock = stock,
        madeToOrder = madeToOrder, active = true
    )
}
