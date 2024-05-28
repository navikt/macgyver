import { z } from 'zod'

const OppgaverField = z.array(z.number())

export type OppgaverField = z.infer<typeof OppgaverField>

const Oppgave = z.object({
    oppgavetype: z.string(),
    mappeId: z.number().nullable(),
    prioritet: z.string(),
    opprettetAvEnhetsnr: z.string().nullable(),
    fristFerdigstillelse: z.string().nullable(),
    aktivDato: z.string(),
    aktoerId: z.string().nullable(),
    versjon: z.number().nullable(),
    tildeltEnhetsnr: z.string().nullable(),
    saksreferanse: z.string().nullable(),
    behandlingstype: z.string().nullable(),
    tilordnetRessurs: z.string().nullable(),
    tema: z.string().nullable(),
    behandlesAvApplikasjon: z.string().nullable(),
    id: z.number().nullable(),
    beskrivelse: z.string().nullable(),
    journalpostId: z.string().nullable(),
    status: z.string().nullable(),
})

export type Oppgave = z.infer<typeof Oppgave>
