import { z } from 'zod'

const finnNarmesteledere = z.object({
    sykmeldtFnr: z.string(),
})

export type FinnNarmesteledere = z.infer<typeof finnNarmesteledere>
