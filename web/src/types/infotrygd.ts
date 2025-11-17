import { z } from 'zod'

export const InfotrygdQuery = z.object({
    ident: z.string(),
    hoveddiagnose: z.string().nullable(),
    hoveddiagnoseKodeSystem: z.string().nullable(),
    bidiagnose: z.string().nullable(),
    bidiagnoseKodeSystem: z.string().nullable(),
    tkNummer: z.string().nullable(),
    identBehandler: z.string().nullable(),
    detailed: z.boolean().default(false),
})

export type InfotrygdQueryType = z.infer<typeof InfotrygdQuery>

export const InfotrygdResponse = z.object({
    identDato: z.string().nullish(),
    traceId: z.string().nullish(),
    tkNummer: z.string().nullish(),
    response: z.string().nullish(),
})

export type InfotrygdResponseType = z.infer<typeof InfotrygdResponse>
