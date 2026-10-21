package com.louaraya.macrame

import com.louaraya.macrame.domain.Validators
import com.louaraya.macrame.ui.viewmodel.ScannerViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Pruebas unitarias de la lógica de validación (no requieren emulador). */
class ValidatorsTest {

    @Test fun email_valido_e_invalido() {
        assertNull(Validators.email("valentina@demo.cl"))
        assertNotNull(Validators.email("valentina@demo"))
        assertNotNull(Validators.email(""))
    }

    @Test fun password_exige_mayuscula_minuscula_y_numero() {
        assertNull(Validators.password("Demo1234"))
        assertEquals("Mínimo 8 caracteres", Validators.password("Ab1"))
        assertEquals("Incluye una mayúscula", Validators.password("demo1234"))
        assertEquals("Incluye un número", Validators.password("DemoDemoo"))
    }

    @Test fun confirmacion_de_password() {
        assertNull(Validators.confirmPassword("Demo1234", "Demo1234"))
        assertEquals("Las contraseñas no coinciden", Validators.confirmPassword("Demo1234", "Demo12345"))
    }

    @Test fun telefono_chileno() {
        assertNull(Validators.phone("+56 9 1234 5678"))
        assertNull(Validators.phone("912345678"))
        assertNotNull(Validators.phone("12345"))
    }

    @Test fun nombre_completo_requiere_apellido() {
        assertNull(Validators.fullName("Valentina Rojas"))
        assertEquals("Incluye nombre y apellido", Validators.fullName("Valentina"))
        assertEquals("Usa solo letras y espacios", Validators.fullName("Vale123 Rojas"))
    }

    @Test fun precio_y_dimensiones_en_rango() {
        assertNull(Validators.price("45000"))
        assertNotNull(Validators.price("500"))
        assertNull(Validators.dimension("120", "el ancho"))
        assertNotNull(Validators.dimension("5", "el ancho"))
        assertNotNull(Validators.dimension("abc", "el ancho"))
    }

    @Test fun fechas_de_campana() {
        assertNull(Validators.endDate(1_000L, 2_000L))
        assertEquals("Debe ser posterior al inicio", Validators.endDate(2_000L, 1_000L))
        assertNotNull(Validators.startDate(null))
    }

    @Test fun campana_exige_productos() {
        assertNotNull(Validators.campaignProducts(emptySet()))
        assertNull(Validators.campaignProducts(setOf(1L)))
    }

    @Test fun codigo_qr_se_extrae_de_texto_o_url() {
        assertEquals("LAM-0001", ScannerViewModel.extractProductCode("lam-0001"))
        assertEquals("LAM-0007", ScannerViewModel.extractProductCode("https://louaraya.cl/p/LAM-0007"))
        assertNull(ScannerViewModel.extractProductCode("hola"))
    }
}
