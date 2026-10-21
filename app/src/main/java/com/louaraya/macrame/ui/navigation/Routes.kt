package com.louaraya.macrame.ui.navigation

object Routes {
    // autenticacion
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"

    // Pestañas principales
    const val HOME = "home"
    const val CATALOG = "catalog"
    const val MAP = "map"
    const val REQUESTS = "requests"
    const val PROFILE = "profile"
    const val ADMIN_PRODUCTS = "admin_products"
    const val ADMIN_CAMPAIGNS = "admin_campaigns"

    // Pantallas secundarias
    const val PRODUCT = "product/{id}"
    const val SCANNER = "scanner"
    const val FAVORITES = "favorites"
    const val NOTIFICATIONS = "notifications"
    const val REQUEST_FORM = "request_form?productId={productId}"
    const val ADMIN_PRODUCT_FORM = "admin_product_form/{id}"
    const val ADMIN_CAMPAIGN_FORM = "admin_campaign_form/{id}"

    fun product(id: Long) = "product/$id"
    fun requestForm(productId: Long? = null) =
        if (productId == null) "request_form" else "request_form?productId=$productId"
    fun adminProductForm(id: Long) = "admin_product_form/$id"
    fun adminCampaignForm(id: Long) = "admin_campaign_form/$id"

    val publicRoutes = setOf(WELCOME, LOGIN, REGISTER)
}
