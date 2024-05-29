import { z } from 'zod'

export const IdentEndringSykmeldtPayloadSchema = z.object({
    fnr: z.string(),
    nyttFnr: z.string(),
})

export type IdentEndringSykmeldtPayload = z.infer<typeof IdentEndringSykmeldtPayloadSchema>
