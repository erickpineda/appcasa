package com.appcasa.core.utils

import java.util.Locale

object Constants {
  object Modules {
    const val TASKS = "TASKS"
    const val PETS = "PETS"
    const val CALENDAR = "CALENDAR"
    const val EXPENSES = "EXPENSES"
    const val POSTITS = "POSTITS"
    const val REWARDS = "REWARDS"
  }

  object Formatting {
    const val DATE_FORMAT_ES = "dd/MM/yyyy"
    const val TIME_FORMAT_ES = "HH:mm"
    const val DATETIME_FORMAT_ES = "dd/MM/yyyy HH:mm"
    const val DAY_MONTH_LONG = "d 'de' MMMM"
    const val DAY_MONTH_FULL_ES = "dd 'de' MMMM"
    const val DAY_MONTH_TIME_ES = "d 'de' MMMM HH:mm"
    const val DAY_MONTH_ALL_DAY_ES = "d 'de' MMMM '(Todo el día)'"
    const val YEAR_MONTH_LONG = "MMMM yyyy"
    const val DATE_TIME_FULL_ES = "d MMM yyyy HH:mm"
    const val TWO_DECIMALS = "%.2f"
    const val ONE_DECIMAL = "%.1f"
    const val DATE_ALL_DAY_FORMAT = "d MMM yyyy '%s'"
    const val DATE_TIME_FORMAT = "d MMM yyyy HH:mm"
    const val DOT_SEPARATOR = " • "
  }

  object Media {
    const val MIME_TYPE_IMAGE = "image/*"
  }

  object UI {
    const val LOADING_OVERLAY_ALPHA = 0.3f
    const val DEFAULT_REWARD_ICON = "card_giftcard"
  }

  object Config {
    const val DARK_MODE = "tema_oscuro"
    const val NOTIFICATIONS_ACTIVE = "notif_activas"
    const val PARTNER_NOTIFICATIONS = "notif_pareja"
    const val COMPACT_VIEW = "vista_compacta"
    const val CURRENCY = "moneda"
    const val SHOP_MODE = "modo_tienda"
    const val MAIN_LIST_ID = "lista_compra_id"
    const val BIOMETRIC_LOCK = "biometric_lock_app"

    const val TRUE = "true"
    const val FALSE = "false"

    const val DEFAULT_CURRENCY = "€"
  }

  object Keys {
    const val MORTGAGE_CAPITAL = "MORTGAGE_CAPITAL"
    const val MORTGAGE_INTEREST = "MORTGAGE_INTEREST"
    const val MORTGAGE_YEARS = "MORTGAGE_YEARS"
    const val BMI_HEIGHT = "BMI_HEIGHT"
    const val BMI_WEIGHT = "BMI_WEIGHT"
    const val SAVINGS_GOAL = "SAVINGS_GOAL"
    const val SAVINGS_MONTHS = "SAVINGS_MONTHS"
  }

  object Calendar {
    val WEEKDAYS_ES = listOf("D", "L", "M", "X", "J", "V", "S")
    const val PREFIX_EVENT = "E_"
    const val PREFIX_TASK = "T_"
    const val PREFIX_REMINDER = "R_"
  }

  object Extras {
    const val ID = "id"
    const val TITLE = "title"
    const val MESSAGE = "message"
  }

  object Locales {
    val SPAIN = Locale("es", "ES")
  }

  object Lists {
    const val PREFIX_SHOPPING_ITEM = "COMPRAR: "
  }
}
