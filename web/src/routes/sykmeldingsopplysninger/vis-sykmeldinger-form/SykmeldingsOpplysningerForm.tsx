import {ReactElement} from "react";
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
                                    <b>Sykmelding med id:  {sykmelding.sykmeldingId} </b>
                                    <ul className={"list-style-type: none;"}>
                                        <li><b>perioder er følgende</b> {sykmelding.perioder.map((periode, periodindex) =>
                                            <div key={periodindex} style={{paddingLeft: '20px'}}>
                                                <p><b>fom</b> = {periode.fom}</p>
                                                <p><b>tom</b> = {periode.tom}</p>
                                            </div>
                                        )}</li>
                                        <li><b>sykmeldingId</b> = {sykmelding.sykmeldingId}</li>
                                        <li><b>mottakId</b> = {sykmelding.mottakId}</li>
                                        <li><b>TssId</b> = {sykmelding.tssId}</li>
                                        <li><b>Merknader er
                                            følgende:</b> {sykmelding.merknader.map((merknad, merknadindex) => (
                                            <div key={merknadindex} style={{paddingLeft: '20px'}}>
                                                <p><b>type</b> = {merknad.type} </p>
                                                <p><b>beskrivelse</b> = {merknad.beskrivelse}</p>
                                            </div>
                                        ))}
                                        </li>
                                        <li><b>Behandlingsutfall status er</b> {sykmelding.behandlingsUtfall.status}
                                        </li>
                                        <li><b>Behandlingsutfall regler:</b>
                                            {sykmelding.behandlingsUtfall.ruleHits.map((ruleHit, index) => (
                                                <div key={index} style={{paddingLeft: '20px'}}>
                                                    <p><b>Rule Name:</b> {ruleHit.ruleName}</p>
                                                    <p><b>Rule Status:</b> {ruleHit.ruleStatus}</p>
                                                    <p><b>Message For User:</b> {ruleHit.messageForUser}</p>
                                                    <p><b>Message For Sender:</b> {ruleHit.messageForSender}</p>
                                                </div>
                                            ))}
                                        </li>
                                    </ul>
                                </div>}
                            >
                                {/*{p.children ?? null}*/}
                            </Timeline.Period>
                        ))}
                    </Timeline.Row>
                ))}
            </Timeline>
        </div>
    )
        ;
};

export default SykmeldingsOpplysningerForm;
