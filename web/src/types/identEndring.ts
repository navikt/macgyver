import { z } from 'zod'

const IdentEndringSykmeldt = z.object({
    fnr: z.string(),
    nyttFnr: z.string(),
})

export type IdentEndringSykmeldt = z.infer<typeof IdentEndringSykmeldt>
