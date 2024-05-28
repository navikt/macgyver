import { z } from 'zod'

const NyNLAltinn = z.object({
    sykmeldingId: z.string(),
    fnr: z.string(),
    orgnummer: z.string(),
})

export type NyNLAltinn = z.infer<typeof NyNLAltinn>
