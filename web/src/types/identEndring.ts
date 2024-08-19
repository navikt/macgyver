import { z } from 'zod'

export const IdentEndringSykmeldtPayloadSchema = z.object({
    fnr: z.string().length(11, 'fnr må være 11 tegn'),
    nyttFnr: z.string().length(11, 'fnr må være 11 tegn'),
})

export type IdentEndringSykmeldtPayload = z.infer<typeof IdentEndringSykmeldtPayloadSchema>
