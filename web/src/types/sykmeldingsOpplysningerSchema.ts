import {z} from 'zod'

export const PeriodeSchema = z.object({
    fom: z.string(),
    tom: z.string(),
})

export const RuleStatusSchema = z.union([z.literal('MANUAL_PROCESSING'), z.literal('OK'), z.literal('INVALID')])

export const RuleHitsSchema = z.object({
    ruleName: z.string(),
    messageForSender: z.string(),
    messageForUser: z.string(),
    ruleStatus: RuleStatusSchema,
})


export const BehandlingsutfallSchema = z.object({
    status: z.string(),
    ruleHits: z.array(RuleHitsSchema)
})

export const MerknadSchema = z.object({
    type: z.string(),
    beskrivelse: z.string(),
})

export const ArbeidsgiverSchema = z.object({
    orgnummer: z.string(),
    orgNavn: z.string(),
})

export const HovedDiagnoseSchema = z.object({
    kode: z.string(),
    system: z.string(),
    tekst: z.string().nullable(),
})


export const SykmeldingSchema = z.object({
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

export type Merknad = z.infer<typeof MerknadSchema>
export type RuleInfo = z.infer<typeof RuleHitsSchema>
export type Periode = z.infer<typeof PeriodeSchema>
export type Arbeidsgiver = z.infer<typeof ArbeidsgiverSchema>
export type HovedDiagnose = z.infer<typeof HovedDiagnoseSchema>
