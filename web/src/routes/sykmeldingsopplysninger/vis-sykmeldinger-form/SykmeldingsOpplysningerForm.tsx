import { ReactElement, useState } from 'react'
import {
    Arbeidsgiver,
    HovedDiagnose,
    Merknad,
    Periode,
    RuleInfo,
    SykmeldingsOpplysninger,
    SykmeldingStatus,
    TidligereArbeidsgiver,
} from '../../../types/sykmeldingsOpplysningerSchema.ts'
import { Timeline } from '@navikt/ds-react'
import { PersonIcon, VirusIcon } from '@navikt/aksel-icons'

interface SykmeldingsOpplysningerProps {
    person: SykmeldingsOpplysninger
}

const SykmeldingsOpplysningerForm = ({ person }: SykmeldingsOpplysningerProps): ReactElement => {
    if (person.sykmeldinger.length === 0) {
        return <div>Ingen sykmeldinger funnet</div>
    }

    const sortedSykmeldinger = [...person.sykmeldinger].sort(
        (a, b) => Date.parse(a.perioder[0].fom) - Date.parse(b.perioder[0].fom),
    )

    interface TimelineRow {
        start: string
        end: string
        sykmeldinger: Sykmelding[]
    }

    const timelineRows: TimelineRow[] = []

    type StatusType = 'success' | 'warning' | 'danger' | 'info' | 'neutral' | undefined

    interface Sykmelding {
        sykmeldingId: string
        merknader: Merknad[]
        tssId: string
        statusEvent: SykmeldingStatus
        mottakId: string
        mottattTidspunkt: string
        behandlingsUtfall: BehandlingsUtfall
        perioder: Periode[]
        synligStatus: string
        arbeidsgiver: Arbeidsgiver
        hovedDiagnose: HovedDiagnose
        tidligereArbeidsgiver: TidligereArbeidsgiver
    }

    interface BehandlingsUtfall {
        status: string
        ruleHits: RuleInfo[]
    }

    let mainRow = {
        start: sortedSykmeldinger[0]?.perioder[0].fom,
        end: sortedSykmeldinger[0]?.perioder[sortedSykmeldinger[0].perioder.length - 1].tom,
        sykmeldinger: [sortedSykmeldinger[0]],
    }

    const [activePeriod, setActivePeriod] = useState<Sykmelding | null>(null)

    const handlePeriodClick = (sykmelding: Sykmelding) => {
        if (activePeriod === sykmelding) {
            setActivePeriod(null)
        } else {
            setActivePeriod(sykmelding)
        }
    }

    for (let i = 0; i < sortedSykmeldinger.length; i++) {
        const sistePeriodeSlutt = sortedSykmeldinger[i].perioder[sortedSykmeldinger[i].perioder.length - 1].tom
        if (i === 0) {
            timelineRows.push(mainRow as TimelineRow)
        } else {
            if (new Date(sortedSykmeldinger[i].perioder[0].fom).getTime() <= new Date(mainRow.end).getTime()) {
                timelineRows.push({
                    start: sortedSykmeldinger[i].perioder[0].fom,
                    end: sistePeriodeSlutt,
                    sykmeldinger: [sortedSykmeldinger[i]],
                } as TimelineRow)
            } else {
                mainRow.sykmeldinger.push(sortedSykmeldinger[i])
                mainRow.end = sistePeriodeSlutt
            }
        }
    }

    return (
        <div className="min-w-[800px] p-6 bg-gray-50">
            <Timeline>
                {timelineRows.map((row, index) => (
                    <Timeline.Row
                        key={index}
                        label={`Overlappende sykmeldinger ${index + 1}`}
                        icon={<PersonIcon aria-hidden />}
                    >
                        {row.sykmeldinger.map((sykmelding) => (
                            <Timeline.Period
                                key={sykmelding.sykmeldingId}
                                start={new Date(sykmelding.perioder[0].fom)}
                                end={new Date(sykmelding.perioder[sykmelding.perioder.length - 1].tom)}
                                status={sykmelding.synligStatus as StatusType}
                                icon={
                                    <div>
                                        <VirusIcon aria-hidden />
                                    </div>
                                }
                                onSelectPeriod={() => handlePeriodClick(sykmelding)}
                                isActive={activePeriod === sykmelding}
                                aria-controls={'timeline-panel'}
                                id={sykmelding.sykmeldingId}
                            >
                                <div className="p-4 bg-white rounded shadow">
                                    <b>Sykmelding med id: {sykmelding.sykmeldingId} </b>
                                    <div className="mt-2">
                                        <b>Perioder:</b>
                                        {sykmelding.perioder.map((periode, periodindex) => (
                                            <div key={periodindex} className="ml-4">
                                                <p>
                                                    <b>Fra:</b> {periode.fom} <b>Til:</b> {periode.tom}
                                                </p>
                                            </div>
                                        ))}
                                    </div>
                                    <p className="mt-2">
                                        <b>Behandlingsutfall status:</b> {sykmelding.behandlingsUtfall.status}
                                    </p>
                                </div>
                            </Timeline.Period>
                        ))}
                    </Timeline.Row>
                ))}
                <Timeline.Zoom>
                    <Timeline.Zoom.Button label="3 mnd" interval="month" count={3} />
                    <Timeline.Zoom.Button label="7 mnd" interval="month" count={7} />
                    <Timeline.Zoom.Button label="9 mnd" interval="month" count={9} />
                    <Timeline.Zoom.Button label="1.5 år" interval="year" count={1.5} />
                </Timeline.Zoom>
            </Timeline>

            {activePeriod && (
                <div
                    className="mt-8 bg-white p-6 rounded-lg shadow-md"
                    aria-controls={activePeriod.sykmeldingId}
                    id={'timeline-panel'}
                >
                    <h2 className="text-xl font-bold">Detaljer for sykmelding med ID: {activePeriod.sykmeldingId}</h2>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
                        <div>
                            <p>
                                <b>Mottatt tidspunkt:</b> {activePeriod.mottattTidspunkt}
                            </p>
                            <p>
                                <b>SykmeldingId:</b> {activePeriod.sykmeldingId}
                            </p>
                            <p>
                                <b>MottakId:</b> {activePeriod.mottakId}
                            </p>
                            <p>
                                <b>TssId:</b> {activePeriod.tssId}
                            </p>
                        </div>
                        <div>
                            <p>
                                <b>Status:</b> {activePeriod.statusEvent.status}
                            </p>
                            <p>
                                <b>Tidspunkt:</b> {activePeriod.statusEvent.timestamp}
                            </p>
                            {activePeriod.statusEvent.status === 'BEKREFTET' && (
                                <div>
                                    <p>
                                        <b>Tidligere Arbeidsgiver Orgnummer:</b>{' '}
                                        {activePeriod.tidligereArbeidsgiver?.orgnummer}
                                    </p>
                                    <p>
                                        <b>Tidligere Arbeidsgiver Navn:</b>{' '}
                                        {activePeriod.tidligereArbeidsgiver?.orgNavn}
                                    </p>
                                </div>
                            )}
                            {activePeriod.statusEvent.status === 'SENDT' && (
                                <div>
                                    <p>
                                        <b>Arbeidsgiver Orgnummer:</b> {activePeriod.arbeidsgiver?.orgnummer}
                                    </p>
                                    <p>
                                        <b>Arbeidsgiver Navn:</b> {activePeriod.arbeidsgiver?.orgNavn}
                                    </p>
                                </div>
                            )}
                        </div>
                    </div>

                    <div className="mt-6">
                        <h3 className="text-lg font-semibold">Perioder:</h3>
                        <div className="grid grid-cols-2 gap-4 mt-2">
                            {activePeriod.perioder.map((periode, periodindex) => (
                                <div key={periodindex} className="bg-gray-100 p-4 rounded-md shadow-sm">
                                    <p>
                                        <b>Fra:</b> {periode.fom}
                                    </p>
                                    <p>
                                        <b>Til:</b> {periode.tom}
                                    </p>
                                </div>
                            ))}
                        </div>
                    </div>

                    <div className="mt-6">
                        <h3 className="text-lg font-semibold">Merknader:</h3>
                        <div className="grid grid-cols-1 gap-4 mt-2">
                            {activePeriod.merknader?.map((merknad, merknadindex) => (
                                <div key={merknadindex} className="bg-gray-100 p-4 rounded-md shadow-sm">
                                    <p>
                                        <b>Type:</b> {merknad.type}
                                    </p>
                                    <p>
                                        <b>Beskrivelse:</b> {merknad.beskrivelse}
                                    </p>
                                </div>
                            ))}
                        </div>
                    </div>

                    <div className="mt-6">
                        <h3 className="text-lg font-semibold">Behandlingsutfall:</h3>
                        <p>
                            <b>Status:</b> {activePeriod.behandlingsUtfall?.status}
                        </p>
                        <h4 className="text-md font-medium mt-4">Regler:</h4>
                        <div className="grid grid-cols-1 gap-4 mt-2">
                            {activePeriod.behandlingsUtfall.ruleHits?.map((ruleHit, index) => (
                                <div key={index} className="bg-gray-100 p-4 rounded-md shadow-sm">
                                    <p>
                                        <b>Rule Name:</b> {ruleHit?.ruleName}
                                    </p>
                                    <p>
                                        <b>Rule Status:</b> {ruleHit?.ruleStatus}
                                    </p>
                                    <p>
                                        <b>Message For User:</b> {ruleHit?.messageForUser}
                                    </p>
                                    <p>
                                        <b>Message For Sender:</b> {ruleHit?.messageForSender}
                                    </p>
                                </div>
                            ))}
                        </div>
                    </div>

                    <div className="mt-6">
                        <h3 className="text-lg font-semibold">Hoveddiagnose:</h3>
                        <div className="bg-gray-100 p-4 rounded-md shadow-sm">
                            <p>
                                <b>Kode:</b> {activePeriod.hovedDiagnose?.kode}
                            </p>
                            <p>
                                <b>System:</b> {activePeriod.hovedDiagnose?.system}
                            </p>
                            <p>
                                <b>Tekst:</b> {activePeriod.hovedDiagnose?.tekst}
                            </p>
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}

export default SykmeldingsOpplysningerForm
