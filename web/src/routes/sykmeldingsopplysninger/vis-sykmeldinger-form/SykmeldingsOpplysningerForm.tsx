import { ReactElement, useState } from 'react'
import {
    Arbeidsgiver,
    HovedDiagnose,
    Merknad,
    Periode,
    RuleInfo,
    SykmeldingsOpplysninger,
    SykmeldingStatus,
} from '../../../types/sykmeldingsOpplysningerSchema.ts'
import { Timeline } from '@navikt/ds-react'
import { PersonIcon, VirusIcon } from '@navikt/aksel-icons'

interface SykmeldingsOpplysningerProps {
    person: SykmeldingsOpplysninger
}

const SykmeldingsOpplysningerForm = ({ person }: SykmeldingsOpplysningerProps): ReactElement => {
    if (person.sykmeldinger.length == 0) {
        return <div>Ingen sykmeldinger funnet</div>
    } else {
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

        for (let i = 0; i < sortedSykmeldinger.length; i++) {
            const sistePeriodeSlutt = sortedSykmeldinger[i].perioder[sortedSykmeldinger[i].perioder.length - 1].tom
            if (i === 0) {
                timelineRows.push(mainRow as TimelineRow)
            } else {
                // If it overlaps with the main row, create a new timeline row for this period
                if (new Date(sortedSykmeldinger[i].perioder[0].fom).getTime() <= new Date(mainRow.end).getTime()) {
                    timelineRows.push({
                        start: sortedSykmeldinger[i].perioder[0].fom,
                        end: sistePeriodeSlutt,
                        sykmeldinger: [sortedSykmeldinger[i]],
                    } as TimelineRow)
                } else {
                    // If it doesn't overlap with the main row, add it to the main row
                    mainRow.sykmeldinger.push(sortedSykmeldinger[i])
                    mainRow.end = sistePeriodeSlutt
                }
            }
        }

        return (
            <div className="min-w-[800px]">
                <Timeline>
                    {timelineRows.map((row, index) => (
                        <Timeline.Row
                            label={`Overlappende sykmeldinger ${index + 1}`}
                            icon={<PersonIcon aria-hidden />}
                        >
                            {row.sykmeldinger.map((sykmelding) => (
                                <Timeline.Period
                                    start={new Date(sykmelding.perioder[0].fom)}
                                    end={new Date(sykmelding.perioder[sykmelding.perioder.length - 1].tom)}
                                    status={sykmelding.synligStatus as StatusType}
                                    icon={
                                        <div>
                                            <VirusIcon aria-hidden />
                                        </div>
                                    }
                                    children={
                                        <div>
                                            <b>Sykmelding med id: {sykmelding.sykmeldingId} </b>
                                            <ul className={'list-style-type: none;'}>
                                                <li>
                                                    <b>Perioder er følgende:</b>{' '}
                                                    {sykmelding.perioder.map((periode, periodindex) => (
                                                        <div key={periodindex} style={{ paddingLeft: '20px' }}>
                                                            <p>
                                                                <b>fom</b> = {periode.fom}
                                                            </p>
                                                            <p>
                                                                <b>tom</b> = {periode.tom}
                                                            </p>
                                                        </div>
                                                    ))}
                                                </li>
                                                <li>
                                                    <b>Behandlingsutfall status er</b>{' '}
                                                    {sykmelding.behandlingsUtfall.status}
                                                </li>
                                            </ul>
                                        </div>
                                    }
                                    onSelectPeriod={() => setActivePeriod(sykmelding)}
                                    isActive={activePeriod === sykmelding}
                                    aria-controls={'timeline-panel'}
                                    id={sykmelding.sykmeldingId}
                                ></Timeline.Period>
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
                    <div className="mt-8" aria-controls={activePeriod.sykmeldingId} id={'timeline-panel'}>
                        <h2>Details for sykmelding with id: {activePeriod.sykmeldingId}</h2>
                        <br />
                        <li>
                            <b>mottatt tidspunkt</b> = {activePeriod.mottattTidspunkt}
                        </li>
                        <ul className={'list-style-type: none;'}>
                            <li>
                                <b>sykmeldingId</b> = {activePeriod.sykmeldingId}
                            </li>
                            <li>
                                <b>Perioder er følgende:</b>{' '}
                                {activePeriod.perioder.map((periode, periodindex) => (
                                    <div key={periodindex} style={{ paddingLeft: '20px' }}>
                                        <p>
                                            <b>fom</b> = {periode.fom}
                                        </p>
                                        <p>
                                            <b>tom</b> = {periode.tom}
                                        </p>
                                    </div>
                                ))}
                            </li>
                            <li>
                                <b>sykmeldingId</b> = {activePeriod.sykmeldingId}
                            </li>
                            <li>
                                <b>mottakId</b> = {activePeriod.mottakId}
                            </li>
                            <li>
                                <b>TssId</b> = {activePeriod.tssId}
                            </li>
                            <li>
                                <b>Merknader er følgende:</b>{' '}
                                {activePeriod.merknader?.map((merknad, merknadindex) => (
                                    <div key={merknadindex} style={{ paddingLeft: '20px' }}>
                                        <p>
                                            <b>type</b> = {merknad.type}{' '}
                                        </p>
                                        <p>
                                            <b>beskrivelse</b> = {merknad.beskrivelse}
                                        </p>
                                    </div>
                                ))}
                            </li>
                            <li>
                                <b>Behandlingsutfall status er</b> {activePeriod.behandlingsUtfall?.status}
                            </li>
                            <li>
                                <b>Behandlingsutfall regler:</b>
                                {activePeriod.behandlingsUtfall.ruleHits?.map((ruleHit, index) => (
                                    <div key={index} style={{ paddingLeft: '20px' }}>
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
                            </li>
                            <li>
                                <b>StatusEvent(bruker innsendingstatus):</b>
                                <div style={{ paddingLeft: '20px' }}>
                                    <p>
                                        <b>Status: </b> {activePeriod.statusEvent.status}
                                    </p>
                                    <p>
                                        <b>tidspunkt:</b> {activePeriod.statusEvent.timestamp}
                                    </p>
                                </div>
                            </li>
                            <li>
                                <b>Arbeidsgiver:</b>
                                <div style={{ paddingLeft: '20px' }}>
                                    <p>
                                        <b>Orgnummer:</b> {activePeriod.arbeidsgiver?.orgnummer}
                                    </p>
                                    <p>
                                        <b>OrgNavn:</b> {activePeriod.arbeidsgiver?.orgNavn}
                                    </p>
                                </div>
                            </li>
                            <li>
                                <b>Hoveddiagnose:</b>
                                <div style={{ paddingLeft: '20px' }}>
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
                            </li>
                        </ul>
                    </div>
                )}
            </div>
        )
    }
}

export default SykmeldingsOpplysningerForm
