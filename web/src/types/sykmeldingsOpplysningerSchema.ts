import {z} from 'zod'

const PeriodeSchema = z.object({
    fom: z.string(),
    tom: z.string(),
})
const RuleStatusSchema = z.union([z.literal('MANUAL_PROCESSING'), z.literal('OK'), z.literal('INVALID')])

const BehandlingsutfallSchema = z.object({
    status: z.string(),
    ruleHits: z.array(z.object({
        ruleName: z.string(),
        ruleStatus: RuleStatusSchema,
        messageForUser: z.string(),
        messageForSender: z.string(),
    }))
})

const MerknadSchema = z.object({
    type: z.string(),
    beskrivelse: z.string(),
})


const SykmeldingSchema = z.object({
    sykmeldingId: z.string(),
    fnr: z.string(),
    mottakId: z.string(),
    mottattTidspunkt: z.string(),
    statusEvent: z.string(),
    merknader: z.array(MerknadSchema),
    behandlingsutfall: BehandlingsutfallSchema,
    tssId: z.string(),
    perioder: z.array(PeriodeSchema),

})


export const SykmeldingsOpplysningerSchema = z.object({
    sykmeldinger: z.array(SykmeldingSchema),
    fnr: z.string(),
})

export type SykmeldingsOpplysninger = z.infer<typeof SykmeldingsOpplysningerSchema>
