import { z } from 'zod'

export const NarmestelederSchema = z.object({
    narmesteLederId: z.string(),
    fnr: z.string(),
    narmesteLederFnr: z.string(),
    orgnummer: z.string(),
    narmesteLederTelefonnummer: z.string(),
    narmesteLederEpost: z.string(),
    aktivFom: z.string(),
    aktivTom: z.string().nullable(),
    arbeidsgiverForskutterer: z.boolean().nullable(),
})

export type Narmesteleder = z.infer<typeof NarmestelederSchema>
