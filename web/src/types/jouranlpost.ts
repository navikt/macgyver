import { z } from 'zod'

const Jouranlpost = z.object({
    journalpostId: z.string(),
    tittel: z.string(),
})

export type Jouranlpost = z.infer<typeof Jouranlpost>
