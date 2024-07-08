import { z } from 'zod'

const finnNarmesteLedere = z.object({
    sykmeldtFnr: z.string(),
})

export type FinnNarmesteLedere = z.infer<typeof finnNarmesteLedere>
