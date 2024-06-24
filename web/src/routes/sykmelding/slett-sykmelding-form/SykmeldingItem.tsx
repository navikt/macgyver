import {ReactElement} from "react";

import {BodyShort} from "@navikt/ds-react";

interface SykmeldingProps {
    fnr: string,
    sykmeldingId: string,
    mottattTidspunkt: string,
    mottakId: string,
    statusEvent: string,
    merknader: MerknadProps[],
    behandlingsutfall: BehandlingsutfallProps,
    tssid: string,
    perioder: PeriodeProps[]
}

const SykmeldingItem = ({fnr, sykmeldingId, mottattTidspunkt, mottakId, statusEvent, merknader, behandlingsutfall, tssid,  perioder}: SykmeldingProps): ReactElement => {
    return (
        <li className="flex border-b border-gray-600 py-1 max-[1030px]:flex-col">
            <BodyShort className="min-w-[15rem] font-bold">
                {fnr}
            </BodyShort>
            <BodyShort>{sykmeldingId}</BodyShort>
            <BodyShort>{mottattTidspunkt}</BodyShort>
            <BodyShort>{mottakId}</BodyShort>
            <BodyShort>{statusEvent}</BodyShort>
            <BodyShort>{tssid}</BodyShort>

            {merknader.map((merknad) => {
                return <MerknadItem
                    type={merknad.type}
                    beskrivelse={merknad.beskrivelse}>
                </MerknadItem>;
            })},

            <BodyShort>{behandlingsutfall.status}</BodyShort>
            {behandlingsutfall.ruleHits.map((ruleHit) => {
                return (
                    <div>
                        <BodyShort>{ruleHit.ruleName}</BodyShort>
                        <BodyShort>{ruleHit.ruleStatus}</BodyShort>
                        <BodyShort>{ruleHit.messageForUser}</BodyShort>
                        <BodyShort>{ruleHit.messageForSender}</BodyShort>
                    </div>
                );
            })},

            {perioder.map((periode) => {
                return <PeriodeItem
                    fom={periode.fom}
                    tom={periode.tom}>
                </PeriodeItem>;
            })},
        </li>
    );
};


export default SykmeldingItem;

interface MerknadProps {
    type: string,
    beskrivelse: string,
}

interface PeriodeProps {
    fom: string,
    tom: string,
}

interface RuleHitProps {
    ruleName: string,
    ruleStatus: string,
    messageForUser: string,
    messageForSender: string,
}

interface BehandlingsutfallProps {
    status: string,
    ruleHits: RuleHitProps[],
}

const PeriodeItem = ({fom, tom}: PeriodeProps) : ReactElement => {
    return (
    <li className="flex border-b border-gray-600 py-1 max-[1030px]:flex-col">
        <BodyShort className="min-w-[15rem] font-bold">
            {fom} {tom}
        </BodyShort>
    </li>
    )
}

const MerknadItem = ({type, beskrivelse}: MerknadProps) : ReactElement => {
    return (
        <li className="flex border-b border-gray-600 py-1 max-[1030px]:flex-col">
            <BodyShort className="min-w-[15rem] font-bold">
                {type} {beskrivelse}
            </BodyShort>
        </li>
    )
}

