import { z } from 'zod'

export const NyNLAltinnSchema = z.object({
    sykmeldingId: z.string().min(10, 'Ser ikke ut som en sykmeldingId tbh.'),
    fnr: z.string().length(11, 'Fødselsnummer må være 11 siffer'),
    orgnummer: z.string().min(3, 'Ser ikke ut som et orgnummer tbh.'),
})

export type NyNLAltinn = z.infer<typeof NyNLAltinnSchema>
