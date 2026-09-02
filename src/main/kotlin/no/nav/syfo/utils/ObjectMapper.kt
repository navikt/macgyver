package no.nav.syfo.utils

import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.jacksonMapperBuilder

 val jsonMapper: JsonMapper =
    jacksonMapperBuilder()
        .enable(
            tools.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT
        )
        .enable(
            tools.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT
        )
        .build()
