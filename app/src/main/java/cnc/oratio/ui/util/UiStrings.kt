package cnc.oratio.ui.util

object UiStrings {
    fun allPrayers(lang: String): String = when (lang) {
        "pt" -> "Todas as Orações"
        "es" -> "Todas las Oraciones"
        "la" -> "Omnes Precationes"
        else -> "All Prayers"
    }

    fun favorites(lang: String): String = when (lang) {
        "pt" -> "Favoritas ⭐"
        "es" -> "Favoritas ⭐"
        "la" -> "Favores ⭐"
        else -> "Favorites ⭐"
    }

    fun categoryName(categoryId: String, fallbackName: String, lang: String): String = when (categoryId) {
        "basic" -> when (lang) {
            "pt" -> "Orações Básicas"
            "es" -> "Oraciones Básicas"
            "la" -> "Precationes Basicae"
            else -> "Basic Prayers"
        }
        "psalms" -> when (lang) {
            "pt" -> "Salmos"
            "es" -> "Salmos"
            "la" -> "Psalmi"
            else -> "Psalms"
        }
        "marian" -> when (lang) {
            "pt" -> "Orações Marianas"
            "es" -> "Oraciones Marianas"
            "la" -> "Precationes Marianae"
            else -> "Marian Prayers"
        }
        "rosary" -> when (lang) {
            "pt" -> "Santo Rosário"
            "es" -> "Santo Rosario"
            "la" -> "Sanctum Rosarium"
            else -> "Holy Rosary"
        }
        else -> fallbackName
    }

    fun noPrayersFound(lang: String): String = when (lang) {
        "pt" -> "Nenhuma oração encontrada."
        "es" -> "No se encontraron oraciones."
        "la" -> "Nulla precatio inventa."
        else -> "No prayers found."
    }

    fun audioNarrationPlaying(lang: String): String = when (lang) {
        "pt" -> "Narração de Áudio • Reproduzindo"
        "es" -> "Narración de Audio • Reproduciendo"
        "la" -> "Recitatio Audio • Ludentem"
        else -> "Audio Narration • Playing"
    }

    fun prayerDetailsTitle(lang: String): String = when (lang) {
        "pt" -> "Detalhes da Oração"
        "es" -> "Detalles de la Oración"
        "la" -> "Precationis Detail"
        else -> "Prayer Details"
    }

    fun copiedToClipboard(lang: String): String = when (lang) {
        "pt" -> "Texto da oração copiado para a área de transferência!"
        "es" -> "¡Texto de la oración copiado al portapapeles!"
        "la" -> "Textus precationis exscriptus est!"
        else -> "Prayer text copied to clipboard!"
    }

    fun searchPlaceholder(lang: String): String = when (lang) {
        "pt" -> "Pesquisar oração ou passagem..."
        "es" -> "Buscar oración o pasaje..."
        "la" -> "Quaerere precationem..."
        else -> "Search prayer or passage..."
    }

    fun primaryLanguageLabel(lang: String): String = when (lang) {
        "pt" -> "Idioma Principal:"
        "es" -> "Idioma Principal:"
        "la" -> "Lingua Principalis:"
        else -> "Primary Language:"
    }

    fun appSubtitle(lang: String): String = when (lang) {
        "pt" -> "Orações e Devoções Multilíngues"
        "es" -> "Oraciones y Devociones Multilingües"
        "la" -> "Precationes et Devotiones Multilingues"
        else -> "Multilingual Prayers & Devotions"
    }

    // Reminders & Notifications Strings
    fun remindersTitle(lang: String): String = when (lang) {
        "pt" -> "Lembretes de Oração"
        "es" -> "Recordatorios de Oración"
        "la" -> "Precationum Monitiones"
        else -> "Prayer Reminders"
    }

    fun addReminder(lang: String): String = when (lang) {
        "pt" -> "Novo Lembrete"
        "es" -> "Nuevo Recordatorio"
        "la" -> "Nova Monitio"
        else -> "New Reminder"
    }

    fun newReminder(lang: String): String = addReminder(lang)

    fun noReminders(lang: String): String = when (lang) {
        "pt" -> "Nenhum lembrete agendado."
        "es" -> "No hay recordatorios programados."
        "la" -> "Nulla monitio ordinata."
        else -> "No reminders scheduled."
    }

    fun prayerSelection(lang: String): String = when (lang) {
        "pt" -> "Oração Selecionada"
        "es" -> "Oración Seleccionada"
        "la" -> "Precatio Selecta"
        else -> "Selected Prayer"
    }

    fun dailyFeaturedPrayer(lang: String): String = when (lang) {
        "pt" -> "Oração do Dia (Aleatória)"
        "es" -> "Oración del Día (Aleatoria)"
        "la" -> "Precatio Diurna (Aleatoria)"
        else -> "Daily Featured Prayer (Random)"
    }

    fun randomDailyPrayer(lang: String): String = dailyFeaturedPrayer(lang)

    fun prayerNotFound(lang: String): String = when (lang) {
        "pt" -> "Oração Não Encontrada"
        "es" -> "Oración No Encontrada"
        "la" -> "Precatio Non Inventa"
        else -> "Prayer Not Found"
    }

    fun frequency(lang: String): String = when (lang) {
        "pt" -> "Frequência"
        "es" -> "Frecuencia"
        "la" -> "Frequentia"
        else -> "Frequency"
    }

    fun daily(lang: String): String = when (lang) {
        "pt" -> "Diária"
        "es" -> "Diaria"
        "la" -> "Diurna"
        else -> "Daily"
    }

    fun specificDays(lang: String): String = when (lang) {
        "pt" -> "Selecionar Dias"
        "es" -> "Seleccionar Días"
        "la" -> "Eligere Dies"
        else -> "Select Days"
    }

    fun weekly(lang: String): String = when (lang) {
        "pt" -> "Semanal"
        "es" -> "Semanal"
        "la" -> "Hebdomadalis"
        else -> "Weekly"
    }

    fun sun(lang: String): String = when (lang) {
        "pt" -> "Dom"
        "es" -> "Dom"
        "la" -> "Dom"
        else -> "Sun"
    }

    fun mon(lang: String): String = when (lang) {
        "pt" -> "Seg"
        "es" -> "Lun"
        "la" -> "Sec"
        else -> "Mon"
    }

    fun tue(lang: String): String = when (lang) {
        "pt" -> "Ter"
        "es" -> "Mar"
        "la" -> "Ter"
        else -> "Tue"
    }

    fun wed(lang: String): String = when (lang) {
        "pt" -> "Qua"
        "es" -> "Mié"
        "la" -> "Mer"
        else -> "Wed"
    }

    fun thu(lang: String): String = when (lang) {
        "pt" -> "Qui"
        "es" -> "Jue"
        "la" -> "Iov"
        else -> "Thu"
    }

    fun fri(lang: String): String = when (lang) {
        "pt" -> "Sex"
        "es" -> "Vie"
        "la" -> "Ven"
        else -> "Fri"
    }

    fun sat(lang: String): String = when (lang) {
        "pt" -> "Sáb"
        "es" -> "Sáb"
        "la" -> "Sat"
        else -> "Sat"
    }

    fun dailyHint(lang: String): String = when (lang) {
        "pt" -> "O alarme será disparado todos os dias"
        "es" -> "El recordatorio se enviará todos los días"
        "la" -> "Monitio cotidie mittetur"
        else -> "Reminder will be sent every day"
    }

    fun selectDays(lang: String): String = when (lang) {
        "pt" -> "Dias da Semana:"
        "es" -> "Días de la Semana:"
        "la" -> "Dies Hebdomadis:"
        else -> "Days of the Week:"
    }

    fun timeLabel(lang: String): String = when (lang) {
        "pt" -> "Horário do Lembrete:"
        "es" -> "Hora del Recordatorio:"
        "la" -> "Hora Monitionis:"
        else -> "Reminder Time:"
    }

    fun cancel(lang: String): String = when (lang) {
        "pt" -> "Cancelar"
        "es" -> "Cancelar"
        "la" -> "Cancellare"
        else -> "Cancel"
    }

    fun save(lang: String): String = when (lang) {
        "pt" -> "Salvar Lembrete"
        "es" -> "Guardar Recordatorio"
        "la" -> "Servare Monitionem"
        else -> "Save Reminder"
    }

    fun ok(lang: String): String = when (lang) {
        "pt" -> "OK"
        "es" -> "OK"
        "la" -> "OK"
        else -> "OK"
    }

    fun delete(lang: String): String = when (lang) {
        "pt" -> "Excluir"
        "es" -> "Eliminar"
        "la" -> "Delere"
        else -> "Delete"
    }

    fun enableNotificationsBanner(lang: String): String = when (lang) {
        "pt" -> "Ative as notificações para receber os lembretes de oração."
        "es" -> "Active las notificaciones para recibir recordatorios de oración."
        "la" -> "Activa monitiones para accipere precationes."
        else -> "Enable notifications to receive prayer reminders."
    }

    fun permissionTitle(lang: String): String = when (lang) {
        "pt" -> "Permissão de Notificação"
        "es" -> "Permiso de Notificación"
        "la" -> "Permissio Monitionis"
        else -> "Notification Permission"
    }

    fun permissionText(lang: String): String = when (lang) {
        "pt" -> "Permita que o Oratio envie lembretes de oração mesmo com o app fechado."
        "es" -> "Permita que Oratio envíe recordatorios de oración incluso con la aplicación cerrada."
        "la" -> "Permitte Oratio monitiones precationis mittere."
        else -> "Allow Oratio to send prayer reminders even when the app is closed."
    }

    fun allow(lang: String): String = when (lang) {
        "pt" -> "Permitir Notificações"
        "es" -> "Permitir Notificaciones"
        "la" -> "Permittere Monitiones"
        else -> "Allow Notifications"
    }
}
