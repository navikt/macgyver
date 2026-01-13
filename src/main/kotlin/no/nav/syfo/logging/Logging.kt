package no.nav.syfo.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger("no.nav.syfo.macgyver")
val auditlogg: Logger = LoggerFactory.getLogger("auditLogger")
inline fun <reified T> T.teamLogger(): Logger =
    LoggerFactory.getLogger("teamlog.${T::class.java.name}")
