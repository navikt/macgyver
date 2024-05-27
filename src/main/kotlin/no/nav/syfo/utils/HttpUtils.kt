package no.nav.syfo.utils

import io.ktor.server.application.*
import io.ktor.server.auth.*
import no.nav.syfo.plugins.UserPrincipal

fun ApplicationCall.safePrincipal(): UserPrincipal =
    this.principal<UserPrincipal>()
        ?: throw IllegalStateException(
            "No principal found in call, this should not happen! Why isn't auth authing?"
        )
