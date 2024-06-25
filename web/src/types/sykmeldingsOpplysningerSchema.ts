import {z} from 'zod'

const PeriodeSchema = z.object({
    fom: z.string(),
    tom: z.string(),
})
const RuleStatusSchema = z.union([z.literal('MANUAL_PROCESSING'), z.literal('OK'), z.literal('INVALID')])

const RuleHitsSchema = z.object({
    ruleName: z.string(),
    messageForSender: z.string(),
    messageForUser: z.string(),
    ruleStatus: RuleStatusSchema,
})


const BehandlingsutfallSchema = z.object({
    status: z.string(),
    ruleHits: z.array(RuleHitsSchema)
})

const MerknadSchema = z.object({
    type: z.string(),
    beskrivelse: z.string(),
})

const ArbeidsgiverSchema = z.object({
    orgnummer: z.string(),
    orgNavn: z.string(),
})
const HovedDiagnoseSchema = z.object({
    kode: z.string(),
    system: z.string(),
    tekst: z.string().nullable(),
})


const SykmeldingSchema = z.object({
    sykmeldingId: z.string(),
    merknader: z.array(MerknadSchema),
    tssId: z.string(),
    statusEvent: z.string(),
    mottakId: z.string(),
    mottattTidspunkt: z.string(),
    behandlingsUtfall: BehandlingsutfallSchema,
    perioder: z.array(PeriodeSchema),
    synligStatus: z.string(),
    arbeidsgiver: ArbeidsgiverSchema,
    hovedDiagnose: HovedDiagnoseSchema,

})


export const SykmeldingsOpplysningerSchema = z.object({
    fnr: z.string(),
    sykmeldinger: z.array(SykmeldingSchema),
})

export type SykmeldingsOpplysninger = z.infer<typeof SykmeldingsOpplysningerSchema>
