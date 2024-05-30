package no.nav.syfo.identendring

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.identendring.update_fnr.UpdateFnrService
import no.nav.syfo.identendring.update_fnr.UpdateIdentException
import no.nav.syfo.logging.AuditLogger
import no.nav.syfo.logging.auditlogg
import no.nav.syfo.logging.sikkerlogg
import no.nav.syfo.model.HttpMessage
import no.nav.syfo.utils.safePrincipal
import org.koin.ktor.ext.inject

fun Route.registerFnrApi() {
    val updateFnrService by inject<UpdateFnrService>()

    post("/sykmelding/fnr") {
        val endreFnr = call.receive<EndreFnrPayload>()
        when {
            endreFnr.fnr.length != 11 || endreFnr.fnr.any { !it.isDigit() } -> {
                // Hvis fnr ikke er et tall på 11 tegn så er det antakeligvis noe rart som har
                // skjedd, og vi bør undersøke ytterligere
                call.respond(
                    HttpStatusCode.BadRequest,
                    HttpMessage("fnr må være et fnr / dnr på 11 tegn"),
                )
                return@post
            }
            endreFnr.nyttFnr.length != 11 || endreFnr.nyttFnr.any { !it.isDigit() } -> {
                call.respond(
                    HttpStatusCode.BadRequest,
                    HttpMessage("nyttFnr må være et fnr / dnr på 11 tegn"),
                )
                return@post
            }
            else -> {
                try {
                    val principal = call.safePrincipal()
                    auditlogg.info(
                        AuditLogger(principal.email)
                            .createcCefMessage(
                                fnr = endreFnr.fnr,
                                operation = AuditLogger.Operation.WRITE,
                                requestPath = "/api/sykmelding/fnr",
                                permit = AuditLogger.Permit.PERMIT,
                            ),
                    )
                    sikkerlogg.info(
                        "enderer fnr for sykmeldt fra: ${endreFnr.fnr} til: ${endreFnr.nyttFnr}",
                    )

                    val updateFnr =
                        updateFnrService.updateFnr(fnr = endreFnr.fnr, nyttFnr = endreFnr.nyttFnr)

                    if (updateFnr) {
                        call.respond(HttpStatusCode.OK, HttpMessage("Vellykket oppdatering."))
                    } else {
                        call.respond(
                            HttpStatusCode.NotModified,
                            HttpMessage("Ingenting ble endret."),
                        )
                    }
                } catch (e: UpdateIdentException) {
                    call.respond(HttpStatusCode.InternalServerError, HttpMessage(e.message))
                }
            }
        }
    }

    post("/api/leder/fnr") {
        val endreFnr = call.receive<EndreFnrPayload>()
        when {
            endreFnr.fnr.length != 11 || endreFnr.fnr.any { !it.isDigit() } -> {
                // Hvis fnr ikke er et tall på 11 tegn så er det antakeligvis noe rart som har
                // skjedd,
                // og vi bør undersøke ytterligere
                call.respond(
                    HttpStatusCode.BadRequest,
                    HttpMessage("fnr må være et fnr / dnr på 11 tegn"),
                )
                return@post
            }
            endreFnr.nyttFnr.length != 11 || endreFnr.nyttFnr.any { !it.isDigit() } -> {
                call.respond(
                    HttpStatusCode.BadRequest,
                    HttpMessage("nyttFnr må være et fnr / dnr på 11 tegn"),
                )
                return@post
            }
            else -> {
                try {
                    val principal = call.safePrincipal()
                    auditlogg.info(
                        AuditLogger(principal.email)
                            .createcCefMessage(
                                fnr = endreFnr.fnr,
                                operation = AuditLogger.Operation.WRITE,
                                requestPath = "/api/leder/fnr",
                                permit = AuditLogger.Permit.PERMIT,
                            ),
                    )

                    sikkerlogg.info(
                        "enderer fnr for leder fra: ${endreFnr.fnr} til: ${endreFnr.nyttFnr}",
                    )

                    val updateNlKoblinger =
                        updateFnrService.updateNlFnr(fnr = endreFnr.fnr, nyttFnr = endreFnr.nyttFnr)

                    if (updateNlKoblinger) {
                        call.respond(HttpStatusCode.OK, HttpMessage("Vellykket oppdatering."))
                    } else {
                        call.respond(
                            HttpStatusCode.NotModified,
                            HttpMessage("Ingenting ble endret."),
                        )
                    }
                } catch (e: UpdateIdentException) {
                    call.respond(HttpStatusCode.InternalServerError, e.message)
                }
            }
        }
    }
}

data class EndreFnrPayload(
    val fnr: String,
    val nyttFnr: String,
)
