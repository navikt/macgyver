import { z } from 'zod'

export const InfotrygdQuery = z.object({
    ident: z.string(),
    hoveddiagnose: z.string().nullable(),
    hoveddiagnoseKodeSystem: z.string().nullable(),
    bidiagnose: z.string().nullable(),
    bidiagnoseKodeSystem: z.string().nullable(),
    tkNummer: z.string().nullable(),
    identBehandler: z.string().nullable(),
})

export type InfotrygdQueryType = z.infer<typeof InfotrygdQuery>

export const InfotrygdResponse = z.object({
    identDato: z.string(),
    traceId: z.string(),
    tkNummer: z.string().nullable(),
})

export type InfotrygdResponseType = z.infer<typeof InfotrygdResponse>
