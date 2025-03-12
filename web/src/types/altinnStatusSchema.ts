import { z } from 'zod'

const StatusChangesSchema = z.object({
    date: z.string(),
    type: z.string(),
})

export const AltinnStatusSchema = z.object({
    correspondenceId: z.string(),
    createdDate: z.string(),
    orgnummer: z.string(),
    sendersReference: z.string(),
    statusChanges: z.array(StatusChangesSchema),
})

export type AltinnStatus = z.infer<typeof AltinnStatusSchema>
