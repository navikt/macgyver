import { z } from 'zod'

const finnNarmesteldere = z.object({
    sykmeldtFnr: z.string(),
})

export type FinnNarmesteldere = z.infer<typeof finnNarmesteldere>
