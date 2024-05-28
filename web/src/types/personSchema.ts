import { z } from 'zod'

const IdentSchema = z.object({
    ident: z.string(),
    historisk: z.boolean(),
    gruppe: z.string(),
})

export const PersonSchema = z.object({
    identer: z.array(IdentSchema),
    navn: z.string().nullable(),
    fnr: z.string(),
})

export type Person = z.infer<typeof PersonSchema>
