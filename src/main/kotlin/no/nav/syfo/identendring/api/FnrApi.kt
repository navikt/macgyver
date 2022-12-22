package no.nav.syfo.identendring.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.syfo.application.HttpMessage
import no.nav.syfo.identendring.UpdateFnrService
import no.nav.syfo.identendring.UpdateIdentException
import no.nav.syfo.sykmelding.api.model.EndreFnr
import no.nav.syfo.utils.getAccessTokenFromAuthHeader
import no.nav.syfo.utils.logNAVEpostAndActionToSecureLog

fun Route.registerFnrApi(updateFnrService: UpdateFnrService) {
    post("/api/sykmelding/fnr") {
        val endreFnr = call.receive<EndreFnr>()
        when {
            endreFnr.fnr.length != 11 || endreFnr.fnr.any { !it.isDigit() } -> {
                // Hvis fnr ikke er et tall på 11 tegn så er det antakeligvis noe rart som har skjedd,
                // og vi bør undersøke ytterligere
                call.respond(HttpStatusCode.BadRequest, HttpMessage("fnr må være et fnr / dnr på 11 tegn"))
                return@post
            }

            endreFnr.nyttFnr.length != 11 || endreFnr.nyttFnr.any { !it.isDigit() } -> {
                call.respond(HttpStatusCode.BadRequest, HttpMessage("nyttFnr må være et fnr / dnr på 11 tegn"))
                return@post
            }

            else -> {
                try {
                    logNAVEpostAndActionToSecureLog(
                        getAccessTokenFromAuthHeader(call.request),
                        "enderer fnr for sykmeldt fra: ${endreFnr.fnr} til: ${endreFnr.nyttFnr}"
                    )

                    val updateFnr = updateFnrService.updateFnr(fnr = endreFnr.fnr, nyttFnr = endreFnr.nyttFnr)

                    if (updateFnr) {
                        call.respond(HttpStatusCode.OK, HttpMessage("Vellykket oppdatering."))
                    } else {
                        call.respond(HttpStatusCode.NotModified, HttpMessage("Ingenting ble endret."))
                    }
                } catch (e: UpdateIdentException) {
                    call.respond(HttpStatusCode.InternalServerError, HttpMessage(e.message))
                }
            }
        }
    }

    post("/api/leder/fnr") {
        val endreFnr = call.receive<EndreFnr>()
        when {
            endreFnr.fnr.length != 11 || endreFnr.fnr.any { !it.isDigit() } -> {
                // Hvis fnr ikke er et tall på 11 tegn så er det antakeligvis noe rart som har skjedd,
                // og vi bør undersøke ytterligere
                call.respond(HttpStatusCode.BadRequest, HttpMessage("fnr må være et fnr / dnr på 11 tegn"))
                return@post
            }

            endreFnr.nyttFnr.length != 11 || endreFnr.nyttFnr.any { !it.isDigit() } -> {
                call.respond(HttpStatusCode.BadRequest, HttpMessage("nyttFnr må være et fnr / dnr på 11 tegn"))
                return@post
            }

            else -> {
                try {
                    logNAVEpostAndActionToSecureLog(
                        getAccessTokenFromAuthHeader(call.request),
                        "enderer fnr for leder fra: ${endreFnr.fnr} til: ${endreFnr.nyttFnr}"
                    )

                    val updateNlKoblinger = updateFnrService.updateNlFnr(fnr = endreFnr.fnr, nyttFnr = endreFnr.nyttFnr)

                    if (updateNlKoblinger) {
                        call.respond(HttpStatusCode.OK, HttpMessage("Vellykket oppdatering."))
                    } else {
                        call.respond(HttpStatusCode.NotModified, HttpMessage("Ingenting ble endret."))
                    }
                } catch (e: UpdateIdentException) {
                    call.respond(HttpStatusCode.InternalServerError, e.message)
                }
            }
        }
    }
}
