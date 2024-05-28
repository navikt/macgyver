import { z } from 'zod'

const narmesteldere = z.object({
    fnr: z.string(),
    narmesteLederFnr: z.string(),
    orgnummer: z.string(),
    narmesteLederTelefonnummer: z.string(),
    narmesteLederEpost: z.string(),
    aktivFom: z.string(),
    aktivTom: z.string().nullable(),
    arbeidsgiverForskutterer: z.boolean().nullable(),
})

export type Narmesteldere = z.infer<typeof narmesteldere>
