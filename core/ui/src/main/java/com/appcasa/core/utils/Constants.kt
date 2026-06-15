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
    const val YEAR_MONTH_LONG = "MMMM yyyy"
  }

  object Media {
    const val MIME_TYPE_IMAGE = "image/*"
  }

  object UI {
    const val LOADING_OVERLAY_ALPHA = 0.3f
  }

  object Locales {
    val SPAIN = Locale("es", "ES")
  }
}
