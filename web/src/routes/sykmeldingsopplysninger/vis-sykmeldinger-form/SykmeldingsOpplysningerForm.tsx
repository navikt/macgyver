import {ReactElement, useState} from "react";
import {SykmeldingsOpplysninger} from "../../../types/sykmeldingsOpplysningerSchema.ts";
import {Timeline} from "@navikt/ds-react";
import {
    PersonIcon, VirusIcon,
} from "@navikt/aksel-icons";

interface SykmeldingsOpplysningerProps {
    person: SykmeldingsOpplysninger;
}

const SykmeldingsOpplysningerForm = ({person}: SykmeldingsOpplysningerProps): ReactElement => {

    // Sort the periods by their start date
    // @ts-ignore
    const sortedSykmeldinger = [...person.sykmeldinger].sort((a, b) => a.perioder[0].fom - b.perioder[0].fom);

// Initialize an array to hold the timeline rows
    const timelineRows = [];

    type StatusType = "success" | "warning" | "danger" | "info" | "neutral" | undefined;

    // Initialize the main row
    let mainRow = null;


    interface Sykmelding {
        sykmeldingId: string;
        merknader: Merknad[];
        tssId: string;
        statusEvent: string;
        mottakId: string;
        mottattTidspunkt: string;
        behandlingsUtfall: BehandlingsUtfall;
        perioder: Periode[];
        synligStatus: string;
        arbeidsgiver: Arbeidsgiver;
        hovedDiagnose: HovedDiagnose;
    }

    interface Merknad {
        type: string;
        beskrivelse: string;
    }

    interface BehandlingsUtfall {
        status: string;
        ruleHits: RuleInfo[];
    }

    interface RuleInfo {
        ruleName: string;
        ruleStatus: string;
        messageForUser: string;
        messageForSender: string;
    }

    interface Periode {
        fom: string;
        tom: string;
    }

    interface Arbeidsgiver {
        orgnummer: string;
        orgNavn: string;
    }

    interface HovedDiagnose {
        kode: string;
        system: string;
        tekst: string | null;
    }

// Now you can use the Sykmelding interface as the type for your state
    const [activePeriod, setActivePeriod] = useState<Sykmelding | null>(null);
    // Add this line to create the activePeriod state variable
    // const [activePeriod, setActivePeriod] = useState("");
// Iterate over the sorted periods

    for (let i = 0; i < sortedSykmeldinger.length; i++) {
        // If it's the first period, add it to the main row
        const sistePeriodeSlutt = sortedSykmeldinger[i].perioder[sortedSykmeldinger[i].perioder.length - 1].tom
        if (i === 0) {
            mainRow = {
                start: sortedSykmeldinger[i].perioder[0].fom,
                end: sistePeriodeSlutt,
                sykmeldinger: [sortedSykmeldinger[i]]
            };
            timelineRows.push(mainRow);
        } else {
            // If it overlaps with the main row, create a new timeline row for this period
            // @ts-ignore
            if (new Date(sortedSykmeldinger[i].perioder[0].fom).getTime() <= new Date(mainRow.end).getTime()) {
                timelineRows.push({
                    start: sortedSykmeldinger[i].perioder[0].fom,
                    end: sistePeriodeSlutt,
                    sykmeldinger: [sortedSykmeldinger[i]]
                });
            } else {
                // If it doesn't overlap with the main row, add it to the main row
                // @ts-ignore
                mainRow.sykmeldinger.push(sortedSykmeldinger[i]);
                // @ts-ignore
                mainRow.end = sistePeriodeSlutt;
            }
        }
    }

// Now you can map over timelineRows to create the Timeline.Row components
    // @ts-ignore
    // @ts-ignore
    return (
        <div className="min-w-[800px]">
            <Timeline>
                {timelineRows.map((row, index) => (
                    <Timeline.Row label={`Overlappende sykmeldinger ${index + 1}`} icon={<PersonIcon aria-hidden/>}>
                        {row.sykmeldinger.map((sykmelding) => (
                            <Timeline.Period
                                start={new Date(sykmelding.perioder[0].fom)}
                                end={new Date(sykmelding.perioder[sykmelding.perioder.length - 1].tom)}
                                status={sykmelding.synligStatus as StatusType}
                                icon={<div><VirusIcon aria-hidden/></div>}
                                children={<div>
                                    <b>Sykmelding med id: {sykmelding.sykmeldingId} </b>
                                    <ul className={"list-style-type: none;"}>
                                        <li><b>periode er
                                            følgende</b> {sykmelding.perioder.map((periode, periodindex) =>
                                            <div key={periodindex} style={{paddingLeft: '20px'}}>
                                                <p><b>fom</b> = {periode.fom}</p>
                                                <p><b>tom</b> = {periode.tom}</p>
                                            </div>
                                        )}</li>
                                        {/*<li><b>sykmeldingId</b> = {sykmelding.sykmeldingId}</li>*/}
                                        {/*<li><b>mottakId</b> = {sykmelding.mottakId}</li>*/}
                                        {/*<li><b>TssId</b> = {sykmelding.tssId}</li>*/}
                                        {/*<li><b>Merknader er*/}
                                        {/*    følgende:</b> {sykmelding.merknader.map((merknad, merknadindex) => (*/}
                                        {/*    <div key={merknadindex} style={{paddingLeft: '20px'}}>*/}
                                        {/*        <p><b>type</b> = {merknad.type} </p>*/}
                                        {/*        <p><b>beskrivelse</b> = {merknad.beskrivelse}</p>*/}
                                        {/*    </div>*/}
                                        {/*))}*/}
                                        {/*</li>*/}
                                        <li><b>Behandlingsutfall status er</b> {sykmelding.behandlingsUtfall.status}
                                        </li>
                                        {/*<li><b>Behandlingsutfall regler:</b>*/}
                                        {/*    {sykmelding.behandlingsUtfall.ruleHits.map((ruleHit, index) => (*/}
                                        {/*        <div key={index} style={{paddingLeft: '20px'}}>*/}
                                        {/*            <p><b>Rule Name:</b> {ruleHit.ruleName}</p>*/}
                                        {/*            <p><b>Rule Status:</b> {ruleHit.ruleStatus}</p>*/}
                                        {/*            <p><b>Message For User:</b> {ruleHit.messageForUser}</p>*/}
                                        {/*            <p><b>Message For Sender:</b> {ruleHit.messageForSender}</p>*/}
                                        {/*        </div>*/}
                                        {/*    ))}*/}
                                        {/*</li>*/}
                                        {/*<li><b>Arbeidsgiver orgNavn:</b> {sykmelding.arbeidsgiver.orgNavn}</li>*/}
                                        {/*<li><b>Arbeidsgiver orgnummer:</b> {sykmelding.arbeidsgiver.orgnummer}</li>*/}
                                        {/*<li><b>Hoveddiagnose</b> {sykmelding.hovedDiagnose.kode}</li>*/}
                                    </ul>
                                </div>}
                                onSelectPeriod={() => setActivePeriod(sykmelding)}
                                isActive={activePeriod === sykmelding.sykmeldingId}
                                aria-controls={"timeline-panel"}
                                id={sykmelding.sykmeldingId}
                            >
                                {/*{p.children ?? null}*/}
                            </Timeline.Period>
                        ))}
                    </Timeline.Row>
                ))}
                <Timeline.Zoom>
                    <Timeline.Zoom.Button label="3 mnd" interval="month" count={3}/>
                    <Timeline.Zoom.Button label="7 mnd" interval="month" count={7}/>
                    <Timeline.Zoom.Button label="9 mnd" interval="month" count={9}/>
                    <Timeline.Zoom.Button label="1.5 år" interval="year" count={1.5}/>
                </Timeline.Zoom>
            </Timeline>
            {/* Clickable periods */}
            {activePeriod && (
                <div className="mt-8" aria-controls={activePeriod.sykmeldingId} id={"timeline-panel"}>
                    <h2>Details for sykmelding with id: {activePeriod.sykmeldingId}</h2>
                    {/* Add the details you want to display here... TODO */}
                    <br/>

                    <ul className={"list-style-type: none;"}>
                        <li><b>perioder er
                            følgende</b> {activePeriod.perioder.map((periode, periodindex) =>
                            <div key={periodindex} style={{paddingLeft: '20px'}}>
                                <p><b>fom</b> = {periode.fom}</p>
                                <p><b>tom</b> = {periode.tom}</p>
                            </div>
                        )}</li>
                        <li><b>sykmeldingId</b> = {activePeriod.sykmeldingId}</li>
                        <li><b>mottakId</b> = {activePeriod.mottakId}</li>
                        <li><b>TssId</b> = {activePeriod.tssId}</li>
                        <li><b>Merknader er
                            følgende:</b> {activePeriod.merknader.map((merknad, merknadindex) => (
                            <div key={merknadindex} style={{paddingLeft: '20px'}}>
                                <p><b>type</b> = {merknad.type} </p>
                                <p><b>beskrivelse</b> = {merknad.beskrivelse}</p>
                            </div>
                        ))}
                        </li>
                        <li><b>Behandlingsutfall status er</b> {activePeriod.behandlingsUtfall.status}
                        </li>
                        <li><b>Behandlingsutfall regler:</b>
                            {activePeriod.behandlingsUtfall.ruleHits.map((ruleHit, index) => (
                                <div key={index} style={{paddingLeft: '20px'}}>
                                    <p><b>Rule Name:</b> {ruleHit.ruleName}</p>
                                    <p><b>Rule Status:</b> {ruleHit.ruleStatus}</p>
                                    <p><b>Message For User:</b> {ruleHit.messageForUser}</p>
                                    <p><b>Message For Sender:</b> {ruleHit.messageForSender}</p>
                                </div>
                            ))}
                        </li>
                        <li><b>StatusEvent(bruker innsendingstatus):</b> {activePeriod.statusEvent}</li>
                        <li><b>Arbeidsgiver:</b>
                            <div style={{paddingLeft: '20px'}}>
                                <p><b>Orgnummer:</b> {activePeriod.arbeidsgiver.orgnummer}</p>
                                <p><b>OrgNavn:</b> {activePeriod.arbeidsgiver.orgNavn}</p>
                            </div>
                        </li>
                        <li><b>Hoveddiagnose:</b>
                            <div style={{paddingLeft: '20px'}}>
                                <p><b>Kode:</b> {activePeriod.hovedDiagnose.kode}</p>
                                <p><b>System:</b> {activePeriod.hovedDiagnose.system}</p>
                                <p><b>Tekst:</b> {activePeriod.hovedDiagnose.tekst}</p>
                            </div>
                        </li>
                    </ul>
                </div>
            )}
        </div>
    )
        ;
};

export default SykmeldingsOpplysningerForm;
